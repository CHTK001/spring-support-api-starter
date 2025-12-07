package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.annotation.TokenForIgnore;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.service.pojo.user.ThirdPartyUnbindRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;

/**
 * 涓夋柟鐧诲綍
 * @author CH
 * @since 2024/9/12
 */
@RestController
@Slf4j
@Api(tags = "涓夋柟鐧诲綍鎺ュ彛")
@Tag(name = "涓夋柟鐧诲綍鎺ュ彛")
@RequestMapping("/v2/login")
@RequiredArgsConstructor
public class ThirdLoginController {

    final SysLoginService sysLoginService;
    /**
     * 涓夋柟鐧诲綍
     *
     * @param loginData     鐧诲綍淇℃伅
     * @param request       璇锋眰
     * @param response      鍝嶅簲
     * @param bindingResult 鍙傛暟澶勭悊
     * @return 缁撴灉
     */
    @Operation(summary = "涓夋柟鐧诲綍鐮")
    @PostMapping("/third/login/code")
    @TokenForIgnore
    public ReturnResult<String> thirdLoginCode(@Valid @RequestBody ThirdLoginData loginData,
                                               HttpServletRequest request,
                                               HttpServletResponse response,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.failed(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysLoginService.thirdLoginCode(loginData, request, response);
    }
    /**
     * 涓夋柟缁戝畾
     *
     * @param loginData     鐧诲綍淇℃伅
     * @param request       璇锋眰
     * @param response      鍝嶅簲
     * @param bindingResult 鍙傛暟澶勭悊
     * @return 缁撴灉
     */
    @Operation(summary = "涓夋柟缁戝畾鐮")
    @PostMapping("/third/bind/code")
    public ReturnResult<String> thirdBindCode(@Valid @RequestBody ThirdLoginData loginData,
                                              HttpServletRequest request,
                                              HttpServletResponse response,
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.failed(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysLoginService.thirdBindCode(loginData, request, response);
    }
    /**
     * 涓夋柟缁戝畾
     *
     * @param userId       userId
     * @return 缁撴灉
     */
    @Operation(summary = "涓夋柟缁戝畾鎯呭喌")
    @PostMapping("/third/bind/info")
    public ReturnResult<List<String>> thirdBindInfo(@UserValue("userId") String userId) {
        return sysLoginService.thirdBindInfo(userId);
    }
    /**
     * 涓夋柟缁戝畾
     *
     * @param userId       userId
     * @return 缁撴灉
     */
    @Operation(summary = "涓夋柟缁戝畾瑙ｇ粦")
    @PostMapping("/third/unbind")
    public ReturnResult<Boolean> thirdBindInfo(@UserValue("userId") String userId, @RequestBody ThirdPartyUnbindRequest thirdPartyUnbindRequest) {
        return sysLoginService.thirdUnbind(userId, thirdPartyUnbindRequest);
    }
}
