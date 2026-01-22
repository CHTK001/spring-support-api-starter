package com.chua.starter.monitor.arthas;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorArthasSession;
import com.chua.starter.monitor.entity.MonitorArthasTunnelConfig;
import com.chua.starter.monitor.mapper.MonitorArthasTunnelConfigMapper;
import com.chua.starter.monitor.pojo.node.OnlineNodeInfo;
import com.chua.starter.monitor.pojo.arthas.*;
import com.chua.starter.monitor.service.arthas.MonitorArthasSessionService;
import com.chua.starter.monitor.service.node.NodeManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
@Lazy
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
                                .uri(URI.create(StringUtils.defaultIfBlank(monitorArthasTunnelConfig.getArthasTunnelConfigHttp(), "http://127.0.0.1:8563/api")))
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

            String httpUrl = StringUtils.defaultIfBlank(monitorArthasTunnelConfig.getArthasTunnelConfigHttp(), "http://127.0.0.1:8563/api");
            List<MonitorArthasSession> runningSessionsByNodeId = sessionService.getRunningSessionsByNodeId(req.getNodeId());
            if(runningSessionsByNodeId.isEmpty()) {
                return ReturnResult.success();
            }
            MonitorArthasSession monitorArthasSession = runningSessionsByNodeId.getFirst();
            try (HttpClient client = HttpClient.newHttpClient()) {
                // 调用 Arthas HTTP API 的异步执行接口
                String requestBody = String.format("""
                        {
                            "action":"async_exec",
                            "command":"%s",
                            "requestId": "%s",
                            "sessionId": "%s",
                            "consumerId": "%s",
                        }""".formatted(
                                req.getCommand(),
                                UUID.randomUUID().toString(),
                                monitorArthasSession.getSessionId(),
                                monitorArthasSession.getConsumerId()
                ));
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
                response.setConsumerId((String) monitorArthasSession.getConsumerId());
                response.setJobId((Integer) ((Map)responseData.get("body")).get("jobId"));

                return ReturnResult.success(response);
            }
        } catch (Exception e) {
            log.error("异步执行 Arthas 命令失败", e);
            return ReturnResult.error("异步执行失败: " + e.getMessage());
        }
    }

    // ========== 语义化HTTP接口：thread/jvm/heap/trace ==========

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
     * 创建 Arthas 会话
     */
    @PostMapping("/init-session")
    @Operation(summary = "创建 Arthas 会话", description = "创建新的Arthas会话，返回sessionId和consumerId")
    public ReturnResult<AsyncExecResponse> initSession(@RequestBody AsyncExecRequest req) {
        try {
            if (req == null || StringUtils.isBlank(req.getNodeId())) {
                return ReturnResult.error("参数无效：nodeId 不能为空");
            }

            // 获取节点信息和HTTP地址
            String httpUrl = getNodeHttpUrl(req.getNodeId());
            if (httpUrl == null) {
                return ReturnResult.error("无法获取节点的Arthas HTTP地址");
            }

            try (HttpClient client = HttpClient.newHttpClient()) {
                // 1. 创建会话
                String initSessionBody = "{\"action\":\"init_session\"}";
                HttpResponse<byte[]> initResponse = client.send(HttpRequest.newBuilder()
                                .timeout(Duration.ofSeconds(10))
                                .headers("Content-Type", "application/json")
                                .uri(URI.create(httpUrl))
                                .POST(HttpRequest.BodyPublishers.ofString(initSessionBody))
                                .build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );

                if(initResponse.statusCode() != 200) {
                    throw new RuntimeException("创建 Arthas 会话失败: " + initResponse.statusCode());
                }

                String initBodyStr = new String(initResponse.body());
                Map<String, Object> initResponseData = Json.getJsonObject(initBodyStr);

                // 解析init_session返回的所有数据
                String sessionId = (String) initResponseData.get("sessionId");
                String consumerId = (String) initResponseData.get("consumerId");
                String state = (String) initResponseData.get("state");

                if (StringUtils.isBlank(sessionId) || StringUtils.isBlank(consumerId)) {
                    throw new RuntimeException("创建会话失败，未获取到sessionId或consumerId");
                }

                if (!"SUCCEEDED".equals(state)) {
                    throw new RuntimeException("创建会话失败，状态: " + state);
                }

                // 2. 如果有命令，则异步执行命令
                Integer jobId = null;
                String jobStatus = null;
                String commandState = null;
                if (StringUtils.isNotBlank(req.getCommand())) {
                    Map<String, Object> execResult = executeAsyncCommandWithDetails(client, httpUrl, sessionId, req.getCommand());
                    jobId = (Integer) execResult.get("jobId");
                    jobStatus = (String) execResult.get("jobStatus");
                    commandState = (String) execResult.get("state");
                }

                // 3. 保存完整的session数据到数据库
                String commandType = sessionService.extractCommandType(req.getCommand());
                MonitorArthasSession session = sessionService.createSession(
                    sessionId,
                    consumerId,
                    jobId,
                    req.getNodeId(),
                    httpUrl,
                    StringUtils.defaultIfBlank(req.getCommand(), ""),
                    commandType
                );

                // 4. 更新session的备注信息，记录完整的初始化数据
                StringBuilder remark = new StringBuilder();
                remark.append("init_session响应: state=").append(state);
                if (jobId != null) {
                    remark.append(", jobId=").append(jobId);
                }
                if (StringUtils.isNotBlank(jobStatus)) {
                    remark.append(", jobStatus=").append(jobStatus);
                }
                if (StringUtils.isNotBlank(commandState)) {
                    remark.append(", commandState=").append(commandState);
                }

                // 更新备注信息
                session.setRemark(remark.toString());
                sessionService.updateSession(session);

                AsyncExecResponse response = new AsyncExecResponse();
                response.setSessionId(sessionId);
                response.setConsumerId(consumerId);
                response.setJobId(jobId);

                log.info("创建Arthas会话成功: sessionId={}, consumerId={}, nodeId={}, command={}, state={}, jobId={}",
                    sessionId, consumerId, req.getNodeId(), req.getCommand(), state, jobId);
                return ReturnResult.success(response);

            }
        } catch (Exception e) {
            log.error("创建 Arthas 会话失败", e);
            return ReturnResult.error("创建会话失败: " + e.getMessage());
        }
    }

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
                    // HTTP请求失败，可能是会话失效，尝试重新创建会话
                    log.warn("拉取结果失败，可能会话失效，尝试重新创建: sessionId={}, statusCode={}",
                        req.getSessionId(), httpResponse.statusCode());

                    // 标记原session为已中断
                    sessionService.interruptSession(req.getSessionId());

                    // 重新创建会话
                    AsyncExecResponse newSession = recreateSession(session.getNodeId(), session.getCommand());

                    // 返回新的session信息，让前端使用新的sessionId和consumerId
                    return ReturnResult.success(Map.of(
                            "sessionId", newSession.getSessionId(),
                            "consumerId", newSession.getConsumerId(),
                            "jobId", newSession.getJobId(),
                            "recreated", true,
                            "message", "原会话失效，已重新创建会话"
                    ));
                }

                String bodyStr = new String(httpResponse.body());
                Map<String, Object> responseData = Json.getJsonObject(bodyStr);

                // 检查响应状态
                String state = (String) responseData.get("state");
                String sessionIdResp = (String) responseData.get("sessionId");
                String consumerIdResp = (String) responseData.get("consumerId");

                if ("FAILED".equals(state) || "REFUSED".equals(state)) {
                    // 会话状态异常，可能需要重新创建
                    log.warn("会话状态异常，尝试重新创建: sessionId={}, state={}", req.getSessionId(), state);

                    // 更新原session的备注信息
                    session.setRemark("pull_results失败: state=" + state + ", 已重新创建会话");
                    sessionService.updateSession(session);

                    // 标记原session为已中断
                    sessionService.interruptSession(req.getSessionId());

                    // 重新创建会话
                    AsyncExecResponse newSession = recreateSession(session.getNodeId(), session.getCommand());

                    return ReturnResult.success(Map.of(
                            "sessionId", newSession.getSessionId(),
                            "consumerId", newSession.getConsumerId(),
                            "jobId", newSession.getJobId(),
                            "recreated", true,
                            "message", "会话状态异常，已重新创建会话"
                    ));
                }

                // 检查任务是否已完成，如果完成则标记session为已完成
                Object body = responseData.get("body");
                String jobStatus = null;
                if (body instanceof Map) {
                    Map<String, Object> bodyMap = (Map<String, Object>) body;
                    jobStatus = (String) bodyMap.get("jobStatus");
                    if ("TERMINATED".equals(jobStatus)) {
                        // 任务已完成，标记session为已完成
                        sessionService.completeSession(req.getSessionId());
                        log.info("Arthas任务已完成，标记session为已完成: sessionId={}", req.getSessionId());

                        // 更新session的备注信息
                        session.setRemark("任务已完成: jobStatus=" + jobStatus + ", state=" + state);
                        sessionService.updateSession(session);
                    } else {
                        // 更新session的最新状态信息
                        StringBuilder remarkBuilder = new StringBuilder();
                        remarkBuilder.append("最新状态: state=").append(state);
                        if (StringUtils.isNotBlank(jobStatus)) {
                            remarkBuilder.append(", jobStatus=").append(jobStatus);
                        }
                        if (StringUtils.isNotBlank(sessionIdResp)) {
                            remarkBuilder.append(", sessionId=").append(sessionIdResp);
                        }
                        session.setRemark(remarkBuilder.toString());
                        sessionService.updateSession(session);
                    }
                }

                return ReturnResult.success(Map.of(
                        "sessionId", req.getSessionId(),
                        "consumerId", req.getConsumerId(),
                        "body", responseData,
                        "recreated", false
                ));
            }
        } catch (Exception e) {
            log.error("拉取 Arthas 命令结果失败: sessionId={}", req.getSessionId(), e);

            // 如果是网络异常或其他异常，也尝试重新创建会话
            try {
                MonitorArthasSession session = sessionService.getSessionBySessionId(req.getSessionId());
                if (session != null) {
                    // 标记原session为已中断
                    sessionService.interruptSession(req.getSessionId());

                    // 重新创建会话
                    AsyncExecResponse newSession = recreateSession(session.getNodeId(), session.getCommand());

                    return ReturnResult.success(Map.of(
                            "sessionId", newSession.getSessionId(),
                            "consumerId", newSession.getConsumerId(),
                            "jobId", newSession.getJobId(),
                            "recreated", true,
                            "message", "网络异常，已重新创建会话"
                    ));
                }
            } catch (Exception recreateEx) {
                log.error("重新创建会话也失败", recreateEx);
            }

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
            if (req == null || StringUtils.isBlank(req.getSessionId())) {
                return ReturnResult.error("参数无效：sessionId 不能为空");
            }

            // 从数据库获取session信息
            MonitorArthasSession session = sessionService.getSessionBySessionId(req.getSessionId());
            if (session == null) {
                // session不存在，直接返回成功（可能已经被清理了）
                log.warn("要中断的session不存在: sessionId={}", req.getSessionId());
                return ReturnResult.success(Map.of(
                        "sessionId", req.getSessionId(),
                        "message", "Session不存在，可能已被清理",
                        "interrupted", true
                ));
            }

            try (HttpClient client = HttpClient.newHttpClient()) {
                // 调用 Arthas HTTP API 的中断任务接口
                String requestBody = String.format("{\"action\":\"interrupt_job\",\"sessionId\":\"%s\"}",
                    req.getSessionId());

                HttpResponse<byte[]> httpResponse = client.send(HttpRequest.newBuilder()
                                .timeout(Duration.ofSeconds(5))
                                .headers("Content-Type", "application/json")
                                .uri(URI.create(session.getHttpUrl()))
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                .build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );

                String bodyStr = new String(httpResponse.body());
                Map<String, Object> responseData = Json.getJsonObject(bodyStr);

                // 无论HTTP请求是否成功，都标记session为已中断
                sessionService.interruptSession(req.getSessionId());
                log.info("Arthas任务已中断，标记session为已中断: sessionId={}", req.getSessionId());

                if (httpResponse.statusCode() != 200) {
                    log.warn("中断命令HTTP请求失败，但已标记session为中断: sessionId={}, statusCode={}",
                        req.getSessionId(), httpResponse.statusCode());
                }

                return ReturnResult.success(Map.of(
                        "sessionId", req.getSessionId(),
                        "jobId", session.getJobId(),
                        "result", responseData,
                        "interrupted", true
                ));
            }
        } catch (Exception e) {
            log.error("中断 Arthas 命令失败: sessionId={}", req.getSessionId(), e);

            // 即使中断失败，也标记session为已中断
            try {
                sessionService.interruptSession(req.getSessionId());
                log.info("中断命令异常，但已标记session为已中断: sessionId={}", req.getSessionId());
            } catch (Exception markEx) {
                log.error("标记session为中断状态失败", markEx);
            }

            return ReturnResult.success(Map.of(
                    "sessionId", req.getSessionId(),
                    "message", "中断请求已处理，session已标记为中断状态",
                    "interrupted", true
            ));
        }
    }

    /**
     * 关闭 Arthas 会话
     */
    @PostMapping("/close-session")
    @Operation(summary = "关闭 Arthas 会话", description = "关闭指定的Arthas会话")
    public ReturnResult<Map<String, Object>> closeSession(@RequestBody Map<String, String> req) {
        try {
            String sessionId = req.get("sessionId");
            if (StringUtils.isBlank(sessionId)) {
                return ReturnResult.error("参数无效：sessionId 不能为空");
            }

            // 从数据库获取session信息
            MonitorArthasSession session = sessionService.getSessionBySessionId(sessionId);
            if (session == null) {
                log.warn("要关闭的session不存在: sessionId={}", sessionId);
                return ReturnResult.success(Map.of(
                        "sessionId", sessionId,
                        "message", "Session不存在，可能已被清理",
                        "closed", true
                ));
            }

            try (HttpClient client = HttpClient.newHttpClient()) {
                // 调用 Arthas HTTP API 的关闭会话接口
                String requestBody = String.format("{\"action\":\"close_session\",\"sessionId\":\"%s\"}", sessionId);

                HttpResponse<byte[]> httpResponse = client.send(HttpRequest.newBuilder()
                                .timeout(Duration.ofSeconds(5))
                                .headers("Content-Type", "application/json")
                                .uri(URI.create(session.getHttpUrl()))
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                .build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );

                String bodyStr = new String(httpResponse.body());
                Map<String, Object> responseData = Json.getJsonObject(bodyStr);

                // 无论HTTP请求是否成功，都标记session为已完成
                sessionService.completeSession(sessionId);
                log.info("Arthas会话已关闭，标记session为已完成: sessionId={}", sessionId);

                if (httpResponse.statusCode() != 200) {
                    log.warn("关闭会话HTTP请求失败，但已标记session为完成: sessionId={}, statusCode={}",
                        sessionId, httpResponse.statusCode());
                }

                return ReturnResult.success(Map.of(
                        "sessionId", sessionId,
                        "result", responseData,
                        "closed", true
                ));
            }
        } catch (Exception e) {
            log.error("关闭 Arthas 会话失败: sessionId={}", req.get("sessionId"), e);

            // 即使关闭失败，也标记session为已完成
            try {
                String sessionId = req.get("sessionId");
                if (StringUtils.isNotBlank(sessionId)) {
                    sessionService.completeSession(sessionId);
                    log.info("关闭会话异常，但已标记session为已完成: sessionId={}", sessionId);
                }
            } catch (Exception markEx) {
                log.error("标记session为完成状态失败", markEx);
            }

            return ReturnResult.success(Map.of(
                    "sessionId", req.get("sessionId"),
                    "message", "关闭请求已处理，session已标记为完成状态",
                    "closed", true
            ));
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

    /**
     * 获取或创建节点的可用会话
     */
    @PostMapping("/get-or-create-session")
    @Operation(summary = "获取或创建节点的可用会话", description = "先检查是否有可用会话，没有则创建新会话")
    public ReturnResult<AsyncExecResponse> getOrCreateSession(@RequestBody AsyncExecRequest req) {
        try {
            if (req == null || StringUtils.isBlank(req.getNodeId())) {
                return ReturnResult.error("参数无效：nodeId 不能为空");
            }

            // 1. 先查找节点是否有可用的运行中会话
            List<MonitorArthasSession> runningSessions = sessionService.getRunningSessionsByNodeId(req.getNodeId());

            // 过滤出可用的会话（最近创建的，且未过期的）
            MonitorArthasSession availableSession = null;
            for (MonitorArthasSession session : runningSessions) {
                // 检查会话是否未过期且状态正常
                if (session.getExpireTime().isAfter(LocalDateTime.now()) &&
                    MonitorArthasSession.STATUS_RUNNING.equals(session.getStatus())) {

                    // 验证会话是否仍然有效
                    if (validateSession(session)) {
                        availableSession = session;
                        break;
                    } else {
                        // 会话无效，标记为中断
                        sessionService.interruptSession(session.getSessionId());
                        log.warn("发现无效会话，已标记为中断: sessionId={}", session.getSessionId());
                    }
                }
            }

            if (availableSession != null) {
                // 2. 找到可用会话，如果需要执行新命令，则执行
                Integer jobId = availableSession.getJobId();
                if (StringUtils.isNotBlank(req.getCommand()) &&
                    !req.getCommand().equals(availableSession.getCommand())) {

                    // 执行新命令
                    try (HttpClient client = HttpClient.newHttpClient()) {
                        Map<String, Object> execResult = executeAsyncCommandWithDetails(
                            client, availableSession.getHttpUrl(),
                            availableSession.getSessionId(), req.getCommand());
                        jobId = (Integer) execResult.get("jobId");

                        // 更新会话信息
                        availableSession.setCommand(req.getCommand());
                        availableSession.setJobId(jobId);
                        availableSession.setCommandType(sessionService.extractCommandType(req.getCommand()));
                        availableSession.setRemark("复用会话执行新命令: " + req.getCommand());
                        sessionService.updateSession(availableSession);
                    }
                }

                AsyncExecResponse response = new AsyncExecResponse();
                response.setSessionId(availableSession.getSessionId());
                response.setConsumerId(availableSession.getConsumerId());
                response.setJobId(jobId);

                log.info("复用现有会话: sessionId={}, nodeId={}, command={}",
                    availableSession.getSessionId(), req.getNodeId(), req.getCommand());
                return ReturnResult.success(response);
            } else {
                // 3. 没有可用会话，创建新会话
                log.info("未找到可用会话，创建新会话: nodeId={}", req.getNodeId());
                return initSession(req);
            }

        } catch (Exception e) {
            log.error("获取或创建会话失败: nodeId={}", req.getNodeId(), e);
            return ReturnResult.error("获取或创建会话失败: " + e.getMessage());
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 获取节点的Arthas HTTP地址
     */
    private String getNodeHttpUrl(String nodeId) {
        try {
            ReturnResult<OnlineNodeInfo> nodeResult = nodeManagementService.getNodeDetails(nodeId);
            if (!nodeResult.isSuccess() || nodeResult.getData() == null) {
                return null;
            }
            OnlineNodeInfo node = nodeResult.getData();
            Object tunnel = node.getMetadata() == null ? null : node.getMetadata().get(ARTHAS_META_KEY);
            if (tunnel == null) {
                return null;
            }

            MonitorArthasTunnelConfig monitorArthasTunnelConfig = monitorArthasTunnelConfigMapper.selectOne(
                    Wrappers.<MonitorArthasTunnelConfig>lambdaQuery()
                            .eq(MonitorArthasTunnelConfig::getMonitorArthasTunnelConfigServerId, nodeId)
                    , false);
            if (null == monitorArthasTunnelConfig) {
                monitorArthasTunnelConfig = MonitorArthasTunnelConfig.EMPTY;
            }

            return StringUtils.defaultIfBlank(monitorArthasTunnelConfig.getArthasTunnelConfigHttp(), "http://127.0.0.1:8563/api");
        } catch (Exception e) {
            log.error("获取节点HTTP地址失败: nodeId={}", nodeId, e);
            return null;
        }
    }

    /**
     * 执行异步命令
     */
    private Integer executeAsyncCommand(HttpClient client, String httpUrl, String sessionId, String command) throws Exception {
        Map<String, Object> result = executeAsyncCommandWithDetails(client, httpUrl, sessionId, command);
        return (Integer) result.get("jobId");
    }

    /**
     * 执行异步命令并返回详细信息
     */
    private Map<String, Object> executeAsyncCommandWithDetails(HttpClient client, String httpUrl, String sessionId, String command) throws Exception {
        String asyncExecBody = String.format("{\"action\":\"async_exec\",\"sessionId\":\"%s\",\"command\":\"%s\"}",
            sessionId, command);
        HttpResponse<byte[]> execResponse = client.send(HttpRequest.newBuilder()
                        .timeout(Duration.ofSeconds(10))
                        .headers("Content-Type", "application/json")
                        .uri(URI.create(httpUrl))
                        .POST(HttpRequest.BodyPublishers.ofString(asyncExecBody))
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );

        if(execResponse.statusCode() != 200) {
            throw new RuntimeException("异步执行 Arthas 命令失败: " + execResponse.statusCode());
        }

        String execBodyStr = new String(execResponse.body());
        Map<String, Object> execResponseData = Json.getJsonObject(execBodyStr);

        // 解析async_exec返回的完整数据
        String state = (String) execResponseData.get("state");
        String sessionIdResp = (String) execResponseData.get("sessionId");
        Map<String, Object> body = (Map<String, Object>) execResponseData.get("body");

        Map<String, Object> result = new HashMap<>();
        result.put("state", state);
        result.put("sessionId", sessionIdResp);

        if (body != null) {
            Integer jobId = (Integer) body.get("jobId");
            String jobStatus = (String) body.get("jobStatus");
            String command_resp = (String) body.get("command");

            result.put("jobId", jobId);
            result.put("jobStatus", jobStatus);
            result.put("command", command_resp);
        }

        return result;
    }

    /**
     * 重新创建会话（当原会话失效时）
     */
    private AsyncExecResponse recreateSession(String nodeId, String command) throws Exception {
        log.warn("原会话失效，重新创建会话: nodeId={}", nodeId);

        // 获取HTTP地址
        String httpUrl = getNodeHttpUrl(nodeId);
        if (httpUrl == null) {
            throw new RuntimeException("无法获取节点的Arthas HTTP地址");
        }

        try (HttpClient client = HttpClient.newHttpClient()) {
            // 1. 创建新会话
            String initSessionBody = "{\"action\":\"init_session\"}";
            HttpResponse<byte[]> initResponse = client.send(HttpRequest.newBuilder()
                            .timeout(Duration.ofSeconds(10))
                            .headers("Content-Type", "application/json")
                            .uri(URI.create(httpUrl))
                            .POST(HttpRequest.BodyPublishers.ofString(initSessionBody))
                            .build(),
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            if(initResponse.statusCode() != 200) {
                throw new RuntimeException("重新创建 Arthas 会话失败: " + initResponse.statusCode());
            }

            String initBodyStr = new String(initResponse.body());
            Map<String, Object> initResponseData = Json.getJsonObject(initBodyStr);

            // 解析init_session返回的所有数据
            String sessionId = (String) initResponseData.get("sessionId");
            String consumerId = (String) initResponseData.get("consumerId");
            String state = (String) initResponseData.get("state");

            if (StringUtils.isBlank(sessionId) || StringUtils.isBlank(consumerId)) {
                throw new RuntimeException("重新创建会话失败，未获取到sessionId或consumerId");
            }

            if (!"SUCCEEDED".equals(state)) {
                throw new RuntimeException("重新创建会话失败，状态: " + state);
            }

            // 2. 如果有命令，则异步执行命令
            Integer jobId = null;
            String jobStatus = null;
            String commandState = null;
            if (StringUtils.isNotBlank(command)) {
                Map<String, Object> execResult = executeAsyncCommandWithDetails(client, httpUrl, sessionId, command);
                jobId = (Integer) execResult.get("jobId");
                jobStatus = (String) execResult.get("jobStatus");
                commandState = (String) execResult.get("state");
            }

            // 3. 保存新session到数据库
            String commandType = sessionService.extractCommandType(command);
            MonitorArthasSession session = sessionService.createSession(
                sessionId,
                consumerId,
                jobId,
                nodeId,
                httpUrl,
                StringUtils.defaultIfBlank(command, ""),
                commandType
            );

            // 4. 更新session的备注信息，记录重建原因和完整数据
            StringBuilder remark = new StringBuilder();
            remark.append("重建会话: state=").append(state);
            if (jobId != null) {
                remark.append(", jobId=").append(jobId);
            }
            if (StringUtils.isNotBlank(jobStatus)) {
                remark.append(", jobStatus=").append(jobStatus);
            }
            if (StringUtils.isNotBlank(commandState)) {
                remark.append(", commandState=").append(commandState);
            }

            // 更新备注信息
            session.setRemark(remark.toString());
            sessionService.updateSession(session);

            AsyncExecResponse response = new AsyncExecResponse();
            response.setSessionId(sessionId);
            response.setConsumerId(consumerId);
            response.setJobId(jobId);

            log.info("重新创建Arthas会话成功: sessionId={}, nodeId={}", sessionId, nodeId);
            return response;
        }
    }

    /**
     * 验证会话是否仍然有效
     */
    private boolean validateSession(MonitorArthasSession session) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            // 尝试拉取一次结果来验证会话是否有效
            String requestBody = String.format("{\"action\":\"pull_results\",\"sessionId\":\"%s\",\"consumerId\":\"%s\"}",
                session.getSessionId(), session.getConsumerId());

            HttpResponse<byte[]> httpResponse = client.send(HttpRequest.newBuilder()
                            .timeout(Duration.ofSeconds(3))  // 短超时，快速验证
                            .headers("Content-Type", "application/json")
                            .uri(URI.create(session.getHttpUrl()))
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build(),
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            if (httpResponse.statusCode() != 200) {
                return false;
            }

            String bodyStr = new String(httpResponse.body());
            Map<String, Object> responseData = Json.getJsonObject(bodyStr);
            String state = (String) responseData.get("state");

            // 如果状态是SUCCEEDED或者其他正常状态，认为会话有效
            return !"FAILED".equals(state) && !"REFUSED".equals(state);

        } catch (Exception e) {
            log.warn("验证会话失败: sessionId={}, error={}", session.getSessionId(), e.getMessage());
            return false;
        }
    }
}
