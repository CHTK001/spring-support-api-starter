package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.network.net.NetAddress;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.properties.ThirdPartyLoginProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.*;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用 OAuth2 第三方登录控制器
 * <p>
 * 提供统一的第三方平台 OAuth2 登录接口，支持：
 * <ul>
 *   <li>获取授权 URL</li>
 *   <li>处理授权回调</li>
 *   <li>支持多平台：Gitee、Github、微信、QQ、钉钉等</li>
 * </ul>
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/18
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${plugin.auth.server.context-path:}/oauth2")
@Tag(name = "OAuth2 第三方登录", description = "提供多平台第三方 OAuth2 登录功能")
public class OAuth2LoginController {

    private final ThirdPartyLoginProperties thirdPartyLoginProperties;
    private final AuthServerProperties authServerProperties;
    private final LoginCheck loginCheck;
    private final AuthStateCache authStateCache = AuthDefaultStateCache.INSTANCE;

    @Value("${plugin.auth.server.context-path:}")
    private String contextPath;

    /**
     * 获取支持的第三方登录平台列表
     *
     * @return 平台列表
     */
    @GetMapping("/platforms")
    @Operation(summary = "获取支持的平台列表", description = "返回所有已启用的第三方登录平台")
    public ReturnResult<Map<String, Boolean>> getPlatforms() {
        Map<String, Boolean> platforms = new HashMap<>();
        platforms.put("gitee", thirdPartyLoginProperties.getGitee().isEnabled());
        platforms.put("github", thirdPartyLoginProperties.getGithub().isEnabled());
        platforms.put("wechat", thirdPartyLoginProperties.getWechat().isEnabled());
        platforms.put("wechat_mp", thirdPartyLoginProperties.getWechatMp().isEnabled());
        platforms.put("qq", thirdPartyLoginProperties.getQq().isEnabled());
        platforms.put("alipay", thirdPartyLoginProperties.getAlipay().isEnabled());
        platforms.put("dingtalk", thirdPartyLoginProperties.getDingtalk().isEnabled());
        platforms.put("wechat_enterprise", thirdPartyLoginProperties.getWechatEnterprise().isEnabled());
        platforms.put("weibo", thirdPartyLoginProperties.getWeibo().isEnabled());
        platforms.put("baidu", thirdPartyLoginProperties.getBaidu().isEnabled());
        platforms.put("google", thirdPartyLoginProperties.getGoogle().isEnabled());
        platforms.put("microsoft", thirdPartyLoginProperties.getMicrosoft().isEnabled());
        return ReturnResult.ok(platforms);
    }

    /**
     * 获取第三方平台授权 URL
     *
     * @param platform 平台标识
     * @param callback 登录成功后的回调地址
     * @return 授权 URL
     */
    @GetMapping("/authorize/{platform}")
    @Operation(summary = "获取授权URL", description = "获取跳转到第三方平台的授权URL")
    @ApiResponse(responseCode = "200", description = "返回授权URL")
    public ReturnResult<String> getAuthorizeUrl(
            @Parameter(description = "平台标识", required = true) @PathVariable String platform,
            @Parameter(description = "登录成功后的回调地址") @RequestParam(required = false) String callback) {

        ThirdPartyLoginProperties.Base config = getPlatformConfig(platform);
        if (config == null || !config.isEnabled()) {
            return ReturnResult.error("平台未启用或不支持: " + platform);
        }

        AuthConfig authConfig = AuthConfig.builder()
                .clientId(config.getClientId())
                .clientSecret(config.getClientSecret())
                .redirectUri(config.getRedirectUri())
                .build();

        AuthRequest authRequest = createAuthRequest(platform, authConfig);
        if (authRequest == null) {
            return ReturnResult.error("不支持的平台: " + platform);
        }

        String state = AuthStateUtils.createState();
        
        // 缓存配置信息供回调使用
        authStateCache.cache(state + "_platform", platform);
        authStateCache.cache(state + "_clientId", config.getClientId());
        authStateCache.cache(state + "_clientSecret", config.getClientSecret());
        authStateCache.cache(state + "_callback", config.getRedirectUri());
        authStateCache.cache(state + "_system_callback", StringUtils.defaultString(callback, "/"));

        String authorizeUrl = authRequest.authorize(state);
        log.info("生成 OAuth2 授权URL: platform={}, state={}", platform, state);
        
        return ReturnResult.ok(authorizeUrl);
    }

