mod server;

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
/// 启动tokio运行时线程，绑定端口，开始accept连接。
/// 每个请求通过FFI callback同步调用Java处理。
///
/// 返回不透明指针，用于后续停止服务器。
#[no_mangle]
pub extern "C" fn rust_server_start(port: u16, response_capacity: i32, callback: RequestCallback) -> *mut c_void {
    let (shutdown_tx, shutdown_rx) = oneshot::channel::<()>();

    std::thread::Builder::new()
        .name("rust-tokio-runtime".to_string())
        .spawn(move || {
            let rt = match tokio::runtime::Runtime::new() {
                Ok(r) => r,
                Err(e) => {
                    eprintln!("[rust-server] Failed to create tokio runtime: {}", e);
                    return;
                }
            };
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

/// 停止Rust HTTP服务器
///
/// 发送关闭信号，tokio运行时优雅退出。
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
