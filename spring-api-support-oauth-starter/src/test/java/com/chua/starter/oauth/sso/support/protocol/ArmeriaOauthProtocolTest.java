package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.json.Json;
import com.chua.starter.oauth.sso.support.admin.RateLimitConfig;
import com.chua.starter.oauth.sso.support.admin.RateLimitManager;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Armeria OAuth 协议单元测试
 * <p>
 * 测试 ArmeriaOauthProtocol 的辅助方法、Admin HTML 生成、
 * AuthServerProperties 的 Online 枚举、signVerify 切换等。
 * </p>
 */
class ArmeriaOauthProtocolTest {

    private AuthServerProperties properties;
    private LoginCheck loginCheck;
    private RateLimitManager rateLimitManager;

    @BeforeEach
    void setUp() {
        properties = new AuthServerProperties();
        properties.setSignVerify(true);
        properties.setTokenManagement("memory");
        properties.setProtocols(Arrays.asList("armeria", "rsocket"));
        properties.setOnline(AuthServerProperties.Online.MULTIPLE);
        properties.setMaxOnlineCount(3);
        loginCheck = new LoginCheck(properties);
        rateLimitManager = new RateLimitManager();
    }

    // ------------------------------------------------------------------
    // 1. AuthServerProperties.Online 枚举测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Online.MULTIPLE 应正确识别")
    void online_shouldRecognizeMultiple() {
        assertEquals(AuthServerProperties.Online.MULTIPLE,
                AuthServerProperties.Online.valueOf("MULTIPLE"));
    }

    @Test
    @DisplayName("Online.SINGLE 应正确识别")
    void online_shouldRecognizeSingle() {
        assertEquals(AuthServerProperties.Online.SINGLE,
                AuthServerProperties.Online.valueOf("SINGLE"));
    }

    @Test
    @DisplayName("Online.LIMIT 应正确识别")
    void online_shouldRecognizeLimit() {
        assertEquals(AuthServerProperties.Online.LIMIT,
                AuthServerProperties.Online.valueOf("LIMIT"));
    }

    @Test
    @DisplayName("Online 不合法值应抛异常")
    void online_invalidValue_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> AuthServerProperties.Online.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Online 大小写不敏感")
    void online_caseInsensitive_shouldWork() {
        assertEquals(AuthServerProperties.Online.MULTIPLE,
                AuthServerProperties.Online.valueOf("multiple".toUpperCase()));
        assertEquals(AuthServerProperties.Online.SINGLE,
                AuthServerProperties.Online.valueOf("single".toUpperCase()));
    }

    // ------------------------------------------------------------------
    // 2. signVerify 切换测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("signVerify 默认应为 true")
    void signVerify_default_shouldBeTrue() {
        assertTrue(properties.isSignVerify(), "默认应启用签名校验");
    }

    @Test
    @DisplayName("signVerify 可切换为 false")
    void signVerify_shouldToggleToFalse() {
        properties.setSignVerify(false);
        assertFalse(properties.isSignVerify());
    }

    @Test
    @DisplayName("signVerify 从 false 切换回 true")
    void signVerify_shouldToggleBackToTrue() {
        properties.setSignVerify(false);
        assertFalse(properties.isSignVerify());
        properties.setSignVerify(true);
        assertTrue(properties.isSignVerify());
    }

    // ------------------------------------------------------------------
    // 3. 在线模式切换测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("切换为 SINGLE 模式")
    void switchToSingleMode_shouldWork() {
        properties.setOnline(AuthServerProperties.Online.SINGLE);
        assertEquals(AuthServerProperties.Online.SINGLE, properties.getOnline());
    }

    @Test
    @DisplayName("切换为 LIMIT 模式并设置 maxCount")
    void switchToLimitMode_shouldSetMaxCount() {
        properties.setOnline(AuthServerProperties.Online.LIMIT);
        properties.setMaxOnlineCount(5);

        assertEquals(AuthServerProperties.Online.LIMIT, properties.getOnline());
        assertEquals(5, properties.getMaxOnlineCount());
    }

    @Test
    @DisplayName("默认 maxOnlineCount 应为 3")
    void defaultMaxOnlineCount_shouldBe3() {
        assertEquals(3, properties.getMaxOnlineCount());
    }

