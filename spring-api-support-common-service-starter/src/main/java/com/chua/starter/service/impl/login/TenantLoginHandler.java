package com.chua.starter.service.impl.login;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiDefault;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.service.SimpleSysUserService;
import com.chua.starter.service.impl.SysLoginServiceImpl;
import com.chua.starter.service.result.LoginResult;
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
@Spi("WEB-TENANT")
public class TenantLoginHandler extends DefaultLoginHandler {

    @AutoInject
    StringRedisTemplate stringRedisTemplate;

    @AutoInject
    SimpleSysUserService simpleSysUserService;

    @Override
    public Result<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint) {
//        if (StringUtils.isEmpty(loginData.getTenantId())) {
//            return Result.failed(REQUEST_PARAM_ERROR, "租户编码不能为空");
//        }
        Result<LoginResult> check = checkEnvironment(sysLoginService, loginData, request);
        if (null != check) {
            return check;
        }
        String address = getIpAddress(request);
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        long startTime = System.currentTimeMillis();
        LoginAuthResult accessToken = clientExecute.getAccessToken(loginData.getUsername(),
                loginData.getPassword(),
                AuthType.TENANT,
                ImmutableBuilder.<String, Object>builderOfMap()
                        .put("address", address)
                        .put("fingerprint", fingerprint)
                        .put("tenantId", loginData.getTenantId())
                        .build()
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
                .refreshToken(accessToken.getRefreshToken())
                .build();
        CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);
        return Result.success(loginResult);
    }

}
