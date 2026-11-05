// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.monitor.agent.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chua.common.support.spi.annotations.Spi;
import com.chua.starter.monitor.agent.SystemMetricProperties;
import com.chua.starter.monitor.agent.SystemMetricProvider;
import com.chua.starter.monitor.agent.SystemMetricRequest;
import com.chua.starter.monitor.agent.SystemMetricResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * {@code hotsport} 数据源 —— 到 hotsport 观测后端的整体反向代理。
 *
 * <p>前端与 hotsport 后端共用同一套 {@code /api/apm/*} 契约，因此这里不需要
 * 逐接口翻译，直接把方法 / 路径 / 查询 / 体透传过去即可，天然覆盖海量接口
 * （传输记录、依赖、泄漏、日志、告警、Agent 配置、JVM、拓扑、导出…）。</p>
 *
 * <p>唯一的例外是 SSE 实时流 {@code /stream}：上游的流实现有缺陷（单线程调度 +
 * 空 payload），不适合逐字透传，改由 {@code synthesizedStream} 用
 * {@code /stats}、{@code /health} 的 REST 快照合成本地实时帧。</p>
 *
 * <p>目标地址拼接规则：{@code system.metric.hotsport.base-url} 以
 * {@code /api/apm} 结尾视为已含统一前缀，直接拼路径；否则拼
 * {@code /api/apm} 后再拼路径。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Spi(value = "hotsport")
public class HotsportSystemMetricProvider implements SystemMetricProvider {

    /** 统一接口前缀（与前端 APM_PREFIX 对齐） */
    private static final String APM_PREFIX = "/api/apm";

    /** SSE 帧前缀（事件名固定为 stats，与 hotsport 原契约一致） */
    private static final String STREAM_EVENT_PREFIX = "event:stats\ndata: ";

    /** SSE 帧结尾（空行结束一帧） */
    private static final String STREAM_EVENT_SUFFIX = "\n\n";

    /** SSE 推送间隔（毫秒），与 hotsport 默认值保持一致 */
    private static final long STREAM_INTERVAL_MS = 3000L;

    /** SSE 单次连接最长时长（30 分钟），到期后关闭，EventSource 会自动重连 */
    private static final long STREAM_MAX_MILLIS = 30L * 60 * 1000;

    @Override
    public String name() {
        return "hotsport";
    }

    @Override
    public String description() {
        return "hotsport 观测后端（整体透传 /api/apm/*）";
    }

    @Override
    public boolean probe(SystemMetricProperties properties) {
        String baseUrl = properties.getHotsport().getBaseUrl();
        if (StrUtil.isBlank(baseUrl)) {
            return false;
        }
        String url = buildUrl(baseUrl, "/health", null);
        try {
            HttpClient client = shortClient(properties);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(properties.getProbeTimeoutMs()))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 500;
        } catch (Exception e) {
            log.debug("[system-metric] hotsport 探测失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public SystemMetricResponse forward(SystemMetricRequest request, SystemMetricProperties properties) throws Exception {
        String baseUrl = properties.getHotsport().getBaseUrl();
        if (StrUtil.isBlank(baseUrl)) {
            throw new IllegalStateException("hotsport.base-url 未配置");
        }
        String url = buildUrl(baseUrl, request.getPath(), request.querySuffix());

        boolean stream = isStream(request.getPath());
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getForwardTimeoutMs()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMillis(properties.getForwardTimeoutMs()));
        // 透传少量白名单请求头（认证 / 内容类型等）
        Map<String, String> headers = request.getHeaders();
        String contentType = headers.getOrDefault("Content-Type", headers.get("content-type"));
        String accept = headers.getOrDefault("Accept", headers.get("accept"));
        String authorization = headers.getOrDefault("Authorization", headers.get("authorization"));
        if (StrUtil.isNotBlank(accept)) {
            builder.header("Accept", accept);
        }
        if (StrUtil.isNotBlank(authorization)) {
            builder.header("Authorization", authorization);
        }

        byte[] body = request.getBody();
        boolean hasBody = body != null && body.length > 0;
        if (hasBody) {
            if (StrUtil.isNotBlank(contentType)) {
                builder.header("Content-Type", contentType);
            } else {
                builder.header("Content-Type", "application/json;charset=UTF-8");
            }
            builder.method(request.getMethod().name(), HttpRequest.BodyPublishers.ofByteArray(body));
        } else {
            String method = request.getMethod().name();
            if ("POST".equals(method)) {
                builder.header("Content-Type", contentType == null ? "application/json;charset=UTF-8" : contentType);
                builder.POST(HttpRequest.BodyPublishers.noBody());
            } else if ("PUT".equals(method)) {
                builder.PUT(HttpRequest.BodyPublishers.noBody());
            } else if ("DELETE".equals(method)) {
                builder.method("DELETE", HttpRequest.BodyPublishers.noBody());
            } else {
                builder.GET();
            }
        }

        if (stream) {
            // SSE 不逐字节透传上游流：hotsport 的流自身有单线程调度与空 payload 问题，
            // 改由 synthesizedStream 用 REST 快照合成（原因见该方法注释）
            return synthesizedStream(properties, baseUrl);
        }

        HttpRequest upstream = builder.build();
        HttpResponse<byte[]> response = client.send(upstream, HttpResponse.BodyHandlers.ofByteArray());
        SystemMetricResponse result = new SystemMetricResponse();
        result.setStatus(response.statusCode());
        result.getHeaders().put("Content-Type", response.headers().firstValue("Content-Type").orElse("application/json;charset=UTF-8"));
        result.setBody(response.body());
        return result;
    }

    /** 是否为 SSE 流式接口 */
    private boolean isStream(String path) {
        return path != null && path.startsWith("/stream");
    }

    /** 探测用短超时客户端 */
    private HttpClient shortClient(SystemMetricProperties properties) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getProbeTimeoutMs()))
                .build();
    }

    /**
     * 拼接上游地址。
     *
     * @param baseUrl 配置的源地址
     * @param path 接口路径（不含前缀）
     * @param querySuffix {@code ?a=b} 或空串
     * @return 完整地址
     */
    private String buildUrl(String baseUrl, String path, String querySuffix) {
        String origin = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String prefix = origin.endsWith(APM_PREFIX) ? "" : APM_PREFIX;
        String p = path == null || path.startsWith("/") ? path : "/" + path;
        return origin + prefix + p + (querySuffix == null ? "" : querySuffix);
    }

    /**
     * 合成 SSE 实时流。
     *
     * <p>不直接透传 hotsport 的 {@code /api/apm/stream}，原因有两条：</p>
     * <ol>
     *   <li>hotsport 的 SSE 由<b>单线程</b>调度器驱动，只要有任何一个客户端断开后
     *       写入阻塞，所有订阅者都会一起断流，透传必然被上游拖死；</li>
     *   <li>它当前推出来的帧是 {@code event:stats} + <b>空 data</b>
     *       （实测原始字节 {@code event:stats\ndata:\n\n}），前端 EventSource
     *       解析不到任何数据，实时看板等于是空的。</li>
     * </ol>
     *
     * <p>所以这里改成「用 REST 快照合成 SSE」：每隔 {@value #STREAM_INTERVAL_MS} 毫秒
     * 取一次 {@code /stats} 与 {@code /health}，合并成
     * {@code {"ts":...,"data":{...}}} 作为 {@code stats} 事件下发。每次都是一次
     * 短连接，任何一个客户端断开都只影响它自己，帧里也带着真实指标。</p>
     *
     * @param properties 指标配置
     * @param baseUrl    hotsport 基础地址
     * @return 流式响应
     */
    private SystemMetricResponse synthesizedStream(SystemMetricProperties properties, String baseUrl) {
        return SystemMetricResponse.stream("text/event-stream;charset=UTF-8", out -> {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(properties.getForwardTimeoutMs()))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            long deadline = System.currentTimeMillis() + STREAM_MAX_MILLIS;
            String statsUrl = buildUrl(baseUrl, "/stats", null);
            String healthUrl = buildUrl(baseUrl, "/health", null);
            try {
                while (System.currentTimeMillis() < deadline && !Thread.currentThread().isInterrupted()) {
                    String payload = buildStatsFrame(client, statsUrl, healthUrl, properties.getForwardTimeoutMs());
                    if (payload == null) {
                        // 上游整体不可达：补一帧降级心跳后结束，EventSource 会按 retry 重连
                        writeStreamErrorFrame(out);
                        return;
                    }
                    out.write(STREAM_EVENT_PREFIX.getBytes(StandardCharsets.UTF_8));
                    out.write(payload.getBytes(StandardCharsets.UTF_8));
                    out.write(STREAM_EVENT_SUFFIX.getBytes(StandardCharsets.UTF_8));
                    // 每帧立即 flush：SSE 帧很小，不 flush 会一直卡在容器缓冲里
                    out.flush();
                    Thread.sleep(STREAM_INTERVAL_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                // 客户端已断开：正常结束
                log.debug("[system-metric] SSE 客户端断开: {}", e.getMessage());
            }
        });
    }

    /**
     * 组装一帧 {@code stats} 事件的数据体。
     *
     * @param client    客户端
     * @param statsUrl  {@code /stats} 地址
     * @param healthUrl {@code /health} 地址
     * @param timeoutMs 单次请求超时
     * @return JSON 文本；上游全部不可达时返回 {@code null}
     */
    private String buildStatsFrame(HttpClient client, String statsUrl, String healthUrl, long timeoutMs) {
        JSONObject stats = fetchDataObject(client, statsUrl, timeoutMs);
        JSONObject health = fetchDataObject(client, healthUrl, timeoutMs);
        if (stats == null && health == null) {
            return null;
        }
        JSONObject data = new JSONObject();
        if (stats != null) {
            data.putAll(stats);
        }
        if (health != null) {
            data.putAll(health);
        }
        JSONObject frame = new JSONObject();
        frame.set("ts", System.currentTimeMillis());
        frame.set("data", data);
        return frame.toString();
    }

    /**
     * 取上游接口 {@code data} 字段对象。
     *
     * @param client    客户端
     * @param url       地址
     * @param timeoutMs 超时
     * @return {@code data} 对象；失败或非对象时返回 {@code null}
     */
    private JSONObject fetchDataObject(HttpClient client, String url, long timeoutMs) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }
            Object data = JSONUtil.parseObj(response.body()).get("data");
            return data instanceof JSONObject object ? object : null;
        } catch (Exception e) {
            log.debug("[system-metric] 取 {} 失败: {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * 写一帧降级心跳，让 EventSource 保持连接并按 retry 重连。
     *
     * @param out 输出流
     */
    private void writeStreamErrorFrame(OutputStream out) {
        try {
            out.write((STREAM_EVENT_PREFIX + "{\"ts\":" + System.currentTimeMillis() + ",\"data\":null}"
                    + STREAM_EVENT_SUFFIX).getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Exception ignored) {
            // 客户端已断开
        }
    }}