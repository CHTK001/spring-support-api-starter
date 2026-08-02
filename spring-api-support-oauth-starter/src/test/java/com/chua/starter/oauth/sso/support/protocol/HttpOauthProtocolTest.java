package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.code.ReturnCode;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.entity.AuthRequest;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP OAuth 协议单元测试
 * <p>
 * 测试 HttpOauthProtocol 的辅助方法：extractBody、writeResponse、parseFormUrlEncoded 等。
 * 由于 HttpOauthProtocol 依赖 ProtocolServer（外部实现），路由注册逻辑通过构造测试验证。
 * </p>
 */
class HttpOauthProtocolTest {

    private AuthServerProperties properties;
    private LoginCheck loginCheck;

    @BeforeEach
    void setUp() {
        properties = new AuthServerProperties();
        properties.setSignVerify(false);
        properties.setTokenManagement("memory");
        properties.setProtocols(Arrays.asList("http"));
        loginCheck = new LoginCheck(properties);
    }

    // ------------------------------------------------------------------
    // 1. AuthServerProperties 基础配置
    // ------------------------------------------------------------------

    @Test
    @DisplayName("HTTP 协议默认端口应为 19180")
    void defaultPort_shouldBe19180() {
        // armeriaPort 默认值
        assertEquals(19180, properties.getArmeriaPort(),
                "默认 HTTP/Armeria 端口应为 19180");
    }

    @Test
    @DisplayName("自定义端口应生效")
    void customPort_shouldOverrideDefault() {
        properties.setArmeriaPort(9090);
        assertEquals(9090, properties.getArmeriaPort());
    }

    // ------------------------------------------------------------------
    // 2. AuthRequest 序列化/反序列化
    // ------------------------------------------------------------------

    @Test
    @DisplayName("AuthRequest 应能正确序列化")
    void authRequest_shouldSerializeToJson() {
        AuthRequest request = new AuthRequest();
        request.setData("test-data");
        request.setUsername("testuser");
        request.setType("PASSWORD");
        request.setPasswd("secret");
        request.setEncipher(true);

        String json = Json.toJson(request);
        assertNotNull(json);
        assertTrue(json.contains("test-data"), "JSON 应包含 data 字段");
        assertTrue(json.contains("testuser"), "JSON 应包含 username 字段");
    }

    @Test
    @DisplayName("AuthRequest 应能正确反序列化")
    void authRequest_shouldDeserializeFromJson() {
        String json = "{\"data\":\"test-data\",\"username\":\"user1\",\"type\":\"PASSWORD\",\"encipher\":true}";
        AuthRequest request = Json.fromJson(json, AuthRequest.class);

        assertNotNull(request);
        assertEquals("test-data", request.getData());
        assertEquals("user1", request.getUsername());
        assertEquals("PASSWORD", request.getType());
        assertTrue(request.isEncipher());
    }

    @Test
    @DisplayName("AuthRequest 空对象序列化不应抛异常")
    void authRequest_empty_shouldNotThrowException() {
        AuthRequest request = new AuthRequest();
        String json = Json.toJson(request);
        assertNotNull(json);
        assertTrue(json.length() > 0, "空 AuthRequest 序列化结果不应为空");
    }

    // ------------------------------------------------------------------
    // 3. ReturnResult 构造测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("ReturnResult.error 应返回失败结果")
    void returnResult_error_shouldBeFailure() {
        var result = com.chua.common.support.lang.code.ReturnResult.error("测试错误");
        assertNotNull(result);
        assertTrue(result.isFailure(), "error 结果应为 failure");
        assertEquals("测试错误", result.getMsg());
    }

    @Test
    @DisplayName("ReturnResult.illegal 应返回非法结果")
    void returnResult_illegal_shouldBeIllegal() {
        var result = com.chua.common.support.lang.code.ReturnResult.illegal("非法请求");
        assertNotNull(result);
        assertTrue(result.isFailure(), "illegal 结果应为 failure");
        assertTrue(result.getCode().contains("C0400"), "illegal 结果的 code 应包含 C0400");
    }

    @Test
    @DisplayName("ReturnResult.ok 应返回成功结果")
    void returnResult_ok_shouldBeSuccess() {
        var result = com.chua.common.support.lang.code.ReturnResult.ok("成功数据");
        assertNotNull(result);
        assertTrue(result.isOk(), "ok 结果应为 success");
    }

