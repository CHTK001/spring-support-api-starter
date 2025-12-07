package com.chua.starter.system.service.impl.login;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiDefault;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.service.SimpleSysUserService;
import com.chua.starter.service.setting.GuestDefaultSetting;
import com.chua.starter.system.result.LoginResult;
import com.chua.starter.system.service.impl.SysLoginServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;
import static com.chua.common.support.lang.code.ReturnCode.USERNAME_OR_PASSWORD_ERROR;
import static com.chua.starter.common.support.utils.RequestUtils.getIpAddress;

/**
 * 默认登录处理器
 *
 * @author CH
 * @since 2024/12/2
 */
@SpiDefault
@Slf4j
@Spi("WEB")
public class DefaultLoginHandler implements LoginHandler {

    @AutoInject
    StringRedisTemplate stringRedisTemplate;

    @AutoInject
    SimpleSysUserService simpleSysUserService;

    private static final GlobalSettingFactory GLOBAL_SETTING_FACTORY = GlobalSettingFactory.getInstance();

    @Override
    public ReturnResult<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint) {
        ReturnResult<LoginResult> check = checkEnvironment(sysLoginService, loginData, request);
        if(null != check) {
            return check;
        }
        String address = getIpAddress(request);
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        long startTime = System.currentTimeMillis();
        LoginAuthResult accessToken = clientExecute.getAccessToken(loginData.getUsername(), loginData.getPassword(), AuthType.valueOf(loginData.getLoginType()),
                ImmutableBuilder.<String, Object>builderOfMap()
                        .put("address", address)
                        .put("fingerprint", fingerprint)
                        .put("ua", request.getHeader("User-Agent"))
                        .build()
        );

        log.info("登录耗时:{}ms", System.currentTimeMillis() - startTime);
        if (null == accessToken) {
            return ReturnResult.error(USERNAME_OR_PASSWORD_ERROR, "账号或者密码不正确");
        }

        if (accessToken.getCode() != 200) {
            return ReturnResult.error(REQUEST_PARAM_ERROR, accessToken.getMessage());
        }

        LoginResult loginResult = LoginResult.builder()
                .userInfo(sysLoginService.getUserLoginInfo(accessToken.getToken()))
                .tokenType("Bearer")
                .accessToken(accessToken.getToken())
                .refreshToken(accessToken.getRefreshToken())
                .build();
        CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);
        return ReturnResult.ok(loginResult);
    }

    /**
     * 检查环境
     *
     * @param sysLoginService sysLoginService
     * @param loginData       loginData
     * @param request         request
     * @return ReturnResult
     */
    protected ReturnResult<LoginResult> checkEnvironment(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request) {
        String code = loginData.getVerifyCodeKey();
        GuestDefaultSetting verifyCodeSetting = GLOBAL_SETTING_FACTORY.get(GuestDefaultSetting.class);
        if (verifyCodeSetting.isCheckCodeOpen()) {
            if (StringUtils.isEmpty(code)) {
                return ReturnResult.error(REQUEST_PARAM_ERROR, "验证码不能为空");
            }
            return sysLoginService.checkVerifyCode(loginData, request);
        }

        if(verifyCodeSetting.isCheckTotpCodeOpen()) {
            if (StringUtils.isEmpty(code)) {
                return ReturnResult.error(REQUEST_PARAM_ERROR, "验证码不能为空");
            }
            ReturnResult<Boolean> verify = simpleSysUserService.verify(code, loginData.getUsername());
            if (!verify.isOk()) {
                return ReturnResult.error("验证码不正确");
            }

        }
        return null;
    }
}
