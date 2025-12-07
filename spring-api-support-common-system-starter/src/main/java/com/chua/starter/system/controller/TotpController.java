package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.service.SimpleSysUserService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 2fa密钥管理
 * @author CH
 * @since 2024/12/10
 */
@RestController
@Slf4j
@Api(tags = "2fa密钥管理")
@Tag(name = "2fa密钥管理")
@RequestMapping("/v2/login/totp")
@RequiredArgsConstructor
public class TotpController {


    final SimpleSysUserService simpleSysUserService;
    /**
     * 绑定
     * @param sysUserId sysUserId
     * @return ReturnResult
     */
    @Operation(summary = "绑定2fa密钥")
    @PutMapping("/bind")
    public ReturnResult<Boolean> bind(@Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId) {
        return simpleSysUserService.bind(sysUserId);
    }


    /**
     * 解绑
     * @param sysUserId sysUserId
     * @return ReturnResult
     */
    @Operation(summary = "解绑2fa密钥")
    @PutMapping("/unbind")
    public ReturnResult<Boolean> unbind(@Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId) {
        return simpleSysUserService.unbind(sysUserId);
    }

    /**
     * 生成
     * @param sysUserId sysUserId
     * @return ReturnResult
     */
    @Operation(summary = "生成2fa密钥")
    @GetMapping("/generate")
    public ReturnResult<byte[]> generate(@Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId) {
        return simpleSysUserService.generate(sysUserId);
    }
    /**
     * 生成
     * @param sysUserId sysUserId
     * @return ReturnResult
     */
    @Operation(summary = "生成2fa密钥URI")
    @GetMapping("/generateUri")
    public ReturnResult<String> generateUri(@Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId) {
        return simpleSysUserService.generateUri(sysUserId);
    }


}
