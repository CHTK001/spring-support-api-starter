package com.chua.starter.payment.support;

import com.chua.starter.payment.properties.PaymentConsoleProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 支付控制台轻量登录拦截器。
 */
@RequiredArgsConstructor
public class PaymentConsoleAuthInterceptor implements HandlerInterceptor {

    private final PaymentConsoleProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!properties.isAuthEnabled()) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (PaymentConsoleSessionSupport.isAuthenticated(request)) {
            return true;
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
                + "/payment-console/login.html?redirect="
                + URLEncoder.encode(target, StandardCharsets.UTF_8);
        response.sendRedirect(loginUrl);
    }
}
