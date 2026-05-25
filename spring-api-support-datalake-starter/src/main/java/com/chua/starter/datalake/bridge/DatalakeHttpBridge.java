package com.chua.starter.datalake.bridge;

import com.chua.common.support.core.utils.PathVariableUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.network.protocol.request.ServletRequest;
import com.chua.common.support.network.protocol.request.ServletResponse;
import com.chua.common.support.network.protocol.server.ProtocolServer;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.text.json.JsonObject;
import com.chua.datalake.support.envelope.DataEnvelope;
import com.chua.datalake.support.engine.PipelineLifecycle;
import com.chua.datalake.support.engine.ScGraph;
import com.chua.datalake.support.model.*;
import com.chua.starter.datalake.model.PipelineCreateRequest;
import com.chua.starter.datalake.model.PipelineUpdateRequest;
import com.chua.starter.datalake.service.DatalakeDataService;
import com.chua.starter.datalake.service.DatalakeMonitorService;
import com.chua.starter.datalake.service.PipelineService;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据中台 HTTP API Bridge — 基于 ProtocolServer 注册路由
 */
public class DatalakeHttpBridge {

    private final PipelineService pipelineService;
    private final DatalakeDataService dataService;
    private final DatalakeMonitorService monitorService;

    /** 订阅存储 */
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    /** 死信队列存储 */
    private final Map<Long, Map<String, Object>> dlqEntries = new ConcurrentHashMap<>();
    private final AtomicLong dlqIdSeq = new AtomicLong(1);

    public DatalakeHttpBridge(PipelineService pipelineService,
                              DatalakeDataService dataService,
                              DatalakeMonitorService monitorService) {
        this.pipelineService = pipelineService;
        this.dataService = dataService;
        this.monitorService = monitorService;
    }

    /**
     * 注册全部路由到 ProtocolServer
     */
    public void register(ProtocolServer server) {
        // Pipeline CRUD
        server.get("/pipeline/list", this::handlePipelineList);
        server.get("/pipeline/{id}", this::handlePipelineGet);
        server.post("/pipeline", this::handlePipelineCreate);
        server.put("/pipeline", this::handlePipelineUpdate);
        server.delete("/pipeline/{id}", this::handlePipelineDelete);

        // Pipeline 生命周期
        server.post("/pipeline/{id}/start", this::handlePipelineStart);
        server.post("/pipeline/{id}/stop", this::handlePipelineStop);
        server.post("/pipeline/{id}/pause", this::handlePipelinePause);
        server.post("/pipeline/{id}/resume", this::handlePipelineResume);
        server.get("/pipeline/{id}/status", this::handlePipelineStatus);

        // 数据查询
        server.post("/data/query", this::handleDataQuery);
        server.post("/data/aggregate", this::handleDataAggregate);
        server.get("/data/trace/{traceId}", this::handleDataTrace);

        // 订阅管理
        server.get("/subscription/list", this::handleSubscriptionList);
        server.post("/subscription", this::handleSubscriptionCreate);
        server.delete("/subscription/{id}", this::handleSubscriptionCancel);

        // 监控
        server.get("/monitor/stats", this::handleMonitorStats);
        server.get("/monitor/throughput", this::handleMonitorThroughput);
        server.get("/monitor/errors", this::handleMonitorErrors);

        // 死信队列
        server.get("/dlq/list", this::handleDlqList);
        server.post("/dlq/{id}/retry", this::handleDlqRetry);
        server.delete("/dlq/{id}", this::handleDlqDelete);
        server.delete("/dlq/clear", this::handleDlqClear);
    }

    // ==================== Pipeline ====================

    private void handlePipelineList(ServletRequest request, ServletResponse response) {
        writeJson(response, ReturnResult.ok(pipelineService.list()));
    }

    private void handlePipelineGet(ServletRequest request, ServletResponse response) {
        String id = extractPathVariable(request, "/pipeline/{id}", "id");
        ScGraph graph = pipelineService.getById(id);
        if (graph == null) {
            writeJson(response, ReturnResult.error("Pipeline not found: " + id));
            return;
        }
        writeJson(response, ReturnResult.ok(graph));
    }

    private void handlePipelineCreate(ServletRequest request, ServletResponse response) {
        JsonObject body = parseBody(request);
        if (body == null) {
            writeJson(response, ReturnResult.error("Invalid request body"));
            return;
        }
        PipelineCreateRequest req = new PipelineCreateRequest();
        req.setId(body.getString("id"));
        req.setName(body.getString("name"));
        req.setVersion(body.getString("version", "1.0.0"));
        req.setDescription(body.getString("description"));
        req.setNodes(toList(body.get("nodes")));
        req.setSinks(toSinksMap(body.get("sinks")));

        if (req.getId() == null || req.getId().isEmpty()) {
            writeJson(response, ReturnResult.error("id is required"));
            return;
        }
        if (pipelineService.getById(req.getId()) != null) {
            writeJson(response, ReturnResult.error("Pipeline already exists: " + req.getId()));
            return;
        }
        writeJson(response, ReturnResult.ok(pipelineService.create(req)));
    }

