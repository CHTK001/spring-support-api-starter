package com.chua.starter.system.controller.callback;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.annotations.Ignore;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.constant.CommonConstant;
import com.chua.common.support.json.Json;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.StringUtils;
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
import com.chua.starter.service.SysUserThirdPartyService;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.service.SysLoginCallbackService;
import com.chua.starter.system.service.SysLoginService;
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
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

/**
 * 微信网页登录回调接口
 * <p>处理微信开放平台OAuth2.0授权回调</p>
 *
 * @author CH
 * @since 2024/12/11
 */
@Controller
@Slf4j
@Api(tags = "第三方登录回调(微信)")
@Tag(name = "第三方登录回调(微信)")
@RequestMapping("/v2/third/login/wechat")
@RequiredArgsConstructor
public class WechatCallbackController {

    private static final String THIRD_PARTY_TYPE = "wechat";
    private static final String SSO_GROUP = "sso";
    private static final String SSO_NAME = "Wechat";

    @Autowired(required = false)
    private SysLoginCallbackService sysLoginCallbackService;
    final SysUserThirdPartyService sysUserThirdPartyService;
    final SysLoginService sysLoginService;
    final SysSettingService sysSettingService;
    final AuthClientProperties authProperties;

    /**
     * 微信账号绑定回调
     * <p>用户授权后回调此接口，将微信账号与系统用户绑定</p>
     *
     * @param authCallback 微信回调参数
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
        view.setUrl(StringUtils.defaultString(systemCallback, "/"));

        // 检查微信登录是否开启
        if (!isThirdPartyEnabled()) {
            log.warn("微信登录未开启，拒绝绑定请求");
            attributes.addFlashAttribute("msg", "微信登录未开启，无法绑定");
            return view;
        }

        if (null == userId) {
            attributes.addFlashAttribute("msg", "请重新登录重试");
            return view;
        }

        long count = sysUserThirdPartyService.count(Wrappers.<SysUserThirdParty>lambdaQuery()
                .eq(SysUserThirdParty::getSysUserId, userId)
                .eq(SysUserThirdParty::getSysUserThirdPartyType, THIRD_PARTY_TYPE));
        if (count > 0) {
            attributes.addFlashAttribute("msg", "已绑定微信，无需重复绑定");
            return view;
        }

        String clientSecret = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_clientSecret");
        String clientId = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_clientId");
        String callback = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_callback");

        AuthConfig authConfig = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(callback)
                .build();

        AuthWeChatOpenRequest authRequest = new AuthWeChatOpenRequest(authConfig, AuthDefaultStateCache.INSTANCE);
        AuthResponse login = authRequest.login(authCallback);
        if (!login.ok()) {
            log.error("微信授权失败: {}", login.getMsg());
            attributes.addFlashAttribute("msg", "微信授权失败: " + login.getMsg());
            return view;
        }

        AuthUser authUser = (AuthUser) login.getData();
        SysUser userServiceById = sysLoginCallbackService.getById(userId);

        // 保存第三方绑定信息
        SysUserThirdParty sysUserThirdParty = new SysUserThirdParty();
        sysUserThirdParty.setSysUserId(Integer.valueOf(userId));
        sysUserThirdParty.setSysUserThirdPartyType(THIRD_PARTY_TYPE);
        sysUserThirdParty.setSysUserThirdPartyName(authUser.getNickname());
        sysUserThirdParty.setSysUserThirdPartyUid(authUser.getUuid());

        // 更新用户头像（如果为空）
        boolean updateUserInfo = false;
        if (StringUtils.isEmpty(userServiceById.getSysUserAvatar()) && StringUtils.isNotEmpty(authUser.getAvatar())) {
            userServiceById.setSysUserAvatar(authUser.getAvatar());
            updateUserInfo = true;
        }

        if (updateUserInfo) {
            sysLoginCallbackService.updateById(userServiceById);
        }
        sysUserThirdPartyService.save(sysUserThirdParty);

        log.info("微信账号绑定成功: userId={}, openId={}", userId, authUser.getUuid());
        return view;
    }

    /**
     * 微信登录回调
     * <p>用户授权后回调此接口进行登录，需要先绑定账号</p>
     *
     * @param authCallback 微信回调参数
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
        errorView.setUrl(StringUtils.defaultString(systemCallback, "/"));

        // 检查微信登录是否开启
        if (!isThirdPartyEnabled()) {
            log.warn("微信登录未开启，拒绝登录请求");
            attributes.addFlashAttribute("msg", "微信登录未开启");
            return errorView;
        }

        String thirdType = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_thirdType");
        if ("0".equals(thirdType)) {
            return binder(authCallback, attributes, request, response);
        }

        String clientSecret = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_clientSecret");
        String clientId = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_clientId");
        String callback = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_callback");

        AuthConfig authConfig = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(callback)
                .build();

        AuthWeChatOpenRequest authRequest = new AuthWeChatOpenRequest(authConfig, AuthDefaultStateCache.INSTANCE);
        AuthResponse login = authRequest.login(authCallback);
        if (!login.ok()) {
            log.error("微信授权失败: {}", login.getMsg());
            attributes.addFlashAttribute("msg", "微信授权失败: " + login.getMsg());
            return errorView;
        }

        AuthUser authUser = (AuthUser) login.getData();
        SysUserThirdParty userThirdParty = sysUserThirdPartyService.getOne(
                Wrappers.<SysUserThirdParty>lambdaQuery()
                        .eq(SysUserThirdParty::getSysUserThirdPartyUid, authUser.getUuid())
                        .eq(SysUserThirdParty::getSysUserThirdPartyType, THIRD_PARTY_TYPE));

        if (null == userThirdParty) {
            log.warn("微信登录失败: 未绑定系统用户, openId={}", authUser.getUuid());
            attributes.addFlashAttribute("msg", "请先绑定微信账号后再登录");
            return errorView;
        }

        Map<String, Object> ext = ImmutableBuilder.<String, Object>builderOfMap()
                .put("loginType", THIRD_PARTY_TYPE)
                .put("thirdPartyType", THIRD_PARTY_TYPE)
                .put("address", RequestUtils.getIpAddress()).build();

        LoginAuthResult accessToken = AuthClientExecute.getInstance()
                .getAccessToken(String.valueOf(userThirdParty.getSysUserId()), null, AuthType.THIRD_PARTY, ext);

        LoginResult loginResult = LoginResult.builder()
                .userInfo(sysLoginService.getUserLoginInfo(accessToken.getToken()))
                .tokenType("Bearer")
                .accessToken(accessToken.getToken())
                .build();

        CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);

        RedirectView view = new RedirectView();
        String nanoTime = StringUtils.padAfter(System.nanoTime() + "", 16, "0");
        String encrypt = DigestUtils.aesEncrypt(Json.toJson(loginResult), "1234567890Oil#@1");
        response.setHeader("x-response-time", nanoTime);
        view.addStaticAttribute("redirectParam", encrypt);
        view.setUrl(StringUtils.defaultString(systemCallback, "/"));

        log.info("微信登录成功: userId={}", userThirdParty.getSysUserId());
        return view;
    }

    /**
     * 检查第三方登录是否开启
     * <p>从sys_setting表中查询sso组下Wechat配置是否为true</p>
     *
     * @return true-开启，false-未开启
     */
    private boolean isThirdPartyEnabled() {
        try {
            SysSetting setting = sysSettingService.getOne(
                    Wrappers.<SysSetting>lambdaQuery()
                            .eq(SysSetting::getSysSettingGroup, SSO_GROUP)
                            .eq(SysSetting::getSysSettingName, SSO_NAME)
                            .eq(SysSetting::getSysSettingStatus, 1)
            );
            return setting != null && CommonConstant.TRUE.equalsIgnoreCase(setting.getSysSettingValue());
        } catch (Exception e) {
            log.error("微信登录开关检查失败: {}", e.getMessage());
            return false;
        }
    }
}
