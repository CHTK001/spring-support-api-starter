package com.chua.starter.system.filter;

import com.chua.starter.system.support.PolicyCacheService;
import com.chua.starter.system.support.SystemRateLimitManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 策略限流过滤器测试（基于 Spring Cache 内存实现，不依赖 Redis）。
 *
 * @author CH
 */
class PolicyRateLimitFilterTest {

    @Test
    void shouldRunGlobalLimitAfterSpecificRulesPass() throws Exception {
        PolicyCacheService cache = cache();
        cache.putRateLimit("interface", "/v2/test", 1);
        cache.putRateLimit("ip", "127.0.0.1", 1);
        RecordingRateLimitManager global = new RecordingRateLimitManager(false);
        PolicyRateLimitFilter filter = new PolicyRateLimitFilter(cache, global);
        ResponseState response = new ResponseState();

        filter.doFilter(request(null).proxy(), response.request(), chainCounter());

        assertEquals(429, response.status());
        assertEquals("{\"code\":\"429\",\"msg\":\"请求过于频繁，请稍后重试\",\"ok\":false}", response.body());
        assertEquals(1, global.calls());
    }

    @Test
    void shouldRejectOnSpecificRuleAndSkipGlobal() throws Exception {
        PolicyCacheService cache = cache();
        cache.putRateLimit("interface", "/v2/test", 1);
        RecordingRateLimitManager global = new RecordingRateLimitManager(true);
        PolicyRateLimitFilter filter = new PolicyRateLimitFilter(cache, global);
        ResponseState first = new ResponseState();
        ResponseState second = new ResponseState();

        filter.doFilter(request(null).proxy(), first.request(), chainCounter());
        filter.doFilter(request(null).proxy(), second.request(), chainCounter());

        assertEquals(200, first.status());
        assertEquals(429, second.status());
        // 第一次通过具体规则后会消耗一次全局令牌；第二次被具体规则拒绝，不再消耗
        assertEquals(1, global.calls());
    }

    @Test
    void shouldSkipGlobalLimitForIpWhitelist() throws Exception {
        PolicyCacheService cache = cache();
        cache.putRateLimit("interface", "/v2/test", 1);
        cache.projectIp(0, "127.0.0.0/8");
        RecordingRateLimitManager global = new RecordingRateLimitManager(false);
        PolicyRateLimitFilter filter = new PolicyRateLimitFilter(cache, global);
        ResponseState response = new ResponseState();

        filter.doFilter(request(null).proxy(), response.request(), chainCounter());

        assertEquals(200, response.status());
        assertEquals(0, global.calls());
    }

    @Test
    void shouldRejectCidrAndAccountBlacklist() throws Exception {
        PolicyCacheService cidrCache = cache();
        cidrCache.projectIp(1, "127.0.0.0/8");
        PolicyRateLimitFilter cidrFilter = new PolicyRateLimitFilter(cidrCache, new RecordingRateLimitManager(true));
        ResponseState cidrResponse = new ResponseState();
        cidrFilter.doFilter(request(null).proxy(), cidrResponse.request(), chainCounter());
        assertEquals(403, cidrResponse.status());

        PolicyCacheService accountCache = cache();
        accountCache.putAccount(1, "ops");
        PolicyRateLimitFilter accountFilter =
                new PolicyRateLimitFilter(accountCache, new RecordingRateLimitManager(true));
        ResponseState accountResponse = new ResponseState();
        accountFilter.doFilter(request("ops").proxy(), accountResponse.request(), chainCounter());
        assertEquals(403, accountResponse.status());
    }

    @Test
    void shouldPreferUsernameAttributeOverHeader() throws Exception {
        PolicyCacheService cache = cache();
        cache.putRateLimit("account", "trusted-user", 1);
        PolicyRateLimitFilter filter = new PolicyRateLimitFilter(cache, new RecordingRateLimitManager(true));
        TestRequest testRequest = request("header-account");
        testRequest.attributes.put("username", "trusted-user");

        filter.doFilter(testRequest.proxy(), new ResponseState().request(), chainCounter());
        ResponseState response = new ResponseState();

        filter.doFilter(testRequest.proxy(), response.request(), chainCounter());

        assertEquals(429, response.status());
    }

    @Test
    void shouldRunAfterAuthFilter() {
        PolicyRateLimitFilter filter = new PolicyRateLimitFilter(cache(), new RecordingRateLimitManager(true));

        assertEquals(Integer.MAX_VALUE - 1, filter.getOrder());
    }

    private static PolicyCacheService cache() {
        return new PolicyCacheService(new ConcurrentMapCacheManager());
    }

    private static TestRequest request(String headerAccount) {
        TestRequest request = new TestRequest();
        if (null != headerAccount) {
            request.headers.put("X-Account", headerAccount);
        }
        return request;
    }

    private static FilterChain chainCounter() {
        return chainCounter(new AtomicInteger());
    }

    private static FilterChain chainCounter(AtomicInteger calls) {
        return (request, response) -> calls.incrementAndGet();
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (boolean.class.equals(type)) {
            return false;
        }
        if (char.class.equals(type)) {
            return '\0';
        }
        if (byte.class.equals(type)) {
            return (byte) 0;
        }
        if (short.class.equals(type)) {
            return (short) 0;
        }
        if (int.class.equals(type)) {
            return 0;
        }
        if (long.class.equals(type)) {
            return 0L;
        }
        if (float.class.equals(type)) {
            return 0F;
        }
        return 0D;
    }

    private static final class RecordingRateLimitManager extends SystemRateLimitManager {
        private final boolean allow;
        private int calls;

        private RecordingRateLimitManager(boolean allow) {
            this.allow = allow;
        }

        @Override
        public boolean tryAcquire() {
            calls++;
            return allow;
        }

        private int calls() {
            return calls;
        }
    }

    private static final class TestRequest {
        private final Map<String, String> headers = new HashMap<>();
        private final Map<String, Object> attributes = new HashMap<>();
        private Principal principal;

        private HttpServletRequest proxy() {
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getRequestURI" -> "/v2/test";
                        case "getContextPath" -> "";
                        case "getRemoteAddr" -> "127.0.0.1";
                        case "getHeader" -> headers.get(String.valueOf(args[0]));
                        case "getAttribute" -> attributes.get(String.valueOf(args[0]));
                        case "getUserPrincipal" -> principal;
                        case "setAttribute" -> {
                            attributes.put(String.valueOf(args[0]), args[1]);
                            yield null;
                        }
                        default -> defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class ResponseState {
        private final AtomicInteger status = new AtomicInteger(200);
        private final StringWriter body = new StringWriter();
        private final PrintWriter writer = new PrintWriter(body);

        private HttpServletResponse request() {
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "setStatus" -> {
                            status.set((Integer) args[0]);
                            yield null;
                        }
                        case "getWriter" -> writer;
                        default -> defaultValue(method.getReturnType());
                    });
        }

        private int status() {
            return status.get();
        }

        private String body() {
            writer.flush();
            return body.toString();
        }
    }
}
