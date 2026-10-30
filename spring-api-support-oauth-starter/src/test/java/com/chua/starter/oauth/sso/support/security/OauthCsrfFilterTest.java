package com.chua.starter.oauth.sso.support.security;

import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link OauthCsrfFilter} 回归测试
 *
 * <p>守护 P0-2：{@code isIgnored} 此前用 {@code uri.contains(pattern)} 判定豁免，而
 * {@code getRequestURI()} 恒含上下文路径 {@code /oauth}，默认豁免列表里的 {@code "/oauth"}
 * 让<b>每一个</b>请求都被豁免——配置写着 {@code enabled=true}，实际一行防护都没生效。</p>
 *
 * <p>同时守护一条硬约束：本服务同时服务浏览器（依赖 Cookie 认证）与后端服务
 * （{@code x-oauth-ak}/{@code x-oauth-sign} 等自定义头），CSRF 只对前者生效，
 * <b>绝不能</b>把服务端到服务端调用打成 403。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("OauthCsrfFilter: CSRF 校验豁免范围")
class OauthCsrfFilterTest {

    /**
     * 上下文路径（与 application.yml 的 server.servlet.context-path 一致）
     */
    private static final String CONTEXT = "/oauth";

    /**
     * 有效 CSRF 令牌
     */
    private static final String GOOD_TOKEN = "good-csrf-token";

    /**
     * 安全配置
     */
    private OauthSecurityProperties properties;

    /**
     * Redis 模板（mock）
     */
    private StringRedisTemplate redis;

