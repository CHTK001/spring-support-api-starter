package com.chua.starter.rustserver;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 连接封装。
 *
 * <p>包装一个 connId，提供 sendText/sendBinary/close 等操作。</p>
 */
public class WsSession {

    private final long connId;
    private final NativeBridge nativeBridge;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private volatile boolean open = true;

    public WsSession(long connId, NativeBridge nativeBridge) {
        this.connId = connId;
        this.nativeBridge = nativeBridge;
    }

    /**
     * 发送文本消息。
     */
    public void sendText(String text) {
        if (!open) return;
        nativeBridge.wsSend(connId, text.getBytes(StandardCharsets.UTF_8), 1);
    }

    /**
     * 发送二进制消息。
     */
    public void sendBinary(byte[] data) {
        if (!open) return;
        nativeBridge.wsSend(connId, data, 2);
    }

    /**
     * 关闭连接。
     */
    public void close() {
        if (!open) return;
        open = false;
        nativeBridge.wsClose(connId);
    }

    public long getId() { return connId; }
    public boolean isOpen() { return open; }

    /**
     * 标记为已关闭（由容器在 onClose/onError 时调用）。
     */
    void markClosed() { this.open = false; }

    public Object getAttribute(String name) { return attributes.get(name); }
    public void setAttribute(String name, Object value) { attributes.put(name, value); }
    public Object removeAttribute(String name) { return attributes.remove(name); }
    public Map<String, Object> getAttributes() { return attributes; }
}
