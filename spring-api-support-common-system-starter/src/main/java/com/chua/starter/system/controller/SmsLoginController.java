package com.chua.starter.system.controller;

import com.chua.common.support.core.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.pojo.SmsCodeRequest;
import com.chua.starter.system.pojo.SmsLoginRequest;
import com.chua.starter.system.service.SmsCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信验证码登录控制器
 * <p>提供短信验证码发送和登录接口</p>
 *
 * @author CH
 * @since 2024/12/11
 */
@Slf4j
@RestController
@Api(tags = "短信验证码登录")
@Tag(name = "短信验证码登录")
@RequestMapping("/v2/sms")
@RequiredArgsConstructor
public class SmsLoginController {

    private final SmsCodeService smsCodeService;

    /**
     * 发送短信验证码
     *
     * @param request 发送请求
     * @return 发送结果
     */
    @Ignore
    @PostMapping("/send")
    @ApiOperation("发送短信验证码")
    @Operation(summary = "发送短信验证码")
    public ReturnResult<Boolean> sendCode(@Valid @RequestBody SmsCodeRequest request) {
        log.debug("发送短信验证码请求: phone={}, bizType={}", request.getPhone(), request.getBizType());
        return smsCodeService.sendCode(request);
    }

    /**
     * 短信验证码登录
     *
     * @param request     登录请求
     * @param httpRequest HTTP请求
     * @param response    HTTP响应
     * @return 登录结果
     */
    @Ignore
    @PostMapping("/login")
    @ApiOperation("短信验证码登录")
    @Operation(summary = "短信验证码登录")
    public ReturnResult<LoginResult> login(@Valid @RequestBody SmsLoginRequest request,
                                           HttpServletRequest httpRequest,
                                           HttpServletResponse response) {
        log.debug("短信验证码登录请求: phone={}", request.getPhone());
        return smsCodeService.login(request, httpRequest, response);
    }
}
