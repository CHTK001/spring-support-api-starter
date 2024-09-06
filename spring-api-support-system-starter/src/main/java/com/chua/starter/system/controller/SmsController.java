package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.system.entity.SysSecret;
import com.chua.starter.system.pojo.sms.SmsRequest;
import com.chua.starter.system.service.SysSmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 短信接口
 *
 * @author CH
 * @since 2024/9/4
 */
@RestController
@Slf4j
@Api(tags = "短信接口")
@Tag(name = "短信接口")
@RequestMapping("/v2/sms")
@RequiredArgsConstructor
public class SmsController {

    final SysSmsService sysSmsService;

    /**
     * 校验码
     *
     * @param request 请求
     */
    @ApiOperation("发送邮件")
    @Operation(summary = "发送邮件")
    @GetMapping
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<List<PushResult>> send(SmsRequest request) {
        return sysSmsService.send(request);
    }


    /**
     * 同步模板
     *
     * @param sysSecret 密钥
     */
    @Operation(summary = "同步模板")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    @PostMapping("/syncTemplate")
    @ApiOperation("同步模板")
    public ReturnResult<Boolean> syncTemplate(@RequestBody SysSecret sysSecret) {
        return sysSmsService.syncTemplate(sysSecret);
    }
}
