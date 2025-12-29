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
 * 鑷畾涔夎繛鎺?Socket IO 鐩戝惉鍣?
 * 鐢ㄤ簬澶勭悊闈炴爣鍑嗗崗璁殑鏈嶅姟鍣ㄨ繛鎺?
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

    // 瀛樺偍浼氳瘽涓庢湇鍔″櫒鐨勬槧灏勫叧绯?
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();
    private final Map<Integer, SocketSession> serverSessionMap = new ConcurrentHashMap<>();

    /**
     * 鑾峰彇鑷畾涔夎繛鎺ユ湇鍔?
     */
    private ServerConnectionService getCustomConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "CUSTOM".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 鏍规嵁鍗忚绫诲瀷鑾峰彇杩炴帴鏈嶅姟
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
     * 鑷畾涔夎繛鎺ヤ簨浠?(remote:custom:connect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Custom.CONNECT)
    public void onConnect(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String protocol = data.getString("protocol");

            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("鏈嶅姟鍣↖D涓嶈兘涓虹┖"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("杩炴帴鏈嶅姟涓嶅彲鐢? " + protocol));
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("鏈嶅姟鍣ㄩ厤缃笉瀛樺湪"));
                return;
            }

            boolean connected = connectionService.connect(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);
                session.send(MonitorSocketTopics.Remote.Custom.CONNECTED, ReturnResult.success("杩炴帴鎴愬姛"));
            } else {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("杩炴帴澶辫触"));
            }

        } catch (Exception e) {
            log.error("鑷畾涔夎繛鎺ュけ璐? {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("杩炴帴澶辫触: " + e.getMessage()));
        }
    }

    /**
     * 鑷畾涔夋柇寮€杩炴帴浜嬩欢 (remote:custom:disconnect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Custom.DISCONNECT)
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

            session.send(MonitorSocketTopics.Remote.Custom.DISCONNECTED, ReturnResult.success("杩炴帴宸叉柇寮€"));

        } catch (Exception e) {
            log.error("鑷畾涔夋柇寮€杩炴帴澶辫触: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("鏂紑杩炴帴澶辫触: " + e.getMessage()));
        }
    }

    /**
     * 鑷畾涔夋暟鎹彂閫佷簨浠?(remote:custom:data)
     */
    @OnEvent(MonitorSocketTopics.Remote.Custom.DATA)
    public void onData(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String inputData = data.getString("data");
            String protocol = data.getString("protocol");

            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("鏈嶅姟鍣↖D涓嶈兘涓虹┖"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService != null) {
                connectionService.sendData(serverId, inputData);
            }

        } catch (Exception e) {
            log.error("鑷畾涔夋暟鎹彂閫佸け璐? {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("鏁版嵁鍙戦€佸け璐? " + e.getMessage()));
        }
    }

    /**
     * 鑷畾涔夊懡浠ゆ墽琛屼簨浠?(remote:custom:command)
     */
    @OnEvent(MonitorSocketTopics.Remote.Custom.COMMAND)
    public void onCommand(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String command = data.getString("command");
            String protocol = data.getString("protocol");

            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("鏈嶅姟鍣↖D涓嶈兘涓虹┖"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService != null) {
                String result = connectionService.executeCommand(serverId, command);
                session.send(MonitorSocketTopics.Remote.Custom.COMMAND_RESULT, ReturnResult.success(result));
            } else {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("连接服务不可用"));
            }

        } catch (Exception e) {
            log.error("鑷畾涔夊懡浠ゆ墽琛屽け璐? {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("鍛戒护鎵ц澶辫触: " + e.getMessage()));
        }
    }

    /**
     * 浜岃繘鍒舵暟鎹姹備簨浠?(remote:custom:binary_request)
     * 鐢ㄤ簬璇锋眰鏈嶅姟鍣ㄧ鐨勪簩杩涘埗鏁版嵁锛堝鏂囦欢銆佸浘鍍忕瓑锛?
     */
    @OnEvent(MonitorSocketTopics.Remote.Custom.BINARY_REQUEST)
    public void onBinaryRequest(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String requestType = data.getString("requestType");
            String protocol = data.getString("protocol");

            if (serverId == null || requestType == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("请求参数不完整"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("连接服务不可用"));
                return;
            }

            // 鏍规嵁璇锋眰绫诲瀷鑾峰彇浜岃繘鍒舵暟鎹?
            byte[] binaryData = getBinaryDataFromServer(serverId, requestType, data, connectionService);
            
            if (binaryData != null && binaryData.length > 0) {
                // 鍙戦€佷簩杩涘埗鏁版嵁
                session.sendBinary(MonitorSocketTopics.Remote.Custom.BINARY_DATA, binaryData);
                log.info("鍙戦€佷簩杩涘埗鏁版嵁鎴愬姛: serverId={}, type={}, size={}", serverId, requestType, binaryData.length);
            } else {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("获取二进制数据失败"));
            }

        } catch (Exception e) {
            log.error("澶勭悊浜岃繘鍒舵暟鎹姹傚け璐? {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("浜岃繘鍒舵暟鎹姹傚け璐? " + e.getMessage()));
        }
    }

    /**
     * 浠庢湇鍔″櫒鑾峰彇浜岃繘鍒舵暟鎹?
     * 鏍规嵁璇锋眰绫诲瀷杩斿洖鐩稿簲鐨勪簩杩涘埗鏁版嵁
     */
    private byte[] getBinaryDataFromServer(Integer serverId, String requestType, JsonObject data, ServerConnectionService connectionService) {
        try {
            byte[] result = null;
            
            // 鏍规嵁涓嶅悓鐨勮姹傜被鍨嬪鐞?
            switch (requestType) {
                case "file":
                    // 鑾峰彇鏂囦欢鏁版嵁
                    String filePath = data.getString("filePath");
                    if (filePath == null || filePath.trim().isEmpty()) {
                        log.warn("鏂囦欢璺緞涓虹┖: serverId={}", serverId);
                        return null;
                    }
                    log.debug("璇锋眰璇诲彇鏂囦欢: serverId={}, filePath={}", serverId, filePath);
                    result = connectionService.readFile(serverId, filePath);
                    if (result == null) {
                        log.warn("杩炴帴鏈嶅姟涓嶆敮鎸佹枃浠惰鍙栧姛鑳? serverId={}, protocol={}", serverId, connectionService.getProtocol());
                    }
                    break;
                
                case "screenshot":
                    // 鑾峰彇鎴浘鏁版嵁
                    log.debug("璇锋眰灞忓箷鎴浘: serverId={}", serverId);
                    result = connectionService.captureScreen(serverId);
                    if (result == null) {
                        log.warn("杩炴帴鏈嶅姟涓嶆敮鎸佸睆骞曟埅鍥惧姛鑳? serverId={}, protocol={}", serverId, connectionService.getProtocol());
                    }
                    break;
                
                case "custom":
                    // 鑷畾涔変簩杩涘埗鏁版嵁
                    String customCommand = data.getString("command");
                    if (customCommand == null || customCommand.trim().isEmpty()) {
                        log.warn("鑷畾涔夊懡浠や负绌? serverId={}", serverId);
                        return null;
                    }
                    log.debug("鎵ц浜岃繘鍒跺懡浠? serverId={}, command={}", serverId, customCommand);
                    result = connectionService.executeBinaryCommand(serverId, customCommand);
                    break;
                
                default:
                    log.warn("鏈煡鐨勪簩杩涘埗鏁版嵁璇锋眰绫诲瀷: serverId={}, type={}", serverId, requestType);
                    return null;
            }
            
            if (result != null && result.length > 0) {
                log.debug("鑾峰彇浜岃繘鍒舵暟鎹垚鍔? serverId={}, type={}, size={} bytes", serverId, requestType, result.length);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("鑾峰彇浜岃繘鍒舵暟鎹け璐? serverId={}, type={}, error={}", serverId, requestType, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 涓诲姩鍚戝鎴风鎺ㄩ€佷簩杩涘埗鏁版嵁
     * 鍙敱鏈嶅姟绔富鍔ㄨ皟鐢紝鍚戞寚瀹氭湇鍔″櫒ID鐨勪細璇濇帹閫佷簩杩涘埗鏁版嵁
     */
    public void pushBinaryData(Integer serverId, byte[] binaryData) {
        try {
            SocketSession session = serverSessionMap.get(serverId);
            if (session != null && binaryData != null && binaryData.length > 0) {
                session.sendBinary(MonitorSocketTopics.Remote.Custom.BINARY_DATA, binaryData);
                log.info("涓诲姩鎺ㄩ€佷簩杩涘埗鏁版嵁鎴愬姛: serverId={}, size={}", serverId, binaryData.length);
            } else {
                log.warn("鎺ㄩ€佷簩杩涘埗鏁版嵁澶辫触: serverId={}, session={}, dataSize={}", 
                    serverId, session != null ? "exist" : "null", binaryData != null ? binaryData.length : 0);
            }
        } catch (Exception e) {
            log.error("鎺ㄩ€佷簩杩涘埗鏁版嵁澶辫触: serverId={}, error={}", serverId, e.getMessage(), e);
        }
    }

    /**
     * 鎵归噺鍚戞墍鏈夎繛鎺ョ殑瀹㈡埛绔帹閫佷簩杩涘埗鏁版嵁
     */
    public void broadcastBinaryData(byte[] binaryData) {
        if (binaryData == null || binaryData.length == 0) {
            log.warn("骞挎挱浜岃繘鍒舵暟鎹け璐? 鏁版嵁涓虹┖");
            return;
        }

        int successCount = 0;
        for (Map.Entry<Integer, SocketSession> entry : serverSessionMap.entrySet()) {
            try {
                entry.getValue().sendBinary(MonitorSocketTopics.Remote.Custom.BINARY_DATA, binaryData);
                successCount++;
            } catch (Exception e) {
                log.error("鍚戝鎴风鎺ㄩ€佷簩杩涘埗鏁版嵁澶辫触: serverId={}, error={}", entry.getKey(), e.getMessage());
            }
        }
        log.info("骞挎挱浜岃繘鍒舵暟鎹畬鎴? 鎴愬姛={}, 鎬绘暟={}, 鏁版嵁澶у皬={}", successCount, serverSessionMap.size(), binaryData.length);
    }

    /**
     * 鑷畾涔夊睆骞?缁堢澶у皬璋冩暣浜嬩欢 (remote:custom:resize)
     */
    @OnEvent(MonitorSocketTopics.Remote.Custom.RESIZE)
    public void onResize(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            Integer width = data.getInteger("width");
            Integer height = data.getInteger("height");
            String protocol = data.getString("protocol");

            if (serverId == null || width == null || height == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("参数不完整"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService != null) {
                connectionService.resizeTerminal(serverId, width, height);
            }

        } catch (Exception e) {
            log.error("鑷畾涔夊睆骞曞ぇ灏忚皟鏁村け璐? {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("灞忓箷澶у皬璋冩暣澶辫触: " + e.getMessage()));
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

                log.info("浼氳瘽鏂紑锛屽凡娓呯悊鑷畾涔夎繛鎺? sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("浼氳瘽鏂紑娓呯悊澶辫触: {}", e.getMessage(), e);
        }
    }
}

