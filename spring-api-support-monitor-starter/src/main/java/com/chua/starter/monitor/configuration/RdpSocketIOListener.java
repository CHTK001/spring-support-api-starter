package com.chua.starter.monitor.configuration;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
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
 * RDP Socket IO 监听器
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
public class RdpSocketIOListener implements SocketListener {

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
     * 获取RDP连接服务
     */
    private ServerConnectionService getRdpConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "RDP".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * RDP连接事件
     */
    @OnEvent("rdp/connect")
    public void onConnect(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                session.send("rdp/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService rdpService = getRdpConnectionService();
            if (rdpService == null) {
                session.send("rdp/error", ReturnResult.illegal("RDP服务不可用"));
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                session.send("rdp/error", ReturnResult.illegal("服务器配置不存在"));
                return;
            }

            boolean connected = rdpService.connect(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);
                session.send("rdp/connected", ReturnResult.success("RDP连接成功"));
            } else {
                session.send("rdp/error", ReturnResult.illegal("RDP连接失败"));
            }

        } catch (Exception e) {
            log.error("RDP连接失败: {}", e.getMessage(), e);
            session.send("rdp/error", ReturnResult.illegal("RDP连接失败: " + e.getMessage()));
        }
    }

    /**
     * RDP断开连接事件
     */
    @OnEvent("rdp/disconnect")
    public void onDisconnect(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                ServerConnectionService rdpService = getRdpConnectionService();
                if (rdpService != null) {
                    rdpService.disconnect(serverId);
                }
                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);
            }

            session.send("rdp/disconnected", ReturnResult.success("RDP连接已断开"));

        } catch (Exception e) {
            log.error("RDP断开连接失败: {}", e.getMessage(), e);
            session.send("rdp/error", ReturnResult.illegal("RDP断开连接失败: " + e.getMessage()));
        }
    }

    /**
     * RDP数据发送事件
     */
    @OnEvent("rdp/data")
    public void onData(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String inputData = data.getString("data");

            if (serverId == null) {
                session.send("rdp/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService rdpService = getRdpConnectionService();
            if (rdpService != null) {
                rdpService.sendData(serverId, inputData);
            }

        } catch (Exception e) {
            log.error("RDP数据发送失败: {}", e.getMessage(), e);
            session.send("rdp/error", ReturnResult.illegal("RDP数据发送失败: " + e.getMessage()));
        }
    }

    /**
     * RDP屏幕大小调整事件
     */
    @OnEvent("rdp/resize")
    public void onResize(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            Integer width = data.getInteger("width");
            Integer height = data.getInteger("height");

            if (serverId == null || width == null || height == null) {
                session.send("rdp/error", ReturnResult.illegal("参数不完整"));
                return;
            }

            ServerConnectionService rdpService = getRdpConnectionService();
            if (rdpService != null) {
                rdpService.resizeTerminal(serverId, width, height);
            }

        } catch (Exception e) {
            log.error("RDP屏幕大小调整失败: {}", e.getMessage(), e);
            session.send("rdp/error", ReturnResult.illegal("RDP屏幕大小调整失败: " + e.getMessage()));
        }
    }

    /**
     * 处理前端发送的 gen/server 事件（RDP部分）
     */
    @OnEvent("gen/server/rdp")
    public void onGenServerRdp(SocketSession session, String messageData) {
        try {
            log.debug("收到 gen/server/rdp 消息: {}", messageData);

            ServerWebSocketMessage message = Json.fromJson(messageData, ServerWebSocketMessage.class);
            if (message == null || message.getMessageType() == null) {
                log.warn("无效的 gen/server/rdp 消息: {}", messageData);
                sendErrorMessage(session, "无效的消息格式");
                return;
            }

            switch (message.getMessageType()) {
                case ServerWebSocketMessage.MessageType.RDP_CONNECT:
                    handleRDPConnect(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.RDP_INPUT:
                    handleRDPInput(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.RDP_DISCONNECT:
                    handleRDPDisconnect(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.RDP_RESIZE:
                    handleRDPResize(session, message);
                    break;
                default:
                    log.debug("未处理的消息类型: {}", message.getMessageType());
                    sendErrorMessage(session, "不支持的消息类型: " + message.getMessageType());
            }

        } catch (Exception e) {
            log.error("处理 gen/server/rdp 消息失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    private void handleRDPConnect(SocketSession session, ServerWebSocketMessage message) {
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

            ServerConnectionService rdpService = getRdpConnectionService();
            if (rdpService == null) {
                sendErrorMessage(session, "RDP服务不可用");
                return;
            }

            boolean connected = rdpService.connect(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);

                ServerWebSocketMessage response = ServerWebSocketMessage.createRdpConnectMessage(
                        serverId,
                        server.getMonitorSysGenServerName(),
                        server.getMonitorSysGenServerHost(),
                        server.getMonitorSysGenServerPort()
                );
                broadcastMessage(response);
                log.info("RDP连接成功: serverId={}", serverId);
            } else {
                sendErrorMessage(session, "RDP连接失败");
            }
        } catch (Exception e) {
            log.error("处理RDP连接请求失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "连接异常: " + e.getMessage());
        }
    }

    private void handleRDPInput(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            String data = message.getData() != null ? message.getData().toString() : "";

            if (serverId == null) {
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            ServerConnectionService rdpService = getRdpConnectionService();
            if (rdpService != null) {
                rdpService.sendData(serverId, data);
            }

        } catch (Exception e) {
            log.error("处理RDP输入失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "输入处理失败: " + e.getMessage());
        }
    }

    private void handleRDPDisconnect(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                ServerConnectionService rdpService = getRdpConnectionService();
                if (rdpService != null) {
                    rdpService.disconnect(serverId);
                }

                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);

                ServerWebSocketMessage response = ServerWebSocketMessage.createRdpDisconnectMessage(
                        serverId, null, "用户主动断开连接");
                broadcastMessage(response);

                log.info("RDP连接已断开: serverId={}", serverId);
            }

        } catch (Exception e) {
            log.error("处理RDP断开连接失败: {}", e.getMessage(), e);
        }
    }

    private void handleRDPResize(SocketSession session, ServerWebSocketMessage message) {
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
                    ServerConnectionService rdpService = getRdpConnectionService();
                    if (rdpService != null) {
                        rdpService.resizeTerminal(serverId, width, height);
                    }
                }
            }

        } catch (Exception e) {
            log.error("处理RDP屏幕大小调整失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "屏幕大小调整失败: " + e.getMessage());
        }
    }

    private void sendErrorMessage(SocketSession session, String errorMessage) {
        try {
            ServerWebSocketMessage errorResponse = ServerWebSocketMessage.createRdpErrorMessage(
                    null, null, errorMessage);
            session.send("gen/server/rdp", Json.toJson(errorResponse));
        } catch (Exception e) {
            log.error("发送错误消息失败: {}", e.getMessage(), e);
        }
    }

    private void broadcastMessage(ServerWebSocketMessage message) {
        try {
            String messageJson = Json.toJson(message);
            socketSessionTemplate.send("gen/server/rdp", messageJson);
        } catch (Exception e) {
            log.error("广播消息失败: {}", e.getMessage(), e);
        }
    }

    public void onSessionDisconnect(SocketSession session) {
        try {
            String sessionId = session.getId();
            Integer serverId = sessionServerMap.get(sessionId);

            if (serverId != null) {
                ServerConnectionService rdpService = getRdpConnectionService();
                if (rdpService != null) {
                    rdpService.disconnect(serverId);
                }

                sessionServerMap.remove(sessionId);
                serverSessionMap.remove(serverId);

                log.info("会话断开，已清理RDP连接: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("会话断开清理失败: {}", e.getMessage(), e);
        }
    }
}
