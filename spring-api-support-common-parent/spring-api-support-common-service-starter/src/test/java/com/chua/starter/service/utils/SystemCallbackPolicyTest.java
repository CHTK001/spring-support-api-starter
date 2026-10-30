package com.chua.starter.service.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link SystemCallbackPolicy} 回归测试（镜像 oauth-starter 同名策略的判定口径）
 *
 * <p>守护 {@code /v2/third/login/*} 链路的开放重定向修复。每条「拒绝」断言都对应
 * 一种真实可用的绕过手法（协议相对 URL、后缀域名、端口跨源、控制字符注入），
 * 只要有人放宽判定，测试立刻红。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("SystemCallbackPolicy: 系统回调地址白名单")
class SystemCallbackPolicyTest {

    /**
     * 白名单为空（默认 fail-closed）的白名单参数
     *
     * @return 空白名单
     */
    private List<String> noHosts() {
        return List.of();
    }

    /**
     * 构造指定白名单
     *
     * @param hosts 白名单
     * @return 白名单
     */
    private List<String> hosts(String... hosts) {
        return List.of(hosts);
    }

    @Test
    @DisplayName("站内相对路径放行")
    void relativePathsAllowed() {
        assertEquals("/home", SystemCallbackPolicy.sanitize("/home", noHosts()));
        assertEquals("/login?from=gitee", SystemCallbackPolicy.sanitize("/login?from=gitee", noHosts()));
        assertNull(SystemCallbackPolicy.rejectReason("/home", noHosts()));
        assertEquals("/home", SystemCallbackPolicy.sanitizeStructure("/home"));
    }

    @Test
    @DisplayName("P0-3 核心反例：任意外部绝对地址默认一律拒绝")
    void absoluteExternalUrlsRejectedByDefault() {
        for (String evil : List.of(
                "https://evil.tld",
                "http://evil.tld/steal",
                "https://evil.tld/#",
                "HTTPS://EVIL.TLD")) {
            assertEquals("/", SystemCallbackPolicy.sanitize(evil, noHosts()), "外部地址必须被拒绝: " + evil);
            assertNotNull(SystemCallbackPolicy.rejectReason(evil, noHosts()));
        }
    }

    @Test
    @DisplayName("协议相对 URL 与反斜杠绕过必须被拒绝")
    void protocolRelativeAndBackslashBypassesRejected() {
        for (String bypass : List.of(
                "//evil.tld",
                "///evil.tld",
                "/\\evil.tld",
                "\\\\evil.tld",
                "/\\/evil.tld")) {
            assertEquals("/", SystemCallbackPolicy.sanitize(bypass, noHosts()), "协议相对 URL 必须被拒绝: " + bypass);
            assertNotNull(SystemCallbackPolicy.rejectReason(bypass, noHosts()));
            assertEquals("/", SystemCallbackPolicy.sanitizeStructure(bypass), "结构消毒也必须拒绝: " + bypass);
        }
    }

    @Test
    @DisplayName("非 http(s) 协议一律拒绝（即便写进白名单也无效）")
    void nonHttpSchemesRejected() {
        List<String> allow = hosts("https://app.example.com", "javascript:", "data:", "ftp://app.example.com");
        for (String url : List.of(
                "javascript:alert(1)",
                "data:text/html,<script>alert(1)</script>",
                "file:///etc/passwd",
                "ftp://app.example.com/x")) {
            assertEquals("/", SystemCallbackPolicy.sanitize(url, allow), "非 http(s) 协议必须被拒绝: " + url);
            assertNotNull(SystemCallbackPolicy.rejectReason(url, allow));
            assertEquals("/", SystemCallbackPolicy.sanitizeStructure(url), "结构消毒必须拒绝非 http(s): " + url);
        }
        assertNotNull(SystemCallbackPolicy.rejectReason("javascript:alert(1)", allow));
    }

    @Test
    @DisplayName("白名单按归一化 scheme://host[:port] 精确匹配，路径与查询被忽略")
    void allowlistMatchesOriginExactly() {
        List<String> allow = hosts("https://app.example.com", "http://localhost:5173");

        assertEquals("https://app.example.com/home",
                SystemCallbackPolicy.sanitize("https://app.example.com/home", allow));
        assertEquals("http://localhost:5173/#/dashboard",
                SystemCallbackPolicy.sanitize("http://localhost:5173/#/dashboard", allow));
        // 端口必须一致：同 host 不同端口视为不同源
        assertEquals("/", SystemCallbackPolicy.sanitize("https://app.example.com:8443/home", allow));
        // 后缀域名不能命中：app.example.com.evil.tld 不是 app.example.com
        assertEquals("/", SystemCallbackPolicy.sanitize("https://app.example.com.evil.tld/home", allow));
        assertEquals("/", SystemCallbackPolicy.sanitize("https://evil.tld/home", allow));
        // 协议必须一致：https 白名单不放行 http（反向同理）
        assertEquals("/", SystemCallbackPolicy.sanitize("http://app.example.com/home", hosts("https://app.example.com")));
    }

    @Test
    @DisplayName("大小写与末尾斜杠不影响白名单判定")
    void allowlistNormalizesCaseAndTrailingSlash() {
        List<String> allow = hosts("HTTPS://App.Example.COM/");
        assertEquals("https://App.Example.COM/home",
                SystemCallbackPolicy.sanitize("https://App.Example.COM/home", allow));
        assertNull(SystemCallbackPolicy.rejectReason("https://App.Example.COM/home", allow));
    }

    @Test
    @DisplayName("控制字符 / 空白一律拒绝（防 Location 头注入）")
    void controlCharactersRejected() {
        List<String> allow = hosts("https://app.example.com");
        for (String injected : List.of(
                "/home\r\nSet-Cookie: admin=1",
                "/home\nLocation: https://evil.tld",
                "/ho me")) {
            assertEquals("/", SystemCallbackPolicy.sanitize(injected, allow), "含控制字符的地址必须被拒绝: " + injected);
            assertNotNull(SystemCallbackPolicy.rejectReason(injected, allow));
            assertEquals("/", SystemCallbackPolicy.sanitizeStructure(injected), "结构消毒必须拒绝控制字符: " + injected);
        }
    }

    @Test
    @DisplayName("空值归一化为站内根路径，不拒绝")
    void blankFallsBackToRoot() {
        assertEquals("/", SystemCallbackPolicy.sanitize(null, noHosts()));
        assertEquals("/", SystemCallbackPolicy.sanitize("", noHosts()));
        assertEquals("/", SystemCallbackPolicy.sanitize("   ", noHosts()));
        assertEquals("/", SystemCallbackPolicy.sanitizeStructure(null));
        assertNull(SystemCallbackPolicy.rejectReason(null, noHosts()));
        assertNull(SystemCallbackPolicy.rejectReason("   ", noHosts()));
    }

    @Test
    @DisplayName("拒绝原因必须可读，便于运维告警定位")
    void rejectReasonIsActionable() {
        String reason = SystemCallbackPolicy.rejectReason("//evil.tld", noHosts());
        assertNotNull(reason);
        assertEquals(true, reason.contains("开放重定向"), "拒绝原因需点明成因: " + reason);
        String reason2 = SystemCallbackPolicy.rejectReason("https://evil.tld", noHosts());
        assertNotNull(reason2);
        assertEquals(true, reason2.contains("allowed-system-callback-hosts"), "空白名单需指明配置项: " + reason2);
    }

    @Test
    @DisplayName("sanitizeStructure 只做结构消毒：绝对 http(s) 地址原样保留（白名单推迟到读取点）")
    void sanitizeStructureDefersAllowlist() {
        // 写入点没有 plugin.system.security 配置上下文，故结构合法的绝对地址保留，
        // 白名单强校验由入口 rejectReason 与读取点 sanitize 完成
        assertEquals("https://evil.tld", SystemCallbackPolicy.sanitizeStructure("https://evil.tld"));
        assertEquals("http://localhost:8096/#/login",
                SystemCallbackPolicy.sanitizeStructure("http://localhost:8096/#/login"));
        // 但结构非法的绝对地址（非 http(s)、无 authority）在写入点即拒绝
        assertEquals("/", SystemCallbackPolicy.sanitizeStructure("javascript:alert(1)"));
        assertEquals("/", SystemCallbackPolicy.sanitizeStructure("https:evil.tld"));
        assertEquals("/", SystemCallbackPolicy.sanitizeStructure(""));
        assertEquals("/", SystemCallbackPolicy.sanitizeStructure("   "));
    }
}
