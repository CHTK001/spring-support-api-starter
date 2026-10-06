package com.chua.starter.oauth.sso.support.protocol;

import com.chua.starter.oauth.sso.support.properties.ThirdPartyLoginProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SystemCallbackPolicy} 回归测试
 *
 * <p>守护 P0-3 的开放重定向修复。每条「拒绝」断言都对应一种真实可用的绕过手法，
 * 只要有人把白名单判定改回「以 / 开头就算相对路径」或「不校验域名」，测试立刻红。</p>
 */
@DisplayName("SystemCallbackPolicy: 系统回调地址白名单")
class SystemCallbackPolicyTest {

    /**
     * 构造白名单为空（默认）的策略
     *
     * @return 策略
     */
    private SystemCallbackPolicy openPolicy() {
        return new SystemCallbackPolicy(new ThirdPartyLoginProperties());
    }

    /**
     * 构造指定白名单的策略
     *
     * @param hosts 白名单
     * @return 策略
     */
    private SystemCallbackPolicy policyWith(String... hosts) {
        ThirdPartyLoginProperties properties = new ThirdPartyLoginProperties();
        properties.setAllowedSystemCallbackHosts(List.of(hosts));
        return new SystemCallbackPolicy(properties);
    }

    @Test
    @DisplayName("站内相对路径放行")
    void relativePathsAllowed() {
        SystemCallbackPolicy policy = openPolicy();
        assertEquals("/home", policy.sanitize("/home"));
        assertEquals("/login?from=gitee", policy.sanitize("/login?from=gitee"));
        assertNull(policy.rejectReason("/home"));
    }

    @Test
    @DisplayName("P0-3 核心反例：任意外部绝对地址默认一律拒绝")
    void absoluteExternalUrlsRejectedByDefault() {
        SystemCallbackPolicy policy = openPolicy();
        for (String evil : List.of(
                "https://evil.tld",
                "http://evil.tld/steal",
                "https://evil.tld/#",
                "HTTPS://EVIL.TLD")) {
            assertEquals("/", policy.sanitize(evil), "外部地址必须被拒绝: " + evil);
            assertNotNull(policy.rejectReason(evil));
        }
    }

    @Test
    @DisplayName("协议相对 URL 与反斜杠绕过必须被拒绝")
    void protocolRelativeAndBackslashBypassesRejected() {
        SystemCallbackPolicy policy = openPolicy();
        for (String bypass : List.of(
                "//evil.tld",
                "///evil.tld",
                "/\\evil.tld",
                "\\\\evil.tld",
                "/\\/evil.tld")) {
            assertEquals("/", policy.sanitize(bypass), "协议相对 URL 必须被拒绝: " + bypass);
            assertNotNull(policy.rejectReason(bypass));
        }
    }

    @Test
    @DisplayName("非 http(s) 协议一律拒绝")
    void nonHttpSchemesRejected() {
        SystemCallbackPolicy policy = policyWith("https://app.example.com", "javascript:", "data:");
        for (String url : List.of(
                "javascript:alert(1)",
                "data:text/html,<script>alert(1)</script>",
                "file:///etc/passwd",
                "ftp://app.example.com/x")) {
            assertEquals("/", policy.sanitize(url), "非 http(s) 协议必须被拒绝: " + url);
        }
        // 即便原样写进白名单也无效 —— scheme 校验在白名单比对之前
        assertNotNull(policy.rejectReason("javascript:alert(1)"));
    }

    @Test
    @DisplayName("白名单按 scheme://host[:port] 前缀匹配，路径与查询被忽略")
    void allowlistMatchesOriginOnly() {
        SystemCallbackPolicy policy = policyWith("https://app.example.com", "http://localhost:5173");

        assertEquals("https://app.example.com/home", policy.sanitize("https://app.example.com/home"));
        assertEquals("http://localhost:5173/#/dashboard",
                policy.sanitize("http://localhost:5173/#/dashboard"));
        // 端口必须一致：同 host 不同端口视为不同源
        assertEquals("/", policy.sanitize("https://app.example.com:8443/home"));
        // host 后缀匹配会被拒绝，不能用 app.example.com.evil.tld 绕过
        assertEquals("/", policy.sanitize("https://app.example.com.evil.tld/home"));
        assertEquals("/", policy.sanitize("https://evil.tld/home"));
    }

    @Test
    @DisplayName("大小写与末尾斜杠不影响白名单判定")
    void allowlistNormalizesCaseAndTrailingSlash() {
        SystemCallbackPolicy policy = policyWith("HTTPS://App.Example.COM/");
        assertEquals("https://App.Example.COM/home", policy.sanitize("https://App.Example.COM/home"));
    }

    @Test
    @DisplayName("控制字符 / 空白一律拒绝（防 Location 头注入）")
    void controlCharactersRejected() {
        SystemCallbackPolicy policy = policyWith("https://app.example.com");
        for (String injected : List.of(
                "/home\r\nSet-Cookie: admin=1",
                "/home\nLocation: https://evil.tld",
                "/ho me")) {
            assertEquals("/", policy.sanitize(injected), "含控制字符的地址必须被拒绝: " + injected);
        }
    }

    @Test
    @DisplayName("空值归一化为站内根路径，不拒绝")
    void blankFallsBackToRoot() {
        SystemCallbackPolicy policy = openPolicy();
        assertEquals("/", policy.sanitize(null));
        assertEquals("/", policy.sanitize(""));
        assertEquals("/", policy.sanitize("   "));
        assertNull(policy.rejectReason(null));
    }

    @Test
    @DisplayName("Cookie 域只认显式配置，未配置返回 null")
    void cookieDomainOnlyFromConfig() {
        ThirdPartyLoginProperties properties = new ThirdPartyLoginProperties();
        // 默认没有 callback 参数可派生 → null，即不设置 Domain
        assertNull(new SystemCallbackPolicy(properties).cookieDomain());

        properties.setCookieDomain("  app.example.com ");
        assertEquals("app.example.com", new SystemCallbackPolicy(properties).cookieDomain());

        properties.setCookieDomain("   ");
        assertNull(new SystemCallbackPolicy(properties).cookieDomain());
    }

    @Test
    @DisplayName("拒绝原因必须可读，便于运维告警定位")
    void rejectReasonIsActionable() {
        SystemCallbackPolicy policy = openPolicy();
        String reason = policy.rejectReason("//evil.tld");
        assertTrue(reason != null && reason.contains("开放重定向"), "拒绝原因需点明成因: " + reason);
    }
}