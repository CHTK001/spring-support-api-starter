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
import com.chua.starter.sync.message.ServerWebSocketMessage;
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
 * VNC Socket 监听器
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
public class VncSocketListener implements SocketListener {

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
     * 获取VNC连接服务
     */
    private ServerConnectionService getVncConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "VNC".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * VNC连接事件 (remote:vnc:connect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Vnc.CONNECT)
    public void onRemoteVncConnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService == null) {
                session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("VNC服务不可用"));
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("服务器配置不存在"));
                return;
            }

            boolean connected = vncService.connect(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);
                
                ServerWebSocketMessage response = ServerWebSocketMessage.createVncConnectMessage(
                        serverId,
                        server.getMonitorSysGenServerName(),
                        server.getMonitorSysGenServerHost(),
                        server.getMonitorSysGenServerPort()
                );
                socketSessionTemplate.send(MonitorSocketTopics.Remote.Vnc.CONNECT, Json.toJson(response));
                log.info("VNC连接成功: serverId={}", serverId);
            } else {
                session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("VNC连接失败"));
            }
        } catch (Exception e) {
            log.error("VNC连接失败: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("VNC连接失败: " + e.getMessage()));
        }
    }

    /**
     * VNC输入事件 (remote:vnc:input)
     */
    @OnEvent(MonitorSocketTopics.Remote.Vnc.INPUT)
    public void onRemoteVncInput(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            String inputData = data.getString("data");

            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                vncService.sendData(serverId, inputData);
            }
        } catch (Exception e) {
            log.error("VNC输入失败: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("VNC输入失败: " + e.getMessage()));
        }
    }

    /**
     * VNC断开事件 (remote:vnc:disconnect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Vnc.DISCONNECT)
    public void onRemoteVncDisconnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                ServerConnectionService vncService = getVncConnectionService();
                if (vncService != null) {
                    vncService.disconnect(serverId);
                }
                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);
                
                ServerWebSocketMessage response = ServerWebSocketMessage.createVncDisconnectMessage(
                        serverId, null, "用户主动断开连接");
                socketSessionTemplate.send(MonitorSocketTopics.Remote.Vnc.DISCONNECT, Json.toJson(response));
                log.info("VNC连接已断开: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("VNC断开连接失败: {}", e.getMessage(), e);
        }
    }

    /**
     * VNC终端大小调整事件 (remote:vnc:resize)
     */
    @OnEvent(MonitorSocketTopics.Remote.Vnc.RESIZE)
    public void onRemoteVncResize(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            Integer width = data.getInteger("width");
            Integer height = data.getInteger("height");

            if (serverId == null || width == null || height == null) {
                session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("参数不完整"));
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                vncService.resizeTerminal(serverId, width, height);
            }
        } catch (Exception e) {
            log.error("VNC终端大小调整失败: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("VNC终端大小调整失败: " + e.getMessage()));
        }
    }

    /**
     * 处理前端发送的 gen/server 事件（VNC部分）
     */
    @OnEvent("gen/server/vnc")
    public void onGenServerVnc(SocketSession session, String messageData) {
        try {
            log.debug("收到 gen/server/vnc 消息: {}", messageData);

            ServerWebSocketMessage message = Json.fromJson(messageData, ServerWebSocketMessage.class);
            if (message == null || message.getMessageType() == null) {
                log.warn("无效的gen/server/vnc消息: {}", messageData);
                sendErrorMessage(session, "无效的消息格式");
                return;
            }

            switch (message.getMessageType()) {
                case ServerWebSocketMessage.MessageType.VNC_CONNECT:
                    handleVNCConnect(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.VNC_INPUT:
                    handleVNCInput(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.VNC_DISCONNECT:
                    handleVNCDisconnect(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.VNC_RESIZE:
                    handleVNCResize(session, message);
                    break;
                default:
                    log.debug("未处理的消息类型: {}", message.getMessageType());
                    sendErrorMessage(session, "不支持的消息类型: " + message.getMessageType());
            }

        } catch (Exception e) {
            log.error("处理 gen/server/vnc 消息失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    private void handleVNCConnect(SocketSession session, ServerWebSocketMessage message) {
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

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService == null) {
                sendErrorMessage(session, "VNC服务不可用");
                return;
            }

            boolean connected = vncService.connect(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);

                ServerWebSocketMessage response = ServerWebSocketMessage.createVncConnectMessage(
                        serverId,
                        server.getMonitorSysGenServerName(),
                        server.getMonitorSysGenServerHost(),
                        server.getMonitorSysGenServerPort()
                );
                broadcastMessage(response);
                log.info("VNC连接成功: serverId={}", serverId);
            } else {
                sendErrorMessage(session, "VNC连接失败");
            }
        } catch (Exception e) {
            log.error("处理VNC连接请求失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "连接异常: " + e.getMessage());
        }
    }

    private void handleVNCInput(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            String data = message.getData() != null ? message.getData().toString() : "";

            if (serverId == null) {
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                vncService.sendData(serverId, data);
            }

        } catch (Exception e) {
            log.error("处理VNC输入失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "输入处理失败: " + e.getMessage());
        }
    }

    private void handleVNCDisconnect(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                ServerConnectionService vncService = getVncConnectionService();
                if (vncService != null) {
                    vncService.disconnect(serverId);
                }

                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);

                ServerWebSocketMessage response = ServerWebSocketMessage.createVncDisconnectMessage(
                        serverId, null, "用户主动断开连接");
                broadcastMessage(response);

                log.info("VNC连接已断开: serverId={}", serverId);
            }

        } catch (Exception e) {
            log.error("处理VNC断开连接失败: {}", e.getMessage(), e);
        }
    }

    private void handleVNCResize(SocketSession session, ServerWebSocketMessage message) {
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
                    ServerConnectionService vncService = getVncConnectionService();
                    if (vncService != null) {
                        vncService.resizeTerminal(serverId, width, height);
                    }
                }
            }

        } catch (Exception e) {
            log.error("处理VNC屏幕大小调整失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "屏幕大小调整失败: " + e.getMessage());
        }
    }

    private void sendErrorMessage(SocketSession session, String errorMessage) {
        try {
            ServerWebSocketMessage errorResponse = ServerWebSocketMessage.createVncErrorMessage(
                    null, null, errorMessage);
            session.send("gen/server/vnc", Json.toJson(errorResponse));
        } catch (Exception e) {
            log.error("发送错误消息失败: {}", e.getMessage(), e);
        }
    }

    private void broadcastMessage(ServerWebSocketMessage message) {
        try {
            String messageJson = Json.toJson(message);
            socketSessionTemplate.send("gen/server/vnc", messageJson);
        } catch (Exception e) {
            log.error("广播消息失败: {}", e.getMessage(), e);
        }
    }

    public void onSessionDisconnect(SocketSession session) {
        try {
            String sessionId = session.getId();
            Integer serverId = sessionServerMap.get(sessionId);

            if (serverId != null) {
                ServerConnectionService vncService = getVncConnectionService();
                if (vncService != null) {
                    vncService.disconnect(serverId);
                }

                sessionServerMap.remove(sessionId);
                serverSessionMap.remove(serverId);

                log.info("会话断开，已清理VNC连接: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("会话断开清理失败: {}", e.getMessage(), e);
        }
    }
}
