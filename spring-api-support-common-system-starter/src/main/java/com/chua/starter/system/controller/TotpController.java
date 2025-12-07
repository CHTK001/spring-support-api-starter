package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.service.SimpleSysUserService;
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
 * 2fa瀵嗛挜绠＄悊
 * @author CH
 * @since 2024/12/10
 */
@RestController
@Slf4j
@Api(tags = "2fa瀵嗛挜绠＄悊")
@Tag(name = "2fa瀵嗛挜绠＄悊")
@RequestMapping("/v2/login/totp")
@RequiredArgsConstructor
public class TotpController {


    final SimpleSysUserService simpleSysUserService;
    /**
     * 绑定
     * @param sysUserId sysUserId
     * @return ReturnResult
     */
    @Operation(summary = "绑定2fa瀵嗛挜")
    @PutMapping("/bind")
    public ReturnResult<Boolean> bind(@Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId) {
        return simpleSysUserService.bind(sysUserId);
    }


    /**
     * 解绑
     * @param sysUserId sysUserId
     * @return ReturnResult
     */
    @Operation(summary = "解绑2fa瀵嗛挜")
    @PutMapping("/unbind")
    public ReturnResult<Boolean> unbind(@Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId) {
        return simpleSysUserService.unbind(sysUserId);
    }

    /**
     * 鐢熸垚
     * @param sysUserId sysUserId
     * @return ReturnResult
     */
    @Operation(summary = "鐢熸垚2fa瀵嗛挜")
    @GetMapping("/generate")
    public ReturnResult<byte[]> generate(@Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId) {
        return simpleSysUserService.generate(sysUserId);
    }
    /**
     * 鐢熸垚
     * @param sysUserId sysUserId
     * @return ReturnResult
     */
    @Operation(summary = "鐢熸垚2fa瀵嗛挜")
    @GetMapping("/generateUri")
    public ReturnResult<String> generateUri(@Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId) {
        return simpleSysUserService.generateUri(sysUserId);
    }


}
