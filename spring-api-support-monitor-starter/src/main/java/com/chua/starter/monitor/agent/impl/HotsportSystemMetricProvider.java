package com.chua.starter.monitor.agent.impl;

import cn.hutool.core.util.StrUtil;
import com.chua.common.support.spi.annotations.Spi;
import com.chua.starter.monitor.agent.SystemMetricProperties;
import com.chua.starter.monitor.agent.SystemMetricProvider;
import com.chua.starter.monitor.agent.SystemMetricRequest;
import com.chua.starter.monitor.agent.SystemMetricResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * {@code hotsport} 数据源 —— 到 hotsport 观测后端的整体反向代理。
 *
 * <p>前端与 hotsport 后端共用同一套 {@code /api/apm/*} 契约，因此这里不需要
 * 逐接口翻译，直接把方法 / 路径 / 查询 / 体透传过去即可，天然覆盖海量接口
 * （传输记录、依赖、泄漏、日志、告警、Agent 配置、JVM、拓扑、导出…），
 * 也天然支持 SSE 实时流（以流式方式回传）。</p>
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

        HttpRequest upstream = builder.build();

        if (stream) {
            return proxyStream(upstream, client);
        }

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

    /** 流式透传（SSE），逐块转发上游输入流 */
    private SystemMetricResponse proxyStream(HttpRequest upstream, HttpClient client) {
        return SystemMetricResponse.stream("text/event-stream;charset=UTF-8", out -> {
            try {
                HttpResponse<InputStream> response = client.send(upstream, HttpResponse.BodyHandlers.ofInputStream());
                try (InputStream in = response.body()) {
                    in.transferTo(out);
                }
                out.flush();
            } catch (Exception e) {
                log.debug("[system-metric] hotsport SSE 转发中断: {}", e.getMessage());
                try {
                    OutputStream os = out;
                    os.write(("data: {\"ts\":" + System.currentTimeMillis() + ",\"data\":null}\n\n")
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8));
                } catch (Exception ignored) {
                    // 客户端已断开
                }
            }
        });
    }
}