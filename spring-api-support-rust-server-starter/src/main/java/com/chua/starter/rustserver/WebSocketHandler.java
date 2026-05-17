package com.chua.starter.rustserver;

/**
 * WebSocket 事件处理器接口。
 *
 * <p>所有方法均为 default，实现类只需覆盖感兴趣的事件。</p>
 */
public interface WebSocketHandler {

    /**
     * 连接建立。
     */
    default void onOpen(WsSession session) {}

    /**
     * 收到文本消息。
     */
    default void onText(WsSession session, String text) {}

    /**
     * 收到二进制消息。
     */
    default void onBinary(WsSession session, byte[] data) {}

    /**
     * 连接关闭。
     */
    default void onClose(WsSession session) {}

    /**
     * 连接错误。
     */
    default void onError(WsSession session, Throwable error) {}
}
