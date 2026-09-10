package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.protocol.support.network.net.NetAddress;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.properties.ThirdPartyLoginProperties;
import com.chua.starter.service.dto.setting.AuthRequestFactory;
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
     * <p>
     * 返回所有已配置的第三方登录平台及其启用状态。
     * 前端可以根据此接口动态显示可用的登录方式。
     * </p>
     * 
     * <h3>支持的平台</h3>
     * <ul>
     *   <li><b>gitee</b> - Gitee 码云登录</li>
     *   <li><b>github</b> - Github 登录</li>
     *   <li><b>wechat_open</b> - 微信开放平台（网站应用扫码登录）</li>
     *   <li><b>wechat_mp</b> - 微信公众号（网页授权登录）</li>
     *   <li><b>wechat_mini</b> - 微信小程序登录</li>
     *   <li><b>wechat_enterprise</b> - 企业微信登录</li>
     *   <li><b>qq</b> - QQ互联登录</li>
     *   <li><b>alipay</b> - 支付宝登录</li>
     *   <li><b>dingtalk</b> - 钉钉登录</li>
     *   <li><b>weibo</b> - 新浪微博登录</li>
     *   <li><b>baidu</b> - 百度登录</li>
     *   <li><b>google</b> - Google 登录</li>
     *   <li><b>microsoft</b> - Microsoft 登录</li>
     * </ul>
     * 
     * <h3>返回示例</h3>
     * <pre>{@code
     * {
     *   "code": 200,
     *   "data": {
     *     "gitee": true,
     *     "github": true,
     *     "wechat_open": false,
     *     "google": true
     *   }
     * }
     * }</pre>
     *
     * @return 平台列表，key为平台标识，value为是否启用
     */
    @GetMapping("/platforms")
    @Operation(summary = "获取支持的平台列表", description = "返回所有已启用的第三方登录平台")
    public ReturnResult<Map<String, Boolean>> getPlatforms() {
        Map<String, Boolean> platforms = new HashMap<>();
        platforms.put("gitee", thirdPartyLoginProperties.getGitee().isEnabled());
        platforms.put("github", thirdPartyLoginProperties.getGithub().isEnabled());
        platforms.put("wechat_open", thirdPartyLoginProperties.getWechatOpen().isEnabled());
        platforms.put("wechat_mp", thirdPartyLoginProperties.getWechatMp().isEnabled());
        platforms.put("wechat_mini", thirdPartyLoginProperties.getWechatMini().isEnabled());
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
     * <p>
     * 生成跳转到第三方平台的授权URL，用户访问此URL后将跳转到第三方平台进行授权。
     * 授权成功后，第三方平台会重定向到配置的回调地址。
     * </p>
     * 
     * <h3>使用流程</h3>
     * <ol>
     *   <li>前端调用此接口获取授权URL</li>
     *   <li>前端跳转到授权URL</li>
     *   <li>用户在第三方平台完成授权</li>
     *   <li>第三方平台重定向到回调地址</li>
     *   <li>后端处理回调，完成登录</li>
     * </ol>
     * 
     * <h3>平台标识说明</h3>
     * <ul>
     *   <li><b>gitee</b> - Gitee 码云
     *     <ul>
     *       <li>申请地址：<a href="https://gitee.com/oauth/applications">https://gitee.com/oauth/applications</a></li>
     *       <li>所需材料：Gitee账号（已实名认证）</li>
     *       <li>回调地址格式：http://your-domain/oauth2/callback/gitee</li>
     *     </ul>
     *   </li>
     *   <li><b>github</b> - Github
     *     <ul>
     *       <li>申请地址：<a href="https://github.com/settings/developers">https://github.com/settings/developers</a></li>
     *       <li>所需材料：Github账号</li>
     *       <li>回调地址格式：http://your-domain/oauth2/callback/github</li>
     *     </ul>
     *   </li>
     *   <li><b>wechat_open</b> - 微信开放平台（网站应用扫码登录）
     *     <ul>
     *       <li>申请地址：<a href="https://open.weixin.qq.com/">https://open.weixin.qq.com/</a></li>
     *       <li>所需材料：企业营业执照、已备案域名</li>
     *       <li>回调地址格式：http://your-domain/oauth2/callback/wechat_open</li>
     *       <li>注意：需要企业资质认证</li>
     *     </ul>
     *   </li>
     *   <li><b>wechat_mp</b> - 微信公众号（网页授权登录）
     *     <ul>
     *       <li>申请地址：<a href="https://mp.weixin.qq.com/">https://mp.weixin.qq.com/</a></li>
     *       <li>所需材料：企业营业执照、已备案域名、认证费用300元/年</li>
     *       <li>回调地址格式：http://your-domain/oauth2/callback/wechat_mp</li>
     *       <li>注意：需要在微信内置浏览器中打开</li>
     *     </ul>
     *   </li>
     *   <li><b>wechat_mini</b> - 微信小程序
     *     <ul>
     *       <li>申请地址：<a href="https://mp.weixin.qq.com/">https://mp.weixin.qq.com/</a></li>
     *       <li>所需材料：微信账号、已备案域名（支持https）</li>
     *       <li>注意：小程序登录不使用此接口，请使用 POST /oauth2/callback/wechat_mini</li>
     *     </ul>
     *   </li>
     *   <li><b>wechat_enterprise</b> - 企业微信
     *     <ul>
     *       <li>申请地址：<a href="https://work.weixin.qq.com/">https://work.weixin.qq.com/</a></li>
     *       <li>所需材料：企业信息</li>
     *       <li>回调地址格式：http://your-domain/oauth2/callback/wechat_enterprise</li>
     *     </ul>
     *   </li>
     *   <li><b>google</b> - Google
     *     <ul>
     *       <li>申请地址：<a href="https://console.cloud.google.com/">https://console.cloud.google.com/</a></li>
     *       <li>所需材料：Google账号</li>
     *       <li>回调地址格式：http://your-domain/oauth2/callback/google</li>
     *       <li>注意：需要启用 Google+ API</li>
     *     </ul>
     *   </li>
     *   <li>其他平台：qq、alipay、dingtalk、weibo、baidu、microsoft</li>
     * </ul>
     * 
     * <h3>配置参数说明</h3>
     * <p>使用前需要在 application.yml 中配置相应平台的参数：</p>
     * <ul>
     *   <li><b>enabled</b> - 是否启用该平台，必须设置为 true</li>
     *   <li><b>client-id</b> - 第三方平台分配的应用ID（必填）</li>
     *   <li><b>client-secret</b> - 第三方平台分配的应用密钥（必填）</li>
     *   <li><b>redirect-uri</b> - 授权回调地址（必填），必须与第三方平台配置一致</li>
     *   <li><b>scope</b> - 权限范围（可选），不同平台支持的scope不同</li>
     * </ul>
     * 
     * <h3>常见错误</h3>
     * <ul>
     *   <li><b>平台未启用或不支持</b>：检查配置文件中该平台的 enabled 是否为 true</li>
     *   <li><b>不支持的平台</b>：检查 platform 参数是否正确</li>
     *   <li><b>配置缺失</b>：检查 client-id、client-secret、redirect-uri 是否已配置</li>
     * </ul>
     * 
     * <h3>请求示例</h3>
     * <pre>{@code
     * GET /oauth2/authorize/gitee?callback=http://your-domain/home
     * }</pre>
     * 
     * <h3>返回示例</h3>
     * <pre>{@code
     * {
     *   "code": 200,
     *   "data": "https://gitee.com/oauth/authorize?client_id=xxx&redirect_uri=xxx&state=xxx"
     * }
     * }</pre>
     *
     * @param platform 平台标识（gitee、github、wechat_open等）
     * @param callback 登录成功后的回调地址（可选），默认为 "/"
     * @return 授权 URL，前端应跳转到此URL
     * @see ThirdPartyLoginProperties 配置类
     */
    @GetMapping("/authorize/{platform}")
    @Operation(summary = "获取授权URL", description = "获取跳转到第三方平台的授权URL")
    @ApiResponse(responseCode = "200", description = "返回授权URL")
    public ReturnResult<String> getAuthorizeUrl(
            @Parameter(description = "平台标识", required = true) @PathVariable String platform,
            @Parameter(description = "登录成功后的回调地址") @RequestParam(required = false) String callback) {

        ThirdPartyLoginProperties.Base config = getPlatformConfig(platform);
        if (config == null || !config.isEnabled()) {
            log.warn("第三方登录平台未启用或不支持: platform={}", platform);
            return ReturnResult.error("登录失败：" + platform + " 平台未启用。" +
                    "解决方法：在配置文件中设置 plugin.oauth.third-party.login." + platform + ".enabled=true，" +
                    "并配置相应的 client-id 和 client-secret");
        }

        AuthConfig authConfig = AuthConfig.builder()
                .clientId(config.getClientId())
                .clientSecret(config.getClientSecret())
                .redirectUri(config.getRedirectUri())
                .build();

        AuthRequest authRequest = AuthRequestFactory.createAuthRequest(platform, authConfig, authStateCache);
        if (authRequest == null) {
            log.error("不支持的第三方登录平台: platform={}", platform);
            return ReturnResult.error("登录失败：不支持的平台 '" + platform + "'。" +
                    "当前支持的平台包括：gitee、github、wechat_open、wechat_mp、wechat_mini、wechat_enterprise、qq、alipay、dingtalk、weibo、baidu、google、microsoft。" +
                    "请检查平台标识是否正确");
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
     * 第三方平台授权回调（页面重定向方式）
     * 
     * <p>
     * 处理第三方平台的授权回调，完成用户登录流程。
     * 此接口由第三方平台在用户授权后自动调用，不需要前端主动调用。
     * </p>
     * 
     * <h3>回调流程</h3>
     * <ol>
     *   <li>用户在第三方平台完成授权</li>
     *   <li>第三方平台重定向到此接口，携带 code 和 state 参数</li>
     *   <li>后端使用 code 换取用户信息</li>
     *   <li>调用 LoginCheck 进行登录验证和用户绑定</li>
     *   <li>生成登录 Token 并设置到 Cookie</li>
     *   <li>重定向到系统回调地址</li>
     * </ol>
     * 
     * <h3>回调地址配置</h3>
     * <p>在第三方平台配置回调地址时，格式为：</p>
     * <pre>{@code
     * http://your-domain/oauth2/callback/{platform}
     * }</pre>
     * <p>例如：</p>
     * <ul>
     *   <li>Gitee: http://your-domain/oauth2/callback/gitee</li>
     *   <li>Github: http://your-domain/oauth2/callback/github</li>
     *   <li>微信开放平台: http://your-domain/oauth2/callback/wechat_open</li>
     * </ul>
     * 
     * <h3>错误处理说明</h3>
     * <p>当登录过程中出现错误时，会通过以下方式处理：</p>
     * <ul>
     *   <li><b>授权已过期</b>：state 参数对应的配置信息已过期（默认5分钟）
     *     <ul>
     *       <li>原因：用户在授权页面停留时间过长</li>
     *       <li>解决方案：重新发起授权请求</li>
     *     </ul>
     *   </li>
     *   <li><b>不支持的登录平台</b>：platform 参数不正确或平台未配置
     *     <ul>
     *       <li>原因：URL中的平台标识错误</li>
     *       <li>解决方案：检查回调地址中的平台标识是否正确</li>
     *     </ul>
     *   </li>
     *   <li><b>授权失败</b>：第三方平台返回错误
     *     <ul>
     *       <li>常见原因：
     *         <ul>
     *           <li>redirect_uri_mismatch：回调地址不匹配</li>
     *           <li>invalid_client：Client ID 或 Secret 错误</li>
     *           <li>access_denied：用户拒绝授权</li>
     *           <li>invalid_grant：授权码已使用或过期</li>
     *         </ul>
     *       </li>
     *       <li>解决方案：
     *         <ul>
     *           <li>检查配置文件中的 client-id、client-secret、redirect-uri 是否正确</li>
     *           <li>检查第三方平台配置的回调地址是否与代码中一致</li>
     *           <li>确保用户同意授权</li>
     *         </ul>
     *       </li>
     *     </ul>
     *   </li>
     *   <li><b>登录失败，请先绑定账号</b>：用户未绑定系统账号
     *     <ul>
     *       <li>原因：第三方账号首次登录，系统中不存在对应用户</li>
     *       <li>解决方案：实现 LoginCheck 接口，处理用户绑定逻辑</li>
     *     </ul>
     *   </li>
     *   <li><b>登录异常</b>：系统内部错误
     *     <ul>
     *       <li>原因：网络异常、配置错误、代码异常等</li>
     *       <li>解决方案：查看日志获取详细错误信息</li>
     *     </ul>
     *   </li>
     * </ul>
     * 
     * <h3>Cookie 说明</h3>
     * <ul>
     *   <li><b>登录成功</b>：设置 Token Cookie，名称由 plugin.auth.server.cookie-name 配置</li>
     *   <li><b>登录失败</b>：设置 Access-Message Cookie，包含错误信息，有效期60秒</li>
     * </ul>
     * 
     * <h3>注意事项</h3>
     * <ul>
     *   <li>回调地址必须与第三方平台配置完全一致（包括协议、域名、端口、路径）</li>
     *   <li>本地测试时，某些平台不支持 localhost，需要使用 127.0.0.1</li>
     *   <li>生产环境建议使用 https:// 协议</li>
     *   <li>微信公众号登录需要在微信内置浏览器中打开</li>
     * </ul>
     *
     * @param platform 平台标识
     * @param authCallback 回调参数（包含 code、state 等）
     * @param request HTTP 请求对象
     * @param response HTTP 响应对象
     * @return 重定向视图，跳转到系统回调地址
     * @see LoginCheck 登录检查接口
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
                addErrorCookie(response, "登录失败：授权已过期。" +
                        "可能原因：在授权页面停留时间过长（超过5分钟）。" +
                        "解决方法：请关闭当前页面，重新发起登录", systemCallback);
                return view;
            }

            AuthConfig authConfig = AuthConfig.builder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .redirectUri(redirectUri)
                    .build();

            AuthRequest authRequest = AuthRequestFactory.createAuthRequest(platform, authConfig, authStateCache);
            if (authRequest == null) {
                log.error("不支持的登录平台: platform={}", platform);
                addErrorCookie(response, "登录失败：不支持的平台 '" + platform + "'。" +
                        "解决方法：请检查回调地址中的平台标识是否正确，或联系系统管理员", systemCallback);
                return view;
            }

            // 执行登录
            AuthResponse<AuthUser> authResponse = authRequest.login(authCallback);
            if (!authResponse.ok()) {
                log.warn("OAuth2 授权失败: platform={}, code={}, msg={}", platform, authResponse.getCode(), authResponse.getMsg());
                String errorMsg = "登录失败：" + authResponse.getMsg();
                
                // 根据错误码提供更详细的错误信息和解决方案
                if (authResponse.getMsg().contains("redirect_uri")) {
                    errorMsg = "登录失败：回调地址不匹配。" +
                            "解决方法：请联系系统管理员检查配置文件中的 redirect-uri 是否与第三方平台配置一致";
                } else if (authResponse.getMsg().contains("client") || authResponse.getMsg().contains("appid")) {
                    errorMsg = "登录失败：应用凭证错误。" +
                            "解决方法：请联系系统管理员检查配置文件中的 client-id 和 client-secret 是否正确";
                } else if (authResponse.getMsg().contains("denied")) {
                    errorMsg = "登录失败：您拒绝了授权。" +
                            "解决方法：如需登录，请重新发起登录并同意授权";
                } else if (authResponse.getMsg().contains("grant") || authResponse.getMsg().contains("code")) {
                    errorMsg = "登录失败：授权码已失效。" +
                            "解决方法：请关闭当前页面，重新发起登录";
                }
                
                addErrorCookie(response, errorMsg, systemCallback);
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
                String errorMsg = loginResultRet != null ? loginResultRet.getMsg() : "登录失败";
                if (errorMsg.contains("绑定")) {
                    errorMsg = "登录失败：账号未绑定。" +
                            "这是您首次使用该第三方账号登录，需要先绑定系统账号。" +
                            "解决方法：请联系系统管理员或使用其他方式登录后在个人中心绑定第三方账号";
                } else {
                    errorMsg = "登录失败：" + errorMsg + "。" +
                            "解决方法：请稍后重试，或联系系统管理员";
                }
                log.warn("OAuth2 登录失败: platform={}, uuid={}, msg={}", 
                        platform, authUser.getUuid(), errorMsg);
                addErrorCookie(response, errorMsg, systemCallback);
            }

        } catch (Exception e) {
            log.error("OAuth2 回调处理异常: platform={}, error={}", platform, e.getMessage(), e);
            String errorMsg = "登录异常：系统处理登录请求时发生错误。" +
                    "错误信息：" + e.getMessage() + "。" +
                    "解决方法：请稍后重试，如问题持续存在请联系系统管理员";
            addErrorCookie(response, errorMsg, systemCallback);
        }

        return view;
    }

    /**
     * 微信小程序登录接口
     * 
     * <p>
     * 处理微信小程序的登录请求。微信小程序登录流程与其他 OAuth2 平台不同，
     * 小程序通过 wx.login() 获取 code，然后直接调用此接口完成登录。
     * </p>
     * 
     * <h3>登录流程</h3>
     * <ol>
     *   <li>小程序调用 wx.login() 获取临时登录凭证 code</li>
     *   <li>小程序将 code 发送到此接口</li>
     *   <li>后端使用 code 调用微信接口换取 session_key 和 openid</li>
     *   <li>后端根据 openid 查询或创建用户</li>
     *   <li>后端生成 Token 返回给小程序</li>
     *   <li>小程序保存 Token 用于后续请求</li>
     * </ol>
     * 
     * <h3>微信接口说明</h3>
     * <p>后端会调用微信接口：</p>
     * <pre>{@code
     * GET https://api.weixin.qq.com/sns/jscode2session
     * ?appid=APPID
     * &secret=SECRET
     * &js_code=CODE
     * &grant_type=authorization_code
     * }</pre>
     * 
     * <p>微信返回：</p>
     * <pre>{@code
     * {
     *   "openid": "用户唯一标识",
     *   "session_key": "会话密钥",
     *   "unionid": "用户在开放平台的唯一标识（可选）"
     * }
     * }</pre>
     * 
     * <h3>请求参数</h3>
     * <ul>
     *   <li><b>code</b> - 临时登录凭证（必填），由 wx.login() 获取</li>
     * </ul>
     * 
     * <h3>返回结果</h3>
     * <p>成功时返回：</p>
     * <pre>{@code
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "openid": "oxxxxxxxxxxxxxxxxxxxxxx",
     *     "userId": "123456",
     *     "username": "微信用户",
     *     "expireTime": 1640000000000
     *   }
     * }
     * }</pre>
     * 
     * <p>失败时返回：</p>
     * <pre>{@code
     * {
     *   "code": 500,
     *   "msg": "登录失败: code 已使用",
     *   "data": null
     * }
     * }</pre>
     * 
     * <h3>错误处理</h3>
     * <ul>
     *   <li><b>平台未启用</b>：检查配置文件中 wechat-mini.enabled 是否为 true</li>
     *   <li><b>code 已使用</b>：code 只能使用一次，需要重新调用 wx.login() 获取新 code</li>
     *   <li><b>code 过期</b>：code 有效期为 5 分钟，超时需要重新获取</li>
     *   <li><b>appid 不匹配</b>：检查配置的 client-id 是否正确</li>
     *   <li><b>appsecret 错误</b>：检查配置的 client-secret 是否正确</li>
     *   <li><b>登录失败</b>：用户未绑定系统账号或其他业务错误</li>
     * </ul>
     * 
     * <h3>配置要求</h3>
     * <pre>{@code
     * plugin:
     *   oauth:
     *     third-party:
     *       login:
     *         wechat-mini:
     *           enabled: true
     *           client-id: wx1234567890abcdef  # 小程序 AppID
     *           client-secret: 1234567890abcdef1234567890abcdef  # 小程序 AppSecret
     * }</pre>
     * 
     * <h3>小程序端示例</h3>
     * <pre>{@code
     * wx.login({
     *   success: (res) => {
     *     if (res.code) {
     *       wx.request({
     *         url: 'https://api.example.com/oauth2/login/wechat_mini',
     *         method: 'POST',
     *         data: { code: res.code },
     *         success: (response) => {
     *           wx.setStorageSync('token', response.data.data.token);
     *         }
     *       });
     *     }
     *   }
     * });
     * }</pre>
     * 
     * <h3>注意事项</h3>
     * <ul>
     *   <li>code 只能使用一次，使用后立即失效</li>
     *   <li>code 有效期为 5 分钟</li>
     *   <li>session_key 有效期为 3 天，用于解密用户信息</li>
     *   <li>不要将 session_key 返回给前端</li>
     *   <li>服务器域名必须配置在小程序后台且支持 https</li>
     *   <li>开发阶段可以在开发者工具中勾选"不校验合法域名"</li>
     * </ul>
     * 
     * <h3>参考文档</h3>
     * <ul>
     *   <li><a href="https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html">微信小程序登录文档</a></li>
     *   <li><a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html">code2Session 接口文档</a></li>
     * </ul>
     *
     * @param code 临时登录凭证，由 wx.login() 获取
     * @param request HTTP 请求对象
     * @return 登录结果，包含 Token 和 openid 等信息
     */
    @PostMapping("/login/wechat_mini")
    @Operation(summary = "微信小程序登录", description = "处理微信小程序的登录请求")
    public ReturnResult<LoginResult> wechatMiniLogin(
            @Parameter(description = "临时登录凭证", required = true) @RequestParam String code,
            HttpServletRequest request) {

        try {
            // 检查配置
            ThirdPartyLoginProperties.Base config = thirdPartyLoginProperties.getWechatMini();
            if (config == null || !config.isEnabled()) {
                log.warn("微信小程序登录未启用: enabled={}", config != null ? config.isEnabled() : "null");
                return ReturnResult.error("微信小程序登录未启用。请检查配置文件中 plugin.oauth.third-party.login.wechat-mini.enabled 是否为 true");
            }

            String appId = config.getClientId();
            String appSecret = config.getClientSecret();

            if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecret)) {
                log.error("微信小程序配置不完整: appId={}, appSecret={}", appId, StringUtils.isBlank(appSecret) ? "未配置" : "已配置");
                return ReturnResult.error("登录失败：微信小程序配置不完整。" +
                        "解决方法：请联系系统管理员检查配置文件中的 client-id（小程序AppID）和 client-secret（小程序AppSecret）是否已正确配置");
            }

            // 调用微信接口换取 session_key 和 openid
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    appId, appSecret, code
            );

            log.info("微信小程序登录: code={}, appId={}", code, appId);

            // 调用微信接口
            java.net.HttpURLConnection connection = null;
            try {
                java.net.URL apiUrl = new java.net.URL(url);
                connection = (java.net.HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    log.error("微信接口调用失败: responseCode={}", responseCode);
                    return ReturnResult.error("登录失败：微信服务器响应异常（HTTP " + responseCode + "）。" +
                            "解决方法：请稍后重试，如问题持续存在请联系系统管理员");
                }
                
                // 读取响应
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                String responseBody = response.toString();
                log.info("微信接口响应: {}", responseBody);
                
                // 解析响应（简单的JSON解析）
                // 响应格式: {"session_key":"xxx","openid":"xxx"} 或 {"errcode":40029,"errmsg":"invalid code"}
                if (responseBody.contains("\"errcode\"")) {
                    // 提取错误信息
                    String errcode = extractJsonValue(responseBody, "errcode");
                    String errmsg = extractJsonValue(responseBody, "errmsg");
                    log.error("微信登录失败: errcode={}, errmsg={}", errcode, errmsg);
                    
                    // 根据错误码提供更友好的错误提示
                    String userFriendlyMsg = switch (errcode) {
                        case "40029" -> "登录失败：登录凭证已失效。解决方法：请重新获取登录凭证（code）";
                        case "40163" -> "登录失败：登录凭证已使用。解决方法：每个登录凭证只能使用一次，请重新获取";
                        case "40125" -> "登录失败：小程序AppSecret错误。解决方法：请联系系统管理员检查配置";
                        case "-1" -> "登录失败：微信服务器繁忙。解决方法：请稍后重试";
                        default -> "登录失败：" + errmsg + "（错误码：" + errcode + "）。解决方法：请稍后重试或联系系统管理员";
                    };
                    
                    return ReturnResult.error(userFriendlyMsg);
                }
                
                String openid = extractJsonValue(responseBody, "openid");
                String sessionKey = extractJsonValue(responseBody, "session_key");
                
                if (StringUtils.isBlank(openid)) {
                    log.error("微信接口返回数据异常: 未获取到openid");
                    return ReturnResult.error("登录失败：微信接口返回数据异常，未获取到用户标识。" +
                            "解决方法：请稍后重试，如问题持续存在请联系系统管理员");
                }
                
                // 缓存 session_key 用于后续解密手机号等加密数据
                // session_key 有效期为 3 天，这里设置缓存时间为 3 天（259200 秒）
                if (StringUtils.isNotBlank(sessionKey)) {
                    authStateCache.cache("wechat_mini_session_" + openid, sessionKey, 259200000L);
                    log.debug("缓存 session_key: openid={}", openid);
                }
                
                log.info("微信小程序登录成功: openid={}", openid);
                
                // 构造AuthUser对象
                AuthUser authUser = AuthUser.builder()
                        .uuid(openid)
                        .username(openid)
                        .source("wechat_mini")
                        .build();
                
                // 调用登录检查，生成Token
                LoginResult loginResult = loginCheck.check(authUser, request);
                
                if (loginResult == null) {
                    log.error("登录检查失败: loginResult为null");
                    return ReturnResult.error("登录失败：账号未绑定。" +
                            "这是您首次使用该微信小程序登录，需要先绑定系统账号。" +
                            "解决方法：请联系系统管理员或使用其他方式登录后在个人中心绑定微信小程序");
                }
                
                log.info("微信小程序登录完成: userId={}", loginResult.getUserResume() != null ? loginResult.getUserResume().getUserId() : "null");
                return ReturnResult.ok(loginResult);
                
            } catch (java.io.IOException e) {
                log.error("调用微信接口异常: error={}", e.getMessage(), e);
                return ReturnResult.error("登录失败：网络连接异常，无法连接到微信服务器。" +
                        "错误信息：" + e.getMessage() + "。" +
                        "解决方法：请检查网络连接后重试");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        } catch (Exception e) {
            log.error("微信小程序登录异常: error={}", e.getMessage(), e);
            return ReturnResult.error("登录异常：系统处理登录请求时发生错误。" +
                    "错误信息：" + e.getMessage() + "。" +
                    "解决方法：请稍后重试，如问题持续存在请联系系统管理员");
        }
    }
    /**
     * 微信小程序手机号授权接口
     *
     * <p>
     * 处理微信小程序的手机号授权请求。小程序通过 button 组件的 open-type="getPhoneNumber"
     * 获取用户手机号，微信会返回加密数据，需要后端使用 session_key 进行解密。
     * </p>
     *
     * <h3>授权流程</h3>
     * <ol>
     *   <li>用户在小程序中点击授权按钮（open-type="getPhoneNumber"）</li>
     *   <li>用户同意授权后，小程序获取到加密数据（encryptedData、iv）</li>
     *   <li>小程序将加密数据和 openid 发送到此接口</li>
     *   <li>后端使用 session_key 解密数据，获取手机号</li>
     *   <li>后端将手机号与用户关联并返回</li>
     * </ol>
     *
     * <h3>小程序端示例</h3>
     * <pre>{@code
     * <button open-type="getPhoneNumber" bindgetphonenumber="getPhoneNumber">
     *   授权手机号
     * </button>
     *
     * getPhoneNumber(e) {
     *   if (e.detail.errMsg === 'getPhoneNumber:ok') {
     *     wx.request({
     *       url: 'https://api.example.com/oauth2/wechat_mini/phone',
     *       method: 'POST',
     *       data: {
     *         encryptedData: e.detail.encryptedData,
     *         iv: e.detail.iv,
     *         openid: wx.getStorageSync('openid')
     *       },
     *       success: (res) => {
     *         console.log('手机号:', res.data.data.phoneNumber);
     *       }
     *     });
     *   }
     * }
     * }</pre>
     *
     * <h3>请求参数</h3>
     * <ul>
     *   <li><b>encryptedData</b> - 加密数据（必填），由微信返回</li>
     *   <li><b>iv</b> - 加密算法的初始向量（必填），由微信返回</li>
     *   <li><b>openid</b> - 用户的 openid（必填），用于获取 session_key</li>
     * </ul>
     *
     * <h3>返回结果</h3>
     * <p>成功时返回：</p>
     * <pre>{@code
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": {
     *     "phoneNumber": "13800138000",
     *     "purePhoneNumber": "13800138000",
     *     "countryCode": "86"
     *   }
     * }
     * }</pre>
     *
     * <h3>解密算法说明</h3>
     * <p>微信使用 AES-128-CBC 算法加密数据：</p>
     * <ul>
     *   <li>算法：AES-128-CBC</li>
     *   <li>密钥：session_key（Base64 解码后）</li>
     *   <li>初始向量：iv（Base64 解码后）</li>
     *   <li>数据块：128位</li>
     *   <li>补码方式：PKCS7</li>
     * </ul>
     *
     * <h3>错误处理</h3>
     * <ul>
     *   <li><b>平台未启用</b>：检查配置文件中 wechat-mini.enabled 是否为 true</li>
     *   <li><b>session_key 不存在</b>：用户需要先调用登录接口获取 session_key</li>
     *   <li><b>session_key 过期</b>：session_key 有效期为 3 天，过期需要重新登录</li>
     *   <li><b>解密失败</b>：检查 encryptedData、iv 是否正确，session_key 是否有效</li>
     * </ul>
     *
     * <h3>注意事项</h3>
     * <ul>
     *   <li>session_key 有效期为 3 天，过期后需要重新登录</li>
     *   <li>每次调用 wx.login() 都会刷新 session_key，旧的 session_key 会失效</li>
     *   <li>不要频繁调用 wx.login()，会导致 session_key 失效</li>
     *   <li>建议在登录后立即获取手机号，避免 session_key 过期</li>
     *   <li>手机号授权需要用户主动点击按钮，不能自动触发</li>
     *   <li>个人小程序无法使用手机号授权功能，需要企业认证</li>
     * </ul>
     *
     * <h3>参考文档</h3>
     * <ul>
     *   <li><a href="https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/getPhoneNumber.html">微信小程序手机号授权文档</a></li>
     *   <li><a href="https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserProfile.html">用户信息授权文档</a></li>
     * </ul>
     *
     * @param encryptedData 加密数据
     * @param iv 加密算法的初始向量
     * @param openid 用户的 openid
     * @param request HTTP 请求对象
     * @return 解密后的手机号信息
     */
    @PostMapping("/wechat_mini/phone")
    @Operation(summary = "微信小程序手机号授权", description = "解密微信小程序的手机号加密数据")
    public ReturnResult<Map<String, String>> wechatMiniPhoneAuth(
            @Parameter(description = "加密数据", required = true) @RequestParam String encryptedData,
            @Parameter(description = "加密算法的初始向量", required = true) @RequestParam String iv,
            @Parameter(description = "用户的 openid", required = true) @RequestParam String openid,
            HttpServletRequest request) {

        try {
            // 检查配置
            ThirdPartyLoginProperties.Base config = thirdPartyLoginProperties.getWechatMini();
            if (config == null || !config.isEnabled()) {
                log.warn("微信小程序登录未启用: enabled={}", config != null ? config.isEnabled() : "null");
                return ReturnResult.error("微信小程序登录未启用。请检查配置文件中 plugin.oauth.third-party.login.wechat-mini.enabled 是否为 true");
            }

            String appId = config.getClientId();

            if (StringUtils.isBlank(appId)) {
                log.error("微信小程序配置不完整: appId={}", appId);
                return ReturnResult.error("微信小程序配置不完整。请检查 client-id 是否已配置");
            }

            // 从缓存中获取 session_key（需要先登录）
            String sessionKey = authStateCache.get("wechat_mini_session_" + openid);

            if (StringUtils.isBlank(sessionKey)) {
                log.warn("session_key 不存在或已过期: openid={}", openid);
                return ReturnResult.error("session_key 不存在或已过期。请先调用登录接口获取 session_key，或重新登录");
            }

            log.info("微信小程序手机号授权: openid={}", openid);

            // 解密手机号数据
            Map<String, String> phoneData = decryptWechatData(encryptedData, sessionKey, iv, appId);

            if (phoneData == null || !phoneData.containsKey("phoneNumber")) {
                log.error("手机号解密失败: openid={}", openid);
                return ReturnResult.error("手机号解密失败。请检查 encryptedData、iv 是否正确，或 session_key 是否有效");
            }

            log.info("微信小程序手机号授权成功: openid={}, phoneNumber={}", openid, phoneData.get("phoneNumber"));

            // 可以在这里将手机号与用户关联
            // loginCheck.bindPhone(openid, phoneData.get("phoneNumber"));

            return ReturnResult.ok(phoneData);

        } catch (Exception e) {
            log.error("微信小程序手机号授权异常: error={}", e.getMessage(), e);
            return ReturnResult.error("手机号授权异常: " + e.getMessage() + "。请查看日志获取详细错误信息");
        }
    }

    /**
     * 微信小程序用户信息授权接口
     * 
     * <p>
     * 处理微信小程序的用户信息授权请求。小程序通过 button 组件的 open-type="getUserInfo" 
     * 或调用 wx.getUserProfile() 获取用户信息，微信会返回加密数据，需要后端使用 session_key 进行解密。
     * </p>
     * 
     * <h3>授权流程</h3>
     * <ol>
     *   <li>用户在小程序中点击授权按钮或调用 wx.getUserProfile()</li>
     *   <li>用户同意授权后，小程序获取到加密数据（encryptedData、iv）</li>
     *   <li>小程序将加密数据和 openid 发送到此接口</li>
     *   <li>后端使用 session_key 解密数据，获取用户信息</li>
     *   <li>后端将用户信息与用户关联并返回</li>
     * </ol>
     * 
     * <h3>小程序端示例</h3>
     * <pre>{@code
     * // 方式1：使用 wx.getUserProfile（推荐）
     * wx.getUserProfile({
     *   desc: '用于完善用户资料',
     *   success: (res) => {
     *     wx.request({
     *       url: 'https://api.example.com/oauth2/wechat_mini/userinfo',
     *       method: 'POST',
     *       data: {
     *         encryptedData: res.encryptedData,
     *         iv: res.iv,
     *         openid: wx.getStorageSync('openid')
     *       },
     *       success: (response) => {
     *         console.log('用户信息:', response.data.data);
     *       }
     *     });
     *   }
     * });
     * 
     * // 方式2：使用 button 组件（已废弃，不推荐）
     * <button open-type="getUserInfo" bindgetuserinfo="getUserInfo">
     *   授权用户信息
     * </button>
     * }</pre>
     * 
     * <h3>请求参数</h3>
     * <ul>
     *   <li><b>encryptedData</b> - 加密数据（必填），由微信返回</li>
     *   <li><b>iv</b> - 加密算法的初始向量（必填），由微信返回</li>
     *   <li><b>openid</b> - 用户的 openid（必填），用于获取 session_key</li>
     * </ul>
     * 
     * <h3>返回结果</h3>
     * <p>成功时返回：</p>
     * <pre>{@code
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": {
     *     "nickName": "张三",
     *     "avatarUrl": "https://wx.qlogo.cn/...",
     *     "gender": "1",
     *     "country": "中国",
     *     "province": "广东",
     *     "city": "深圳",
     *     "language": "zh_CN"
     *   }
     * }
     * }</pre>
     * 
     * <h3>解密算法说明</h3>
     * <p>微信使用 AES-128-CBC 算法加密数据：</p>
     * <ul>
     *   <li>算法：AES-128-CBC</li>
     *   <li>密钥：session_key（Base64 解码后）</li>
     *   <li>初始向量：iv（Base64 解码后）</li>
     *   <li>数据块：128位</li>
     *   <li>补码方式：PKCS7</li>
     * </ul>
     * 
     * <h3>错误处理</h3>
     * <ul>
     *   <li><b>平台未启用</b>：检查配置文件中 wechat-mini.enabled 是否为 true</li>
     *   <li><b>session_key 不存在</b>：用户需要先调用登录接口获取 session_key</li>
     *   <li><b>session_key 过期</b>：session_key 有效期为 3 天，过期需要重新登录</li>
     *   <li><b>解密失败</b>：检查 encryptedData、iv 是否正确，session_key 是否有效</li>
     * </ul>
     * 
     * <h3>注意事项</h3>
     * <ul>
     *   <li>session_key 有效期为 3 天，过期后需要重新登录</li>
     *   <li>每次调用 wx.login() 都会刷新 session_key，旧的 session_key 会失效</li>
     *   <li>不要频繁调用 wx.login()，会导致 session_key 失效</li>
     *   <li>建议在登录后立即获取用户信息，避免 session_key 过期</li>
     *   <li>用户信息授权需要用户主动触发，不能自动获取</li>
     *   <li>wx.getUserInfo 已废弃，建议使用 wx.getUserProfile</li>
     *   <li>获取的头像和昵称可能是默认值，需要引导用户填写真实信息</li>
     * </ul>
     * 
     * <h3>参考文档</h3>
     * <ul>
     *   <li><a href="https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserProfile.html">微信小程序用户信息授权文档</a></li>
     *   <li><a href="https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/signature.html">开放数据校验与解密</a></li>
     * </ul>
     *
     * @param encryptedData 加密数据
     * @param iv 加密算法的初始向量
     * @param openid 用户的 openid
     * @param request HTTP 请求对象
     * @return 解密后的用户信息
     */
    @PostMapping("/wechat_mini/userinfo")
    @Operation(summary = "微信小程序用户信息授权", description = "解密微信小程序的用户信息加密数据")
    public ReturnResult<Map<String, String>> wechatMiniUserInfoAuth(
            @Parameter(description = "加密数据", required = true) @RequestParam String encryptedData,
            @Parameter(description = "加密算法的初始向量", required = true) @RequestParam String iv,
            @Parameter(description = "用户的 openid", required = true) @RequestParam String openid,
            HttpServletRequest request) {

        try {
            // 检查配置
            ThirdPartyLoginProperties.Base config = thirdPartyLoginProperties.getWechatMini();
            if (config == null || !config.isEnabled()) {
                log.warn("微信小程序登录未启用: enabled={}", config != null ? config.isEnabled() : "null");
                return ReturnResult.error("微信小程序登录未启用。请检查配置文件中 plugin.oauth.third-party.login.wechat-mini.enabled 是否为 true");
            }

            String appId = config.getClientId();

            if (StringUtils.isBlank(appId)) {
                log.error("微信小程序配置不完整: appId={}", appId);
                return ReturnResult.error("微信小程序配置不完整。请检查 client-id 是否已配置");
            }

            // 从缓存中获取 session_key（需要先登录）
            String sessionKey = authStateCache.get("wechat_mini_session_" + openid);

            if (StringUtils.isBlank(sessionKey)) {
                log.warn("session_key 不存在或已过期: openid={}", openid);
                return ReturnResult.error("session_key 不存在或已过期。请先调用登录接口获取 session_key，或重新登录");
            }

            log.info("微信小程序用户信息授权: openid={}", openid);

            // 解密用户信息数据
            Map<String, String> userInfo = decryptWechatUserInfo(encryptedData, sessionKey, iv, appId);

            if (userInfo == null || !userInfo.containsKey("nickName")) {
                log.error("用户信息解密失败: openid={}", openid);
                return ReturnResult.error("用户信息解密失败。请检查 encryptedData、iv 是否正确，或 session_key 是否有效");
            }

            log.info("微信小程序用户信息授权成功: openid={}, nickName={}", openid, userInfo.get("nickName"));

            // 可以在这里将用户信息与用户关联
            // loginCheck.updateUserInfo(openid, userInfo);

            return ReturnResult.ok(userInfo);

        } catch (Exception e) {
            log.error("微信小程序用户信息授权异常: error={}", e.getMessage(), e);
            return ReturnResult.error("用户信息授权异常: " + e.getMessage() + "。请查看日志获取详细错误信息");
        }
    }

    /**
     * 解密微信用户信息数据
     * 
     * <p>
     * 使用 AES-128-CBC 算法解密微信返回的用户信息加密数据。
     * 微信使用 session_key 作为密钥，iv 作为初始向量。
     * </p>
     * 
     * <h3>解密步骤</h3>
     * <ol>
     *   <li>对 encryptedData 进行 Base64 解码</li>
     *   <li>对 session_key 进行 Base64 解码得到密钥</li>
     *   <li>对 iv 进行 Base64 解码得到初始向量</li>
     *   <li>使用 AES-128-CBC 算法解密</li>
     *   <li>去除 PKCS7 补码</li>
     *   <li>解析 JSON 数据</li>
     *   <li>验证 appId 是否一致</li>
     * </ol>
     * 
     * @param encryptedData 加密数据（Base64 编码）
     * @param sessionKey 会话密钥（Base64 编码）
     * @param iv 初始向量（Base64 编码）
     * @param appId 小程序 AppID，用于验证数据来源
     * @return 解密后的数据，包含 nickName、avatarUrl、gender、country、province、city、language 等字段
     * @throws Exception 解密失败时抛出异常
     */
    private Map<String, String> decryptWechatUserInfo(String encryptedData, String sessionKey, String iv, String appId) throws Exception {
        try {
            // Base64 解码
            byte[] encryptedBytes = java.util.Base64.getDecoder().decode(encryptedData);
            byte[] keyBytes = java.util.Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = java.util.Base64.getDecoder().decode(iv);

            // 创建 AES 解密器
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
            javax.crypto.spec.IvParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(ivBytes);

            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // 解密
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);

            log.debug("解密后的用户信息数据: {}", decryptedData);

            // 解析 JSON（简单实现）
            Map<String, String> result = new HashMap<>();

            // 验证 appId
            String dataAppId = extractJsonValue(decryptedData, "watermark", "appid");
            if (!appId.equals(dataAppId)) {
                log.error("appId 验证失败: expected={}, actual={}", appId, dataAppId);
                throw new IllegalArgumentException("appId 验证失败，数据可能被篡改");
            }

            // 提取用户信息
            String nickName = extractJsonValue(decryptedData, "nickName");
            String avatarUrl = extractJsonValue(decryptedData, "avatarUrl");
            String gender = extractJsonValue(decryptedData, "gender");
            String country = extractJsonValue(decryptedData, "country");
            String province = extractJsonValue(decryptedData, "province");
            String city = extractJsonValue(decryptedData, "city");
            String language = extractJsonValue(decryptedData, "language");

            result.put("nickName", nickName);
            result.put("avatarUrl", avatarUrl);
            result.put("gender", gender);
            result.put("country", country);
            result.put("province", province);
            result.put("city", city);
            result.put("language", language);

            return result;

        } catch (Exception e) {
            log.error("解密微信用户信息失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 解密微信加密数据
     *
     * <p>
     * 使用 AES-128-CBC 算法解密微信返回的加密数据。
     * 微信使用 session_key 作为密钥，iv 作为初始向量。
     * </p>
     *
     * <h3>解密步骤</h3>
     * <ol>
     *   <li>对 encryptedData 进行 Base64 解码</li>
     *   <li>对 session_key 进行 Base64 解码得到密钥</li>
     *   <li>对 iv 进行 Base64 解码得到初始向量</li>
     *   <li>使用 AES-128-CBC 算法解密</li>
     *   <li>去除 PKCS7 补码</li>
     *   <li>解析 JSON 数据</li>
     *   <li>验证 appId 是否一致</li>
     * </ol>
     *
     * @param encryptedData 加密数据（Base64 编码）
     * @param sessionKey 会话密钥（Base64 编码）
     * @param iv 初始向量（Base64 编码）
     * @param appId 小程序 AppID，用于验证数据来源
     * @return 解密后的数据，包含 phoneNumber、purePhoneNumber、countryCode 等字段
     * @throws Exception 解密失败时抛出异常
     */
    private Map<String, String> decryptWechatData(String encryptedData, String sessionKey, String iv, String appId) throws Exception {
        try {
            // Base64 解码
            byte[] encryptedBytes = java.util.Base64.getDecoder().decode(encryptedData);
            byte[] keyBytes = java.util.Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = java.util.Base64.getDecoder().decode(iv);

            // 创建 AES 解密器
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
            javax.crypto.spec.IvParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(ivBytes);

            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // 解密
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);

            log.debug("解密后的数据: {}", decryptedData);

            // 解析 JSON（简单实现）
            Map<String, String> result = new HashMap<>();

            // 验证 appId
            String dataAppId = extractJsonValue(decryptedData, "watermark", "appid");
            if (!appId.equals(dataAppId)) {
                log.error("appId 验证失败: expected={}, actual={}", appId, dataAppId);
                throw new IllegalArgumentException("appId 验证失败，数据可能被篡改");
            }

            // 提取手机号信息
            String phoneNumber = extractJsonValue(decryptedData, "phoneNumber");
            String purePhoneNumber = extractJsonValue(decryptedData, "purePhoneNumber");
            String countryCode = extractJsonValue(decryptedData, "countryCode");

            result.put("phoneNumber", phoneNumber);
            result.put("purePhoneNumber", purePhoneNumber);
            result.put("countryCode", countryCode);

            return result;

        } catch (Exception e) {
            log.error("解密微信数据失败: error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 从嵌套 JSON 字符串中提取指定字段的值
     *
     * @param json JSON字符串
     * @param keys 字段路径（支持嵌套）
     * @return 字段值，如果不存在返回空字符串
     */
    private String extractJsonValue(String json, String... keys) {
        String current = json;

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String searchKey = "\"" + key + "\"";
            int keyIndex = current.indexOf(searchKey);

            if (keyIndex == -1) {
                return "";
            }

            int colonIndex = current.indexOf(":", keyIndex);
            if (colonIndex == -1) {
                return "";
            }

            int valueStart = colonIndex + 1;
            // 跳过空格
            while (valueStart < current.length() && current.charAt(valueStart) == ' ') {
                valueStart++;
            }

            if (valueStart >= current.length()) {
                return "";
            }

            char firstChar = current.charAt(valueStart);

            // 如果是最后一个 key，提取值
            if (i == keys.length - 1) {
                int valueEnd;

                if (firstChar == '"') {
                    // 字符串值
                    valueStart++;
                    valueEnd = current.indexOf('"', valueStart);
                    if (valueEnd == -1) {
                        return "";
                    }
                    return current.substring(valueStart, valueEnd);
                } else if (firstChar == '{' || firstChar == '[') {
                    // 对象或数组值（不处理）
                    return "";
                } else {
                    // 数字或布尔值
                    valueEnd = valueStart;
                    while (valueEnd < current.length() &&
                           current.charAt(valueEnd) != ',' &&
                           current.charAt(valueEnd) != '}' &&
                           current.charAt(valueEnd) != ']') {
                        valueEnd++;
                    }
                    return current.substring(valueStart, valueEnd).trim();
                }
            } else {
                // 如果不是最后一个 key，需要进入嵌套对象
                if (firstChar == '{') {
                    // 找到对应的右括号
                    int braceCount = 1;
                    int endIndex = valueStart + 1;
                    while (endIndex < current.length() && braceCount > 0) {
                        if (current.charAt(endIndex) == '{') {
                            braceCount++;
                        } else if (current.charAt(endIndex) == '}') {
                            braceCount--;
                        }
                        endIndex++;
                    }
                    current = current.substring(valueStart, endIndex);
                } else {
                    return "";
                }
            }
        }

        return "";
    }
    
    /**
     * 微信小程序手机号授权接口
     * 
     * <p>
     * 处理微信小程序的手机号授权请求。小程序通过 button 组件的 open-type="getPhoneNumber" 
     * 获取用户手机号，微信会返回加密数据，需要后端使用 session_key 进行解密。
     * </p>
     * 
     * <h3>授权流程</h3>
     * <ol>
     *   <li>用户在小程序中点击授权按钮（open-type="getPhoneNumber"）</li>
     *   <li>用户同意授权后，小程序获取到加密数据（encryptedData、iv）</li>
     *   <li>小程序将加密数据和 openid 发送到此接口</li>
     *   <li>后端使用 session_key 解密数据，获取手机号</li>
     *   <li>后端将手机号与用户关联并返回</li>
     * </ol>
     * 
     * <h3>小程序端示例</h3>
     * <pre>{@code
     * <button open-type="getPhoneNumber" bindgetphonenumber="getPhoneNumber">
     *   授权手机号
     * </button>
     * 
     * getPhoneNumber(e) {
     *   if (e.detail.errMsg === 'getPhoneNumber:ok') {
     *     wx.request({
     *       url: 'https://api.example.com/oauth2/wechat_mini/phone',
     *       method: 'POST',
     *       data: {
     *         encryptedData: e.detail.encryptedData,
     *         iv: e.detail.iv,
     *         openid: wx.getStorageSync('openid')
     *       },
     *       success: (res) => {
     *         console.log('手机号:', res.data.data.phoneNumber);
     *       }
     *     });
     *   }
     * }
     * }</pre>
     * 
     * <h3>请求参数</h3>
     * <ul>
     *   <li><b>encryptedData</b> - 加密数据（必填），由微信返回</li>
     *   <li><b>iv</b> - 加密算法的初始向量（必填），由微信返回</li>
     *   <li><b>openid</b> - 用户的 openid（必填），用于获取 session_key</li>
     * </ul>
     * 
     * <h3>返回结果</h3>
     * <p>成功时返回：</p>
     * <pre>{@code
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": {
     *     "phoneNumber": "13800138000",
     *     "purePhoneNumber": "13800138000",
     *     "countryCode": "86"
     *   }
     * }
     * }</pre>
     * 
     * <h3>解密算法说明</h3>
     * <p>微信使用 AES-128-CBC 算法加密数据：</p>
     * <ul>
     *   <li>算法：AES-128-CBC</li>
     *   <li>密钥：session_key（Base64 解码后）</li>
     *   <li>初始向量：iv（Base64 解码后）</li>
     *   <li>数据块：128位</li>
     *   <li>补码方式：PKCS7</li>
     * </ul>
     * 
     * <h3>错误处理</h3>
     * <ul>
     *   <li><b>平台未启用</b>：检查配置文件中 wechat-mini.enabled 是否为 true</li>
     *   <li><b>session_key 不存在</b>：用户需要先调用登录接口获取 session_key</li>
     *   <li><b>session_key 过期</b>：session_key 有效期为 3 天，过期需要重新登录</li>
     *   <li><b>解密失败</b>：检查 encryptedData、iv 是否正确，session_key 是否有效</li>
     * </ul>
     * 
     * <h3>注意事项</h3>
     * <ul>
     *   <li>session_key 有效期为 3 天，过期后需要重新登录</li>
     *   <li>每次调用 wx.login() 都会刷新 session_key，旧的 session_key 会失效</li>
     *   <li>不要频繁调用 wx.login()，会导致 session_key 失效</li>
     *   <li>建议在登录后立即获取手机号，避免 session_key 过期</li>
     *   <li>手机号授权需要用户主动点击按钮，不能自动触发</li>
     *   <li>个人小程序无法使用手机号授权功能，需要企业认证</li>
     * </ul>
     * 
     * <h3>参考文档</h3>
     * <ul>
     *   <li><a href="https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/getPhoneNumber.html">微信小程序手机号授权文档</a></li>
     *   <li><a href="https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserProfile.html">用户信息授权文档</a></li>
     * </ul>
     *
     * @param encryptedData 加密数据
     * @param iv 加密算法的初始向量
     * @param openid 用户的 openid
     * @param request HTTP 请求对象
     * @return 解密后的手机号信息
     */
    /**
     * API 方式的授权回调（返回 JSON）
     * 
     * <p>
     * 以 API 方式处理第三方平台的授权回调，返回 JSON 格式的登录结果。
     * 适用于前后端分离的应用，前端可以通过 AJAX 方式调用此接口。
     * </p>
     * 
     * <h3>使用场景</h3>
     * <ul>
     *   <li>前后端分离应用</li>
     *   <li>移动端应用</li>
     *   <li>需要自定义登录成功后的处理逻辑</li>
     * </ul>
     * 
     * <h3>调用流程</h3>
     * <ol>
     *   <li>前端调用 GET /oauth2/authorize/{platform} 获取授权URL</li>
     *   <li>前端打开新窗口跳转到授权URL</li>
     *   <li>用户在第三方平台完成授权</li>
     *   <li>第三方平台重定向到回调地址，前端从URL中获取 code 和 state</li>
     *   <li>前端调用此接口，传入 code 和 state</li>
     *   <li>后端返回登录结果（包含 Token）</li>
     *   <li>前端保存 Token 并跳转到目标页面</li>
     * </ol>
     * 
     * <h3>请求参数</h3>
     * <ul>
     *   <li><b>platform</b> - 平台标识（路径参数）</li>
     *   <li><b>code</b> - 授权码（必填），由第三方平台返回</li>
     *   <li><b>state</b> - 状态码（必填），用于验证请求来源</li>
     * </ul>
     * 
     * <h3>返回结果</h3>
     * <p>成功时返回：</p>
     * <pre>{@code
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "userId": "123456",
     *     "username": "张三",
     *     "expireTime": 1640000000000
     *   }
     * }
     * }</pre>
     * 
     * <p>失败时返回：</p>
     * <pre>{@code
     * {
     *   "code": 500,
     *   "msg": "授权失败: redirect_uri_mismatch",
     *   "data": null
     * }
     * }</pre>
     * 
     * <h3>错误处理</h3>
     * <ul>
     *   <li><b>授权已过期</b>：state 对应的配置信息已过期，需要重新发起授权</li>
     *   <li><b>不支持的平台</b>：platform 参数不正确</li>
     *   <li><b>授权失败</b>：第三方平台返回错误，检查配置是否正确</li>
     *   <li><b>登录失败</b>：用户未绑定系统账号或其他业务错误</li>
     * </ul>
     * 
     * <h3>注意事项</h3>
     * <ul>
     *   <li>code 只能使用一次，使用后立即失效</li>
     *   <li>state 有效期为5分钟，过期后需要重新发起授权</li>
     *   <li>Token 需要前端自行保存（localStorage、sessionStorage等）</li>
     *   <li>跨域请求需要配置 CORS</li>
     * </ul>
     * 
     * <h3>请求示例</h3>
     * <pre>{@code
     * POST /oauth2/callback/gitee
     * Content-Type: application/x-www-form-urlencoded
     * 
     * code=abc123&state=xyz789
     * }</pre>
     *
     * @param platform 平台标识
     * @param code 授权码，由第三方平台返回
     * @param state 状态码，用于验证请求来源
     * @param request HTTP 请求对象
     * @return 登录结果，包含 Token 等信息
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
                log.warn("OAuth2 API回调配置已过期: platform={}, state={}", platform, state);
                return ReturnResult.error("登录失败：授权已过期。" +
                        "可能原因：在授权页面停留时间过长（超过5分钟）。" +
                        "解决方法：请关闭当前页面，重新发起登录");
            }

            AuthConfig authConfig = AuthConfig.builder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .redirectUri(redirectUri)
                    .build();

            AuthRequest authRequest = AuthRequestFactory.createAuthRequest(platform, authConfig, authStateCache);
            if (authRequest == null) {
                log.error("不支持的登录平台: platform={}", platform);
                return ReturnResult.error("登录失败：不支持的平台 '" + platform + "'。" +
                        "当前支持的平台包括：gitee、github、wechat_open、wechat_mp、wechat_mini、wechat_enterprise、qq、alipay、dingtalk、weibo、baidu、google、microsoft。" +
                        "请检查平台标识是否正确");
            }

            AuthCallback authCallback = AuthCallback.builder()
                    .code(code)
                    .state(state)
                    .build();

            AuthResponse<AuthUser> authResponse = authRequest.login(authCallback);
            if (!authResponse.ok()) {
                log.warn("OAuth2 API授权失败: platform={}, code={}, msg={}", platform, authResponse.getCode(), authResponse.getMsg());
                String errorMsg = "登录失败：" + authResponse.getMsg();
                
                // 根据错误码提供更详细的错误信息和解决方案
                if (authResponse.getMsg().contains("redirect_uri")) {
                    errorMsg = "登录失败：回调地址不匹配。" +
                            "解决方法：请联系系统管理员检查配置文件中的 redirect-uri 是否与第三方平台配置一致";
                } else if (authResponse.getMsg().contains("client") || authResponse.getMsg().contains("appid")) {
                    errorMsg = "登录失败：应用凭证错误。" +
                            "解决方法：请联系系统管理员检查配置文件中的 client-id 和 client-secret 是否正确";
                } else if (authResponse.getMsg().contains("denied")) {
                    errorMsg = "登录失败：您拒绝了授权。" +
                            "解决方法：如需登录，请重新发起登录并同意授权";
                } else if (authResponse.getMsg().contains("grant") || authResponse.getMsg().contains("code")) {
                    errorMsg = "登录失败：授权码已失效。" +
                            "解决方法：请关闭当前页面，重新发起登录";
                }
                
                return ReturnResult.error(errorMsg);
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
                String errorMsg = loginResultRet != null ? loginResultRet.getMsg() : "登录失败";
                if (errorMsg.contains("绑定")) {
                    errorMsg = "登录失败：账号未绑定。" +
                            "这是您首次使用该第三方账号登录，需要先绑定系统账号。" +
                            "解决方法：请联系系统管理员或使用其他方式登录后在个人中心绑定第三方账号";
                } else {
                    errorMsg = "登录失败：" + errorMsg + "。" +
                            "解决方法：请稍后重试，或联系系统管理员";
                }
                log.warn("OAuth2 API登录失败: platform={}, uuid={}, msg={}", 
                        platform, authUser.getUuid(), errorMsg);
                return ReturnResult.error(errorMsg);
            }

        } catch (Exception e) {
            log.error("OAuth2 API回调处理异常: platform={}, error={}", platform, e.getMessage(), e);
            return ReturnResult.error("登录异常：系统处理登录请求时发生错误。" +
                    "错误信息：" + e.getMessage() + "。" +
                    "解决方法：请稍后重试，如问题持续存在请联系系统管理员");
        }
    }

    /**
     * 获取平台配置
     * 
     * <p>
     * 根据平台标识获取对应的配置信息。
     * 支持平台标识的多种写法（如 wechat 和 wechat_open 都映射到微信开放平台）。
     * </p>
     * 
     * @param platform 平台标识（不区分大小写）
     * @return 平台配置，如果平台不存在则返回 null
     */
    private ThirdPartyLoginProperties.Base getPlatformConfig(String platform) {
        return switch (platform.toLowerCase()) {
            case "gitee" -> thirdPartyLoginProperties.getGitee();
            case "github" -> thirdPartyLoginProperties.getGithub();
            case "wechat", "wechat_open" -> thirdPartyLoginProperties.getWechatOpen();
            case "wechat_mp" -> thirdPartyLoginProperties.getWechatMp();
            case "wechat_mini" -> thirdPartyLoginProperties.getWechatMini();
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
     * 添加错误信息到 Cookie
     * 
     * <p>
     * 当登录失败时，将错误信息写入 Cookie，前端可以从 Cookie 中读取错误信息并显示给用户。
     * Cookie 名称为 "Access-Message"，有效期为 60 秒。
     * </p>
     * 
     * <h3>Cookie 设置</h3>
     * <ul>
     *   <li><b>名称</b>：Access-Message</li>
     *   <li><b>值</b>：错误信息（URL 编码两次）</li>
     *   <li><b>路径</b>：/</li>
     *   <li><b>域名</b>：从回调地址中提取</li>
     *   <li><b>有效期</b>：60 秒</li>
     * </ul>
     * 
     * <h3>前端读取示例</h3>
     * <pre>{@code
     * // JavaScript
     * function getErrorMessage() {
     *   const cookies = document.cookie.split(';');
     *   for (let cookie of cookies) {
     *     const [name, value] = cookie.trim().split('=');
     *     if (name === 'Access-Message') {
     *       return decodeURIComponent(decodeURIComponent(value));
     *     }
     *   }
     *   return null;
     * }
     * }</pre>
     * 
     * @param response HTTP 响应对象
     * @param message 错误信息
     * @param callback 回调地址，用于提取域名
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
