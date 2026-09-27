package com.chua.starter.oauth.sso.support.security;

import com.chua.starter.oauth.sso.support.admin.RateLimitManager;
import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OAuth IP 级请求限流过滤器
 *
 * <p>限流链路: 全局令牌桶(RateLimitManager) → 按维度规则令牌桶(ip/account/interface,
 * 规则来自 Redis {@code koiRateLimit:rule:{dimension}:{target}}) → 原有 Redis 窗口计数。</p>
 *
 * @since 2025/01/01
 * @author CH
 */
@Slf4j
public class OauthRateLimitFilter implements Filter, Ordered {

    /**
     * Rate_限制_前缀
     */
    private static final String RATE_LIMIT_PREFIX = "oauth:security:rate:";

    /**
     * 规则 key 前缀
     */
    private static final String RULE_KEY_PREFIX = "koiRateLimit:rule:";

    /**
     * 安全性属性
     */
    private final OauthSecurityProperties securityProperties;
    /**
     * 字符串Redis模板
     */
    private final StringRedisTemplate stringRedisTemplate;
    /**
     * 全局限流管理器
     */
    private final RateLimitManager rateLimitManager;

    /**
     * 按规则缓存的令牌桶: ruleKey -> bucket
     */
    private final Map<String, RuleBucket> ruleBuckets = new ConcurrentHashMap<>();

    /**
     * 构造
     *
     * @param securityProperties 安全配置
     * @param stringRedisTemplate Redis 模板
     * @param rateLimitManager 全局限流管理器
     */
    public OauthRateLimitFilter(OauthSecurityProperties securityProperties,
                                StringRedisTemplate stringRedisTemplate,
                                RateLimitManager rateLimitManager) {
        this.securityProperties = securityProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.rateLimitManager = rateLimitManager;
    }

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

        // 1. 全局令牌桶
        if (!rateLimitManager.tryAcquire()) {
            log.warn("全局限流触发: ip={}, uri={}", clientIp, request.getRequestURI());
            writeTooManyRequests(response, securityProperties.getRateLimit());
            return;
        }

        // 2. 按维度规则令牌桶(ip / interface / account)
        if (!enforceRule("ip", clientIp, response)
                || !enforceRule("interface", request.getRequestURI(), response)
                || !enforceAccountRule(request, clientIp, response)) {
            return;
        }

        // 3. 原有 Redis 窗口计数
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
     * 校验账号维度规则(尝试从请求头取当前账号, 取不到则跳过)
     *
     * @param request   请求
     * @param clientIp  客户端IP
     * @param response  响应
     * @return true=放行, false=已拒绝
     */
    private boolean enforceAccountRule(HttpServletRequest request, String clientIp, HttpServletResponse response)
            throws IOException {
        String account = request.getHeader("X-Account");
        if (!StringUtils.hasText(account)) {
            account = request.getHeader("account");
        }
        if (!StringUtils.hasText(account)) {
            return true;
        }
        return enforceRule("account", account, response);
    }

    /**
     * 按单条规则做令牌桶检查
     *
     * @param dimension 维度
     * @param target    目标
     * @param response  响应
     * @return true=放行, false=已拒绝
     */
    private boolean enforceRule(String dimension, String target, HttpServletResponse response)
            throws IOException {
        String ruleKey = RULE_KEY_PREFIX + dimension + ":" + target;
        Integer qps;
        try {
            String val = stringRedisTemplate.opsForValue().get(ruleKey);
            if (!StringUtils.hasText(val)) {
                return true;
            }
            qps = Integer.parseInt(val.trim());
        } catch (Exception e) {
            // 规则读取失败不影响放行
            return true;
        }
        if (qps == null || qps <= 0) {
            return true;
        }
        RuleBucket bucket = ruleBuckets.compute(ruleKey, (k, old) -> {
            if (null != old && old.qps == qps) {
                return old;
            }
            return new RuleBucket(qps, RateLimiter.create(qps));
        });
        if (!bucket.limiter.tryAcquire()) {
            log.warn("规则限流触发: dim={}, target={}, qps={}", dimension, target, qps);
            writeTooManyRequests(response, securityProperties.getRateLimit());
            return false;
        }
        return true;
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
         * @param qps qps
         * @param limiter limiter
         */
        RuleBucket(int qps, RateLimiter limiter) {
            this.qps = qps;
            this.limiter = limiter;
        }
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
    /**
     * 获取订单
    */
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
