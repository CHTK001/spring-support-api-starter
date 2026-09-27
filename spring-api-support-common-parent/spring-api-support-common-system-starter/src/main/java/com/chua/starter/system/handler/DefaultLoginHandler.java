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
import com.chua.starter.oauth.client.support.ratelimit.RateLimitService;
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
 * @since 2024/12/2
 * @author CH
 */
@SpiDefault
@Slf4j
@Spi("WEB")
public class DefaultLoginHandler implements LoginHandler {

    @Override
    /**
     * Login
    */
    public ReturnResult<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint) {
        long startTime = System.currentTimeMillis();
        // 锁定功能暂时下线：不再做登录前 isAccountLocked 拦截；仅保留失败计数
        RateLimitService rateLimitService = RateLimitService.getInstance();

        // 1) 图形验证码（若启用）
        ReturnResult<LoginResult> check = checkEnvironment(sysLoginService, loginData, request);
        if (null != check) {
            sysLoginService.publishLoginLog(loginData, request, fingerprint, null, check.getMsg(), System.currentTimeMillis() - startTime);
            return check;
        }

        // 2) 先统一校验账号 / 口令（static 本地认证），不签发令牌
        com.chua.starter.entity.system.SysUser user;
        try {
            user = sysLoginService.verifyLocalUser(loginData);
        } catch (SysLoginServiceImpl.LoginRejectException e) {
            sysLoginService.publishLoginLog(loginData, request, fingerprint, null, e.getMessage(), System.currentTimeMillis() - startTime);
            return ReturnResult.error(REQUEST_PARAM_ERROR.getCode(), e.getMessage());
        }
        if (null == user) {
            // 账号或口令不正确，记录失败次数（锁定功能暂时下线，不再镜像到 sys_user_lock）
            rateLimitService.recordLoginFailure(loginData.getUsername());
            sysLoginService.publishLoginLog(loginData, request, fingerprint, null, "账号或者密码不正确", System.currentTimeMillis() - startTime);
            return ReturnResult.error(USERNAME_OR_PASSWORD_ERROR.getCode(), "账号或者密码不正确");
        }

        // 3) 仅当该用户已绑定 TOTP 时才校验动态码（未绑定不拦截，与全局开关无关）
        ReturnResult<LoginResult> totp = sysLoginService.verifyBoundTotp(user, loginData);
        if (null != totp) {
            // 口令已正确，缺 / 错动态码不计为密码失败
            sysLoginService.publishLoginLog(loginData, request, fingerprint, null, totp.getMsg(), System.currentTimeMillis() - startTime);
            return totp;
        }

        // 4) 校验全部通过，签发令牌
        LoginAuthResult accessToken = sysLoginService.issueLocalToken(user);

        log.info("登录耗时:{}ms", System.currentTimeMillis() - startTime);
        if (null == accessToken || accessToken.getCode() != SystemConstant.HTTP_STATUS_OK) {
            rateLimitService.recordLoginFailure(loginData.getUsername());
            String reason = null != accessToken ? accessToken.getMessage() : "签发令牌失败";
            sysLoginService.publishLoginLog(loginData, request, fingerprint, accessToken, reason, System.currentTimeMillis() - startTime);
            return ReturnResult.error(REQUEST_PARAM_ERROR.getCode(), reason);
        }

        // 登录成功，清除失败计数与锁定状态
        rateLimitService.resetAccount(loginData.getUsername());
        sysLoginService.publishLoginLog(loginData, request, fingerprint, accessToken, null, System.currentTimeMillis() - startTime);

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
