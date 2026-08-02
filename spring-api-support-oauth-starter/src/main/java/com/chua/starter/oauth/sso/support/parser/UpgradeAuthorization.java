package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.information.UpgradeAuthInformation;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * 鉴权信息
 *
 * @author CH
 */
@Slf4j
public final class UpgradeAuthorization implements Authorization {
    private final String token;
    private final String cookie;
    private final String refreshToken;
    private final String upgradeType;
    final ApplicationContext applicationContext;
    final LoginCheck loginCheck;
    private final UpgradeAuthInformation authInformation;

    public UpgradeAuthorization(UpgradeAuthInformation authInformation,
                                String token,
                                String cookie,
                                String refreshToken,
                                String upgradeType,
                                ApplicationContext applicationContext,
                                LoginCheck loginCheck) {
        this.authInformation = authInformation;
        this.token = token;
        this.cookie = cookie;
        this.refreshToken = refreshToken;
        this.upgradeType = upgradeType;
        this.applicationContext = applicationContext;
        this.loginCheck = loginCheck;
    }

    @Override
    public ReturnResult<String> authentication() {
        long startTime = System.currentTimeMillis();
        String tokenManagement = authInformation.getAuthServerProperties().getTokenManagement();
        TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class).getExtension(tokenManagement);
        if (null == tokenResolver) {
            return ReturnResult.error("认证服务器无法认证");
        }

        log.info("[Upgrade] 开始: type={}, token={}...", upgradeType,
                token != null && token.length() > 8 ? token.substring(0, 8) : token);

        ReturnResult<UserResume> returnResult;
        if ("refresh".equalsIgnoreCase(upgradeType)) {
            returnResult = tokenResolver.upgradeForRefresh(token, refreshToken);
        } else if ("version".equalsIgnoreCase(upgradeType)) {
            returnResult = tokenResolver.upgradeForVersion(token, refreshToken);
        } else {
            returnResult = tokenResolver.upgradeForTimestamp(token, refreshToken);
        }

        long cost = System.currentTimeMillis() - startTime;
        if (!returnResult.isSuccess()) {
            log.warn("[Upgrade] 失败: type={}, 原因={}, 耗时={}ms", upgradeType, returnResult.getMsg(), cost);
            return ReturnResult.error(returnResult.getMsg());
        }

        log.info("[Upgrade] 成功: type={}, userId={}, 耗时={}ms",
                upgradeType, returnResult.getData() != null ? returnResult.getData().getUserId() : "?", cost);

        UserResume userResult = returnResult.getData();
        String json = Json.toJson(userResult);
        return ReturnResult.ok(authInformation.encodeHex(json));
    }

}