    private void handlePipelineUpdate(ServletRequest request, ServletResponse response) {
        JsonObject body = parseBody(request);
        if (body == null) {
            writeJson(response, ReturnResult.error("Invalid request body"));
            return;
        }
        PipelineUpdateRequest req = new PipelineUpdateRequest();
        req.setId(body.getString("id"));
        req.setName(body.getString("name"));
        req.setVersion(body.getString("version"));
        req.setDescription(body.getString("description"));
        req.setNodes(toList(body.get("nodes")));
        req.setSinks(toSinksMap(body.get("sinks")));

        ScGraph graph = pipelineService.update(req);
        if (graph == null) {
            writeJson(response, ReturnResult.error("Pipeline not found: " + req.getId()));
            return;
        }
        writeJson(response, ReturnResult.ok(graph));
    }

    private void handlePipelineDelete(ServletRequest request, ServletResponse response) {
        String id = extractPathVariable(request, "/pipeline/{id}", "id");
        boolean ok = pipelineService.delete(id);
        writeJson(response, ok ? ReturnResult.ok(true) : ReturnResult.error("Pipeline not found: " + id));
    }

    private void handlePipelineStart(ServletRequest request, ServletResponse response) {
        String id = extractPathVariable(request, "/pipeline/{id}/start", "id");
        boolean ok = pipelineService.start(id);
        writeJson(response, ok ? ReturnResult.ok(true) : ReturnResult.error("Pipeline not found: " + id));
    }

    private void handlePipelineStop(ServletRequest request, ServletResponse response) {
        String id = extractPathVariable(request, "/pipeline/{id}/stop", "id");
        boolean ok = pipelineService.stop(id);
        writeJson(response, ok ? ReturnResult.ok(true) : ReturnResult.error("Pipeline not found: " + id));
    }

    private void handlePipelinePause(ServletRequest request, ServletResponse response) {
        String id = extractPathVariable(request, "/pipeline/{id}/pause", "id");
        boolean ok = pipelineService.pause(id);
        writeJson(response, ok ? ReturnResult.ok(true) : ReturnResult.error("Cannot pause: not running or not found"));
    }

    private void handlePipelineResume(ServletRequest request, ServletResponse response) {
        String id = extractPathVariable(request, "/pipeline/{id}/resume", "id");
        boolean ok = pipelineService.resume(id);
        writeJson(response, ok ? ReturnResult.ok(true) : ReturnResult.error("Cannot resume: not paused or not found"));
    }

    private void handlePipelineStatus(ServletRequest request, ServletResponse response) {
        String id = extractPathVariable(request, "/pipeline/{id}/status", "id");
        ScGraph graph = pipelineService.getById(id);
        if (graph == null) {
            writeJson(response, ReturnResult.error("Pipeline not found: " + id));
            return;
        }
        Map<String, Object> status = new HashMap<>();
        status.put("id", id);
        status.put("lifecycle", pipelineService.getLifecycle(id).name());
        writeJson(response, ReturnResult.ok(status));
    }

    // ==================== 数据查询 ====================

    private void handleDataQuery(ServletRequest request, ServletResponse response) {
        JsonObject body = parseBody(request);
        if (body == null) {
            writeJson(response, ReturnResult.error("Invalid request body"));
            return;
        }
        QueryRequest req = new QueryRequest();
        req.setPipelineId(body.getString("pipelineId"));
        req.setOffset(body.getInt("offset", 0));
        req.setLimit(body.getInt("limit", 20));
        writeJson(response, ReturnResult.ok(dataService.query(req)));
    }

    private void handleDataAggregate(ServletRequest request, ServletResponse response) {
        JsonObject body = parseBody(request);
        if (body == null) {
            writeJson(response, ReturnResult.error("Invalid request body"));
            return;
        }
        AggregationRequest req = new AggregationRequest();
        req.setPipelineId(body.getString("pipelineId"));
        req.setFunction(body.getString("function"));
        writeJson(response, ReturnResult.ok(dataService.aggregate(req)));
    }

    private void handleDataTrace(ServletRequest request, ServletResponse response) {
        String traceId = extractPathVariable(request, "/data/trace/{traceId}", "traceId");
        DataEnvelope envelope = dataService.trace(traceId);
        if (envelope == null) {
            writeJson(response, ReturnResult.error("Trace not found: " + traceId));
            return;
        }
        writeJson(response, ReturnResult.ok(envelope));
    }

    // ==================== 订阅管理 ====================

    private void handleSubscriptionList(ServletRequest request, ServletResponse response) {
        writeJson(response, ReturnResult.ok(new ArrayList<>(subscriptions.values())));
    }

