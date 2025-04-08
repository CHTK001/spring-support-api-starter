package com.chua.starter.pay.support.controller;

import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.ApiGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.group.IgnoreGroup;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.pay.support.dto.PayOrderStatisticDTO;
import com.chua.starter.pay.support.entity.PayMerchantOrder;
import com.chua.starter.pay.support.pojo.PayMerchantOrderQueryRequest;
import com.chua.starter.pay.support.pojo.PayOrderQueryRequest;
import com.chua.starter.pay.support.pojo.PayRefundCreateRequest;
import com.chua.starter.pay.support.pojo.PayRefundRequest;
import com.chua.starter.pay.support.result.PayRefundResponse;
import com.chua.starter.pay.support.service.PayMerchantOrderService;
import com.chua.starter.pay.support.service.PayMerchantOrderWaterService;
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
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 订单接口
 *
 * @author CH
 * @since 2024/12/30
 */
@Api(tags = "订单管理接口")
@Tag(name = "订单管理接口")
@RestController
@RequestMapping("/v3/pay/order/history")
@Slf4j
@RequiredArgsConstructor
public class PayOrderController {


    final PayOrderService payOrderService;
    final PayMerchantOrderService payMerchantOrderService;
    final PayMerchantOrderWaterService payMerchantOrderWaterService;

    /**
     * 分页查询
     *
     * @param page          分页
     * @param request       请求
     * @param bindingResult 验证
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询当前用户订单(v3)")
    public ReturnPageResult<PayMerchantOrder> page(@ParameterObject Query<PayMerchantOrder> page,
                                                   @Validated(SelectGroup.class) @ParameterObject PayOrderQueryRequest request,
                                                   @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                                                   @Parameter(hidden = true) @UserValue("deptId") String deptId,
                                                   @Parameter(hidden = true) @UserValue("userId") String userId,
                                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnPageResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return payMerchantOrderService.page(page, roles, deptId, userId, request);
    }

    /**
     * 分页查询
     *
     * @param page          分页
     * @param request       请求
     * @param bindingResult 验证
     * @return
     */
    @GetMapping("/merchant")
    @Operation(summary = "分页查询订单(商家)")
    @Permission({"sys:pay:order:page"})
    public ReturnPageResult<PayMerchantOrder> page(@ParameterObject Query<PayMerchantOrder> page,
                                                   @Validated(SelectGroup.class) @ParameterObject PayMerchantOrderQueryRequest request,
                                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnPageResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return payMerchantOrderService.page(page, request);
    }


    /**
     * 详情
     */
    @GetMapping("/detail")
    @Operation(summary = "订单详情")
    @ApiGroup(IgnoreGroup.class)
    public ReturnResult<PayMerchantOrder> detail(String payMerchantOrderCode) {
        return payOrderService.detail(payMerchantOrderCode);
    }

    /**
     * 最新未支付订单
     */
    @GetMapping("/newestNoPayOrder")
    @Operation(summary = "最新未支付订单")
    public ReturnResult<String> newestNoPayOrder(@Parameter(hidden = true) @UserValue("userId") String userId) {
        return payOrderService.newestNoPayOrder(userId);
    }

    /**
     * 获取订单统计数据
     */
    @GetMapping("/statistic")
    @Operation(summary = "获取订单统计数据")
    @Permission({"sys:pay:order:statistic"})
    public ReturnResult<PayOrderStatisticDTO> statistic() {
        return ReturnResult.ok(payOrderService.statOrderInfo());
    }

    /**
     * 关闭订单
     */
    @PutMapping("/cancel")
    @Operation(summary = "关闭订单(退款)")
    @Permission({"sys:pay:cancel"})
    public ReturnResult<PayRefundResponse> cancel(@Validated(UpdateGroup.class) @RequestBody PayRefundCreateRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        PayRefundRequest refundRequest = BeanUtils.copyProperties(request, PayRefundRequest.class);
        BeanUtils.copyProperties(request, refundRequest);
        return payOrderService.cancel(refundRequest);
    }

    /**
     * 退款
     */
    @PutMapping("/refund")
    @Operation(summary = "退款")
    @Permission({"sys:pay:refund"})
    public ReturnResult<PayRefundResponse> refund(@Validated(UpdateGroup.class) @RequestBody PayRefundCreateRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        PayRefundRequest refundRequest = BeanUtils.copyProperties(request, PayRefundRequest.class);
        BeanUtils.copyProperties(request, refundRequest);
        return payOrderService.refund(refundRequest);
    }
}
