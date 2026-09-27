package com.chua.starter.system.controller;

import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.pojo.verify.EmailConnectivityVO;
import com.chua.starter.system.pojo.EmailCodeRequest;
import com.chua.starter.system.pojo.EmailLoginRequest;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.service.EmailCodeService;
import com.chua.starter.system.service.UserVerifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;

/**
 * 邮箱服务器配置接口
 *
 * <p>系统配置里的 SMTP/IMAP 填完只是字符串，握手成功才算生效，因此这里提供一次真实探测，
 * 让管理员在配置页当场看到"发不出码"的原因，而不是等用户点验证才报错。</p>
 *
 * @author CH
 * @since 4.0.0.50
 */
@Tag(name = "邮箱服务器接口")
@RestController
@RequestMapping("/v2/email")
@RequiredArgsConstructor
public class SysEmailController {

    /**
     * 账号资料验证服务
    */
    private final UserVerifyService userVerifyService;

    /**
     * 邮箱验证码服务
     */
    private final EmailCodeService emailCodeService;

    /**
     * 探测服务器邮箱配置（SMTP 与 IMAP 各握手一次）
     *
     * @return 两条通道的探测结果
     */
    @Operation(summary = "检测邮箱服务器连通性")
    @PostMapping("/check")
    @SysLog
    @Permission(role = {ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<EmailConnectivityVO> check() {
        return userVerifyService.checkEmailConnectivity();
    }

    /**
     * 发送邮箱验证码
     *
     * @param request 发送请求
     * @return 发送结果
     */
    @Ignore
    @PostMapping("/send")
    @Operation(summary = "发送邮箱验证码")
    public ReturnResult<Boolean> sendCode(@Valid @RequestBody EmailCodeRequest request) {
        return emailCodeService.sendCode(request);
    }

    /**
     * 邮箱验证码登录
     *
     * @param request     登录请求
     * @param httpRequest HTTP 请求
     * @param response    HTTP 响应
     * @return 登录结果
     */
    @Ignore
    @PostMapping("/login")
    @Operation(summary = "邮箱验证码登录")
    public ReturnResult<LoginResult> login(@Valid @RequestBody EmailLoginRequest request,
                                           HttpServletRequest httpRequest,
                                           HttpServletResponse response) {
        return emailCodeService.login(request, httpRequest, response);
    }
}
