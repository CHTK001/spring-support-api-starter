package com.chua.starter.monitor.websocket;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.net.UrlQuery;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.monitor.arthas.support.ArthasProxyUtils;
import com.chua.starter.monitor.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.service.NodeManagementService;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import static com.chua.report.client.arthas.starter.environment.ArthasReportDiscoveryEnvironment.REPORT_ARTHAS_CLIENT_ID;

/**
 * @author CH
 * @since 2025/8/18 8:43
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ServerEndpoint("/v1/arthas/console/ws")
public class ArthasWebSocketHandler {

    private NodeManagementService nodeManagementService;

    /** 节点元数据中的 Arthas Tunnel 地址键名 */
    private static final String ARTHAS_META_KEY = ArthasProxyUtils.ARTHAS_META_KEY;

    /** 下游 sessionId -> 上游 WebSocket 映射 */
    private static final Map<String, WebSocket> sessionToUpstream = new ConcurrentHashMap<>();
    public ArthasWebSocketHandler() {
        nodeManagementService = SpringBeanUtils.getBean(NodeManagementService.class);
    }
    @OnOpen
    public void onOpen(Session session) throws IOException {
        log.info("Arthas WS 建立: sessionId={}, uri={}", session.getId(), session.getRequestURI());
        UrlQuery urlQuery = UrlQuery.of(session.getRequestURI().getQuery(), StandardCharsets.UTF_8);
        log.info("Arthas WS 建立: sessionId={}, uri={}, query={}", session.getId(), session.getRequestURI(), urlQuery);
        String nodeId = urlQuery.get("nodeId");
        if (StringUtils.isBlank(nodeId)) {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "缺少 nodeId"));
            return;
        }
        ReturnResult<OnlineNodeInfo> nodeResult = nodeManagementService.getNodeDetails(nodeId);
        if (!nodeResult.isSuccess() || nodeResult.getData() == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "节点不可用"));
            return;
        }
        OnlineNodeInfo node = nodeResult.getData();
        Object tunnel = node.getMetadata() == null ? null : node.getMetadata().get(ARTHAS_META_KEY);
        if (tunnel == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "节点未配置 Arthas 元数据"));
            return;
        }
        String upstream = String.valueOf(tunnel).trim();
        if (StringUtils.isBlank(upstream)) {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Arthas 地址为空"));
            return;
        }

        try {
            // 确保为 ws(s)://.../ws，若为 http(s):// 则转换为 ws(s)://
            if (!(StringUtils.startsWithIgnoreCase(upstream, "ws://")
                    || StringUtils.startsWithIgnoreCase(upstream, "wss://"))) {
                if (StringUtils.startsWithIgnoreCase(upstream, "http://")) {
                    upstream = "ws://" + upstream.substring("http://".length());
                } else if (StringUtils.startsWithIgnoreCase(upstream, "https://")) {
                    upstream = "wss://" + upstream.substring("https://".length());
                }
            }
            upstream = upstream.trim() + "?method=connectArthas&id=" + node.getString(REPORT_ARTHAS_CLIENT_ID);

            CompletableFuture<WebSocket> fut = HttpClient.newHttpClient().newWebSocketBuilder().connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(URI.create(upstream), new UpstreamListener(session));

            String finalUpstream = upstream;
            fut.whenComplete((ws, ex) -> {
                if (ex != null) {
                    log.error("连接上游 Arthas WS 失败: {}", finalUpstream, ex);
                    tryClose(session, new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, "上游连接失败"));
                } else {
                    sessionToUpstream.put(session.getId(), ws);
                    log.info("上游 Arthas WS 已连接: {}", finalUpstream);
                }
            });
        } catch (Exception e) {
            log.error("建立上游 WS 连接异常", e);
            tryClose(session, new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, "上游连接异常"));
        }
    }


    @OnMessage
    public void onMessage(String message, Session session) {
        WebSocket upstream = sessionToUpstream.get(session.getId());
        if (upstream != null) {
            upstream.sendText(message, true);
        }
    }
    @OnMessage
    public void onMessage(ByteBuffer byteBuffer, Session session) {
        WebSocket upstream = sessionToUpstream.get(session.getId());
        if (upstream != null) {
            upstream.sendBinary(byteBuffer, true);
        }
    }


    @OnClose
    public void onClose(Session session) {
        WebSocket upstream = sessionToUpstream.remove(session.getId());
        if (upstream != null) {
            try {
                upstream.sendClose(WebSocket.NORMAL_CLOSURE, "client closed");
            } catch (Exception ignored) {
            }
        }
        log.info("Arthas WS 关闭: sessionId={}, status={}", session.getId(), WebSocket.NORMAL_CLOSURE);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.warn("Arthas WS 传输错误: sessionId={}, err={}", session.getId(), throwable.getMessage());
        tryClose(session, new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, throwable.getMessage()));
    }

    /** 尝试关闭 WebSocketSession */
    private void tryClose(Session session, CloseReason reason) {
        try {
            session.close(reason);
        } catch (Exception ignored) {
        }
    }

    /** 上游 WS 监听器：将上游消息转发给前端会话 */
    private static class UpstreamListener implements WebSocket.Listener {
        private final Session downstream;

        UpstreamListener(Session downstream) {
            this.downstream = downstream;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            try {
                if (downstream.isOpen()){
                    downstream.getAsyncRemote().sendText(data.toString());
                }
            } catch (Exception ignored) {
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            try {
                if (downstream.isOpen()) {
                    downstream.getAsyncRemote().sendBinary(data);
                }
            } catch (Exception ignored) {
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            try {
                if (downstream.isOpen()) {
                    downstream.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, reason));
                }
            } catch (Exception ignored) {
            }
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            try {
                if (downstream.isOpen()){
                    downstream.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, error.getMessage()));

                }
            } catch (Exception ignored) {
            }
        }
    }
}
