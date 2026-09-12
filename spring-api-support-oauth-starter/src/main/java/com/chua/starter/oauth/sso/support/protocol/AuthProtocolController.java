package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.information.AuthInformation;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.information.LogoutAuthInformation;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.information.TemporaryTokenAuthInformation;
import com.chua.starter.oauth.sso.support.information.UpgradeAuthInformation;
import com.chua.starter.oauth.sso.support.parser.Authorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证协议 HTTP 入口控制器
 *
 * <p>接收 OAuth 客户端（oauth-client http 协议）的认证请求，按请求路径分派到
   * 对应的认证信息处理器：登录、注销、升级、临时令牌、鉴权（app键/Bearer/JWT/OAuth2）。</p>
 *
 * <p>请求体格式：{@code {"data":"..."}}，data 为加密（x-oauth-encode=true）或明文的
   * 认证参数 JSON；响应格式：{@code {"code":"00000","data":"..."}}，数据 加密时
 * 响应头携带 {@code x-oauth-response-serial} 供客户端解密。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthProtocolController {

    /** 认证服务器属性 */
    private final AuthServerProperties authServerProperties;
    /** Application上下文 */
    private final ApplicationContext applicationContext;
    /** Logincheck */
    private final LoginCheck loginCheck;

    /**
     * 统一认证入口
     *
     * @param body     请求体（含 数据 字段）
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @return 认证结果
     */
    @PostMapping(value = {"/login", "/logout", "/upgrade", "/temporary-token", "/verify", "/oauth"})
    public ReturnResult<String> handle(@RequestBody(required = false) Map<String, Object> body,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        String data = body == null ? null : String.valueOf(body.get("data"));
        String path = request.getRequestURI();
        AuthInformation authInformation = createAuthInformation(path, data, request, response);
        if (authInformation == null) {
            return ReturnResult.error("不支持的认证路径: " + path);
        }
        Authorization authorization = authInformation.authorization();
        ReturnResult<String> result = authorization.authentication();
        if (log.isDebugEnabled()) {
            log.debug("[AuthProtocol] path={}, code={}, msg={}", path, result.getCode(), result.getMsg());
        }
        return result;
    }

    /**
     * 按请求路径创建对应的认证信息处理器
     *
     * @param path     请求路径
     * @param data     请求数据
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @return 认证信息处理器
     */
    private AuthInformation createAuthInformation(String path, String data,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
        if (path.endsWith("/login")) {
            return new LoginAuthInformation(authServerProperties, data, request, response, applicationContext, loginCheck);
        }
        if (path.endsWith("/logout")) {
            return new LogoutAuthInformation(authServerProperties, data, request, response, applicationContext, loginCheck);
        }
        if (path.endsWith("/upgrade")) {
            return new UpgradeAuthInformation(authServerProperties, data, request, response, applicationContext, loginCheck);
        }
        if (path.endsWith("/temporary-token")) {
            return new TemporaryTokenAuthInformation(authServerProperties, data, request, response, applicationContext, loginCheck);
        }
        if (path.endsWith("/verify") || path.endsWith("/oauth")) {
            return new OauthAuthInformation(authServerProperties, data, request, response, applicationContext, loginCheck);
        }
        return null;
    }
}
