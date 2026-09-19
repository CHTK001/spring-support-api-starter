package com.chua.starter.oauth.sso.support.parser.handler;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.spi.annotations.SpiDefault;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认认证处理器（令牌认证）
 * <p>
 * 处理基于令牌的标准认证请求，返回用户信息（包含指纹）由客户端决定是否验证
 * </p>
 *
 * @since 2024/12/11
 * @author CH
 */
@Slf4j
@Spi("default")
@SpiDefault
public class DefaultAuthorizationHandler implements AuthorizationHandler {

    @Override
    /**
     * Authenticate
     * @param authInformation 认证信息
     * @param token 令牌
     * @param cookie Cookie
     * @param userCode 用户编码
     */
    public ReturnResult<String> authenticate(OauthAuthInformation authInformation,
                                            String token,
                                            String cookie,
                                            String userCode) {
        log.info("使用Token标准认证方式");
        
        String tokenManagement = authInformation.getAuthServerProperties().getTokenManagement();
        TokenResolver tokenResolver = null;
        LoginCheck loginCheck = authInformation.getLoginCheck();
        if (loginCheck != null) {
            tokenResolver = loginCheck.resolveTokenResolver(tokenManagement);
        }
        if (null == tokenResolver) {
            tokenResolver = ServiceProvider.of(TokenResolver.class).getExtension(tokenManagement);
        }
        if (null == tokenResolver) {
            return ReturnResult.error("认证服务器无法认证");
        }
        
        ReturnResult<UserResume> resolve = tokenResolver.resolve(cookie, token);
        if (!resolve.isSuccess()) {
            return ReturnResult.error(resolve.getMsg());
        }

        UserResume userResult = resolve.getData();
        
 // 添加appid支持：从请求中提取appid并设置到用户resume
        String appId = MapUtils.getString(authInformation.getBodyData(), "x-oauth-app-id");
        if (StringUtils.isBlank(appId)) {
            appId = authInformation.getRequestHeaderValue("x-oauth-app-id");
        }
        if (StringUtils.isNotBlank(appId)) {
            userResult.setAppId(appId);
            log.debug("Token认证中提取到appId: {}", maskAppId(appId));
        }
        
        String json = Json.toJson(userResult);
        
        log.info("Token认证成功: userId={}, appId={}", 
                userResult.getUserId(), 
                userResult.getAppId() != null ? maskAppId(userResult.getAppId()) : "null");
        return ReturnResult.ok(authInformation.encodeHex(json));
    }
    
    /**
     * 脱敏appid
     * @param appId appId
     * @return maskAppId的结果
     */
    private String maskAppId(String appId) {
        if (appId == null || appId.length() < 8) {
            return "***";
        }
        return appId.substring(0, 4) + "..." + appId.substring(appId.length() - 4);
    }

    @Override
    /** 支持 */
    public boolean supports(String subProtocol, String token, String userCode) {
 // 令牌认证：有令牌参数，无用户编码参数
        return StringUtils.isNotBlank(token) && StringUtils.isBlank(userCode);
    }
}
