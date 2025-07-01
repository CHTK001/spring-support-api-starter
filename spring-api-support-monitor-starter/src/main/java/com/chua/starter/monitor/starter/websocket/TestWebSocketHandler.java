package com.chua.starter.monitor.starter.websocket;

import com.chua.common.support.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试 WebSocket处理器
 * 用于测试 WebSocket 连接是否正常工作
 *
 * @author CH
 * @since 2024/12/30
 */
@Component
@Slf4j
public class TestWebSocketHandler implements WebSocketHandler {

    /**
     * WebSocket会话映射
     */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("测试 WebSocket连接建立: sessionId={}", sessionId);

        // 发送连接成功消息
        sendMessage(session, Map.of(
                "type", "connected",
                "sessionId", sessionId,
                "success", true,
                "message", "测试 WebSocket 连接成功",
                "timestamp", System.currentTimeMillis()
        ));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = message.getPayload().toString();
            log.info("收到测试 WebSocket 消息: sessionId={}, payload={}", session.getId(), payload);

            // 尝试解析为JSON
            try {
                Map<String, Object> data = Json.fromJson(payload, Map.class);
                String type = (String) data.get("type");

                switch (type) {
                    case "ping":
                        handlePing(session, data);
                        break;
                    case "echo":
                        handleEcho(session, data);
                        break;
                    default:
                        sendMessage(session, Map.of(
                                "type", "response",
                                "originalType", type,
                                "message", "收到消息: " + type,
                                "timestamp", System.currentTimeMillis()
                        ));
                }
            } catch (Exception e) {
                // 如果不是JSON格式，直接回显
                sendMessage(session, Map.of(
                        "type", "echo",
                        "message", "收到文本消息: " + payload,
                        "timestamp", System.currentTimeMillis()
                ));
            }

        } catch (Exception e) {
            log.error("处理测试 WebSocket 消息失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("测试 WebSocket 传输错误: sessionId={}, error={}", sessionId, exception.getMessage(), exception);
        
        // 发送错误消息
        try {
            sendErrorMessage(session, "传输错误: " + exception.getMessage());
        } catch (Exception e) {
            log.error("发送错误消息失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        log.info("测试 WebSocket 连接关闭: sessionId={}, closeStatus={}", sessionId, closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 处理ping消息
     */
    private void handlePing(WebSocketSession session, Map<String, Object> data) throws IOException {
        Object timestamp = data.get("timestamp");
        sendMessage(session, Map.of(
                "type", "pong",
                "originalTimestamp", timestamp,
                "serverTimestamp", System.currentTimeMillis(),
                "message", "pong"
        ));
    }

    /**
     * 处理echo消息
     */
    private void handleEcho(WebSocketSession session, Map<String, Object> data) throws IOException {
        sendMessage(session, Map.of(
                "type", "echo-response",
                "originalData", data,
                "message", "回显消息",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, Map<String, Object> message) throws IOException {
        if (session.isOpen()) {
            String jsonMessage = Json.toJson(message);
            session.sendMessage(new TextMessage(jsonMessage));
            log.debug("发送测试 WebSocket 消息: sessionId={}, message={}", session.getId(), jsonMessage);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) throws IOException {
        sendMessage(session, Map.of(
                "type", "error",
                "error", true,
                "message", errorMessage,
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * 广播消息到所有连接的会话
     */
    public void broadcastMessage(Map<String, Object> message) {
        String jsonMessage = Json.toJson(message);
        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
            } catch (IOException e) {
                log.error("广播消息失败: sessionId={}, error={}", session.getId(), e.getMessage());
            }
        });
    }
}
