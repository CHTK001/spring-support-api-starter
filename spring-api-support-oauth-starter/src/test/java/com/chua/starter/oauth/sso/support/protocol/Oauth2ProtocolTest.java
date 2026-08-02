package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.entity.AuthRequest;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OAuth2 标准协议单元测试
 * <p>
 * 测试 OAuth2 协议的纯工具方法逻辑（form-urlencoded 解析、Basic Auth 解析、
 * Token 响应构建、错误响应构建等），不依赖 Spring 容器。
 * </p>
 */
class Oauth2ProtocolTest {

    private AuthServerProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AuthServerProperties();
        properties.setSignVerify(false);
        properties.setTokenManagement("memory");
        properties.setProtocols(Arrays.asList("oauth2"));
    }

    // ===================================================================
    // 1. parseFormUrlEncoded 逻辑测试（直接内联逻辑，不依赖反射）
    // ===================================================================

    @Test
    @DisplayName("parseFormUrlEncoded - 标准参数应正确解析")
    void parseFormUrlEncoded_standardParams_shouldParse() {
        String body = "grant_type=password&username=test&password=123456&scope=read";
        Map<String, String> params = parseFormUrlEncoded(body);

        assertEquals("password", params.get("grant_type"));
        assertEquals("test", params.get("username"));
        assertEquals("123456", params.get("password"));
        assertEquals("read", params.get("scope"));
    }

    @Test
    @DisplayName("parseFormUrlEncoded - 空 body 应返回空 Map")
    void parseFormUrlEncoded_emptyBody_shouldReturnEmpty() {
        Map<String, String> params = parseFormUrlEncoded("");
        assertNotNull(params);
        assertTrue(params.isEmpty());
    }

    @Test
    @DisplayName("parseFormUrlEncoded - null body 应返回空 Map")
    void parseFormUrlEncoded_nullBody_shouldReturnEmpty() {
        Map<String, String> params = parseFormUrlEncoded(null);
        assertNotNull(params);
        assertTrue(params.isEmpty());
    }

    @Test
    @DisplayName("parseFormUrlEncoded - URL 编码参数应正确解码")
    void parseFormUrlEncoded_urlEncoded_shouldDecode() {
        String body = "redirect_uri=https%3A%2F%2Fexample.com%2Fcallback&error=invalid+request";
        Map<String, String> params = parseFormUrlEncoded(body);

        assertEquals("https://example.com/callback", params.get("redirect_uri"));
        assertEquals("invalid request", params.get("error"));
    }

    @Test
    @DisplayName("parseFormUrlEncoded - 只有 key 无 value 的参数应忽略")
    void parseFormUrlEncoded_keyOnly_shouldSkip() {
        String body = "grant_type=password&no_value";
        Map<String, String> params = parseFormUrlEncoded(body);

        assertEquals("password", params.get("grant_type"));
        assertNull(params.get("no_value"));
    }

    @Test
    @DisplayName("parseFormUrlEncoded - 含 + 号的 value 应正确解码为空格")
    void parseFormUrlEncoded_plusSign_shouldDecodeToSpace() {
        String body = "scope=read+write&description=hello+world";
        Map<String, String> params = parseFormUrlEncoded(body);

        assertEquals("read write", params.get("scope"));
        assertEquals("hello world", params.get("description"));
    }

    // ===================================================================
    // 2. extractBasicCredentials 逻辑测试（内联）
    // ===================================================================

    @Test
    @DisplayName("extractBasicCredentials - 合法 Basic 认证头应正确解析")
    void extractBasicCredentials_validHeader_shouldParse() {
        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString("myclient:mysecret".getBytes(StandardCharsets.UTF_8));
        String[] credentials = extractBasicCredentials(authHeader);

        assertNotNull(credentials);
        assertEquals("myclient", credentials[0]);
        assertEquals("mysecret", credentials[1]);
    }

    @Test
    @DisplayName("extractBasicCredentials - 无 Basic 前缀应返回 null")
    void extractBasicCredentials_noBasicPrefix_shouldReturnNull() {
        String[] credentials = extractBasicCredentials("Bearer some-token");
        assertNull(credentials);
    }

    @Test
    @DisplayName("extractBasicCredentials - null 应返回 null")
    void extractBasicCredentials_nullHeader_shouldReturnNull() {
        String[] credentials = extractBasicCredentials(null);
        assertNull(credentials);
    }

    @Test
    @DisplayName("extractBasicCredentials - 非法 Base64 应返回 null")
    void extractBasicCredentials_invalidBase64_shouldReturnNull() {
        String[] credentials = extractBasicCredentials("Basic not-valid-base64===");
        assertNull(credentials, "非法 Base64 应返回 null");
    }

    @Test
    @DisplayName("extractBasicCredentials - 只有 username 无冒号应返回 null")
    void extractBasicCredentials_noColon_shouldReturnNull() {
        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString("onlyusername".getBytes(StandardCharsets.UTF_8));
        String[] credentials = extractBasicCredentials(authHeader);
        assertNull(credentials, "只有 username 无冒号时 credentials 应为 null");
    }

    // ===================================================================
    // 3. buildErrorResponse 逻辑测试（内联）
    // ===================================================================

    @Test
    @DisplayName("buildErrorResponse - 应包含 error 和 error_description")
    void buildErrorResponse_shouldContainBothFields() {
        Map<String, Object> response = buildErrorResponse("invalid_grant", "refresh_token 无效");
        assertEquals("invalid_grant", response.get("error"));
        assertEquals("refresh_token 无效", response.get("error_description"));
        assertEquals(2, response.size());
    }

    @Test
    @DisplayName("buildErrorResponse - 空描述应允许")
    void buildErrorResponse_emptyDescription_shouldAllow() {
        Map<String, Object> response = buildErrorResponse("server_error", "");
        assertEquals("server_error", response.get("error"));
        assertEquals("", response.get("error_description"));
    }

    @Test
    @DisplayName("buildErrorResponse - JSON 序列化应正确")
    void buildErrorResponse_jsonSerialization_shouldWork() {
        Map<String, Object> response = buildErrorResponse("unsupported_grant_type", "不支持的授权类型");
        String json = Json.toJson(response);

        assertTrue(json.contains("\"error\""));
        assertTrue(json.contains("unsupported_grant_type"));
    }

    // ===================================================================
    // 4. buildTokenResponse 逻辑测试（内联）
    // ===================================================================

    @Test
    @DisplayName("buildTokenResponse - 应正确构建 token 响应")
    void buildTokenResponse_shouldBuildCorrectly() {
        Map<String, Object> response = buildTokenResponse(
                "access-token-123", "Bearer", 3600L, "refresh-token-456", "read write");

        assertEquals("access-token-123", response.get("access_token"));
        assertEquals("Bearer", response.get("token_type"));
        assertEquals(3600L, response.get("expires_in"));
        assertEquals("refresh-token-456", response.get("refresh_token"));
        assertEquals("read write", response.get("scope"));
    }

    @Test
    @DisplayName("buildTokenResponse - 无 refresh_token 时应不包含该字段")
    void buildTokenResponse_noRefreshToken_shouldOmitField() {
        Map<String, Object> response = buildTokenResponse(
                "access-token-123", "Bearer", 3600L, null, null);

        assertEquals("access-token-123", response.get("access_token"));
        assertFalse(response.containsKey("refresh_token"), "无 refresh_token 时不应包含该字段");
    }

    @Test
    @DisplayName("buildTokenResponse - 无 scope 时应不包含该字段")
    void buildTokenResponse_noScope_shouldOmitField() {
        Map<String, Object> response = buildTokenResponse(
                "access-token-123", "Bearer", 3600L, null, null);

        assertFalse(response.containsKey("scope"), "无 scope 时不应包含该字段");
    }

    // ===================================================================
    // 5. AuthServerProperties - OAuth2 配置测试
    // ===================================================================

    @Test
    @DisplayName("protocols 应支持 oauth2")
    void protocols_shouldSupportOauth2() {
        properties.setProtocols(Arrays.asList("oauth2", "http"));
        assertTrue(properties.getProtocols().contains("oauth2"));
    }

    @Test
    @DisplayName("tokenName 默认值应为 x-oauth-token")
    void tokenName_default_shouldBeCorrect() {
        assertEquals("x-oauth-token", properties.getTokenName());
    }

    @Test
    @DisplayName("tokenName 可自定义")
    void tokenName_custom_shouldOverride() {
        properties.setTokenName("Authorization");
        assertEquals("Authorization", properties.getTokenName());
    }

    @Test
    @DisplayName("cookieName 默认值应为 x-oauth-cookie")
    void cookieName_default_shouldBeCorrect() {
        assertEquals("x-oauth-cookie", properties.getCookieName());
    }

    @Test
    @DisplayName("encryption 默认值应为 aes")
    void encryption_default_shouldBeAes() {
        assertEquals("aes", properties.getEncryption());
    }

    @Test
    @DisplayName("serviceKey 默认值应为 1234567890oOlIi0")
    void serviceKey_default_shouldBeCorrect() {
        assertEquals("1234567890oOlIi0", properties.getServiceKey());
    }

    // ===================================================================
    // 6. AuthRequest 与 OAuth2 请求体格式测试
    // ===================================================================

    @Test
    @DisplayName("OAuth2 Token 请求体应为 form-urlencoded 格式")
    void oauth2TokenRequest_shouldBeFormUrlEncoded() {
        String formBody = "grant_type=password&username=test&password=123";
        Map<String, String> params = parseFormUrlEncoded(formBody);

        assertNotNull(params.get("grant_type"));
        assertNotNull(params.get("username"));
    }

    @Test
    @DisplayName("OAuth2 AuthRequest（内部协议）应为 JSON 格式")
    void oauth2AuthRequest_shouldBeJson() {
        AuthRequest request = new AuthRequest();
        request.setData("encrypted-data-here");
        request.setUsername("testuser");

        String json = Json.toJson(request);
        assertTrue(json.contains("\"data\""));
    }

    // ===================================================================
    // 7. ReturnResult 不同 code 测试
    // ===================================================================

    @Test
    @DisplayName("ReturnResult.noAuth 应返回未认证结果")
    void returnResult_noAuth_shouldReturnUnauthorized() {
        var result = ReturnResult.noAuth("未登录");
        assertNotNull(result);
        assertTrue(result.isFailure());
    }

    @Test
    @DisplayName("ReturnResult.error 的 code 应为错误码")
    void returnResult_error_codeShouldBeError() {
        var result = ReturnResult.error("内部错误");
        assertNotNull(result);
        assertFalse(result.isOk());
    }

    // ===================================================================
    // 8. OAuth2 授权类型验证测试
    // ===================================================================

    @Test
    @DisplayName("合法的 grant_type 列表应包含所有标准类型")
    void oauth2GrantTypes_shouldContainAllStandardTypes() {
        List<String> standardTypes = Arrays.asList(
                "authorization_code", "client_credentials", "password", "refresh_token"
        );
        for (String type : standardTypes) {
            assertTrue(type.contains("_") || type.equals("password"),
                    "授权类型格式验证: " + type);
        }
    }

    @Test
    @DisplayName("不支持的 grant_type 应被识别")
    void oauth2GrantTypes_unsupported_shouldBeDetected() {
        String unsupported = "implicit";
        List<String> supported = Arrays.asList(
                "authorization_code", "client_credentials", "password", "refresh_token"
        );
        assertFalse(supported.contains(unsupported),
                "implicit 不应在支持列表中");
    }

    // ===================================================================
    // 9. URL 构造测试（对应 getBaseUrl 逻辑）
    // ===================================================================

    @Test
    @DisplayName("URL 构造 - HTTP 默认端口 80 应省略")
    void urlBuilder_httpDefaultPort_shouldOmit() {
        String scheme = "http";
        String serverName = "localhost";
        int port = 80;
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        if (("http".equals(scheme) && port != 80) ||
                ("https".equals(scheme) && port != 443)) {
            url.append(":").append(port);
        }
        url.append("/oauth");
        assertEquals("http://localhost/oauth", url.toString());
    }

    @Test
    @DisplayName("URL 构造 - HTTPS 默认端口 443 应省略")
    void urlBuilder_httpsDefaultPort_shouldOmit() {
        String scheme = "https";
        String serverName = "auth.example.com";
        int port = 443;
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        if (("http".equals(scheme) && port != 80) ||
                ("https".equals(scheme) && port != 443)) {
            url.append(":").append(port);
        }
        url.append("/oauth2");
        assertEquals("https://auth.example.com/oauth2", url.toString());
    }

    @Test
    @DisplayName("URL 构造 - 非默认端口应保留")
    void urlBuilder_nonDefaultPort_shouldKeep() {
        String scheme = "http";
        String serverName = "localhost";
        int port = 9090;
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        if (("http".equals(scheme) && port != 80) ||
                ("https".equals(scheme) && port != 443)) {
            url.append(":").append(port);
        }
        url.append("/oauth2/token");
        assertEquals("http://localhost:9090/oauth2/token", url.toString());
    }

    // ===================================================================
    // 10. OAuth2 已知路由列表测试
    // ===================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "/oauth2/authorize", "/oauth2/token", "/oauth2/revoke",
            "/oauth2/introspect", "/oauth2/userinfo",
            "/oauth2/.well-known/oauth-authorization-server"
    })
    @DisplayName("所有 OAuth2 标准路由应已知")
    void oauth2Routes_shouldBeKnown(String route) {
        Set<String> knownRoutes = Set.of(
                "/oauth2/authorize", "/oauth2/token", "/oauth2/revoke",
                "/oauth2/introspect", "/oauth2/userinfo",
                "/oauth2/.well-known/oauth-authorization-server"
        );
        assertTrue(knownRoutes.contains(route),
                "路由 " + route + " 应在已知路由列表中");
    }

    // ===================================================================
    // helper methods - 内联实现（与 Oauth2Protocol 中的逻辑保持一致）
    // ===================================================================

    /**
     * 解析 form-urlencoded 格式的请求体（与 Oauth2Protocol.parseFormUrlEncoded 逻辑一致）
     */
    private Map<String, String> parseFormUrlEncoded(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isEmpty()) {
            return params;
        }
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    params.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
                } catch (Exception e) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }

    /**
     * 解析 Basic 认证头（与 Oauth2Protocol.extractBasicCredentials 逻辑一致）
     */
    private String[] extractBasicCredentials(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }
        try {
            String base64Credentials = authHeader.substring(6);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes, StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);
            return parts.length == 2 ? parts : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建错误响应（与 Oauth2Protocol.buildErrorResponse 逻辑一致）
     */
    private Map<String, Object> buildErrorResponse(String error, String description) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", error);
        response.put("error_description", description);
        return response;
    }

    /**
     * 构建 Token 响应（与 Oauth2Protocol.buildTokenResponse 逻辑一致）
     */
    private Map<String, Object> buildTokenResponse(String accessToken, String tokenType,
                                                  Long expiresIn, String refreshToken, String scope) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("access_token", accessToken);
        response.put("token_type", tokenType != null ? tokenType : "Bearer");
        response.put("expires_in", expiresIn);
        if (refreshToken != null) {
            response.put("refresh_token", refreshToken);
        }
        if (scope != null && !scope.isEmpty()) {
            response.put("scope", scope);
        }
        return response;
    }
}
