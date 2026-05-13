package com.chua.payment.support.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.payment.support.channel.RechargeRequest;
import com.chua.payment.support.channel.TransferRequest;
import com.chua.payment.support.channel.WithdrawRequest;
import com.chua.payment.support.common.PageResult;
import com.chua.payment.support.channel.PaymentResult;
import com.chua.payment.support.dto.OrderCreateDTO;
import com.chua.payment.support.dto.OrderPayDTO;
import com.chua.payment.support.dto.WalletRechargeDTO;
import com.chua.payment.support.dto.WalletTransferDTO;
import com.chua.payment.support.dto.WalletWithdrawDTO;
import com.chua.payment.support.entity.WalletOrder;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.service.PaymentOrderService;
import com.chua.payment.support.service.WalletAccountService;
import com.chua.payment.support.service.WalletOrderService;
import com.chua.payment.support.vo.OrderVO;
import java.math.RoundingMode;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 钱包订单控制器
 */
@RestController
@RequestMapping("/wallet/order")
@RequiredArgsConstructor
public class WalletOrderController {

    private final WalletOrderService walletOrderService;
    private final WalletAccountService walletAccountService;
    private final PaymentOrderService paymentOrderService;

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

    @GetMapping("/{orderNo}")
    @Permission(value = "payment:wallet:view", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN, AuthConstant.OPS})
    public ReturnResult<WalletOrder> getByOrderNo(@PathVariable String orderNo) {
        WalletOrder order = walletOrderService.getByOrderNo(orderNo);
        if (order == null) {
            throw new PaymentException("钱包订单不存在");
        }
        return ReturnResult.success(order);
    }

    @PostMapping("/recharge")
    @Permission(value = "payment:wallet:recharge", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<WalletOrder> createRechargeOrder(@RequestBody WalletRechargeDTO dto) {
        RechargeRequest request = new RechargeRequest();
        request.setRechargeNo(dto.getRechargeNo());
        request.setMerchantId(dto.getMerchantId());
        request.setUserId(dto.getUserId());
        request.setAmount(dto.getAmount());
        request.setOperator(dto.getOperator());
        request.setRemark(dto.getRemark());
        request.setCreateOrder(dto.getCreateOrder());
        WalletOrder order = walletOrderService.createRechargeOrder(request);
        return ReturnResult.success(order);
    }

    @PostMapping("/recharge/pay")
    @Permission(value = "payment:wallet:recharge", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<PaymentResult> rechargePay(@RequestBody WalletRechargeDTO dto) {
        // 1. 创建钱包订单（PENDING）
        RechargeRequest rechargeRequest = new RechargeRequest();
        rechargeRequest.setRechargeNo(dto.getRechargeNo());
        rechargeRequest.setMerchantId(dto.getMerchantId());
        rechargeRequest.setUserId(dto.getUserId());
        rechargeRequest.setAmount(dto.getAmount());
        rechargeRequest.setOperator(dto.getOperator());
        rechargeRequest.setRemark(dto.getRemark());
        rechargeRequest.setCreateOrder(true);
        WalletOrder walletOrder = walletOrderService.createRechargeOrder(rechargeRequest);

        // 2. 创建支付订单（businessOrderNo = WALLET_RECHARGE_ + 钱包订单号）
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setMerchantId(dto.getMerchantId());
        createDTO.setUserId(dto.getUserId());
        createDTO.setOrderAmount(dto.getAmount());
        createDTO.setChannelType("ALIPAY");
        createDTO.setSubject("钱包充值-" + walletOrder.getOrderNo());
        createDTO.setBody("钱包充值:" + dto.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString() + "元");
        createDTO.setBusinessOrderNo("WALLET_RECHARGE_" + walletOrder.getOrderNo());
        createDTO.setRemark(dto.getRemark());
        OrderVO order = paymentOrderService.createOrder(createDTO);

        // 3. 发起支付宝支付
        OrderPayDTO payDTO = new OrderPayDTO();
        payDTO.setOperator(dto.getOperator());
        PaymentResult result = paymentOrderService.payOrder(order.getId(), payDTO);

        return ReturnResult.success(result);
    }

    @PostMapping("/transfer")
    @Permission(value = "payment:wallet:transfer", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<WalletOrder> createTransferOrder(@RequestBody WalletTransferDTO dto) {
        TransferRequest request = new TransferRequest();
        request.setTransferNo(dto.getTransferNo());
        request.setMerchantId(dto.getMerchantId());
        request.setFromUserId(dto.getFromUserId());
        request.setToUserId(dto.getToUserId());
        request.setAmount(dto.getAmount());
        request.setOperator(dto.getOperator());
        request.setRemark(dto.getRemark());
        request.setTransferMethod(dto.getTransferMethod() != null ? dto.getTransferMethod() : "WALLET");
        WalletOrder order = walletOrderService.createTransferOrder(request);
        return ReturnResult.success(order);
    }

    @PostMapping("/withdraw")
    @Permission(value = "payment:wallet:withdraw", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<WalletOrder> createWithdrawOrder(@RequestBody WalletWithdrawDTO dto) {
        WithdrawRequest request = new WithdrawRequest();
        request.setWithdrawNo(dto.getWithdrawNo());
        request.setMerchantId(dto.getMerchantId());
        request.setUserId(dto.getUserId());
        request.setAmount(dto.getAmount());
        request.setBankAccount(dto.getBankAccount());
        request.setBankName(dto.getBankName());
        request.setAccountName(dto.getAccountName());
        request.setNotifyUrl(dto.getNotifyUrl());
        request.setOperator(dto.getOperator());
        request.setRemark(dto.getRemark());
        WalletOrder order = walletOrderService.createWithdrawOrder(request);
        return ReturnResult.success(order);
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
        walletOrderService.markSuccess(order.getOrderNo(), null, null);
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
