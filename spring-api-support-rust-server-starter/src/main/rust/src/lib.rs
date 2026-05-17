mod server;
pub mod ws;

use std::ffi::c_void;
use tokio::sync::oneshot;

/// 服务器不透明句柄
pub struct ServerHandle {
    shutdown_tx: Option<oneshot::Sender<()>>,
}

/// Java Upcall 回调类型
///
/// # 参数
/// - request_id: 请求递增ID
/// - method_ptr / method_len: HTTP方法 ("GET"/"POST"/...)
/// - path_ptr / path_len: 请求路径 (如 "/api/hello")
/// - headers_ptr / headers_len: TLV序列化请求头
/// - body_ptr / body_len: 请求体原始字节
/// - response_ptr / response_capacity: 响应缓冲区 (Java写入)
///
/// # 响应缓冲区布局
/// [0..1]   status: u16 大端
/// [2..5]   headers_len: i32 小端
/// [6..9]   body_len: i32 小端
/// [10..]   headers: TLV序列化 (key_len:u16BE + key + val_len:u16BE + val)
/// [10+headers_len..] body: 原始字节
pub type RequestCallback = extern "C" fn(
    i64,            // request_id
    *const u8, i32, // method, method_len
    *const u8, i32, // path, path_len
    *const u8, i32, // headers, headers_len
    *const u8, i32, // body, body_len
    *mut u8, i32,   // response_ptr, response_capacity
);

/// 启动Rust HTTP服务器
///
/// 启动tokio运行时线程（可配置worker线程数），绑定端口，开始accept连接。
/// 每个请求通过FFI callback同步调用Java处理。
///
/// 返回不透明指针，用于后续停止服务器。
#[no_mangle]
pub extern "C" fn rust_server_start(port: u16, response_capacity: i32, callback: RequestCallback, worker_threads: i32) -> *mut c_void {
    let (shutdown_tx, shutdown_rx) = oneshot::channel::<()>();

    std::thread::Builder::new()
        .name("rust-tokio-runtime".to_string())
        .spawn(move || {
            let thread_count = if worker_threads > 0 {
                worker_threads as usize
            } else {
                std::thread::available_parallelism()
                    .map(|n| n.get() * 2)
                    .unwrap_or(4)
            };
            let rt = match tokio::runtime::Builder::new_multi_thread()
                .worker_threads(thread_count)
                .enable_all()
                .build()
            {
                Ok(r) => r,
                Err(e) => {
                    eprintln!("[rust-server] Failed to create tokio runtime: {}", e);
                    return;
                }
            };
            eprintln!("[rust-server] Tokio runtime started with {} worker threads", thread_count);
            rt.block_on(async {
                server::run(port, response_capacity, callback, shutdown_rx).await;
            });
        })
        .expect("Failed to spawn tokio runtime thread");

    let handle = Box::new(ServerHandle {
        shutdown_tx: Some(shutdown_tx),
    });
    Box::into_raw(handle) as *mut c_void
}

/// 停止Rust HTTP服务器（立即停止）
///
/// 发送关闭信号，tokio运行时退出。
#[no_mangle]
pub extern "C" fn rust_server_stop(handle: *mut c_void) {
    if handle.is_null() {
        return;
    }
    let mut handle = unsafe { Box::from_raw(handle as *mut ServerHandle) };
    if let Some(tx) = handle.shutdown_tx.take() {
        let _ = tx.send(());
    }
}

