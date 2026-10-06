package com.chua.starter.oauth.sso.support.security;

import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OauthAdminAuthFilter} 回归测试
 *
 * <p>守护 P0-1：{@code /oauth/admin/**} 此前完全无鉴权，一次未授权的 POST 就能
 * {@code sign-verify=false} 摘掉所有协议入口的请求真实性控制。</p>
 */
@DisplayName("OauthAdminAuthFilter: 管理控制台鉴权")
class OauthAdminAuthFilterTest {

    /**
     * 安全配置
     */
    private OauthSecurityProperties properties;

    /**
     * 记录是否被放行
     */
    private static final class RecordingChain implements FilterChain {
        private boolean invoked;
        private MockHttpServletResponse response;

        @Override
        public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse resp) {
            invoked = true;
            response = (MockHttpServletResponse) resp;
        }
    }

    @BeforeEach
    void setUp() {
        properties = new OauthSecurityProperties();
        properties.getAdmin().setToken("s3cret-admin-token-value");
    }

    /**
     * 构造管理面请求
     *
     * @param path 逻辑路径
     * @return 请求
     */
    private MockHttpServletRequest adminRequest(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/oauth" + path);
        request.setContextPath("/oauth");
        request.setRemoteAddr("10.0.0.5");
        return request;
    }

    @Test
    @DisplayName("P0-1 核心反例：未授权请求不得进入管理接口")
    void unauthenticatedRequestIsRejected() throws Exception {
        MockHttpServletRequest request = adminRequest("/admin/sign-verify");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingChain chain = new RecordingChain();

        new OauthAdminAuthFilter(properties).doFilter(request, response, chain);

        assertFalse(chain.invoked, "未授权请求不得触达管理接口");
        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("P0-1 核心反例：错误令牌不得进入管理接口")
    void wrongTokenIsRejected() throws Exception {
        MockHttpServletRequest request = adminRequest("/admin/sign-verify");
        request.addHeader("X-OAuth-Admin-Token", "wrong-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingChain chain = new RecordingChain();

        new OauthAdminAuthFilter(properties).doFilter(request, response, chain);

        assertFalse(chain.invoked);
        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("fail-closed：未配置管理令牌时拒绝一切，而不是放行")
    void missingTokenConfigurationIsFailClosed() throws Exception {
        properties.getAdmin().setToken(null);
        MockHttpServletRequest request = adminRequest("/admin/status");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingChain chain = new RecordingChain();

        new OauthAdminAuthFilter(properties).doFilter(request, response, chain);

        assertFalse(chain.invoked, "未配置令牌必须拒绝，绝不能退化为无令牌即放行");
        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("携带正确令牌时放行")
    void correctTokenIsAllowed() throws Exception {
        MockHttpServletRequest request = adminRequest("/admin/sign-verify");
        request.addHeader("X-OAuth-Admin-Token", "s3cret-admin-token-value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingChain chain = new RecordingChain();

        new OauthAdminAuthFilter(properties).doFilter(request, response, chain);

        assertTrue(chain.invoked, "正确令牌必须放行");
    }

    @Test
    @DisplayName("支持 Authorization: Bearer 携带令牌")
    void bearerTokenIsAccepted() throws Exception {
        MockHttpServletRequest request = adminRequest("/admin/rate-limit");
        request.addHeader("Authorization", "Bearer s3cret-admin-token-value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingChain chain = new RecordingChain();

        new OauthAdminAuthFilter(properties).doFilter(request, response, chain);

        assertTrue(chain.invoked);
    }

    @Test
    @DisplayName("非管理路径不受影响")
    void nonAdminPathsAreNotBlocked() throws Exception {
        MockHttpServletRequest request = adminRequest("/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingChain chain = new RecordingChain();

        new OauthAdminAuthFilter(properties).doFilter(request, response, chain);

        assertTrue(chain.invoked, "/login 不应被管理鉴权拦截");
    }

    @Test
    @DisplayName("管理路径锚定匹配：/administrator 不得被当成 /admin")
    void adminPathMatchingIsAnchored() throws Exception {
        MockHttpServletRequest request = adminRequest("/administrator");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingChain chain = new RecordingChain();

        new OauthAdminAuthFilter(properties).doFilter(request, response, chain);

        // 确实不在 /admin/** 之内 → 不拦截（由其它层负责鉴权）
        assertTrue(chain.invoked);
    }

    @Test
    @DisplayName("IP 白名单：非白名单来源被拒")
    void ipWhitelistIsEnforced() throws Exception {
        properties.getAdmin().setIpWhitelist(java.util.List.of("192.168.1.0/24"));
        MockHttpServletRequest request = adminRequest("/admin/status");
        request.addHeader("X-OAuth-Admin-Token", "s3cret-admin-token-value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingChain chain = new RecordingChain();

        new OauthAdminAuthFilter(properties).doFilter(request, response, chain);

        assertFalse(chain.invoked);
        assertEquals(403, response.getStatus());
    }

    @Test
    @DisplayName("IP 白名单：CIDR 命中放行")
    void ipWhitelistCidrMatch() throws Exception {
        properties.getAdmin().setIpWhitelist(java.util.List.of("192.168.1.0/24"));
        MockHttpServletRequest request = adminRequest("/admin/status");
        request.setRemoteAddr("192.168.1.77");
        request.addHeader("X-OAuth-Admin-Token", "s3cret-admin-token-value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingChain chain = new RecordingChain();

        new OauthAdminAuthFilter(properties).doFilter(request, response, chain);

        assertTrue(chain.invoked);
    }

    @Test
    @DisplayName("响应体不得泄露令牌明文")
    void responseDoesNotLeakToken() throws Exception {
        MockHttpServletRequest request = adminRequest("/admin/status");
        request.addHeader("X-OAuth-Admin-Token", "s3cret-admin-token-value-but-wrong");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new OauthAdminAuthFilter(properties).doFilter(request, response, new RecordingChain());

        assertNotEquals(-1, response.getStatus());
        assertFalse(response.getContentAsString().contains("s3cret-admin-token"),
                "响应体不得回显令牌: " + response.getContentAsString());
    }
}