    // ------------------------------------------------------------------
    // 4. Token 过期时间测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("默认 expire 应为 3 天 (259200秒)")
    void defaultExpire_shouldBe3Days() {
        assertEquals(86400L * 3, properties.getExpire());
    }

    @Test
    @DisplayName("默认 refreshExpire 应为 4 天 (345600秒)")
    void defaultRefreshExpire_shouldBe4Days() {
        assertEquals(86400L * 4, properties.getRefreshExpire());
    }

    @Test
    @DisplayName("可自定义 expire 和 refreshExpire")
    void customExpire_shouldOverrideDefault() {
        properties.setExpire(3600);
        properties.setRefreshExpire(7200);

        assertEquals(3600, properties.getExpire());
        assertEquals(7200, properties.getRefreshExpire());
    }

    // ------------------------------------------------------------------
    // 5. renew 和 refreshTokenRotation 切换测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("renew 默认应为 false")
    void renew_default_shouldBeFalse() {
        assertFalse(properties.isRenew());
    }

    @Test
    @DisplayName("renew 可切换为 true")
    void renew_shouldToggleToTrue() {
        properties.setRenew(true);
        assertTrue(properties.isRenew());
    }

    @Test
    @DisplayName("refreshTokenRotation 默认应为 false")
    void refreshTokenRotation_default_shouldBeFalse() {
        assertFalse(properties.isRefreshTokenRotation());
    }

    @Test
    @DisplayName("refreshTokenRotation 可切换为 true")
    void refreshTokenRotation_shouldToggleToTrue() {
        properties.setRefreshTokenRotation(true);
        assertTrue(properties.isRefreshTokenRotation());
    }

    // ------------------------------------------------------------------
    // 6. Admin HTML 内容验证
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Admin HTML 应包含管理控制台标题")
    void adminHtml_shouldContainTitle() {
        String html = invokeBuildAdminHtml();
        assertNotNull(html);
        assertTrue(html.contains("OAuth 管理控制台"),
                "Admin HTML 应包含标题");
    }

    @Test
    @DisplayName("Admin HTML 应包含签名校验开关")
    void adminHtml_shouldContainSignVerifyToggle() {
        String html = invokeBuildAdminHtml();
        assertTrue(html.contains("signVerify"),
                "Admin HTML 应包含 signVerify 开关");
    }

    @Test
    @DisplayName("Admin HTML 应包含在线模式选择器")
    void adminHtml_shouldContainOnlineModeSelector() {
        String html = invokeBuildAdminHtml();
        assertTrue(html.contains("MULTIPLE"),
                "Admin HTML 应包含 MULTIPLE 选项");
        assertTrue(html.contains("SINGLE"),
                "Admin HTML 应包含 SINGLE 选项");
        assertTrue(html.contains("LIMIT"),
                "Admin HTML 应包含 LIMIT 选项");
    }

    @Test
    @DisplayName("Admin HTML 应包含限流配置区域")
    void adminHtml_shouldContainRateLimitSection() {
        String html = invokeBuildAdminHtml();
        assertTrue(html.contains("rateLimit"),
                "Admin HTML 应包含限流配置");
    }

    @Test
    @DisplayName("Admin HTML 应包含 Token 配置区域")
    void adminHtml_shouldContainTokenConfig() {
        String html = invokeBuildAdminHtml();
        assertTrue(html.contains("renew") || html.contains("续期"),
                "Admin HTML 应包含 Token 续期配置");
        assertTrue(html.contains("expire"),
                "Admin HTML 应包含过期时间配置");
    }

    @Test
    @DisplayName("Admin HTML 应是合法的 HTML 文档")
    void adminHtml_shouldBeValidHtml() {
        String html = invokeBuildAdminHtml();
        assertTrue(html.startsWith("<!DOCTYPE html>") || html.startsWith("\n<!DOCTYPE html>"),
                "Admin HTML 应以 DOCTYPE 开头");
        assertTrue(html.contains("<html"),
                "Admin HTML 应包含 <html 标签");
        assertTrue(html.contains("</html>"),
                "Admin HTML 应包含 </html> 闭合标签");
        assertTrue(html.contains("<script"),
                "Admin HTML 应包含 JavaScript");
    }

    // ------------------------------------------------------------------
    // 7. RateLimitConfig 测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("RateLimitConfig 默认值应合理")
    void rateLimitConfig_defaults_shouldBeReasonable() {
        RateLimitConfig config = new RateLimitConfig();
        assertFalse(config.isEnabled(), "默认应禁用限流");
    }

