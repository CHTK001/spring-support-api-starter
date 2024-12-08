package com.chua.starter.service.impl.login;

import com.chua.common.support.annotations.SpiDefault;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.otp.TotpGenerator;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.service.impl.SysLoginServiceImpl;
import com.chua.starter.service.setting.GuestDefaultSetting;
import com.chua.starter.system.result.LoginResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;
import static com.chua.common.support.lang.code.ReturnCode.USERNAME_OR_PASSWORD_ERROR;
import static com.chua.starter.common.support.utils.RequestUtils.getIpAddress;
import static com.chua.starter.service.impl.SysLoginServiceImpl.GLOBAL_SETTING_FACTORY;

/**
 * 默认登录处理器
 *
 * @author CH
 * @since 2024/12/2
 */
@SpiDefault
@Slf4j
public class DefaultLoginHandler implements LoginHandler {
    @Override
    public Result<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        Result<LoginResult> check = checkEnvironment(sysLoginService, loginData, request);
        if(null != check) {
            return check;
        }
        String address = getIpAddress(request);
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        long startTime = System.currentTimeMillis();
        LoginAuthResult accessToken = clientExecute.getAccessToken(loginData.getUsername(), loginData.getPassword(), AuthType.valueOf(loginData.getLoginType()),
                ImmutableBuilder.<String, Object>builderOfMap().put("address", address).build()
        );

        log.info("登录耗时:{}ms", System.currentTimeMillis() - startTime);
        if (null == accessToken) {
            return Result.failed(USERNAME_OR_PASSWORD_ERROR, "账号或者密码不正确");
        }

        if (accessToken.getCode() != 200) {
            return Result.failed(REQUEST_PARAM_ERROR, accessToken.getMessage());
        }

        LoginResult loginResult = LoginResult.builder()
                .userInfo(sysLoginService.getUserLoginInfo(accessToken.getToken()))
                .tokenType("Bearer")
                .accessToken(accessToken.getToken())
                .build();
        CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);
        return Result.success(loginResult);
    }

    /**
     * 检查环境
     *
     * @param sysLoginService sysLoginService
     * @param loginData       loginData
     * @param request         request
     * @return Result
     */
    private Result<LoginResult> checkEnvironment(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request) {
        String code = loginData.getVerifyCodeKey();
        GuestDefaultSetting verifyCodeSetting = GLOBAL_SETTING_FACTORY.get(GuestDefaultSetting.class);
        if (verifyCodeSetting.isCheckCodeOpen()) {
            if (StringUtils.isEmpty(code)) {
                return Result.failed(REQUEST_PARAM_ERROR, "验证码不能为空");
            }
            Result<LoginResult> rs = sysLoginService.checkVerifyCode(loginData, request);
            if (null != rs) {
                return rs;
            }

            return null;
        }

        if(verifyCodeSetting.isCheckTotpCodeOpen()) {
            if (StringUtils.isEmpty(code)) {
                return Result.failed(REQUEST_PARAM_ERROR, "验证码不能为空");
            }
            String secretString = loginData.getUsername() + loginData.getLoginType();
            byte[] bytes = secretString.getBytes(StandardCharsets.UTF_8);

            TotpGenerator otpGenerator = ServiceProvider.of(TotpGenerator.class).getNewExtension("default", bytes, ZoneOffset.UTC);
            boolean verify = otpGenerator.verify(code);
            if (!verify) {
                return Result.failed("验证码不正确");
            }

        }
        return null;
    }
}