    private void handleSubscriptionCreate(ServletRequest request, ServletResponse response) {
        JsonObject body = parseBody(request);
        if (body == null) {
            writeJson(response, ReturnResult.error("Invalid request body"));
            return;
        }
        Subscription sub = new Subscription();
        sub.setSubscriptionId(UUID.randomUUID().toString().replace("-", ""));
        sub.setClientId("client-" + System.currentTimeMillis());
        sub.setPipelineId(body.getString("pipelineId"));
        sub.setFilter(toMap(body.get("filter")));
        sub.setCreatedAt(System.currentTimeMillis());
        subscriptions.put(sub.getSubscriptionId(), sub);
        writeJson(response, ReturnResult.ok(sub));
    }

    private void handleSubscriptionCancel(ServletRequest request, ServletResponse response) {
        String id = extractPathVariable(request, "/subscription/{id}", "id");
        Subscription removed = subscriptions.remove(id);
        if (removed == null) {
            writeJson(response, ReturnResult.error("Subscription not found: " + id));
            return;
        }
        writeJson(response, ReturnResult.ok(true));
    }

    // ==================== 监控 ====================

    private void handleMonitorStats(ServletRequest request, ServletResponse response) {
        writeJson(response, ReturnResult.ok(monitorService.stats()));
    }

    private void handleMonitorThroughput(ServletRequest request, ServletResponse response) {
        writeJson(response, ReturnResult.ok(monitorService.throughput()));
    }

    private void handleMonitorErrors(ServletRequest request, ServletResponse response) {
        writeJson(response, ReturnResult.ok(monitorService.errors()));
    }

    // ==================== 死信队列 ====================

    private void handleDlqList(ServletRequest request, ServletResponse response) {
        int page = parseInt(request.getParameter("page"), 1);
        int size = parseInt(request.getParameter("size"), 20);
        List<Map<String, Object>> all = new ArrayList<>(dlqEntries.values());
        all.sort((a, b) -> Long.compare(
                (long) b.getOrDefault("createdAt", 0L),
                (long) a.getOrDefault("createdAt", 0L)));
        int from = Math.min((page - 1) * size, all.size());
        int to = Math.min(from + size, all.size());
        writeJson(response, ReturnResult.ok(all.subList(from, to)));
    }

    private void handleDlqRetry(ServletRequest request, ServletResponse response) {
        long id = parseLong(extractPathVariable(request, "/dlq/{id}/retry", "id"), -1);
        Map<String, Object> entry = dlqEntries.remove(id);
        if (entry == null) {
            writeJson(response, ReturnResult.error("DLQ entry not found: " + id));
            return;
        }
        writeJson(response, ReturnResult.ok(true));
    }

    private void handleDlqDelete(ServletRequest request, ServletResponse response) {
        long id = parseLong(extractPathVariable(request, "/dlq/{id}", "id"), -1);
        Map<String, Object> removed = dlqEntries.remove(id);
        if (removed == null) {
            writeJson(response, ReturnResult.error("DLQ entry not found: " + id));
            return;
        }
        writeJson(response, ReturnResult.ok(true));
    }

    private void handleDlqClear(ServletRequest request, ServletResponse response) {
        dlqEntries.clear();
        writeJson(response, ReturnResult.ok(true));
    }

    /** 供内部调用 — 写入死信 */
    public void offerDlq(String traceId, String pipelineId, String errorMessage) {
        long id = dlqIdSeq.getAndIncrement();
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", id);
        entry.put("traceId", traceId);
        entry.put("pipelineId", pipelineId);
        entry.put("errorMessage", errorMessage);
        entry.put("failCount", 1);
        entry.put("createdAt", System.currentTimeMillis());
        dlqEntries.put(id, entry);
    }

    // ==================== 工具方法 ====================

    private void writeJson(ServletResponse response, Object data) {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharset(StandardCharsets.UTF_8.name());
        response.setStatus(200);
        response.setBodyString(Json.toJson(data));
    }

    private JsonObject parseBody(ServletRequest request) {
        try {
            byte[] body = request.getBody();
            if (body == null || body.length == 0) return null;
            return JsonObject.parse(new String(body, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从请求路径中提取路径变量
     * 使用 PathVariableUtils 从实际请求路径匹配模式路径并提取变量
     */
    private String extractPathVariable(ServletRequest request, String pattern, String name) {
        String requestPath = request.getPath();
        Map<String, String> vars = PathVariableUtils.extractPathVariables(pattern, requestPath);
        return vars.getOrDefault(name, "");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toList(Object obj) {
        if (obj instanceof List) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : (List<?>) obj) {
                if (item instanceof Map) {
                    result.add((Map<String, Object>) item);
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> toSinksMap(Object obj) {
        if (obj instanceof Map) {
            Map<String, Map<String, Object>> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) obj).entrySet()) {
                if (entry.getValue() instanceof Map) {
                    result.put(entry.getKey(), (Map<String, Object>) entry.getValue());
                }
            }
            return result;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        return null;
    }

    private int parseInt(String s, int defaultValue) {
        try { return Integer.parseInt(s); } catch (Exception e) { return defaultValue; }
    }

    private long parseLong(String s, long defaultValue) {
        try { return Long.parseLong(s); } catch (Exception e) { return defaultValue; }
    }
}