    /**
     * ValueOperations（mock）
     */
    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> valueOps;

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
        redis = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);
        // 默认：Redis 中存在 GOOD_TOKEN
        when(valueOps.get(anyString())).thenReturn("1");
    }

    /**
     * 构造带上下文路径的请求
     *
     * @param method HTTP 方法
     * @param path   逻辑路径
     * @return 请求
     */
    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, CONTEXT + path);
        request.setRequestURI(CONTEXT + path);
        request.setContextPath(CONTEXT);
        request.setRemoteAddr("10.0.0.5");
        return request;
    }

    /**
     * 模拟一个「浏览器已登录、依赖 Cookie 认证」的请求
     *
     * @param path 逻辑路径
     * @return 请求
     */
    private MockHttpServletRequest cookieRequest(String path) {
        MockHttpServletRequest request = request("POST", path);
        request.setCookies(new jakarta.servlet.http.Cookie("SESSION", "sess-1"));
        return request;
    }

    /**
     * 执行一次过滤
     *
     * @param request 请求
     * @return 执行结果
     */
    private FilterOutcome run(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingChain chain = new RecordingChain();
        new OauthCsrfFilter(properties, redis).doFilter(request, response, chain);
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

    // ------------------------------------------------------------------
    // P0-2 核心反例：豁免判定退化
    // ------------------------------------------------------------------

    @Test
    @DisplayName("P0-2 核心反例：豁免列表里出现上下文路径 /oauth 也不得让全站免检")
    void contextPathInIgnoreListMustNotExemptEverything() throws Exception {
        // 这正是修复前的默认值：一条 "/oauth" 让 contains() 对每个请求都成立
        properties.getCsrf().setIgnorePaths(List.of("/oauth"));

        FilterOutcome outcome = run(cookieRequest("/login"));

        assertFalse(outcome.passed(), "含上下文路径的豁免项不得使 CSRF 校验整体失效");
        assertEquals(403, outcome.response().getStatus());
    }

    @Test
    @DisplayName("P0-2 核心反例：带 /oauth 前缀的请求仍受 CSRF 保护")
    void requestUriCarryingContextPathIsStillProtected() throws Exception {
        FilterOutcome outcome = run(cookieRequest("/user/info"));

        assertFalse(outcome.passed());
        assertEquals(403, outcome.response().getStatus());
    }

    @Test
    @DisplayName("默认豁免列表不得包含裸上下文路径（防止退化复发）")
    void defaultIgnorePathsMustNotContainBareContextPath() {
        List<String> defaults = new OauthSecurityProperties().getCsrf().getIgnorePaths();
        assertFalse(defaults.contains(CONTEXT),
                "默认豁免列表含裸上下文路径 " + CONTEXT + "，会让锚定匹配退化: " + defaults);
    }

    // ------------------------------------------------------------------
    // 服务端到服务端调用：绝不能被打成 403
    // ------------------------------------------------------------------

    @Test
    @DisplayName("M2M：x-oauth-ak + x-oauth-sign 签名调用免 CSRF（不破坏服务间调用）")
    void machineToMachineAkSignIsExempt() throws Exception {
        MockHttpServletRequest request = cookieRequest("/upgrade");
        request.addHeader("x-oauth-ak", "service-ak");
        request.addHeader("x-oauth-sign", "c2lnbmF0dXJl");

        assertTrue(run(request).passed(), "ak/sk 签名调用绝不能被 CSRF 拦成 403");
    }

    @Test
    @DisplayName("M2M：x-oauth-token 头免 CSRF")
    void machineToMachineTokenHeaderIsExempt() throws Exception {
        MockHttpServletRequest request = cookieRequest("/temporary-token");
        request.addHeader("x-oauth-token", "jwt-token-value");

        assertTrue(run(request).passed());
    }

    @Test
    @DisplayName("M2M：Authorization Bearer 免 CSRF")
    void bearerAuthorizationIsExempt() throws Exception {
        MockHttpServletRequest request = cookieRequest("/login");
        request.addHeader("Authorization", "Bearer jwt-token-value");

        assertTrue(run(request).passed());
    }

    @Test
    @DisplayName("凭据头占位值 unknown（网关未清洗）不得被当作豁免依据")
    void unknownHeaderValueDoesNotExempt() throws Exception {
        MockHttpServletRequest request = cookieRequest("/login");
        request.addHeader("x-oauth-ak", "unknown");

        FilterOutcome outcome = run(request);
        assertFalse(outcome.passed(), "占位值 unknown 说明头被中间层吃掉，不构成凭据");
        assertEquals(403, outcome.response().getStatus());
    }

    @Test
    @DisplayName("空凭据头不得豁免")
    void blankCredentialHeaderDoesNotExempt() throws Exception {
        MockHttpServletRequest request = cookieRequest("/login");
        request.addHeader("x-oauth-sign", "   ");

        assertFalse(run(request).passed());
    }

    // ------------------------------------------------------------------
    // Cookie 认证请求：必须真正校验
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Cookie 认证的状态变更请求，缺少令牌一律 403")
    void cookieAuthenticatedPostWithoutTokenIsForbidden() throws Exception {
        FilterOutcome outcome = run(cookieRequest("/user/info"));

        assertFalse(outcome.passed());
        assertEquals(403, outcome.response().getStatus());
    }

    @Test
    @DisplayName("请求头与 Cookie 令牌一致且 Redis 命中则放行（double-submit 正常工作）")
    void matchingDoubleSubmitTokenIsAccepted() throws Exception {
        MockHttpServletRequest request = cookieRequest("/user/info");
        request.addHeader("X-CSRF-TOKEN", GOOD_TOKEN);
        request.setCookies(new jakarta.servlet.http.Cookie("SESSION", "sess-1"),
                new jakarta.servlet.http.Cookie("XSRF-TOKEN", GOOD_TOKEN));

        assertTrue(run(request).passed(), "合法的 double-submit 不得被误杀");
    }

    @Test
    @DisplayName("请求头与 Cookie 令牌不一致则 403")
    void mismatchedDoubleSubmitTokenIsForbidden() throws Exception {
        MockHttpServletRequest request = cookieRequest("/user/info");
        request.addHeader("X-CSRF-TOKEN", "attacker-token");
        request.setCookies(new jakarta.servlet.http.Cookie("XSRF-TOKEN", GOOD_TOKEN));

        FilterOutcome outcome = run(request);
        assertFalse(outcome.passed());
        assertEquals(403, outcome.response().getStatus());
    }

    @Test
    @DisplayName("只有请求头没有 Cookie（Cookie 被剥掉）时 403")
    void headerOnlyWithoutCookieIsForbidden() throws Exception {
        MockHttpServletRequest request = request("POST", "/user/info");
        request.addHeader("X-CSRF-TOKEN", GOOD_TOKEN);

        assertFalse(run(request).passed(), "缺 Cookie 的请求不构成 double-submit");
    }

    @Test
    @DisplayName("Redis 中无该令牌（已过期/已注销）时 403")
    void tokenMissingInRedisIsForbidden() throws Exception {
        when(valueOps.get(anyString())).thenReturn(null);
        MockHttpServletRequest request = cookieRequest("/user/info");
        request.addHeader("X-CSRF-TOKEN", GOOD_TOKEN);
        request.setCookies(new jakarta.servlet.http.Cookie("XSRF-TOKEN", GOOD_TOKEN));

        assertFalse(run(request).passed());
    }

    @Test
    @DisplayName("Redis 异常时 fail-closed（拒绝而非放行）")
    void redisFailureFailsClosed() throws Exception {
        when(valueOps.get(anyString())).thenThrow(new RuntimeException("redis down"));
        MockHttpServletRequest request = cookieRequest("/user/info");
        request.addHeader("X-CSRF-TOKEN", GOOD_TOKEN);
        request.setCookies(new jakarta.servlet.http.Cookie("XSRF-TOKEN", GOOD_TOKEN));

        FilterOutcome outcome = run(request);
        assertFalse(outcome.passed(), "Redis 不可用时不得放行");
        assertEquals(403, outcome.response().getStatus());
    }

    // ------------------------------------------------------------------
    // 豁免路径语义
    // ------------------------------------------------------------------

    @Test
    @DisplayName("逻辑路径豁免生效：三方登录顶层回调被豁免")
    void logicalPathIgnoreWorks() throws Exception {
        // /oauth2/callback/** 是逻辑路径，真实请求为 /oauth/oauth2/callback/gitee
        assertTrue(run(cookieRequest("/oauth2/callback/gitee")).passed(),
                "三方平台以顶层跳转发起、无 Cookie 会话，不适用 double-submit");
    }

    @Test
    @DisplayName("锚定匹配：/oauth2 下的其它路径不被 callback 规则误伤")
    void anchoredMatchingDoesNotOverExempt() throws Exception {
        assertFalse(run(cookieRequest("/oauth2/attacker")).passed(),
                "只有 /oauth2/callback/** 才是豁免路径");
    }

    @Test
    @DisplayName("锚定匹配：精确豁免项不得前缀命中 /actuator/healthz")
    void anchoredMatchingIsExactForHealth() throws Exception {
        // 默认值是 /actuator/** （整个 actuator 前缀豁免），这里改成精确项以验证锚定语义
        properties.getCsrf().setIgnorePaths(List.of("/actuator/health"));

        assertTrue(run(cookieRequest("/actuator/health")).passed());
        assertFalse(run(cookieRequest("/actuator/healthz")).passed(),
                "/actuator/health 不得前缀命中 /actuator/healthz");
    }

    @Test
    @DisplayName("csrf.enabled=false 时整体豁免")
    void disabledCsrfExemptsEverything() throws Exception {
        properties.getCsrf().setEnabled(false);

        assertTrue(run(cookieRequest("/login")).passed());
    }

    @Test
    @DisplayName("需要校验的方法集合可配置，收窄后 GET 不再签发令牌")
    void methodsAreConfigurable() throws Exception {
        properties.getCsrf().setMethods(List.of("DELETE"));

        assertTrue(run(cookieRequest("/user/info")).passed(), "POST 不在集合内不应校验");
    }

    // ------------------------------------------------------------------
    // 安全方法应签发令牌
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET 放行并签发 CSRF 令牌 Cookie")
    void getIssuesTokenCookie() throws Exception {
        MockHttpServletRequest request = request("GET", "/authorize");
        FilterOutcome outcome = run(request);

        assertTrue(outcome.passed());
        assertNotNull(outcome.response().getCookie("XSRF-TOKEN"), "GET 应下发 CSRF 令牌 Cookie");
        assertNotNull(outcome.response().getHeader("X-CSRF-TOKEN"), "GET 应同时回带头部令牌");
    }

    @Test
    @DisplayName("已有有效令牌 Cookie 时不重复下发")
    void existingValidCookieIsNotReissued() throws Exception {
        when(redis.hasKey(anyString())).thenReturn(Boolean.TRUE);
        MockHttpServletRequest request = request("GET", "/authorize");
        request.setCookies(new jakarta.servlet.http.Cookie("XSRF-TOKEN", GOOD_TOKEN));

        FilterOutcome outcome = run(request);

        assertTrue(outcome.passed());
        assertEquals(null, outcome.response().getCookie("XSRF-TOKEN"),
                "已有有效令牌时不应重复下发");
    }

    @Test
    @DisplayName("CSRF 失败响应不得回显令牌")
    void forbiddenResponseDoesNotLeakToken() throws Exception {
        MockHttpServletRequest request = cookieRequest("/user/info");
        request.addHeader("X-CSRF-TOKEN", "attacker-token");
        request.setCookies(new jakarta.servlet.http.Cookie("XSRF-TOKEN", GOOD_TOKEN));

        FilterOutcome outcome = run(request);

        assertFalse(outcome.response().getContentAsString().contains("attacker-token"));
        assertFalse(outcome.response().getContentAsString().contains(GOOD_TOKEN));
    }
}
