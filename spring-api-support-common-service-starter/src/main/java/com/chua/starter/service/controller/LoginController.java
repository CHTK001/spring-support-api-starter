package com.chua.starter.service.controller;

import com.chua.common.support.constant.Action;
import com.chua.common.support.task.debounce.Debounce;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.common.support.annotations.UserLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.annotation.TokenForIgnore;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.service.SysLoginService;
import com.chua.starter.service.pojo.user.RefreshToken;
import com.chua.starter.service.result.LoginResult;
import com.chua.starter.service.result.UserInfoVO;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;
import static com.chua.starter.common.support.logger.SysLoggerPointcutAdvisor.X_REQ_FINGERPRINT;

/**
 * 登录
 *
 * @author CH
 * @since 2024/9/12
 */
@RestController("service:loginController")
@Slf4j
@Api(tags = "登录接口")
@Tag(name = "登录接口")
@RequestMapping("/v2/user")
@ConditionalOnMissingBean(name = "loginController")
@RequiredArgsConstructor
public class LoginController {

    final SysLoginService sysLoginService;

    /**
     * 登录
     *
     * @param loginData     登录信息
     * @param request       请求
     * @param response      响应
     * @param bindingResult 参数处理
     * @return 结果
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    @SysLog
    @UserLogger(module = "LOGIN",
            name = "用户登录",
            action = Action.LOGIN,
            loginType = "WEB",
            content = "#{#now} #{#args[0].username} 登录系统, 登录返回[状态:#{#result.code}(#{#result.msg})]")
    @Debounce(key = "#loginData.username #loginData.loginType")
    public Result<LoginResult> login(@Valid @RequestBody LoginData loginData,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestHeader(name = X_REQ_FINGERPRINT, required = false) String fingerprint,
                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.failed(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysLoginService.login(loginData, request, response, fingerprint);
    }

    @Operation(summary = "刷新token")
    @PostMapping("/refresh-token")
    @SysLog
    @TokenForIgnore
    public Result<LoginResult> refreshToken(@Valid @RequestBody RefreshToken refreshToken,
                                            HttpServletRequest request,
                                            HttpServletResponse response,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.failed(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysLoginService.refresh(refreshToken, request, response);
    }

    @Operation(summary = "注销")
    @UserLogger(module = "LOGIN", name = "用户登出", action = Action.LOGOUT, content = "#{#now} #{#args[0].username} 退出系统, 登录返回[状态:#{#result.code}(#{#result.msg})]")
    @DeleteMapping("/logout")
    @SysLog
    public Result logout(@UserValue("token") String uid, String loginType, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(uid)) {
            return Result.success("注销成功");
        }
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        LoginAuthResult accessToken = clientExecute.logout(uid, loginType, LogoutType.LOGOUT);
        if (null == accessToken) {
            return Result.failed("退出失败请稍后重试");
        }

        if (accessToken.getCode() != 200) {
            return Result.failed(accessToken.getMessage());
        }

        CookieUtil.remove(request, response, "x-oauth-cookie");
        return Result.success("注销成功");
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 获取当前登录用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<UserInfoVO> getUserLoginInfo() {
        return Result.success(sysLoginService.getUserLoginInfo());
    }


}
