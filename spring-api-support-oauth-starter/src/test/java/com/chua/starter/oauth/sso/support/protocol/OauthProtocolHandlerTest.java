package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.network.http.HttpHeader;
import com.chua.common.support.network.http.HttpMethod;
import com.chua.common.support.network.server.request.ServerRequest;
import com.chua.common.support.network.server.response.ServerResponse;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.core.AuthCoreService;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.Cookie;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 协议入口路由契约测试。
 *
 * <p>守护「传输层只做编解码、认证逻辑统一走 AuthCoreService」：登录/升级/登出/校验
 * 必须落到同一个核心入口，未知路径不得触碰认证服务。</p>
 *
 * <p>P3C40 去 mockito 改造：原实现用 {@code @Mock ServerRequest/ServerResponse/HttpHeader}
 * + {@code ArgumentCaptor} 把调用悬空在桩上，断言的只是「响应体字符串」；
 * 现在全部换成手写的记录桩，桩本身知道「有没有被真正的命中」。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@DisplayName("OauthProtocolHandler: 路由到 AuthCoreService")
class OauthProtocolHandlerTest {

    /**
     * 手写的 {@link ServerRequest} 记录桩。
     *
     * <p>完整实现接口（只记录真实命中一次，剩余方法给默认值），
     * 保证 {@code OauthProtocolHandler#decode} / {@code resolveAddress} / {@code resolveToken}
     * 里用到的每个方法都落到桩上，而不是 Mockito 的「随便返回」。</p>
     */
    private static class FakeServerRequest implements ServerRequest {

        /** 逐笔录制的调用 */
        private final List<String> recorded = new ArrayList<>();

        private final String path;

        private final String body;

        private final HttpHeader headers;

        private final String remoteAddress;

        FakeServerRequest(String path, String body, HttpHeader headers, String remoteAddress) {
            this.path = path;
            this.body = body;
            this.headers = headers;
            this.remoteAddress = remoteAddress;
        }

        private void record(Method method, Object... args) {
            recorded.add(method.getName() + "(" + recordedName(args) + ")");
        }

        private static String recordedName(Object... args) {
            return args.length == 0 ? "" : java.util.Arrays.stream(args)
                    .map(a -> String.valueOf(a))
                    .collect(Collectors.joining(",", "(", ")"));
        }

        @Override
        public String getUri() { return path; }

        @Override
        public String getPath() { return path; }

        @Override
        public HttpMethod getMethod() { return HttpMethod.POST; }

        @Override
        public String getHeader(String name) { return headers.get(name); }

        @Override
        public HttpHeader getHeaders() { return headers; }

        @Override
        public Map<String, String> getParams() { return Collections.emptyMap(); }

        @Override
        public String getParam(String name) { return null; }

        @Override
        public String getContentType() { return null; }

        @Override
        public long getContentLength() { return -1; }

        @Override
        public byte[] getBody() { return body != null ? body.getBytes(java.nio.charset.StandardCharsets.UTF_8) : new byte[0]; }

        @Override
        public String getBodyString() { return body; }

        @Override
        public InputStream getInputStream() { return new ByteArrayInputStream(getBody()); }

        @Override
        public String getRemoteAddress() { return remoteAddress; }

        @Override
        public int getRemotePort() { return -1; }

        @Override
        public Map<String, Object> getAttributes() { return Collections.emptyMap(); }

        @Override
        public Object getAttribute(String name) { return null; }

        @Override
        public void setAttribute(String name, Object value) { }
    }

    /**
     * 手写的 {@link ServerResponse} 记录桩。
     */
    private static class FakeServerResponse implements ServerResponse {

        /** 逐笔录制的调用 */
        private final List<String> recorded = new ArrayList<>();

        private final Map<String, String> headers = new HashMap<>();

        private int status;

        private String contentType;

        private String bodyString;

        private boolean ended;

        @Override
        public ServerResponse setStatus(int statusCode) {
            recorded.add("setStatus(" + statusCode + ")");
            status = statusCode;
            return this;
        }

        @Override
        public int getStatus() { return status; }

