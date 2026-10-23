package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.network.http.HttpHeader;
import com.chua.common.support.network.server.request.ServerRequest;
import com.chua.common.support.network.server.response.ServerResponse;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.core.AuthCoreService;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 协议入口路由契约测试。
 *
 * <p>守护「传输层只做编解码、认证逻辑统一走 AuthCoreService」：登录/升级/登出/校验
 * 必须落到同一个核心入口，未知路径不得触碰认证服务。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OauthProtocolHandler: 路由到 AuthCoreService")
class OauthProtocolHandlerTest {

    /**
     * 请求
     */
    @Mock
    private ServerRequest request;

    /**
     * 响应
     */
    @Mock
    private ServerResponse response;

    /**
     * 请求头
     */
    @Mock
    private HttpHeader headers;

    /**
     * 认证核心服务
     */
    @Mock
    private AuthCoreService authCoreService;

    /**
     * 登录检查
     */
    @Mock
    private LoginCheck loginCheck;

    /**
     * 被测处理器
     */
    private OauthProtocolHandler handler;

    /**
     * 初始化。
     */
    @BeforeEach
    void setUp() {
        handler = new OauthProtocolHandler(new AuthServerProperties(), loginCheck, authCoreService, false);
    }

    /**
     * 构造请求：路径 + data 字段内的业务 JSON。
     *
     * @param path 路径
     * @param data 业务 JSON
     */
    private void givenRequest(String path, String data) {
        when(request.getPath()).thenReturn(path);
        when(request.getBodyString()).thenReturn("{\"data\":" + Json.toJson(data) + "}");
        when(request.getHeaders()).thenReturn(headers);
        when(request.getRemoteAddress()).thenReturn("1.2.3.4");
        when(headers.get(anyString())).thenReturn(null);
        when(headers.toMap()).thenReturn(Map.of());
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
        when(authCoreService.login("1.2.3.4", "alice", "pwd", "WEB", null))
                .thenReturn(ReturnResult.ok(new LoginResult()));

        handler.handle(request, response);

        verify(authCoreService).login("1.2.3.4", "alice", "pwd", "WEB", null);
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
        when(authCoreService.verify(null, "tok")).thenReturn(ReturnResult.ok(new UserResume()));

        handler.handle(request, response);

        verify(authCoreService).verify(null, "tok");
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
        when(authCoreService.upgrade("refresh", "t", "r")).thenReturn(ReturnResult.ok(new UserResume()));

        handler.handle(request, response);

        verify(authCoreService).upgrade("refresh", "t", "r");
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
        when(authCoreService.logout("42", LogoutType.KICK, null)).thenReturn(ReturnResult.ok("登出成功"));

        handler.handle(request, response);

        verify(authCoreService).logout("42", LogoutType.KICK, null);
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

        handler.handle(request, response);

        verifyNoInteractions(authCoreService);
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(response).setBody(body.capture());
        assertTrue(body.getValue().contains("不支持的认证路径"));
    }
}
