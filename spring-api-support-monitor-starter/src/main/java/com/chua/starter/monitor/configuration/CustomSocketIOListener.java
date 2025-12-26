package com.chua.starter.monitor.configuration;

import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socket.support.SocketListener;
import com.chua.socket.support.annotations.OnEvent;
import com.chua.socket.support.session.SocketSession;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.server.ServerConnectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义连接 Socket IO 监听器
 * 用于处理非标准协议的服务器连接
 *
 * @author CH
 * @since 2024/12/25
 */
@Component
@Slf4j
public class CustomSocketIOListener implements SocketListener {

    @Autowired
    @Lazy
    private SocketSessionTemplate socketSessionTemplate;

    @Autowired
    @Lazy
    private MonitorSysGenServerService serverService;

    @Autowired
    @Lazy
    private List<ServerConnectionService> serverConnectionServices;

    // 存储会话与服务器的映射关系
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();
    private final Map<Integer, SocketSession> serverSessionMap = new ConcurrentHashMap<>();

    /**
     * 获取自定义连接服务
     */
    private ServerConnectionService getCustomConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "CUSTOM".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据协议类型获取连接服务
     */
    private ServerConnectionService getConnectionServiceByProtocol(String protocol) {
        if (protocol == null) {
            return getCustomConnectionService();
        }
        return serverConnectionServices.stream()
                .filter(service -> protocol.equalsIgnoreCase(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 自定义连接事件
     */
    @OnEvent("custom/connect")
    public void onConnect(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String protocol = data.getString("protocol");

            if (serverId == null) {
                session.send("custom/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService == null) {
                session.send("custom/error", ReturnResult.illegal("连接服务不可用: " + protocol));
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                session.send("custom/error", ReturnResult.illegal("服务器配置不存在"));
                return;
            }

            boolean connected = connectionService.connect(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);
                session.send("custom/connected", ReturnResult.success("连接成功"));
            } else {
                session.send("custom/error", ReturnResult.illegal("连接失败"));
            }

        } catch (Exception e) {
            log.error("自定义连接失败: {}", e.getMessage(), e);
            session.send("custom/error", ReturnResult.illegal("连接失败: " + e.getMessage()));
        }
    }

    /**
     * 自定义断开连接事件
     */
    @OnEvent("custom/disconnect")
    public void onDisconnect(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String protocol = data.getString("protocol");

            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
                if (connectionService != null) {
                    connectionService.disconnect(serverId);
                }
                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);
            }

            session.send("custom/disconnected", ReturnResult.success("连接已断开"));

        } catch (Exception e) {
            log.error("自定义断开连接失败: {}", e.getMessage(), e);
            session.send("custom/error", ReturnResult.illegal("断开连接失败: " + e.getMessage()));
        }
    }

    /**
     * 自定义数据发送事件
     */
    @OnEvent("custom/data")
    public void onData(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String inputData = data.getString("data");
            String protocol = data.getString("protocol");

            if (serverId == null) {
                session.send("custom/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService != null) {
                connectionService.sendData(serverId, inputData);
            }

        } catch (Exception e) {
            log.error("自定义数据发送失败: {}", e.getMessage(), e);
            session.send("custom/error", ReturnResult.illegal("数据发送失败: " + e.getMessage()));
        }
    }

    /**
     * 自定义命令执行事件
     */
    @OnEvent("custom/command")
    public void onCommand(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String command = data.getString("command");
            String protocol = data.getString("protocol");

            if (serverId == null) {
                session.send("custom/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService != null) {
                String result = connectionService.executeCommand(serverId, command);
                session.send("custom/command-result", ReturnResult.success(result));
            } else {
                session.send("custom/error", ReturnResult.illegal("连接服务不可用"));
            }

        } catch (Exception e) {
            log.error("自定义命令执行失败: {}", e.getMessage(), e);
            session.send("custom/error", ReturnResult.illegal("命令执行失败: " + e.getMessage()));
        }
    }

    /**
     * 自定义屏幕/终端大小调整事件
     */
    @OnEvent("custom/resize")
    public void onResize(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            Integer width = data.getInteger("width");
            Integer height = data.getInteger("height");
            String protocol = data.getString("protocol");

            if (serverId == null || width == null || height == null) {
                session.send("custom/error", ReturnResult.illegal("参数不完整"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService != null) {
                connectionService.resizeTerminal(serverId, width, height);
            }

        } catch (Exception e) {
            log.error("自定义屏幕大小调整失败: {}", e.getMessage(), e);
            session.send("custom/error", ReturnResult.illegal("屏幕大小调整失败: " + e.getMessage()));
        }
    }

    /**
     * 处理前端发送的 gen/server/custom 事件
     */
    @OnEvent("gen/server/custom")
    public void onGenServerCustom(SocketSession session, String messageData) {
        try {
            log.debug("收到 gen/server/custom 消息: {}", messageData);

            ServerWebSocketMessage message = Json.fromJson(messageData, ServerWebSocketMessage.class);
            if (message == null || message.getMessageType() == null) {
                log.warn("无效的 gen/server/custom 消息: {}", messageData);
                sendErrorMessage(session, "无效的消息格式");
                return;
            }

            switch (message.getMessageType()) {
                case ServerWebSocketMessage.MessageType.CUSTOM_CONNECT:
                    handleCustomConnect(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.CUSTOM_INPUT:
                    handleCustomInput(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.CUSTOM_DISCONNECT:
                    handleCustomDisconnect(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.CUSTOM_COMMAND:
                    handleCustomCommand(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.CUSTOM_RESIZE:
                    handleCustomResize(session, message);
                    break;
                default:
                    log.debug("未处理的消息类型: {}", message.getMessageType());
                    sendErrorMessage(session, "不支持的消息类型: " + message.getMessageType());
            }

        } catch (Exception e) {
            log.error("处理 gen/server/custom 消息失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    private void handleCustomConnect(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                sendErrorMessage(session, "服务器配置不存在");
                return;
            }

            String protocol = server.getMonitorSysGenServerProtocol();
            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService == null) {
                sendErrorMessage(session, "连接服务不可用: " + protocol);
                return;
            }

            boolean connected = connectionService.connect(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);

                ServerWebSocketMessage response = ServerWebSocketMessage.createCustomConnectMessage(
                        serverId,
                        server.getMonitorSysGenServerName(),
                        server.getMonitorSysGenServerHost(),
                        server.getMonitorSysGenServerPort(),
                        protocol
                );
                broadcastMessage(response);
                log.info("自定义连接成功: serverId={}, protocol={}", serverId, protocol);
            } else {
                sendErrorMessage(session, "连接失败");
            }
        } catch (Exception e) {
            log.error("处理自定义连接请求失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "连接异常: " + e.getMessage());
        }
    }

    private void handleCustomInput(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            String data = message.getData() != null ? message.getData().toString() : "";

            if (serverId == null) {
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server != null) {
                String protocol = server.getMonitorSysGenServerProtocol();
                ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
                if (connectionService != null) {
                    connectionService.sendData(serverId, data);
                }
            }

        } catch (Exception e) {
            log.error("处理自定义输入失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "输入处理失败: " + e.getMessage());
        }
    }

    private void handleCustomDisconnect(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
                if (server != null) {
                    String protocol = server.getMonitorSysGenServerProtocol();
                    ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
                    if (connectionService != null) {
                        connectionService.disconnect(serverId);
                    }
                }

                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);

                ServerWebSocketMessage response = ServerWebSocketMessage.createCustomDisconnectMessage(
                        serverId, null, "用户主动断开连接");
                broadcastMessage(response);

                log.info("自定义连接已断开: serverId={}", serverId);
            }

        } catch (Exception e) {
            log.error("处理自定义断开连接失败: {}", e.getMessage(), e);
        }
    }

    private void handleCustomCommand(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            Object dataObj = message.getData();
            String command = "";
            if (dataObj instanceof Map<?, ?> dataMap) {
                command = (String) dataMap.get("command");
            } else if (dataObj instanceof String) {
                command = (String) dataObj;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server != null) {
                String protocol = server.getMonitorSysGenServerProtocol();
                ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
                if (connectionService != null) {
                    String result = connectionService.executeCommand(serverId, command);
                    ServerWebSocketMessage response = ServerWebSocketMessage.createCustomDataMessage(
                            serverId, server.getMonitorSysGenServerName(),
                            Map.of("type", "command_result", "result", result)
                    );
                    session.send("gen/server/custom", Json.toJson(response));
                }
            }

        } catch (Exception e) {
            log.error("处理自定义命令失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "命令执行失败: " + e.getMessage());
        }
    }

    private void handleCustomResize(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            Object dataObj = message.getData();
            if (dataObj instanceof Map<?, ?> resizeData) {
                Integer width = (Integer) resizeData.get("width");
                Integer height = (Integer) resizeData.get("height");

                if (width != null && height != null) {
                    MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
                    if (server != null) {
                        String protocol = server.getMonitorSysGenServerProtocol();
                        ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
                        if (connectionService != null) {
                            connectionService.resizeTerminal(serverId, width, height);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("处理自定义屏幕大小调整失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "屏幕大小调整失败: " + e.getMessage());
        }
    }

    private void sendErrorMessage(SocketSession session, String errorMessage) {
        try {
            ServerWebSocketMessage errorResponse = ServerWebSocketMessage.createCustomErrorMessage(
                    null, null, errorMessage);
            session.send("gen/server/custom", Json.toJson(errorResponse));
        } catch (Exception e) {
            log.error("发送错误消息失败: {}", e.getMessage(), e);
        }
    }

    private void broadcastMessage(ServerWebSocketMessage message) {
        try {
            String messageJson = Json.toJson(message);
            socketSessionTemplate.send("gen/server/custom", messageJson);
        } catch (Exception e) {
            log.error("广播消息失败: {}", e.getMessage(), e);
        }
    }

    public void onSessionDisconnect(SocketSession session) {
        try {
            String sessionId = session.getId();
            Integer serverId = sessionServerMap.get(sessionId);

            if (serverId != null) {
                MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
                if (server != null) {
                    String protocol = server.getMonitorSysGenServerProtocol();
                    ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
                    if (connectionService != null) {
                        connectionService.disconnect(serverId);
                    }
                }

                sessionServerMap.remove(sessionId);
                serverSessionMap.remove(serverId);

                log.info("会话断开，已清理自定义连接: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("会话断开清理失败: {}", e.getMessage(), e);
        }
    }
}
