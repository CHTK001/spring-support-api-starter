package com.chua.starter.monitor.controller.apm;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.agent.SystemMetricRegistry;
import com.chua.starter.monitor.agent.SystemMetricRequest;
import com.chua.starter.monitor.agent.SystemMetricResponse;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * 系统指标接口（统一前缀 {@code /api/apm}）。
 *
 * <p>前端 hotsport 页面不受数据源影响，始终请求这一套契约；本控制器把请求交给
 * {@link SystemMetricRegistry}，由 SPI 自动路由到 {@code none} / {@code hotsport}
 * / {@code skywalker}。另有 {@code /api/apm/provider} 返回数据源探测状态供前端展示。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@RestController
@RequestMapping("/api/apm")
@Api(tags = "系统指标")
@Tag(name = "系统指标")
@RequiredArgsConstructor
public class SystemMetricController {

    /** 统一下游接口前缀 */
    private static final String API_PREFIX = "/api/apm";

    /** 透传给上游的请求头白名单（键已小写） */
    private static final Set<String> ALLOWED_HEADERS = Set.of(
            "content-type", "accept", "authorization", "x-request-id",
            "range", "if-none-match", "cookie"
    );

    private final SystemMetricRegistry registry;

    /**
     * 系统指标统一入口。
     *
     * @param request 原始 HTTP 请求
     * @return 上游响应或降级响应
     * @throws IOException 请求体读取失败
     */
    @RequestMapping(value = "/**", method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.PATCH
    })
    public ResponseEntity<Object> handle(HttpServletRequest request) throws IOException {
        String path = extractPath(request);
        // 兜底：即使 /provider 的专用映射未被命中（如路径规范差异），也返回状态
        if ("/provider".equals(path)) {
            return ResponseEntity.ok(ReturnResult.ok(registry.providerInfo()));
        }

        SystemMetricRequest metricRequest = toMetricRequest(request);
        metricRequest.setPath(path);

        SystemMetricResponse response = registry.forward(metricRequest);
        return toResponseEntity(response);
    }

    /**
     * SSE 实时流入口（{@code /api/apm/stream}）。
     *
     * <p><b>为什么单独成一个方法</b>：统一入口返回 {@code ResponseEntity<Object>}，
     * 泛型解析出 {@code Object}，SSE 帧会被当成普通响应体交给 Jackson 序列化，
     * 报 {@code No converter for [...] with preset Content-Type 'text/event-stream'}
     * （HTTP 500 “系统繁忙”）。所以流式响应单独一个方法写。</p>
     *
     * <p><b>为什么直接写 {@code ServletOutputStream} 而不用
     * {@code ResponseEntity<StreamingResponseBody>}</b>：后者要走 Spring 的异步
     * 分发，实测在本工程里有两个副作用 —— 一是异步请求受
     * {@code spring.mvc.async.request-timeout}（默认 30s）约束，长连接会被
     * {@code AsyncRequestTimeoutException} 掐断；二是帧虽然被 {@code flush()}，
     * 客户端却一直接不到（响应迟迟不提交）。直接同步写输出流并逐帧 flush，
     * 行为与浏览器 EventSource 的期待一致，不依赖异步分发的任何配置。</p>
     *
     * <p>路径映射为精确 {@code /stream}，比统一入口的 {@code /**} 更具体，
     * Spring 会优先匹配本方法，因此不会与之冲突。</p>
     *
     * @param request  原始 HTTP 请求
     * @param response 原始 HTTP 响应（流式写出）
     * @throws IOException 取输出流或数据源转发失败
     */
    @RequestMapping(value = "/stream", method = {RequestMethod.GET, RequestMethod.POST})
    public void stream(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SystemMetricRequest metricRequest = toMetricRequest(request);
        metricRequest.setPath("/stream");

        SystemMetricResponse metricResponse = registry.forward(metricRequest);

        Consumer<OutputStream> streamBody = metricResponse.getStreamBody();
        if (streamBody == null) {
            // 数据源未提供流（降级/异常）：回一帧后正常关闭，EventSource 会按 retry 重连，
            // 页面保持空态而不是一直转圈
            byte[] body = metricResponse.getBody();
            streamBody = out -> {
                try {
                    if (body != null && body.length > 0) {
                        out.write("data: ".getBytes(StandardCharsets.UTF_8));
                        out.write(body);
                        out.write("\n\n".getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    }
                } catch (IOException ignored) {
                    // 客户端断开时静默结束
                }
            };
        }

        String contentType = metricResponse.getHeaders().get("Content-Type");
        response.setStatus(metricResponse.getStatus());
        response.setContentType(contentType == null ? "text/event-stream;charset=UTF-8" : contentType);
        // 关掉各级缓存与反代缓冲，否则 SSE 帧会被攒着不下发
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform");
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Connection", "keep-alive");

        ServletOutputStream out = response.getOutputStream();
        log.info("[system-metric] SSE 开始写入: out={}, resp={}", out.getClass().getName(), response.getClass().getName());
        streamBody.accept(out);
        out.flush();
    }

    /**
     * 无数据源字段展示用的状态接口（保留给前端徽标轮询）。
     *
     * @return 数据源状态
     */
    @RequestMapping(value = "/provider", method = RequestMethod.GET)
    public ResponseEntity<ReturnResult<SystemMetricRegistry.ProviderInfo>> provider() {
        return ResponseEntity.ok(ReturnResult.ok(registry.providerInfo()));
    }

    // ---- 内部工具 ----

    /**
     * 把原始 HTTP 请求翻译成数据源无关的请求对象（不含 path，由调用方补齐）。
     *
     * @param request 原始 HTTP 请求
     * @return 指标请求
     * @throws IOException 请求体读取失败
     */
    private SystemMetricRequest toMetricRequest(HttpServletRequest request) throws IOException {
        SystemMetricRequest metricRequest = new SystemMetricRequest();
        metricRequest.setMethod(HttpMethod.valueOf(request.getMethod()));
        metricRequest.setQuery(request.getQueryString());
        metricRequest.setBody(readBody(request));
        metricRequest.setHeaders(readAllowedHeaders(request));
        return metricRequest;
    }

    private String extractPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        String prefix = context + API_PREFIX;
        if (uri.startsWith(prefix)) {
            uri = uri.substring(prefix.length());
        }
        if (uri.isEmpty()) {
            return "/";
        }
        return uri;
    }

    private byte[] readBody(HttpServletRequest request) throws IOException {
        return request.getInputStream().readAllBytes();
    }

    private Map<String, String> readAllowedHeaders(HttpServletRequest request) {
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        java.util.Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            String lower = name.toLowerCase(Locale.ROOT);
            if (ALLOWED_HEADERS.contains(lower)) {
                headers.put(lower, request.getHeader(name));
            }
        }
        return headers;
    }

    private ResponseEntity<Object> toResponseEntity(SystemMetricResponse response) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.getStatus());
        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }
        if (response.getStreamBody() != null) {
            // 统一入口返回 ResponseEntity<Object>，泛型解析不到 StreamingResponseBody，
            // Spring 不会走流式处理器（会报 No converter）。SSE 已由上面的 stream()
            // 独占 /stream，这里只可能是「一次性可结束」的导出类流，直接缓冲成字节后返回。
            log.warn("[系统指标] 非 /stream 路径返回流式响应，已缓冲为普通响应");
            return builder.body(bufferStream(response.getStreamBody()));
        }
        if (response.getBody() == null) {
            return builder.build();
        }
        return builder.body(response.getBody());
    }

    /**
     * 把一次性流式响应缓冲成字节数组。
     *
     * @param stream 流写入逻辑
     * @return 缓冲结果，失败时返回空数组
     */
    private byte[] bufferStream(Consumer<OutputStream> stream) {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        try {
            stream.accept(buffer);
            return buffer.toByteArray();
        } catch (Exception e) {
            log.warn("[系统指标] 缓冲流式响应失败: {}", e.getMessage());
            return new byte[0];
        }
    }
}