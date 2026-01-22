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
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import com.chua.starter.monitor.service.impl.WebRTCServiceMonitor;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebRTC Socket 监听器
 * <p>
 * 处理WebRTC远程桌面连接，支持：
 * - WebRTC信令交换（offer/answer/ice-candidate）
 * - 实时视频流传输
 * - 连接状态管理
 *
 * @author CH
 * @since 2026/01/03
 */
@Component
@Slf4j
public class WebRTCSocketListener implements SocketListener {

    @Autowired
    @Lazy
    private WebRTCServiceMonitor webrtcService;

    @Autowired
    @Lazy
    private SocketSessionTemplate socketSessionTemplate;

    @Autowired
    @Lazy
    private MonitorSysGenServerService serverService;

    /**
     * 会话与服务器的映射关系
     */
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();
    private final Map<Integer, SocketSession> serverSessionMap = new ConcurrentHashMap<>();

    /**
     * WebRTC连接事件
     */
    @OnEvent(MonitorSocketTopics.Remote.WebRTC.CONNECT)
    public void onConnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");

            if (serverId == null) {
                sendError(session, "服务器ID不能为空");
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                sendError(session, "服务器配置不存在");
                return;
            }

            // 启动WebRTC服务
            int port = data.getIntValue("port", server.getMonitorSysGenServerPort());
            long handle = webrtcService.startService(serverId, port);

