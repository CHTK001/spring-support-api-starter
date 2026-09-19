package com.chua.starter.oauth.sso.support.parser.handler;

import com.chua.common.support.spi.annotations.Spi;
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
 * Bearer 令牌 认证处理器
 * <p>
 * 处理标准的 授权: Bearer &lt;令牌&gt; 认证请求。
 * 通过 SPI 机制由 sub协议="bearer" 匹配选择，
 * 从请求头或 主体数据 中提取 令牌 后调用 令牌解析器 验证。
 * </p>
 *
 * @author CH
 * @since 4.0.0
 */
@Slf4j
@Spi("bearer")
public class BearerAuthorizationHandler implements AuthorizationHandler {

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
        log.info("使用 Bearer Token 认证方式");

 // 从 认证信息 提取 Bearer 令牌（兜底：从请求头取）
        String bearerToken = token;
        if (StringUtils.isBlank(bearerToken)) {
            bearerToken = extractBearerToken(authInformation);
        }

        if (StringUtils.isBlank(bearerToken)) {
            return ReturnResult.error("Bearer Token 不能为空");
        }

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

        ReturnResult<UserResume> resolve = tokenResolver.resolve(cookie, bearerToken);
        if (!resolve.isSuccess()) {
            return ReturnResult.error(resolve.getMsg());
        }

        UserResume userResult = resolve.getData();

 // 添加appid支持
        String appId = MapUtils.getString(authInformation.getBodyData(), "x-oauth-app-id");
        if (StringUtils.isBlank(appId)) {
            appId = authInformation.getRequestHeaderValue("x-oauth-app-id");
        }
        if (StringUtils.isNotBlank(appId)) {
            userResult.setAppId(appId);
        }

        String json = Json.toJson(userResult);
        log.info("Bearer Token 认证成功: userId={}", userResult.getUserId());
        return ReturnResult.ok(authInformation.encodeHex(json));
    }

    /**
     * 从请求头 授权: Bearer 提取 令牌
     * @param authInformation 认证信息
     * @return extractbearer令牌的结果
     */
    private String extractBearerToken(OauthAuthInformation authInformation) {
        String authHeader = authInformation.getRequestHeaderValue("Authorization");
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    @Override
    /**
     * 支持
    */
    public boolean supports(String subProtocol, String token, String userCode) {
        // 1. subProtocol 明确为 "bearer"
        if ("bearer".equalsIgnoreCase(subProtocol)) {
            return true;
        }
        // 2. 或者 token 以 "Bearer " 开头（直接访问的场景）
        if (StringUtils.isNotBlank(token) && token.startsWith("Bearer ")) {
            return true;
        }
        return false;
    }
}