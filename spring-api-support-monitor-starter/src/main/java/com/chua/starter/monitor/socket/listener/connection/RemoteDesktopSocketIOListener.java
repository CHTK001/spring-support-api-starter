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
import com.chua.starter.monitor.service.impl.RemoteDesktopServerConnectionServiceImpl;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 杩滅▼妗岄潰 Socket IO 鐩戝惉鍣?
 * <p>
 * 澶勭悊鍩轰簬鍥剧墖浼犺緭鐨勮繙绋嬫闈㈣繛鎺ワ紝鏀寔锛?
 * - 鑷姩妫€娴嬫湇鍔＄妯″紡锛堟闈?SSH锛?
 * - 灞忓箷鍥剧墖瀹炴椂浼犺緭锛堜簩杩涘埗锛?
 * - 榧犳爣鍜岄敭鐩樹簨浠朵紶杈?
 * - 鐢昏川妯″紡鍜屽帇缂╁櫒閰嶇疆
 *
 * @author CH
 * @since 2024/12/28
 */
@Component
@Slf4j
public class RemoteDesktopSocketIOListener implements SocketListener {

    @Autowired
    @Lazy
    private RemoteDesktopServerConnectionServiceImpl remoteDesktopService;

    @Autowired
    @Lazy
    private SocketSessionTemplate socketSessionTemplate;

    @Autowired
    @Lazy
    private MonitorSysGenServerService serverService;

    // 瀛樺偍浼氳瘽涓庢湇鍔″櫒鐨勬槧灏勫叧绯?
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();
    private final Map<Integer, SocketSession> serverSessionMap = new ConcurrentHashMap<>();

