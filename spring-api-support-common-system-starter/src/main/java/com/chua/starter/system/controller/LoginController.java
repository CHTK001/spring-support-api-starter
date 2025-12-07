package com.chua.starter.system.controller;

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
import com.chua.starter.service.pojo.user.RefreshToken;
import com.chua.starter.service.result.LoginResult;
import com.chua.starter.service.result.UserInfoVO;
import com.chua.starter.system.service.SysLoginService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;
import static com.chua.starter.common.support.logger.SysLoggerPointcutAdvisor.X_REQ_FINGERPRINT;

/**
 * 鐧诲綍
 * @author CH
 * @since 2024/9/12
 */
@RestController
@Slf4j
@Api(tags = "鐧诲綍鎺ュ彛")
@Tag(name = "鐧诲綍鎺ュ彛")
@RequestMapping("/v2/login")
@RequiredArgsConstructor
public class LoginController {

    final SysLoginService sysLoginService;
    /**
     * 鐧诲綍
     *
     * @param loginData     鐧诲綍淇℃伅
     * @param request       璇锋眰
     * @param response      鍝嶅簲
     * @param bindingResult 鍙傛暟澶勭悊
     * @return 缁撴灉
     */
    @Operation(summary = "鐧诲綍")
    @PostMapping("/login")
    @SysLog
    @UserLogger(module = "LOGIN",
            name = "鐢ㄦ埛鐧诲綍",
            action = Action.LOGIN,
            loginType = "WEB",
            content = "#{#now} #{#args[0].username} 鐧诲綍绯荤粺, 鐧诲綍杩斿洖[鐘舵€?#{#result.code}(#{#result.msg})]")
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
    @Operation(summary = "鍒锋柊token")
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

    @Operation(summary = "娉ㄩ攢")
    @UserLogger(module = "LOGIN", name = "鐢ㄦ埛鐧诲嚭", action = Action.LOGOUT, content = "#{#now} #{#args[0].username} 閫€鍑虹郴缁? 鐧诲綍杩斿洖[鐘舵€?#{#result.code}(#{#result.msg})]")
    @DeleteMapping("/logout")
    @SysLog
    public Result logout(@UserValue("token") String uid,String loginType, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(uid)) {
            return Result.success("娉ㄩ攢鎴愬姛");
        }
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        LoginAuthResult accessToken = clientExecute.logout(uid, loginType, LogoutType.LOGOUT);
        if (null == accessToken) {
            return Result.failed("閫€鍑哄け璐ヨ绋嶅悗閲嶈瘯");
        }

        if (accessToken.getCode() != 200) {
            return Result.failed(accessToken.getMessage());
        }

        CookieUtil.remove(request, response, "x-oauth-cookie");
        return Result.success("娉ㄩ攢鎴愬姛");
    }

    /**
     * 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛淇℃伅
     *
     * @return 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛淇℃伅
     */
    @Operation(summary = "鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛淇℃伅")
    @GetMapping("/me")
    public Result<UserInfoVO> getUserLoginInfo() {
        return Result.success(sysLoginService.getUserLoginInfo());
    }




}
