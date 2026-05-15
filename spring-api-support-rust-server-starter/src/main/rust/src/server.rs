use std::future::Future;
use std::net::SocketAddr;
use std::pin::Pin;

use bytes::Bytes;
use hyper::body::Incoming;
use hyper::server::conn::http1;
use hyper::service::Service;
use hyper::{Request, Response, StatusCode};
use http_body_util::{BodyExt, Full};
use tokio::net::TcpListener;
use tokio::sync::oneshot;

use crate::RequestCallback;

/// 启动HTTP服务器事件循环
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

    tokio::select! {
        _ = shutdown_rx => {
            eprintln!("[rust-server] Shutting down (signal received)");
        }
        result = serve(listener, response_capacity, callback) => {
            if let Err(e) = result {
                eprintln!("[rust-server] Server error: {}", e);
            }
        }
    }
}

async fn serve(
    listener: TcpListener,
    response_capacity: i32,
    callback: RequestCallback,
) -> Result<(), Box<dyn std::error::Error + Send + Sync>> {
    let mut request_id: i64 = 0;

    loop {
        let (stream, peer) = listener.accept().await?;
        request_id += 1;
        let current_id = request_id;

        eprintln!("[rust-server] Accepted connection from {} (id={})", peer, current_id);

        tokio::spawn(async move {
            let svc = RustService {
                request_id: current_id,
                response_capacity,
                callback,
            };

            if let Err(err) = http1::Builder::new()
                .serve_connection(stream, svc)
                .await
            {
                eprintln!("[rust-server] Connection error (id={}): {}", current_id, err);
            }
        });
    }
}

/// hyper Service实现：接收请求 → 调用Java callback → 返回响应
struct RustService {
    request_id: i64,
    response_capacity: i32,
    callback: RequestCallback,
}

impl Service<Request<Incoming>> for RustService {
    type Response = Response<Full<Bytes>>;
    type Error = hyper::Error;
    type Future = Pin<Box<dyn Future<Output = Result<Self::Response, Self::Error>> + Send>>;

    fn call(&self, req: Request<Incoming>) -> Self::Future {
        let callback = self.callback;
        let request_id = self.request_id;
        let response_capacity = self.response_capacity;

        let (parts, body) = req.into_parts();

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

            // 准备方法
            let method = parts.method.as_str().as_bytes().to_vec();

            // 准备路径
            let path = parts
                .uri
                .path_and_query()
                .map(|pq| pq.as_str().as_bytes().to_vec())
                .unwrap_or_else(|| vec![b'/']);

            // 序列化请求头: [key_len:2BE][key][val_len:2BE][val]...
            let mut headers_buf = Vec::new();
            for (name, value) in parts.headers.iter() {
                let name_bytes = name.as_str().as_bytes();
                let val_bytes = value.as_bytes();
                headers_buf.extend_from_slice(&(name_bytes.len() as u16).to_be_bytes());
                headers_buf.extend_from_slice(name_bytes);
                headers_buf.extend_from_slice(&(val_bytes.len() as u16).to_be_bytes());
                headers_buf.extend_from_slice(val_bytes);
            }

            // 分配响应缓冲区 (堆分配)
            let cap = if response_capacity > 0 { response_capacity as usize } else { 65536 };
            let mut response_buf = vec![0u8; cap];

            // 同步调用Java callback
            // 注：此调用会阻塞当前tokio worker线程，但Java处理速度快时可接受
            callback(
                request_id,
                method.as_ptr(),
                method.len() as i32,
                path.as_ptr(),
                path.len() as i32,
                headers_buf.as_ptr(),
                headers_buf.len() as i32,
                body_bytes.as_ptr(),
                body_bytes.len() as i32,
                response_buf.as_mut_ptr(),
                response_buf.len() as i32,
            );

            // 解析响应状态码
            if response_buf.len() < 10 {
                let mut resp = Response::new(Full::new(Bytes::from("Invalid response")));
                *resp.status_mut() = StatusCode::INTERNAL_SERVER_ERROR;
                return Ok(resp);
            }

            let status = u16::from_be_bytes([response_buf[0], response_buf[1]]);
            let headers_len = i32::from_ne_bytes([
                response_buf[2], response_buf[3],
                response_buf[4], response_buf[5],
            ]) as usize;
            let body_len = i32::from_ne_bytes([
                response_buf[6], response_buf[7],
                response_buf[8], response_buf[9],
            ]) as usize;

            let total_needed = 10 + headers_len + body_len;
            if total_needed > response_buf.len() {
                let mut resp = Response::new(Full::new(Bytes::from("Response too large")));
                *resp.status_mut() = StatusCode::INTERNAL_SERVER_ERROR;
                return Ok(resp);
            }

            // 构建HTTP响应
            let mut response = Response::new(Full::new(Bytes::copy_from_slice(
                &response_buf[10 + headers_len..10 + headers_len + body_len],
            )));
            *response.status_mut() = StatusCode::from_u16(status).unwrap_or(StatusCode::INTERNAL_SERVER_ERROR);

            // 解析响应头
            let mut offset = 10;
            let headers_end = 10 + headers_len;

            while offset + 4 <= headers_end {
                let key_len = u16::from_be_bytes([response_buf[offset], response_buf[offset + 1]]) as usize;
                offset += 2;
                if offset + key_len + 2 > headers_end {
                    break;
                }
                let key = match std::str::from_utf8(&response_buf[offset..offset + key_len]) {
                    Ok(k) => k,
                    Err(_) => break,
                };
                offset += key_len;

                let val_len = u16::from_be_bytes([response_buf[offset], response_buf[offset + 1]]) as usize;
                offset += 2;
                if offset + val_len > headers_end {
                    break;
                }
                let value = match std::str::from_utf8(&response_buf[offset..offset + val_len]) {
                    Ok(v) => v,
                    Err(_) => break,
                };
                offset += val_len;

                if let (Ok(hname), Ok(hval)) = (
                    hyper::header::HeaderName::from_bytes(key.as_bytes()),
                    hyper::header::HeaderValue::from_str(value),
                ) {
                    response.headers_mut().insert(hname, hval);
                }
            }

            Ok(response)
        })
    }
}
