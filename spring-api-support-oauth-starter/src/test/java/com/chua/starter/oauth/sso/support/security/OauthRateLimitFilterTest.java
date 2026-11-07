// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.oauth.sso.support.security;

import com.chua.starter.oauth.sso.support.admin.RateLimitManager;
import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import jakarta.servlet.FilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OauthRateLimitFilter} 回归测试
 *
 * <p>守护 P1-5：① 限流范围判定此前用 {@code contains}，默认 paths 里的裸上下文路径
 * {@code /oauth} 让判定退化；② 无条件信任 {@code X-Forwarded-For} 等 5 个可伪造头，
 * 攻击者轮换该头即可让每个请求落到不同的限流桶；③ Redis 异常时放行；
 * ④ 窗口计数 key 含完整 URI，同类攻击分散到多个协议路径即可拿到 N 倍预算。</p>
 *
 * <p>所有对 {@code ValueOperations} 的模拟都改为一个可观测的 RealRedis 桩：调用序列、
 * 回填值、异常行为都由测试显式装配，而不是 mockito 的 "any → 返回一个值"。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("OauthRateLimitFilter: IP 限流范围与客户端 IP 解析")
class OauthRateLimitFilterTest {

    /**
     * 上下文路径
     */
    private static final String CONTEXT = "/oauth";

    /**
     * 安全配置
     */
    private OauthSecurityProperties properties;

    /**
     * 可观测的 Redis 桩：逐操作录制，计数、查询、过期、删除全部可断言。
     */
    private static class RecordingStringRedisTemplate extends StringRedisTemplate {

        /** 按键名录制的 String 型值 */
        private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();

        /** 记录收到过的操作 */
        private final List<String> recorded = new ArrayList<>();

        /** 未命中键时返回的值（null 表示 Redis 空） */
        private String defaultValue = null;

        /** 回填器：key 的 "无值" 结果向上应用一层规则 */
        private final List<BiFunction<String, String, String>> fallbacks = new ArrayList<>();

        /** 不再执行有参调用的 Redis 异常开关（字面查询） */
        private boolean throwOnGet = false;

        /** 不再执行有参调用的 Redis 异常开关（增量计数） */
        private boolean throwOnIncrement = false;

        /**
         * 记录键值对，暴露给断言使用。
         *
         * @return this
         */
        RecordingStringRedisTemplate store(String key, String value) {
            store.put(key, value);
            recorded.add("set " + key + " -> " + value);
            return this;
        }

        /**
         * 给 key 升一升档：每次未命中时尝试回填（首个匹配的重合规则优先）。
         *
         * @return this
         */
        RecordingStringRedisTemplate defaultBy(Predicate<String> keyMatcher, String value) {
            fallbacks.add((key, resolved) -> keyMatcher.test(key) ? value : resolved);
            return this;
        }

        List<String> getRecorded() {
            return recorded;
        }

        String getStoredValue(String key) {
            return store.get(key);
        }

        /** 关闭字面查询（让 Redis 故障场景可测） */
        void setThrowOnGet(boolean throwOnGet) {
            this.throwOnGet = throwOnGet;
        }

        /** 关闭增量计数（让 Redis 故障场景可测） */
        void setThrowOnIncrement(boolean throwOnIncrement) {
            this.throwOnIncrement = throwOnIncrement;
        }

        @Override
        public ValueOperations<String, String> opsForValue() {
            return (ValueOperations<String, String>) Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class<?>[]{ValueOperations.class},
                    new RecordingHandler());
        }

        /**
         * 逐操作录制的 InvocationHandler
         */
        private class RecordingHandler implements InvocationHandler {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("get".equals(method.getName()) && args.length == 1) {
                    if (throwOnGet) {
                        throw new RuntimeException("redis down");
                    }
                    String key = String.valueOf(args[0]);
                    // 先取真实 store，unset 时再问回填规则（后者只在 defaultBy 注册时生效）
                    String value = store.get(key);
                    if (value == null) {
                        value = resolveFallbacks(key);
                    }
                    if (value == null) {
                        value = defaultValue;
                    }
                    recorded.add("get " + key + " -> " + value);
                    return value;
                }
                if ("increment".equals(method.getName()) && args.length == 1) {
                    if (throwOnIncrement) {
                        throw new RuntimeException("redis down");
                    }
                    String key = String.valueOf(args[0]);
                    String current = store.containsKey(key) ? store.get(key) : "0";
                    long next = Long.parseLong(current) + 1L;
                    store.put(key, String.valueOf(next));
                    recorded.add("increment " + key + " -> " + next);
                    return next;
                }
                if ("set".equals(method.getName()) && args.length >= 2) {
                    recorded.add("set " + String.valueOf(args[0]) + " -> " + String.valueOf(args[1]));
                    return null;
                }
                if ("delete".equals(method.getName()) && args.length == 1) {
                    String key = String.valueOf(args[0]);
                    recorded.add("delete " + key);
                    return store.remove(key) != null;
                }
                recorded.add(method.getName() + " " + String.join(",", String.valueOf(args)));
                return null;
            }

