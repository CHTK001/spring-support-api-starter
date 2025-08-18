package com.chua.starter.monitor.arthas;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.service.NodeManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Arthas 命令执行（HTTP接口）
 *
 * 说明：
 * - 前端通过 HTTP 调用本接口，后端在内部通过 WebSocket 与上游 arthas 建立连接，发送命令并收集输出后返回
 * - 适用于“非终端”场景（线程、trace、jvm、heap 等一次性输出类），不提供持续流式输出
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/arthas")
@Tag(name = "Arthas 命令执行(HTTP)")
public class ArthasCommandController {

    private static final String ARTHAS_META_KEY = com.chua.starter.monitor.arthas.support.ArthasProxyUtils.ARTHAS_META_KEY;

    private final NodeManagementService nodeManagementService;

    /**
     * 请求体
     */
    @lombok.Data
    public static class ExecRequest {
        /** 节点ID（必填） */
        private String nodeId;
        /** 要执行的 arthas 命令（必填），例如：thread -n 20 */
        private String command;
        /** 收集输出的毫秒数（可选，默认3000ms） */
        private Integer collectMillis;
    }

    /**
     * 通过 HTTP 执行 arthas 命令，收集一段时间输出并返回
     */
    @PostMapping("/exec")
    @Operation(summary = "执行 Arthas 命令(HTTP)", description = "内部通过WS连接上游arthas，发送命令并收集输出一段时间后返回")
    public ReturnResult<Map<String, Object>> exec(@RequestBody ExecRequest req) {
        try {
            if (req == null || StringUtils.isBlank(req.getNodeId()) || StringUtils.isBlank(req.getCommand())) {
                return ReturnResult.error("参数无效：nodeId/command 不能为空");
            }
            int collectMs = req.getCollectMillis() == null || req.getCollectMillis() <= 0 ? 3000 : req.getCollectMillis();

            ReturnResult<OnlineNodeInfo> nodeResult = nodeManagementService.getNodeDetails(req.getNodeId());
            if (!nodeResult.isSuccess() || nodeResult.getData() == null) {
                return ReturnResult.error("节点不可用");
            }
            OnlineNodeInfo node = nodeResult.getData();
            Object tunnel = node.getMetadata() == null ? null : node.getMetadata().get(ARTHAS_META_KEY);
            if (tunnel == null) {
                return ReturnResult.error("节点未配置 Arthas 元数据");
            }
            String upstream = String.valueOf(tunnel).trim();
            if (StringUtils.isBlank(upstream)) {
                return ReturnResult.error("Arthas 地址为空");
            }
            // 归一化为 ws(s)://.../ws
            if (!(StringUtils.startsWithIgnoreCase(upstream, "ws://") || StringUtils.startsWithIgnoreCase(upstream, "wss://"))) {
                if (StringUtils.startsWithIgnoreCase(upstream, "http://")) {
                    upstream = "ws://" + upstream.substring("http://".length());
                } else if (StringUtils.startsWithIgnoreCase(upstream, "https://")) {
                    upstream = "wss://" + upstream.substring("https://".length());
                }
            }

            StringBuilder out = new StringBuilder();
            CountDownLatch connected = new CountDownLatch(1);
            CountDownLatch finished = new CountDownLatch(1);

            HttpClient client = HttpClient.newHttpClient();
            CompletableFuture<WebSocket> fut = client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(URI.create(upstream), new WebSocket.Listener() {
                        @Override
                        public void onOpen(WebSocket webSocket) {
                            connected.countDown();
                            webSocket.request(1);
                        }

                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            out.append(data);
                            webSocket.request(1);
                            return null;
                        }

                        @Override
                        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                            finished.countDown();
                            return null;
                        }

                        @Override
                        public void onError(WebSocket webSocket, Throwable error) {
                            finished.countDown();
                        }
                    });