/// 启动Rust TLS（HTTPS）HTTP服务器
///
/// 参数与 rust_server_start 相同，额外接受 PEM 证书和私钥路径。
#[no_mangle]
pub extern "C" fn rust_server_start_tls(
    port: u16,
    response_capacity: i32,
    callback: RequestCallback,
    worker_threads: i32,
    cert_path: *const u8,
    cert_len: i32,
    key_path: *const u8,
    key_len: i32,
) -> *mut c_void {
    let (shutdown_tx, shutdown_rx) = oneshot::channel::<()>();

    // 复制证书和密钥数据到 Rust 堆上
    let cert_data = if !cert_path.is_null() && cert_len > 0 {
        unsafe { std::slice::from_raw_parts(cert_path, cert_len as usize) }.to_vec()
    } else {
        eprintln!("[rust-server] TLS: cert_path is null or empty");
        return std::ptr::null_mut();
    };
    let key_data = if !key_path.is_null() && key_len > 0 {
        unsafe { std::slice::from_raw_parts(key_path, key_len as usize) }.to_vec()
    } else {
        eprintln!("[rust-server] TLS: key_path is null or empty");
        return std::ptr::null_mut();
    };

    std::thread::Builder::new()
        .name("rust-tokio-tls-runtime".to_string())
        .spawn(move || {
            let thread_count = if worker_threads > 0 {
                worker_threads as usize
            } else {
                std::thread::available_parallelism()
                    .map(|n| n.get() * 2)
                    .unwrap_or(4)
            };
            let rt = match tokio::runtime::Builder::new_multi_thread()
                .worker_threads(thread_count)
                .enable_all()
                .build()
            {
                Ok(r) => r,
                Err(e) => {
                    eprintln!("[rust-server] Failed to create tokio runtime for TLS: {}", e);
                    return;
                }
            };
            eprintln!("[rust-server] Tokio TLS runtime started with {} worker threads", thread_count);
            rt.block_on(async {
                server::run_tls(port, response_capacity, callback, shutdown_rx, &cert_data, &key_data).await;
            });
        })
        .expect("Failed to spawn tokio TLS runtime thread");

    let handle = Box::new(ServerHandle {
        shutdown_tx: Some(shutdown_tx),
    });
    Box::into_raw(handle) as *mut c_void
}

/// 优雅停止Rust HTTP服务器（带超时）
///
/// 发送关闭信号后等待 in-flight 请求完成，最多等待 timeout_ms 毫秒。
#[no_mangle]
pub extern "C" fn rust_server_stop_graceful(handle: *mut c_void, _timeout_ms: i32) {
    if handle.is_null() {
        return;
    }
    let mut handle = unsafe { Box::from_raw(handle as *mut ServerHandle) };
    if let Some(tx) = handle.shutdown_tx.take() {
        let _ = tx.send(());
    }
}

/// 注册 WebSocket 回调
///
/// Java 侧在初始化时调用，注册 WebSocket 事件回调。
#[no_mangle]
pub extern "C" fn rust_server_set_ws_callback(callback: ws::WsCallback) {
    ws::set_ws_callback(callback);
    eprintln!("[rust-server] WebSocket callback registered");
}

/// Java 线程池处理完请求后通知 Rust 响应已就绪
#[no_mangle]
pub extern "C" fn rust_send_response(request_id: i64) {
    server::rust_send_response(request_id);
}

/// 发送 WebSocket 消息
///
/// 从 Java 侧发送消息到指定 WebSocket 连接。
/// msg_type: 1=text, 2=binary
/// 返回值: 0=成功, -1=连接不存在
#[no_mangle]
pub extern "C" fn rust_ws_send(conn_id: i64, data: *const u8, data_len: i32, msg_type: i32) -> i32 {
    if data.is_null() || data_len <= 0 {
        return -1;
    }
    let bytes = unsafe { std::slice::from_raw_parts(data, data_len as usize) }.to_vec();
    if ws::global_manager().send(conn_id, ws::WsMessage { data: bytes, msg_type }) {
        0
    } else {
        -1
    }
}

/// 关闭 WebSocket 连接
#[no_mangle]
pub extern "C" fn rust_ws_close(conn_id: i64) {
    // 移除连接——发送端被 drop 后，WebSocket 读取任务会检测到并关闭
    ws::global_manager().remove(conn_id);
}
