package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.service.SysLoginService;
import com.chua.starter.system.pojo.user.ThirdPartyUnbindRequest;
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
 * 三方登录
 * @author CH
 * @since 2024/9/12
 */
@RestController
@Slf4j
@Api(tags = "三方登录接口")
@Tag(name = "三方登录接口")
@RequestMapping("/v2/user")
@RequiredArgsConstructor
public class ThirdLoginController {

    final SysLoginService sysLoginService;
    /**
     * 三方登录
     *
     * @param loginData     登录信息
     * @param request       请求
     * @param response      响应
     * @param bindingResult 参数处理
     * @return 结果
     */
    @Operation(summary = "三方登录码")
    @PostMapping("/third/login/code")
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
     * 三方绑定
     *
     * @param loginData     登录信息
     * @param request       请求
     * @param response      响应
     * @param bindingResult 参数处理
     * @return 结果
     */
    @Operation(summary = "三方绑定码")
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
     * 三方绑定
     *
     * @param userId       userId
     * @return 结果
     */
    @Operation(summary = "三方绑定情况")
    @PostMapping("/third/bind/info")
    public ReturnResult<List<String>> thirdBindInfo(@UserValue("userId") String userId) {
        return sysLoginService.thirdBindInfo(userId);
    }
    /**
     * 三方绑定
     *
     * @param userId       userId
     * @return 结果
     */
    @Operation(summary = "三方绑定解绑")
    @PostMapping("/third/unbind")
    public ReturnResult<Boolean> thirdBindInfo(@UserValue("userId") String userId, @RequestBody ThirdPartyUnbindRequest thirdPartyUnbindRequest) {
        return sysLoginService.thirdUnbind(userId, thirdPartyUnbindRequest);
    }
}
