use std::cell::RefCell;
use std::collections::HashMap;
use std::future::Future;
use std::net::SocketAddr;
use std::pin::Pin;
use std::sync::atomic::{AtomicI64, Ordering};
use std::sync::{Arc, LazyLock, Mutex};
use std::time::Duration;
use std::io::BufReader;

use bytes::Bytes;
use hyper::body::Incoming;
use hyper::service::Service;
use hyper::{Request, Response, StatusCode};
use hyper_util::rt::TokioIo;
use http_body_util::{BodyExt, Full};
use tokio::net::TcpListener;
use tokio::sync::oneshot;
use tokio_util::sync::CancellationToken;
use tokio::task::JoinSet;
use rustls::pki_types::CertificateDer;
use rustls::ServerConfig;
use tokio_rustls::TlsAcceptor;
use tokio_tungstenite::accept_async;
use futures_util::StreamExt;
use futures_util::SinkExt;

use crate::ws;
use crate::RequestCallback;

// 线程本地缓存：复用请求头缓冲区
thread_local! {
    static HEADERS_BUF: RefCell<Vec<u8>> = RefCell::new(Vec::new());
}

/// 异步 FFI 模式下，等待 Java 响应的注册表
///
/// Java 线程池处理完请求后调用 rust_send_response()，
/// 通过此注册表找到对应的 oneshot Sender，通知 Rust Future 读取响应。
struct PendingRequest {
    response: Vec<u8>,
    sender: Option<oneshot::Sender<Vec<u8>>>,
}

static PENDING: LazyLock<Mutex<HashMap<i64, PendingRequest>>> =
    LazyLock::new(|| Mutex::new(HashMap::new()));

/// 全局递增请求 ID，每个 FFI 调用分配唯一标识
static NEXT_REQUEST_ID: AtomicI64 = AtomicI64::new(1);

/// 启动HTTP服务器事件循环（支持优雅关闭）
pub async fn run(
    port: u16,
    response_capacity: i32,
    callback: RequestCallback,
    shutdown_rx: oneshot::Receiver<()>,
) {
    let addr = SocketAddr::from(([0, 0, 0, 0], port));

    let listener = match TcpListener::bind(addr).await {
        Ok(l) => l,
        Err(e) => {
            eprintln!("[rust-server] Failed to bind port {}: {}", port, e);
            return;
        }
    };

    eprintln!("[rust-server] Listening on {}", addr);

    let cancel = CancellationToken::new();
    let mut join_set: JoinSet<()> = JoinSet::new();

    tokio::select! {
        _ = shutdown_rx => {
            eprintln!("[rust-server] Shutting down gracefully...");
        }
        result = serve(listener, response_capacity, callback, cancel.clone(), &mut join_set) => {
            if let Err(e) = result {
                eprintln!("[rust-server] Server error: {}", e);
            }
        }
    }

    // 停止接受新连接
    cancel.cancel();

    // 等待所有 in-flight 连接完成（带超时）
    eprintln!("[rust-server] Waiting for {} active connections...", join_set.len());
    tokio::select! {
        _ = join_set.shutdown() => {
            eprintln!("[rust-server] All connections finished");
        }
        _ = tokio::time::sleep(Duration::from_secs(30)) => {
            eprintln!("[rust-server] Graceful shutdown timeout, forcing exit");
        }
    }
    eprintln!("[rust-server] Server stopped");
}

