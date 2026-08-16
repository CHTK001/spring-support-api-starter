package com.chua.starter.system.controller.callback;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.lang.algorithm.cipher.AesCipher;
import com.chua.common.support.lang.json.Json;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserThirdParty;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.properties.SystemSecurityProperties;
import com.chua.starter.system.service.SysLoginCallbackService;
import com.chua.starter.system.service.SysLoginService;
import com.chua.starter.system.service.SysUserThirdPartyService;
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
import me.zhyd.oauth.request.AuthGoogleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 第三方登录回调(Google)
 *
 * @author CH
 * @since 2026/08/03
 */
@Controller
@Slf4j
@Api(tags = "第三方登录回调(Google)")
@Tag(name = "第三方登录回调(Google)")
@RequestMapping("/v2/third/login/google")
@RequiredArgsConstructor
public class GoogleCallbackController {

    /**
     * 第三方登录类型标识
     */
    private static final String THIRD_PARTY_TYPE = "google";

    /**
     * 登录开关配置组编码
     */
    private static final String SSO_GROUP = "sso";

    /**
     * 登录开关配置名
     */
    private static final String SSO_NAME = "Google";

    /**
     * 登录回调服务（允许缺省）
     */
    @Autowired(required = false)
    private SysLoginCallbackService sysLoginCallbackService;

    /**
     * 三方用户绑定服务
     */
    final SysUserThirdPartyService sysUserThirdPartyService;

    /**
     * 登录服务
     */
    final SysLoginService sysLoginService;

    /**
     * 系统设置服务
     */
    final SysSettingService sysSettingService;

    /**
     * 认证客户端配置
     */
    final AuthClientProperties authProperties;

    /**
     * 系统安全配置
     */
    final SystemSecurityProperties securityProperties;

    @Ignore
    @GetMapping("binder")
    public RedirectView binder(AuthCallback authCallback, RedirectAttributes attributes, HttpServletRequest request, HttpServletResponse response) {
        String userId = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_userId");
        RedirectView view = new RedirectView();
        String systemCallback = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_system_callback");
        view.setUrl(StringUtils.defaultString(systemCallback, "/"));

        if (!isThirdPartyEnabled()) {
            attributes.addFlashAttribute("msg", "Google登录未开启，无法绑定");
            return view;
        }

        if (userId == null) {
            attributes.addFlashAttribute("msg", "请重新登录重试");
            return view;
        }

        long count = sysUserThirdPartyService.count(Wrappers.<SysUserThirdParty>lambdaQuery()
                .eq(SysUserThirdParty::getSysUserId, userId)
                .eq(SysUserThirdParty::getSysUserThirdPartyType, SysUserThirdParty.Type.GOOGLE.name()));
        if (count > 0) {
            attributes.addFlashAttribute("msg", "已绑定Google，无需重复绑定");
            return view;
        }

        AuthUser authUser = doAuthLogin(authCallback);
        if (authUser == null) {
            attributes.addFlashAttribute("msg", "Google授权失败");
            return view;
        }

        SysUser sysUser = sysLoginCallbackService.getById(userId);
        SysUserThirdParty thirdParty = new SysUserThirdParty();
        thirdParty.setSysUserId(Integer.valueOf(userId));
        thirdParty.setSysUserThirdPartyType(SysUserThirdParty.Type.GOOGLE.name());
        thirdParty.setSysUserThirdPartyName(StringUtils.defaultString(authUser.getUsername(), authUser.getNickname()));
        thirdParty.setSysUserThirdPartyUid(authUser.getUuid());
        thirdParty.setSysUserThirdPartyValue(authUser.getEmail());

        boolean updateUserInfo = false;
        if (StringUtils.isEmpty(sysUser.getSysUserAvatar()) && StringUtils.isNotEmpty(authUser.getAvatar())) {
            sysUser.setSysUserAvatar(authUser.getAvatar());
            updateUserInfo = true;
        }
        if (StringUtils.isEmpty(sysUser.getSysUserEmail()) && StringUtils.isNotEmpty(authUser.getEmail())) {
            sysUser.setSysUserEmail(authUser.getEmail());
            updateUserInfo = true;
        }
        if (updateUserInfo) {
            sysLoginCallbackService.updateById(sysUser);
        }

        sysUserThirdPartyService.save(thirdParty);
        return view;
    }

    @Ignore
    @GetMapping("callback")
    public RedirectView callback(AuthCallback authCallback, RedirectAttributes attributes, HttpServletRequest request, HttpServletResponse response) {
        String systemCallback = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_system_callback");
        RedirectView errorView = new RedirectView();
        errorView.setUrl(StringUtils.defaultString(systemCallback, "/"));

        if (!isThirdPartyEnabled()) {
            attributes.addFlashAttribute("msg", "Google登录未开启");
            return errorView;
        }

        String thirdType = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_thirdType");
        if ("0".equals(thirdType)) {
            return binder(authCallback, attributes, request, response);
        }

        AuthUser authUser = doAuthLogin(authCallback);
        if (authUser == null) {
            attributes.addFlashAttribute("msg", "Google授权失败");
            return errorView;
        }

        SysUserThirdParty userThirdParty = sysUserThirdPartyService.getOne(
                Wrappers.<SysUserThirdParty>lambdaQuery()
                        .eq(SysUserThirdParty::getSysUserThirdPartyUid, authUser.getUuid())
                        .eq(SysUserThirdParty::getSysUserThirdPartyType, SysUserThirdParty.Type.GOOGLE.name())
        );
        if (userThirdParty == null) {
            attributes.addFlashAttribute("msg", "请先绑定Google账号后再登录");
            return errorView;
        }

        Map<String, Object> ext = new HashMap<>();
        ext.put("loginType", THIRD_PARTY_TYPE);
        ext.put("thirdPartyType", THIRD_PARTY_TYPE);
        ext.put("address", RequestUtils.getIpAddress(request));
        LoginAuthResult accessToken = AuthClientExecute.getInstance()
                .getAccessToken(String.valueOf(userThirdParty.getSysUserId()), null, AuthType.THIRD_PARTY, ext);
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
        String encrypt = new AesCipher().encryptToString(securityProperties.getThirdPartyCallbackKey().getBytes(StandardCharsets.UTF_8), Json.toJson(loginResult));
        response.setHeader("x-response-time", nanoTime);
        view.addStaticAttribute("redirectParam", encrypt);
        view.setUrl(StringUtils.defaultString(systemCallback, "/"));
        return view;
    }

    private AuthUser doAuthLogin(AuthCallback authCallback) {
        String clientSecret = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_clientSecret");
        String clientId = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_clientId");
        String callback = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_callback");
        AuthConfig authConfig = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(callback)
                .build();
        AuthGoogleRequest authRequest = new AuthGoogleRequest(authConfig, AuthDefaultStateCache.INSTANCE);
        AuthResponse<?> login = authRequest.login(authCallback);
        if (!login.ok()) {
            log.warn("Google授权失败: {}", login.getMsg());
            return null;
        }
        return (AuthUser) login.getData();
    }

    private boolean isThirdPartyEnabled() {
        try {
            SysSetting setting = sysSettingService.getOne(
                    Wrappers.<SysSetting>lambdaQuery()
                            .eq(SysSetting::getSysSettingGroup, SSO_GROUP)
                            .eq(SysSetting::getSysSettingName, SSO_NAME)
                            .eq(SysSetting::getSysSettingStatus, 1)
            );
            return setting != null && "true".equalsIgnoreCase(setting.getSysSettingValue());
        } catch (Exception e) {
            log.error("Google登录开关检查失败: {}", e.getMessage());
            return false;
        }
    }
}
