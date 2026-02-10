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
import com.chua.starter.monitor.service.impl.WebRTCServiceMonitor;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.webrtc.WebRTCWebSocketClientManager;
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
     * WebSocket 客户端管理器
     */
    private final WebRTCWebSocketClientManager webSocketClientManager = new WebRTCWebSocketClientManager();

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
            MonitorSysGenServer server = null;

            // 如果提供了 serverId，直接使用
            if (serverId != null) {
                server = serverService.getById(String.valueOf(serverId));
                if (server == null) {
                    sendError(session, "服务器配置不存在: serverId=" + serverId);
                    return;
                }
            } else {
                // 如果没有提供 serverId，尝试根据 host 查找
                String host = data.getString("host");
                if (host != null && !host.isEmpty()) {
                    server = serverService.findByIpAddress(host);
                    if (server != null) {
                        serverId = server.getMonitorSysGenServerId();
                    }
                }

                // 如果还是找不到，返回错误
                if (server == null || serverId == null) {
                    sendError(session, "请提供服务器ID或有效的服务器地址");
                    return;
                }
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

                // 建立与 Rust WebRTC 服务端的 WebSocket 连接
                if (webSocketUrl != null && !webSocketUrl.isEmpty()) {
                    final Integer finalServerId = serverId;
                    var rustSession = webSocketClientManager.getOrCreateConnection(serverId, webSocketUrl);
                    if (rustSession != null) {
                        // 注册消息处理器，用于接收来自 Rust 服务端的消息
                        var handlerId = "listener_" + serverId;
                        webSocketClientManager.registerMessageHandler(serverId, handlerId, (message) -> {
                            try {
                                // 解析消息，判断类型并转发到前端
                                JsonObject msgData = Json.getJsonObject(message);
                                if (msgData != null) {
                                    String msgType = msgData.getString("type");
                                    if ("answer".equals(msgType)) {
                                        // 转发 Answer 到前端
                                        onAnswer(session, message);
                                    } else if ("ice_candidate".equals(msgType)) {
                                        // 转发 ICE Candidate 到前端
                                        onIceCandidate(session, message);
                                    }
                                }
                            } catch (Exception e) {
                                log.error("[WebRTC][Socket] 处理来自Rust服务端的消息失败: serverId={}", finalServerId, e);
                            }
                        });
                        log.info("[WebRTC][Socket] 已建立与Rust服务端的WebSocket连接: serverId={}", serverId);
                    } else {
                        log.warn("[WebRTC][Socket] 无法建立与Rust服务端的WebSocket连接: serverId={}, url={}", serverId, webSocketUrl);
                    }
                }

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
                
                // 关闭与 Rust 服务端的 WebSocket 连接
                webSocketClientManager.closeConnection(serverId);

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
     * <p>
     * 前端发送 offer → 后端接收 → 转发到 Rust WebRTC 服务端
     * </p>
     *
     * @param session  前端会话
     * @param rawData  offer 数据（JSON格式）
     */
    @OnEvent(MonitorSocketTopics.Remote.WebRTC.OFFER)
    public void onOffer(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                log.warn("[WebRTC][Socket] 收到Offer但找不到对应的serverId: sessionId={}", session.getId());
                return;
            }

            // 获取 Rust WebRTC 服务端的 WebSocket URL
            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                log.error("[WebRTC][Socket] 找不到服务器配置: serverId={}", serverId);
                return;
            }

            String webSocketUrl = webrtcService.getWebSocketUrl(serverId, server.getMonitorSysGenServerHost(), server.getMonitorSysGenServerPort());
            if (webSocketUrl == null || webSocketUrl.isEmpty()) {
                log.error("[WebRTC][Socket] 无法获取WebSocket URL: serverId={}", serverId);
                return;
            }

            // 确保 WebSocket 连接已建立
            var rustSession = webSocketClientManager.getOrCreateConnection(serverId, webSocketUrl);
            if (rustSession == null) {
                log.error("[WebRTC][Socket] 无法建立与Rust服务端的WebSocket连接: serverId={}", serverId);
                return;
            }

            // 构造信令消息，添加 serverId
            JsonObject offerData = Json.getJsonObject(rawData);
            if (offerData == null) {
                offerData = new JsonObject();
            }
            offerData.put("serverId", serverId);
            offerData.put("type", "offer");
            
            String offerMessage = Json.toJson(offerData);
            
            // 转发 offer 到 Rust WebRTC 服务端
            boolean sent = webSocketClientManager.sendMessage(serverId, offerMessage);
            if (sent) {
                log.info("[WebRTC][Socket] 已转发Offer到Rust服务端: serverId={}", serverId);
            } else {
                log.error("[WebRTC][Socket] 转发Offer到Rust服务端失败: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("[WebRTC][Socket] 处理Offer失败", e);
        }
    }

    /**
     * WebRTC Answer事件（信令交换）
     * <p>
     * Rust 服务端返回 answer → 后端接收 → 转发回前端
     * </p>
     *
     * @param session  会话（可能是 Rust 服务端或前端）
     * @param rawData  answer 数据（JSON格式）
     */
    @OnEvent(MonitorSocketTopics.Remote.WebRTC.ANSWER)
    public void onAnswer(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                // 可能是从 Rust 服务端收到的 answer，需要通过 serverId 查找对应的前端会话
                JsonObject data = Json.getJsonObject(rawData);
                if (data != null) {
                    serverId = data.getInteger("serverId");
                }
            }

            if (serverId == null) {
                log.warn("[WebRTC][Socket] 收到Answer但找不到对应的serverId");
                return;
            }

            // 转发 answer 到前端会话
            SocketSession frontendSession = serverSessionMap.get(serverId);
            if (frontendSession != null) {
                frontendSession.send(MonitorSocketTopics.Remote.WebRTC.ANSWER, rawData);
                log.debug("[WebRTC][Socket] 转发Answer到前端: serverId={}", serverId);
            } else {
                log.warn("[WebRTC][Socket] 找不到前端会话: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("[WebRTC][Socket] 处理Answer失败", e);
        }
    }

    /**
     * WebRTC ICE Candidate事件（信令交换）
     * <p>
     * 双向转发：前端 ↔ 后端 ↔ Rust 服务端
     * </p>
     *
     * @param session  会话
     * @param rawData  ICE candidate 数据（JSON格式）
     */
    @OnEvent(MonitorSocketTopics.Remote.WebRTC.ICE_CANDIDATE)
    public void onIceCandidate(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                // 可能是从 Rust 服务端收到的 ICE candidate
                JsonObject data = Json.getJsonObject(rawData);
                if (data != null) {
                    serverId = data.getInteger("serverId");
                }
            }

            if (serverId == null) {
                log.warn("[WebRTC][Socket] 收到ICE Candidate但找不到对应的serverId");
                return;
            }

            // 判断消息来源：如果是前端发送的，转发到 Rust 服务端；如果是 Rust 服务端发送的，转发到前端
            SocketSession frontendSession = serverSessionMap.get(serverId);
            if (frontendSession != null && frontendSession.getId().equals(session.getId())) {
                // 来自前端，转发到 Rust 服务端
                MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
                if (server == null) {
                    log.error("[WebRTC][Socket] 找不到服务器配置: serverId={}", serverId);
                    return;
                }
                
                String webSocketUrl = webrtcService.getWebSocketUrl(serverId, server.getMonitorSysGenServerHost(), server.getMonitorSysGenServerPort());
                if (webSocketUrl != null && !webSocketUrl.isEmpty()) {
                    // 确保 WebSocket 连接已建立
                    var rustSession = webSocketClientManager.getOrCreateConnection(serverId, webSocketUrl);
                    if (rustSession != null) {
                        // 构造信令消息，添加 serverId
                        JsonObject iceData = Json.getJsonObject(rawData);
                        if (iceData == null) {
                            iceData = new JsonObject();
                        }
                        iceData.put("serverId", serverId);
                        iceData.put("type", "ice_candidate");
                        
                        String iceMessage = Json.toJson(iceData);
                        
                        // 转发 ICE Candidate 到 Rust 服务端
                        boolean sent = webSocketClientManager.sendMessage(serverId, iceMessage);
                        if (sent) {
                            log.debug("[WebRTC][Socket] 已转发ICE Candidate到Rust服务端: serverId={}", serverId);
                        } else {
                            log.error("[WebRTC][Socket] 转发ICE Candidate到Rust服务端失败: serverId={}", serverId);
                        }
                    }
                }
            } else {
                // 来自 Rust 服务端，转发到前端
                if (frontendSession != null) {
                    frontendSession.send(MonitorSocketTopics.Remote.WebRTC.ICE_CANDIDATE, rawData);
                    log.debug("[WebRTC][Socket] 已转发ICE Candidate到前端: serverId={}", serverId);
                }
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
                
                // 关闭与 Rust 服务端的 WebSocket 连接
                webSocketClientManager.closeConnection(serverId);
                
                log.info("[WebRTC][Socket] 会话断开，已清理WebRTC连接: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("[WebRTC][Socket] 会话断开清理失败", e);
        }
    }
}
