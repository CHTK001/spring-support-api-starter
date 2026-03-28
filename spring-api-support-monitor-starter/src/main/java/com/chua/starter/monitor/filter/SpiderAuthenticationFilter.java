package com.chua.starter.monitor.filter;

import com.chua.starter.monitor.support.SpiderConsoleSessionSupport;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Spider 轻页面接口认证过滤器。
 */
public class SpiderAuthenticationFilter implements Filter {

    private static final String[] PROTECTED_PREFIXES = {
            "/v1/spider/",
            "/api/v1/spider/",
            "/monitor/api/v1/spider/",
            "/system/api/v1/spider/"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        if (!requiresAuthentication(uri) || "OPTIONS".equalsIgnoreCase(req.getMethod()) || uri.contains("/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (SpiderConsoleSessionSupport.isAuthenticated(session)) {
            chain.doFilter(request, response);
            return;
        }

        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"code\":\"401\",\"message\":\"未登录\",\"success\":false}");
    }

    private boolean requiresAuthentication(String uri) {
        if (uri == null || uri.isEmpty()) {
            return false;
        }
        for (String prefix : PROTECTED_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
