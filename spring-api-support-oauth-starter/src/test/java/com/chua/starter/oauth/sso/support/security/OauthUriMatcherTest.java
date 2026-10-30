package com.chua.starter.oauth.sso.support.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OauthUriMatcher} 回归测试
 *
 * <p>本类守护的是 P0-2 的修复：原实现用 {@code uri.contains(pattern)} 判定豁免，
 * 而服务上下文路径恰为 {@code /oauth}，导致默认豁免项 {@code "/oauth"} 命中所有请求，
 * CSRF 校验被整体短路。下面每一条断言都是针对这一失效模式的反例。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("OauthUriMatcher: 剥离上下文路径后的锚定 Ant 匹配")
class OauthUriMatcherTest {

    /**
     * 构造带上下文路径的请求
     *
     * @param uri 逻辑路径
     * @return 请求
     */
    private HttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/oauth" + uri);
        request.setContextPath("/oauth");
        return request;
    }

    @Test
    @DisplayName("P0-2 核心反例：默认豁免项 /oauth 不得命中任何业务请求")
    void contextPathMustNotSelfMatch() {
        // 这是修复前必然发生的事：/oauth/admin/status contains "/oauth" == true
        List<String> legacyIgnorePaths = List.of("/oauth", "/temporary-token", "/actuator");

        assertFalse(OauthUriMatcher.matchesAny(
                        OauthUriMatcher.logicalPath(request("/admin/status")), legacyIgnorePaths),
                "上下文路径 /oauth 不得被当成豁免路径");
        assertFalse(OauthUriMatcher.matchesAny(
                        OauthUriMatcher.logicalPath(request("/login")), legacyIgnorePaths),
                "上下文路径 /oauth 不得被当成豁免路径");
        assertFalse(OauthUriMatcher.matchesAny(
                        OauthUriMatcher.logicalPath(request("/verify")), legacyIgnorePaths),
                "上下文路径 /oauth 不得被当成豁免路径");
    }

    @Test
    @DisplayName("真实豁免路径仍然命中，不破坏三方平台回调")
    void realIgnorePathsStillMatch() {
        List<String> defaults = List.of("/actuator/**", "/oauth2/callback/**", "/gitee/**", "/health");

        assertTrue(OauthUriMatcher.matchesAny(
                OauthUriMatcher.logicalPath(request("/oauth2/callback/gitee")), defaults));
        assertTrue(OauthUriMatcher.matchesAny(
                OauthUriMatcher.logicalPath(request("/actuator/health")), defaults));
        assertTrue(OauthUriMatcher.matchesAny(
                OauthUriMatcher.logicalPath(request("/gitee/callback")), defaults));
        assertFalse(OauthUriMatcher.matchesAny(
                OauthUriMatcher.logicalPath(request("/oauth2/authorize/gitee")), defaults));
    }

    @Test
    @DisplayName("锚定匹配：不得出现子串误伤")
    void anchoredMatchingRejectsSubstringHits() {
        List<String> patterns = List.of("/admin");

        // contains 语义下这两个都会命中；锚定语义下必须不命中
        assertFalse(OauthUriMatcher.matchesAny("/administrator", patterns));
        assertFalse(OauthUriMatcher.matchesAny("/foo/admin", patterns));
        assertTrue(OauthUriMatcher.matchesAny("/admin", patterns));
        assertTrue(OauthUriMatcher.matchesAny("/admin/login/reset", patterns) == false);
    }

    @Test
    @DisplayName("上下文路径剥离与归一化")
    void logicalPathNormalization() {
        assertEquals("/admin/status", OauthUriMatcher.logicalPath(request("/admin/status")));
        // 请求 URI 等于上下文路径本身
        assertEquals("/", OauthUriMatcher.stripContextPath("/oauth", "/oauth"));
        // 末尾斜杠归一化，避免 /admin/ 与 /admin 行为不一致
        assertEquals("/admin", OauthUriMatcher.stripContextPath("/oauth/admin/", "/oauth"));
        // 无上下文路径时原样返回
        assertEquals("/admin", OauthUriMatcher.stripContextPath("/admin", ""));
        assertEquals("/admin", OauthUriMatcher.stripContextPath("/admin", "/"));
        // 空输入
        assertEquals("/", OauthUriMatcher.stripContextPath("", "/oauth"));
    }

    @Test
    @DisplayName("限流范围判定：随机路径不再撑爆桶缓存")
    void randomPathsDoNotShareBucket() {
        List<String> limited = List.of("/login", "/temporary-token", "/upgrade", "/verify", "/admin/**");

        assertTrue(OauthUriMatcher.matchesAny(OauthUriMatcher.logicalPath(request("/login")), limited));
        assertTrue(OauthUriMatcher.matchesAny(
                OauthUriMatcher.logicalPath(request("/admin/rate-limit")), limited));
        assertFalse(OauthUriMatcher.matchesAny(
                OauthUriMatcher.logicalPath(request("/random-attack-path")), limited));
        // 修复前 contains("/oauth") 恒真，导致限流范围判定完全失控
        assertFalse(OauthUriMatcher.matchesAny(
                OauthUriMatcher.logicalPath(request("/random-attack-path")), List.of("/oauth")));
    }

    @Test
    @DisplayName("空模式列表不豁免任何路径（fail-closed）")
    void emptyPatternListMatchesNothing() {
        assertFalse(OauthUriMatcher.matchesAny("/login", List.of()));
        assertFalse(OauthUriMatcher.matchesAny("/login", null));
    }
}