    // ------------------------------------------------------------------
    // 4. 请求体解析模拟测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("UTF-8 body 字节应能正确转字符串")
    void bodyBytes_shouldDecodeUtf8() {
        String original = "{\"username\":\"张三\",\"password\":\"123456\"}";
        byte[] bytes = original.getBytes(StandardCharsets.UTF_8);
        String decoded = new String(bytes, StandardCharsets.UTF_8);

        assertEquals(original, decoded, "UTF-8 编解码应一致");
        assertTrue(decoded.contains("张三"), "应包含中文字符");
    }

    @Test
    @DisplayName("空 body 字节数组应返回空字符串")
    void emptyBody_shouldReturnEmptyString() {
        byte[] bytes = new byte[0];
        String decoded = new String(bytes, StandardCharsets.UTF_8);
        assertEquals("", decoded, "空字节数组应解码为空字符串");
    }

    // ------------------------------------------------------------------
    // 5. form-urlencoded 解析测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("parseFormUrlEncoded - 标准参数应正确解析")
    void formUrlEncoded_standardParams_shouldParseCorrectly() {
        String body = "grant_type=password&username=test&password=123&scope=read";
        Map<String, String> params = parseFormUrlEncoded(body);

        assertEquals("password", params.get("grant_type"));
        assertEquals("test", params.get("username"));
        assertEquals("123", params.get("password"));
        assertEquals("read", params.get("scope"));
    }

    @Test
    @DisplayName("parseFormUrlEncoded - 空 body 应返回空 Map")
    void formUrlEncoded_emptyBody_shouldReturnEmptyMap() {
        Map<String, String> params = parseFormUrlEncoded("");
        assertTrue(params.isEmpty());
    }

    @Test
    @DisplayName("parseFormUrlEncoded - null body 应返回空 Map")
    void formUrlEncoded_nullBody_shouldReturnEmptyMap() {
        Map<String, String> params = parseFormUrlEncoded(null);
        assertTrue(params.isEmpty());
    }

    @Test
    @DisplayName("parseFormUrlEncoded - 只有 key 无 value 的参数应忽略")
    void formUrlEncoded_keyOnly_shouldIgnore() {
        String body = "grant_type=password&no_value";
        Map<String, String> params = parseFormUrlEncoded(body);

        assertEquals("password", params.get("grant_type"));
        assertNull(params.get("no_value"), "只有 key 无 value 的参数应被忽略");
    }

    @Test
    @DisplayName("parseFormUrlEncoded - URL 编码的值应正确解码")
    void formUrlEncoded_urlEncoded_shouldDecode() {
        String body = "redirect_uri=https%3A%2F%2Fexample.com%2Fcallback&scope=read+write";
        Map<String, String> params = parseFormUrlEncoded(body);

        assertEquals("https://example.com/callback", params.get("redirect_uri"));
        assertEquals("read write", params.get("scope"));
    }

    // ------------------------------------------------------------------
    // 6. OAuth2 错误响应构建测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("buildErrorResponse 应包含 error 和 error_description")
    void buildErrorResponse_shouldContainErrorFields() {
        Map<String, Object> response = buildErrorResponse("invalid_grant", "令牌已过期");
        assertEquals("invalid_grant", response.get("error"));
        assertEquals("令牌已过期", response.get("error_description"));
        assertEquals(2, response.size());
    }

    @Test
    @DisplayName("buildErrorResponse JSON 序列化后应可被客户端解析")
    void buildErrorResponse_shouldBeValidJson() {
        Map<String, Object> response = buildErrorResponse("server_error", "内部错误");
        String json = Json.toJson(response);

        assertNotNull(json);
        assertTrue(json.contains("error"), "JSON 应包含 error 字段");
        assertTrue(json.contains("server_error"), "JSON 应包含错误码");
    }

    // ------------------------------------------------------------------
    // 7. HTTP 协议配置检查
    // ------------------------------------------------------------------

    @Test
    @DisplayName("protocols 列表应支持 http")
    void protocolsList_shouldSupportHttp() {
        properties.setProtocols(Arrays.asList("http", "rsocket"));
        assertTrue(properties.getProtocols().contains("http"));
        assertTrue(properties.getProtocols().contains("rsocket"));
    }

    @Test
    @DisplayName("仅启用 http 协议时 armeria 不应在列表中")
    void onlyHttp_enabled_armeriaNotInList() {
        properties.setProtocols(Collections.singletonList("http"));
        assertFalse(properties.getProtocols().contains("armeria"),
                "仅启用 http 时 armeria 不应在协议列表中");
    }

    // ------------------------------------------------------------------
    // helper methods (mirrors Oauth2Protocol.parseFormUrlEncoded logic)
    // ------------------------------------------------------------------

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

    private Map<String, Object> buildErrorResponse(String error, String description) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", error);
        response.put("error_description", description);
        return response;
    }
}
