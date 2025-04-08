package com.chua.starter.pay.support.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.pay.support.entity.PayMerchantOrderWater;
import com.chua.starter.pay.support.service.PayMerchantOrderService;
import com.chua.starter.pay.support.service.PayMerchantOrderWaterService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 订单流水管理接口
 *
 * @author CH
 * @since 2024/12/30
 */
@Api(tags = "订单流水管理接口")
@Tag(name = "订单流水管理接口")
@RestController
@RequestMapping("/v3/pay/order/water")
@Slf4j
@RequiredArgsConstructor
public class PayOrderWaterController {


    final PayMerchantOrderService payMerchantOrderService;
    final PayMerchantOrderWaterService payMerchantOrderWaterService;


    /**
     * 订单查询流水
     *
     * @param payMerchantOrderCode 订单编号
     * @return 流水
     */
    @GetMapping("/water")
    @Operation(summary = "订单查询流水")
    public ReturnResult<List<PayMerchantOrderWater>> water(@Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                                                           @Parameter(hidden = true) @UserValue("deptId") String deptId,
                                                           @Parameter(hidden = true) @UserValue("userId") String userId,
                                                           String payMerchantOrderCode) {
        return payMerchantOrderWaterService.water(roles, deptId, userId, payMerchantOrderCode);
    }
}
