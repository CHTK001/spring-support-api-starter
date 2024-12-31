package com.chua.starter.pay.support.controller;

import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.pay.support.pojo.PayOrderCreateRequest;
import com.chua.starter.pay.support.pojo.PayOrderRequest;
import com.chua.starter.pay.support.pojo.PayRefundCreateRequest;
import com.chua.starter.pay.support.pojo.PayRefundRequest;
import com.chua.starter.pay.support.result.PayOrderResponse;
import com.chua.starter.pay.support.result.PayRefundResponse;
import com.chua.starter.pay.support.service.PayMerchantService;
import com.chua.starter.pay.support.service.PayOrderService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付接口
 * @author CH
 * @since 2024/12/30
 */
@Api(tags = "支付接口")
@Tag(name = "支付接口")
@RestController
@RequestMapping("/v2/pay/order")
@Slf4j
@RequiredArgsConstructor
public class PayController {


    final PayMerchantService payMerchantService;
    final PayOrderService payOrderService;


    /**
     * 退款
     */
    @PutMapping("/refund")
    @Operation(summary = "退款")
    public ReturnResult<PayRefundResponse> refund(@Validated(UpdateGroup.class) @ParameterObject PayRefundCreateRequest request, @Parameter(hidden = true) @UserValue("userId") String userId, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        PayRefundRequest refundRequest = BeanUtils.copyProperties(request, PayRefundRequest.class);
        BeanUtils.copyProperties(request, refundRequest);
        return payOrderService.refund(refundRequest);
    }
    /**
     * 创建订单
     */
    @PutMapping("/createOrder")
    @Operation(summary = "创建订单")
    public ReturnResult<PayOrderResponse> createOrder(@Validated(AddGroup.class) @ParameterObject PayOrderCreateRequest request,
                                                      @Parameter(hidden = true) @UserValue("userId") String userId,
                                                      BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        PayOrderRequest payOrderRequest = BeanUtils.copyProperties(request, PayOrderRequest.class);
        payOrderRequest.setUserId("oKAK06xoEXmuSKB-XdbCsPKzZM4I");
        return payOrderService.createOrder(payOrderRequest);
    }
}
