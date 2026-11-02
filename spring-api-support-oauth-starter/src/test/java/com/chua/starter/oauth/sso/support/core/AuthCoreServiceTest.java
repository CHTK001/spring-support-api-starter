package com.chua.starter.oauth.sso.support.core;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 认证核心服务测试。
 *
 * <p>守护收敛后的语义：升级类型忽略大小写、登出按 uid/token 分流、
 * 令牌解析器不可用时返回错误而不是 NPE。</p>
 *
 * <p>P3C40 去 mockito 改造：原实现用 {@code @Mock LoginCheck} / {@code @Mock TokenResolver}
 * 把登录/升级/登出的全部分支悬空——什么都测不到，只断言了必然放行的假密钥。
 * 现在这两层都是手写的记录桩（{@link FakeLoginCheck} / {@link FakeTokenResolver}），
 * 用「调用是否真的落到记录序列」代替 {@code verify()}，把逻辑验证。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@DisplayName("AuthCoreService: 登录/升级/登出收敛")
class AuthCoreServiceTest {

    /**
     * 登录检查桩：记录每笔路由调用。
     */
    private static class FakeLoginCheck extends LoginCheck {

        /** 解析出的令牌解析器 */
        private TokenResolver tokenResolver;

        /** 逐笔录制的调用 */
        private final List<String> recorded = new ArrayList<>();

        /** doLogin 返回的结果 */
        private ReturnResult<LoginResult> doLoginResult = ReturnResult.ok(new LoginResult());

        FakeLoginCheck(AuthServerProperties properties) {
            super(properties);
        }

        @Override
        public ReturnResult<LoginResult> doLogin(String address, String principal,
                                                 String credential, String authType, Object ext) {
            recorded.add("doLogin(" + address + ", " + principal + ", " + credential + ", " + authType + ")");
            return doLoginResult;
        }

        @Override
        public TokenResolver resolveTokenResolver(String tokenManagement) {
            recorded.add("resolveTokenResolver(" + tokenManagement + ")");
            return tokenResolver;
        }

        /**
         * 设置解析器（用于模拟缺失场景）
         *
         * @param resolver 解析器
         */
        void setTokenResolver(TokenResolver resolver) {
            this.tokenResolver = resolver;
        }

        /**
         * 设置 doLogin 返回的结果（用于验证登录委托时原值送达）
         *
         * @param result 返回结果
         */
        void setDoLoginResult(ReturnResult<LoginResult> result) {
            this.doLoginResult = result;
        }

        void assertRecorded(String name) {
            assertTrue(recorded.contains(name), "未记录到期望调用：记录=" + recorded);
        }

        void assertNotRecorded(String name) {
            assertFalse(recorded.contains(name), "不应记录到调用：记录=" + recorded);
        }
    }

    /**
     * 令牌解析器桩：记录每笔调用，暴露 try/resolve/upgrade 返回值。
     */
    private static class FakeTokenResolver implements TokenResolver {

        /** 逐笔录制的调用 */
        private final List<String> recorded = new ArrayList<>();

        /** resolve 返回的结果 */
        private ResolveResult<UserResume> resolveResult =
                new ResolveResult<>(true, new UserResume());

        /** upgrade 返回的结果 */
        private ResolveResult<UserResume> upgradeResult =
                new ResolveResult<>(true, new UserResume());

        private record ResolveResult<T>(boolean ok, T data) {}

        @Override
        public ReturnResult<LoginResult> createToken(String address, UserResult userResult, String authType) {
            recorded.add("createToken(" + address + ", " + userResult.getUserId() + ", " + authType + ")");
            LoginResult loginResult = new LoginResult();
            loginResult.setToken("token-" + userResult.getUserId());
            return ReturnResult.ok(loginResult);
        }

        @Override
        public void logout(String token) {
            recorded.add("logout(token=" + token + ")");
        }

        @Override
        public void logout(String uid, LogoutType type) {
            recorded.add("logout(uid=" + uid + ", " + type + ")");
        }

        @Override
        public ReturnResult<UserResume> resolve(String cookie, String token) {
            recorded.add("resolve(cookie=" + cookie + ", token=" + token + ")");
            return resolveResult.ok()
                    ? ReturnResult.ok(resolveResult.data())
                    : ReturnResult.error("未认证");
        }

        @Override
        public ReturnResult<UserResume> upgradeForTimestamp(String token, String refreshToken) {
            recorded.add("upgradeForTimestamp(" + token + ", " + refreshToken + ")");
            return ReturnResult.ok(upgradeResult.data());
        }

        @Override
        public ReturnResult<UserResume> upgradeForVersion(String token, String refreshToken) {
            recorded.add("upgradeForVersion(" + token + ", " + refreshToken + ")");
            return ReturnResult.ok(upgradeResult.data());
        }

        @Override
        public ReturnResult<UserResume> upgradeForRefresh(String token, String refreshToken) {
            recorded.add("upgradeForRefresh(" + token + ", " + refreshToken + ")");
            return ReturnResult.ok(upgradeResult.data());
        }

        @Override
        public void logout(Cookie[] cookies, String token, String refreshToken, String cookieName) {
            recorded.add("logout(cookies, token=" + token + ", cookie=" + cookieName + ")");
        }

        @Override
        public ReturnResult<UserResume> resolveUserCode(String userCode) {
            recorded.add("resolveUserCode(" + userCode + ")");
            return ReturnResult.ok(new UserResume());
        }

        void assertRecorded(String name) {
            assertTrue(recorded.contains(name), "未记录到期望调用：记录=" + recorded);
        }

        void assertNotRecorded(String name) {
            assertFalse(recorded.contains(name), "不应记录到调用：记录=" + recorded);
        }
    }

