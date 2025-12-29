package com.chua.starter.monitor.socket.listener.connection;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socket.support.SocketListener;
import com.chua.socket.support.annotations.OnEvent;
import com.chua.socket.support.session.SocketSession;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.constant.MonitorSocketTopics;
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
 * RDP Socket IO 鐩戝惉鍣?
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

    // 瀛樺偍浼氳瘽涓庢湇鍔″櫒鐨勬槧灏勫叧绯?
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();
    private final Map<Integer, SocketSession> serverSessionMap = new ConcurrentHashMap<>();

    /**
     * 鑾峰彇RDP杩炴帴鏈嶅姟
     */
    private ServerConnectionService getRdpConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "RDP".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * RDP杩炴帴浜嬩欢 (remote:rdp:connect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Rdp.CONNECT)
    public void onRemoteRdpConnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Rdp.ERROR, ReturnResult.illegal("鏈嶅姟鍣↖D涓嶈兘涓虹┖"));
                return;
            }

            ServerConnectionService rdpService = getRdpConnectionService();
            if (rdpService == null) {
                session.send(MonitorSocketTopics.Remote.Rdp.ERROR, ReturnResult.illegal("RDP服务不可用"));
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                session.send(MonitorSocketTopics.Remote.Rdp.ERROR, ReturnResult.illegal("鏈嶅姟鍣ㄩ厤缃笉瀛樺湪"));
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
                socketSessionTemplate.send(MonitorSocketTopics.Remote.Rdp.CONNECT, Json.toJson(response));
                log.info("RDP杩炴帴鎴愬姛: serverId={}", serverId);
            } else {
                session.send(MonitorSocketTopics.Remote.Rdp.ERROR, ReturnResult.illegal("RDP杩炴帴澶辫触"));
            }
        } catch (Exception e) {
            log.error("RDP杩炴帴澶辫触: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Rdp.ERROR, ReturnResult.illegal("RDP杩炴帴澶辫触: " + e.getMessage()));
        }
    }

    /**
     * RDP杈撳叆浜嬩欢 (remote:rdp:input)
     */
    @OnEvent(MonitorSocketTopics.Remote.Rdp.INPUT)
    public void onRemoteRdpInput(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            String inputData = data.getString("data");

            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Rdp.ERROR, ReturnResult.illegal("鏈嶅姟鍣↖D涓嶈兘涓虹┖"));
                return;
            }

            ServerConnectionService rdpService = getRdpConnectionService();
            if (rdpService != null) {
                rdpService.sendData(serverId, inputData);
            }
        } catch (Exception e) {
            log.error("RDP杈撳叆澶辫触: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Rdp.ERROR, ReturnResult.illegal("RDP杈撳叆澶辫触: " + e.getMessage()));
        }
    }

    /**
     * RDP鏂紑浜嬩欢 (remote:rdp:disconnect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Rdp.DISCONNECT)
    public void onRemoteRdpDisconnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
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
                
                ServerWebSocketMessage response = ServerWebSocketMessage.createRdpDisconnectMessage(
                        serverId, null, "鐢ㄦ埛涓诲姩鏂紑杩炴帴");
                socketSessionTemplate.send(MonitorSocketTopics.Remote.Rdp.DISCONNECT, Json.toJson(response));
                log.info("RDP杩炴帴宸叉柇寮€: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("RDP鏂紑杩炴帴澶辫触: {}", e.getMessage(), e);
        }
    }

    /**
     * RDP缁堢澶у皬璋冩暣浜嬩欢 (remote:rdp:resize)
     */
    @OnEvent(MonitorSocketTopics.Remote.Rdp.RESIZE)
    public void onRemoteRdpResize(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            Integer width = data.getInteger("width");
            Integer height = data.getInteger("height");

            if (serverId == null || width == null || height == null) {
                session.send(MonitorSocketTopics.Remote.Rdp.ERROR, ReturnResult.illegal("参数不完整"));
                return;
            }

            ServerConnectionService rdpService = getRdpConnectionService();
            if (rdpService != null) {
                rdpService.resizeTerminal(serverId, width, height);
            }
        } catch (Exception e) {
            log.error("RDP缁堢澶у皬璋冩暣澶辫触: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Rdp.ERROR, ReturnResult.illegal("RDP缁堢澶у皬璋冩暣澶辫触: " + e.getMessage()));
        }
    }

    /**
     * 澶勭悊鍓嶇鍙戦€佺殑 gen/server 浜嬩欢锛圧DP閮ㄥ垎锛?
     */
    @OnEvent("gen/server/rdp")
    public void onGenServerRdp(SocketSession session, String messageData) {
        try {
            log.debug("鏀跺埌 gen/server/rdp 娑堟伅: {}", messageData);

            ServerWebSocketMessage message = Json.fromJson(messageData, ServerWebSocketMessage.class);
            if (message == null || message.getMessageType() == null) {
                log.warn("无效的gen/server/rdp消息: {}", messageData);
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
                    log.debug("鏈鐞嗙殑娑堟伅绫诲瀷: {}", message.getMessageType());
                    sendErrorMessage(session, "涓嶆敮鎸佺殑娑堟伅绫诲瀷: " + message.getMessageType());
            }

        } catch (Exception e) {
            log.error("澶勭悊 gen/server/rdp 娑堟伅澶辫触: {}", e.getMessage(), e);
            sendErrorMessage(session, "娑堟伅澶勭悊澶辫触: " + e.getMessage());
        }
    }

    private void handleRDPConnect(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                sendErrorMessage(session, "鏈嶅姟鍣↖D涓嶈兘涓虹┖");
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                sendErrorMessage(session, "鏈嶅姟鍣ㄩ厤缃笉瀛樺湪");
                return;
            }

            ServerConnectionService rdpService = getRdpConnectionService();
            if (rdpService == null) {
                sendErrorMessage(session, "无效的消息格式");
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
                log.info("RDP杩炴帴鎴愬姛: serverId={}", serverId);
            } else {
                sendErrorMessage(session, "无效的消息格式");
            }
        } catch (Exception e) {
            log.error("澶勭悊RDP杩炴帴璇锋眰澶辫触: {}", e.getMessage(), e);
            sendErrorMessage(session, "杩炴帴寮傚父: " + e.getMessage());
        }
    }

    private void handleRDPInput(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            String data = message.getData() != null ? message.getData().toString() : "";

            if (serverId == null) {
                sendErrorMessage(session, "鏈嶅姟鍣↖D涓嶈兘涓虹┖");
                return;
            }

            ServerConnectionService rdpService = getRdpConnectionService();
            if (rdpService != null) {
                rdpService.sendData(serverId, data);
            }

        } catch (Exception e) {
            log.error("澶勭悊RDP杈撳叆澶辫触: {}", e.getMessage(), e);
            sendErrorMessage(session, "杈撳叆澶勭悊澶辫触: " + e.getMessage());
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
                        serverId, null, "鐢ㄦ埛涓诲姩鏂紑杩炴帴");
                broadcastMessage(response);

                log.info("RDP杩炴帴宸叉柇寮€: serverId={}", serverId);
            }

        } catch (Exception e) {
            log.error("澶勭悊RDP鏂紑杩炴帴澶辫触: {}", e.getMessage(), e);
        }
    }

    private void handleRDPResize(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                sendErrorMessage(session, "鏈嶅姟鍣↖D涓嶈兘涓虹┖");
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
            log.error("澶勭悊RDP灞忓箷澶у皬璋冩暣澶辫触: {}", e.getMessage(), e);
            sendErrorMessage(session, "灞忓箷澶у皬璋冩暣澶辫触: " + e.getMessage());
        }
    }

    private void sendErrorMessage(SocketSession session, String errorMessage) {
        try {
            ServerWebSocketMessage errorResponse = ServerWebSocketMessage.createRdpErrorMessage(
                    null, null, errorMessage);
            session.send("gen/server/rdp", Json.toJson(errorResponse));
        } catch (Exception e) {
            log.error("鍙戦€侀敊璇秷鎭け璐? {}", e.getMessage(), e);
        }
    }

    private void broadcastMessage(ServerWebSocketMessage message) {
        try {
            String messageJson = Json.toJson(message);
            socketSessionTemplate.send("gen/server/rdp", messageJson);
        } catch (Exception e) {
            log.error("骞挎挱娑堟伅澶辫触: {}", e.getMessage(), e);
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

                log.info("浼氳瘽鏂紑锛屽凡娓呯悊RDP杩炴帴: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("浼氳瘽鏂紑娓呯悊澶辫触: {}", e.getMessage(), e);
        }
    }
}