    /**
     * 第三方平台授权回调
     *
     * @param platform 平台标识
     * @param authCallback 回调参数
     * @param request 请求
     * @param response 响应
     * @return 重定向视图
     */
    @GetMapping("/callback/{platform}")
    @Operation(summary = "授权回调", description = "处理第三方平台的授权回调")
    public RedirectView callback(
            @Parameter(description = "平台标识") @PathVariable String platform,
            AuthCallback authCallback,
            HttpServletRequest request,
            HttpServletResponse response) {

        RedirectView view = new RedirectView();
        String state = authCallback.getState();
        String systemCallback = authStateCache.get(state + "_system_callback");
        view.setUrl(StringUtils.defaultString(systemCallback, "/"));

        try {
            // 获取缓存的配置
            String clientId = authStateCache.get(state + "_clientId");
            String clientSecret = authStateCache.get(state + "_clientSecret");
            String redirectUri = authStateCache.get(state + "_callback");

            if (StringUtils.isBlank(clientId)) {
                log.warn("OAuth2 回调配置已过期: platform={}, state={}", platform, state);
                addErrorCookie(response, "授权已过期，请重新登录", systemCallback);
                return view;
            }

            AuthConfig authConfig = AuthConfig.builder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .redirectUri(redirectUri)
                    .build();

            AuthRequest authRequest = createAuthRequest(platform, authConfig);
            if (authRequest == null) {
                addErrorCookie(response, "不支持的登录平台", systemCallback);
                return view;
            }

            // 执行登录
            AuthResponse<AuthUser> authResponse = authRequest.login(authCallback);
            if (!authResponse.ok()) {
                log.warn("OAuth2 授权失败: platform={}, msg={}", platform, authResponse.getMsg());
                addErrorCookie(response, "授权失败: " + authResponse.getMsg(), systemCallback);
                return view;
            }

            AuthUser authUser = authResponse.getData();
            
            // 调用登录检查，创建 Token
            ReturnResult<LoginResult> loginResultRet = loginCheck.doLogin(
                    RequestUtils.getIpAddress(request),
                    authUser.getUuid(),
                    authUser.getUsername(),
                    platform,
                    authUser
            );

            if (loginResultRet != null && loginResultRet.isOk()) {
                LoginResult loginResult = loginResultRet.getData();
                
                // 设置 Cookie
                Cookie cookie = new Cookie(authServerProperties.getCookieName(), loginResult.getToken());
                cookie.setPath("/");
                if (StringUtils.isNotBlank(systemCallback)) {
                    NetAddress netAddress = NetAddress.of(systemCallback);
                    cookie.setDomain(netAddress.getHost());
                }
                cookie.setMaxAge(Integer.MAX_VALUE);
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
                
                response.setHeader("Access-Control-Allow-Credentials", "true");
                
                log.info("OAuth2 登录成功: platform={}, uuid={}, username={}", 
                        platform, authUser.getUuid(), authUser.getUsername());
            } else {
                String errorMsg = loginResultRet != null ? loginResultRet.getMsg() : "登录失败，请先绑定账号";
                addErrorCookie(response, errorMsg, systemCallback);
            }

        } catch (Exception e) {
            log.error("OAuth2 回调处理异常: platform={}", platform, e);
            addErrorCookie(response, "登录异常: " + e.getMessage(), systemCallback);
        }

        return view;
    }