            if (handle != 0) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);

                String host = data.getString("host");
                if (host == null || host.isEmpty()) {
                    host = server.getMonitorSysGenServerHost();
                }

                String clientUrl = webrtcService.getClientUrl(serverId, host, port);
                String webSocketUrl = webrtcService.getWebSocketUrl(serverId, host, port);

                ServerWebSocketMessage response = ServerWebSocketMessage.builder()
                        .messageType("webrtc_connect")
                        .serverId(serverId)
                        .serverName(server.getMonitorSysGenServerName())
                        .serverHost(host)
                        .serverPort(port)
                        .serverProtocol("WebRTC")
                        .data(Map.of(
                                "success", true,
                                "handle", handle,
                                "clientUrl", clientUrl != null ? clientUrl : "",
                                "webSocketUrl", webSocketUrl != null ? webSocketUrl : ""
                        ))
                        .timestamp(System.currentTimeMillis())
                        .build();

                socketSessionTemplate.send(MonitorSocketTopics.Remote.WebRTC.CONNECTED, Json.toJson(response));
                log.info("[WebRTC][Socket] WebRTC连接成功: serverId={}, handle={}, port={}", serverId, handle, port);
            } else {
                sendError(session, "WebRTC服务启动失败");
            }
        } catch (Exception e) {
            log.error("[WebRTC][Socket] WebRTC连接失败", e);
            sendError(session, "连接失败: " + e.getMessage());
        }
    }

    /**
     * WebRTC断开事件
     */
    @OnEvent(MonitorSocketTopics.Remote.WebRTC.DISCONNECT)
    public void onDisconnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data != null ? data.getInteger("serverId") : null;
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                webrtcService.stopService(serverId);
                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);

                ServerWebSocketMessage response = ServerWebSocketMessage.builder()
                        .messageType("webrtc_disconnect")
                        .serverId(serverId)
                        .data(Map.of("reason", "用户主动断开"))
                        .timestamp(System.currentTimeMillis())
                        .build();

                socketSessionTemplate.send(MonitorSocketTopics.Remote.WebRTC.DISCONNECTED, Json.toJson(response));
                log.info("[WebRTC][Socket] WebRTC连接已断开: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("[WebRTC][Socket] 断开WebRTC连接失败", e);
        }
    }

    /**
     * WebRTC Offer事件（信令交换）
     */
    @OnEvent(MonitorSocketTopics.Remote.WebRTC.OFFER)
    public void onOffer(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                return;
            }

            // 转发offer到其他会话（如果需要）
            JsonObject data = Json.getJsonObject(rawData);
            SocketSession targetSession = serverSessionMap.get(serverId);
            if (targetSession != null && !targetSession.getId().equals(session.getId())) {
                targetSession.send(MonitorSocketTopics.Remote.WebRTC.OFFER, rawData);
            }
        } catch (Exception e) {
            log.error("[WebRTC][Socket] 处理Offer失败", e);
        }
    }

    /**
     * WebRTC Answer事件（信令交换）
     */
    @OnEvent(MonitorSocketTopics.Remote.WebRTC.ANSWER)
    public void onAnswer(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                return;
            }

            // 转发answer到其他会话（如果需要）
            JsonObject data = Json.getJsonObject(rawData);
            SocketSession targetSession = serverSessionMap.get(serverId);
            if (targetSession != null && !targetSession.getId().equals(session.getId())) {
                targetSession.send(MonitorSocketTopics.Remote.WebRTC.ANSWER, rawData);
            }
        } catch (Exception e) {
            log.error("[WebRTC][Socket] 处理Answer失败", e);
        }
    }

    /**
     * WebRTC ICE Candidate事件（信令交换）
     */
    @OnEvent(MonitorSocketTopics.Remote.WebRTC.ICE_CANDIDATE)
    public void onIceCandidate(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                return;
            }

            // 转发ICE candidate到其他会话（如果需要）
            JsonObject data = Json.getJsonObject(rawData);
            SocketSession targetSession = serverSessionMap.get(serverId);
            if (targetSession != null && !targetSession.getId().equals(session.getId())) {
                targetSession.send(MonitorSocketTopics.Remote.WebRTC.ICE_CANDIDATE, rawData);
            }
        } catch (Exception e) {
            log.error("[WebRTC][Socket] 处理ICE Candidate失败", e);
        }
    }

    /**
     * WebRTC 视频流开始事件
     */
    @OnEvent(MonitorSocketTopics.Remote.WebRTC.STREAM_STARTED)
    public void onStreamStarted(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                return;
            }

            JsonObject data = Json.getJsonObject(rawData);
            ServerWebSocketMessage response = ServerWebSocketMessage.builder()
                    .messageType("webrtc_stream_started")
                    .serverId(serverId)
                    .data(Map.of(
                            "success", true,
                            "timestamp", System.currentTimeMillis()
                    ))
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(MonitorSocketTopics.Remote.WebRTC.STREAM_STARTED, Json.toJson(response));
            log.info("[WebRTC][Socket] 视频流已开始: serverId={}", serverId);
        } catch (Exception e) {
            log.error("[WebRTC][Socket] 处理视频流开始事件失败", e);
        }
    }

    /**
     * WebRTC 视频流统计事件（帧率、分辨率等）
     */
    @OnEvent(MonitorSocketTopics.Remote.WebRTC.STREAM_STATS)
    public void onStreamStats(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                return;
            }

            JsonObject data = Json.getJsonObject(rawData);
            ServerWebSocketMessage response = ServerWebSocketMessage.builder()
                    .messageType("webrtc_stream_stats")
                    .serverId(serverId)
                    .data(data != null ? data.toMap() : Map.of())
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(MonitorSocketTopics.Remote.WebRTC.STREAM_STATS, Json.toJson(response));
        } catch (Exception e) {
            log.error("[WebRTC][Socket] 处理视频流统计事件失败", e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendError(SocketSession session, String message) {
        try {
            session.send(MonitorSocketTopics.Remote.WebRTC.ERROR, ReturnResult.illegal(message));
        } catch (Exception e) {
            log.error("[WebRTC][Socket] 发送错误消息失败", e);
        }
    }

    /**
     * 会话断开时的清理工作
     */
    public void onSessionDisconnect(SocketSession session) {
        try {
            String sessionId = session.getId();
            Integer serverId = sessionServerMap.get(sessionId);

            if (serverId != null) {
                webrtcService.stopService(serverId);
                sessionServerMap.remove(sessionId);
                serverSessionMap.remove(serverId);
                log.info("[WebRTC][Socket] 会话断开，已清理WebRTC连接: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("[WebRTC][Socket] 会话断开清理失败", e);
        }
    }
}

