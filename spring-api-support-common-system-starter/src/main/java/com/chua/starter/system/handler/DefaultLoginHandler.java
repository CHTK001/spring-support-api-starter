package com.chua.starter.system.handler;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.spi.annotations.SpiDefault;
import com.chua.starter.common.support.collection.ImmutableBuilder;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.service.impl.SysLoginServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;
import static com.chua.common.support.lang.code.ReturnCode.USERNAME_OR_PASSWORD_ERROR;
import static com.chua.starter.common.support.utils.RequestUtils.getIpAddress;

/**
 * 默认登录处理器
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/2
 */
@SpiDefault
@Slf4j
@Spi("WEB")
public class DefaultLoginHandler implements LoginHandler {

    @Override
    public ReturnResult<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint) {
        ReturnResult<LoginResult> check = checkEnvironment(sysLoginService, loginData, request);
        if (null != check) {
            return check;
        }
        long startTime = System.currentTimeMillis();

        // 使用本地认证，绕过远程OAuth服务器
        LoginAuthResult accessToken = sysLoginService.authenticateLocal(loginData);

        log.info("登录耗时:{}ms", System.currentTimeMillis() - startTime);
        if (null == accessToken) {
            return ReturnResult.error(USERNAME_OR_PASSWORD_ERROR.getCode(), "账号或者密码不正确");
        }

        if (accessToken.getCode() != SystemConstant.HTTP_STATUS_OK) {
            return ReturnResult.error(REQUEST_PARAM_ERROR.getCode(), accessToken.getMessage());
        }

        LoginResult loginResult = LoginResult.builder()
                .userInfo(accessToken.getUserResume() != null
                        ? sysLoginService.buildUserInfo(accessToken.getUserResume())
                        : sysLoginService.getUserLoginInfo(accessToken.getToken()))
                .tokenType("Bearer")
                .accessToken(accessToken.getToken())
                .refreshToken(accessToken.getRefreshToken())
                .expires(null != accessToken.getUserResume() ? accessToken.getUserResume().getExpireTime() : null)
                .build();
        CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);
        return ReturnResult.ok(loginResult);
    }

    /**
     * 检查环境（验证码等）
     *
     * @param sysLoginService 登录服务
     * @param loginData       登录数据
     * @param request         请求
     * @return 检查结果
     */
    protected ReturnResult<LoginResult> checkEnvironment(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request) {
        ReturnResult<LoginResult> result = sysLoginService.checkEnvironment(loginData, request);
        if (null == result && StringUtils.isEmpty(loginData.getVerifyCodeKey())) {
            log.debug("未提供验证码，跳过验证码检查");
        }
        return result;
    }
}
