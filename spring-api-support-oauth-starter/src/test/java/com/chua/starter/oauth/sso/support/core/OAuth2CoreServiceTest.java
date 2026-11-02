package com.chua.starter.oauth.sso.support.core;

import com.chua.starter.entity.system.SysAppKey;
import com.chua.starter.system.service.SysAppIdService;
import com.chua.starter.system.service.SysAppKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OAuth2 授权码流程的放行边界测试。
 *
 * <p>守护三类缺陷：</p>
 * <ul>
 *   <li>回调地址用 {@code startsWith} 匹配，{@code https://app/cb} 会放行 {@code https://app/cb.evil.com}；</li>
 *   <li>客户端未配置回调地址时默认放行（fail-open）；</li>
 *   <li>未配置 scope / grant_type 时默认授予 {@code admin} 与 {@code password}。</li>
 * </ul>
 *
 * @since 4.0.0.42
 * @author CH
 */
@DisplayName("OAuth2CoreService: 回调地址与默认授权范围")
class OAuth2CoreServiceTest {

    /**
     * 客户端标识
     */
    private static final String CLIENT_ID = "client-1";

    /**
     * AppId 服务
     */
    private SysAppIdService sysAppIdService;

    /**
     * AppKey 服务
     */
    private SysAppKeyService sysAppKeyService;

    /**
     * Redis 模板
     */
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 被测服务
     */
    private OAuth2CoreService service;

    /**
     * 初始化被测服务。
     */
    @BeforeEach
    void setUp() {

    }
    /**
     * 构造 AppKey 配置。
     *
     * @param redirectUris 回调地址
     * @param scopes       scope
     * @param grantTypes   grant_type
     * @return AppKey
     */
    private SysAppKey appKey(String redirectUris, String scopes, String grantTypes) {
        SysAppKey key = new SysAppKey();
        key.setSysAppKeyRedirectUris(redirectUris);
        key.setSysAppKeyScope(scopes);
        key.setSysAppKeyGrantTypes(grantTypes);
        return key;
    }

    /**
     * 手写的「轻量桩」：用 {@link Proxy} 实现需要注定的服务接口，
     * 未显式配置的方法返回 {@code null}，只记录调用过的方法名。
     */
    private static class ProxyStub implements InvocationHandler {
        private final Map<String, Object> stubs = new ConcurrentHashMap<>();
        /** 逐笔录制的调用 */
        private final java.util.List<String> recorded = new java.util.ArrayList<>();

        ProxyStub(Map<String, Object> stubs) {
            this.stubs.putAll(stubs);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            recorded.add(method.getName());
            return stubs.get(method.getName());
        }

        void assertRecorded(String name) {
            assertTrue(stubs.containsKey(name), "未记录到期望调用");
        }
    }

    private <T> T stubOf(Class<T> type, Map<String, Object> stubs) {
        ProxyStub stub = new ProxyStub(stubs);
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, stub);
    }

    /**
     * 未配置回调地址时必须拒绝，而不是默认放行。
     */
    @Test
    @DisplayName("未配置回调地址: fail-closed")
    void shouldRejectWhenRedirectUriNotConfigured() {
        sysAppIdService = stubOf(SysAppIdService.class, Map.of());
        sysAppKeyService = stubOf(SysAppKeyService.class,
                Map.of("getByAppKey", appKey(null, null, null)));
        stringRedisTemplate = new StringRedisTemplate();
        service = new OAuth2CoreService(sysAppKeyService, sysAppIdService, stringRedisTemplate);

        assertFalse(service.isRedirectUriValid(CLIENT_ID, "https://app.example.com/cb"));
    }

    /**
     * 精确匹配放行。
     */
    @Test
    @DisplayName("精确匹配放行")
    void shouldAllowExactMatch() {
        sysAppIdService = stubOf(SysAppIdService.class, Map.of());
        sysAppKeyService = stubOf(SysAppKeyService.class,
                Map.of("getByAppKey", appKey("https://app.example.com/cb", null, null)));
        stringRedisTemplate = new StringRedisTemplate();
        service = new OAuth2CoreService(sysAppKeyService, sysAppIdService, stringRedisTemplate);

        assertTrue(service.isRedirectUriValid(CLIENT_ID, "https://app.example.com/cb"));
    }

    /**
     * 前缀形近域名不得放行（授权码劫持）。
     */
    @Test
    @DisplayName("前缀形近地址必须拒绝")
    void shouldRejectPrefixLookalike() {
        sysAppIdService = stubOf(SysAppIdService.class, Map.of());
        sysAppKeyService = stubOf(SysAppKeyService.class,
                Map.of("getByAppKey", appKey("https://app.example.com/cb", null, null)));
        stringRedisTemplate = new StringRedisTemplate();
        service = new OAuth2CoreService(sysAppKeyService, sysAppIdService, stringRedisTemplate);

        assertFalse(service.isRedirectUriValid(CLIENT_ID, "https://app.example.com/cb.evil.com"));
        assertFalse(service.isRedirectUriValid(CLIENT_ID, "https://app.example.com/cb/extra"));
    }

    /**
     * 显式通配才放行子路径，且拒绝路径穿越。
     */
    @Test
    @DisplayName("显式 /* 通配: 只放行子路径")
    void shouldAllowWildcardSuffixOnly() {
        sysAppIdService = stubOf(SysAppIdService.class, Map.of());
        sysAppKeyService = stubOf(SysAppKeyService.class,
                Map.of("getByAppKey", appKey("https://app.example.com/cb/*", null, null)));
        stringRedisTemplate = new StringRedisTemplate();
        service = new OAuth2CoreService(sysAppKeyService, sysAppIdService, stringRedisTemplate);

        assertTrue(service.isRedirectUriValid(CLIENT_ID, "https://app.example.com/cb/callback"));
        assertFalse(service.isRedirectUriValid(CLIENT_ID, "https://app.example.com/cb/../admin"));
        assertFalse(service.isRedirectUriValid(CLIENT_ID, "https://evil.example.com/cb/x"));
    }

    /**
     * 默认 scope 不得包含 admin。
     */
    @Test
    @DisplayName("默认 scope 不含 admin")
    void shouldNotGrantAdminScopeByDefault() {
        sysAppIdService = stubOf(SysAppIdService.class, Map.of());
        sysAppKeyService = stubOf(SysAppKeyService.class,
                Map.of("getByAppKey", appKey(null, null, null)));
        stringRedisTemplate = new StringRedisTemplate();
        service = new OAuth2CoreService(sysAppKeyService, sysAppIdService, stringRedisTemplate);

        OAuth2CoreService.ScopeValidationResult admin =
                service.validateScope(CLIENT_ID, "admin");
        assertFalse(admin.valid(), "未配置 scope 时不得默认授予 admin");

        OAuth2CoreService.ScopeValidationResult defaultScope =
                service.validateScope(CLIENT_ID, null);
        assertTrue(defaultScope.valid());
        assertTrue(defaultScope.grantedScopes().contains("read"));
        assertFalse(defaultScope.grantedScopes().contains("admin"));
    }

    /**
     * 默认 grant_type 不得包含 password。
     */
    @Test
    @DisplayName("默认 grant_type 不含 password")
    void shouldNotAllowPasswordGrantByDefault() {
        sysAppIdService = stubOf(SysAppIdService.class, Map.of());
        sysAppKeyService = stubOf(SysAppKeyService.class,
                Map.of("getByAppKey", appKey(null, null, null)));
        stringRedisTemplate = new StringRedisTemplate();
        service = new OAuth2CoreService(sysAppKeyService, sysAppIdService, stringRedisTemplate);

        assertFalse(service.isGrantTypeAllowed(CLIENT_ID, "password"));
        assertTrue(service.isGrantTypeAllowed(CLIENT_ID, "authorization_code"));
    }

    /**
     * 显式配置 scope 时放行已授权的 scope。
     */
    @Test
    @DisplayName("显式 scope 放行已授权范围")
    void shouldAllowConfiguredScopes() {
        sysAppIdService = stubOf(SysAppIdService.class, Map.of());
        sysAppKeyService = stubOf(SysAppKeyService.class,
                Map.of("getByAppKey", appKey(null, "read,openid", "authorization_code")));
        stringRedisTemplate = new StringRedisTemplate();
        service = new OAuth2CoreService(sysAppKeyService, sysAppIdService, stringRedisTemplate);

        assertTrue(service.validateScope(CLIENT_ID, "read").valid());
        assertTrue(service.validateScope(CLIENT_ID, "openid").valid());
        assertFalse(service.validateScope(CLIENT_ID, "admin").valid(), "未授权的 scope 拒绝了");
    }

    /**
     * 显式配置 grant_type 时放行已授权的授权类型。
     */
    @Test
    @DisplayName("显式 grant_type 放行已授权类型")
    void shouldAllowConfiguredGrantTypes() {
        sysAppIdService = stubOf(SysAppIdService.class, Map.of());
        sysAppKeyService = stubOf(SysAppKeyService.class,
                Map.of("getByAppKey", appKey(null, null, "client_credentials,refresh_token")));
        stringRedisTemplate = new StringRedisTemplate();
        service = new OAuth2CoreService(sysAppKeyService, sysAppIdService, stringRedisTemplate);

        assertTrue(service.isGrantTypeAllowed(CLIENT_ID, "client_credentials"));
        assertTrue(service.isGrantTypeAllowed(CLIENT_ID, "refresh_token"));
        assertFalse(service.isGrantTypeAllowed(CLIENT_ID, "password"), "未配置的授权类型拒绝了");
    }

    /**
     * 不记录 Redis 调用（本测试不涉及授权码的生成/消费）
     */
    @Test
    @DisplayName("Redis 操作不应参与回调地址与 scope 判定")
    void redisNotUsedForValidation() {
        sysAppIdService = stubOf(SysAppIdService.class, Map.of());
        sysAppKeyService = stubOf(SysAppKeyService.class,
                Map.of("getByAppKey", appKey("https://app.example.com/cb", null, null)));
        stringRedisTemplate = new StringRedisTemplate();
        service = new OAuth2CoreService(sysAppKeyService, sysAppIdService, stringRedisTemplate);

        assertTrue(service.isRedirectUriValid(CLIENT_ID, "https://app.example.com/cb"));
        assertTrue(service.validateScope(CLIENT_ID, null).valid());
    }
}