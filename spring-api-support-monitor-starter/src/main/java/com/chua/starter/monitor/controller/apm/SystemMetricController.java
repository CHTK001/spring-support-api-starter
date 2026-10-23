package com.chua.starter.monitor.controller.apm;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.agent.SystemMetricRegistry;
import com.chua.starter.monitor.agent.SystemMetricRequest;
import com.chua.starter.monitor.agent.SystemMetricResponse;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

        SystemMetricRequest metricRequest = new SystemMetricRequest();
        metricRequest.setMethod(HttpMethod.valueOf(request.getMethod()));
        metricRequest.setPath(path);
        metricRequest.setQuery(request.getQueryString());
        metricRequest.setBody(readBody(request));
        metricRequest.setHeaders(readAllowedHeaders(request));

        SystemMetricResponse response = registry.forward(metricRequest);
        return toResponseEntity(response);
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
            builder.contentType(MediaType.TEXT_EVENT_STREAM);
            return builder.body((StreamingResponseBody) out -> response.getStreamBody().accept(out));
        }
        if (response.getBody() == null) {
            return builder.build();
        }
        return builder.body(response.getBody());
    }
}