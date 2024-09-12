package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.mail.Email;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.service.EmailService;
import com.chua.starter.system.pojo.email.EmailRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮件接口
 *
 * @author CH
 * @since 2024/9/4
 */
@RestController
@Slf4j
@Api(tags = "邮件接口")
@Tag(name = "邮件接口")
@RequestMapping("/v2/email")
@RequiredArgsConstructor
public class EmailController {

    final EmailService emailService;

    /**
     * 校验码
     *
     * @param request 请求
     */
    @ApiOperation("发送邮件")
    @Operation(summary = "发送邮件")
    @GetMapping
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> sendEmail(EmailRequest request) {
        return emailService.send(Email.builder()
                .title(request.getTitle())
                .to(request.getTo())
                .content(request.getContent())
                .build());
    }
}
