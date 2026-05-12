package com.chua.payment.support.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.payment.support.common.PageResult;
import com.chua.payment.support.entity.WalletOrder;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.service.WalletAccountService;
import com.chua.payment.support.service.WalletOrderService;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 钱包订单控制器
 */
@RestController
@RequestMapping("/api/wallet/order")
@RequiredArgsConstructor
public class WalletOrderController {

    private final WalletOrderService walletOrderService;
    private final WalletAccountService walletAccountService;

    @GetMapping("/page")
    @Permission(value = "payment:wallet:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<PageResult<WalletOrder>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "10") int pageSize,
                                                       @RequestParam(required = false) Long merchantId,
                                                       @RequestParam(required = false) Long userId,
                                                       @RequestParam(required = false) String orderType,
                                                       @RequestParam(required = false) String status) {
        Page<WalletOrder> page = walletOrderService.page(pageNum, pageSize, merchantId, userId, orderType, status);
        return ReturnResult.success(PageResult.of(page));
    }

    @PutMapping("/{orderNo}/confirm")
    @Permission(value = "payment:wallet:withdraw", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Void> confirmWithdraw(@PathVariable String orderNo) {
        WalletOrder order = walletOrderService.getByOrderNo(orderNo);
        if (order == null) {
            throw new PaymentException("钱包订单不存在");
        }
        if (!"PROCESSING".equals(order.getStatus())) {
            throw new PaymentException("仅处理中的订单可以确认到账");
        }
        walletAccountService.confirmWithdraw(order.getMerchantId(), order.getUserId(), order.getOrderNo(), order.getAmount());
        walletOrderService.markSuccess(order.getOrderNo(), null);
        return ReturnResult.success();
    }

    @PutMapping("/{orderNo}/cancel")
    @Permission(value = "payment:wallet:withdraw", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Void> cancelWithdraw(@PathVariable String orderNo) {
        WalletOrder order = walletOrderService.getByOrderNo(orderNo);
        if (order == null) {
            throw new PaymentException("钱包订单不存在");
        }
        if (!"PROCESSING".equals(order.getStatus())) {
            throw new PaymentException("仅处理中的订单可以取消");
        }
        walletAccountService.cancelWithdraw(order.getMerchantId(), order.getUserId(), order.getOrderNo(), order.getAmount(), "管理员取消提现");
        walletOrderService.markCancelled(order.getOrderNo(), "管理员取消");
        return ReturnResult.success();
    }
}
