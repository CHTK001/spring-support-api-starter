use std::collections::HashMap;
use std::sync::atomic::{AtomicI64, Ordering};
use std::sync::Mutex;
use std::sync::OnceLock;
use tokio::sync::mpsc;

/// WebSocket 消息类型常量
pub const WS_OPEN: i32 = 0;
pub const WS_TEXT: i32 = 1;
pub const WS_BINARY: i32 = 2;
pub const WS_CLOSE: i32 = 3;
pub const WS_ERROR: i32 = 4;

/// WebSocket 消息
pub struct WsMessage {
    pub data: Vec<u8>,
    pub msg_type: i32,
}

/// WebSocket 连接管理器（线程安全）
pub struct WsConnectionManager {
    senders: Mutex<HashMap<i64, mpsc::UnboundedSender<WsMessage>>>,
    next_id: AtomicI64,
}

impl WsConnectionManager {
    pub fn new() -> Self {
        WsConnectionManager {
            senders: Mutex::new(HashMap::new()),
            next_id: AtomicI64::new(1),
        }
    }

    /// 注册新连接，返回 conn_id 和消息接收器
    pub fn register(&self) -> (i64, mpsc::UnboundedReceiver<WsMessage>) {
        let conn_id = self.next_id.fetch_add(1, Ordering::SeqCst);
        let (tx, rx) = mpsc::unbounded_channel();
        self.senders.lock().unwrap().insert(conn_id, tx);
        (conn_id, rx)
    }

    /// 发送消息到指定连接
    pub fn send(&self, conn_id: i64, msg: WsMessage) -> bool {
        if let Some(tx) = self.senders.lock().unwrap().get(&conn_id) {
            tx.send(msg).is_ok()
        } else {
            false
        }
    }

    /// 移除连接
    pub fn remove(&self, conn_id: i64) {
        self.senders.lock().unwrap().remove(&conn_id);
    }

    /// 活动连接数
    pub fn active_count(&self) -> usize {
        self.senders.lock().unwrap().len()
    }
}

/// 全局 WebSocket 回调类型
pub type WsCallback = extern "C" fn(i64, *const u8, i32, i32);

/// 全局连接管理器
static WS_MANAGER: OnceLock<WsConnectionManager> = OnceLock::new();
static WS_CALLBACK: OnceLock<WsCallback> = OnceLock::new();

pub fn global_manager() -> &'static WsConnectionManager {
    WS_MANAGER.get_or_init(WsConnectionManager::new)
}

pub fn set_ws_callback(cb: WsCallback) {
    let _ = WS_CALLBACK.set(cb);
}

pub fn get_ws_callback() -> Option<&'static WsCallback> {
    WS_CALLBACK.get()
}
