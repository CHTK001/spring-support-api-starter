package com.chua.payment.support.channel.impl;

import com.chua.payment.support.channel.*;
import com.chua.payment.support.channel.support.AbstractMerchantPaymentChannel;
import com.chua.payment.support.entity.MerchantChannel;
import com.chua.payment.support.entity.RechargeRewardRule;
import com.chua.payment.support.entity.WalletAccountLog;
import com.chua.payment.support.entity.WalletOrder;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.service.MerchantChannelService;
import com.chua.payment.support.service.RechargeRewardRuleService;
import com.chua.payment.support.service.UserCouponService;
import com.chua.payment.support.service.WalletAccountService;
import com.chua.payment.support.service.WalletLimitService;
import com.chua.payment.support.service.WalletOrderService;
import com.chua.payment.support.vo.WalletAccountVO;
import com.chua.starter.strategy.template.IdempotentTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 钱包余额支付
 */
@Slf4j
@Component
public class WalletBalancePaymentChannel extends AbstractMerchantPaymentChannel implements PaymentChannel {

    private final WalletAccountService walletAccountService;
    private final WalletOrderService walletOrderService;
    private final WalletLimitService walletLimitService;
    private final IdempotentTemplate idempotentTemplate;
    private final RechargeRewardRuleService rechargeRewardRuleService;
    private final UserCouponService userCouponService;

