package com.chua.starter.oauth.sso.support.parser.handler;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.annotation.SpiDefault;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.MapUtils;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认认证处理器（Token认证）
 * <p>
 * 处理基于Token的标准认证请求，返回用户信息（包含指纹）由客户端决定是否验证
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Slf4j
@Spi("default")
@SpiDefault
public class DefaultAuthorizationHandler implements AuthorizationHandler {

    @Override
    public ReturnResult<String> authenticate(OauthAuthInformation authInformation,
                                            String token,
                                            String cookie,
                                            String userCode) {
        log.info("使用Token标准认证方式");
        
        String tokenManagement = authInformation.getAuthServerProperties().getTokenManagement();
        TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class).getExtension(tokenManagement);
        if (null == tokenResolver) {
            return ReturnResult.error("认证服务器无法认证");
        }
        
        ReturnResult<UserResume> resolve = tokenResolver.resolve(cookie, token);
        if (!resolve.isSuccess()) {
            return ReturnResult.error(resolve.getMsg());
        }

        UserResume userResult = resolve.getData();
        
        // 添加appId支持：从请求中提取appId并设置到UserResume
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
     * 脱敏AppId
     */
    private String maskAppId(String appId) {
        if (appId == null || appId.length() < 8) {
            return "***";
        }
        return appId.substring(0, 4) + "..." + appId.substring(appId.length() - 4);
    }

    @Override
    public boolean supports(String subProtocol, String token, String userCode) {
        // Token认证：有token参数，无userCode参数
        return StringUtils.isNotBlank(token) && StringUtils.isBlank(userCode);
    }
}
