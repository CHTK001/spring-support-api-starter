// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.controller.callback;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.system.properties.SystemSecurityProperties;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserThirdParty;
import com.chua.starter.system.service.SysLoginService;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.service.SysLoginCallbackService;
import com.chua.starter.system.service.SysUserThirdPartyService;
import com.chua.starter.system.support.ThirdPartyCallbackSupport;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthGiteeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

/**
 * gitee接口
 *
 * @since 2024/9/4
 * @author CH
 */
@Controller
@Slf4j
@Api(tags = "第三方登录回调(Gitee)")
@Tag(name = "第三方登录回调(Gitee)")
@RequestMapping("/v2/third/login/gitee")
@RequiredArgsConstructor
public class GiteeCallbackController {


    /**
     * Third_party_类型
    */
    private static final String THIRD_PARTY_TYPE = "gitee";
    /**
     * 单点登录_群体
    */
    private static final String SSO_GROUP = "sso";

    @Autowired(required = false)
    /**
     * syslogincallback服务
    */
    private SysLoginCallbackService sysLoginCallbackService;
    final SysUserThirdPartyService sysUserThirdPartyService; // sys用户thirdparty服务
    final SysLoginService sysLoginService; // syslogin服务
    final SysSettingService sysSettingService; // syssetting服务
    final AuthClientProperties authProperties; // 认证属性
    final SystemSecurityProperties securityProperties; // 安全性属性

