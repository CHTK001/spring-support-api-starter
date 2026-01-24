package com.chua.starter.monitor.webrtc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * WebRTC WebSocket 消息处理器
 * <p>
 * 处理来自 Rust WebRTC 服务端的消息
 * </p>
 *
 * @author CH
 * @since 2026/01/03
 */
@Slf4j
public class WebRTCWebSocketHandler extends TextWebSocketHandler {

    /**
     * 服务器ID
     */
    private final Integer serverId;

    /**
     * 客户端管理器
     */
    private final WebRTCWebSocketClientManager manager;

    /**
     * 消息处理器映射（支持多个处理器）
     */
    private final Map<String, Consumer<String>> messageHandlers = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param serverId 服务器ID
     * @param manager  客户端管理器
     */
    public WebRTCWebSocketHandler(Integer serverId, WebRTCWebSocketClientManager manager) {
        this.serverId = serverId;
        this.manager = manager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("[WebRTC][WebSocket] 连接已建立: serverId={}, sessionId={}", serverId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        var payload = message.getPayload();
        log.debug("[WebRTC][WebSocket] 收到消息: serverId={}, message={}", serverId, payload);
        
        // 通知所有消息处理器
        messageHandlers.values().forEach(handler -> {
            try {
                handler.accept(payload);
            } catch (Exception e) {
                log.error("[WebRTC][WebSocket] 处理消息失败: serverId={}", serverId, e);
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("[WebRTC][WebSocket] 连接已关闭: serverId={}, status={}", serverId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("[WebRTC][WebSocket] 传输错误: serverId={}", serverId, exception);
    }

    /**
     * 注册消息处理器
     *
     * @param handlerId 处理器ID
     * @param handler   消息处理器
     */
    public void registerMessageHandler(String handlerId, Consumer<String> handler) {
        messageHandlers.put(handlerId, handler);
    }

    /**
     * 移除消息处理器
     *
     * @param handlerId 处理器ID
     */
    public void removeMessageHandler(String handlerId) {
        messageHandlers.remove(handlerId);
    }
}