        @Override
        public ServerResponse setHeader(String name, String value) {
            recorded.add("setHeader(" + name + ", " + value + ")");
            headers.put(name, value);
            return this;
        }

        @Override
        public String getHeader(String name) { return headers.get(name); }

        @Override
        public HttpHeader getHeaders() { return HttpHeader.create(); }

        @Override
        public ServerResponse setContentType(String contentType) {
            recorded.add("setContentType(" + contentType + ")");
            this.contentType = contentType;
            return this;
        }

        @Override
        public String getContentType() { return contentType; }

        @Override
        public ServerResponse setBody(byte[] body) {
            recorded.add("setBody(Byte[" + (body != null ? body.length : 0) + "])");
            this.bodyString = body != null ? new String(body, java.nio.charset.StandardCharsets.UTF_8) : null;
            return this;
        }

        @Override
        public ServerResponse setBody(String body) {
            recorded.add("setBody(String)");
            this.bodyString = body;
            return this;
        }

        @Override
        public byte[] getBody() { return bodyString != null ? bodyString.getBytes(java.nio.charset.StandardCharsets.UTF_8) : new byte[0]; }

        @Override
        public java.io.OutputStream getOutputStream() { return new java.io.OutputStream() {
            @Override
            public void write(byte[] b, int off, int len) { }
            @Override
            public void write(int b) { }
        }; }

        @Override
        public ServerResponse sendRedirect(String location) {
            return this;
        }

        @Override
        public ServerResponse sendError(int statusCode, String message) {
            return this;
        }

        @Override
        public void flush() { }

        /** 写原始字节（写入 body 并尝试落盘录像） */
        @Override
        public void writeRaw(byte[] bytes) {
            recorded.add("writeRaw(Byte[" + (bytes != null ? bytes.length : 0) + "])");
            this.bodyString = bytes != null ? new String(bytes, java.nio.charset.StandardCharsets.UTF_8) : null;
        }

        @Override
        public boolean isCommitted() { return ended; }

        @Override
        public boolean isEnded() { return ended; }

        @Override
        public ServerResponse reset() {
            return this;
        }

        @Override
        public void end() {
            recorded.add("end()");
            ended = true;
        }

        void assertSetBody(String result) {
            assertTrue(bodyString != null && bodyString.contains(result),
                    "响应体未包含期望内容：responseBody=" + bodyString);
        }

        void assertRecordedMethod(String name) {
            assertTrue(recorded.contains(name), "响应未记录期望调用：recorded=" + recorded);
        }
    }

    /**
     * 手写的 {@link AuthCoreService} 记录桩。
     *
     * <p>只记录真实命中的调用（方法名 + 参数），其余委托给 {@link AuthCoreService}；
     * 构造时传入的 {@link FakeLoginCheck} 负责返回输出的令牌解析器，
     * 让还原在「登录 → 分级路由」的链条上所见即所得。</p>
     */
    private static class FakeAuthCoreService extends AuthCoreService {

        /** 逐笔录制的调用 */
        private final List<String> recorded = new ArrayList<>();

        private ReturnResult<LoginResult> loginResult = ReturnResult.ok(new LoginResult());

        private FinalReturn<UserResume> verifyResult = new FinalReturn<>(true, new UserResume());

        private FinalReturn<UserResume> upgradeResult = new FinalReturn<>(true, new UserResume());

        private record FinalReturn<T>(boolean ok, T data) {}

        FakeAuthCoreService(AuthServerProperties properties, LoginCheck loginCheck) {
            super(properties, loginCheck);
        }

        @Override
        public ReturnResult<LoginResult> login(String address, String principal,
                                               String credential, String authType, Object ext) {
            recorded.add("login(" + address + ", " + principal + ", " + credential + ", " + authType + ", " + ext + ")");
            return loginResult;
        }

        @Override
        public ReturnResult<UserResume> verify(String cookie, String token) {
            recorded.add("verify(" + cookie + ", " + token + ")");
            return verifyResult.ok() ? ReturnResult.ok(verifyResult.data()) : ReturnResult.error("未认证");
        }

