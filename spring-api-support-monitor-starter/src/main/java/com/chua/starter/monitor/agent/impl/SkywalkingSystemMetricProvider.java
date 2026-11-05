// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.monitor.agent.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chua.common.support.spi.annotations.Spi;
import com.chua.starter.monitor.agent.SystemMetricProperties;
import com.chua.starter.monitor.agent.SystemMetricProvider;
import com.chua.starter.monitor.agent.SystemMetricRequest;
import com.chua.starter.monitor.agent.SystemMetricResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code skywalker} 数据源 —— Apache SkyWalking OAP GraphQL 翻译层。
 *
 * <p>SkyWalking 原生是 GraphQL，前端 hotsport 页面需要的是 REST 契约
 * {@code /api/apm/*}。本实现把常用接口翻译成前端结构：</p>
 * <ul>
 *   <li>{@code /stats}、{@code /health}：QueryServices + QueryServiceMetrics；</li>
 *   <li>{@code /services}：QueryServices；</li>
 *   <li>{@code /dependencies}：QueryTopology；</li>
 *   <li>{@code /trace/{id}}、{@code /trace/{id}/waterfall}：QueryTrace。</li>
 * </ul>
 *
 * <p>未翻译的接口返回 200 空数组，避免页面空白或报错；翻译失败向上抛异常，
 * 由 {@link com.chua.starter.monitor.agent.SystemMetricRegistry} 按自动模式
 * 回退到其他数据源（最终 none）。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Spi(value = "skywalker")
public class SkywalkingSystemMetricProvider implements SystemMetricProvider {

    /** 最近时间窗口（分钟） */
    private static final String DURATION = "{ start: \"-15m\", end: \"now\", step: MINUTE }";

    private static final String QUERY_SERVICES =
            "{ queryServices(duration: " + DURATION + ") { id name } }";

    private static final String QUERY_SERVICE_METRIC =
            "{ queryServiceMetrics(serviceId: \"%s\", duration: " + DURATION
                    + ", metric: { name: \"%s\", scope: SERVICE }) { values { value } } }";

    private static final String QUERY_TOPOLOGY =
            "{ queryTopology(duration: " + DURATION
                    + ") { nodes { id name type isReal } calls { source target componentId detectPoint } } }";

    private static final String QUERY_TRACE =
            "{ queryTrace(traceId: \"%s\") { data { traceId segments { segmentId spans { "
                    + "spanId parentSpanId operationName spanType spanLayer startTime endTime isError peer "
                    + "serviceCode serviceInstanceName } } } } }";

    @Override
    public String name() {
        return "skywalker";
    }

    @Override
    public String description() {
        return "Apache SkyWalking OAP GraphQL（REST 契约翻译）";
    }

    @Override
    public boolean probe(SystemMetricProperties properties) {
        String oapUrl = properties.getSkywalking().getOapUrl();
        if (StrUtil.isBlank(oapUrl)) {
            return false;
        }
        try {
            JSONObject result = graphql(oapUrl, QUERY_SERVICES, properties.getProbeTimeoutMs());
            return result != null
                    && result.containsKey("data")
                    && !result.containsKey("errors");
        } catch (Exception e) {
            log.debug("[system-metric] skywalker 探测失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public SystemMetricResponse forward(SystemMetricRequest request, SystemMetricProperties properties) throws Exception {
        String oapUrl = properties.getSkywalking().getOapUrl();
        if (StrUtil.isBlank(oapUrl)) {
            throw new IllegalStateException("skywalker.oap-url 未配置");
        }
        if (!"GET".equalsIgnoreCase(request.getMethod().name())) {
            return jsonEmpty(request);
        }

        String path = request.getPath();
        Object data;
        if ("/stats".equals(path)) {
            data = buildStats(oapUrl, properties);
        } else if ("/health".equals(path)) {
            data = buildHealth(oapUrl, properties);
        } else if ("/services".equals(path)) {
            data = buildServices(oapUrl, properties);
        } else if ("/dependencies".equals(path) || "/dependencies/aggregated".equals(path)) {
            data = buildDependencies(oapUrl, properties);
        } else if (path.startsWith("/trace/") && path.endsWith("/waterfall")) {
            data = buildTraceWaterfall(oapUrl, traceIdFromPath(path), false, properties);
        } else if (path.startsWith("/trace/")) {
            data = buildTraceWaterfall(oapUrl, traceIdFromPath(path), true, properties);
        } else {
            // 未翻译接口：返回空数组，页面显示空态；不翻译成 404 以免页面报错
            return jsonEmpty(request);
        }
        return json(data);
    }

    // ---- 翻译实现 ----

    /** /stats */
    private JSONObject buildStats(String oapUrl, SystemMetricProperties properties) throws Exception {
        Set<String> serviceIds = serviceIds(oapUrl, properties);
        long now = System.currentTimeMillis();
        long total = serviceIds.size();
        double sumResp = 0;
        double sumCpm = 0;
        double sumSla = 0;
        long metricCount = 0;
        for (String serviceId : serviceIds) {
            Double resp = lastMetric(oapUrl, serviceId, "service_resp_time", properties);
            Double cpm = lastMetric(oapUrl, serviceId, "service_cpm", properties);
            Double sla = lastMetric(oapUrl, serviceId, "service_sla", properties);
            if (resp != null) {
                sumResp += resp;
                metricCount++;
            }
            if (cpm != null) {
                sumCpm += cpm;
                metricCount++;
            }
            if (sla != null) {
                sumSla += sla;
                metricCount++;
            }
        }
        double errorRate = metricCount > 0 && sumSla > 0 ? Math.max(0, 100 - sumSla / serviceIds.size()) : 0;
        JSONObject stats = new JSONObject(new LinkedHashMap<>());
        stats.set("agentActive", total > 0);
        stats.set("agentEnabled", total > 0);
        stats.set("transmissionCount", 0);
        stats.set("leakCount", 0);
        stats.set("errorCount", 0);
        stats.set("traceCount", 0);
        stats.set("dependencyCount", 0);
        stats.set("health", errorRate > 5 ? "SATURATED" : errorRate > 1 ? "DEGRADED" : "HEALTHY");
        stats.set("qps", Math.round(sumCpm / 60.0 * 100.0) / 100.0);
        stats.set("errorRate", Math.round(errorRate * 100.0) / 100.0);
        stats.set("activeLeaks", 0);
        stats.set("recentAlerts", JSONUtil.createArray());
        stats.set("generatedAt", now);
        return stats;
    }

    /** /health */
    private JSONObject buildHealth(String oapUrl, SystemMetricProperties properties) throws Exception {
        JSONObject stats = buildStats(oapUrl, properties);
        JSONObject health = new JSONObject(new LinkedHashMap<>());
        health.set("health", stats.getStr("health"));
        health.set("qps", stats.get("qps"));
        health.set("errorRate", stats.get("errorRate"));
        health.set("activeLeaks", 0);
        health.set("windowSeconds", 15 * 60);
        return health;
    }

    /** /services */
    private JSONArray buildServices(String oapUrl, SystemMetricProperties properties) throws Exception {
        JSONObject root = graphql(oapUrl, QUERY_SERVICES, properties.getForwardTimeoutMs());
        JSONArray result = JSONUtil.createArray();
        JSONArray services = safeArray(root, "data", "queryServices");
        for (int i = 0; i < services.size(); i++) {
            JSONObject service = services.getJSONObject(i);
            JSONObject item = new JSONObject(new LinkedHashMap<>());
            item.set("service", service.getStr("name", service.getStr("id")));
            item.set("endpointCount", 0);
            item.set("callCount", 0);
            item.set("errorCount", 0);
            item.set("errorRate", 0);
            result.add(item);
        }
        return result;
    }

    /** /dependencies */
    private JSONArray buildDependencies(String oapUrl, SystemMetricProperties properties) throws Exception {
        JSONObject root = graphql(oapUrl, QUERY_TOPOLOGY, properties.getForwardTimeoutMs());
        JSONArray result = JSONUtil.createArray();
        JSONObject topology = safeObject(root, "data", "queryTopology");
        JSONArray nodes = safeArray(topology, "nodes");
        Map<String, String> nameById = new LinkedHashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            nameById.put(node.getStr("id"), node.getStr("name", node.getStr("id")));
        }
        JSONArray calls = safeArray(topology, "calls");
        for (int i = 0; i < calls.size(); i++) {
            JSONObject call = calls.getJSONObject(i);
            String sourceId = call.getStr("source");
            String targetId = call.getStr("target");
            JSONObject edge = new JSONObject(new LinkedHashMap<>());
            edge.set("source", nameById.getOrDefault(sourceId, sourceId));
            edge.set("target", nameById.getOrDefault(targetId, targetId));
            edge.set("protocol", "");
            edge.set("software", "");
            edge.set("callCount", 0);
            edge.set("errorCount", 0);
            edge.set("avgDurationMs", 0);
            edge.set("lastError", "");
            edge.set("lastCallAt", "");
            edge.set("edgeId", sourceId + "->" + targetId);
            edge.set("errorRate", 0);
            edge.set("p95Ms", 0);
            result.add(edge);
        }
        return result;
    }

    /** /trace/{id}[/waterfall] @param legacy 返回旧传输记录数组还是瀑布结构 */
    private Object buildTraceWaterfall(String oapUrl, String traceId, boolean legacy,
                                       SystemMetricProperties properties) throws Exception {
        if (StrUtil.isBlank(traceId)) {
            return legacy ? JSONUtil.createArray() : null;
        }
        JSONObject root = graphql(oapUrl, String.format(QUERY_TRACE, escape(traceId)),
                properties.getForwardTimeoutMs());
        JSONArray segments = safeArray(root, "data", "data", "0", "segments");
        if (segments.isEmpty()) {
            return legacy ? JSONUtil.createArray() : null;
        }

        List<JSONObject> spans = new ArrayList<>();
        long minStart = Long.MAX_VALUE;
        Map<String, String> parentMap = new LinkedHashMap<>();
        Set<String> keys = new LinkedHashSet<>();
        for (int i = 0; i < segments.size(); i++) {
            JSONObject segment = segments.getJSONObject(i);
            JSONArray segmentSpans = safeArray(segment, "spans");
            for (int j = 0; j < segmentSpans.size(); j++) {
                JSONObject span = segmentSpans.getJSONObject(j);
                String spanId = span.getStr("spanId");
                String parentSpanId = span.getStr("parentSpanId");
                keys.add(spanId);
                parentMap.put(spanId, parentSpanId);
                long start = span.getLong("startTime", 0L);
                long end = span.getLong("endTime", start);
                if (start > 0) {
                    minStart = Math.min(minStart, start);
                }
                JSONObject mapped = new JSONObject(new LinkedHashMap<>());
                mapped.set("spanId", spanId);
                mapped.set("parentSpanId", parentSpanId);
                mapped.set("traceId", resolveString(root, "data", "data", "0", "traceId"));
                mapped.set("source", span.getStr("serviceCode"));
                mapped.set("target", span.getStr("peer"));
                mapped.set("protocol", "");
                mapped.set("software", span.getStr("serviceInstanceName"));
                mapped.set("operation", span.getStr("operationName"));
                mapped.set("className", "");
                mapped.set("methodName", "");
                mapped.set("status", Boolean.TRUE.equals(span.getBool("isError")) ? "ERROR" : "OK");
                mapped.set("statusCode", Boolean.TRUE.equals(span.getBool("isError")) ? 500 : 200);
                mapped.set("startTime", String.valueOf(start));
                mapped.set("endTime", String.valueOf(end));
                mapped.set("durationMs", Math.max(0, end - start));
                mapped.set("spanKind", span.getStr("spanType"));
                mapped.set("attributes", new JSONObject(new LinkedHashMap<>()));
                mapped.set("bytesOut", 0);
                mapped.set("bytesIn", 0);
                if (Boolean.TRUE.equals(span.getBool("isError"))) {
                    mapped.set("errorType", "ERROR");
                    mapped.set("errorMessage", "SkyWalking segment error");
                }
                spans.add(mapped);
            }
        }

        if (legacy) {
            // /trace/{id} 在老契约里返回 TransmissionRecord[]（此处映射 span 简化结构）
            JSONArray list = JSONUtil.createArray();
            for (JSONObject span : spans) {
                JSONObject item = new JSONObject(new LinkedHashMap<>());
                item.set("id", 0);
                item.set("traceId", span.getStr("traceId"));
                item.set("spanId", span.getStr("spanId"));
                item.set("parentSpanId", span.getStr("parentSpanId"));
                item.set("source", span.getStr("source"));
                item.set("target", span.getStr("target"));
                item.set("protocol", span.getStr("protocol"));
                item.set("software", "SkyWalking");
                item.set("durationMs", span.getLong("durationMs", 0L));
                item.set("status", span.getStr("status"));
                item.set("startTime", span.getStr("startTime"));
                item.set("errorType", span.getStr("errorType"));
                item.set("errorMessage", span.getStr("errorMessage"));
                list.add(item);
            }
            return list;
        }

        JSONObject waterfall = new JSONObject(new LinkedHashMap<>());
        waterfall.set("traceId", traceId);
        waterfall.set("spans", new JSONArray(spans));
        waterfall.set("totalMs", minStart == Long.MAX_VALUE ? 0 : spans.isEmpty() ? 0 : maxEnd(spans) - minStart);
        waterfall.set("spanCount", spans.size());
        return waterfall;
    }

    /** 计算最大结束时间 */
    private long maxEnd(List<JSONObject> spans) {
        long value = 0;
        for (JSONObject span : spans) {
            String end = span.getStr("endTime");
            if (end != null) {
                try {
                    value = Math.max(value, Long.parseLong(end));
                } catch (NumberFormatException ignored) {
                    // skip
                }
            }
        }
        return value;
    }

    // ---- GraphQL 公共逻辑 ----

    /** 获取服务 ID 集合 */
    private Set<String> serviceIds(String oapUrl, SystemMetricProperties properties) throws Exception {
        JSONObject root = graphql(oapUrl, QUERY_SERVICES, properties.getForwardTimeoutMs());
        JSONArray services = safeArray(root, "data", "queryServices");
        Set<String> ids = new LinkedHashSet<>();
        for (int i = 0; i < services.size(); i++) {
            String id = services.getJSONObject(i).getStr("id");
            if (StrUtil.isNotBlank(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    /** 读取某个服务指标的最近值 */
    private Double lastMetric(String oapUrl, String serviceId, String metric, SystemMetricProperties properties) throws Exception {
        JSONObject root = graphql(oapUrl, String.format(QUERY_SERVICE_METRIC, escape(serviceId), metric),
                properties.getForwardTimeoutMs());
        JSONArray values = safeArray(root, "data", "queryServiceMetrics", "values");
        if (values.isEmpty()) {
            return null;
        }
        JSONObject last = values.getJSONObject(values.size() - 1);
        Object value = last.get("value");
        if (value == null || value instanceof JSONObject) {
            // 有的 OAP 版本 value 为对象 {id,value}
            value = ((JSONObject) value).get("value");
        }
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * GraphQL 调用。
     *
     * @param oapUrl OAP 地址
     * @param query 查询文本
     * @param timeoutMs 超时
     * @return 响应 JSON，若 HTTP 失败抛 IOException
     */
    private JSONObject graphql(String oapUrl, String query, long timeoutMs) throws IOException, InterruptedException {
        String endpoint = oapUrl.endsWith("/") ? oapUrl : oapUrl + "/";
        JSONObject payload = new JSONObject(new LinkedHashMap<>());
        payload.set("query", query);
        byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint + "graphql"))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Content-Type", "application/json;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("SkyWalking OAP 返回 " + response.statusCode());
        }
        String text = new String(response.body(), StandardCharsets.UTF_8);
        JSONObject result = JSONUtil.parseObj(text);
        if (result.containsKey("errors")) {
            throw new IOException("SkyWalking GraphQL 返回错误: " + result.getStr("errors"));
        }
        return result;
    }

    // ---- 工具 ----

    private String traceIdFromPath(String path) {
        int start = "/trace/".length();
        String tail = path.substring(start);
        if (tail.endsWith("/waterfall")) {
            tail = tail.substring(0, tail.length() - "/waterfall".length());
        }
        return tail;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** 沿路径逐级取对象；支持数字下标（JSONArray 下标） */
    private JSONObject safeObject(JSONObject root, String... path) {
        Object current = resolve(root, path);
        return current instanceof JSONObject ? (JSONObject) current : new JSONObject(new LinkedHashMap<>());
    }

    /** 沿路径逐级取数组；支持数字下标 */
    private JSONArray safeArray(JSONObject root, String... path) {
        Object current = resolve(root, path);
        return current instanceof JSONArray ? (JSONArray) current : JSONUtil.createArray();
    }

    /** 沿路径逐级取值并转字符串 */
    private String resolveString(JSONObject root, String... path) {
        Object current = resolve(root, path);
        return current == null ? "" : String.valueOf(current);
    }

    /** 通用路径解析：数字路径段按 JSONArray 下标处理，其余按 JSONObject 键 */
    private Object resolve(Object current, String... path) {
        if (current == null) {
            return null;
        }
        for (String key : path) {
            if (current instanceof JSONObject) {
                current = ((JSONObject) current).get(key);
            } else if (current instanceof JSONArray && isInteger(key)) {
                int index = Integer.parseInt(key);
                JSONArray array = (JSONArray) current;
                current = index >= 0 && index < array.size() ? array.get(index) : null;
            } else {
                return null;
            }
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private boolean isInteger(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private SystemMetricResponse json(Object data) {
        JSONObject wrapper = new JSONObject(new LinkedHashMap<>());
        wrapper.set("code", 200);
        wrapper.set("msg", "ok");
        wrapper.set("data", data);
        return SystemMetricResponse.of(200, "application/json;charset=UTF-8",
                wrapper.toString().getBytes(StandardCharsets.UTF_8));
    }

    private SystemMetricResponse jsonEmpty(SystemMetricRequest request) {
        Object data = "GET".equalsIgnoreCase(request.getMethod().name())
                ? JSONUtil.createArray()
                : null;
        return json(data);
    }}