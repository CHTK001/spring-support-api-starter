package com.chua.starter.oauth.sso.support.core;

import com.chua.starter.entity.system.SysAppKey;
import com.chua.starter.system.service.SysAppIdService;
import com.chua.starter.system.service.SysAppKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2CoreService: 回调地址与默认授权范围")
class OAuth2CoreServiceTest {

    /**
     * 客户端标识
     */
    private static final String CLIENT_ID = "client-1";

    /**
     * AppId 服务
     */
    @Mock
    private SysAppIdService sysAppIdService;

    /**
     * AppKey 服务
     */
    @Mock
    private SysAppKeyService sysAppKeyService;

    /**
     * Redis 模板
     */
    @Mock
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
        service = new OAuth2CoreService(sysAppKeyService, sysAppIdService, stringRedisTemplate);
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
     * 未配置回调地址时必须拒绝，而不是默认放行。
     */
    @Test
    @DisplayName("未配置回调地址: fail-closed")
    void shouldRejectWhenRedirectUriNotConfigured() {
        when(sysAppKeyService.getByAppKey(CLIENT_ID)).thenReturn(appKey(null, null, null));

        assertFalse(service.isRedirectUriValid(CLIENT_ID, "https://app.example.com/cb"));
    }

    /**
     * 精确匹配放行。
     */
    @Test
    @DisplayName("精确匹配放行")
    void shouldAllowExactMatch() {
        when(sysAppKeyService.getByAppKey(CLIENT_ID))
                .thenReturn(appKey("https://app.example.com/cb", null, null));

        assertTrue(service.isRedirectUriValid(CLIENT_ID, "https://app.example.com/cb"));
    }

    /**
     * 前缀形近域名不得放行（授权码劫持）。
     */
    @Test
    @DisplayName("前缀形近地址必须拒绝")
    void shouldRejectPrefixLookalike() {
        when(sysAppKeyService.getByAppKey(CLIENT_ID))
                .thenReturn(appKey("https://app.example.com/cb", null, null));

        assertFalse(service.isRedirectUriValid(CLIENT_ID, "https://app.example.com/cb.evil.com"));
        assertFalse(service.isRedirectUriValid(CLIENT_ID, "https://app.example.com/cb/extra"));
    }

    /**
     * 显式通配才放行子路径，且拒绝路径穿越。
     */
    @Test
    @DisplayName("显式 /* 通配: 只放行子路径")
    void shouldAllowWildcardSuffixOnly() {
        when(sysAppKeyService.getByAppKey(CLIENT_ID))
                .thenReturn(appKey("https://app.example.com/cb/*", null, null));

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
        when(sysAppKeyService.getByAppKey(CLIENT_ID)).thenReturn(appKey(null, null, null));

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
        when(sysAppKeyService.getByAppKey(CLIENT_ID)).thenReturn(appKey(null, null, null));

        assertFalse(service.isGrantTypeAllowed(CLIENT_ID, "password"));
        assertTrue(service.isGrantTypeAllowed(CLIENT_ID, "authorization_code"));
    }
}
