package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * 鉴权信息
 *
 * @author CH
 */
@Slf4j
public final class LoginAuthorization implements Authorization {
    private final String username;
    private final String password;
    private final String loginType;
    private final Map<String, String> ext;
    private final HttpServletResponse response;
    private final AuthServerProperties authServerProperties;
    final ApplicationContext applicationContext;
    final LoginCheck loginCheck;
    private final LoginAuthInformation authInformation;

    public LoginAuthorization(LoginAuthInformation authInformation,
                              String username,
                              String password,
                              String loginType,
                              Map<String, String> ext,
                              HttpServletResponse response,
                              AuthServerProperties authServerProperties,
                              ApplicationContext applicationContext,
                              LoginCheck loginCheck) {
        this.authInformation = authInformation;
        this.username = username;
        this.password = password;
        this.loginType = loginType;
        this.ext = ext;
        this.response = response;
        this.authServerProperties = authServerProperties;
        this.applicationContext = applicationContext;
        this.loginCheck = loginCheck;
    }

    @Override
    public ReturnResult<String> authentication() {
        ReturnResult<LoginResult> returnResult = loginCheck.doLogin(MapUtils.getString(this.ext, "address"),
                username,
                password,
                loginType,
                ext
        );

        if(!returnResult.isOk()) {
            return ReturnResult.illegal(returnResult.getMsg());
        }
        LoginResult returnResultData = returnResult.getData();
        CookieUtil.set(response, authServerProperties.getCookieName(), returnResultData.getToken(), true);
        String json = Json.toJson(returnResultData);
        return ReturnResult.ok(authInformation.encodeHex(json));
    }

}