    /**
     * API 方式的授权回调（返回 JSON）
     *
     * @param platform 平台标识
     * @param code 授权码
     * @param state 状态码
     * @param request 请求
     * @return 登录结果
     */
    @PostMapping("/callback/{platform}")
    @Operation(summary = "API授权回调", description = "以API方式处理第三方平台的授权回调，返回JSON")
    public ReturnResult<LoginResult> callbackApi(
            @Parameter(description = "平台标识") @PathVariable String platform,
            @Parameter(description = "授权码") @RequestParam String code,
            @Parameter(description = "状态码") @RequestParam String state,
            HttpServletRequest request) {

        try {
            // 获取缓存的配置
            String clientId = authStateCache.get(state + "_clientId");
            String clientSecret = authStateCache.get(state + "_clientSecret");
            String redirectUri = authStateCache.get(state + "_callback");

            if (StringUtils.isBlank(clientId)) {
                return ReturnResult.error("授权已过期，请重新登录");
            }

            AuthConfig authConfig = AuthConfig.builder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .redirectUri(redirectUri)
                    .build();

            AuthRequest authRequest = createAuthRequest(platform, authConfig);
            if (authRequest == null) {
                return ReturnResult.error("不支持的平台: " + platform);
            }

            AuthCallback authCallback = AuthCallback.builder()
                    .code(code)
                    .state(state)
                    .build();

            AuthResponse<AuthUser> authResponse = authRequest.login(authCallback);
            if (!authResponse.ok()) {
                return ReturnResult.error("授权失败: " + authResponse.getMsg());
            }

            AuthUser authUser = authResponse.getData();
            
            ReturnResult<LoginResult> loginResultRet = loginCheck.doLogin(
                    RequestUtils.getIpAddress(request),
                    authUser.getUuid(),
                    authUser.getUsername(),
                    platform,
                    authUser
            );

            if (loginResultRet != null && loginResultRet.isOk()) {
                log.info("OAuth2 API登录成功: platform={}, uuid={}", platform, authUser.getUuid());
                return loginResultRet;
            } else {
                return ReturnResult.error(loginResultRet != null ? loginResultRet.getMsg() : "登录失败");
            }

        } catch (Exception e) {
            log.error("OAuth2 API回调处理异常: platform={}", platform, e);
            return ReturnResult.error("登录异常: " + e.getMessage());
        }
    }

    /**
     * 获取平台配置
     */
    private ThirdPartyLoginProperties.Base getPlatformConfig(String platform) {
        return switch (platform.toLowerCase()) {
            case "gitee" -> thirdPartyLoginProperties.getGitee();
            case "github" -> thirdPartyLoginProperties.getGithub();
            case "wechat" -> thirdPartyLoginProperties.getWechat();
            case "wechat_mp" -> thirdPartyLoginProperties.getWechatMp();
            case "qq" -> thirdPartyLoginProperties.getQq();
            case "alipay" -> thirdPartyLoginProperties.getAlipay();
            case "dingtalk" -> thirdPartyLoginProperties.getDingtalk();
            case "wechat_enterprise" -> thirdPartyLoginProperties.getWechatEnterprise();
            case "weibo" -> thirdPartyLoginProperties.getWeibo();
            case "baidu" -> thirdPartyLoginProperties.getBaidu();
            case "google" -> thirdPartyLoginProperties.getGoogle();
            case "microsoft" -> thirdPartyLoginProperties.getMicrosoft();
            default -> null;
        };
    }

    /**
     * 创建 AuthRequest
     */
    private AuthRequest createAuthRequest(String platform, AuthConfig authConfig) {
        return switch (platform.toLowerCase()) {
            case "gitee" -> new AuthGiteeRequest(authConfig, authStateCache);
            case "github" -> new AuthGithubRequest(authConfig, authStateCache);
            case "wechat" -> new AuthWeChatOpenRequest(authConfig, authStateCache);
            case "wechat_mp" -> new AuthWeChatMpRequest(authConfig, authStateCache);
            case "wechat_enterprise" -> new AuthWeChatEnterpriseQrcodeRequest(authConfig, authStateCache);
            case "qq" -> new AuthQqRequest(authConfig, authStateCache);
            case "alipay" -> new AuthAlipayRequest(authConfig, authStateCache);
            case "dingtalk" -> new AuthDingTalkRequest(authConfig, authStateCache);
            case "weibo" -> new AuthWeiboRequest(authConfig, authStateCache);
            case "baidu" -> new AuthBaiduRequest(authConfig, authStateCache);
            case "google" -> new AuthGoogleRequest(authConfig, authStateCache);
            case "microsoft" -> new AuthMicrosoftRequest(authConfig, authStateCache);
            case "taobao" -> new AuthTaobaoRequest(authConfig, authStateCache);
            case "douyin" -> new AuthDouyinRequest(authConfig, authStateCache);
            case "feishu" -> new AuthFeishuRequest(authConfig, authStateCache);
            default -> null;
        };
    }

    /**
     * 添加错误信息到 Cookie
     */
    private void addErrorCookie(HttpServletResponse response, String message, String callback) {
        try {
            Cookie cookie = new Cookie("Access-Message", 
                    URLEncoder.encode(URLEncoder.encode(message, StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            cookie.setPath("/");
            if (StringUtils.isNotBlank(callback)) {
                NetAddress netAddress = NetAddress.of(callback);
                cookie.setDomain(netAddress.getHost());
            }
            cookie.setMaxAge(60);
            response.addCookie(cookie);
        } catch (Exception e) {
            log.warn("设置错误Cookie失败: {}", e.getMessage());
        }
    }
}
