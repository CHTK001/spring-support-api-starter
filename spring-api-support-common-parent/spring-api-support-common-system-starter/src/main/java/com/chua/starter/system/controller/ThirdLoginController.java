package com.chua.starter.system.controller;
import com.chua.starter.common.support.annotations.SysLog;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.annotation.TokenForIgnore;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.system.service.SysLoginService;
import com.chua.starter.system.pojo.ThirdPartyUnbindRequest;
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
 * 第三方登录
 * @since 2024/9/12
 * @author CH
 */
@RestController
@Slf4j
@Api(tags = "第三方登录接口")
@Tag(name = "第三方登录接口")
@RequestMapping("/v2/user")
@RequiredArgsConstructor
public class ThirdLoginController {

    /**
     * 用户登录服务，处理第三方授权登录逻辑
    */
    final SysLoginService sysLoginService;
    /**
     * 第三方授权登录，将授权码换取用户身份并写入 令牌。
     *
     * @param loginData     第三方登录凭证（包含 提供者 与 编码）
     * @param request       HTTP请求
     * @param response      HTTP响应
     * @param bindingResult 参数校验结果
     * @return 登录结果，包含 access令牌 等信息
     */
    @Operation(summary = "第三方登录码")
    @PostMapping("/third/login/code")
    @TokenForIgnore
    public ReturnResult<String> thirdLoginCode(@Valid @RequestBody ThirdLoginData loginData,
                                               HttpServletRequest request,
                                               HttpServletResponse response,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.error(REQUEST_PARAM_ERROR.getCode(), bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysLoginService.thirdLoginCode(loginData, request, response);
    }
    /**
     * 将第三方账号绑定到当前登录用户。
     *
     * @param loginData     第三方授权凭证
     * @param request       HTTP请求
     * @param response      HTTP响应
     * @param bindingResult 参数校验结果
     * @return 绑定操作结果
     */
    @Operation(summary = "第三方绑定码")
    @PostMapping("/third/bind/code")
    @SysLog(module = "绑定")
    public ReturnResult<String> thirdBindCode(@Valid @RequestBody ThirdLoginData loginData,
                                              HttpServletRequest request,
                                              HttpServletResponse response,
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.error(REQUEST_PARAM_ERROR.getCode(), bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysLoginService.thirdBindCode(loginData, request, response);
    }
    /**
     * 查询当前用户的第三方账号绑定情况。
     *
     * @param userId 当前登录用户 标识
     * @return 已绑定的第三方平台列表
     * @param @UserValue("userId" @UserValue("userId"
     */
    @Operation(summary = "第三方绑定情况")
    @PostMapping("/third/bind/info")
    public ReturnResult<List<String>> thirdBindInfo(@UserValue("userId") String userId) {
        return sysLoginService.thirdBindInfo(userId);
    }
    /**
     * 解绑指定第三方账号。
     *
     * @param userId                  当前登录用户 标识
     * @param thirdPartyUnbindRequest 解绑请求，包含目标 提供者 标识
     * @return 是否解绑成功
     * @param @UserValue("userId" @UserValue("userId"
     */
    @Operation(summary = "第三方绑定解绑")
    @PostMapping("/third/unbind")
    @SysLog(module = "解绑")
    public ReturnResult<Boolean> thirdUnbindForLogin(@UserValue("userId") String userId, @RequestBody ThirdPartyUnbindRequest thirdPartyUnbindRequest) {
        return sysLoginService.thirdUnbind(userId, thirdPartyUnbindRequest);
    }
}
