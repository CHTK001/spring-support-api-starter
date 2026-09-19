package com.chua.starter.oauth.sso.support.parser.handler;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.service.dto.setting.AuthRequestFactory;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.*;
import org.springframework.context.ApplicationContext;

/**
 * OAuth2 第三方登录认证处理器
 * <p>
 * 处理基于 OAuth2 协议的第三方平台登录认证，支持：
 * <ul>
 *   <li>Gitee、Github</li>
 *   <li>微信开放平台、微信公众号、企业微信</li>
 *   <li>QQ、支付宝、钉钉</li>
 *   <li>微博、百度</li>
 *   <li>Google、Microsoft</li>
 *   <li>抖音、飞书、淘宝等</li>
 * </ul>
 * </p>
 *
 * @since 2024/12/18
 * @author CH
 */
@Slf4j
@Spi("oauth2")
public class OAuth2AuthorizationHandler implements AuthorizationHandler {

    /** 认证状态缓存 */
    private final AuthStateCache authStateCache = AuthDefaultStateCache.INSTANCE;

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
        log.info("使用 OAuth2 第三方登录认证方式");

        try {
 // 从 令牌 中解析 OAuth2 回调参数
 // 格式: {platform}:{编码}:{状态} 或通过 认证callback 传递
            String[] parts = token.split(":", 3);
            if (parts.length < 3) {
                return ReturnResult.error("OAuth2 回调参数格式错误");
            }

            String platform = parts[0];
            String code = parts[1];
            String state = parts[2];

            // 从缓存获取配置信息
            String clientId = authStateCache.get(state + "_clientId");
            String clientSecret = authStateCache.get(state + "_clientSecret");
            String redirectUri = authStateCache.get(state + "_callback");

            if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
                return ReturnResult.error("OAuth2 配置信息已过期，请重新登录");
            }

            AuthConfig authConfig = AuthConfig.builder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .redirectUri(redirectUri)
                    .build();

 // 创建对应平台的 认证请求
            AuthRequest authRequest = AuthRequestFactory.createAuthRequest(platform, authConfig, authStateCache);
            if (authRequest == null) {
                return ReturnResult.error("不支持的 OAuth2 平台: " + platform);
            }

            // 执行登录
            AuthCallback authCallback = AuthCallback.builder()
                    .code(code)
                    .state(state)
                    .build();

            AuthResponse<AuthUser> authResponse = authRequest.login(authCallback);
            if (!authResponse.ok()) {
                log.warn("OAuth2 认证失败: platform={}, msg={}", platform, authResponse.getMsg());
                return ReturnResult.error("OAuth2 认证失败: " + authResponse.getMsg());
            }

            AuthUser authUser = authResponse.getData();
            
            // 构建用户信息
            UserResume userResume = buildUserResume(authUser, platform);
            
 // 添加appid支持：从请求中提取appid并设置到用户resume
            String appId = com.chua.common.support.utils.MapUtils.getString(authInformation.getBodyData(), "x-oauth-app-id");
            if (StringUtils.isBlank(appId)) {
                appId = authInformation.getRequestHeaderValue("x-oauth-app-id");
            }
            if (StringUtils.isNotBlank(appId)) {
                userResume.setAppId(appId);
                log.debug("OAuth2认证中提取到appId: {}", maskAppId(appId));
            }
            
            String json = Json.toJson(userResume);
            log.info("OAuth2 认证成功: platform={}, uuid={}, username={}, appId={}", 
                    platform, authUser.getUuid(), authUser.getUsername(),
                    userResume.getAppId() != null ? maskAppId(userResume.getAppId()) : "null");
            
            return ReturnResult.ok(authInformation.encodeHex(json));

        } catch (Exception e) {
            log.error("OAuth2 认证异常", e);
            return ReturnResult.error("认证失败: " + e.getMessage());
        }
    }

    /**
     * 构建用户信息
     * @param authUser 认证用户
     * @param platform platform
     * @return 构建用户resume的结果
     */
    private UserResume buildUserResume(AuthUser authUser, String platform) {
        UserResume userResume = new UserResume();
        
        // 使用第三方平台的 UUID 作为用户标识
        userResume.setUserId(authUser.getUuid());
        userResume.setUsername(authUser.getUsername());
        userResume.setNickName(authUser.getNickname());
        userResume.setOpenId(authUser.getUuid());
        userResume.setUnionId(authUser.getToken().getUnionId());
        
        // 设置登录类型
        userResume.setLoginType("OAUTH2_" + platform.toUpperCase());
        
        return userResume;
    }

    @Override
    /** 支持 */
    public boolean supports(String subProtocol, String token, String userCode) {
        // OAuth2 认证条件：
        // 1. subProtocol 为 "oauth2"
        // 2. 或者 token 包含 OAuth2 回调格式 (platform:code:state)
        if ("oauth2".equalsIgnoreCase(subProtocol)) {
            return true;
        }
        
        if (StringUtils.isNotBlank(token) && token.contains(":")) {
            String[] parts = token.split(":", 3);
            if (parts.length == 3) {
                String platform = parts[0].toLowerCase();
                return isSupportedPlatform(platform);
            }
        }
        
        return false;
    }

    /**
     * 检查是否支持该平台
     * @param platform platform
     * @return 是否支持platform的结果
     */
    private boolean isSupportedPlatform(String platform) {
        return switch (platform.toLowerCase()) {
            case "gitee", "github", "wechat", "wechat_mp", "wechat_enterprise",
                 "qq", "alipay", "dingtalk", "weibo", "baidu",
                 "google", "microsoft", "taobao", "douyin", "feishu" -> true;
            default -> false;
        };
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
}
