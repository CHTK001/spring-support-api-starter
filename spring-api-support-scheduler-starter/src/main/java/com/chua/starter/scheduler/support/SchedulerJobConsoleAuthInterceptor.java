package com.chua.starter.scheduler.support;

import com.chua.common.support.annotation.Ignore;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 轻量控制台登录拦截器。
 * <p>
 * 这里只保护内置控制台页面资源，不干预平台 API 的现有调用方。
 * </p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@RequiredArgsConstructor
public class SchedulerJobConsoleAuthInterceptor implements HandlerInterceptor {

    /**
     * 调度平台配置
     */
    private final SchedulerJobPlatformProperties properties;

    /**
     * 登录页路径前缀
     */
    private static final String LOGIN_PATH_PREFIX = "/job-console/login.html?redirect=";

    /**
     * 控制台 API 路径前缀
     */
    private static final String CONSOLE_API_PREFIX = "/job-console/api/";

    /**
     * 未登录 JSON 响应内容
     */
    private static final String UNAUTHORIZED_JSON = "{\"code\":\"401\",\"msg\":\"请先登录控制台\"}";

    @Override
    /** Pre处理 */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!properties.getConsole().isAuthEnabled()) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.getMethod().isAnnotationPresent(Ignore.class) ||
                    handlerMethod.getBeanType().isAnnotationPresent(Ignore.class)) {
                return true;
            }
        }
        if (SchedulerJobConsoleSessionSupport.isAuthenticated(request)) {
            return true;
        }
        if (isConsoleApi(request)) {
            writeUnauthorizedJson(response);
            return false;
        }
        redirectToLogin(request, response);
        return false;
    }

    /**
     * 将请求重定向到控制台登录页。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws IOException 写入重定向失败时抛出
     */
    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contextPath = request.getContextPath();
        String target = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            target = target + "?" + query;
        }
        String loginUrl = contextPath
                + LOGIN_PATH_PREFIX
                + URLEncoder.encode(target, StandardCharsets.UTF_8);
        response.sendRedirect(loginUrl);
    }

    /**
     * 判断请求是否属于控制台 API。
     *
     * @param request HTTP 请求
     * @return 控制台 API 请求返回 true, 否则返回 false
     */
    private boolean isConsoleApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relative = contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)
                ? uri.substring(contextPath.length())
                : uri;
        return relative.startsWith(CONSOLE_API_PREFIX);
    }

    /**
     * 输出未登录的统一 JSON 响应。
     *
     * @param response HTTP 响应
     * @throws IOException 写入响应失败时抛出
     */
    private void writeUnauthorizedJson(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(UNAUTHORIZED_JSON);
    }
}