            WebSocket ws = fut.join();
            // 等待连接
            connected.await(2, TimeUnit.SECONDS);
            // 发送命令并回车
            ws.sendText(req.getCommand() + "\n", true);
            // 收集输出指定时间
            TimeUnit.MILLISECONDS.sleep(collectMs);
            try { ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye"); } catch (Exception ignore) {}
            finished.await(1, TimeUnit.SECONDS);

            return ReturnResult.success(Map.of(
                    "nodeId", req.getNodeId(),
                    "command", req.getCommand(),
                    "collectMillis", collectMs,
                    "output", out.toString()
            ));
        } catch (Exception e) {
            return ReturnResult.error("执行失败: " + e.getMessage());
        }
    }

    // ========== 语义化HTTP接口：thread/jvm/heap/trace ==========

    @lombok.Data
    public static class ThreadRequest {
        private String nodeId;
        private String threadId; // 优先
        private Boolean blocking; // 仅阻塞
        private Integer count; // -n
        private Integer collectMillis;
    }

    @PostMapping("/thread")
    @Operation(summary = "执行 thread 命令(HTTP)")
    public ReturnResult<Map<String, Object>> thread(@RequestBody ThreadRequest req) {
        String cmd;
        String id = StringUtils.trimToEmpty(req.getThreadId());
        if (StringUtils.isNotBlank(id)) {
            cmd = "thread -i " + id;
        } else if (Boolean.TRUE.equals(req.getBlocking())) {
            cmd = "thread -b";
        } else {
            int n = (req.getCount() == null || req.getCount() <= 0) ? 20 : req.getCount();
            cmd = "thread -n " + n;
        }
        ExecRequest er = new ExecRequest();
        er.setNodeId(req.getNodeId());
        er.setCommand(cmd);
        er.setCollectMillis(req.getCollectMillis());
        return exec(er);
    }

    @lombok.Data
    public static class SimpleRequest {
        private String nodeId;
        private Integer collectMillis;
    }

    @PostMapping("/jvm")
    @Operation(summary = "执行 jvm 命令(HTTP)")
    public ReturnResult<Map<String, Object>> jvm(@RequestBody SimpleRequest req) {
        ExecRequest er = new ExecRequest();
        er.setNodeId(req.getNodeId());
        er.setCommand("jvm");
        er.setCollectMillis(req.getCollectMillis());
        return exec(er);
    }

    @PostMapping("/heap")
    @Operation(summary = "执行 heap 命令(HTTP)")
    public ReturnResult<Map<String, Object>> heap(@RequestBody SimpleRequest req) {
        ExecRequest er = new ExecRequest();
        er.setNodeId(req.getNodeId());
        er.setCommand("heap");
        er.setCollectMillis(req.getCollectMillis());
        return exec(er);
    }

    @lombok.Data
    public static class TraceRequest {
        private String nodeId;
        private String classPattern; // 必填
        private String methodPattern; // 可选，默认 *
        private String condition; // 可选
        private Boolean useRegex; // -E
        private Integer count; // -n
        private Integer expand; // -x
        private Integer collectMillis;
    }

    @PostMapping("/trace")
    @Operation(summary = "执行 trace 命令(HTTP)")
    public ReturnResult<Map<String, Object>> trace(@RequestBody TraceRequest req) {
        if (StringUtils.isBlank(req.getNodeId()) || StringUtils.isBlank(StringUtils.trimToEmpty(req.getClassPattern()))) {
            return ReturnResult.error("参数无效：nodeId/classPattern 不能为空");
        }
        StringBuilder sb = new StringBuilder("trace ");
        if (Boolean.TRUE.equals(req.getUseRegex())) sb.append("-E ");
        sb.append(StringUtils.trimToEmpty(req.getClassPattern())).append(' ');
        String method = StringUtils.defaultIfBlank(StringUtils.trimToEmpty(req.getMethodPattern()), "*");
        sb.append(method).append(' ');
        String cond = StringUtils.trimToEmpty(req.getCondition());
        if (StringUtils.isNotBlank(cond)) sb.append('\'').append(cond).append('\'').append(' ');
        if (req.getCount() != null && req.getCount() > 0) sb.append("-n ").append(req.getCount()).append(' ');
        if (req.getExpand() != null && req.getExpand() > 0) sb.append("-x ").append(req.getExpand()).append(' ');
        ExecRequest er = new ExecRequest();
        er.setNodeId(req.getNodeId());
        er.setCommand(sb.toString().trim());
        er.setCollectMillis(req.getCollectMillis());
        return exec(er);
    }
}

