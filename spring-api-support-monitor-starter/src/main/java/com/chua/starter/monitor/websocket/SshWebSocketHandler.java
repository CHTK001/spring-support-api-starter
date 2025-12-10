package com.chua.starter.monitor.websocket;

import com.chua.common.support.json.Json;
import com.chua.socketio.support.constant.SocketTopics;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.server.ServerConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSH WebSocket处理器
 *
 * @author CH
 * @since 2024/12/23
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SshWebSocketHandler implements WebSocketHandler {

    private final List<ServerConnectionService> serverConnectionServices;
    private final MonitorSysGenServerService serverService;
    private final SocketSessionTemplate socketSessionTemplate;

    // 存储WebSocket会话与服务器ID的映射
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();
    // 存储服务器ID与WebSocket会话的映射
    private final Map<Integer, WebSocketSession> serverSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("SSH WebSocket连接建立: sessionId={}", session.getId());
        sendMessage(session, "connect-ready", Map.of("message", "SSH连接就绪"));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = message.getPayload().toString();
            Map<String, Object> data = Json.fromJson(payload, Map.class);
            String type = (String) data.get("type");

            switch (type) {
                case "connect":
                    handleConnect(session, data);
                    break;
                case "input":
                    handleInput(session, data);
                    break;
                case "resize":
                    handleResize(session, data);
                    break;
                case "disconnect":
                    handleDisconnect(session, data);
                    break;
                default:
                    log.warn("未知的SSH消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理SSH WebSocket消息失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("SSH WebSocket传输错误: sessionId={}", session.getId(), exception);
        cleanupSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("SSH WebSocket连接关闭: sessionId={}, status={}", session.getId(), closeStatus);
        cleanupSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 处理SSH连接请求
     */
    private void handleConnect(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = (Integer) data.get("serverId");
            if (serverId == null) {
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                sendErrorMessage(session, "服务器配置不存在");
                return;
            }

            // 建立SSH连接
            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                sendErrorMessage(session, "SSH服务不可用");
                return;
            }

            boolean connected = sshService.connect(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);

                sendMessage(session, "connect-result", Map.of(
                    "success", true,
                    "serverId", serverId,
                    "message", "SSH连接成功"
                ));

                // 广播SSH连接消息
                broadcastSSHConnect(server);

                // 设置数据监听
                setupDataListener(serverId, session);

            } else {
                sendMessage(session, "connect-result", Map.of(
                    "success", false,
                    "serverId", serverId,
                    "message", "SSH连接失败"
                ));
            }
        } catch (Exception e) {
            log.error("处理SSH连接请求失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "连接失败: " + e.getMessage());
        }
    }

    /**
     * 处理用户输入
     */
    private void handleInput(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "未建立SSH连接");
                return;
            }

            String input = (String) data.get("data");
            if (input != null) {
                ServerConnectionService sshService = getSshConnectionService();
                if (sshService != null) {
                    sshService.sendData(serverId, input);
                }
            }
        } catch (Exception e) {
            log.error("处理SSH输入失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "输入处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理终端大小调整
     */
    private void handleResize(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                return;
            }

            Integer cols = (Integer) data.get("cols");
            Integer rows = (Integer) data.get("rows");
            if (cols != null && rows != null) {
                ServerConnectionService sshService = getSshConnectionService();
                if (sshService != null) {
                    sshService.resizeTerminal(serverId, cols, rows);
                }
            }
        } catch (Exception e) {
            log.error("处理SSH终端大小调整失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理断开连接
     */
    private void handleDisconnect(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId != null) {
                ServerConnectionService sshService = getSshConnectionService();
                if (sshService != null) {
                    sshService.disconnect(serverId);
                }

                MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
                if (server != null) {
                    broadcastSSHDisconnect(server, "用户主动断开");
                }
            }
            cleanupSession(session);
        } catch (Exception e) {
            log.error("处理SSH断开连接失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 设置数据监听
     */
    private void setupDataListener(Integer serverId, WebSocketSession session) {
        // 这里应该设置SSH服务的数据回调
        // 当SSH服务接收到数据时，通过WebSocket发送给前端
        ServerConnectionService sshService = getSshConnectionService();
        if (sshService != null) {
            sshService.setDataCallback(serverId, (data) -> {
                try {
                    sendMessage(session, "data", Map.of("data", data));
                } catch (Exception e) {
                    log.error("发送SSH数据失败: {}", e.getMessage(), e);
                }
            });
        }
    }

    /**
     * 清理会话
     */
    private void cleanupSession(WebSocketSession session) {
        try {
            Integer serverId = sessionServerMap.remove(session.getId());
            if (serverId != null) {
                serverSessionMap.remove(serverId);
                ServerConnectionService sshService = getSshConnectionService();
                if (sshService != null) {
                    sshService.disconnect(serverId);
                }

                MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
                if (server != null) {
                    broadcastSSHDisconnect(server, "连接断开");
                }
            }
        } catch (Exception e) {
            log.error("清理SSH会话失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, String type, Object data) throws IOException {
        if (session.isOpen()) {
            Map<String, Object> message = Map.of(
                "type", type,
                "data", data,
                "timestamp", System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(Json.toJson(message)));
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String error) {
        try {
            sendMessage(session, "error", Map.of("message", error));
        } catch (Exception e) {
            log.error("发送错误消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 广播SSH连接消息
     */
    private void broadcastSSHConnect(MonitorSysGenServer server) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.createSSHConnectMessage(
                server.getMonitorSysGenServerId(),
                server.getMonitorSysGenServerName(),
                server.getMonitorSysGenServerHost(),
                server.getMonitorSysGenServerPort()
            );

            socketSessionTemplate.send(SocketTopics.Monitor.Server.SSH_CONNECT, Json.toJson(message));
        } catch (Exception e) {
            log.error("广播SSH连接消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 广播SSH断开连接消息
     */
    private void broadcastSSHDisconnect(MonitorSysGenServer server, String reason) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.createSSHDisconnectMessage(
                server.getMonitorSysGenServerId(),
                server.getMonitorSysGenServerName(),
                reason
            );

            socketSessionTemplate.send(SocketTopics.Monitor.Server.SSH_DISCONNECT, Json.toJson(message));
        } catch (Exception e) {
            log.error("广播SSH断开连接消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取SSH连接服务
     */
    private ServerConnectionService getSshConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "SSH".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }
}
