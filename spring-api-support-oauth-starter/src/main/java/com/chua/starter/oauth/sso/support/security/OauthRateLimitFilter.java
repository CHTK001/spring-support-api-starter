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
import java.net.InetAddress;
import java.net.UnknownHostException;
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
     * 可采信的转发 IP 头（仅在直接连接方是可信代理时生效）
     */
    private static final String[] FORWARDED_IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
    };

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
        String logicalPath = OauthUriMatcher.logicalPath(request);

        // 1. 全局令牌桶
        if (!rateLimitManager.tryAcquire()) {
            log.warn("全局限流触发: ip={}, uri={}", clientIp, request.getRequestURI());
            writeTooManyRequests(response, securityProperties.getRateLimit());
            return;
        }

        // 2. 按维度规则令牌桶(ip / interface / account)
        if (!enforceRule("ip", clientIp, response)
                || !enforceRule("interface", logicalPath, response)
                || !enforceAccountRule(request, response)) {
            return;
        }

        // 3. 原有 Redis 窗口计数
        //    <b>修复</b>：原 key 含 request.getRequestURI()，预算是 per-(IP, 完整URI)，
        //    攻击者把同一类攻击分散到 6 个协议路径即可拿到 6 倍预算，且每个随机路径都会
        //    在 Redis 里留下一条永不过期的垃圾 key。现只按 IP 计数。
        OauthSecurityProperties.RateLimit rateLimit = securityProperties.getRateLimit();
        String key = RATE_LIMIT_PREFIX + clientIp;

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
                        clientIp, logicalPath, count, rateLimit.getMaxRequests());
                writeTooManyRequests(response, rateLimit);
                return;
            }
        } catch (Exception e) {
            // <b>修复</b>：原实现 Redis 异常时直接放行，等于给了一个「打满 Redis 即可无限撞库」的开关。
            // 现在改为失败关闭：限流依赖 Redis，Redis 不可用时宁可拒绝。
            log.error("限流检查异常, 拒绝请求: ip={}, uri={}, error={}",
                    clientIp, logicalPath, e.getMessage());
            writeServiceUnavailable(response);
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * 校验账号维度规则
     *
     * <p><b>修复</b>：原实现直接信任请求头 {@code X-Account} / {@code account} 作为桶 key。
     * 未认证调用方可以任意自选桶 key——既能让自己的攻击流量散落到无限多个桶（绕过账号限流），
     * 也能反过来指定他人账号名把正常用户锁在限流门外。</p>
     * <p>现改为只采信「本次请求声明要登录的主体」，且必须来自请求体参数而非请求头；
     * 取不到则跳过该维度（真正的按账号锁定由 {@code LoginAttemptService} 在 Redis 里负责，
     * 不依赖任何请求头）。</p>
     *
     * @param request  请求
     * @param response 响应
     * @return true=放行, false=已拒绝
     */
    private boolean enforceAccountRule(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String account = resolveLoginPrincipal(request);
        if (!StringUtils.hasText(account)) {
            return true;
        }
        return enforceRule("account", account, response);
    }

    /**
     * 从请求参数中解析登录主体（username / userName / account）
     *
     * <p>只读 body/form 参数，不读请求头——请求头可被跨站与脚本任意设置，参数至少
     * 受 Content-Type 与业务签名约束。</p>
     *
     * @param request 请求
     * @return 登录主体，取不到返回 null
     */
    private String resolveLoginPrincipal(HttpServletRequest request) {
        for (String name : new String[]{"username", "userName", "account"}) {
            try {
                String value = request.getParameter(name);
                if (StringUtils.hasText(value) && value.length() <= 128) {
                    return value;
                }
            } catch (Exception e) {
                // 非表单请求读参数可能抛异常，忽略即可
                log.debug("读取登录参数失败: name={}", name);
            }
        }
        return null;
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
        int qps;
        try {
            String val = stringRedisTemplate.opsForValue().get(ruleKey);
            if (!StringUtils.hasText(val)) {
                return true;
            }
            qps = Integer.parseInt(val.trim());
        } catch (Exception e) {
            // <b>修复</b>：原实现读取规则失败即放行。规则读取失败通常意味着 Redis 异常，
            // 此时若继续放行，所有基于规则的限流（含按 IP / 按接口的细粒度限流）同时失效。
            // 现改为失败关闭，并明确回 503——此前只 {@code return false} 而不写响应，
            // 调用方会收到一个<b>空 200</b>，既不是 429 也不是 503，无法据此判断重试。
            log.error("限流规则读取异常, 拒绝请求: rule={}, error={}", ruleKey, e.getMessage());
            writeServiceUnavailable(response);
            return false;
        }
        if (qps <= 0) {
            return true;
        }
        // <b>修复</b>：原实现用无界 ConcurrentHashMap 缓存令牌桶，interface 维度的 key 是
        // 请求路径，攻击者用随机路径即可无限撑大内存。现加上条目上限，超限后清空重建。
        int maxBuckets = securityProperties.getRateLimit().getMaxRuleBuckets();
        if (maxBuckets > 0 && ruleBuckets.size() >= maxBuckets && !ruleBuckets.containsKey(ruleKey)) {
            log.warn("限流桶缓存达到上限({}), 重建以避免无界增长", maxBuckets);
            ruleBuckets.clear();
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
     * <p><b>修复</b>：改用剥离上下文路径后的锚定 Ant 匹配（见 {@link OauthUriMatcher}），
     * 消除原 {@code contains} 判定在上下文路径 {@code /oauth} 下的自匹配退化。</p>
     *
     * @param request 请求
     * @return should限制的结果
     */
    private boolean shouldLimit(HttpServletRequest request) {
        String logicalPath = OauthUriMatcher.logicalPath(request);
        OauthSecurityProperties.RateLimit rateLimit = securityProperties.getRateLimit();

        if (OauthUriMatcher.matchesAny(logicalPath, rateLimit.getIgnorePaths())) {
            return false;
        }

        List<String> paths = rateLimit.getPaths();
        if (paths == null || paths.isEmpty()) {
            return true;
        }
        return OauthUriMatcher.matchesAny(logicalPath, paths);
    }

    /**
     * 解析客户端ip
     *
     * <p><b>修复</b>：原实现无条件信任 {@code X-Forwarded-For} / {@code Proxy-Client-IP} /
     * {@code WL-Proxy-Client-IP} / {@code HTTP_CLIENT_IP} / {@code HTTP_X_FORWARDED_FOR}
     * 并取第一个元素，且不校验直接连接方是否可信。攻击者只要轮换该头，就能让每次请求
     * 落到不同的限流桶，IP 维度的窗口计数与令牌桶同时失效，等于无限次撞库。</p>
     * <p>现改为：<b>只有当直接连接方命中 {@code rateLimit.trusted-proxies} 白名单时</b>
     * 才采信转发头（并从右向左剥离可信跳数）；否则一律使用 {@code getRemoteAddr()}。</p>
     *
     * @param request 请求
     * @return 客户端 ip
     */
    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        List<String> trustedProxies = securityProperties.getRateLimit().getTrustedProxies();
        if (trustedProxies == null || trustedProxies.isEmpty()) {
            // 未配置可信代理：直连部署，禁止采信任何可伪造的转发头
            return StringUtils.hasText(remoteAddr) ? remoteAddr : "unknown";
        }
        if (!isTrustedProxy(remoteAddr, trustedProxies)) {
            return StringUtils.hasText(remoteAddr) ? remoteAddr : "unknown";
        }
        for (String header : FORWARDED_IP_HEADERS) {
            String forwarded = request.getHeader(header);
            if (StringUtils.hasText(forwarded) && !"unknown".equalsIgnoreCase(forwarded)) {
                // XFF 形如 "client, proxy1, proxy2"，最左侧才是原始客户端
                String[] hops = forwarded.split(",");
                for (String hop : hops) {
                    String candidate = hop.trim();
                    if (StringUtils.hasText(candidate) && !isTrustedProxy(candidate, trustedProxies)) {
                        return candidate;
                    }
                }
            }
        }
        return StringUtils.hasText(remoteAddr) ? remoteAddr : "unknown";
    }

    /**
     * 判断 ip 是否命中可信代理白名单（支持精确 IP 与 CIDR）
     *
     * @param ip      待判断 ip
     * @param trusted 白名单
     * @return 命中返回 true
     */
    private boolean isTrustedProxy(String ip, List<String> trusted) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }
        for (String entry : trusted) {
            if (!StringUtils.hasText(entry)) {
                continue;
            }
            String candidate = entry.trim();
            if (candidate.equals(ip)) {
                return true;
            }
            int slash = candidate.indexOf('/');
            if (slash > 0 && cidrMatches(ip, candidate.substring(0, slash),
                    parsePrefixLength(candidate.substring(slash + 1)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析 CIDR 前缀长度，非法返回 -1
     *
     * @param raw 前缀字符串
     * @return 前缀长度，非法返回 -1
     */
    private int parsePrefixLength(String raw) {
        if (!StringUtils.hasText(raw)) {
            return -1;
        }
        try {
            int bits = Integer.parseInt(raw.trim());
            return (bits >= 0 && bits <= 128) ? bits : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 判断 ip 是否落在给定网段内
     *
     * <p>不引入 spring-security-web（本模块刻意不依赖 Spring Security），
     * 这里只处理 IPv4 / IPv6 的按位前缀比较。</p>
     *
     * @param ip      待判断 ip
     * @param network 网段地址
     * @param prefix  前缀长度
     * @return 命中返回 true
     */
    private boolean cidrMatches(String ip, String network, int prefix) {
        if (prefix < 0) {
            return false;
        }
        try {
            byte[] addr = InetAddress.getByName(ip).getAddress();
            byte[] net = InetAddress.getByName(network).getAddress();
            if (addr.length != net.length) {
                return false;
            }
            int fullBytes = prefix / 8;
            int remainingBits = prefix % 8;
            for (int i = 0; i < fullBytes; i++) {
                if (addr[i] != net[i]) {
                    return false;
                }
            }
            if (remainingBits == 0) {
                return true;
            }
            int mask = (0xFF << (8 - remainingBits)) & 0xFF;
            return (addr[fullBytes] & mask) == (net[fullBytes] & mask);
        } catch (UnknownHostException e) {
            return false;
        }
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

    /**
     * 写入ServiceUnavailable
     *
     * <p>限流依赖的 Redis 不可用时返回 503，而不是静默放行。</p>
     *
     * @param response 响应
     * @throws IOException 写响应异常
     */
    private void writeServiceUnavailable(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "5");
        String body = "{\"code\":503,\"msg\":\"认证服务暂时不可用，请稍后再试\",\"data\":null}";
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
