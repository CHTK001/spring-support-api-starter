package com.chua.starter.collect.support;

import com.chua.starter.collect.properties.CollectProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 采集控制台登录拦截器。
 *
 * <p>只保护本模块的 {@code /v2/collect/**} 接口。这里的 {@code @Ignore} 注解只表示跳过平台 oauth
 * 链路，不能用于跳过控制台登录，登录接口由 {@code /collect-console/auth/**} 独立路径暴露。
 * 控制台前端是独立部署的 Vue 应用，因此未登录一律返回 401 JSON，不做页面重定向。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@RequiredArgsConstructor
public class CollectConsoleAuthInterceptor implements HandlerInterceptor {

    /**
     * 控制台登录配置来源。
     */
    private final CollectProperties properties;

    /**
     * 未登录响应体。
     */
    private static final String UNAUTHORIZED_JSON = "{\"code\":\"401\",\"msg\":\"请先登录采集控制台\"}";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!properties.getConsole().isAuthEnabled() || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (CollectConsoleSessionSupport.isAuthenticated(request)) {
            return true;
        }
        writeUnauthorized(response);
        return false;
    }

    /**
     * 输出未登录的 JSON 响应。
     *
     * @param response HTTP 响应
     * @throws IOException 写出失败
     */
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(UNAUTHORIZED_JSON);
    }
}