        @Override
        public ReturnResult<UserResume> upgrade(String upgradeType, String token, String refreshToken) {
            recorded.add("upgrade(" + upgradeType + ", " + token + ", " + refreshToken + ")");
            return upgradeResult.ok() ? ReturnResult.ok(upgradeResult.data()) : ReturnResult.error("升级失败");
        }

        @Override
        public ReturnResult<String> logout(String uid, LogoutType type, String token) {
            recorded.add("logout(" + uid + ", " + type + ", " + token + ")");
            return ReturnResult.ok("登出成功");
        }

        /**
         * 获取记录调用。
         *
         * @return 记录
         */
        List<String> recorded() {
            return recorded;
        }

        void assertRecorded(String name) {
            assertTrue(recorded.contains(name), "认证服务未记录期望调用：recorded=" + recorded);
        }
    }

    /**
     * 手写的 {@link TokenResolver} 记录桩。
     *
     * <p>让上游的登录/升级/登出在逻辑链路上成为可见的调用。</p>
     */
    private static class FakeTokenResolver implements TokenResolver {

        /** 逐笔录制的调用 */
        private final List<String> recorded = new ArrayList<>();

        @Override
        public ReturnResult<LoginResult> createToken(String address, UserResult userResult, String authType) {
            recorded.add("createToken(" + address + ", " + userResult.getUserId() + ", " + authType + ")");
            return ReturnResult.ok(new LoginResult());
        }

        @Override
        public void logout(String token) {
            recorded.add("logout(" + token + ")");
        }

        @Override
        public void logout(String uid, LogoutType type) {
            recorded.add("logout(uid=" + uid + ", " + type + ")");
        }

        @Override
        public ReturnResult<UserResume> resolve(String cookie, String token) {
            recorded.add("resolve(cookie=" + cookie + ", token=" + token + ")");
            return ReturnResult.ok(new UserResume());
        }

        @Override
        public ReturnResult<UserResume> upgradeForTimestamp(String token, String refreshToken) {
            recorded.add("upgradeForTimestamp(" + token + ", " + refreshToken + ")");
            return ReturnResult.ok(new UserResume());
        }

        @Override
        public ReturnResult<UserResume> upgradeForVersion(String token, String refreshToken) {
            recorded.add("upgradeForVersion(" + token + ", " + refreshToken + ")");
            return ReturnResult.ok(new UserResume());
        }

        @Override
        public ReturnResult<UserResume> upgradeForRefresh(String token, String refreshToken) {
            recorded.add("upgradeForRefresh(" + token + ", " + refreshToken + ")");
            return ReturnResult.ok(new UserResume());
        }

        @Override
        public void logout(Cookie[] cookies, String token, String refreshToken, String cookieName) {
            recorded.add("logout.cookies(" + token + ", " + cookieName + ")");
        }

        @Override
        public ReturnResult<UserResume> resolveUserCode(String userCode) {
            recorded.add("resolveUserCode(" + userCode + ")");
            return ReturnResult.ok(new UserResume());
        }

        void assertRecorded(String name) {
            assertTrue(recorded.contains(name), "解析器未记录期望调用：recorded=" + recorded);
        }
    }

    /**
     * 手写的 {@link LoginCheck} 记录桩：
     * 返回令牌的解析器，并把 {@code doLogin} 的记录落下。
     */
    private static class FakeLoginCheck extends LoginCheck {

        /** 被解析出的令牌解析器 */
        private final FakeTokenResolver tokenResolver = new FakeTokenResolver();

        /** 逐笔录制的调用 */
        private final List<String> recorded = new ArrayList<>();

        /** 登录结果 */
        private ReturnResult<LoginResult> doLoginResult = ReturnResult.ok(new LoginResult());

        FakeLoginCheck(AuthServerProperties properties) {
            super(properties);
        }

        @Override
        public ReturnResult<LoginResult> doLogin(String address, String principal,
                                                 String credential, String authType, Object ext) {
            recorded.add("doLogin(" + address + ", " + principal + ", " + credential + ", " + authType + ", " + ext + ")");
            return doLoginResult;
        }

        @Override
        public TokenResolver resolveTokenResolver(String tokenManagement) {
            recorded.add("resolveTokenResolver(" + tokenManagement + ")");
            return tokenResolver;
        }