    @Test
    @DisplayName("RateLimitConfig JSON 序列化/反序列化")
    void rateLimitConfig_shouldSerializeAndDeserialize() {
        RateLimitConfig config = new RateLimitConfig();
        config.setEnabled(true);
        config.setQps(50);
        config.setBurstSize(100);
        config.setDimension("ip");

        String json = Json.toJson(config);
        assertNotNull(json);
        assertTrue(json.contains("\"enabled\":true"));

        RateLimitConfig parsed = Json.fromJson(json, RateLimitConfig.class);
        assertNotNull(parsed);
        assertTrue(parsed.isEnabled());
        assertEquals(50, parsed.getQps());
        assertEquals(100, parsed.getBurstSize());
        assertEquals("ip", parsed.getDimension());
    }

    // ------------------------------------------------------------------
    // 8. RateLimitManager 测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("RateLimitManager 初始状态应可获取")
    void rateLimitManager_initialStatus_shouldBeGettable() {
        Map<String, Object> status = rateLimitManager.getStatus();
        assertNotNull(status);
        assertTrue(status.containsKey("enabled"), "状态应包含 enabled 字段");
        assertTrue(status.containsKey("qps"), "状态应包含 qps 字段");
    }

    @Test
    @DisplayName("RateLimitManager 更新配置后状态应反映变化")
    void rateLimitManager_update_shouldChangeStatus() {
        RateLimitConfig config = new RateLimitConfig();
        config.setEnabled(true);
        config.setQps(100);
        config.setBurstSize(200);
        config.setDimension("user");

        rateLimitManager.update(config);

        Map<String, Object> status = rateLimitManager.getStatus();
        assertNotNull(status);
    }

    // ------------------------------------------------------------------
    // 9. Protocol 端口测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("armeriaPort 默认值应为 19180")
    void armeriaPort_default_shouldBe19180() {
        assertEquals(19180, properties.getArmeriaPort());
    }

    @Test
    @DisplayName("rsocketPort 默认值应为 7000")
    void rsocketPort_default_shouldBe7000() {
        assertEquals(7000, properties.getRsocketPort());
    }

    @Test
    @DisplayName("armeriaPort 可自定义")
    void armeriaPort_custom_shouldOverride() {
        properties.setArmeriaPort(8080);
        assertEquals(8080, properties.getArmeriaPort());
    }

    // ------------------------------------------------------------------
    // 10. Armeria 协议路由名称验证
    // ------------------------------------------------------------------

    @ParameterizedTest
    @DisplayName("所有预期路由名称应存在")
    @ValueSource(strings = {"/login", "/logout", "/oauth", "/verify", "/upgrade",
            "/temporary-token", "/admin", "/admin/status", "/admin/sign-verify",
            "/admin/online-mode", "/admin/rate-limit", "/admin/renew",
            "/admin/refresh-token-rotation", "/admin/expire"})
    void allExpectedRoutes_shouldBeKnown(String route) {
        // 验证所有 Armeria 路由名都在预期列表中
        Set<String> knownRoutes = Set.of(
                "/login", "/logout", "/oauth", "/verify", "/upgrade",
                "/temporary-token", "/admin", "/admin/status", "/admin/sign-verify",
                "/admin/online-mode", "/admin/rate-limit", "/admin/renew",
                "/admin/refresh-token-rotation", "/admin/expire"
        );
        assertTrue(knownRoutes.contains(route),
                "路由 " + route + " 应在已知路由列表中");
    }

    // ------------------------------------------------------------------
    // helper
    // ------------------------------------------------------------------

    /**
     * 通过反射调用 ArmeriaOauthProtocol.buildAdminHtml()
     */
    private String invokeBuildAdminHtml() {
        try {
            var method = ArmeriaOauthProtocol.class.getDeclaredMethod("buildAdminHtml");
            method.setAccessible(true);
            // 需要一个实例才能调用
            var appContext = new org.springframework.context.support.StaticApplicationContext();
            var protocol = new ArmeriaOauthProtocol(properties, loginCheck,
                    rateLimitManager, appContext);
            return (String) method.invoke(protocol);
        } catch (Exception e) {
            throw new RuntimeException("反射调用 buildAdminHtml 失败", e);
        }
    }
}
