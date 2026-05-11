package com.chua.starter.scheduler.support;

import com.chua.common.support.core.annotation.Ignore;
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
 */
@RequiredArgsConstructor
public class SchedulerJobConsoleAuthInterceptor implements HandlerInterceptor {

    private final SchedulerJobPlatformProperties properties;

    @Override
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

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contextPath = request.getContextPath();
        String target = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            target = target + "?" + query;
        }
        String loginUrl = contextPath
                + "/job-console/login.html?redirect="
                + URLEncoder.encode(target, StandardCharsets.UTF_8);
        response.sendRedirect(loginUrl);
    }

    private boolean isConsoleApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relative = contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)
                ? uri.substring(contextPath.length())
                : uri;
        return relative.startsWith("/job-console/api/");
    }

    private void writeUnauthorizedJson(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"401\",\"msg\":\"请先登录控制台\"}");
    }
}
