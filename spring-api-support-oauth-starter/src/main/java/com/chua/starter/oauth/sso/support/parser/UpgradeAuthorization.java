package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.core.AuthCoreService;
import com.chua.starter.oauth.sso.support.information.UpgradeAuthInformation;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * 鉴权信息
 *
 * @author CH
 * @since 4.0.0
 */
@Slf4j
public final class UpgradeAuthorization implements Authorization {
    /**
     * 令牌
    */
    private final String token;
    /**
     * Cookie
    */
    private final String cookie;
    /**
     * Refresh令牌
    */
    private final String refreshToken;
    /**
     * Upgrade类型
    */
    private final String upgradeType;
    final ApplicationContext applicationContext; // application上下文
    private final AuthCoreService authCoreService; // 认证核心服务
    /**
     * Authinformation
    */
    private final UpgradeAuthInformation authInformation;

    /**
     * 创建 upgrade授权 实例
     * @param authInformation 认证信息
     * @param token 令牌
     * @param cookie Cookie
     * @param refreshToken refresh令牌
     * @param upgradeType upgrade类型
     * @param applicationContext application上下文
     * @param authCoreService 认证核心服务
     */
    public UpgradeAuthorization(UpgradeAuthInformation authInformation,
                                String token,
                                String cookie,
                                String refreshToken,
                                String upgradeType,
                                ApplicationContext applicationContext,
                                AuthCoreService authCoreService) {
        this.authInformation = authInformation;
        this.token = token;
        this.cookie = cookie;
        this.refreshToken = refreshToken;
        this.upgradeType = upgradeType;
        this.applicationContext = applicationContext;
        this.authCoreService = authCoreService;
    }

    @Override
    /**
     * 认证
    */
    public ReturnResult<String> authentication() {
        long startTime = System.currentTimeMillis();
        log.info("[Upgrade] 开始: type={}, token={}...", upgradeType,
                token != null && token.length() > 8 ? token.substring(0, 8) : token);

        ReturnResult<UserResume> returnResult =
                authCoreService.upgrade(upgradeType, token, refreshToken);

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
