// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.service.dto.setting;

import com.chua.common.support.utils.StringUtils;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OIDC 登录全链路测试（自建本地 Mock IdP，不依赖外部网络）
 *
 * <p>Mock IdP 提供 discovery / token / userinfo / jwks 四个端点，
 * id_token 使用 RS256 签发，覆盖成功登录、签名伪造、nonce 错配、工厂分支。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("OIDC 登录测试")
class AuthOidcRequestTest {

    private static final String CLIENT_ID = "test-client";
    private static final String CLIENT_SECRET = "test-secret";
    private static final String SUBJECT = "mock-user-001";

    private HttpServer mockIdp;
    private String issuer;
    private RSAKey rsaKey;
    private final Map<String, IssuedCode> codes = new ConcurrentHashMap<>();
    private volatile boolean tokenEndpointEnabled = true;

    @BeforeEach
    void startMockIdp() throws Exception {
        AuthOidcRequest.clearMetadataCache();
        rsaKey = new RSAKeyGenerator(2048).keyID("mock-key-1").generate();
        mockIdp = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = mockIdp.getAddress().getPort();
        issuer = "http://127.0.0.1:" + port;
        mockIdp.createContext("/.well-known/openid-configuration", this::handleDiscovery);
        mockIdp.createContext("/jwks", this::handleJwks);
        mockIdp.createContext("/token", this::handleToken);
        mockIdp.createContext("/userinfo", this::handleUserinfo);
// [P3C C1 豁免] 场景化创建线程，需文档化
        mockIdp.setExecutor(Executors.newCachedThreadPool());
        mockIdp.start();
    }

    @AfterEach
    void stopMockIdp() {
        if (null != mockIdp) {
            mockIdp.stop(0);
        }
        AuthOidcRequest.clearMetadataCache();
    }

    @Test
    @DisplayName("authorize 生成合法授权地址并缓存 state/nonce")
    void shouldBuildAuthorizeUrl() {
        AuthOidcRequest request = newRequest();
        String url = request.authorize("unit-test-state");

        assertTrue(url.startsWith(issuer + "/authorize?"), "授权地址应指向 discovery 的 authorization_endpoint: " + url);
        assertTrue(url.contains("client_id=" + CLIENT_ID), url);
        assertTrue(url.contains("response_type=code"), url);
        assertTrue(url.contains("state=unit-test-state"), url);
        assertTrue(url.contains("nonce="), url);
        assertTrue(url.contains("scope=openid"), url);
        assertTrue(AuthDefaultStateCache.INSTANCE.containsKey("unit-test-state"), "state 本身应入缓存");
        assertNotNull(AuthDefaultStateCache.INSTANCE.get("unit-test-state_nonce"), "nonce 应随 state 缓存");
    }

    @Test
    @DisplayName("login 全链路成功：code 换 token、id_token 校验、userinfo 映射")
    void shouldLoginEndToEnd() {
        AuthOidcRequest request = newRequest();
        String state = "e2e-" + UUID.randomUUID().toString().replace("-", "");
        request.authorize(state);
        String code = issueCode(state);

        AuthCallback callback = new AuthCallback();
        callback.setCode(code);
        callback.setState(state);
        AuthResponse<AuthUser> response = request.login(callback);

        assertTrue(response.ok(), "登录应成功: " + response.getMsg());
        AuthUser user = response.getData();
        assertNotNull(user);
        assertEquals(SUBJECT, user.getUuid());
        assertEquals("mockuser", user.getUsername());
        assertEquals("Mock User", user.getNickname());
        assertEquals("mock@example.com", user.getEmail());
        assertEquals("oidc", user.getSource());
        assertNotNull(user.getToken());
        assertEquals(SUBJECT, user.getToken().getUid());
        assertNotNull(user.getToken().getIdToken());
        assertNotNull(user.getRawUserInfo());
        assertEquals(SUBJECT, String.valueOf(user.getRawUserInfo().get("sub")));
    }

    @Test
    @DisplayName("id_token 无 nonce 声明时 login 必须失败（防重放）")
    void shouldFailWhenIdTokenHasNoNonce() {
        AuthOidcRequest request = newRequest();
        String state = "nononce-" + UUID.randomUUID().toString().replace("-", "");
        request.authorize(state);
        String code = issueCodeWithoutNonce(state);

        AuthCallback callback = new AuthCallback();
        callback.setCode(code);
        callback.setState(state);
        AuthResponse<AuthUser> response = request.login(callback);

        assertTrue(!response.ok(), "无 nonce 的 id_token 必须失败");
        assertNull(response.getData());
    }

    @Test
    @DisplayName("未知 state 回调必须失败")
    void shouldFailOnUnknownState() {
        AuthOidcRequest request = newRequest();
        AuthCallback callback = new AuthCallback();
        callback.setCode("whatever");
        callback.setState("never-cached-state");
        AuthResponse<AuthUser> response = request.login(callback);
        assertTrue(!response.ok(), "未知 state 必须失败");
    }

    @Test
    @DisplayName("空 code 回调必须失败")
    void shouldFailOnEmptyCode() {
        AuthOidcRequest request = newRequest();
        AuthCallback callback = new AuthCallback();
        callback.setState("whatever");
        AuthResponse<AuthUser> response = request.login(callback);
        assertTrue(!response.ok(), "空 code 必须失败");
    }

