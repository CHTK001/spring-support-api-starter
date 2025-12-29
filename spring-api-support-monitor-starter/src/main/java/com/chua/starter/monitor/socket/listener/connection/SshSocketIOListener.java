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
import com.chua.starter.monitor.service.server.SshCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSH Socket IO 鐩戝惉鍣?
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
public class SshSocketIOListener implements SocketListener {

    private final SshCommandService sshCommandService;

    @Autowired
    @Lazy
    private SocketSessionTemplate socketSessionTemplate;

    @Autowired
    @Lazy
    private MonitorSysGenServerService serverService;

    @Autowired
    @Lazy
    private List<ServerConnectionService> serverConnectionServices;

    public SshSocketIOListener(SshCommandService sshCommandService) {
        this.sshCommandService = sshCommandService;
    }

    // 瀛樺偍浼氳瘽涓庢湇鍔″櫒鐨勬槧灏勫叧绯?
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();
    private final Map<Integer, SocketSession> serverSessionMap = new ConcurrentHashMap<>();

    /**
     * 鑾峰彇SSH杩炴帴鏈嶅姟
     */
    private ServerConnectionService getSshConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "SSH".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * SSH杩炴帴浜嬩欢 (remote:ssh:connect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Ssh.CONNECT)
    public void onRemoteSshConnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("鏈嶅姟鍣↖D涓嶈兘涓虹┖"));
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("鏈嶅姟鍣ㄩ厤缃笉瀛樺湪"));
                return;
            }

            boolean connected = sshCommandService.connectSSH(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);
                
                ServerWebSocketMessage response = ServerWebSocketMessage.createSSHConnectMessage(
                        serverId,
                        server.getMonitorSysGenServerName(),
                        server.getMonitorSysGenServerHost(),
                        server.getMonitorSysGenServerPort()
                );
                socketSessionTemplate.send(MonitorSocketTopics.Remote.Ssh.CONNECT, Json.toJson(response));
                log.info("SSH杩炴帴鎴愬姛: serverId={}", serverId);
            } else {
                session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("SSH杩炴帴澶辫触"));
            }
        } catch (Exception e) {
            log.error("SSH杩炴帴澶辫触: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("SSH杩炴帴澶辫触: " + e.getMessage()));
        }
    }

    /**
     * SSH杈撳叆浜嬩欢 (remote:ssh:input)
     */
    @OnEvent(MonitorSocketTopics.Remote.Ssh.INPUT)
    public void onRemoteSshInput(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            String inputData = data.getString("data");

            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("鏈嶅姟鍣↖D涓嶈兘涓虹┖"));
                return;
            }

            if (!sshCommandService.isSSHConnected(serverId)) {
                session.send(MonitorSocketTopics.Remote.Ssh.DISCONNECT, ReturnResult.illegal("SSH未连接"));
                return;
            }

            sshCommandService.sendSSHInput(serverId, inputData != null ? inputData : "");
        } catch (Exception e) {
            log.error("SSH杈撳叆澶辫触: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("SSH杈撳叆澶辫触: " + e.getMessage()));
        }
    }

    /**
     * SSH鏂紑浜嬩欢 (remote:ssh:disconnect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Ssh.DISCONNECT)
    public void onRemoteSshDisconnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                sshCommandService.disconnectSSH(serverId);
                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);
                
                ServerWebSocketMessage response = ServerWebSocketMessage.createSSHDisconnectMessage(
                        serverId, null, "鐢ㄦ埛涓诲姩鏂紑杩炴帴");
                socketSessionTemplate.send(MonitorSocketTopics.Remote.Ssh.DISCONNECT, Json.toJson(response));
                log.info("SSH杩炴帴宸叉柇寮€: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("SSH鏂紑杩炴帴澶辫触: {}", e.getMessage(), e);
        }
    }

    /**
     * SSH缁堢澶у皬璋冩暣浜嬩欢 (remote:ssh:resize)
     */
    @OnEvent(MonitorSocketTopics.Remote.Ssh.RESIZE)
    public void onRemoteSshResize(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            Integer cols = data.getInteger("cols");
            Integer rows = data.getInteger("rows");

            if (serverId == null || cols == null || rows == null) {
                session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("参数不完整"));
                return;
            }

            sshCommandService.resizeSSHTerminal(serverId, cols, rows);
        } catch (Exception e) {
            log.error("SSH缁堢澶у皬璋冩暣澶辫触: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("SSH缁堢澶у皬璋冩暣澶辫触: " + e.getMessage()));
        }
    }

    /**
     * 澶勭悊鍓嶇鍙戦€佺殑 gen/server 浜嬩欢
     * 杩欐槸鍓嶇 Socket.IO 瀹㈡埛绔彂閫佺殑缁熶竴浜嬩欢
     */
    @OnEvent("gen/server")
    public void onGenServer(SocketSession session, String messageData) {
        try {
            log.debug("鏀跺埌 gen/server 娑堟伅: {}", messageData);

            // 瑙ｆ瀽娑堟伅
            ServerWebSocketMessage message = Json.fromJson(messageData, ServerWebSocketMessage.class);
            if (message == null || message.getMessageType() == null) {
                log.warn("无效的gen/server消息: {}", messageData);
                sendErrorMessage(session, "无效的消息格式");
                return;
            }

            // 鏍规嵁娑堟伅绫诲瀷澶勭悊
            switch (message.getMessageType()) {
                case ServerWebSocketMessage.MessageType.SSH_CONNECT:
                    handleSSHConnect(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.SSH_INPUT:
                    Integer serverId = message.getServerId();
                    if(!sshCommandService.isSSHConnected(serverId)) {
                        handleSSHDisconnect(session, message);
                        break;
                    }
                    handleSSHInput(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.SSH_DISCONNECT:
                    handleSSHDisconnect(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.SSH_RESIZE:
                    handleSSHResize(session, message);
                    break;
                default:
                    log.debug("鏈鐞嗙殑娑堟伅绫诲瀷: {}", message.getMessageType());
                    sendErrorMessage(session, "涓嶆敮鎸佺殑娑堟伅绫诲瀷: " + message.getMessageType());
            }

        } catch (Exception e) {
            log.error("澶勭悊 gen/server 娑堟伅澶辫触: {}", e.getMessage(), e);
            sendErrorMessage(session, "娑堟伅澶勭悊澶辫触: " + e.getMessage());
        }
    }

    /**
     * 澶勭悊SSH杩炴帴璇锋眰
     */
    private void handleSSHConnect(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                log.warn("SSH杩炴帴璇锋眰缂哄皯鏈嶅姟鍣↖D");
                sendErrorMessage(session, "鏈嶅姟鍣↖D涓嶈兘涓虹┖");
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                sendErrorMessage(session, "鏈嶅姟鍣ㄩ厤缃笉瀛樺湪");
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                sendErrorMessage(session, "无效的消息格式");
                return;
            }

            // 浣跨敤 SshCommandService 寤虹珛SSH杩炴帴
            boolean connected = sshCommandService.connectSSH(server);
            if (connected) {
                // 瀛樺偍浼氳瘽鏄犲皠
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);

                // 鍙戦€佽繛鎺ユ垚鍔熸秷鎭?
                ServerWebSocketMessage response = ServerWebSocketMessage.createSSHConnectMessage(
                        serverId,
                        server.getMonitorSysGenServerName(),
                        server.getMonitorSysGenServerHost(),
                        server.getMonitorSysGenServerPort()
                );

                // 骞挎挱鍒版墍鏈夎闃呬簡 gen/server 涓婚鐨勫鎴风
                broadcastMessage(response);

                // 涔熷彂閫佺粰褰撳墠浼氳瘽
//                session.send("gen/server", Json.toJson(response));

                log.info("SSH杩炴帴鎴愬姛: serverId={}", serverId);
            } else {
                sendErrorMessage(session, "无效的消息格式");
            }
        } catch (Exception e) {
            log.error("澶勭悊SSH杩炴帴璇锋眰澶辫触: {}", e.getMessage(), e);
            sendErrorMessage(session, "杩炴帴寮傚父: " + e.getMessage());
        }
    }

    /**
     * 澶勭悊SSH杈撳叆
     */
    private void handleSSHInput(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            String data = message.getData() != null ? message.getData().toString() : "";

            if (serverId == null) {
                sendErrorMessage(session, "鏈嶅姟鍣↖D涓嶈兘涓虹┖");
                return;
            }

            if (StringUtils.isEmpty(data)) {
                // 绌烘暟鎹篃鏄湁鏁堢殑锛堟瘮濡傚洖杞﹂敭锛?
                data = "";
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                sendErrorMessage(session, "SSH服务不可用");
                return;
            }

            // 浣跨敤 SshCommandService 鍙戦€佽緭鍏ュ埌SSH浼氳瘽
            boolean success = sshCommandService.sendSSHInput(serverId, data);
            if (!success) {
                sendErrorMessage(session, "鍙戦€丼SH杈撳叆澶辫触");
            }

        } catch (Exception e) {
            log.error("澶勭悊SSH杈撳叆澶辫触: {}", e.getMessage(), e);
            sendErrorMessage(session, "杈撳叆澶勭悊澶辫触: " + e.getMessage());
        }
    }

    /**
     * 澶勭悊SSH鏂紑杩炴帴
     */
    private void handleSSHDisconnect(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                // 灏濊瘯浠庝細璇濇槧灏勪腑鑾峰彇鏈嶅姟鍣↖D
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId == null) {
                log.warn("SSH鏂紑杩炴帴璇锋眰缂哄皯鏈嶅姟鍣↖D");
                return;
            }

            // 浣跨敤 SshCommandService 鏂紑SSH杩炴帴
            sshCommandService.disconnectSSH(serverId);

            // 娓呯悊浼氳瘽鏄犲皠
            sessionServerMap.remove(session.getId());
            serverSessionMap.remove(serverId);

            // 鍙戦€佹柇寮€杩炴帴娑堟伅
            ServerWebSocketMessage response = ServerWebSocketMessage.createSSHDisconnectMessage(
                    serverId, null, "鐢ㄦ埛涓诲姩鏂紑杩炴帴");
            broadcastMessage(response);

            log.info("SSH杩炴帴宸叉柇寮€: serverId={}", serverId);

        } catch (Exception e) {
            log.error("澶勭悊SSH鏂紑杩炴帴澶辫触: {}", e.getMessage(), e);
        }
    }

    /**
     * 澶勭悊SSH缁堢澶у皬璋冩暣
     */
    private void handleSSHResize(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                sendErrorMessage(session, "鏈嶅姟鍣↖D涓嶈兘涓虹┖");
                return;
            }

            // 浠庢秷鎭腑鑾峰彇缁堢澶у皬淇℃伅
            Object dataObj = message.getData();
            if (dataObj == null) {
                sendErrorMessage(session, "缂哄皯缁堢澶у皬淇℃伅");
                return;
            }

            Map<String, Object> resizeData;
            if (dataObj instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> tempMap = (Map<String, Object>) dataObj;
                resizeData = tempMap;
            } else {
                sendErrorMessage(session, "缁堢澶у皬鏁版嵁鏍煎紡閿欒");
                return;
            }

            Integer cols = (Integer) resizeData.get("cols");
            Integer rows = (Integer) resizeData.get("rows");

            if (cols == null || rows == null) {
                sendErrorMessage(session, "缁堢澶у皬鍙傛暟鏃犳晥");
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                sendErrorMessage(session, "SSH服务不可用");
                return;
            }

            // 浣跨敤 SshCommandService 璋冩暣缁堢澶у皬
            boolean success = sshCommandService.resizeSSHTerminal(serverId, cols, rows);
            if (!success) {
                sendErrorMessage(session, "璋冩暣缁堢澶у皬澶辫触");
            }

        } catch (Exception e) {
            log.error("澶勭悊SSH缁堢澶у皬璋冩暣澶辫触: {}", e.getMessage(), e);
            sendErrorMessage(session, "缁堢澶у皬璋冩暣澶辫触: " + e.getMessage());
        }
    }

    /**
     * 鍙戦€侀敊璇秷鎭粰瀹㈡埛绔?
     */
    private void sendErrorMessage(SocketSession session, String errorMessage) {
        try {
            ServerWebSocketMessage errorResponse = ServerWebSocketMessage.createSSHErrorMessage(
                    null, null, errorMessage);
            session.send("gen/server", Json.toJson(errorResponse));
        } catch (Exception e) {
            log.error("鍙戦€侀敊璇秷鎭け璐? {}", e.getMessage(), e);
        }
    }

    /**
     * 骞挎挱娑堟伅鍒版墍鏈夎闃呬簡 gen/server 涓婚鐨勫鎴风
     */
    private void broadcastMessage(ServerWebSocketMessage message) {
        try {
            String messageJson = Json.toJson(message);
            // 浣跨敤 SocketSessionTemplate 骞挎挱娑堟伅
            socketSessionTemplate.send("gen/server", messageJson);
        } catch (Exception e) {
            log.error("骞挎挱娑堟伅澶辫触: {}", e.getMessage(), e);
        }
    }

    /**
     * 浼氳瘽鏂紑鏃剁殑娓呯悊宸ヤ綔
     */
    public void onSessionDisconnect(SocketSession session) {
        try {
            String sessionId = session.getId();
            Integer serverId = sessionServerMap.get(sessionId);

            if (serverId != null) {
                // 浣跨敤 SshCommandService 鏂紑SSH杩炴帴
                sshCommandService.disconnectSSH(serverId);

                // 娓呯悊鏄犲皠
                sessionServerMap.remove(sessionId);
                serverSessionMap.remove(serverId);

                log.info("浼氳瘽鏂紑锛屽凡娓呯悊SSH杩炴帴: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("浼氳瘽鏂紑娓呯悊澶辫触: {}", e.getMessage(), e);
        }
    }
}

