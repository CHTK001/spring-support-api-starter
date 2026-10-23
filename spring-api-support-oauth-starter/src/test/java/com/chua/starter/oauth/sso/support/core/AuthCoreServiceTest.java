package com.chua.starter.oauth.sso.support.core;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 认证核心服务测试。
 *
 * <p>守护收敛后的语义：升级类型忽略大小写、登出按 uid/token 分流、
 * 令牌解析器不可用时返回错误而不是 NPE。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthCoreService: 登录/升级/登出收敛")
class AuthCoreServiceTest {

    /**
     * 登录检查
     */
    @Mock
    private LoginCheck loginCheck;

    /**
     * 令牌解析器
     */
    @Mock
    private TokenResolver tokenResolver;

    /**
     * 被测服务
     */
    private AuthCoreService authCoreService;

    /**
     * 初始化。
     */
    @BeforeEach
    void setUp() {
        AuthServerProperties properties = new AuthServerProperties();
        properties.setTokenManagement("redis");
        authCoreService = new AuthCoreService(properties, loginCheck);
    }

    /**
     * 登录直接委托给 LoginCheck。
     */
    @Test
    @DisplayName("login: 委托 LoginCheck")
    void shouldDelegateLogin() {
        ReturnResult<LoginResult> expected = ReturnResult.ok(new LoginResult());
        when(loginCheck.doLogin("1.2.3.4", "alice", "pwd", "WEB", null)).thenReturn(expected);

        assertSame(expected, authCoreService.login("1.2.3.4", "alice", "pwd", "WEB", null));
    }

    /**
     * refresh 升级。
     */
    @Test
    @DisplayName("upgrade(refresh): 走 refresh 分支")
    void shouldUpgradeRefresh() {
        when(loginCheck.resolveTokenResolver("redis")).thenReturn(tokenResolver);
        when(tokenResolver.upgradeForRefresh("t", "r")).thenReturn(ReturnResult.ok(new UserResume()));

        assertTrue(authCoreService.upgrade("refresh", "t", "r").isOk());
        verify(tokenResolver).upgradeForRefresh("t", "r");
    }

    /**
     * 版本升级大小写不敏感（早期小写会被静默降级成 TIMESTAMP）。
     */
    @Test
    @DisplayName("upgrade(VeRsIoN): 大小写不敏感")
    void shouldUpgradeVersionIgnoreCase() {
        when(loginCheck.resolveTokenResolver("redis")).thenReturn(tokenResolver);
        when(tokenResolver.upgradeForVersion("t", "r")).thenReturn(ReturnResult.ok(new UserResume()));

        assertTrue(authCoreService.upgrade("VeRsIoN", "t", "r").isOk());
        verify(tokenResolver).upgradeForVersion("t", "r");
        verify(tokenResolver, never()).upgradeForTimestamp("t", "r");
    }

    /**
     * 未知/空类型回落时间戳升级，保持历史行为。
     */
    @Test
    @DisplayName("upgrade(null): 回落 TIMESTAMP")
    void shouldFallbackToTimestamp() {
        when(loginCheck.resolveTokenResolver("redis")).thenReturn(tokenResolver);
        when(tokenResolver.upgradeForTimestamp("t", "r")).thenReturn(ReturnResult.ok(new UserResume()));

        assertTrue(authCoreService.upgrade(null, "t", "r").isOk());
        verify(tokenResolver).upgradeForTimestamp("t", "r");
    }

    /**
     * 有 uid 时按 uid + 类型处理（KICK/DISABLE 等）。
     */
    @Test
    @DisplayName("logout(uid): 按 uid + 类型")
    void shouldLogoutByUid() {
        when(loginCheck.resolveTokenResolver("redis")).thenReturn(tokenResolver);

        assertTrue(authCoreService.logout("42", LogoutType.KICK, "tok").isOk());
        verify(tokenResolver).logout("42", LogoutType.KICK);
    }

    /**
     * 只有令牌时按单令牌登出。
     */
    @Test
    @DisplayName("logout(token): 按单令牌")
    void shouldLogoutByToken() {
        when(loginCheck.resolveTokenResolver("redis")).thenReturn(tokenResolver);

        assertTrue(authCoreService.logout(null, LogoutType.LOGOUT, "tok").isOk());
        verify(tokenResolver).logout("tok");
    }

    /**
     * 令牌解析器不可用时返回错误，不抛 NPE。
     */
    @Test
    @DisplayName("解析器缺失: 返回错误")
    void shouldFailWhenResolverMissing() {
        when(loginCheck.resolveTokenResolver("redis")).thenReturn(null);

        assertFalse(authCoreService.logout("42", LogoutType.KICK, null).isOk());
        assertFalse(authCoreService.upgrade("refresh", "t", "r").isOk());
    }
}
