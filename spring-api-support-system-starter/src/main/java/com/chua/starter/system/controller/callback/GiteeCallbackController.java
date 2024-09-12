package com.chua.starter.system.controller.callback;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.annotations.Ignore;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.json.Json;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.service.SysUserThirdPartyService;
import com.chua.starter.system.entity.SysUser;
import com.chua.starter.system.entity.SysUserThirdParty;
import com.chua.starter.system.result.LoginResult;
import com.chua.starter.system.service.SysUserService;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

/**
 * gitee接口
 *
 * @author CH
 * @since 2024/9/4
 */
@Controller
@Slf4j
@Api(tags = "三方登录回调(gitee)")
@Tag(name = "三方登录回调(gitee)")
@RequestMapping("/v2/third/login/gitee")
@RequiredArgsConstructor
public class GiteeCallbackController {


    final SysUserService sysUserService;
    final SysUserThirdPartyService sysUserThirdPartyService;
    final AuthClientProperties authProperties;

    /**
     * 校验码
     *
     * @param request 请求
     */
    @Ignore
    @GetMapping("binder")
    public RedirectView binder(AuthCallback authCallback, RedirectAttributes attributes, HttpServletRequest request, HttpServletResponse response) {
        String userId = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_userId");
        RedirectView view = new RedirectView();
        String systemCallback = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_system_callback");
        view.setUrl(StringUtils.defaultString(systemCallback, "/"));
        if(null == userId) {
            attributes.addFlashAttribute("msg", "请重试/重新登录重试");
            return view;
        }
        long count = sysUserThirdPartyService.count(Wrappers.<SysUserThirdParty>lambdaQuery().eq(SysUserThirdParty::getSysUserId, userId));
        if(count > 0) {
            attributes.addFlashAttribute("msg", "已绑定, 无须重复绑定");
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
        SysUser userServiceById = sysUserService.getById(userId);
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
            sysUserService.updateById(userServiceById);
        }
        sysUserThirdPartyService.save(sysUserThirdParty);
        return view;
    }
    /**
     * 校验码
     *
     * @param request 请求
     */
    @Ignore
    @GetMapping("callback")
    public RedirectView callback(AuthCallback authCallback, RedirectAttributes attributes, HttpServletRequest request, HttpServletResponse response) {
        String thirdType = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_thirdType");
        if("0".equals(thirdType)) {
            return binder(authCallback, attributes, request, response);
        }
        String info = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_info");
        String clientSecret = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_clientSecret");
        String clientId = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_clientId");
        String userId = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_userId");
        String callback = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_callback");
        String systemCallback = AuthDefaultStateCache.INSTANCE.get(authCallback.getState() + "_system_callback");
        AuthConfig authConfig = AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(callback)
                .build();
        AuthGiteeRequest authGiteeRequest = new AuthGiteeRequest(authConfig, AuthDefaultStateCache.INSTANCE);
        AuthResponse login = authGiteeRequest.login(authCallback);
        AuthUser authUser = (AuthUser) login.getData();
        SysUserThirdParty userThirdParty = sysUserThirdPartyService.getOne(Wrappers.<SysUserThirdParty>lambdaQuery().eq(SysUserThirdParty::getSysUserThirdPartyUid, authUser.getUuid()));
        if(null == userThirdParty) {
            attributes.addFlashAttribute("msg", "第三方登录失败, 请重试");
            return new RedirectView();
        }

        Map<String, Object> ext = ImmutableBuilder.<String, Object>builderOfMap()
                .put("loginType", "gitee")
                .put("address", RequestUtils.getIpAddress()).build();
        LoginAuthResult accessToken = AuthClientExecute.getInstance().getAccessToken(String.valueOf(userThirdParty.getSysUserId()), null, AuthType.THIRD_PARTY, ext);
        LoginResult loginResult = LoginResult.builder()
                .userInfo(sysUserService.getUserLoginInfo(accessToken.getToken()))
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
        return view;
    }
}
