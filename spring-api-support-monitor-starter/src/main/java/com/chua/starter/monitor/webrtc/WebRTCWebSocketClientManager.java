package com.chua.starter.monitor.webrtc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * WebRTC WebSocket 客户端管理器
 * <p>
 * 管理多个 WebSocket 连接，用于与 Rust WebRTC 服务端通信
 * </p>
 *
 * @author CH
 * @since 2026/01/03
 */
@Slf4j
public class WebRTCWebSocketClientManager {

    /**
     * WebSocket 客户端连接映射（serverId -> WebSocketSession）
     */
    private final Map<Integer, WebSocketSession> clientSessions = new ConcurrentHashMap<>();
    
    /**
     * WebSocket 处理器映射（serverId -> WebRTCWebSocketHandler）
     */
    private final Map<Integer, WebRTCWebSocketHandler> handlers = new ConcurrentHashMap<>();

    /**
     * WebSocket 客户端
     */
    private final WebSocketClient webSocketClient;

    /**
     * 构造函数
     */
    public WebRTCWebSocketClientManager() {
        this.webSocketClient = new StandardWebSocketClient();
    }

    /**
     * 获取或创建 WebSocket 连接
     *
     * @param serverId     服务器ID
     * @param webSocketUrl WebSocket URL
     * @return WebSocket 会话，如果连接失败返回 null
     */
    public WebSocketSession getOrCreateConnection(Integer serverId, String webSocketUrl) {
        // 检查是否已存在连接
        var existingSession = clientSessions.get(serverId);
        if (existingSession != null && existingSession.isOpen()) {
            log.debug("[WebRTC][WebSocket] 使用现有连接: serverId={}", serverId);
            return existingSession;
        }

        // 创建新连接
        try {
            var uri = new URI(webSocketUrl);
            var handler = new WebRTCWebSocketHandler(serverId, this);
            
            var future = webSocketClient.doHandshake(handler, webSocketUrl);
            var session = future.get(5, TimeUnit.SECONDS);
            
            if (session != null && session.isOpen()) {
                clientSessions.put(serverId, session);
                handlers.put(serverId, handler);
                log.info("[WebRTC][WebSocket] 连接已建立: serverId={}, url={}", serverId, webSocketUrl);
                return session;
            } else {
                log.error("[WebRTC][WebSocket] 连接失败: serverId={}, url={}", serverId, webSocketUrl);
                return null;
            }
        } catch (Exception e) {
            log.error("[WebRTC][WebSocket] 创建连接失败: serverId={}, url={}", serverId, webSocketUrl, e);
            return null;
        }
    }

    /**
     * 发送消息到 Rust 服务端
     *
     * @param serverId 服务器ID
     * @param message  消息内容
     * @return true 表示发送成功
     */
    public boolean sendMessage(Integer serverId, String message) {
        var session = clientSessions.get(serverId);
        if (session == null || !session.isOpen()) {
            log.warn("[WebRTC][WebSocket] 连接不存在或已关闭: serverId={}", serverId);
            return false;
        }

        try {
            session.sendMessage(new TextMessage(message));
            log.debug("[WebRTC][WebSocket] 已发送消息: serverId={}, message={}", serverId, message);
            return true;
        } catch (Exception e) {
            log.error("[WebRTC][WebSocket] 发送消息失败: serverId={}", serverId, e);
            return false;
        }
    }

    /**
     * 关闭连接
     *
     * @param serverId 服务器ID
     */
    public void closeConnection(Integer serverId) {
        var session = clientSessions.remove(serverId);
        handlers.remove(serverId);
        if (session != null) {
            try {
                if (session.isOpen()) {
                    session.close();
                }
                log.info("[WebRTC][WebSocket] 连接已关闭: serverId={}", serverId);
            } catch (Exception e) {
                log.error("[WebRTC][WebSocket] 关闭连接失败: serverId={}", serverId, e);
            }
        }
    }

    /**
     * 检查连接是否存在
     *
     * @param serverId 服务器ID
     * @return true 表示连接存在且打开
     */
    public boolean isConnected(Integer serverId) {
        var session = clientSessions.get(serverId);
        return session != null && session.isOpen();
    }

    /**
     * 注册消息处理器（用于接收来自 Rust 服务端的消息）
     *
     * @param serverId  服务器ID
     * @param handlerId 处理器ID
     * @param handler   消息处理器
     */
    public void registerMessageHandler(Integer serverId, String handlerId, java.util.function.Consumer<String> handler) {
        var wsHandler = handlers.get(serverId);
        if (wsHandler != null) {
            wsHandler.registerMessageHandler(handlerId, handler);
        } else {
            log.warn("[WebRTC][WebSocket] 无法注册消息处理器，连接不存在: serverId={}", serverId);
        }
    }

    /**
     * 移除消息处理器
     *
     * @param serverId  服务器ID
     * @param handlerId 处理器ID
     */
    public void removeMessageHandler(Integer serverId, String handlerId) {
        var wsHandler = handlers.get(serverId);
        if (wsHandler != null) {
            wsHandler.removeMessageHandler(handlerId);
        }
    }

    /**
     * 清理所有连接
     */
    public void closeAll() {
        var serverIds = new java.util.ArrayList<>(clientSessions.keySet());
        for (var serverId : serverIds) {
            closeConnection(serverId);
        }
        log.info("[WebRTC][WebSocket] 所有连接已关闭");
    }
}

