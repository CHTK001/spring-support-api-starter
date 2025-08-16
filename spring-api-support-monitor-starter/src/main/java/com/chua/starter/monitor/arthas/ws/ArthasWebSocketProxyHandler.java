package com.chua.starter.monitor.arthas.ws;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.service.NodeManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Arthas 控制台 WebSocket 反向代理处理器
 *
 * 前端通过 /v1/arthas/console/{nodeId}/ws 建立 WS 连接： 1) 从节点元数据中读取
 * report.client.arthas.port（ws(s)://host:port/ws） 2) 后端与上游建立 WS
 * 连接，转发双向消息，实现“后端连接 arthas-console”
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArthasWebSocketProxyHandler extends AbstractWebSocketHandler {

    /** 节点元数据中的 Arthas Tunnel 地址键名 */
    private static final String ARTHAS_META_KEY = com.chua.starter.monitor.arthas.support.ArthasProxyUtils.ARTHAS_META_KEY;

    private final NodeManagementService nodeManagementService;

    /** 下游 sessionId -> 上游 WebSocket 映射 */
    private final Map<String, WebSocket> sessionToUpstream = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String uri = session.getUri() == null ? null : session.getUri().toString();
        String nodeId = extractNodeId(session.getUri());
        log.info("Arthas WS 建立: sessionId={}, uri={}, nodeId={}", session.getId(), uri, nodeId);

        if (StringUtils.isBlank(nodeId)) {
            session.close(CloseStatus.BAD_DATA.withReason("缺少 nodeId"));
            return;
        }

        ReturnResult<OnlineNodeInfo> nodeResult = nodeManagementService.getNodeDetails(nodeId);
        if (!nodeResult.isSuccess() || nodeResult.getData() == null) {
            session.close(CloseStatus.BAD_DATA.withReason("节点不可用"));
            return;
        }

        OnlineNodeInfo node = nodeResult.getData();
        Object tunnel = node.getMetadata() == null ? null : node.getMetadata().get(ARTHAS_META_KEY);
        if (tunnel == null) {
            session.close(CloseStatus.BAD_DATA.withReason("节点未配置 Arthas 元数据"));
            return;
        }
        String upstream = String.valueOf(tunnel).trim();
        if (StringUtils.isBlank(upstream)) {
            session.close(CloseStatus.BAD_DATA.withReason("Arthas 地址为空"));
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

            HttpClient client = HttpClient.newHttpClient();
            CompletableFuture<WebSocket> fut = client.newWebSocketBuilder().connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(URI.create(upstream), new UpstreamListener(session));

            String finalUpstream = upstream;
            fut.whenComplete((ws, ex) -> {
                if (ex != null) {
                    log.error("连接上游 Arthas WS 失败: {}", finalUpstream, ex);
                    tryClose(session, CloseStatus.SERVER_ERROR.withReason("上游连接失败"));
                } else {
                    sessionToUpstream.put(session.getId(), ws);
                    log.info("上游 Arthas WS 已连接: {}", finalUpstream);
                }
            });
        } catch (Exception e) {
            log.error("建立上游 WS 连接异常", e);
            tryClose(session, CloseStatus.SERVER_ERROR.withReason("上游连接异常"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WebSocket upstream = sessionToUpstream.get(session.getId());
        if (upstream != null) {
            upstream.sendText(message.getPayload(), true);
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        WebSocket upstream = sessionToUpstream.get(session.getId());
        if (upstream != null) {
            upstream.sendBinary(message.getPayload(), true);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("Arthas WS 传输错误: sessionId={}, err={}", session.getId(), exception.getMessage());
        tryClose(session, CloseStatus.SERVER_ERROR);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        WebSocket upstream = sessionToUpstream.remove(session.getId());
        if (upstream != null) {
            try {
                upstream.sendClose(WebSocket.NORMAL_CLOSURE, "client closed");
            } catch (Exception ignored) {
            }
        }
        log.info("Arthas WS 关闭: sessionId={}, status={}", session.getId(), status);
    }

    private void tryClose(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception ignored) {
        }
    }

    /**
     * 从路径 /v1/arthas/console/{nodeId}/ws 中提取 nodeId
     */
    private String extractNodeId(URI uri) {
        if (uri == null)
            return null;
        String path = uri.getPath();
        // 简单解析：/v1/arthas/console/{nodeId}/ws
        String prefix = "/v1/arthas/console/";
        String suffix = "/ws";
        int i = StringUtils.indexOf(path, prefix);
        int j = StringUtils.lastIndexOf(path, suffix);
        if (i >= 0 && j > i) {
            String middle = path.substring(i + prefix.length(), j);
            if (middle.endsWith("/"))
                middle = middle.substring(0, middle.length() - 1);
            return middle;
        }
        return null;
    }

    /** 上游 WS 监听器：将上游消息转发给前端会话 */
    private static class UpstreamListener implements WebSocket.Listener {
        private final WebSocketSession downstream;

        UpstreamListener(WebSocketSession downstream) {
            this.downstream = downstream;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            try {
                if (downstream.isOpen())
                    downstream.sendMessage(new TextMessage(data.toString()));
            } catch (Exception ignored) {
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            try {
                if (downstream.isOpen())
                    downstream.sendMessage(new BinaryMessage(data));
            } catch (Exception ignored) {
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            try {
                if (downstream.isOpen())
                    downstream.close(CloseStatus.NORMAL);
            } catch (Exception ignored) {
            }
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            try {
                if (downstream.isOpen())
                    downstream.close(CloseStatus.SERVER_ERROR);
            } catch (Exception ignored) {
            }
        }
    }
}
