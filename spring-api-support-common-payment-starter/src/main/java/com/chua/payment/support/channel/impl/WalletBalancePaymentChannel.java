package com.chua.payment.support.channel.impl;

import com.chua.payment.support.channel.*;
import com.chua.payment.support.channel.support.AbstractMerchantPaymentChannel;
import com.chua.payment.support.entity.MerchantChannel;
import com.chua.payment.support.entity.WalletAccountLog;
import com.chua.payment.support.entity.WalletOrder;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.service.MerchantChannelService;
import com.chua.payment.support.service.WalletAccountService;
import com.chua.payment.support.service.WalletLimitService;
import com.chua.payment.support.service.WalletOrderService;
import com.chua.payment.support.vo.WalletAccountVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * 钱包余额支付
 */
@Component
public class WalletBalancePaymentChannel extends AbstractMerchantPaymentChannel implements PaymentChannel {

    private final WalletAccountService walletAccountService;
    private final WalletOrderService walletOrderService;
    private final WalletLimitService walletLimitService;

    public WalletBalancePaymentChannel(MerchantChannelService merchantChannelService,
                                       ObjectMapper objectMapper,
                                       WalletAccountService walletAccountService,
                                       WalletOrderService walletOrderService,
                                       WalletLimitService walletLimitService) {
        super(merchantChannelService, objectMapper);
        this.walletAccountService = walletAccountService;
        this.walletOrderService = walletOrderService;
        this.walletLimitService = walletLimitService;
    }

    @Override
    public boolean supports(String channelType, String channelSubType) {
        return "WALLET".equalsIgnoreCase(channelType) && "BALANCE".equalsIgnoreCase(channelSubType);
    }

    @Override
    public OrderCreateResult createOrder(MerchantChannel channel, OrderCreateRequest request) {
        if (request.getUserId() == null) {
            throw new PaymentException("钱包订单必须提供 userId");
        }
        walletAccountService.ensureAccount(channel.getMerchantId(), request.getUserId());

        OrderCreateResult result = new OrderCreateResult();
        result.setSuccess(true);
        result.setOrderNo(request.getOrderNo());
        result.setMessage("钱包订单创建成功");
        return result;
    }

    @Override
    public PaymentResult pay(MerchantChannel channel, PaymentRequest request) {
        if (request.getUserId() == null) {
            throw new PaymentException("钱包支付必须提供 userId");
        }
        walletAccountService.pay(channel.getMerchantId(),
                request.getUserId(),
                request.getAmount(),
                request.getOrderNo(),
                "wallet-pay",
                firstNonBlank(request.getSubject(), request.getBody(), request.getOrderNo()));

        PaymentResult result = new PaymentResult();
        result.setSuccess(true);
        result.setTradeNo("WALLET_PAY_" + request.getOrderNo());
        result.setPaidAmount(request.getAmount());
        result.setStatus("PAID");
        result.setMessage("钱包余额支付成功");
        result.setRawResponse("wallet-balance-pay");
        return result;
    }

    @Override
    public PaymentResult query(MerchantChannel channel, String orderNo) {
        WalletAccountLog log = walletAccountService.getLog("PAY", channel.getMerchantId(), null, orderNo);
        PaymentResult result = new PaymentResult();
        result.setSuccess(log != null);
        result.setTradeNo(log != null ? "WALLET_PAY_" + orderNo : null);
        result.setPaidAmount(log != null ? log.getChangeAmount() : null);
        result.setStatus(log != null ? "PAID" : "FAILED");
        result.setMessage(log != null ? "钱包余额支付成功" : "未找到钱包支付记录");
        result.setRawResponse(log != null ? toJson(log) : null);
        return result;
    }

    @Override
    public RefundResult refund(MerchantChannel channel, RefundRequest request) {
        if (request.getUserId() == null) {
            throw new PaymentException("钱包退款缺少用户上下文");
        }
        walletAccountService.refund(channel.getMerchantId(),
                request.getUserId(),
                request.getRefundAmount(),
                request.getRefundNo(),
                "wallet-refund",
                request.getReason());

        RefundResult result = new RefundResult();
        result.setSuccess(true);
        result.setRefundNo(request.getRefundNo());
        result.setTradeNo("WALLET_REFUND_" + request.getRefundNo());
        result.setRefundAmount(request.getRefundAmount());
        result.setStatus("REFUNDED");
        result.setMessage("钱包退款成功");
        result.setRawResponse("wallet-balance-refund");
        return result;
    }

    @Override
    public RefundResult queryRefund(MerchantChannel channel, RefundRequest request) {
        WalletAccountLog log = walletAccountService.getLog("REFUND", channel.getMerchantId(), null, request.getRefundNo());
        RefundResult result = new RefundResult();
        result.setSuccess(log != null);
        result.setRefundNo(request.getRefundNo());
        result.setTradeNo(log != null ? "WALLET_REFUND_" + request.getRefundNo() : null);
        result.setRefundAmount(log != null ? log.getChangeAmount() : request.getRefundAmount());
        result.setStatus(log != null ? "REFUNDED" : "FAILED");
        result.setMessage(log != null ? "钱包退款成功" : "未找到钱包退款记录");
        result.setRawResponse(log != null ? toJson(log) : null);
        return result;
    }

