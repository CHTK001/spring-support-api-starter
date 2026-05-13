package com.chua.payment.support.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.payment.support.dto.ChannelConfigDTO;
import com.chua.payment.support.service.MerchantChannelService;
import com.chua.payment.support.vo.MerchantChannelVO;
import com.chua.payment.support.vo.PaymentMethodGuideVO;
import com.chua.payment.support.vo.ProviderSpiOptionVO;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户渠道管理控制器
 */
@Tag(name = "渠道管理", description = "支付渠道配置相关接口")
@RestController
@RequestMapping("/channel")
@RequiredArgsConstructor
public class MerchantChannelController {

    private final MerchantChannelService channelService;

    @Operation(summary = "创建渠道配置")
    @PostMapping
    @Permission(value = "payment:channel:add", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<MerchantChannelVO> createChannel(@RequestBody ChannelConfigDTO dto) {
        return ReturnResult.success(channelService.createChannel(dto));
    }

    @Operation(summary = "更新渠道配置")
    @PutMapping("/{id}")
    @Permission(value = "payment:channel:edit", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<MerchantChannelVO> updateChannel(@PathVariable Long id, @RequestBody ChannelConfigDTO dto) {
        return ReturnResult.success(channelService.updateChannel(id, dto));
    }

    @Operation(summary = "查询渠道配置")
    @GetMapping("/{id}")
    @Permission(value = "payment:channel:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<MerchantChannelVO> getChannel(@PathVariable Long id) {
        return ReturnResult.success(channelService.getChannel(id));
    }

    @Operation(summary = "查询商户的所有渠道")
    @GetMapping("/merchant/{merchantId}")
    @Permission(value = "payment:channel:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<List<MerchantChannelVO>> listChannels(@PathVariable Long merchantId,
                                                        @RequestParam(required = false) String channelType,
                                                        @RequestParam(required = false) Integer status) {
        return ReturnResult.success(channelService.listChannels(merchantId, channelType, status));
    }

    @Operation(summary = "查询渠道配置列表")
    @GetMapping("/list")
    @Permission(value = "payment:channel:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<List<MerchantChannelVO>> listAllChannels(@RequestParam(required = false) Long merchantId,
                                                           @RequestParam(required = false) String channelType,
                                                           @RequestParam(required = false) Integer status) {
        return ReturnResult.success(channelService.listChannels(merchantId, channelType, status));
    }

    @Operation(summary = "支付方式目录与开通指引")
    @GetMapping("/catalog")
    @Permission(value = "payment:channel:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<List<PaymentMethodGuideVO>> listCatalog() {
        return ReturnResult.success(channelService.listCatalog());
    }

    @Operation(summary = "查询 provider SPI 选项")
    @GetMapping("/provider-options")
    @Permission(value = "payment:channel:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<List<ProviderSpiOptionVO>> listProviderOptions(@RequestParam String channelType) {
        return ReturnResult.success(channelService.listProviderOptions(channelType));
    }

    @Operation(summary = "启用渠道")
    @PutMapping("/{id}/enable")
    @Permission(value = "payment:channel:enable", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> enableChannel(@PathVariable Long id) {
        return ReturnResult.success(channelService.enableChannel(id));
    }

    @Operation(summary = "禁用渠道")
    @PutMapping("/{id}/disable")
    @Permission(value = "payment:channel:disable", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> disableChannel(@PathVariable Long id) {
        return ReturnResult.success(channelService.disableChannel(id));
    }

    @Operation(summary = "删除渠道配置")
    @DeleteMapping("/{id}")
    @Permission(value = "payment:channel:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> deleteChannel(@PathVariable Long id) {
        return ReturnResult.success(channelService.deleteChannel(id));
    }
}