    public WalletBalancePaymentChannel(MerchantChannelService merchantChannelService,
                                       ObjectMapper objectMapper,
                                       WalletAccountService walletAccountService,
                                       WalletOrderService walletOrderService,
                                       WalletLimitService walletLimitService,
                                       IdempotentTemplate idempotentTemplate,
                                       RechargeRewardRuleService rechargeRewardRuleService,
                                       UserCouponService userCouponService) {
        super(merchantChannelService, objectMapper);
        this.walletAccountService = walletAccountService;
        this.walletOrderService = walletOrderService;
        this.walletLimitService = walletLimitService;
        this.idempotentTemplate = idempotentTemplate;
        this.rechargeRewardRuleService = rechargeRewardRuleService;
        this.userCouponService = userCouponService;
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

        walletAccountService.ensureAccount(request.getMerchantId(), request.getUserId());
        WalletAccountVO account = walletAccountService.getAccount(request.getMerchantId(), request.getUserId());
        walletLimitService.validateBalanceLimit(request.getMerchantId(), request.getUserId(), account.getAvailableBalance(), request.getAmount());

        String idempotentKey = "payment:idempotent:order:RECHARGE:" + request.getMerchantId() + ":" + request.getUserId() + ":" + request.getRechargeNo();
        if (!idempotentTemplate.tryAcquire(idempotentKey, 10, TimeUnit.MINUTES)) {
            throw new PaymentException("请求正在处理中，请勿重复提交");
        }
        boolean createWalletOrder = request.getCreateOrder() == null || request.getCreateOrder();
        WalletOrder order = null;
        String orderNo;
        try {
            if (createWalletOrder) {
                order = walletOrderService.createRechargeOrder(request);
                if (order == null) {
                    throw new PaymentException("充值订单创建失败");
                }
                orderNo = order.getOrderNo();
            } else {
                orderNo = "RECHARGE_" + request.getRechargeNo();
            }

            walletAccountService.recharge(
                    com.chua.payment.support.dto.WalletRechargeDTO.builder()
                            .merchantId(request.getMerchantId())
                            .userId(request.getUserId())
                            .rechargeNo(orderNo)
                            .amount(request.getAmount())
                            .operator(request.getOperator())
                            .remark(request.getRemark())
                            .build()
            );

            if (createWalletOrder) {
                walletOrderService.markSuccess(order.getOrderNo(), toJson(request));
            }

            // 充值奖励：匹配并发放奖励
            applyRechargeRewards(request.getMerchantId(), request.getUserId(), request.getAmount(), request.getOperator(), orderNo);

            RechargeResult result = new RechargeResult();
            result.setSuccess(true);
            result.setRechargeNo(orderNo);
            result.setTradeNo("WALLET_RECHARGE_" + orderNo);
            result.setAmount(request.getAmount());
            result.setStatus("SUCCESS");
            result.setMessage("钱包充值成功");
            result.setRawResponse(createWalletOrder && order != null ? toJson(order) : null);
            return result;
        } catch (Exception e) {
            if (order != null) {
                walletOrderService.markFailed(order.getOrderNo(), null, e.getMessage());
            }
            throw e;
        } finally {
            idempotentTemplate.release(idempotentKey);
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

        String method = request.getTransferMethod();
        if (method == null || method.isEmpty()) {
            method = "WALLET";
        }

        if (!"WALLET".equals(method)) {
            throw new PaymentException("暂不支持" + method + "转账方式，当前仅支持 WALLET（钱包转账）");
        }

        String idempotentKey = "payment:idempotent:order:TRANSFER:" + request.getMerchantId() + ":" + request.getFromUserId() + ":" + request.getTransferNo();
        if (!idempotentTemplate.tryAcquire(idempotentKey, 10, TimeUnit.MINUTES)) {
            throw new PaymentException("请求正在处理中，请勿重复提交");
        }
        WalletOrder order = null;
        try {
            order = walletOrderService.createTransferOrder(request);
            if (order == null) {
                throw new PaymentException("转账订单创建失败");
            }
            walletAccountService.transfer(
                    com.chua.payment.support.dto.WalletTransferDTO.builder()
                            .merchantId(request.getMerchantId())
                            .fromUserId(request.getFromUserId())
                            .toUserId(request.getToUserId())
                            .transferNo(order.getOrderNo())
                            .amount(request.getAmount())
                            .operator(request.getOperator())
                            .remark(request.getRemark())
                            .transferMethod(method)
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
            if (order != null) {
                walletOrderService.markFailed(order.getOrderNo(), null, e.getMessage());
            }
            throw e;
        } finally {
            idempotentTemplate.release(idempotentKey);
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

        String idempotentKey = "payment:idempotent:order:WITHDRAW:" + request.getMerchantId() + ":" + request.getUserId() + ":" + request.getWithdrawNo();
        if (!idempotentTemplate.tryAcquire(idempotentKey, 10, TimeUnit.MINUTES)) {
            throw new PaymentException("请求正在处理中，请勿重复提交");
        }
        WalletOrder order = null;
        try {
            order = walletOrderService.createWithdrawOrder(request);
            if (order == null) {
                throw new PaymentException("提现订单创建失败");
            }
            // 冻结余额，不直接扣减（待管理员确认到账）
            walletAccountService.freezeForWithdraw(
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

            walletOrderService.markProcessing(order.getOrderNo(), null, toJson(request));

            WithdrawResult result = new WithdrawResult();
            result.setSuccess(true);
            result.setWithdrawNo(order.getOrderNo());
            result.setTradeNo("WALLET_WITHDRAW_" + order.getOrderNo());
            result.setAmount(request.getAmount());
            result.setStatus("PROCESSING");
            result.setMessage("钱包提现申请已提交，等待确认到账");
            result.setRawResponse(toJson(order));
            return result;
        } catch (Exception e) {
            if (order != null) {
                walletOrderService.markFailed(order.getOrderNo(), null, e.getMessage());
            }
            throw e;
        } finally {
            idempotentTemplate.release(idempotentKey);
        }
    }

    /**
     * 充值奖励发放
     */
    private void applyRechargeRewards(Long merchantId, Long userId, BigDecimal amount, String operator, String rechargeOrderNo) {
        try {
            List<RechargeRewardRule> rules = rechargeRewardRuleService.matchRules(merchantId, amount);
            for (RechargeRewardRule rule : rules) {
                String rewardType = rule.getRewardType();
                if ("COUPON".equals(rewardType) && rule.getRewardValue() != null) {
                    Long templateId = Long.parseLong(rule.getRewardValue());
                    userCouponService.receive(merchantId, userId, templateId);
                    log.info("充值赠送优惠券: merchantId={}, userId={}, templateId={}, rechargeNo={}",
                            merchantId, userId, templateId, rechargeOrderNo);
                } else if ("BALANCE".equals(rewardType) && rule.getRewardValue() != null) {
                    BigDecimal bonusRate = new BigDecimal(rule.getRewardValue());
                    BigDecimal bonus = amount.multiply(bonusRate).setScale(2, RoundingMode.HALF_UP);
                    if (bonus.compareTo(BigDecimal.ZERO) > 0) {
                        walletAccountService.recharge(
                                com.chua.payment.support.dto.WalletRechargeDTO.builder()
                                        .merchantId(merchantId)
                                        .userId(userId)
                                        .rechargeNo("BONUS_" + rechargeOrderNo)
                                        .amount(bonus)
                                        .operator(operator)
                                        .remark("充值奖励: 充值" + amount + "赠送" + bonus)
                                        .build()
                        );
                        log.info("充值赠送余额: merchantId={}, userId={}, bonus={}, rechargeNo={}",
                                merchantId, userId, bonus, rechargeOrderNo);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("充值奖励发放异常: merchantId={}, userId={}, rechargeNo={}",
                    merchantId, userId, rechargeOrderNo, e);
        }
    }

}
