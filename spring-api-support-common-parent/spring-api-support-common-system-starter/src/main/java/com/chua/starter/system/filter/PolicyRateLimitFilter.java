// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.filter;

import com.chua.starter.system.support.ClientIpResolver;
import com.chua.starter.system.support.PolicyCacheService;
import com.chua.starter.system.support.PolicyHitCollector;
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

        String clientIp = ClientIpResolver.resolve(request);
        String account = resolveAccount(request);
        String interfaceTarget = stripContextPath(request);

        boolean whitelisted = false;
        try {
            // 1. 黑名单: IP / 账号
            String hitIpBlack = policyCacheService.matchIp(IP_TYPE_BLACK, clientIp);
            if (null != hitIpBlack) {
                // 归因到命中的那一行（可能是精确 IP，也可能是 CIDR），而不是客户端 IP
                PolicyHitCollector.getInstance().record(PolicyHitCollector.TYPE_IP_BLACK, hitIpBlack, clientIp);
                log.warn("IP 黑名单拦截: ip={}, entry={}, uri={}", clientIp, hitIpBlack, interfaceTarget);
                writeForbidden(response);
                return;
            }
            String hitAccountBlack = StringUtils.hasText(account)
                    ? policyCacheService.matchAccount(ACCOUNT_TYPE_BLACK, account)
                    : null;
            if (null != hitAccountBlack) {
                PolicyHitCollector.getInstance().record(PolicyHitCollector.TYPE_ACCOUNT_BLACK, hitAccountBlack, clientIp);
                log.warn("账号黑名单拦截: account={}, uri={}", account, interfaceTarget);
                writeForbidden(response);
                return;
            }

            // 2. 白名单: 存在则跳过限流（命中白名单也算策略生效，一并统计）
            String hitIpWhite = policyCacheService.matchIp(IP_TYPE_WHITE, clientIp);
            String hitAccountWhite = StringUtils.hasText(account)
                    ? policyCacheService.matchAccount(ACCOUNT_TYPE_WHITE, account)
                    : null;
            if (null != hitIpWhite) {
                PolicyHitCollector.getInstance().record(PolicyHitCollector.TYPE_IP_WHITE, hitIpWhite, clientIp);
            }
            if (null != hitAccountWhite) {
                PolicyHitCollector.getInstance().record(PolicyHitCollector.TYPE_ACCOUNT_WHITE, hitAccountWhite, clientIp);
            }
            whitelisted = null != hitIpWhite || null != hitAccountWhite;

            if (!whitelisted
                    && (!enforceRule("interface", interfaceTarget, clientIp, response)
                    || !enforceRule("ip", clientIp, clientIp, response)
                    || (StringUtils.hasText(account) && !enforceRule("account", account, clientIp, response)))) {
                return;
            }
        } catch (Exception e) {
            // 检查异常一律放行, 避免误伤
            log.warn("策略限流检查异常, 放行: ip={}, uri={}", clientIp, interfaceTarget, e);
        }

        if (!whitelisted && !systemRateLimitManager.tryAcquire()) {
            PolicyHitCollector.getInstance().record(PolicyHitCollector.TYPE_RATE_LIMIT_GLOBAL, "global", clientIp);
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
     * @param clientIp  来源 IP（仅用于命中统计归因）
     * @param response  响应
     * @return true=放行, false=已拒绝
     */
    private boolean enforceRule(String dimension, String target, String clientIp,
                                HttpServletResponse response) throws IOException {
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
            // 规则键与缓存一致（dimension:target），列表页据此归因到具体那一行
            PolicyHitCollector.getInstance().record(PolicyHitCollector.TYPE_RATE_LIMIT, ruleKey, clientIp);
            log.warn("规则限流触发: dim={}, target={}, qps={}", dimension, target, qps);
            writeTooManyRequests(response);
            return false;
        }
        return true;
    }

    /**
     * 是否需要过滤
     *
     * <p>放行两类路径：SSE 流端点与登录接口（否则长连接/登录风暴会被自己挡住）。</p>
     *
     * <p><b>已知未处理的自锁风险</b>：本方法不豁免策略管理面（{@code /v2/policy/**}）。
     * 把限流规则的 target 配成 {@code /v2/policy/hit/counts} 或
     * {@code /v2/policy/ratelimit/list} 之后，管理员会连命中统计都读不到、限流列表都打不开。
     * 修法有两种取法 ——「管理面整体豁免」还是「只豁免读与撤销操作」—— 属于策略安全边界
     * 的设计决策，需要单独立项评审后再改，<b>不要</b>在别的任务里顺手加一行放行判断。
     * 详见 backups/self-lock-bypass-rollback-20260928/MANIFEST.md。</p>
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