            /** resolveFallbacks 方法 */
            private String resolveFallbacks(String key) {
                if (!fallbacks.isEmpty()) {
                    for (BiFunction<String, String, String> f : fallbacks) {
                        try {
                            String resolved = f.apply(key, null);
                            if (resolved != null) {
                                return resolved;
                            }
                        } catch (RuntimeException e) {
                            return null;
                        }
                    }
                }
                return null;
            }
        }

        @Override
        public Boolean expire(String key, Duration timeout) {
            recorded.add("expire " + key + " ttl=" + timeout);
            return Boolean.TRUE;
        }

        @Override
        public Boolean expire(String key, long timeout, TimeUnit unit) {
            recorded.add("expire " + key + " ttl=" + timeout + " " + unit);
            return Boolean.TRUE;
        }

        @Override
        public Boolean hasKey(String key) {
            recorded.add("hasKey " + key + " -> " + store.containsKey(key));
            return store.containsKey(key);
        }

        @Override
        public Long getExpire(String key) {
            recorded.add("getExpire " + key);
            return 1L;
        }

        @Override
        public Boolean delete(String key) {
            recorded.add("delete " + key);
            return store.remove(key) != null;
        }

        private String fallback(String resolved) {
            return resolved;
        }
    }

    private RecordingStringRedisTemplate redis;

    /**
     * 记录是否被放行
     */
    private static final class RecordingChain implements FilterChain {
        private boolean invoked;

        @Override
        public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse resp) {
            invoked = true;
        }
    }

    @BeforeEach
    void setUp() {
        properties = new OauthSecurityProperties();
        properties.getRateLimit().setMaxRequests(100);
        properties.getRateLimit().setWindowSeconds(60);
        redis = new RecordingStringRedisTemplate();
        filter = new OauthRateLimitFilter(properties, redis, new RateLimitManager());
    }

    /**
     * 被测过滤器（全测试共享同一实例，以便断言桶缓存大小）
     */
    private OauthRateLimitFilter filter;

    /**
     * 构造请求
     *
     * @param method    方法
     * @param path      逻辑路径
     * @param remoteAddr 直接连接方
     * @return 请求
     */
    private MockHttpServletRequest request(String method, String path, String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, CONTEXT + path);
        request.setRequestURI(CONTEXT + path);
        request.setContextPath(CONTEXT);
        request.setRemoteAddr(remoteAddr);
        return request;
    }

    /**
     * 执行一次过滤
     *
     * @param request 请求
     * @return 结果
     */
    private FilterOutcome run(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingChain chain = new RecordingChain();
        filter.doFilter(request, response, chain);
        return new FilterOutcome(chain.invoked, response);
    }

    /**
     * 过滤结果
     *
     * @param passed   是否放行
     * @param response 响应
     */
    private record FilterOutcome(boolean passed, MockHttpServletResponse response) {
    }

    /**
     * 录制的增量 key 清单
     *
     * @return 增量的 key 列表
     */
    private List<String> recordedIncrementKeys() {
        List<String> result = new ArrayList<>();
        for (String r : redis.getRecorded()) {
            if (r.startsWith("increment ")) {
                int end = r.indexOf(" -> ");
                result.add(end > 0 ? r.substring("increment ".length(), end) : r.substring("increment ".length()));
            }
        }
        return result;
    }

    /**
     * 录制的字面查询 key 清单
     *
     * @return 查询的 key 列表
     */
    private List<String> recordedGetKeys() {
        List<String> result = new ArrayList<>();
        for (String r : redis.getRecorded()) {
            if (r.startsWith("get ")) {
                int end = r.indexOf(" -> ");
                result.add(end > 0 ? r.substring("get ".length(), end) : r.substring("get ".length()));
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // 限流范围判定
    // ------------------------------------------------------------------

    @Test
    @DisplayName("P1 核心反例：paths 里出现裸上下文路径 /oauth 也不得让全站进入限流")
    void bareContextPathInPathsMustNotLimitEverything() throws Exception {
        // 修复前的默认值
        properties.getRateLimit().setPaths(List.of("/oauth"));

        run(request("POST", "/user/info", "10.0.0.5"));

        assertTrue(redis.getRecorded().stream().noneMatch(r -> r.startsWith("increment ")),
                "不在限流逻辑路径内的请求不得计数: " + redis.getRecorded());
    }

    @Test
    @DisplayName("逻辑路径命中 paths 时进入限流")
    void logicalPathHitEntersRateLimit() throws Exception {
        assertTrue(run(request("POST", "/login", "10.0.0.5")).passed());
        assertTrue(redis.getRecorded().contains("increment oauth:security:rate:10.0.0.5 -> 1"),
                "预期记录: " + redis.getRecorded());
    }

    @Test
    @DisplayName("ignorePaths 豁免生效")
    void ignorePathsAreExempt() throws Exception {
        properties.getRateLimit().setIgnorePaths(List.of("/login"));

        assertTrue(run(request("POST", "/login", "10.0.0.5")).passed());
        assertTrue(redis.getRecorded().stream().noneMatch(r -> r.startsWith("increment ")),
                "豁免路径不得计数");
    }

    @Test
    @DisplayName("paths 为空表示全部限流")
    void emptyPathsMeansLimitAll() throws Exception {
        properties.getRateLimit().setPaths(List.of());

        assertTrue(run(request("POST", "/anything", "10.0.0.5")).passed());
        assertTrue(redis.getRecorded().contains("increment oauth:security:rate:10.0.0.5 -> 1"),
                "空 paths 等价全量");
    }

    // ------------------------------------------------------------------
    // 客户端 IP 解析：转发头不可无条件采信
    // ------------------------------------------------------------------

    @Test
    @DisplayName("未配置可信代理时 X-Forwarded-For 被忽略（防轮换绕过限流）")
    void forwardedHeaderIgnoredWithoutTrustedProxies() throws Exception {
        MockHttpServletRequest a = request("POST", "/login", "10.0.0.5");
        a.addHeader("X-Forwarded-For", "1.1.1.1");
        MockHttpServletRequest b = request("POST", "/login", "10.0.0.5");
        b.addHeader("X-Forwarded-For", "2.2.2.2");

        run(a);
        run(b);

        assertTrue(recordedIncrementKeys().stream()
                        .allMatch("oauth:security:rate:10.0.0.5"::equals),
                "伪造的转发头不得改变限流桶: " + recordedIncrementKeys());
    }

    @Test
    @DisplayName("五个可伪造头在未配置可信代理时全部被忽略")
    void allSpoofableHeadersIgnoredWithoutTrustedProxies() throws Exception {
        String[] spoofable = {
                "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR",
        };
        for (String header : spoofable) {
            setUp();
            MockHttpServletRequest req = request("POST", "/login", "10.0.0.5");
            req.addHeader(header, "9.9.9.9");
            run(req);
            assertTrue(redis.getRecorded().contains("increment oauth:security:rate:10.0.0.5 -> 1"),
                    "同一个桶: " + header + " " + recordedIncrementKeys());
        }
    }

    @Test
    @DisplayName("直接连接方命中可信代理白名单时才采信转发头")
    void forwardedHeaderHonoredOnlyFromTrustedProxy() throws Exception {
        properties.getRateLimit().setTrustedProxies(List.of("10.0.0.0/8"));

        MockHttpServletRequest req = request("POST", "/login", "10.0.0.5");
        req.addHeader("X-Forwarded-For", "203.0.113.9");

        run(req);

        assertTrue(redis.getRecorded().contains("increment oauth:security:rate:203.0.113.9 -> 1"),
                "可信代理的转发头取代直连地址: " + recordedIncrementKeys());
    }

    @Test
    @DisplayName("直接连接方不在白名单时转发头被忽略")
    void forwardedHeaderIgnoredFromUntrustedRemote() throws Exception {
        properties.getRateLimit().setTrustedProxies(List.of("10.0.0.0/8"));

        MockHttpServletRequest req = request("POST", "/login", "192.168.9.9");
        req.addHeader("X-Forwarded-For", "203.0.113.9");

        run(req);

        assertTrue(redis.getRecorded().contains("increment oauth:security:rate:192.168.9.9 -> 1"),
                "直连地址不可信时不采用转发头: " + recordedIncrementKeys());
    }

    @Test
    @DisplayName("XFF 多跳时取最左侧的第一个非可信地址")
    void xffTakesLeftmostUntrustedHop() throws Exception {
        properties.getRateLimit().setTrustedProxies(List.of("10.0.0.0/8"));

        MockHttpServletRequest req = request("POST", "/login", "10.0.0.5");
        // 攻击者自己伪造的左侧前缀 + 真实客户端
        req.addHeader("X-Forwarded-For", "10.0.0.7, 203.0.113.9, 10.0.0.5");

        run(req);

        // 10.0.0.7 在可信段内 → 跳过；203.0.113.9 非可信 → 采用
        assertTrue(redis.getRecorded().contains("increment oauth:security:rate:203.0.113.9 -> 1"),
                "多跳 XFF 取最左侧非可信地址: " + recordedIncrementKeys());
    }

    @Test
    @DisplayName("XFF 全部为可信地址时回退到 remoteAddr")
    void xffAllTrustedFallsBackToRemoteAddr() throws Exception {
        properties.getRateLimit().setTrustedProxies(List.of("10.0.0.0/8"));

        MockHttpServletRequest req = request("POST", "/login", "10.0.0.5");
        req.addHeader("X-Forwarded-For", "10.0.0.7, 10.0.0.5");

        run(req);

        assertTrue(redis.getRecorded().contains("increment oauth:security:rate:10.0.0.5 -> 1"),
                "全部可信回退到直连地址: " + recordedIncrementKeys());
    }

    @Test
    @DisplayName("XFF 占位值 unknown 被忽略")
    void xffPlaceholderUnknownIgnored() throws Exception {
        properties.getRateLimit().setTrustedProxies(List.of("10.0.0.0/8"));

        MockHttpServletRequest req = request("POST", "/login", "10.0.0.5");
        req.addHeader("X-Forwarded-For", "unknown");

        run(req);

        assertTrue(redis.getRecorded().contains("increment oauth:security:rate:10.0.0.5 -> 1"),
                "unknown 占位值像没写一样被忽略: " + recordedIncrementKeys());
    }

    @Test
    @DisplayName("CIDR 白名单按位匹配，不做前缀字符串匹配")
    void cidrMatchingIsBitwise() throws Exception {
        properties.getRateLimit().setTrustedProxies(List.of("10.0.0.0/8"));

        MockHttpServletRequest hit = request("POST", "/login", "10.255.255.254");
        MockHttpServletRequest miss = request("POST", "/login", "11.0.0.1");

        assertTrue(run(hit).passed());
        assertTrue(run(miss).passed());
        List<String> keys = recordedIncrementKeys();
        assertTrue(keys.contains("oauth:security:rate:10.255.255.254"), "CIDR 前缀命中" + keys);
        assertTrue(keys.contains("oauth:security:rate:11.0.0.1"), "CIDR 前缀不生效" + keys);
    }

    @Test
    @DisplayName("非法 CIDR 前缀长度不会误判为命中")
    void malformedCidrIsNotTrusted() throws Exception {
        properties.getRateLimit().setTrustedProxies(List.of("10.0.0.0/999", "not-an-ip/24"));

        MockHttpServletRequest req = request("POST", "/login", "10.0.0.5");
        req.addHeader("X-Forwarded-For", "203.0.113.9");

        run(req);

        assertTrue(redis.getRecorded().contains("increment oauth:security:rate:10.0.0.5 -> 1"),
                "非法 CIDR 前缀长度不生效: " + recordedIncrementKeys());
    }

    @Test
    @DisplayName("IPv6 可信代理同样生效")
    void ipv6TrustedProxyWorks() throws Exception {
        properties.getRateLimit().setTrustedProxies(List.of("2001:db8::/32"));

        MockHttpServletRequest req = request("POST", "/login", "2001:db8::1");
        // 客户端地址刻意选在可信网段之外，否则会被当作另一跳代理继续剥离
        req.addHeader("X-Forwarded-For", "203.0.113.9");

        run(req);

        assertTrue(redis.getRecorded().contains("increment oauth:security:rate:203.0.113.9 -> 1"),
                "IPv6 可信代理同样生效: " + recordedIncrementKeys());
    }

    @Test
    @DisplayName("IPv4 与 IPv6 网段不互相命中（地址族长度不同即不匹配）")
    void ipv4AndIpv6DoNotCrossMatch() throws Exception {
        properties.getRateLimit().setTrustedProxies(List.of("2001:db8::/32"));

        MockHttpServletRequest req = request("POST", "/login", "10.0.0.5");
        req.addHeader("X-Forwarded-For", "203.0.113.9");

        assertTrue(run(req).passed());
        // IPv6 白名单不得让 IPv4 的直接连接方变成可信代理
        assertTrue(redis.getRecorded().contains("increment oauth:security:rate:10.0.0.5 -> 1"),
                "IPv4 直连地址与 IPv6 白名单不互: " + recordedIncrementKeys());
    }

    // ------------------------------------------------------------------
    // 窗口计数与失败关闭
    // ------------------------------------------------------------------

    @Test
    @DisplayName("窗口计数 key 只含 IP，不含 URI（同 IP 多路径共享一个预算）")
    void windowKeyIsIpOnly() throws Exception {
        run(request("POST", "/login", "10.0.0.5"));
        run(request("POST", "/temporary-token", "10.0.0.5"));
        run(request("POST", "/verify", "10.0.0.5"));

        List<String> keys = recordedIncrementKeys();
        assertTrue(keys.stream().allMatch("oauth:security:rate:10.0.0.5"::equals),
                "同一 IP 的不同路径必须共享同一限流桶: " + keys);
        assertEquals(3, keys.size(), "三次窗口计数都必须落在同一 key");
    }

    @Test
    @DisplayName("超过 maxRequests 返回 429 并带 Retry-After")
    void exceedingBudgetReturns429() throws Exception {
        properties.getRateLimit().setMaxRequests(2);
        properties.getRateLimit().setWindowSeconds(30);

        run(request("POST", "/login", "10.0.0.5"));
        run(request("POST", "/login", "10.0.0.5"));
        FilterOutcome third = run(request("POST", "/login", "10.0.0.5"));

        assertFalse(third.passed());
        assertEquals(429, third.response().getStatus());
        assertEquals("30", third.response().getHeader("Retry-After"));
    }

    @Test
    @DisplayName("首次计数即设置窗口过期时间")
    void firstIncrementSetsExpiry() throws Exception {
        run(request("POST", "/login", "10.0.0.5"));

        List<String> expiries = redis.getRecorded().stream().filter(r -> r.startsWith("expire "))
                .toList();
        assertTrue(expiries.contains("expire oauth:security:rate:10.0.0.5 ttl=PT1M"),
                "首次计数必须设置窗口过期时间: " + expiries);
    }

    @Test
    @DisplayName("Redis 计数异常时 fail-closed 返回 503")
    void redisFailureFailsClosedWith503() throws Exception {
        redis.setThrowOnIncrement(true);

        FilterOutcome outcome = run(request("POST", "/login", "10.0.0.5"));

        assertFalse(outcome.passed(), "Redis 不可用时不得放行（否则等于送一个撞库开关）");
        assertEquals(503, outcome.response().getStatus());
        assertFalse(outcome.response().getContentAsString().isEmpty(),
                "503 必须有响应体，客户端才能识别重试");
    }

    @Test
    @DisplayName("限流规则读取异常时 fail-closed 返回 503 且不返回空 200")
    void ruleReadFailureFailsClosedWith503() throws Exception {
        redis.setThrowOnGet(true);

        FilterOutcome outcome = run(request("POST", "/login", "10.0.0.5"));

        assertFalse(outcome.passed());
        assertEquals(503, outcome.response().getStatus());
    }

    // ------------------------------------------------------------------
    // 账号维度：不得采信请求头
    // ------------------------------------------------------------------

    @Test
    @DisplayName("X-Account 请求头不得影响账号限流桶")
    void accountHeaderIsNotTrusted() throws Exception {
        MockHttpServletRequest req = request("POST", "/login", "10.0.0.5");
        req.addHeader("X-Account", "victim");

        run(req);

        assertTrue(redis.getRecorded().stream().noneMatch(r ->
                        r.contains("koiRateLimit:rule:account:victim")),
                "X-Account 头不得参与账号限流: " + recordedGetKeys());
    }

    @Test
    @DisplayName("account 请求参数参与账号限流（参数可用，但头不可用）")
    void accountQueryParamIsUsed() throws Exception {
        MockHttpServletRequest req = request("POST", "/login", "10.0.0.5");
        req.addParameter("account", "victim");

        run(req);

        assertTrue(recordedGetKeys().contains("koiRateLimit:rule:account:victim"),
                "参数驱动的账号维度查询: " + recordedGetKeys());
    }

    @Test
    @DisplayName("username 请求参数命中账号维度规则")
    void usernameParameterIsUsedForAccountRule() throws Exception {
        MockHttpServletRequest req = request("POST", "/login", "10.0.0.5");
        req.addParameter("username", "alice");
        // 只有 ip 维度无规则，username 维度有规则
        redis.store("koiRateLimit:rule:account:alice", "5");

        run(req);

        assertTrue(redis.getRecorded().contains("get koiRateLimit:rule:account:alice -> 5"),
                "username 查询命中参数维度的限流: " + redis.getRecorded());
    }

    @Test
    @DisplayName("超长 username 不参与账号限流（防 key 撑爆）")
    void overlyLongUsernameIsIgnored() throws Exception {
        MockHttpServletRequest req = request("POST", "/login", "10.0.0.5");
        req.addParameter("username", "u".repeat(200));

        run(req);

        assertTrue(redis.getRecorded().stream().noneMatch(r ->
                        r.startsWith("get koiRateLimit:rule:account:")),
                "超长 username 不参与账号限流: " + recordedGetKeys());
    }

    // ------------------------------------------------------------------
    // 规则令牌桶
    // ------------------------------------------------------------------

    @Test
    @DisplayName("按 IP 维度规则存在时超 QPS 返回 429")
    void ipRuleQpsExceededReturns429() throws Exception {
        redis.store("koiRateLimit:rule:ip:10.0.0.5", "1");

        // RateLimiter.create(1) 允许首个令牌，第二次触发
        assertTrue(run(request("POST", "/login", "10.0.0.5")).passed());
        FilterOutcome second = run(request("POST", "/login", "10.0.0.5"));

        assertFalse(second.passed());
        assertEquals(429, second.response().getStatus());
    }

    @Test
    @DisplayName("规则 qps<=0 视为不限流")
    void nonPositiveRuleQpsMeansUnlimited() throws Exception {
        redis.store("koiRateLimit:rule:ip:10.0.0.5", "0");

        for (int i = 0; i < 20; i++) {
            assertTrue(run(request("POST", "/login", "10.0.0.5")).passed());
        }
    }

    @Test
    @DisplayName("interface 维度按逻辑路径而非完整 URI 建桶")
    void interfaceRuleUsesLogicalPath() throws Exception {
        redis.store("koiRateLimit:rule:interface:/login", "100");

        run(request("POST", "/login", "10.0.0.5"));

        assertTrue(redis.getRecorded().contains("get koiRateLimit:rule:interface:/login -> 100"),
                "逻辑路径是 interface 维度建桶的 key");
        assertTrue(recordedGetKeys().stream().noneMatch("koiRateLimit:rule:interface:/oauth/login"::startsWith),
                "不拼接上下文路径作为 key");
    }

    @Test
    @DisplayName("限流桶缓存有上限：随机路径不得撑爆内存")
    void ruleBucketCacheIsBounded() throws Exception {
        properties.getRateLimit().setMaxRuleBuckets(5);
        // 任意 interface 路径都有规则 → 制造大量不同桶
        redis.defaultBy(k -> k.startsWith("koiRateLimit:rule:interface:interface-"), "1000");
        redis.defaultBy(k -> k.startsWith("koiRateLimit:rule:interface:"), "1000");

        for (int i = 0; i < 50; i++) {
            run(request("POST", "/random-path-" + i, "10.0.0.5"));
        }

        // 反射读取桶缓存，验证始终未超过上限
        int size = ruleBucketSize();
        assertTrue(size <= 5, "限流桶缓存超过上限: " + size);
    }

    /**
     * 反射读取私有桶缓存大小
     *
     * @return 桶数量
     */
    private int ruleBucketSize() throws Exception {
        java.lang.reflect.Field field = OauthRateLimitFilter.class.getDeclaredField("ruleBuckets");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, ?> map = (java.util.Map<String, ?>) field.get(filter);
        return map.size();
    }
}
