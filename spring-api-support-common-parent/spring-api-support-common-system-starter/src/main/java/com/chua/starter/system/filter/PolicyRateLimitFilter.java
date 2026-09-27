package com.chua.starter.system.filter;

import com.chua.starter.system.support.PolicyCacheService;
import com.chua.starter.system.support.SystemRateLimitManager;
import com.google.common.util.concurrent.RateLimiter;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 策略限流/黑白名单执行过滤器。
 *
 * <p>在 AuthFilter 之后执行(可拿到登录账号), 对 {@code /v2/**} 请求:
 * <ol>
 *   <li>IP/账号黑名单 → 403</li>
 *   <li>IP/账号白名单存在 → 跳过限流检查</li>
 *   <li>按维度(interface/ip/account)读取 Spring Cache 中的限流规则
 *       (key = {@code dimension:target}, value = qps),
 *       用 Guava 令牌桶限流, 任一维度超限 → 429</li>
 * </ol>
 *
 * @since 4.0.0.52
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyRateLimitFilter implements Filter, Ordered {

    /**
     * 限流规则 key 前缀
     */
    private static final int IP_TYPE_WHITE = 0;
    private static final int IP_TYPE_BLACK = 1;
    private static final int ACCOUNT_TYPE_WHITE = 2;
    private static final int ACCOUNT_TYPE_BLACK = 1;

    /**
     * 规则令牌桶缓存: ruleKey -> bucket
     */
    private final Map<String, RuleBucket> buckets = new ConcurrentHashMap<>();

    /**
     * 策略运行时缓存（Spring Cache）
     */
    private final PolicyCacheService policyCacheService;

    private final SystemRateLimitManager systemRateLimitManager;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (!shouldFilter(request)) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        String account = resolveAccount(request);
        String interfaceTarget = stripContextPath(request);

        boolean whitelisted = false;
        try {
            // 1. 黑名单: IP / 账号
            if (policyCacheService.matchesIp(IP_TYPE_BLACK, clientIp)) {
                log.warn("IP 黑名单拦截: ip={}, uri={}", clientIp, interfaceTarget);
                writeForbidden(response);
                return;
            }
            if (StringUtils.hasText(account) && policyCacheService.matchesAccount(ACCOUNT_TYPE_BLACK, account)) {
                log.warn("账号黑名单拦截: account={}, uri={}", account, interfaceTarget);
                writeForbidden(response);
                return;
            }

            // 2. 白名单: 存在则跳过限流
            whitelisted = policyCacheService.matchesIp(IP_TYPE_WHITE, clientIp)
                    || (StringUtils.hasText(account) && policyCacheService.matchesAccount(ACCOUNT_TYPE_WHITE, account));

            if (!whitelisted
                    && (!enforceRule("interface", interfaceTarget, response)
                    || !enforceRule("ip", clientIp, response)
                    || (StringUtils.hasText(account) && !enforceRule("account", account, response)))) {
                return;
            }
        } catch (Exception e) {
            // 检查异常一律放行, 避免误伤
            log.warn("策略限流检查异常, 放行: ip={}, uri={}", clientIp, interfaceTarget, e);
        }

        if (!whitelisted && !systemRateLimitManager.tryAcquire()) {
            log.warn("全局限流触发: ip={}, account={}, uri={}", clientIp, account, interfaceTarget);
            writeTooManyRequests(response);
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * 按单条规则做令牌桶检查
     *
     * @param dimension 维度
     * @param target    目标
     * @param response  响应
     * @return true=放行, false=已拒绝
     */
    private boolean enforceRule(String dimension, String target, HttpServletResponse response) throws IOException {
        if (!StringUtils.hasText(target)) {
            return true;
        }
        String ruleKey = dimension + ":" + target;
        int qps;
        try {
            String val = policyCacheService.getRateLimit(dimension, target);
            if (!StringUtils.hasText(val)) {
                return true;
            }
            qps = Integer.parseInt(val.trim());
        } catch (Exception e) {
            return true;
        }
        if (qps <= 0) {
            return true;
        }
        RuleBucket bucket = buckets.compute(ruleKey, (k, old) -> {
            if (null != old && old.qps == qps) {
                return old;
            }
            return new RuleBucket(qps, RateLimiter.create(qps));
        });
        if (!bucket.limiter.tryAcquire()) {
            log.warn("规则限流触发: dim={}, target={}, qps={}", dimension, target, qps);
            writeTooManyRequests(response);
            return false;
        }
        return true;
    }

    /**
     * 是否需要过滤
     *
     * @param request 请求
     * @return true=需要过滤
     */
    private boolean shouldFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (StringUtils.hasText(ctx) && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        if (!StringUtils.hasText(uri) || !uri.startsWith("/v2/")) {
            return false;
        }
        // SSE 流端点不限流
        if (uri.endsWith("/stream")) {
            return false;
        }
        // 登录接口不限流
        if (uri.endsWith("/login")) {
            return false;
        }
        return true;
    }

    /**
     * 去掉 context-path 的接口目标
     *
     * @param request 请求
     * @return 接口路径
     */
    private String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (StringUtils.hasText(ctx) && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        return uri;
    }

    /**
     * 从请求头取当前登录账号(网关/认证过滤器写入)
     *
     * @param request 请求
     * @return 账号, 可能为空
     */
    private String resolveAccount(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        if (null != username && StringUtils.hasText(String.valueOf(username))) {
            return String.valueOf(username);
        }
        Principal principal = request.getUserPrincipal();
        if (null != principal && StringUtils.hasText(principal.getName())) {
            return principal.getName();
        }
        Object accountAttribute = request.getAttribute("account");
        if (null != accountAttribute && StringUtils.hasText(String.valueOf(accountAttribute))) {
            return String.valueOf(accountAttribute);
        }
        String account = request.getHeader("X-Account");
        if (!StringUtils.hasText(account)) {
            account = request.getHeader("account");
        }
        return account;
    }

    /**
     * 解析客户端 IP
     *
     * @param request 请求
     * @return ip
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
            ip = request.getRemoteAddr();
        }
        if (null != ip && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip == null ? "unknown" : ip;
    }

    /**
     * 写 429
     *
     * @param response 响应
     */
    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":\"429\",\"msg\":\"请求过于频繁，请稍后重试\",\"ok\":false}");
    }

    /**
     * 写 403
     *
     * @param response 响应
     */
    private void writeForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(403);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":\"403\",\"msg\":\"访问被拒绝\",\"ok\":false}");
    }

    @Override
    public int getOrder() {
        // 在 AuthFilter 之后
        return Ordered.LOWEST_PRECEDENCE - 1;
    }

    /**
     * 规则令牌桶
     */
    private static final class RuleBucket {
        /**
         * qps
         */
        final int qps;
        /**
         * limiter
         */
        final RateLimiter limiter;

        /**
         * 构造
         *
         * @param qps      qps
         * @param limiter  limiter
         */
        RuleBucket(int qps, RateLimiter limiter) {
            this.qps = qps;
            this.limiter = limiter;
        }
    }
}