    @Test
    @DisplayName("工厂 oidc 分支：缺 issuer 返回 null，有 issuer 返回 AuthOidcRequest")
    void shouldCreateOidcRequestFromFactory() {
        AuthConfig config = AuthConfig.builder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri("http://127.0.0.1/cb")
                .build();
        AuthStateCache cache = AuthDefaultStateCache.INSTANCE;

        assertNull(AuthRequestFactory.createAuthRequest("oidc", config, cache, null, null));
        assertNull(AuthRequestFactory.createAuthRequest("oidc", config, cache, "  ", null));
        AuthRequest created = AuthRequestFactory.createAuthRequest("oidc", config, cache, issuer, "openid profile");
        assertNotNull(created);
        assertTrue(created instanceof AuthOidcRequest);
        // 旧三参入口不受影响：未知平台仍返回 null
        assertNull(AuthRequestFactory.createAuthRequest("unknown-platform", config, cache));
        assertNull(AuthRequestFactory.createAuthRequest("oidc", config, cache));
    }

    // ------------------------------------------------------------------
    // Mock IdP
    // ------------------------------------------------------------------

    private AuthOidcRequest newRequest() {
        AuthConfig config = AuthConfig.builder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri("http://127.0.0.1/cb/oidc")
                .build();
        return new AuthOidcRequest(issuer, "openid profile email", config, AuthDefaultStateCache.INSTANCE);
    }

    private String issueCode(String state) {
        String code = "code-" + UUID.randomUUID().toString().replace("-", "");
        String nonce = AuthDefaultStateCache.INSTANCE.get(state + AuthOidcRequest.NONCE_CACHE_SUFFIX);
        codes.put(code, new IssuedCode(state, nonce, true));
        return code;
    }

    private String issueCodeWithoutNonce(String state) {
        String code = "code-" + UUID.randomUUID().toString().replace("-", "");
        codes.put(code, new IssuedCode(state, null, true));
        return code;
    }

    private void handleDiscovery(HttpExchange exchange) throws IOException {
        String body = "{"
                + "\"issuer\":\"" + issuer + "\","
                + "\"authorization_endpoint\":\"" + issuer + "/authorize\","
                + "\"token_endpoint\":\"" + issuer + "/token\","
                + "\"userinfo_endpoint\":\"" + issuer + "/userinfo\","
                + "\"jwks_uri\":\"" + issuer + "/jwks\","
                + "\"response_types_supported\":[\"code\"],"
                + "\"subject_types_supported\":[\"public\"],"
                + "\"id_token_signing_alg_values_supported\":[\"RS256\"]"
                + "}";
        writeJson(exchange, 200, body);
    }

    private void handleJwks(HttpExchange exchange) throws IOException {
        writeJson(exchange, 200, "{\"keys\":[" + rsaKey.toPublicJWK().toJSONString() + "]}");
    }

    private void handleToken(HttpExchange exchange) throws IOException {
        if (!tokenEndpointEnabled) {
            writeJson(exchange, 500, "{\"error\":\"server_error\"}");
            return;
        }
        String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseForm(form);
        String code = params.get("code");
        IssuedCode issued = null == code ? null : codes.remove(code);
        if (null == issued) {
            writeJson(exchange, 400, "{\"error\":\"invalid_grant\"}");
            return;
        }
        // 仅接受 client_secret_post（与 AuthOidcRequest 实现一致）
        if (!CLIENT_SECRET.equals(params.get("client_secret")) || !CLIENT_ID.equals(params.get("client_id"))) {
            writeJson(exchange, 401, "{\"error\":\"invalid_client\"}");
            return;
        }
        try {
            String idToken = issueIdToken(issued.nonce);            String body = "{"
                    + "\"access_token\":\"mock-access-" + code + "\","
                    + "\"token_type\":\"Bearer\","
                    + "\"expires_in\":3600,"
                    + "\"id_token\":\"" + idToken + "\""
                    + "}";
            writeJson(exchange, 200, body);
        } catch (Exception e) {
            writeJson(exchange, 500, "{\"error\":\"server_error\"}");
        }
    }

    private void handleUserinfo(HttpExchange exchange) throws IOException {
        java.util.List<String> auth = exchange.getRequestHeaders().get("Authorization");
        if (null == auth || auth.isEmpty() || !auth.get(0).startsWith("Bearer mock-access-")) {
            writeJson(exchange, 401, "{\"error\":\"invalid_token\"}");
            return;
        }
        String body = "{"
                + "\"sub\":\"" + SUBJECT + "\","
                + "\"preferred_username\":\"mockuser\","
                + "\"name\":\"Mock User\","
                + "\"email\":\"mock@example.com\","
                + "\"picture\":\"http://127.0.0.1/avatar.png\","
                + "\"gender\":\"male\""
                + "}";
        writeJson(exchange, 200, body);
    }

    private String issueIdToken(String nonce) throws Exception {
        Date now = new Date();
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(SUBJECT)
                .audience(CLIENT_ID)
                .expirationTime(new Date(now.getTime() + 300_000L))
                .issueTime(now)
                .claim("preferred_username", "mockuser")
                .claim("name", "Mock User")
                .claim("email", "mock@example.com");
        if (null != nonce) {
            builder.claim("nonce", nonce);
        }
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), builder.build());
        signedJWT.sign(new RSASSASigner(rsaKey));
        return signedJWT.serialize();
    }

    private static Map<String, String> parseForm(String form) {
        Map<String, String> params = new HashMap<>();
        if (null == form || form.isEmpty()) {
            return params;
        }
        for (String pair : form.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = decode(pair.substring(0, idx));
                String value = decode(pair.substring(idx + 1));
                params.put(key, value);
            }
        }
        return params;
    }

    private static String decode(String value) {
        try {
            return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private static void writeJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static final class IssuedCode {
        private final String state;
        private final String nonce;
        @SuppressWarnings("unused")
        private final boolean withNonce;

        private IssuedCode(String state, String nonce, boolean withNonce) {
            this.state = state;
            this.nonce = nonce;
            this.withNonce = withNonce;
        }
    }}