    /**
     * 登录检查
     */
    private FakeLoginCheck loginCheck;

    /**
     * 认证核心服务
     */
    private AuthCoreService authCoreService;

    /**
     * 初始化
     */
    @BeforeEach
    void setUp() {
        AuthServerProperties properties = new AuthServerProperties();
        properties.setTokenManagement("redis");
        loginCheck = new FakeLoginCheck(properties);
        loginCheck.setTokenResolver(new FakeTokenResolver());
        authCoreService = new AuthCoreService(properties, loginCheck);
    }

    /**
     * 登录直接委托给 LoginCheck
     */
    @Test
    @DisplayName("login: 委托 LoginCheck")
    void shouldDelegateLogin() {
        ReturnResult<LoginResult> expected = ReturnResult.ok(new LoginResult());
        // 桩的 doLogin 直接原值返回：授权返回经过 loginCheck ，不做任何包装。
        loginCheck.setDoLoginResult(expected);

        assertSame(expected, authCoreService.login("1.2.3.4", "alice", "pwd", "WEB", null));

        loginCheck.assertRecorded("doLogin(1.2.3.4, alice, pwd, WEB)");
    }

    /**
     * 标准令牌校验走统一入口
     */
    @Test
    @DisplayName("verify: 委托令牌解析器")
    void shouldVerifyToken() {
        UserResume resume = new UserResume();
        FakeTokenResolver resolver = fakeTokenResolver();
        resolver.resolveResult = new FakeTokenResolver.ResolveResult<>(true, resume);

        assertSame(resume, authCoreService.verify("cookie", "token").getData());

        resolver.assertRecorded("resolve(cookie=cookie, token=token)");
    }

    /**
     * 解析器缺失时 verify 返回错误而不是 NPE
     */
    @Test
    @DisplayName("verify: 解析器缺失返回错误")
    void shouldFailVerifyWhenResolverMissing() {
        loginCheck.setTokenResolver(null);

        assertFalse(authCoreService.verify("cookie", "token").isOk());
        assertEquals("认证服务器无法认证", authCoreService.verify("cookie", "token").getMsg());
    }

    /**
     * refresh 升级
     */
    @Test
    @DisplayName("upgrade(refresh): 走 refresh 分支")
    void shouldUpgradeRefresh() {
        assertTrue(authCoreService.upgrade("refresh", "t", "r").isOk());

        FakeTokenResolver resolver = fakeTokenResolver();
        resolver.assertRecorded("upgradeForRefresh(t, r)");
        resolver.assertNotRecorded("upgradeForVersion(t, r)");
        resolver.assertNotRecorded("upgradeForTimestamp(t, r)");
    }

    /**
     * 版本升级大小写不敏感
     */
    @Test
    @DisplayName("upgrade(VeRsIoN): 大小写不敏感")
    void shouldUpgradeVersionIgnoreCase() {
        assertTrue(authCoreService.upgrade("VeRsIoN", "t", "r").isOk());

        FakeTokenResolver resolver = fakeTokenResolver();
        resolver.assertRecorded("upgradeForVersion(t, r)");
        resolver.assertNotRecorded("upgradeForTimestamp(t, r)");
    }

    /**
     * 未知/空类型回落时间戳升级，保持历史行为
     */
    @Test
    @DisplayName("upgrade(null): 回落 TIMESTAMP")
    void shouldFallbackToTimestamp() {
        assertTrue(authCoreService.upgrade(null, "t", "r").isOk());

        FakeTokenResolver resolver = fakeTokenResolver();
        resolver.assertRecorded("upgradeForTimestamp(t, r)");
        resolver.assertNotRecorded("upgradeForVersion(t, r)");
    }

    /**
     * 有 uid 时按 uid + 类型处理
     */
    @Test
    @DisplayName("logout(uid): 按 uid + 类型")
    void shouldLogoutByUid() {
        assertTrue(authCoreService.logout("42", LogoutType.KICK, "tok").isOk());

        FakeTokenResolver resolver = fakeTokenResolver();
        resolver.assertRecorded("logout(uid=42, KICK)");
        resolver.assertNotRecorded("logout(token=tok)");
    }

    /**
     * 只有令牌时按单令牌登出
     */
    @Test
    @DisplayName("logout(token): 按单令牌")
    void shouldLogoutByToken() {
        assertTrue(authCoreService.logout(null, LogoutType.LOGOUT, "tok").isOk());

        FakeTokenResolver resolver = fakeTokenResolver();
        resolver.assertRecorded("logout(token=tok)");
        resolver.assertNotRecorded("logout(uid=42, KICK)");
    }

    /**
     * 令牌解析器不可用时返回错误，不抛 NPE
     */
    @Test
    @DisplayName("解析器缺失: 返回错误")
    void shouldFailWhenResolverMissing() {
        loginCheck.setTokenResolver(null);

        assertFalse(authCoreService.logout("42", LogoutType.KICK, null).isOk());
        assertEquals("认证服务器无法认证", authCoreService.logout("42", LogoutType.KICK, null).getMsg());
        assertFalse(authCoreService.upgrade("refresh", "t", "r").isOk());
    }

    /**
     * doLogin 的结果原样透传
     */
    @Test
    @DisplayName("login: 透传登录失败结果")
    void shouldPassLoginFailure() {
        loginCheck.doLoginResult = ReturnResult.error("用户名或密码错误");

        assertFalse(authCoreService.login("1.2.3.4", "alice", "pwd", "WEB", null).isOk());
    }

    /**
     * 方便取到当前使用的解析器桩
     *
     * @return 解析器桩
     */
    private FakeTokenResolver fakeTokenResolver() {
        return (FakeTokenResolver) loginCheck.resolveTokenResolver("redis");
    }
}
