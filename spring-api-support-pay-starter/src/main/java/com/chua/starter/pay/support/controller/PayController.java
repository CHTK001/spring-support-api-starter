package com.chua.starter.pay.support.controller;

import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.pay.support.pojo.*;
import com.chua.starter.pay.support.result.PayOrderResponse;
import com.chua.starter.pay.support.result.PaySignResponse;
import com.chua.starter.pay.support.service.PayService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付接口
 *
 * @author CH
 * @since 2024/12/30
 */
@Api(tags = "支付管理接口")
@Tag(name = "支付管理接口")
@RestController
@RequestMapping("/v3/pay/order")
@Slf4j
@RequiredArgsConstructor
public class PayController {

    final PayService payService;

    /**
     * 创建订单
     */
    @PutMapping("/createOrder")
    @Operation(summary = "创建订单")
    public ReturnResult<PayOrderResponse> createOrder(@Validated(AddGroup.class) @RequestBody PayOrderCreateRequest request,
                                                      @UserValue("openId") String openId,
                                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        PayOrderRequest payOrderRequest = BeanUtils.copyProperties(request, PayOrderRequest.class);
        payOrderRequest.setUserId(StringUtils.defaultString(openId, null));
        return payService.createOrder(payOrderRequest);
    }


    /**
     * 重建订单
     */
    @PutMapping("/updateOrder")
    @Operation(summary = "更新订单")
    public ReturnResult<PayOrderResponse> updateOrder(@Validated(AddGroup.class) @RequestBody PayUpdateOrderRequest request,
                                                      @Parameter(hidden = true) @UserValue("userId") String userId,
                                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return payService.updateOrder(request, userId);
    }


    /**
     * 重建订单
     */
    @PutMapping("/reCreateOrder")
    @Operation(summary = "创建订单")
    public ReturnResult<PayOrderResponse> reCreateOrder(@Validated(AddGroup.class) @RequestBody PayReOrderRequest request,
                                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return payService.recreateOrder(request);
    }

    /**
     * 创建签名
     */
    @PutMapping("/createSign")
    @Operation(summary = "创建签名")
    public ReturnResult<PaySignResponse> createSign(@Validated(AddGroup.class) @RequestBody PaySignCreateRequest request,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return payService.createSign(request);
    }
}
