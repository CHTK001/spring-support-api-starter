package com.chua.starter.monitor.starter.websocket;

import com.chua.common.support.json.Json;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.ServerConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VNC WebSocket处理器
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VncWebSocketHandler implements WebSocketHandler {

    private final List<ServerConnectionService> serverConnectionServices;
    private final MonitorSysGenServerService serverService;
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * WebSocket会话映射
     */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 会话与服务器ID映射
     */
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();

    /**
     * 服务器与会话映射
     */
    private final Map<Integer, WebSocketSession> serverSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("VNC WebSocket连接建立: sessionId={}", sessionId);
        
        // 发送连接成功消息
        sendMessage(session, "connection", Map.of(
            "type", "connected",
            "sessionId", sessionId,
            "timestamp", System.currentTimeMillis()
        ));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage textMessage) {
            handleTextMessage(session, textMessage);
        } else if (message instanceof BinaryMessage binaryMessage) {
            handleBinaryMessage(session, binaryMessage);
        }
    }

    /**
     * 处理文本消息
     */
    private void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            @SuppressWarnings("unchecked")
            Map<String, Object> data = Json.fromJson(payload, Map.class);
            
            String action = (String) data.get("action");
            if (action == null) {
                sendErrorMessage(session, "action不能为空");
                return;
            }

            switch (action) {
                case "connect" -> handleConnect(session, data);
                case "disconnect" -> handleDisconnect(session, data);
                case "mouse" -> handleMouseEvent(session, data);
                case "keyboard" -> handleKeyboardEvent(session, data);
                case "resize" -> handleScreenResize(session, data);
                case "refresh" -> handleScreenRefresh(session, data);
                case "clipboard" -> handleClipboard(session, data);
                case "screenshot" -> handleScreenshot(session, data);
                default -> sendErrorMessage(session, "未知的操作类型: " + action);
            }
        } catch (Exception e) {
            log.error("处理VNC WebSocket消息失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理二进制消息
     */
    private void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            String sessionId = session.getId();
            Integer serverId = sessionServerMap.get(sessionId);
            
            if (serverId == null) {
                log.warn("会话未关联服务器: sessionId={}", sessionId);
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                // 将二进制数据转换为字符串发送
                byte[] payload = message.getPayload().array();
                String data = new String(payload);
                vncService.sendData(serverId, data);
            }
        } catch (Exception e) {
            log.error("处理VNC二进制消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理连接请求
     */
    private void handleConnect(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = (Integer) data.get("serverId");
            if (serverId == null) {
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                sendErrorMessage(session, "服务器不存在");
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService == null) {
                sendErrorMessage(session, "VNC服务不可用");
                return;
            }

            boolean connected = vncService.connect(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);

                // 发送WebSocket消息
                sendMessage(session, "connect-result", Map.of(
                    "success", true,
                    "serverId", serverId,
                    "message", "VNC连接成功"
                ));

                // 通过Socket.IO广播VNC连接状态
                broadcastVncConnectionStatus(serverId, server.getMonitorSysGenServerName(), "connected");

                // 设置数据回调监听
                setupVncDataListener(serverId, session);

            } else {
                sendMessage(session, "connect-result", Map.of(
                    "success", false,
                    "serverId", serverId,
                    "message", "VNC连接失败"
                ));

                // 广播连接失败状态
                broadcastVncConnectionStatus(serverId, server.getMonitorSysGenServerName(), "failed");
            }
        } catch (Exception e) {
            log.error("处理VNC连接请求失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "连接失败: " + e.getMessage());
        }
    }

    /**
     * 处理断开连接请求
     */
    private void handleDisconnect(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                vncService.disconnect(serverId);
            }

            sessionServerMap.remove(session.getId());
            serverSessionMap.remove(serverId);

            sendMessage(session, "disconnect-result", Map.of(
                "success", true,
                "serverId", serverId,
                "message", "VNC连接已断开"
            ));

            // 广播断开连接状态
            MonitorSysGenServer server = serverService.getById(serverId);
            String serverName = server != null ? server.getMonitorSysGenServerName() : "Server-" + serverId;
            broadcastVncConnectionStatus(serverId, serverName, "disconnected");

        } catch (Exception e) {
            log.error("处理VNC断开连接失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "断开连接失败: " + e.getMessage());
        }
    }

    /**
     * 处理鼠标事件
     */
    private void handleMouseEvent(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                String mouseData = Json.toJson(data);
                vncService.sendData(serverId, mouseData);
            }

            sendMessage(session, "mouse-result", Map.of("success", true));
        } catch (Exception e) {
            log.error("处理VNC鼠标事件失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "鼠标事件处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理键盘事件
     */
    private void handleKeyboardEvent(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                String keyData = Json.toJson(data);
                vncService.sendData(serverId, keyData);
            }

            sendMessage(session, "keyboard-result", Map.of("success", true));
        } catch (Exception e) {
            log.error("处理VNC键盘事件失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "键盘事件处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理屏幕调整
     */
    private void handleScreenResize(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            Integer width = (Integer) data.get("width");
            Integer height = (Integer) data.get("height");

            if (width == null || height == null) {
                sendErrorMessage(session, "屏幕尺寸参数不能为空");
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                vncService.resizeTerminal(serverId, width, height);
            }

            sendMessage(session, "resize-result", Map.of(
                "success", true,
                "width", width,
                "height", height
            ));
        } catch (Exception e) {
            log.error("处理VNC屏幕调整失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "屏幕调整失败: " + e.getMessage());
        }
    }

    /**
     * 处理屏幕刷新
     */
    private void handleScreenRefresh(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                // VNC屏幕刷新逻辑
                Map<String, Object> refreshData = Map.of(
                    "action", "refresh",
                    "x", data.getOrDefault("x", 0),
                    "y", data.getOrDefault("y", 0),
                    "width", data.getOrDefault("width", 1024),
                    "height", data.getOrDefault("height", 768)
                );
                vncService.sendData(serverId, Json.toJson(refreshData));
            }

            sendMessage(session, "refresh-result", Map.of("success", true));
        } catch (Exception e) {
            log.error("处理VNC屏幕刷新失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "屏幕刷新失败: " + e.getMessage());
        }
    }

    /**
     * 处理剪贴板操作
     */
    private void handleClipboard(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            String clipboardData = (String) data.get("data");
            if (clipboardData == null) {
                sendErrorMessage(session, "剪贴板数据不能为空");
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                vncService.sendData(serverId, "clipboard:" + clipboardData);
            }

            sendMessage(session, "clipboard-result", Map.of("success", true));
        } catch (Exception e) {
            log.error("处理VNC剪贴板操作失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "剪贴板操作失败: " + e.getMessage());
        }
    }

    /**
     * 处理屏幕截图
     */
    private void handleScreenshot(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                Map<String, Object> serverInfo = vncService.getServerInfo(serverId);
                sendMessage(session, "screenshot-result", Map.of(
                    "success", true,
                    "serverInfo", serverInfo
                ));
            } else {
                sendErrorMessage(session, "VNC服务不可用");
            }
        } catch (Exception e) {
            log.error("处理VNC屏幕截图失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "屏幕截图失败: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("VNC WebSocket传输错误: sessionId={}, error={}", session.getId(), exception.getMessage(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        log.info("VNC WebSocket连接关闭: sessionId={}, status={}", sessionId, closeStatus);
        
        // 清理会话
        sessions.remove(sessionId);
        Integer serverId = sessionServerMap.remove(sessionId);
        
        // 断开服务器连接
        if (serverId != null) {
            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                vncService.disconnect(serverId);
            }
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 获取VNC连接服务
     */
    private ServerConnectionService getVncConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "VNC".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, String type, Object data) {
        try {
            Map<String, Object> message = Map.of(
                "type", type,
                "data", data,
                "timestamp", System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(Json.toJson(message)));
        } catch (IOException e) {
            log.error("发送WebSocket消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String error) {
        sendMessage(session, "error", Map.of("message", error));
    }

    /**
     * 广播VNC连接状态
     */
    private void broadcastVncConnectionStatus(Integer serverId, String serverName, String status) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.createVncConnectionMessage(
                serverId, serverName, status
            );
            socketSessionTemplate.send("gen/server", Json.toJson(message));
            log.debug("广播VNC连接状态: serverId={}, status={}", serverId, status);
        } catch (Exception e) {
            log.error("广播VNC连接状态失败: serverId={}", serverId, e);
        }
    }

    /**
     * 设置VNC数据监听
     */
    private void setupVncDataListener(Integer serverId, WebSocketSession session) {
        ServerConnectionService vncService = getVncConnectionService();
        if (vncService != null) {
            vncService.setDataCallback(serverId, (data) -> {
                try {
                    // 发送到WebSocket客户端
                    sendMessage(session, "vnc-data", Map.of("data", data));

                    // 通过Socket.IO广播VNC数据
                    MonitorSysGenServer server = serverService.getById(serverId);
                    String serverName = server != null ? server.getMonitorSysGenServerName() : "Server-" + serverId;

                    ServerWebSocketMessage message = ServerWebSocketMessage.createVncDataMessage(
                        serverId, serverName, data
                    );
                    socketSessionTemplate.send("gen/server", Json.toJson(message));

                } catch (Exception e) {
                    log.error("发送VNC数据失败: serverId={}", serverId, e);
                }
            });
        }
    }

    /**
     * 发送VNC数据到前端
     */
    public void sendVncDataToClient(Integer serverId, Object data) {
        try {
            WebSocketSession session = serverSessionMap.get(serverId);
            if (session != null && session.isOpen()) {
                sendMessage(session, "vnc-data", Map.of("data", data));
            }

            // 同时通过Socket.IO广播
            MonitorSysGenServer server = serverService.getById(serverId);
            String serverName = server != null ? server.getMonitorSysGenServerName() : "Server-" + serverId;

            ServerWebSocketMessage message = ServerWebSocketMessage.createVncDataMessage(
                serverId, serverName, data
            );
            socketSessionTemplate.send("gen/server", Json.toJson(message));

        } catch (Exception e) {
            log.error("发送VNC数据到前端失败: serverId={}", serverId, e);
        }
    }
}