/// 启动 TLS（HTTPS）HTTP 服务器事件循环
pub async fn run_tls(
    port: u16,
    response_capacity: i32,
    callback: RequestCallback,
    shutdown_rx: oneshot::Receiver<()>,
    cert_pem: &[u8],
    key_pem: &[u8],
) {
    let addr = SocketAddr::from(([0, 0, 0, 0], port));

    let tls_config = match build_tls_config(cert_pem, key_pem) {
        Ok(c) => Arc::new(c),
        Err(e) => {
            eprintln!("[rust-server] Failed to build TLS config: {}", e);
            return;
        }
    };
    let tls_acceptor = TlsAcceptor::from(tls_config.clone());

    let listener = match TcpListener::bind(addr).await {
        Ok(l) => l,
        Err(e) => {
            eprintln!("[rust-server] Failed to bind TLS port {}: {}", port, e);
            return;
        }
    };

    eprintln!("[rust-server] Listening on {} (TLS)", addr);

    let cancel = CancellationToken::new();
    let mut join_set: JoinSet<()> = JoinSet::new();

    tokio::select! {
        _ = shutdown_rx => {
            eprintln!("[rust-server] Shutting down gracefully (TLS)...");
        }
        result = serve_tls(listener, tls_acceptor, response_capacity, callback, cancel.clone(), &mut join_set) => {
            if let Err(e) = result {
                eprintln!("[rust-server] TLS server error: {}", e);
            }
        }
    }

    // 停止接受新连接
    cancel.cancel();

    // 等待所有 in-flight 连接完成
    eprintln!("[rust-server] Waiting for {} active connections (TLS)...", join_set.len());
    tokio::select! {
        _ = join_set.shutdown() => {
            eprintln!("[rust-server] All TLS connections finished");
        }
        _ = tokio::time::sleep(Duration::from_secs(30)) => {
            eprintln!("[rust-server] TLS graceful shutdown timeout, forcing exit");
        }
    }
    eprintln!("[rust-server] TLS server stopped");
}

fn build_tls_config(cert_pem: &[u8], key_pem: &[u8]) -> Result<ServerConfig, Box<dyn std::error::Error>> {
    let certs: Vec<CertificateDer> = rustls_pemfile::certs(&mut BufReader::new(cert_pem))
        .collect::<Result<Vec<_>, _>>()?;

    let key = rustls_pemfile::private_key(&mut BufReader::new(key_pem))?
        .ok_or("No private key found")?;

    let config = ServerConfig::builder()
        .with_no_client_auth()
        .with_single_cert(certs, key)?;

    Ok(config)
}

async fn serve_tls(
    listener: TcpListener,
    tls_acceptor: TlsAcceptor,
    response_capacity: i32,
    callback: RequestCallback,
    cancel: CancellationToken,
    join_set: &mut JoinSet<()>,
) -> Result<(), Box<dyn std::error::Error + Send + Sync>> {
    loop {
        tokio::select! {
            _ = cancel.cancelled() => {
                eprintln!("[rust-server] Stop accepting new TLS connections");
                break;
            }
            result = listener.accept() => {
                let (stream, peer) = match result {
                    Ok(s) => s,
                    Err(e) => {
                        eprintln!("[rust-server] TLS accept error: {}, continuing...", e);
                        continue;
                    }
                };

                eprintln!("[rust-server] Accepted TLS connection from {}", peer);

                let acceptor = tls_acceptor.clone();
                join_set.spawn(async move {
                    let tls_stream = match acceptor.accept(stream).await {
                        Ok(s) => s,
                        Err(e) => {
                            eprintln!("[rust-server] TLS handshake error: {}", e);
                            return;
                        }
                    };
                    let io = TokioIo::new(tls_stream);
                    let svc = RustService {
                        response_capacity,
                        callback,
                    };

                    if let Err(err) = hyper::server::conn::http1::Builder::new()
                        .serve_connection(io, svc)
                        .await
                    {
                        eprintln!("[rust-server] TLS connection error: {}", err);
                    }
                });
            }
        }
    }

    Ok(())
}

async fn serve(
    listener: TcpListener,
    response_capacity: i32,
    callback: RequestCallback,
    cancel: CancellationToken,
    join_set: &mut JoinSet<()>,
) -> Result<(), Box<dyn std::error::Error + Send + Sync>> {
    loop {
        tokio::select! {
            _ = cancel.cancelled() => {
                eprintln!("[rust-server] Stop accepting new connections");
                break;
            }
            result = listener.accept() => {
                let (stream, peer) = match result {
                    Ok(s) => s,
                    Err(e) => {
                        eprintln!("[rust-server] Accept error: {}, continuing...", e);
                        continue;
                    }
                };

                eprintln!("[rust-server] Accepted connection from {}", peer);

                let io = TokioIo::new(stream);
                let svc = RustService {
                    response_capacity,
                    callback,
                };

                join_set.spawn(async move {
                    if let Err(err) = hyper::server::conn::http1::Builder::new()
                        .serve_connection(io, svc)
                        .await
                    {
                        eprintln!("[rust-server] Connection error: {}", err);
                    }
                });
            }
        }
    }

    Ok(())
}

