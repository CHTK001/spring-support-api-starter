package com.chua.starter.oauth.sso.support.security;

import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * OAuth IP 级请求限流过滤器
 *
 * <p>基于 Redis 计数器按客户端 IP 限制单位时间窗口内的请求次数，防止接口被刷。</p>
 * <p>与 {@link LoginAttemptService} 互补：本过滤器按 IP 限流，登录锁定按用户名限流。</p>
 *
 * @since 2025/01/01
 * @author CH
 */
@Slf4j
@RequiredArgsConstructor
public class OauthRateLimitFilter implements Filter, Ordered {

    /** Rate_限制_前缀 */
    private static final String RATE_LIMIT_PREFIX = "oauth:security:rate:";

    /** 安全性属性 */
    private final OauthSecurityProperties securityProperties;
    /** 字符串Redis模板 */
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    /**
    * 执行过滤
    * @param servletRequest servlet请求
    * @param servletResponse servlet响应
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (!shouldLimit(request)) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        OauthSecurityProperties.RateLimit rateLimit = securityProperties.getRateLimit();
        String key = RATE_LIMIT_PREFIX + clientIp + ":" + request.getRequestURI();

        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count == null) {
                count = 1L;
            }
            if (count == 1L) {
                stringRedisTemplate.expire(key, Duration.ofSeconds(rateLimit.getWindowSeconds()));
            }

            if (count > rateLimit.getMaxRequests()) {
                log.warn("IP限流触发: ip={}, uri={}, count={}, max={}",
                        clientIp, request.getRequestURI(), count, rateLimit.getMaxRequests());
                writeTooManyRequests(response, rateLimit);
                return;
            }
        } catch (Exception e) {
            log.error("限流检查异常, 放行请求: ip={}, uri={}, error={}",
                    clientIp, request.getRequestURI(), e.getMessage());
        }

        chain.doFilter(request, response);
    }

    /**
     * 是否应该限制
     *
     * @param request 请求
     * @return should限制的结果
     */
    private boolean shouldLimit(HttpServletRequest request) {
        String uri = request.getRequestURI();
        OauthSecurityProperties.RateLimit rateLimit = securityProperties.getRateLimit();

        if (matchesAny(uri, rateLimit.getIgnorePaths())) {
            return false;
        }

        List<String> paths = rateLimit.getPaths();
        if (paths == null || paths.isEmpty()) {
            return true;
        }
        return matchesAny(uri, paths);
    }

    /**
     * 匹配任意
     *
     * @param uri uri
     * @param patterns 模式
     * @return 匹配任意的结果
     */
    private boolean matchesAny(String uri, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (!StringUtils.hasText(pattern)) {
                continue;
            }
            if (uri.equals(pattern) || uri.endsWith(pattern) || uri.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析客户端ip
     *
     * @param request 请求
     * @return resolve客户端ip的结果
     */
    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip == null ? "unknown" : ip;
    }

    /**
      * 写入toomanyRequests
     * @param response 响应
     */
    private void writeTooManyRequests(HttpServletResponse response, OauthSecurityProperties.RateLimit rateLimit)
            throws IOException {
        response.setStatus(429);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(rateLimit.getWindowSeconds()));
        String body = "{\"code\":429,\"msg\":\"请求过于频繁，请稍后再试\",\"data\":null}";
        response.getWriter().write(body);
    }

    @Override
    /** 获取订单 */
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
