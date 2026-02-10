package com.chua.starter.monitor.socket.listener.connection;

import com.chua.common.support.text.json.Json;
import com.chua.common.support.text.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socket.support.SocketListener;
import com.chua.socket.support.annotations.OnEvent;
import com.chua.socket.support.session.SocketSession;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.constant.MonitorSocketTopics;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.sync.support.message.ServerWebSocketMessage;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.impl.RemoteDesktopServiceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 远程桌面 Socket 监听器
 * <p>
 * 处理远程桌面连接相关的Socket事件，采用与SSH相同的方式
 * </p>
 *
 * @author CH
 * @since 2026/01/24
 */
@Component
@Slf4j
public class RemoteDesktopSocketListener implements SocketListener {

    @Autowired
    @Lazy
    private SocketSessionTemplate socketSessionTemplate;

    @Autowired
    @Lazy
    private MonitorSysGenServerService serverService;

    @Autowired
    @Lazy
    private RemoteDesktopServiceMonitor remoteDesktopService;

    // 存储会话与服务器的映射关系
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();
    private final Map<Integer, SocketSession> serverSessionMap = new ConcurrentHashMap<>();

    /**
     * 远程桌面连接事件 (remote:desktop:connect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.CONNECT)
    public void onRemoteDesktopConnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Desktop.ERROR, ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                session.send(MonitorSocketTopics.Remote.Desktop.ERROR, ReturnResult.illegal("服务器配置不存在"));
                return;
            }

            // 启动远程桌面服务
            var handles = remoteDesktopService.startService(serverId, null, null);
            if (handles.isEmpty()) {
                session.send(MonitorSocketTopics.Remote.Desktop.ERROR, ReturnResult.illegal("远程桌面服务启动失败"));
                return;
            }

            sessionServerMap.put(session.getId(), serverId);
            serverSessionMap.put(serverId, session);

            var host = remoteDesktopService.getDefaultHost();
            var wsPort = remoteDesktopService.getDefaultWebSocketPort();
            var wsUrl = remoteDesktopService.getWebSocketUrl(serverId, host, wsPort);

            Map<String, Object> connectData = Map.of(
                    "serverId", serverId,
                    "host", host,
                    "websocketPort", wsPort,
                    "websocketUrl", wsUrl,
                    "handles", handles
            );

            ServerWebSocketMessage response = ServerWebSocketMessage.createRemoteDesktopConnectMessage(
                    serverId,
                    server.getMonitorSysGenServerName(),
                    host,
                    wsPort,
                    wsUrl
            );
            socketSessionTemplate.send(MonitorSocketTopics.Remote.Desktop.CONNECTED, Json.toJson(response));
            log.info("[远程桌面][Socket] 连接成功: serverId={}", serverId);
        } catch (Exception e) {
            log.error("[远程桌面][Socket] 连接失败: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Desktop.ERROR, ReturnResult.illegal("远程桌面连接失败: " + e.getMessage()));
        }
    }

    /**
     * 远程桌面鼠标移动事件 (remote:desktop:mouse_move)
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.MOUSE_MOVE)
    public void onRemoteDesktopMouseMove(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId == null) {
                return;
            }

            // 鼠标移动事件通过WebSocket直接转发到Rust服务端
            // 这里只是记录日志，实际处理在Rust服务端
            log.debug("[远程桌面][Socket] 鼠标移动: serverId={}", serverId);
        } catch (Exception e) {
            log.error("[远程桌面][Socket] 处理鼠标移动失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 远程桌面鼠标点击事件 (remote:desktop:mouse_click)
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.MOUSE_CLICK)
    public void onRemoteDesktopMouseClick(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId == null) {
                return;
            }

            log.debug("[远程桌面][Socket] 鼠标点击: serverId={}", serverId);
        } catch (Exception e) {
            log.error("[远程桌面][Socket] 处理鼠标点击失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 远程桌面鼠标滚轮事件 (remote:desktop:mouse_wheel)
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.MOUSE_WHEEL)
    public void onRemoteDesktopMouseWheel(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId == null) {
                return;
            }

            log.debug("[远程桌面][Socket] 鼠标滚轮: serverId={}", serverId);
        } catch (Exception e) {
            log.error("[远程桌面][Socket] 处理鼠标滚轮失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 远程桌面按键事件 (remote:desktop:key_press)
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.KEY_PRESS)
    public void onRemoteDesktopKeyPress(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId == null) {
                return;
            }

            log.debug("[远程桌面][Socket] 按键按下: serverId={}", serverId);
        } catch (Exception e) {
            log.error("[远程桌面][Socket] 处理按键按下失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 远程桌面按键释放事件 (remote:desktop:key_release)
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.KEY_RELEASE)
    public void onRemoteDesktopKeyRelease(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId == null) {
                return;
            }

            log.debug("[远程桌面][Socket] 按键释放: serverId={}", serverId);
        } catch (Exception e) {
            log.error("[远程桌面][Socket] 处理按键释放失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 远程桌面断开事件 (remote:desktop:disconnect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.DISCONNECT)
    public void onRemoteDesktopDisconnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                remoteDesktopService.stopService(serverId, null);
                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);

                ServerWebSocketMessage response = ServerWebSocketMessage.createRemoteDesktopDisconnectMessage(
                        serverId, null, "用户主动断开连接");
                socketSessionTemplate.send(MonitorSocketTopics.Remote.Desktop.DISCONNECTED, Json.toJson(response));
                log.info("[远程桌面][Socket] 连接已断开: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("[远程桌面][Socket] 断开连接失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理前端发送的 gen/server 事件
     * 这是前端 Socket.IO 客户端发送的统一事件
     */
    @OnEvent("gen/server")
    public void onGenServer(SocketSession session, String messageData) {
        try {
            log.debug("[远程桌面][Socket] 收到 gen/server 消息: {}", messageData);

            // 解析消息
            ServerWebSocketMessage message = Json.fromJson(messageData, ServerWebSocketMessage.class);
            if (message == null || message.getMessageType() == null) {
                log.warn("[远程桌面][Socket] 无效的gen/server消息: {}", messageData);
                sendErrorMessage(session, "无效的消息格式");
                return;
            }

            // 根据消息类型处理
            switch (message.getMessageType()) {
                case ServerWebSocketMessage.MessageType.REMOTE_DESKTOP_CONNECT:
                    handleRemoteDesktopConnect(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.REMOTE_DESKTOP_DISCONNECT:
                    handleRemoteDesktopDisconnect(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.REMOTE_DESKTOP_MOUSE_MOVE:
                case ServerWebSocketMessage.MessageType.REMOTE_DESKTOP_MOUSE_CLICK:
                case ServerWebSocketMessage.MessageType.REMOTE_DESKTOP_MOUSE_WHEEL:
                case ServerWebSocketMessage.MessageType.REMOTE_DESKTOP_KEY_PRESS:
                case ServerWebSocketMessage.MessageType.REMOTE_DESKTOP_KEY_RELEASE:
                    // 这些事件直接转发到Rust服务端，不需要特殊处理
                    log.debug("[远程桌面][Socket] 转发事件: {}", message.getMessageType());
                    break;
                default:
                    log.debug("[远程桌面][Socket] 未处理的消息类型: {}", message.getMessageType());
            }

        } catch (Exception e) {
            log.error("[远程桌面][Socket] 处理 gen/server 消息失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理远程桌面连接请求
     */
    private void handleRemoteDesktopConnect(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                log.warn("[远程桌面][Socket] 连接请求缺少服务器ID");
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                sendErrorMessage(session, "服务器配置不存在");
                return;
            }

            // 启动远程桌面服务
            var handles = remoteDesktopService.startService(serverId, null, null);
            if (handles.isEmpty()) {
                sendErrorMessage(session, "远程桌面服务启动失败");
                return;
            }

            // 存储会话映射
            sessionServerMap.put(session.getId(), serverId);
            serverSessionMap.put(serverId, session);

            var host = remoteDesktopService.getDefaultHost();
            var wsPort = remoteDesktopService.getDefaultWebSocketPort();
            var wsUrl = remoteDesktopService.getWebSocketUrl(serverId, host, wsPort);

            // 发送连接成功消息
            ServerWebSocketMessage response = ServerWebSocketMessage.createRemoteDesktopConnectMessage(
                    serverId,
                    server.getMonitorSysGenServerName(),
                    host,
                    wsPort,
                    wsUrl
            );

            // 广播到所有订阅了 gen/server 主题的客户端
            broadcastMessage(response);

            log.info("[远程桌面][Socket] 连接成功: serverId={}", serverId);
        } catch (Exception e) {
            log.error("[远程桌面][Socket] 处理连接请求失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "连接异常: " + e.getMessage());
        }
    }

    /**
     * 处理远程桌面断开连接
     */
    private void handleRemoteDesktopDisconnect(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                // 尝试从会话映射中获取服务器ID
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId == null) {
                log.warn("[远程桌面][Socket] 断开连接请求缺少服务器ID");
                return;
            }

            // 停止远程桌面服务
            remoteDesktopService.stopService(serverId, null);

            // 清理会话映射
            sessionServerMap.remove(session.getId());
            serverSessionMap.remove(serverId);

            // 发送断开连接消息
            ServerWebSocketMessage response = ServerWebSocketMessage.createRemoteDesktopDisconnectMessage(
                    serverId, null, "用户主动断开连接");
            broadcastMessage(response);

            log.info("[远程桌面][Socket] 连接已断开: serverId={}", serverId);

        } catch (Exception e) {
            log.error("[远程桌面][Socket] 处理断开连接失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送错误消息给客户端
     */
    private void sendErrorMessage(SocketSession session, String errorMessage) {
        try {
            ServerWebSocketMessage errorResponse = ServerWebSocketMessage.createRemoteDesktopErrorMessage(
                    null, null, errorMessage);
            session.send("gen/server", Json.toJson(errorResponse));
        } catch (Exception e) {
            log.error("[远程桌面][Socket] 发送错误消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 广播消息到所有订阅了 gen/server 主题的客户端
     */
    private void broadcastMessage(ServerWebSocketMessage message) {
        try {
            String messageJson = Json.toJson(message);
            // 使用 SocketSessionTemplate 广播消息
            socketSessionTemplate.send("gen/server", messageJson);
        } catch (Exception e) {
            log.error("[远程桌面][Socket] 广播消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 会话断开时的清理工作
     */
    public void onSessionDisconnect(SocketSession session) {
        try {
            String sessionId = session.getId();
            Integer serverId = sessionServerMap.get(sessionId);

            if (serverId != null) {
                // 停止远程桌面服务
                remoteDesktopService.stopService(serverId, null);

                // 清理映射
                sessionServerMap.remove(sessionId);
                serverSessionMap.remove(serverId);

                log.info("[远程桌面][Socket] 会话断开，已清理连接: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("[远程桌面][Socket] 会话断开清理失败: {}", e.getMessage(), e);
        }
    }
}
