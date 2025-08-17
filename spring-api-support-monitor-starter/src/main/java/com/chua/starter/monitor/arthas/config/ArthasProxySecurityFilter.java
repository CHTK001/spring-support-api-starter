package com.chua.starter.monitor.arthas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Arthas 代理 HTTP 安全过滤器（可配置）
 *
 * - 仅在 monitor.arthas.proxy.security-enabled=true 时生效
 * - 校验访问令牌（X-Arthas-Token 或 ?token=）与 IP 白名单
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
@RequiredArgsConstructor
public class ArthasProxySecurityFilter extends OncePerRequestFilter {

    private final ArthasProxyProperties properties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!properties.isSecurityEnabled()) return true;
        // 仅过滤 /v1/arthas/**
        return uri == null || !uri.contains("/v1/arthas/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (checkAllowed(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("禁止访问: Arthas 代理安全校验未通过");
    }

    private boolean checkAllowed(HttpServletRequest request) {
        // Token 校验
        String token = StringUtils.defaultIfBlank(request.getHeader("X-Arthas-Token"), request.getParameter("token"));
        if (StringUtils.isNotBlank(properties.getToken())) {
            if (!StringUtils.equals(properties.getToken(), token)) {
                log.warn("Arthas 访问被拒绝: token 不匹配, uri={}, ip={}", request.getRequestURI(), getClientIp(request));
                return false;
            }
        }
        // IP 白名单
        List<String> whitelist = properties.getIpWhitelist();
        if (whitelist != null && !whitelist.isEmpty()) {
            String ip = getClientIp(request);
            boolean matched = whitelist.stream().anyMatch(w -> StringUtils.equals(w, ip));
            if (!matched) {
                log.warn("Arthas 访问被拒绝: IP 不在白名单, uri={}, ip={}", request.getRequestURI(), ip);
                return false;
            }
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP"};
        for (String h : headers) {
            String v = request.getHeader(h);
            if (StringUtils.isNotBlank(v)) {
                int idx = v.indexOf(',');
                return idx > 0 ? v.substring(0, idx).trim() : v.trim();
            }
        }
        return request.getRemoteAddr();
    }
}

