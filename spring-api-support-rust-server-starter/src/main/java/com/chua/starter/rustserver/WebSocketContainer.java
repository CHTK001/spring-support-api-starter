package com.chua.starter.rustserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 连接容器。
 *
 * <p>管理活跃的 {@link WsSession}，将 Rust FFI 层的事件路由到对应的 {@link WebSocketHandler}。</p>
 */
public class WebSocketContainer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketContainer.class);

    private final Map<Long, WsSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketHandler> handlers = new ConcurrentHashMap<>();

    /**
     * 注册路径对应的 WebSocket 处理器。
     *
     * @param path    请求路径，如 /ws/chat
     * @param handler 事件处理器
     */
    public void registerHandler(String path, WebSocketHandler handler) {
        handlers.put(path, handler);
        log.info("[RustWS] Handler registered for path: {}", path);
    }

    /**
     * 创建 NativeBridge.WsCallback 适配器，将 FFI 事件路由到处理器。
     */
    public NativeBridge.WsCallback createCallback(NativeBridge nativeBridge) {
        return (connId, data, dataLen, msgType) -> {
            try {
                switch (msgType) {
                    case 0: // WS_OPEN
                        handleOpen(connId, nativeBridge);
                        break;
                    case 1: // WS_TEXT
                        handleText(connId, data, dataLen);
                        break;
                    case 2: // WS_BINARY
                        handleBinary(connId, data, dataLen);
                        break;
                    case 3: // WS_CLOSE
                        handleClose(connId);
                        break;
                    case 4: // WS_ERROR
                        handleError(connId, new RuntimeException("WebSocket error"));
                        break;
                    default:
                        log.warn("[RustWS] Unknown msgType: {}", msgType);
                }
            } catch (Exception e) {
                log.error("[RustWS] Event handling error: {}", e.getMessage(), e);
            }
        };
    }

    /**
     * 根据路径查找处理器并创建 session。
     */
    private void handleOpen(long connId, NativeBridge nativeBridge) {
        WsSession session = new WsSession(connId, nativeBridge);
        sessions.put(connId, session);
        log.debug("[RustWS] Session opened: {}", connId);
        // 通知所有已注册的 handler（目前简单广播，后续可按路径匹配）
        for (WebSocketHandler handler : handlers.values()) {
            try {
                handler.onOpen(session);
            } catch (Exception e) {
                log.error("[RustWS] onOpen error for session {}: {}", connId, e.getMessage());
            }
        }
    }

    private void handleText(long connId, MemorySegment data, int dataLen) {
        WsSession session = sessions.get(connId);
        if (session == null) return;
        String text = dataLen > 0 ? new String(data.asSlice(0, dataLen).toArray(java.lang.foreign.ValueLayout.JAVA_BYTE), StandardCharsets.UTF_8) : "";
        for (WebSocketHandler handler : handlers.values()) {
            try {
                handler.onText(session, text);
            } catch (Exception e) {
                log.error("[RustWS] onText error for session {}: {}", connId, e.getMessage());
            }
        }
    }

    private void handleBinary(long connId, MemorySegment data, int dataLen) {
        WsSession session = sessions.get(connId);
        if (session == null) return;
        byte[] bytes = dataLen > 0 ? data.asSlice(0, dataLen).toArray(java.lang.foreign.ValueLayout.JAVA_BYTE) : new byte[0];
        for (WebSocketHandler handler : handlers.values()) {
            try {
                handler.onBinary(session, bytes);
            } catch (Exception e) {
                log.error("[RustWS] onBinary error for session {}: {}", connId, e.getMessage());
            }
        }
    }

    private void handleClose(long connId) {
        WsSession session = sessions.remove(connId);
        if (session == null) return;
        session.markClosed();
        for (WebSocketHandler handler : handlers.values()) {
            try {
                handler.onClose(session);
            } catch (Exception e) {
                log.error("[RustWS] onClose error for session {}: {}", connId, e.getMessage());
            }
        }
    }

    private void handleError(long connId, Throwable error) {
        WsSession session = sessions.remove(connId);
        if (session == null) return;
        session.markClosed();
        for (WebSocketHandler handler : handlers.values()) {
            try {
                handler.onError(session, error);
            } catch (Exception e) {
                log.error("[RustWS] onError error for session {}: {}", connId, e.getMessage());
            }
        }
    }

    /**
     * 获取活跃的 session 数量。
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
}
