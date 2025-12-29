package com.chua.starter.monitor.socket.listener.connection;

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
 * VNC Socket IO 鐩戝惉鍣?
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
public class VncSocketIOListener implements SocketListener {

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
     * 鑾峰彇VNC杩炴帴鏈嶅姟
     */
    private ServerConnectionService getVncConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "VNC".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * VNC杩炴帴浜嬩欢 (remote:vnc:connect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Vnc.CONNECT)
    public void onRemoteVncConnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("鏈嶅姟鍣↖D涓嶈兘涓虹┖"));
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService == null) {
                session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("VNC鏈嶅姟涓嶅彲鐢?));
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("鏈嶅姟鍣ㄩ厤缃笉瀛樺湪"));
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
                log.info("VNC杩炴帴鎴愬姛: serverId={}", serverId);
            } else {
                session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("VNC杩炴帴澶辫触"));
            }
        } catch (Exception e) {
            log.error("VNC杩炴帴澶辫触: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("VNC杩炴帴澶辫触: " + e.getMessage()));
        }
    }

    /**
     * VNC杈撳叆浜嬩欢 (remote:vnc:input)
     */
    @OnEvent(MonitorSocketTopics.Remote.Vnc.INPUT)
    public void onRemoteVncInput(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            String inputData = data.getString("data");

            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("鏈嶅姟鍣↖D涓嶈兘涓虹┖"));
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                vncService.sendData(serverId, inputData);
            }
        } catch (Exception e) {
            log.error("VNC杈撳叆澶辫触: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("VNC杈撳叆澶辫触: " + e.getMessage()));
        }
    }

    /**
     * VNC鏂紑浜嬩欢 (remote:vnc:disconnect)
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
                        serverId, null, "鐢ㄦ埛涓诲姩鏂紑杩炴帴");
                socketSessionTemplate.send(MonitorSocketTopics.Remote.Vnc.DISCONNECT, Json.toJson(response));
                log.info("VNC杩炴帴宸叉柇寮€: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("VNC鏂紑杩炴帴澶辫触: {}", e.getMessage(), e);
        }
    }

    /**
     * VNC缁堢澶у皬璋冩暣浜嬩欢 (remote:vnc:resize)
     */
    @OnEvent(MonitorSocketTopics.Remote.Vnc.RESIZE)
    public void onRemoteVncResize(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            Integer width = data.getInteger("width");
            Integer height = data.getInteger("height");

            if (serverId == null || width == null || height == null) {
                session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("鍙傛暟涓嶅畬鏁?));
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                vncService.resizeTerminal(serverId, width, height);
            }
        } catch (Exception e) {
            log.error("VNC缁堢澶у皬璋冩暣澶辫触: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Vnc.ERROR, ReturnResult.illegal("VNC缁堢澶у皬璋冩暣澶辫触: " + e.getMessage()));
        }
    }

    /**
     * 澶勭悊鍓嶇鍙戦€佺殑 gen/server 浜嬩欢锛圴NC閮ㄥ垎锛?
     */
    @OnEvent("gen/server/vnc")
    public void onGenServerVnc(SocketSession session, String messageData) {
        try {
            log.debug("鏀跺埌 gen/server/vnc 娑堟伅: {}", messageData);

            ServerWebSocketMessage message = Json.fromJson(messageData, ServerWebSocketMessage.class);
            if (message == null || message.getMessageType() == null) {
                log.warn("鏃犳晥鐨?gen/server/vnc 娑堟伅: {}", messageData);
                sendErrorMessage(session, "鏃犳晥鐨勬秷鎭牸寮?);
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
                    log.debug("鏈鐞嗙殑娑堟伅绫诲瀷: {}", message.getMessageType());
                    sendErrorMessage(session, "涓嶆敮鎸佺殑娑堟伅绫诲瀷: " + message.getMessageType());
            }

        } catch (Exception e) {
            log.error("澶勭悊 gen/server/vnc 娑堟伅澶辫触: {}", e.getMessage(), e);
            sendErrorMessage(session, "娑堟伅澶勭悊澶辫触: " + e.getMessage());
        }
    }

    private void handleVNCConnect(SocketSession session, ServerWebSocketMessage message) {
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

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService == null) {
                sendErrorMessage(session, "VNC鏈嶅姟涓嶅彲鐢?);
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
                log.info("VNC杩炴帴鎴愬姛: serverId={}", serverId);
            } else {
                sendErrorMessage(session, "VNC杩炴帴澶辫触");
            }
        } catch (Exception e) {
            log.error("澶勭悊VNC杩炴帴璇锋眰澶辫触: {}", e.getMessage(), e);
            sendErrorMessage(session, "杩炴帴寮傚父: " + e.getMessage());
        }
    }

    private void handleVNCInput(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            String data = message.getData() != null ? message.getData().toString() : "";

            if (serverId == null) {
                sendErrorMessage(session, "鏈嶅姟鍣↖D涓嶈兘涓虹┖");
                return;
            }

            ServerConnectionService vncService = getVncConnectionService();
            if (vncService != null) {
                vncService.sendData(serverId, data);
            }

        } catch (Exception e) {
            log.error("澶勭悊VNC杈撳叆澶辫触: {}", e.getMessage(), e);
            sendErrorMessage(session, "杈撳叆澶勭悊澶辫触: " + e.getMessage());
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
                        serverId, null, "鐢ㄦ埛涓诲姩鏂紑杩炴帴");
                broadcastMessage(response);

                log.info("VNC杩炴帴宸叉柇寮€: serverId={}", serverId);
            }

        } catch (Exception e) {
            log.error("澶勭悊VNC鏂紑杩炴帴澶辫触: {}", e.getMessage(), e);
        }
    }

    private void handleVNCResize(SocketSession session, ServerWebSocketMessage message) {
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
                    ServerConnectionService vncService = getVncConnectionService();
                    if (vncService != null) {
                        vncService.resizeTerminal(serverId, width, height);
                    }
                }
            }

        } catch (Exception e) {
            log.error("澶勭悊VNC灞忓箷澶у皬璋冩暣澶辫触: {}", e.getMessage(), e);
            sendErrorMessage(session, "灞忓箷澶у皬璋冩暣澶辫触: " + e.getMessage());
        }
    }

    private void sendErrorMessage(SocketSession session, String errorMessage) {
        try {
            ServerWebSocketMessage errorResponse = ServerWebSocketMessage.createVncErrorMessage(
                    null, null, errorMessage);
            session.send("gen/server/vnc", Json.toJson(errorResponse));
        } catch (Exception e) {
            log.error("鍙戦€侀敊璇秷鎭け璐? {}", e.getMessage(), e);
        }
    }

    private void broadcastMessage(ServerWebSocketMessage message) {
        try {
            String messageJson = Json.toJson(message);
            socketSessionTemplate.send("gen/server/vnc", messageJson);
        } catch (Exception e) {
            log.error("骞挎挱娑堟伅澶辫触: {}", e.getMessage(), e);
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

                log.info("浼氳瘽鏂紑锛屽凡娓呯悊VNC杩炴帴: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("浼氳瘽鏂紑娓呯悊澶辫触: {}", e.getMessage(), e);
        }
    }
}