/// hyper Service实现：接收请求 → 异步调用Java callback → 等待响应
/// 支持 WebSocket 升级
struct RustService {
    response_capacity: i32,
    callback: RequestCallback,
}

impl Service<Request<Incoming>> for RustService {
    type Response = Response<Full<Bytes>>;
    type Error = hyper::Error;
    type Future = Pin<Box<dyn Future<Output = Result<Self::Response, Self::Error>> + Send>>;

    fn call(&self, req: Request<Incoming>) -> Self::Future {
        let callback = self.callback;
        let response_capacity = self.response_capacity;

        let (parts, body) = req.into_parts();

        // 检测 WebSocket 升级请求
        let is_ws = parts.headers.get("upgrade")
            .and_then(|v| v.to_str().ok())
            .map(|v| v.to_lowercase().contains("websocket"))
            .unwrap_or(false);

        if is_ws {
            return Box::pin(async move {
                let mut req = Request::from_parts(parts, body);
                match hyper::upgrade::on(&mut req).await {
                    Ok(upgraded) => {
                        let conn_id = NEXT_REQUEST_ID.fetch_add(1, Ordering::Relaxed);
                        let (_, rx) = ws::global_manager().register();

                        // 通知 Java 新 WebSocket 连接已建立
                        if let Some(ws_cb) = ws::get_ws_callback() {
                            ws_cb(conn_id, std::ptr::null(), 0, ws::WS_OPEN);
                        }

                        // 启动 WebSocket 处理任务
                        tokio::spawn(async move {
                            let io = TokioIo::new(upgraded);
                            let ws_stream = match accept_async(io).await {
                                Ok(stream) => stream,
                                Err(e) => {
                                    eprintln!("[rust-server] WS accept error (id={}): {}", conn_id, e);
                                    if let Some(ws_cb) = ws::get_ws_callback() {
                                        ws_cb(conn_id, std::ptr::null(), 0, ws::WS_ERROR);
                                    }
                                    ws::global_manager().remove(conn_id);
                                    return;
                                }
                            };
                            eprintln!("[rust-server] WebSocket opened (id={})", conn_id);

                            let (mut write, mut read) = ws_stream.split();
                            let mut rx = rx;
                            let conn_id2 = conn_id;

                            loop {
                                tokio::select! {
                                    msg = read.next() => {
                                        match msg {
                                            Some(Ok(tokio_tungstenite::tungstenite::Message::Text(text))) => {
                                                if let Some(ws_cb) = ws::get_ws_callback() {
                                                    let bytes = text.as_bytes();
                                                    ws_cb(conn_id2, bytes.as_ptr(), bytes.len() as i32, ws::WS_TEXT);
                                                }
                                            }
                                            Some(Ok(tokio_tungstenite::tungstenite::Message::Binary(data))) => {
                                                if let Some(ws_cb) = ws::get_ws_callback() {
                                                    ws_cb(conn_id2, data.as_ptr(), data.len() as i32, ws::WS_BINARY);
                                                }
                                            }
                                            Some(Ok(tokio_tungstenite::tungstenite::Message::Close(_))) => {
                                                if let Some(ws_cb) = ws::get_ws_callback() {
                                                    ws_cb(conn_id2, std::ptr::null(), 0, ws::WS_CLOSE);
                                                }
                                                break;
                                            }
                                            Some(Err(e)) => {
                                                eprintln!("[rust-server] WS error (id={}): {}", conn_id2, e);
                                                if let Some(ws_cb) = ws::get_ws_callback() {
                                                    ws_cb(conn_id2, std::ptr::null(), 0, ws::WS_ERROR);
                                                }
                                                break;
                                            }
                                            None => break,
                                            _ => {}
                                        }
                                    }
                                    msg = rx.recv() => {
                                        match msg {
                                            Some(ws_msg) => {
                                                let result = if ws_msg.msg_type == ws::WS_TEXT {
                                                    let text = String::from_utf8_lossy(&ws_msg.data);
                                                    write.send(tokio_tungstenite::tungstenite::Message::Text(text.into_owned())).await
                                                } else {
                                                    write.send(tokio_tungstenite::tungstenite::Message::Binary(ws_msg.data.into())).await
                                                };
                                                if let Err(e) = result {
                                                    eprintln!("[rust-server] WS send error (id={}): {}", conn_id2, e);
                                                    break;
                                                }
                                            }
                                            None => break,
                                        }
                                    }
                                }
                            }

                            ws::global_manager().remove(conn_id2);
                            eprintln!("[rust-server] WebSocket closed (id={})", conn_id2);
                            if let Some(ws_cb) = ws::get_ws_callback() {
                                ws_cb(conn_id2, std::ptr::null(), 0, ws::WS_CLOSE);
                            }
                        });

                        let mut resp = Response::new(Full::new(Bytes::new()));
                        *resp.status_mut() = StatusCode::SWITCHING_PROTOCOLS;
                        Ok(resp)
                    }
                    Err(e) => {
                        eprintln!("[rust-server] WebSocket upgrade failed: {}", e);
                        let mut resp = Response::new(Full::new(Bytes::from("WebSocket upgrade failed")));
                        *resp.status_mut() = StatusCode::BAD_REQUEST;
                        Ok(resp)
                    }
                }
            });
        }

        // ── 异步 FFI 请求处理 ──
        Box::pin(async move {
            // 读取完整请求体
            let body_bytes = match BodyExt::collect(body).await {
                Ok(collected) => collected.to_bytes(),
                Err(_) => {
                    let mut resp = Response::new(Full::new(Bytes::from("Failed to read body")));
                    *resp.status_mut() = StatusCode::BAD_REQUEST;
                    return Ok(resp);
                }
            };

            // 准备方法/路径 (零分配，从 parts 借用)
            let method_bytes = parts.method.as_str().as_bytes();
            let path_str = parts.uri.path_and_query()
                .map(|pq| pq.as_str())
                .unwrap_or("/");
            let path_bytes = path_str.as_bytes();

            // 序列化请求头到线程本地缓冲区
            let (hbuf_ptr, hbuf_len) = HEADERS_BUF.with(|cell| {
                let mut buf = cell.borrow_mut();
                buf.clear();
                for (name, value) in parts.headers.iter() {
                    let name_bytes = name.as_str().as_bytes();
                    let val_bytes = value.as_bytes();
                    buf.extend_from_slice(&(name_bytes.len() as u16).to_be_bytes());
                    buf.extend_from_slice(name_bytes);
                    buf.extend_from_slice(&(val_bytes.len() as u16).to_be_bytes());
                    buf.extend_from_slice(val_bytes);
                }
                (buf.as_ptr(), buf.len())
            });

            // ── Async FFI: 每请求分配独立响应缓冲区 ──
            let cap = if response_capacity > 0 { response_capacity as usize } else { 65536 };
            let request_id = NEXT_REQUEST_ID.fetch_add(1, Ordering::Relaxed);

            let (tx, rx) = oneshot::channel::<Vec<u8>>();

            // 预分配响应缓冲区，获取原始指针
            let mut response_buf = vec![0u8; cap];
            let buf_ptr = response_buf.as_mut_ptr();

            // 存入全局注册表（Vec 所有权移入 map，但 buf_ptr 仍指向堆内存）
            PENDING.lock().unwrap().insert(request_id, PendingRequest {
                response: response_buf,
                sender: Some(tx),
            });

            // 同步调用 Java upcall——Java 提交到线程池后立即返回
            callback(
                request_id,
                method_bytes.as_ptr(),
                method_bytes.len() as i32,
                path_bytes.as_ptr(),
                path_bytes.len() as i32,
                hbuf_ptr,
                hbuf_len as i32,
                body_bytes.as_ptr(),
                body_bytes.len() as i32,
                buf_ptr,
                cap as i32,
            );

            // 异步等待 Java 处理完成（不阻塞 tokio 工作线程）
            let response_data = match tokio::time::timeout(Duration::from_secs(30), rx).await {
                Ok(Ok(data)) => data,
                Ok(Err(_)) => {
                    // Java 处理异常（sender 被 dropped）
                    let _ = PENDING.lock().map(|mut g| g.remove(&request_id));
                    let mut resp = Response::new(Full::new(Bytes::from("Internal Server Error")));
                    *resp.status_mut() = StatusCode::INTERNAL_SERVER_ERROR;
                    return Ok(resp);
                }
                Err(_) => {
                    // 超时（默认30秒），清理注册表
                    let _ = PENDING.lock().map(|mut g| g.remove(&request_id));
                    let mut resp = Response::new(Full::new(Bytes::from("Gateway Timeout")));
                    *resp.status_mut() = StatusCode::GATEWAY_TIMEOUT;
                    return Ok(resp);
                }
            };

            // 从响应缓冲区解析 HTTP 响应
            if response_data.len() < 10 {
                let mut resp = Response::new(Full::new(Bytes::from("Invalid response")));
                *resp.status_mut() = StatusCode::INTERNAL_SERVER_ERROR;
                return Ok(resp);
            }

            let status = u16::from_be_bytes([response_data[0], response_data[1]]);
            let headers_len = i32::from_ne_bytes([
                response_data[2], response_data[3],
                response_data[4], response_data[5],
            ]) as usize;
            let body_len = i32::from_ne_bytes([
                response_data[6], response_data[7],
                response_data[8], response_data[9],
            ]) as usize;

            let total_needed = 10 + headers_len + body_len;
            if total_needed > response_data.len() {
                let mut resp = Response::new(Full::new(Bytes::from("Response too large")));
                *resp.status_mut() = StatusCode::INTERNAL_SERVER_ERROR;
                return Ok(resp);
            }

            // 构建 HTTP 响应
            let mut response = Response::new(Full::new(Bytes::copy_from_slice(
                &response_data[10 + headers_len..10 + headers_len + body_len],
            )));
            *response.status_mut() = StatusCode::from_u16(status).unwrap_or(StatusCode::INTERNAL_SERVER_ERROR);

            // 解析响应头（TLV 格式）
            let mut offset = 10;
            let headers_end = 10 + headers_len;

            while offset + 4 <= headers_end {
                let key_len = u16::from_be_bytes([response_data[offset], response_data[offset + 1]]) as usize;
                offset += 2;
                if offset + key_len + 2 > headers_end {
                    break;
                }
                let key = match std::str::from_utf8(&response_data[offset..offset + key_len]) {
                    Ok(k) => k,
                    Err(_) => break,
                };
                offset += key_len;

                let val_len = u16::from_be_bytes([response_data[offset], response_data[offset + 1]]) as usize;
                offset += 2;
                if offset + val_len > headers_end {
                    break;
                }
                let value = match std::str::from_utf8(&response_data[offset..offset + val_len]) {
                    Ok(v) => v,
                    Err(_) => break,
                };
                offset += val_len;

                if let (Ok(hname), Ok(hval)) = (
                    hyper::header::HeaderName::from_bytes(key.as_bytes()),
                    hyper::header::HeaderValue::from_str(value),
                ) {
                    response.headers_mut().append(hname, hval);
                }
            }

            Ok(response)
        })
    }
}

/// Java 线程池处理完成后调用此函数，通知 Rust Future 读取响应
pub fn rust_send_response(request_id: i64) {
    if let Some(mut pending) = PENDING.lock().unwrap().remove(&request_id) {
        if let Some(sender) = pending.sender.take() {
            let _ = sender.send(pending.response);
        }
    }
}
