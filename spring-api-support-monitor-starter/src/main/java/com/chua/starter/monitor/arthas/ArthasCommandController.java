package com.chua.starter.monitor.arthas;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorArthasSession;
import com.chua.starter.monitor.entity.MonitorArthasTunnelConfig;
import com.chua.starter.monitor.mapper.MonitorArthasTunnelConfigMapper;
import com.chua.starter.monitor.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.service.MonitorArthasSessionService;
import com.chua.starter.monitor.service.NodeManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Arthas 命令执行（HTTP接口）
 * <p>
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
    private final MonitorArthasTunnelConfigMapper monitorArthasTunnelConfigMapper;
    private final MonitorArthasSessionService sessionService;

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

            MonitorArthasTunnelConfig monitorArthasTunnelConfig = monitorArthasTunnelConfigMapper.selectOne(
                    Wrappers.<MonitorArthasTunnelConfig>lambdaQuery()
                            .eq(MonitorArthasTunnelConfig::getMonitorArthasTunnelConfigServerId, req.getNodeId())
                    , false);
            if (null == monitorArthasTunnelConfig) {
                monitorArthasTunnelConfig = MonitorArthasTunnelConfig.EMPTY;
            }

            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpResponse<byte[]> httpResponse = client.send(HttpRequest.newBuilder()
                                .timeout(Duration.ofSeconds(10))
                                .headers("Content-Type", "application/json")
                                .uri(URI.create(StringUtils.defaultString(monitorArthasTunnelConfig.getArthasTunnelConfigHttp(), "http://127.0.0.1:8563/api")))
                                .POST(HttpRequest.BodyPublishers.ofString("{\"action\":\"exec\",\"command\":\"%s\"}".formatted(
                                        req.getCommand()
                                )))
                                .build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );
                if (httpResponse.statusCode() != 200) {
                    throw new RuntimeException("执行 Arthas 命令失败: " + httpResponse.statusCode());
                }
                String bodyStr = new String(httpResponse.body());
                //System.out.println();
                return ReturnResult.success(Map.of(
                        "nodeId", req.getNodeId(),
                        "command", req.getCommand(),
                        "collectMillis", collectMs,
                        "output", Json.getJsonObject(bodyStr)));
            }
        } catch (Exception e) {
            return ReturnResult.error("执行失败: " + e.getMessage());
        }
    }

    /**
     * 异步执行 arthas 命令
     */
    @PostMapping("/async-exec")
    @Operation(summary = "异步执行 Arthas 命令", description = "异步启动命令执行，返回sessionId和consumerId用于后续拉取结果")
    public ReturnResult<AsyncExecResponse> asyncExec(@RequestBody AsyncExecRequest req) {
        try {
            if (req == null || StringUtils.isBlank(req.getNodeId()) || StringUtils.isBlank(req.getCommand())) {
                return ReturnResult.error("参数无效：nodeId/command 不能为空");
            }

            ReturnResult<OnlineNodeInfo> nodeResult = nodeManagementService.getNodeDetails(req.getNodeId());
            if (!nodeResult.isSuccess() || nodeResult.getData() == null) {
                return ReturnResult.error("节点不可用");
            }
            OnlineNodeInfo node = nodeResult.getData();
            Object tunnel = node.getMetadata() == null ? null : node.getMetadata().get(ARTHAS_META_KEY);
            if (tunnel == null) {
                return ReturnResult.error("节点未配置 Arthas 元数据");
            }

            MonitorArthasTunnelConfig monitorArthasTunnelConfig = monitorArthasTunnelConfigMapper.selectOne(
                    Wrappers.<MonitorArthasTunnelConfig>lambdaQuery()
                            .eq(MonitorArthasTunnelConfig::getMonitorArthasTunnelConfigServerId, req.getNodeId())
                    , false);
            if (null == monitorArthasTunnelConfig) {
                monitorArthasTunnelConfig = MonitorArthasTunnelConfig.EMPTY;
            }

            String httpUrl = StringUtils.defaultString(monitorArthasTunnelConfig.getArthasTunnelConfigHttp(), "http://127.0.0.1:8563/api");

            try (HttpClient client = HttpClient.newHttpClient()) {
                // 调用 Arthas HTTP API 的异步执行接口
                String requestBody = String.format("{\"action\":\"async_exec\",\"command\":\"%s\"}", req.getCommand());
                HttpResponse<byte[]> httpResponse = client.send(HttpRequest.newBuilder()
                                .timeout(Duration.ofSeconds(10))
                                .headers("Content-Type", "application/json")
                                .uri(URI.create(httpUrl))
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                .build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );

                if (httpResponse.statusCode() != 200) {
                    throw new RuntimeException("异步执行 Arthas 命令失败: " + httpResponse.statusCode());
                }

                String bodyStr = new String(httpResponse.body());
                Map<String, Object> responseData = Json.getJsonObject(bodyStr);

                // 解析响应，提取sessionId、consumerId和jobId
                AsyncExecResponse response = new AsyncExecResponse();
                response.setSessionId((String) responseData.get("sessionId"));
                response.setConsumerId((String) responseData.get("consumerId"));
                response.setJobId((Integer) responseData.get("jobId"));

                // 创建session记录
                if (response.getSessionId() != null) {
                    String commandType = sessionService.extractCommandType(req.getCommand());
                    sessionService.createSession(
                            response.getSessionId(),
                            response.getConsumerId(),
                            response.getJobId(),
                            req.getNodeId(),
                            httpUrl,
                            req.getCommand(),
                            commandType
                    );
                }

                return ReturnResult.success(response);
            }
        } catch (Exception e) {
            log.error("异步执行 Arthas 命令失败", e);
            return ReturnResult.error("异步执行失败: " + e.getMessage());
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

    // ========== 异步执行相关接口 ==========

    /**
     * 拉取 arthas 命令执行结果
     */
    @PostMapping("/pull-results")
    @Operation(summary = "拉取 Arthas 命令执行结果", description = "根据sessionId和consumerId拉取命令执行结果")
    public ReturnResult<Map<String, Object>> pullResults(@RequestBody PullResultsRequest req) {
        try {
            if (req == null || StringUtils.isBlank(req.getSessionId()) || StringUtils.isBlank(req.getConsumerId())) {
                return ReturnResult.error("参数无效：sessionId/consumerId 不能为空");
            }

            // 从数据库获取session信息
            MonitorArthasSession session = sessionService.getSessionBySessionId(req.getSessionId());
            if (session == null) {
                return ReturnResult.error("Session不存在或已过期: " + req.getSessionId());
            }

            try (HttpClient client = HttpClient.newHttpClient()) {
                // 调用 Arthas HTTP API 的拉取结果接口
                String requestBody = String.format("{\"action\":\"pull_results\",\"sessionId\":\"%s\",\"consumerId\":\"%s\"}",
                        req.getSessionId(), req.getConsumerId());

                HttpResponse<byte[]> httpResponse = client.send(HttpRequest.newBuilder()
                                .timeout(Duration.ofSeconds(5))
                                .headers("Content-Type", "application/json")
                                .uri(URI.create(session.getHttpUrl()))
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                .build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );

                if (httpResponse.statusCode() != 200) {
                    throw new RuntimeException("拉取 Arthas 命令结果失败: " + httpResponse.statusCode());
                }

                String bodyStr = new String(httpResponse.body());
                Map<String, Object> responseData = Json.getJsonObject(bodyStr);

                // 检查任务是否已完成，如果完成则标记session为已完成
                Object body = responseData.get("body");
                if (body instanceof Map) {
                    Map<String, Object> bodyMap = (Map<String, Object>) body;
                    String jobStatus = (String) bodyMap.get("jobStatus");
                    if ("TERMINATED".equals(jobStatus)) {
                        // 任务已完成，标记session为已完成
                        sessionService.completeSession(req.getSessionId());
                        log.info("Arthas任务已完成，标记session为已完成: sessionId={}", req.getSessionId());
                    }
                }

                return ReturnResult.success(Map.of(
                        "sessionId", req.getSessionId(),
                        "consumerId", req.getConsumerId(),
                        "body", responseData));
            }
        } catch (Exception e) {
            log.error("拉取 Arthas 命令结果失败", e);
            return ReturnResult.error("拉取结果失败: " + e.getMessage());
        }
    }

    /**
     * 中断 arthas 命令执行
     */
    @PostMapping("/interrupt-job")
    @Operation(summary = "中断 Arthas 命令执行", description = "根据sessionId和jobId中断正在执行的命令")
    public ReturnResult<Map<String, Object>> interruptJob(@RequestBody InterruptJobRequest req) {
        try {
            if (req == null || StringUtils.isBlank(req.getSessionId()) || req.getJobId() == null) {
                return ReturnResult.error("参数无效：sessionId/jobId 不能为空");
            }

            // 从数据库获取session信息
            MonitorArthasSession session = sessionService.getSessionBySessionId(req.getSessionId());
            if (session == null) {
                return ReturnResult.error("Session不存在或已过期: " + req.getSessionId());
            }

            try (HttpClient client = HttpClient.newHttpClient()) {
                // 调用 Arthas HTTP API 的中断任务接口
                String requestBody = String.format("{\"action\":\"interrupt_job\",\"sessionId\":\"%s\",\"jobId\":%d}",
                        req.getSessionId(), req.getJobId());

                HttpResponse<byte[]> httpResponse = client.send(HttpRequest.newBuilder()
                                .timeout(Duration.ofSeconds(5))
                                .headers("Content-Type", "application/json")
                                .uri(URI.create(session.getHttpUrl()))
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                .build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );

                if (httpResponse.statusCode() != 200) {
                    throw new RuntimeException("中断 Arthas 命令失败: " + httpResponse.statusCode());
                }

                String bodyStr = new String(httpResponse.body());
                Map<String, Object> responseData = Json.getJsonObject(bodyStr);

                // 标记session为已中断
                sessionService.interruptSession(req.getSessionId());
                log.info("Arthas任务已中断，标记session为已中断: sessionId={}", req.getSessionId());

                return ReturnResult.success(Map.of(
                        "sessionId", req.getSessionId(),
                        "jobId", req.getJobId(),
                        "result", responseData));
            }
        } catch (Exception e) {
            log.error("中断 Arthas 命令失败", e);
            return ReturnResult.error("中断任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取节点的运行中session列表
     */
    @GetMapping("/sessions/{nodeId}")
    @Operation(summary = "获取节点的运行中session列表")
    public ReturnResult<List<MonitorArthasSession>> getNodeSessions(@PathVariable String nodeId) {
        try {
            List<MonitorArthasSession> sessions = sessionService.getRunningSessionsByNodeId(nodeId);
            return ReturnResult.success(sessions);
        } catch (Exception e) {
            log.error("获取节点session列表失败", e);
            return ReturnResult.error("获取session列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取session状态统计
     */
    @GetMapping("/sessions/stats")
    @Operation(summary = "获取session状态统计")
    public ReturnResult<List<Map<String, Object>>> getSessionStats() {
        try {
            List<Map<String, Object>> stats = sessionService.getSessionStatusStats();
            return ReturnResult.success(stats);
        } catch (Exception e) {
            log.error("获取session统计失败", e);
            return ReturnResult.error("获取session统计失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期的session
     */
    @PostMapping("/sessions/clean-expired")
    @Operation(summary = "清理过期的session")
    public ReturnResult<Map<String, Object>> cleanExpiredSessions() {
        try {
            int count = sessionService.cleanExpiredSessions();
            return ReturnResult.success(Map.of("cleanedCount", count));
        } catch (Exception e) {
            log.error("清理过期session失败", e);
            return ReturnResult.error("清理过期session失败: " + e.getMessage());
        }
    }

    /**
     * 强制清理指定节点的所有运行中session
     */
    @PostMapping("/sessions/clean-node/{nodeId}")
    @Operation(summary = "强制清理指定节点的所有运行中session")
    public ReturnResult<Map<String, Object>> cleanNodeSessions(@PathVariable String nodeId) {
        try {
            int count = sessionService.forceCleanNodeSessions(nodeId);
            return ReturnResult.success(Map.of("cleanedCount", count));
        } catch (Exception e) {
            log.error("清理节点session失败", e);
            return ReturnResult.error("清理节点session失败: " + e.getMessage());
        }
    }

    /**
     * 删除历史session记录
     */
    @PostMapping("/sessions/clean-history")
    @Operation(summary = "删除历史session记录")
    public ReturnResult<Map<String, Object>> cleanHistorySessions() {
        try {
            int count = sessionService.deleteOldSessions();
            return ReturnResult.success(Map.of("deletedCount", count));
        } catch (Exception e) {
            log.error("删除历史session失败", e);
            return ReturnResult.error("删除历史session失败: " + e.getMessage());
        }
    }

    // ========== Session 管理接口 ==========

    /**
     * 请求体
     */
    @lombok.Data
    public static class ExecRequest {
        /**
         * 节点ID（必填）
         */
        private String nodeId;
        /**
         * 要执行的 arthas 命令（必填），例如：thread -n 20
         */
        private String command;
        /**
         * 收集输出的毫秒数（可选，默认3000ms）
         */
        private Integer collectMillis;
    }

    /**
     * 异步执行请求体
     */
    @lombok.Data
    public static class AsyncExecRequest {
        /**
         * 节点ID（必填）
         */
        private String nodeId;
        /**
         * 要执行的 arthas 命令（必填）
         */
        private String command;
    }

    /**
     * 异步执行响应
     */
    @lombok.Data
    public static class AsyncExecResponse {
        /**
         * 会话ID
         */
        private String sessionId;
        /**
         * 消费者ID
         */
        private String consumerId;
        /**
         * 任务ID
         */
        private Integer jobId;
    }

    /**
     * 拉取结果请求体
     */
    @lombok.Data
    public static class PullResultsRequest {
        /**
         * 会话ID
         */
        private String sessionId;
        /**
         * 消费者ID
         */
        private String consumerId;
    }

    /**
     * 中断任务请求体
     */
    @lombok.Data
    public static class InterruptJobRequest {
        /**
         * 会话ID
         */
        private String sessionId;
        /**
         * 任务ID
         */
        private Integer jobId;
    }
}
