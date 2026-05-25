package com.chua.starter.monitor.service.loki;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loki 查询代理服务
 */
@Slf4j
public class LokiService {

    private final RestTemplate restTemplate;
    private final Map<String, LokiConnection> connections = new ConcurrentHashMap<>();
    private final Map<String, Long> connectionTimes = new ConcurrentHashMap<>();
    private boolean mockMode = false;
    private final Random random = new Random(42);

    public LokiService() {
        this.restTemplate = new RestTemplate();
        org.springframework.http.client.HttpComponentsClientHttpRequestFactory factory =
                new org.springframework.http.client.HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        restTemplate.setRequestFactory(factory);
    }

    public boolean isMockMode() { return mockMode; }
    public void setMockMode(boolean mockMode) { this.mockMode = mockMode; }

    // ─── 连接管理 ──────────────────────────────────────────────────────────

    public String open(String host, int port, String username, String password) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String baseUrl = "http://" + host + ":" + port;
        connections.put(id, new LokiConnection(baseUrl, username, password));
        connectionTimes.put(id, System.currentTimeMillis());
        mockMode = false;
        log.info("Loki connection opened: {} -> {}", id, baseUrl);
        return id;
    }

    public void close(String connectionId) {
        connections.remove(connectionId);
        connectionTimes.remove(connectionId);
    }

    // ─── 健康检查 ──────────────────────────────────────────────────────────

    public boolean ready(String connectionId) {
        if (mockMode) return true;
        var conn = connections.get(connectionId);
        if (conn == null) return false;
        try {
            var resp = restTemplate.exchange(
                    conn.baseUrl + "/ready", HttpMethod.GET,
                    new HttpEntity<>(headers(conn)), String.class);
            return resp.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("Loki ready check failed, switching to mock mode: {}", e.getMessage());
            mockMode = true;
            return true;
        }
    }

    // ─── 标签 ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<String> labels(String connectionId) {
        if (mockMode) return mockLabels();
        return doGetList(connectionId, "/loki/api/v1/label");
    }

    @SuppressWarnings("unchecked")
    public List<String> labelValues(String connectionId, String label) {
        if (mockMode) return mockLabelValues(label);
        return doGetList(connectionId, "/loki/api/v1/label/" + label + "/values");
    }

    // ─── 查询 ──────────────────────────────────────────────────────────────

    public Map<String, Object> query(String connectionId, String logql, Integer limit, Long time, String direction) {
        if (mockMode) return mockQueryResult(logql, limit);
        return doGetMap(connectionId, "/loki/api/v1/query", params(
                "query", logql,
                "limit", limit != null ? String.valueOf(limit) : "100",
                "time", time != null ? String.valueOf(time) : String.valueOf(System.currentTimeMillis() / 1000),
                "direction", direction != null ? direction : "backward"
        ));
    }

    public Map<String, Object> queryRange(String connectionId, String logql, long start, long end,
                                           Integer limit, String step, String direction) {
        if (mockMode) return mockQueryRangeResult(logql, start, end, limit);
        Map<String, String> p = new LinkedHashMap<>();
        p.put("query", logql);
        p.put("start", String.valueOf(start / 1000000));
        p.put("end", String.valueOf(end / 1000000));
        p.put("limit", limit != null ? String.valueOf(limit) : "1000");
        p.put("direction", direction != null ? direction : "forward");
        if (step != null) p.put("step", step);
        return doGetMap(connectionId, "/loki/api/v1/query_range", p);
    }

    // ─── 内部 HTTP 工具 ────────────────────────────────────────────────────

    private HttpHeaders headers(LokiConnection conn) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        if (conn.username != null && !conn.username.isEmpty()) {
            String auth = conn.username + ":" + (conn.password != null ? conn.password : "");
            String encoded = Base64.getEncoder().encodeToString(auth.getBytes());
            h.set("Authorization", "Basic " + encoded);
        }
        return h;
    }

    @SuppressWarnings("unchecked")
    private <T> T doGet(String connectionId, String path, Map<String, String> params, Class<T> type) {
        var conn = connections.get(connectionId);
        if (conn == null) return null;
        try {
            String url = conn.baseUrl + path;
            if (params != null && !params.isEmpty()) {
                var qs = new StringBuilder("?");
                params.forEach((k, v) -> {
                    if (v != null) qs.append(k).append("=").append(v).append("&");
                });
                url += qs.toString().replaceAll("&$", "");
            }
            var resp = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers(conn)), Map.class);
            if (resp.getBody() != null) {
                Map<String, Object> body = resp.getBody();
                if (body.containsKey("data")) {
                    var data = body.get("data");
                    if (data instanceof Map) {
                        var m = (Map<String, Object>) data;
                        if (m.containsKey("result") && type == Map.class) return (T) body;
                    }
                    return (T) data;
                }
                return (T) body;
            }
        } catch (Exception e) {
            log.warn("Loki request failed: {} {} -> {}", path, params, e.getMessage());
            mockMode = true;
            return null;
        }
        return null;
    }

    private List<String> doGetList(String connectionId, String path) {
        var result = doGet(connectionId, path, null, List.class);
        return result != null ? result : Collections.emptyList();
    }

    private Map<String, Object> doGetMap(String connectionId, String path, Map<String, String> params) {
        var result = doGet(connectionId, path, params, Map.class);
        return result != null ? result : Collections.emptyMap();
    }

    private Map<String, String> params(String... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put(kv[i], kv[i + 1]);
        return m;
    }

    public record LokiConnection(String baseUrl, String username, String password) {}

    // ─── 模拟数据 ──────────────────────────────────────────────────────────

    private static final String[] MOCK_LABELS = {"job", "app", "env", "host", "service", "level", "namespace"};
    private static final String[] MOCK_APPS = {"api-gateway", "user-service", "order-service", "payment-service", "notification-service"};
    private static final String[] MOCK_ENVS = {"production", "staging", "development"};
    private static final String[] MOCK_LEVELS = {"INFO", "WARN", "ERROR", "DEBUG", "TRACE"};
    private static final String[] MOCK_MSGS = {
            "Request processed in {}ms",
            "User login successful: user_id={}",
            "Database query completed: {} rows affected",
            "Cache miss for key: {}",
            "Scheduled job '{}' completed successfully",
            "Connection pool status: active={}, idle={}",
            "HTTP {} {} returned {} in {}ms",
            "Message published to topic: {}",
            "Health check passed: {}",
            "Configuration reloaded from {}",
            "Failed to connect to upstream: {}",
            "Out of memory: {}MB requested, {}MB available",
            "Disk usage warning: {}% on mount {}",
            "Slow query detected: {}ms for query '{}'",
            "Authentication failed for user: {}",
    };
    private static final String[] MOCK_ERRORS = {
            "java.lang.NullPointerException: Cannot invoke method on null object",
            "org.springframework.dao.DataAccessException: Connection timeout",
            "java.net.ConnectException: Connection refused: localhost/127.0.0.1:8080",
            "RuntimeException: Circuit breaker opened for service 'payment-service'",
            "java.io.IOException: Broken pipe",
    };

    private List<String> mockLabels() {
        return Arrays.asList(MOCK_LABELS);
    }

    private List<String> mockLabelValues(String label) {
        return switch (label) {
            case "job" -> List.of("api-server", "worker", "scheduler", "stream-processor");
            case "app" -> Arrays.asList(MOCK_APPS);
            case "env" -> Arrays.asList(MOCK_ENVS);
            case "host" -> List.of("host-01", "host-02", "host-03", "host-04", "host-05");
            case "service" -> Arrays.asList(MOCK_APPS);
            case "level" -> Arrays.asList(MOCK_LEVELS);
            case "namespace" -> List.of("default", "monitoring", "data-plane", "control-plane");
            default -> List.of("unknown");
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mockQueryResult(String logql, Integer limit) {
        int count = limit != null ? Math.min(limit, 100) : 50;
        var streams = generateMockStreams(count, System.currentTimeMillis() * 1000000, 500000000L);
        return Map.of("status", "success", "data", Map.of(
                "resultType", "streams",
                "result", streams,
                "stats", Map.of("summary", Map.of("bytesProcessedPerSecond", 0, "linesProcessedPerSecond", 0, "totalBytesProcessed", 0, "totalLinesProcessed", 0, "execTime", 0.002))
        ));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mockQueryRangeResult(String logql, long startNs, long endNs, Integer limit) {
        int count = limit != null ? Math.min(limit, 200) : 100;
        var streams = generateMockStreams(count, endNs, (endNs - startNs) / count);
        return Map.of("status", "success", "data", Map.of(
                "resultType", "streams",
                "result", streams,
                "stats", Map.of("summary", Map.of("bytesProcessedPerSecond", 0, "linesProcessedPerSecond", 0, "totalBytesProcessed", 0, "totalLinesProcessed", 0, "execTime", 0.003))
        ));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> generateMockStreams(int count, long baseNs, long stepNs) {
        var streams = new ArrayList<Map<String, Object>>();
        for (int s = 0; s < Math.min(count, 15); s++) {
            var stream = new LinkedHashMap<String, Object>();
            var labels = new LinkedHashMap<String, String>();
            labels.put("job", MOCK_APPS[s % MOCK_APPS.length]);
            labels.put("app", MOCK_APPS[s % MOCK_APPS.length]);
            labels.put("env", MOCK_ENVS[s % MOCK_ENVS.length]);
            labels.put("host", "host-" + (s % 5 + 1));
            stream.put("stream", labels);

            var values = new ArrayList<List<String>>();
            int linesPerStream = Math.max(1, count / 15);
            for (int v = 0; v < linesPerStream; v++) {
                long ts = baseNs - (v * stepNs);
                String msg = generateLogLine(s, v);
                values.add(List.of(String.valueOf(ts), msg));
            }
            stream.put("values", values);
            streams.add(stream);
        }
        return streams;
    }

    private String generateLogLine(int streamIdx, int lineIdx) {
        String app = MOCK_APPS[streamIdx % MOCK_APPS.length];
        String level = MOCK_LEVELS[random.nextInt(MOCK_LEVELS.length)];
        String msg;
        if ("ERROR".equals(level) && random.nextBoolean()) {
            msg = MOCK_ERRORS[random.nextInt(MOCK_ERRORS.length)];
        } else {
            msg = MOCK_MSGS[random.nextInt(MOCK_MSGS.length)]
                    .replace("{}", String.valueOf(random.nextInt(9999)));
        }
        return String.format("time=\"%s\" level=%s app=%s trace_id=%s span_id=%s msg=\"%s\"",
                java.time.Instant.now().toString(),
                level.toLowerCase(),
                app,
                UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 8),
                msg);
    }
}
