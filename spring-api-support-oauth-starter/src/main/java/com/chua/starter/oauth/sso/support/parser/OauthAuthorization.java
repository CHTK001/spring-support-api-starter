package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * 鉴权信息
 *
 * @author CH
 */
@Slf4j
public final class OauthAuthorization implements Authorization {
    private final String token;
    private final String cookie;
    private final String subProtocol;
    final ApplicationContext applicationContext;
    final LoginCheck loginCheck;
    private final OauthAuthInformation authInformation;

    public OauthAuthorization(OauthAuthInformation authInformation,
                              String token,
                              String cookie,
                              String subProtocol,
                              ApplicationContext applicationContext,
                              LoginCheck loginCheck) {
        this.authInformation = authInformation;
        this.token = token;
        this.cookie = cookie;
        this.subProtocol = subProtocol;
        this.applicationContext = applicationContext;
        this.loginCheck = loginCheck;
    }

    @Override
    public ReturnResult<String> authentication() {
        String tokenManagement = authInformation.getAuthServerProperties().getTokenManagement();
        TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class).getExtension(tokenManagement);
        if (null == tokenResolver) {
            return ReturnResult.error(null, "认证服务器无法认证");
        }
        ReturnResult<UserResume> resolve = tokenResolver.resolve(cookie, token);
        if (!resolve.isSuccess()) {
            return ReturnResult.error( resolve.getMsg());
        }

        UserResume userResult = resolve.getData();
        String json = Json.toJson(userResult);
        return ReturnResult.ok(authInformation.encodeHex(json));
    }

}
