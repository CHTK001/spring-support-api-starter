package com.chua.payment.support.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.payment.support.entity.PaymentGlobalConfig;
import com.chua.payment.support.service.PaymentGlobalConfigService;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 支付全局配置控制器
 */
@Tag(name = "支付全局配置")
@RestController
@RequestMapping("/payment-global-config")
@RequiredArgsConstructor
public class PaymentGlobalConfigController {

    private final PaymentGlobalConfigService globalConfigService;

    @Operation(summary = "获取全局配置")
    @GetMapping
    @Permission(value = "payment:global:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<PaymentGlobalConfig> getGlobalConfig() {
        return ReturnResult.success(globalConfigService.getConfig());
    }

    @Operation(summary = "保存全局配置")
    @PutMapping
    @Permission(value = "payment:global:edit", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<PaymentGlobalConfig> updateGlobalConfig(@RequestBody PaymentGlobalConfig config) {
        PaymentGlobalConfig result = globalConfigService.saveOrUpdate(config);
        return ReturnResult.success(result);
    }
}