    @Override
    public RechargeResult recharge(MerchantChannel channel, RechargeRequest request) {
        if (request.getUserId() == null) {
            throw new PaymentException("钱包充值必须提供 userId");
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new PaymentException("充值金额必须大于0");
        }

        walletLimitService.validateRechargeLimit(request.getMerchantId(), request.getUserId(), request.getAmount());

        WalletAccountVO account = walletAccountService.getAccount(request.getMerchantId(), request.getUserId());
        walletLimitService.validateBalanceLimit(request.getMerchantId(), request.getUserId(), account.getAvailableBalance(), request.getAmount());

        WalletOrder order = walletOrderService.createRechargeOrder(request);
        try {
            walletAccountService.recharge(
                    com.chua.payment.support.dto.WalletRechargeDTO.builder()
                            .merchantId(request.getMerchantId())
                            .userId(request.getUserId())
                            .rechargeNo(order.getOrderNo())
                            .amount(request.getAmount())
                            .operator(request.getOperator())
                            .remark(request.getRemark())
                            .build()
            );

            walletOrderService.markSuccess(order.getOrderNo(), toJson(request));

            RechargeResult result = new RechargeResult();
            result.setSuccess(true);
            result.setRechargeNo(order.getOrderNo());
            result.setTradeNo("WALLET_RECHARGE_" + order.getOrderNo());
            result.setAmount(request.getAmount());
            result.setStatus("SUCCESS");
            result.setMessage("钱包充值成功");
            result.setRawResponse(toJson(order));
            return result;
        } catch (Exception e) {
            walletOrderService.markFailed(order.getOrderNo(), null, e.getMessage());
            throw e;
        }
    }

    @Override
    public TransferResult transfer(MerchantChannel channel, TransferRequest request) {
        if (request.getFromUserId() == null || request.getToUserId() == null) {
            throw new PaymentException("转账必须提供转出和转入用户ID");
        }
        if (request.getFromUserId().equals(request.getToUserId())) {
            throw new PaymentException("不能转账给自己");
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new PaymentException("转账金额必须大于0");
        }

        walletLimitService.validateTransferLimit(request.getMerchantId(), request.getFromUserId(), request.getAmount());

        WalletOrder order = walletOrderService.createTransferOrder(request);
        try {
            walletAccountService.transfer(
                    com.chua.payment.support.dto.WalletTransferDTO.builder()
                            .merchantId(request.getMerchantId())
                            .fromUserId(request.getFromUserId())
                            .toUserId(request.getToUserId())
                            .transferNo(order.getOrderNo())
                            .amount(request.getAmount())
                            .operator(request.getOperator())
                            .remark(request.getRemark())
                            .build()
            );

            walletOrderService.markSuccess(order.getOrderNo(), toJson(request));

            TransferResult result = new TransferResult();
            result.setSuccess(true);
            result.setTransferNo(order.getOrderNo());
            result.setTradeNo("WALLET_TRANSFER_" + order.getOrderNo());
            result.setAmount(request.getAmount());
            result.setStatus("SUCCESS");
            result.setMessage("钱包转账成功");
            result.setRawResponse(toJson(order));
            return result;
        } catch (Exception e) {
            walletOrderService.markFailed(order.getOrderNo(), null, e.getMessage());
            throw e;
        }
    }

    @Override
    public WithdrawResult withdraw(MerchantChannel channel, WithdrawRequest request) {
        if (request.getUserId() == null) {
            throw new PaymentException("钱包提现必须提供 userId");
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new PaymentException("提现金额必须大于0");
        }
        if (!org.springframework.util.StringUtils.hasText(request.getBankAccount())) {
            throw new PaymentException("银行账号不能为空");
        }

        walletLimitService.validateWithdrawLimit(request.getMerchantId(), request.getUserId(), request.getAmount());

        WalletOrder order = walletOrderService.createWithdrawOrder(request);
        try {
            walletAccountService.withdraw(
                    com.chua.payment.support.dto.WalletWithdrawDTO.builder()
                            .merchantId(request.getMerchantId())
                            .userId(request.getUserId())
                            .withdrawNo(order.getOrderNo())
                            .amount(request.getAmount())
                            .bankAccount(request.getBankAccount())
                            .bankName(request.getBankName())
                            .accountName(request.getAccountName())
                            .operator(request.getOperator())
                            .remark(request.getRemark())
                            .build()
            );

            walletOrderService.markSuccess(order.getOrderNo(), toJson(request));

            WithdrawResult result = new WithdrawResult();
            result.setSuccess(true);
            result.setWithdrawNo(order.getOrderNo());
            result.setTradeNo("WALLET_WITHDRAW_" + order.getOrderNo());
            result.setAmount(request.getAmount());
            result.setStatus("SUCCESS");
            result.setMessage("钱包提现成功");
            result.setRawResponse(toJson(order));
            return result;
        } catch (Exception e) {
            walletOrderService.markFailed(order.getOrderNo(), null, e.getMessage());
            throw e;
        }
    }

}