    /**
     * Gitee账号绑定回调
     * <p>用户授权后回调此接口，将Gitee账号与系统用户绑定</p>
     *
     * @param authCallback Gitee回调参数
     * @param attributes   重定向属性
     * @param request      请求
     * @param response     响应
     * @return 重定向视图
     */
    @Ignore
    @GetMapping("binder")
    public RedirectView binder(AuthCallback authCallback, RedirectAttributes attributes, HttpServletRequest request, HttpServletResponse response) {
        String userId = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_userId");
        RedirectView view = new RedirectView();
        String systemCallback = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_system_callback");
        // 读取点兜底：缓存里的回调地址过白名单（开放重定向读取点防线）
        view.setUrl(ThirdPartyCallbackSupport.safeSystemCallback(systemCallback, securityProperties));
        
        // 检查 Gitee 登录是否开启
        if (!isThirdPartyEnabled()) {
            log.warn("Gitee登录未开启，拒绝绑定请求");
            ThirdPartyCallbackSupport.markFailure(attributes, view, "Gitee登录未开启，无法绑定");
            return view;
        }
        
        if(null == userId) {
            ThirdPartyCallbackSupport.markFailure(attributes, view, "请重新登录重试");
            return view;
        }
        long count = sysUserThirdPartyService.count(Wrappers.<SysUserThirdParty>lambdaQuery().eq(SysUserThirdParty::getSysUserId, userId));
        if(count > 0) {
            ThirdPartyCallbackSupport.markFailure(attributes, view, "已绑定，无需重复绑定");
            return view;
        }
        String info = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_info");
        String clientSecret = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_clientSecret");
        String thirdType = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_thirdType");
        String clientId = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_clientId");
        String callback = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_callback");
        AuthConfig authConfig = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(callback)
                .build();
        AuthGiteeRequest authGiteeRequest = new AuthGiteeRequest(authConfig, AuthDefaultStateCache.INSTANCE);
        AuthResponse login = authGiteeRequest.login(authCallback);
        AuthUser authUser = (AuthUser) login.getData();
        if (null == authUser) {
            log.warn("Gitee绑定授权失败: {}", login.getMsg());
            ThirdPartyCallbackSupport.markFailure(attributes, view, "Gitee授权失败，请重试");
            return view;
        }
        SysUser userServiceById = sysLoginCallbackService.getById(userId);
        SysUserThirdParty sysUserThirdParty = new SysUserThirdParty();
        sysUserThirdParty.setSysUserId(Integer.valueOf(userId));
        sysUserThirdParty.setSysUserThirdPartyType("gitee");
        sysUserThirdParty.setSysUserThirdPartyName(authUser.getUsername());
        sysUserThirdParty.setSysUserThirdPartyUid(authUser.getUuid());
        boolean updateUserInfo = false;
        if(StringUtils.isEmpty(userServiceById.getSysUserAvatar())) {
            userServiceById.setSysUserAvatar(authUser.getAvatar());
            updateUserInfo = true;
        }

        if(StringUtils.isEmpty(userServiceById.getSysUserEmail())) {
            userServiceById.setSysUserEmail(authUser.getEmail());
            updateUserInfo = true;
        }

        if(updateUserInfo) {
            sysLoginCallbackService.updateById(userServiceById);
        }
        sysUserThirdPartyService.save(sysUserThirdParty);
        return view;
    }
    /**
     * Gitee登录回调
     * <p>用户授权后回调此接口进行登录，需要先绑定账号</p>
     *
     * @param authCallback Gitee回调参数
     * @param attributes   重定向属性
     * @param request      请求
     * @param response     响应
     * @return 重定向视图
     */
    @Ignore
    @GetMapping("callback")
    public RedirectView callback(AuthCallback authCallback, RedirectAttributes attributes, HttpServletRequest request, HttpServletResponse response) {
        String systemCallback = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_system_callback");
        RedirectView errorView = new RedirectView();
        // 读取点兜底：缓存里的回调地址过白名单（开放重定向读取点防线）
        errorView.setUrl(ThirdPartyCallbackSupport.safeSystemCallback(systemCallback, securityProperties));
        
        // 检查 Gitee 登录是否开启
        if (!isThirdPartyEnabled()) {
            log.warn("Gitee登录未开启，拒绝登录请求");
            ThirdPartyCallbackSupport.markFailure(attributes, errorView, "Gitee登录未开启");
            return errorView;
        }
        
        String thirdType = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_thirdType");
        if("0".equals(thirdType)) {
            return binder(authCallback, attributes, request, response);
        }
        String info = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_info");
        String clientSecret = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_clientSecret");
        String clientId = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_clientId");
        String userId = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_userId");
        String callback = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_callback");
        AuthConfig authConfig = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(callback)
                .build();
        AuthGiteeRequest authGiteeRequest = new AuthGiteeRequest(authConfig, AuthDefaultStateCache.INSTANCE);
        AuthResponse login = authGiteeRequest.login(authCallback);
        AuthUser authUser = (AuthUser) login.getData();
        if (null == authUser) {
            log.warn("Gitee登录授权失败: {}", login.getMsg());
            ThirdPartyCallbackSupport.markFailure(attributes, errorView, "Gitee授权失败，请重试");
            return errorView;
        }
        SysUserThirdParty userThirdParty = sysUserThirdPartyService.getOne(Wrappers.<SysUserThirdParty>lambdaQuery().eq(SysUserThirdParty::getSysUserThirdPartyUid, authUser.getUuid()));
        if(null == userThirdParty) {
            ThirdPartyCallbackSupport.markFailure(attributes, errorView, "第三方登录失败，请重试");
            return errorView;
        }

        Map<String, Object> ext = new HashMap<>();
        ext.put("loginType", THIRD_PARTY_TYPE);
        ext.put("thirdPartyType", THIRD_PARTY_TYPE);
        ext.put("address", RequestUtils.getIpAddress(request));
        LoginAuthResult accessToken = AuthClientExecute.getInstance().getAccessToken(String.valueOf(userThirdParty.getSysUserId()), null, AuthType.THIRD_PARTY, ext);
        if (null == accessToken || StringUtils.isBlank(accessToken.getToken())) {
            log.warn("Gitee三方登录换取令牌失败");
            ThirdPartyCallbackSupport.markFailure(attributes, errorView, "第三方登录失败，请重试");
            return errorView;
        }
        LoginResult loginResult = LoginResult.builder()
                .userInfo(accessToken.getUserResume() != null
                        ? sysLoginService.buildUserInfo(accessToken.getUserResume())
                        : sysLoginService.getUserLoginInfo(accessToken.getToken()))
                .tokenType("Bearer")
                .accessToken(accessToken.getToken())
                .build();
        CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);
        RedirectView view = new RedirectView();
        String nanoTime = StringUtils.padAfter(System.nanoTime() + "", 16, '0');
        // 加密登录结果 + 登记一次性 nonce（供 /v2/user/third/login/exchange 兑换）
        String encrypt = ThirdPartyCallbackSupport.encryptRedirectParam(loginResult, securityProperties);
        response.setHeader("x-response-time", nanoTime);
        view.addStaticAttribute("redirectParam", encrypt);
        view.setUrl(ThirdPartyCallbackSupport.safeSystemCallback(systemCallback, securityProperties));
        return view;
    }

    /**
     * 检查第三方登录是否开启
     * <p>从sys_setting表中查询sso组下Gitee配置是否为true</p>
     *
     * @return true-开启，false-未开启
     */
    private boolean isThirdPartyEnabled() {
        try {
            SysSetting setting = sysSettingService.getOne(
                    Wrappers.<SysSetting>lambdaQuery()
                            .eq(SysSetting::getSysSettingGroup, SSO_GROUP)
                            .eq(SysSetting::getSysSettingName, "Gitee")
                            .eq(SysSetting::getSysSettingStatus, 1)
            );
            return setting != null && "true".equalsIgnoreCase(setting.getSysSettingValue());
        } catch (Exception e) {
            log.error("Gitee登录开关检查失败: {}", e.getMessage());
            return false;
        }
    }
}