    /**
     * 杩滅▼妗岄潰杩炴帴浜嬩欢
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.CONNECT)
    public void onConnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            
            if (serverId == null) {
                sendError(session, "鏈嶅姟鍣↖D涓嶈兘涓虹┖");
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                sendError(session, "鏈嶅姟鍣ㄩ厤缃笉瀛樺湪");
                return;
            }

            // 寤虹珛杩滅▼妗岄潰杩炴帴
            boolean connected = remoteDesktopService.connect(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);

                String mode = remoteDesktopService.getConnectionMode(serverId);

                // 璁剧疆鏁版嵁鍥炶皟
                setupDataCallback(serverId, session);

                // 鍙戦€佽繛鎺ユ垚鍔熸秷鎭?
                ServerWebSocketMessage response = ServerWebSocketMessage.builder()
                        .messageType("remote_desktop_connect")
                        .serverId(serverId)
                        .serverName(server.getMonitorSysGenServerName())
                        .serverHost(server.getMonitorSysGenServerHost())
                        .serverPort(server.getMonitorSysGenServerPort())
                        .serverProtocol("REMOTE")
                        .data(Map.of("mode", mode, "success", true))
                        .timestamp(System.currentTimeMillis())
                        .build();

                socketSessionTemplate.send(MonitorSocketTopics.Remote.Desktop.CONNECTED, Json.toJson(response));
                log.info("杩滅▼妗岄潰杩炴帴鎴愬姛: serverId={}, mode={}", serverId, mode);

                // 濡傛灉鏄?SSH 妯″紡锛岄€氱煡鍓嶇
                if ("SSH".equals(mode)) {
                    ServerWebSocketMessage modeMsg = ServerWebSocketMessage.builder()
                            .messageType("mode_detected")
                            .serverId(serverId)
                            .data(Map.of("mode", "SSH"))
                            .timestamp(System.currentTimeMillis())
                            .build();
                    session.send(MonitorSocketTopics.Remote.Desktop.MODE_DETECTED, Json.toJson(modeMsg));
                }
            } else {
                sendError(session, "杩滅▼妗岄潰杩炴帴澶辫触");
            }
        } catch (Exception e) {
            log.error("杩滅▼妗岄潰杩炴帴澶辫触: {}", e.getMessage(), e);
            sendError(session, "杩炴帴澶辫触: " + e.getMessage());
        }
    }

    /**
     * 榧犳爣绉诲姩浜嬩欢
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.MOUSE_MOVE)
    public void onMouseMove(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            int x = data.getIntValue("x", 0);
            int y = data.getIntValue("y", 0);
            remoteDesktopService.sendMouseMove(serverId, x, y);
        } catch (Exception e) {
            log.error("澶勭悊榧犳爣绉诲姩澶辫触: {}", e.getMessage());
        }
    }

    /**
     * 榧犳爣鐐瑰嚮浜嬩欢
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.MOUSE_CLICK)
    public void onMouseClick(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            int x = data.getIntValue("x", 0);
            int y = data.getIntValue("y", 0);
            int button = data.getIntValue("button", 1);
            boolean pressed = data.getBooleanValue("pressed", false);
            remoteDesktopService.sendMouseClick(serverId, x, y, button, pressed);
        } catch (Exception e) {
            log.error("澶勭悊榧犳爣鐐瑰嚮澶辫触: {}", e.getMessage());
        }
    }

    /**
     * 榧犳爣婊氳疆浜嬩欢
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.MOUSE_WHEEL)
    public void onMouseWheel(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            int x = data.getIntValue("x", 0);
            int y = data.getIntValue("y", 0);
            int amount = data.getIntValue("amount", 0);
            remoteDesktopService.sendMouseWheel(serverId, x, y, amount);
        } catch (Exception e) {
            log.error("澶勭悊榧犳爣婊氳疆澶辫触: {}", e.getMessage());
        }
    }

    /**
     * 閿洏鎸変笅浜嬩欢
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.KEY_PRESS)
    public void onKeyPress(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            int keyCode = data.getIntValue("keyCode", 0);
            String keyCharStr = data.getString("keyChar");
            char keyChar = (keyCharStr != null && !keyCharStr.isEmpty()) ? keyCharStr.charAt(0) : '\0';
            int modifiers = data.getIntValue("modifiers", 0);
            remoteDesktopService.sendKeyPress(serverId, keyCode, keyChar, modifiers);
        } catch (Exception e) {
            log.error("澶勭悊閿洏鎸変笅澶辫触: {}", e.getMessage());
        }
    }

    /**
     * 閿洏閲婃斁浜嬩欢
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.KEY_RELEASE)
    public void onKeyRelease(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            int keyCode = data.getIntValue("keyCode", 0);
            String keyCharStr = data.getString("keyChar");
            char keyChar = (keyCharStr != null && !keyCharStr.isEmpty()) ? keyCharStr.charAt(0) : '\0';
            int modifiers = data.getIntValue("modifiers", 0);
            remoteDesktopService.sendKeyRelease(serverId, keyCode, keyChar, modifiers);
        } catch (Exception e) {
            log.error("澶勭悊閿洏閲婃斁澶辫触: {}", e.getMessage());
        }
    }

    /**
     * 璁剧疆鐢昏川妯″紡
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.SET_QUALITY)
    public void onSetQuality(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            String mode = data.getString("mode");
            if (mode != null) {
                remoteDesktopService.setQualityMode(serverId, mode);
                log.info("璁剧疆鐢昏川妯″紡: serverId={}, mode={}", serverId, mode);
            }
        } catch (Exception e) {
            log.error("璁剧疆鐢昏川妯″紡澶辫触: {}", e.getMessage());
        }
    }

    /**
     * 璁剧疆鍘嬬缉鍣?
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.SET_COMPRESSOR)
    public void onSetCompressor(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            String compressor = data.getString("compressor");
            if (compressor != null) {
                remoteDesktopService.setCompressor(serverId, compressor);
                log.info("璁剧疆鍘嬬缉鍣? serverId={}, compressor={}", serverId, compressor);
            }
        } catch (Exception e) {
            log.error("璁剧疆鍘嬬缉鍣ㄥけ璐? {}", e.getMessage());
        }
    }

    /**
     * 閲嶇疆鎹曡幏锛堝己鍒跺彂閫佸叏灞忥級
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.CAPTURE_RESET)
    public void onCaptureReset(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId != null) {
                remoteDesktopService.requestCaptureReset(serverId);
                log.info("璇锋眰閲嶇疆鎹曡幏: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("閲嶇疆鎹曡幏澶辫触: {}", e.getMessage());
        }
    }

    /**
     * 鏂紑杩炴帴浜嬩欢
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.DISCONNECT)
    public void onDisconnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data != null ? data.getInteger("serverId") : null;
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                remoteDesktopService.disconnect(serverId);
                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);

                ServerWebSocketMessage response = ServerWebSocketMessage.builder()
                        .messageType("remote_desktop_disconnect")
                        .serverId(serverId)
                        .data(Map.of("reason", "鐢ㄦ埛涓诲姩鏂紑"))
                        .timestamp(System.currentTimeMillis())
                        .build();
                socketSessionTemplate.send(MonitorSocketTopics.Remote.Desktop.DISCONNECTED, Json.toJson(response));

                log.info("杩滅▼妗岄潰杩炴帴宸叉柇寮€: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("鏂紑杩滅▼妗岄潰杩炴帴澶辫触: {}", e.getMessage(), e);
        }
    }

    /**
     * 璁剧疆鏁版嵁鍥炶皟锛堝睆骞曞浘鐗囦互浜岃繘鍒舵牸寮忓彂閫侊級
     */
    private void setupDataCallback(Integer serverId, SocketSession session) {
        // 浣跨敤浜岃繘鍒跺洖璋冿紝鐩存帴鍙戦€佸師濮嬪浘鍍忓瓧鑺?
        remoteDesktopService.setBinaryDataCallback(serverId, (imageBytes) -> {
            try {
                // 鐩存帴鍙戦€佷簩杩涘埗鍥惧儚鏁版嵁
                session.send(MonitorSocketTopics.Remote.Desktop.SCREEN, imageBytes);
            } catch (Exception e) {
                log.error("鍙戦€佸睆骞曟暟鎹け璐? {}", e.getMessage());
            }
        });
    }

    /**
     * 鍙戦€侀敊璇秷鎭?
     */
    private void sendError(SocketSession session, String message) {
        try {
            session.send(MonitorSocketTopics.Remote.Desktop.ERROR, ReturnResult.illegal(message));
        } catch (Exception e) {
            log.error("鍙戦€侀敊璇秷鎭け璐? {}", e.getMessage());
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
                remoteDesktopService.disconnect(serverId);
                sessionServerMap.remove(sessionId);
                serverSessionMap.remove(serverId);
                log.info("浼氳瘽鏂紑锛屽凡娓呯悊杩滅▼妗岄潰杩炴帴: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("浼氳瘽鏂紑娓呯悊澶辫触: {}", e.getMessage(), e);
        }
    }
}

