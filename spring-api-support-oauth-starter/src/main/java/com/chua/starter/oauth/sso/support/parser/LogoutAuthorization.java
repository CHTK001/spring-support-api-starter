package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.information.LogoutAuthInformation;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * 鉴权信息
 *
 * @author CH
 */
@Slf4j
public final class LogoutAuthorization implements Authorization {
    private final String token;
    private final String cookie;
    private final String uid;
    private final String logoutType;
    private final HttpServletResponse response;
    private final AuthServerProperties authServerProperties;
    final ApplicationContext applicationContext;
    final LoginCheck loginCheck;
    private final LogoutAuthInformation authInformation;

    public LogoutAuthorization(LogoutAuthInformation authInformation,
                               String token,
                               String cookie,
                               String uid,
                               String logoutType,
                               HttpServletResponse response,
                               AuthServerProperties authServerProperties,
                               ApplicationContext applicationContext,
                               LoginCheck loginCheck) {
        this.authInformation = authInformation;
        this.token = token;
        this.cookie = cookie;
        this.uid = uid;
        this.logoutType = logoutType;
        this.response = response;
        this.authServerProperties = authServerProperties;
        this.applicationContext = applicationContext;
        this.loginCheck = loginCheck;
    }

    @Override
    public ReturnResult<String> authentication() {
        String tokenManagement = authInformation.getAuthServerProperties().getTokenManagement();
        TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class).getExtension(tokenManagement);
        if (null == tokenResolver) {
            return ReturnResult.error("认证服务器无法认证");
        }
        tokenResolver.logout(uid, LogoutType.valueOf(logoutType));
        return ReturnResult.ok(authInformation.encodeHex("SUCCESS"));
    }

}