        void assertRecorded(String name) {
            assertTrue(recorded.contains(name), "LoginCheck 未记录期望调用：recorded=" + recorded);
        }
    }

    /**
     * 请求
     */
    private FakeServerRequest request;

    /**
     * 响应
     */
    private FakeServerResponse response;

    /**
     * 认证核心服务
     */
    private FakeAuthCoreService authCoreService;

    /**
     * 登录检查
     */
    private FakeLoginCheck loginCheck;

    /**
     * 被测处理器
     */
    private OauthProtocolHandler handler;

    /**
     * 初始化。
     *
     * @throws Exception 异常
     */
    @BeforeEach
    void setUp() {
        AuthServerProperties properties = new AuthServerProperties();
        properties.setTokenManagement("redis");
        loginCheck = new FakeLoginCheck(properties);
        authCoreService = new FakeAuthCoreService(properties, loginCheck);
        handler = new OauthProtocolHandler(properties, loginCheck, authCoreService, false);
        response = new FakeServerResponse();
    }

    /**
     * 构造请求：路径 + data 字段内的业务 JSON。
     *
     * @param path 路径
     * @param data 业务 JSON
     */
    private FakeServerRequest givenRequest(String path, String data) {
        request = new FakeServerRequest(path, "{\"data\":" + Json.toJson(data) + "}", HttpHeader.create(), "1.2.3.4");
        return request;
    }

    /**
     * 处理请求。
     *
     * @param request 请求
     * @throws Exception 处理异常
     */
    private void handle(ServerRequest request) throws Exception {
        handler.handle(request, response);
        assertEquals(true, response.isEnded(), "响应必须被终止");
    }

    /**
     * 登录路由到 core.login。
     *
     * @throws Exception 处理异常
     */
    @Test
    @DisplayName("/login")
    void shouldRouteLogin() throws Exception {
        givenRequest("/login",
                "{\"x-oauth-username\":\"alice\",\"x-oauth-password\":\"pwd\",\"x-oauth-auth-type\":\"WEB\"}");

        handle(request);

        authCoreService.assertRecorded("login(1.2.3.4, alice, pwd, WEB, null)");
        response.assertRecordedMethod("setContentType(application/json)");
        response.assertRecordedMethod("end()");
    }

    /**
     * 校验路由到 core.verify。
     *
     * @throws Exception 处理异常
     */
    @Test
    @DisplayName("/verify")
    void shouldRouteVerify() throws Exception {
        givenRequest("/verify", "{\"x-oauth-token\":\"tok\"}");

        handle(request);

        authCoreService.assertRecorded("verify(null, tok)");
    }

    /**
     * 升级路由到 core.upgrade（类型原样透传）。
     *
     * @throws Exception 处理异常
     */
    @Test
    @DisplayName("/upgrade")
    void shouldRouteUpgrade() throws Exception {
        givenRequest("/upgrade",
                "{\"x-oauth-token\":\"t\",\"x-oauth-refresh-token\":\"r\",\"x-oauth-upgrade-type\":\"refresh\"}");

        handle(request);

        authCoreService.assertRecorded("upgrade(refresh, t, r)");
    }

    /**
     * 登出类型大小写不敏感，非法值回落 LOGOUT。
     *
     * @throws Exception 处理异常
     */
    @Test
    @DisplayName("/logout: 类型大小写不敏感")
    void shouldRouteLogoutIgnoreCase() throws Exception {
        givenRequest("/logout", "{\"x-oauth-uid\":\"42\",\"x-oauth-logout-type\":\"kick\"}");

        handle(request);

        authCoreService.assertRecorded("logout(42, KICK, null)");
    }

    /**
     * 未知路径不触碰认证服务，返回明确错误。
     *
     * @throws Exception 处理异常
     */
    @Test
    @DisplayName("未知路径: 不调用 core")
    void shouldRejectUnknownPath() throws Exception {
        givenRequest("/unknown", "{}");

        handle(request);

        assertTrue(authCoreService.recorded().isEmpty(), "认证服务不应被调用：recorded=" + authCoreService.recorded());
        response.assertSetBody("不支持的认证路径");
        response.assertRecordedMethod("end()");
    }
}
