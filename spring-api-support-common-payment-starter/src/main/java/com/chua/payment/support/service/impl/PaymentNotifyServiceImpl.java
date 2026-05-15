package com.chua.payment.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.payment.support.channel.support.AbstractMerchantPaymentChannel;
import com.chua.payment.support.entity.MerchantChannel;
import com.chua.payment.support.dto.WalletRechargeDTO;
import com.chua.payment.support.entity.PaymentNotifyLog;
import com.chua.payment.support.entity.PaymentOrder;
import com.chua.payment.support.entity.PaymentRefundOrder;
import com.chua.payment.support.entity.WalletOrder;
import com.chua.payment.support.enums.ChannelStatus;
import com.chua.payment.support.enums.OrderState;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.mapper.MerchantChannelMapper;
import com.chua.payment.support.mapper.PaymentOrderMapper;
import com.chua.payment.support.provider.PaymentProviderGatewayRegistry;
import com.chua.payment.support.service.MerchantChannelService;
import com.chua.payment.support.service.PaymentNotifyLogService;
import com.chua.payment.support.service.PaymentNotifyService;
import com.chua.payment.support.service.PaymentOrderService;
import com.chua.payment.support.service.PaymentRefundOrderService;
import com.chua.payment.support.service.WalletAccountService;
import com.chua.payment.support.service.WalletOrderService;
import com.chua.starter.aliyun.support.payment.AliyunAlipayGateway;
import com.chua.starter.aliyun.support.payment.dto.AliyunAlipayNotifyRequest;
import com.chua.starter.aliyun.support.payment.dto.AliyunAlipayPayNotifyPayload;
import com.chua.starter.aliyun.support.properties.AliyunAlipayProperties;
import com.chua.starter.tencent.support.payment.TencentWechatPayGateway;
import com.chua.starter.tencent.support.payment.dto.TencentWechatNotifyRequest;
import com.chua.starter.tencent.support.payment.dto.TencentWechatPayNotifyPayload;
import com.chua.starter.tencent.support.payment.dto.TencentWechatRefundNotifyPayload;
import com.chua.starter.tencent.support.properties.TencentWechatPayProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 第三方支付异步通知处理
 */
@Slf4j
@Service
@ConditionalOnClass(name = {
        "com.chua.starter.aliyun.support.payment.AliyunAlipayGateway",
        "com.chua.starter.tencent.support.payment.TencentWechatPayGateway"
})
public class PaymentNotifyServiceImpl extends AbstractMerchantPaymentChannel implements PaymentNotifyService {

    private final MerchantChannelMapper merchantChannelMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final PaymentOrderService paymentOrderService;
    private final PaymentRefundOrderService paymentRefundOrderService;
    private final PaymentNotifyLogService paymentNotifyLogService;
    private final PaymentProviderGatewayRegistry providerGatewayRegistry;
    private final WalletAccountService walletAccountService;
    private final WalletOrderService walletOrderService;

    public PaymentNotifyServiceImpl(MerchantChannelService merchantChannelService,
                                    ObjectMapper objectMapper,
                                    MerchantChannelMapper merchantChannelMapper,
                                    PaymentOrderMapper paymentOrderMapper,
                                    PaymentOrderService paymentOrderService,
                                    PaymentRefundOrderService paymentRefundOrderService,
                                    PaymentNotifyLogService paymentNotifyLogService,
                                    PaymentProviderGatewayRegistry providerGatewayRegistry,
                                    WalletAccountService walletAccountService,
                                    WalletOrderService walletOrderService) {
        super(merchantChannelService, objectMapper);
        this.merchantChannelMapper = merchantChannelMapper;
        this.paymentOrderMapper = paymentOrderMapper;
        this.paymentOrderService = paymentOrderService;
        this.paymentRefundOrderService = paymentRefundOrderService;
        this.paymentNotifyLogService = paymentNotifyLogService;
        this.providerGatewayRegistry = providerGatewayRegistry;
        this.walletAccountService = walletAccountService;
        this.walletOrderService = walletOrderService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWechatPayNotify(Long channelId,
                                      String serialNumber,
                                      String timestamp,
                                      String nonce,
                                      String signature,
                                      String signType,
                                      String body) {
        String requestHeaders = toJson(buildWechatHeaders(serialNumber, timestamp, nonce, signature, signType));
        PaymentNotifyLog notifyLog = paymentNotifyLogService.start("WECHAT_PAY", channelId, requestHeaders, body);
        PaymentOrder relatedOrder = null;
        MerchantChannel channel = null;
        try {
            channel = requireEnabledChannel(channelId, "WECHAT");
            TencentWechatPayNotifyPayload payload = wechatGateway(providerSpi(channel, null))
                    .parsePayNotify(buildWechatProperties(channel), buildWechatNotifyRequest(serialNumber, timestamp, nonce, signature, signType, body));

            relatedOrder = requireOrder(payload.getOrderNo());
            validateOrderChannel(relatedOrder, channel);
            validateWechatIdentity(channel, payload.getAppId(), payload.getMerchantId());
            validateAmount(relatedOrder.getOrderAmount(), fenToYuan(payload.getTotalAmountFen()), "微信支付通知金额不匹配");

            if (!StringUtils.hasText(payload.getTradeState())) {
                throw new PaymentException("微信支付通知缺少交易状态");
            }

            switch (payload.getTradeState()) {
                case "SUCCESS", "REFUND" -> paymentOrderService.paySuccess(
                        relatedOrder.getId(),
                        resolveWechatPaidAmount(payload),
                        payload.getTransactionId(),
                        "wechat-notify");
                case "CLOSED", "REVOKED" -> {
                    if (canCancel(relatedOrder.getStatus())) {
                        paymentOrderService.cancelOrder(relatedOrder.getId(), "wechat-notify", firstNonBlank(payload.getTradeStateDesc(), "微信订单已关闭"));
                    } else {
                        log.info("微信支付关闭通知已忽略: orderNo={}, status={}", relatedOrder.getOrderNo(), relatedOrder.getStatus());
                    }
                }
                case "PAYERROR" -> paymentOrderService.payFail(relatedOrder.getId(), firstNonBlank(payload.getTradeStateDesc(), "微信支付失败"), "wechat-notify");
                case "NOTPAY", "USERPAYING", "ACCEPT" -> log.info("微信支付通知保持处理中: orderNo={}, tradeState={}", relatedOrder.getOrderNo(), payload.getTradeState());
                default -> log.info("微信支付通知未触发状态更新: orderNo={}, tradeState={}", relatedOrder.getOrderNo(), payload.getTradeState());
            }
            paymentNotifyLogService.finish(notifyLog.getId(),
                    1,
                    channel.getChannelType(),
                    channel.getChannelSubType(),
                    "SUCCESS",
                    firstNonBlank(payload.getTradeState(), "SUCCESS"),
                    relatedOrder.getId(),
                    null);
        } catch (Exception e) {
            paymentNotifyLogService.finish(notifyLog.getId(),
                    0,
                    channel != null ? channel.getChannelType() : null,
                    channel != null ? channel.getChannelSubType() : null,
                    "FAILED",
                    e.getMessage(),
                    relatedOrder != null ? relatedOrder.getId() : null,
                    null);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWechatRefundNotify(Long channelId,
                                         String serialNumber,
                                         String timestamp,
                                         String nonce,
                                         String signature,
                                         String signType,
                                         String body) {
        String requestHeaders = toJson(buildWechatHeaders(serialNumber, timestamp, nonce, signature, signType));
        PaymentNotifyLog notifyLog = paymentNotifyLogService.start("WECHAT_REFUND", channelId, requestHeaders, body);
        PaymentOrder relatedOrder = null;
        PaymentRefundOrder relatedRefundOrder = null;
        MerchantChannel channel = null;
        try {
            channel = requireEnabledChannel(channelId, "WECHAT");
            TencentWechatRefundNotifyPayload payload = wechatGateway(providerSpi(channel, null))
                    .parseRefundNotify(buildWechatProperties(channel), buildWechatNotifyRequest(serialNumber, timestamp, nonce, signature, signType, body));

            relatedRefundOrder = requireRefundOrder(payload.getRefundNo());
            relatedOrder = requireOrder(relatedRefundOrder.getOrderId());
            validateOrderChannel(relatedOrder, channel);
            validateAmount(relatedOrder.getOrderAmount(), fenToYuan(payload.getTotalAmountFen()), "微信退款通知原订单金额不匹配");

            BigDecimal refundAmount = fenToYuan(payload.getRefundAmountFen());
            if (!StringUtils.hasText(payload.getRefundStatus())) {
                throw new PaymentException("微信退款通知缺少退款状态");
            }

            switch (payload.getRefundStatus()) {
                case "SUCCESS" -> paymentOrderService.refundSuccess(
                        relatedRefundOrder.getRefundNo(),
                        refundAmount,
                        payload.getRefundId(),
                        "wechat-refund-notify",
                        toJson(payload),
                        "微信退款成功");
                case "CLOSED", "ABNORMAL" -> paymentOrderService.refundFail(
                        relatedRefundOrder.getRefundNo(),
                        toJson(payload),
                        "wechat-refund-notify",
                        "微信退款失败:" + payload.getRefundStatus());
                case "PROCESSING" -> {
                    paymentRefundOrderService.markProcessing(relatedRefundOrder.getRefundNo(), payload.getRefundId(), toJson(payload), "微信退款处理中");
                    log.info("微信退款通知保持处理中: orderNo={}, outRefundNo={}", relatedOrder.getOrderNo(), payload.getRefundNo());
                }
                default -> log.info("微信退款通知未触发状态更新: orderNo={}, refundStatus={}", relatedOrder.getOrderNo(), payload.getRefundStatus());
            }
            paymentNotifyLogService.finish(notifyLog.getId(),
                    1,
                    channel.getChannelType(),
                    channel.getChannelSubType(),
                    "SUCCESS",
                    firstNonBlank(payload.getRefundStatus(), "SUCCESS"),
                    relatedOrder.getId(),
                    relatedRefundOrder.getId());
        } catch (Exception e) {
            paymentNotifyLogService.finish(notifyLog.getId(),
                    0,
                    channel != null ? channel.getChannelType() : null,
                    channel != null ? channel.getChannelSubType() : null,
                    "FAILED",
                    e.getMessage(),
                    relatedOrder != null ? relatedOrder.getId() : null,
                    relatedRefundOrder != null ? relatedRefundOrder.getId() : null);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlipayPayNotify(Long channelId, Map<String, String> params) {
        PaymentNotifyLog notifyLog = paymentNotifyLogService.start("ALIPAY_PAY", channelId, null, toJson(params));
        PaymentOrder relatedOrder = null;
        MerchantChannel channel = null;
        try {
            channel = requireEnabledChannel(channelId, "ALIPAY");
            AliyunAlipayPayNotifyPayload payload = alipayGateway(providerSpi(channel, null))
                    .verifyAndParsePayNotify(buildAlipayProperties(channel), buildAlipayNotifyRequest(params));

            relatedOrder = requireOrder(payload.getOrderNo());
            validateOrderChannel(relatedOrder, channel);
            validateAlipayIdentity(channel, payload.getAppId(), payload.getSellerId());
            validateAmount(relatedOrder.getOrderAmount(), payload.getTotalAmount(), "支付宝支付通知金额不匹配");

            String tradeStatus = payload.getTradeStatus();
            if (!StringUtils.hasText(tradeStatus)) {
                throw new PaymentException("支付宝支付通知缺少 trade_status");
            }

            switch (tradeStatus) {
                case "TRADE_SUCCESS", "TRADE_FINISHED" -> {
                    paymentOrderService.paySuccess(relatedOrder.getId(), payload.getTotalAmount(), payload.getTradeNo(), "alipay-notify");
                    handleWalletRechargeIfNeeded(relatedOrder, payload);
                }
                case "TRADE_CLOSED" -> {
                    if (canCancel(relatedOrder.getStatus())) {
                        paymentOrderService.cancelOrder(relatedOrder.getId(), "alipay-notify", "支付宝交易已关闭");
                    } else {
                        log.info("支付宝关闭通知已忽略: orderNo={}, status={}", relatedOrder.getOrderNo(), relatedOrder.getStatus());
                    }
                }
                case "WAIT_BUYER_PAY" -> log.info("支付宝支付通知保持处理中: orderNo={}", relatedOrder.getOrderNo());
                default -> log.info("支付宝通知未触发状态更新: orderNo={}, tradeStatus={}", relatedOrder.getOrderNo(), tradeStatus);
            }
            paymentNotifyLogService.finish(notifyLog.getId(),
                    1,
                    channel.getChannelType(),
                    channel.getChannelSubType(),
                    "SUCCESS",
                    firstNonBlank(tradeStatus, "SUCCESS"),
                    relatedOrder.getId(),
                    null);
        } catch (Exception e) {
            paymentNotifyLogService.finish(notifyLog.getId(),
                    0,
                    channel != null ? channel.getChannelType() : null,
                    channel != null ? channel.getChannelSubType() : null,
                    "FAILED",
                    e.getMessage(),
                    relatedOrder != null ? relatedOrder.getId() : null,
                    null);
            throw e;
        }
    }

    /**
     * 支付宝钱包异步通知（充值/转账/提现）
     * <p>
     * 当前钱包操作（recharge/withdraw/transfer）为同步完成，无需等待外部回调。
     * 此方法主要用于记录通知日志以备审计追溯，不重复执行业务操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlipayWalletNotify(String orderType, String orderNo, Map<String, String> params) {
        log.info("收到支付宝钱包异步通知: orderType={}, orderNo={}, params={}", orderType, orderNo, params);
        paymentNotifyLogService.start("ALIPAY_WALLET_" + orderType, 0L, null, toJson(params));
    }

    /**
     * 微信钱包异步通知（充值/转账/提现）
     * <p>
     * 当前钱包操作（recharge/withdraw/transfer）为同步完成，无需等待外部回调。
     * 此方法主要用于记录通知日志以备审计追溯，不重复执行业务操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWechatWalletNotify(String orderType, String orderNo,
                                         String serialNumber, String timestamp, String nonce,
                                         String signature, String signType, String body) {
        String requestHeaders = toJson(buildWechatHeaders(serialNumber, timestamp, nonce, signature, signType));
        log.info("收到微信钱包异步通知: orderType={}, orderNo={}", orderType, orderNo);
        paymentNotifyLogService.start("WECHAT_WALLET_" + orderType, 0L, requestHeaders, body);
    }

    /**
     * 检测支付宝支付回调是否为钱包充值，若是则增加钱包余额
     */
    private void handleWalletRechargeIfNeeded(PaymentOrder order, AliyunAlipayPayNotifyPayload payload) {
        if (order.getBusinessOrderNo() == null || !order.getBusinessOrderNo().startsWith("WALLET_RECHARGE_")) {
            return;
        }
        String walletOrderNo = order.getBusinessOrderNo().substring("WALLET_RECHARGE_".length());
        WalletOrder walletOrder = walletOrderService.getByOrderNo(walletOrderNo);
        if (walletOrder == null) {
            log.error("钱包充值订单不存在: {}", walletOrderNo);
            return;
        }
        if (!"PENDING".equals(walletOrder.getStatus())) {
            log.info("钱包充值订单已处理，跳过: {}", walletOrderNo);
            return;
        }

        // 增加余额
        WalletRechargeDTO rechargeDTO = WalletRechargeDTO.builder()
                .merchantId(order.getMerchantId())
                .userId(order.getUserId())
                .rechargeNo(walletOrderNo)
                .amount(payload.getTotalAmount() != null ? payload.getTotalAmount() : walletOrder.getAmount())
                .operator("alipay-notify")
                .remark("支付宝充值到账:" + payload.getTradeNo())
                .build();
        walletAccountService.recharge(rechargeDTO);

        // 标记钱包订单成功
        walletOrderService.markSuccess(walletOrderNo, payload.getTradeNo(), "支付宝充值到账");

        log.info("钱包充值回调处理成功: walletOrderNo={}, tradeNo={}", walletOrderNo, payload.getTradeNo());
    }

    private TencentWechatPayGateway wechatGateway(String extension) {
        return providerGatewayRegistry.tencentWechatPayGateway(extension);
    }

    private AliyunAlipayGateway alipayGateway(String extension) {
        return providerGatewayRegistry.aliyunAlipayGateway(extension);
    }

    private MerchantChannel requireEnabledChannel(Long channelId, String channelType) {
        MerchantChannel channel = merchantChannelMapper.selectById(channelId);
        if (channel == null) {
            throw new PaymentException("支付方式不存在");
        }
        if (!channelType.equalsIgnoreCase(channel.getChannelType())) {
            throw new PaymentException("支付方式类型不匹配");
        }
        if (!Integer.valueOf(ChannelStatus.ENABLED.getCode()).equals(channel.getStatus())) {
            throw new PaymentException("支付方式未启用");
        }
        return channel;
    }

    private PaymentOrder requireOrder(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            throw new PaymentException("通知中缺少订单号");
        }
        PaymentOrder order = paymentOrderMapper.selectOne(new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getOrderNo, orderNo)
                .and(wrapper -> wrapper.isNull(PaymentOrder::getDeleted).or().eq(PaymentOrder::getDeleted, 0))
                .last("limit 1"));
        if (order == null) {
            throw new PaymentException("订单不存在: " + orderNo);
        }
        return order;
    }

    private PaymentOrder requireOrder(Long orderId) {
        PaymentOrder order = paymentOrderMapper.selectById(orderId);
        if (order == null || Integer.valueOf(1).equals(order.getDeleted())) {
            throw new PaymentException("订单不存在: " + orderId);
        }
        return order;
    }

    private PaymentRefundOrder requireRefundOrder(String refundNo) {
        if (!StringUtils.hasText(refundNo)) {
            throw new PaymentException("通知中缺少退款单号");
        }
        PaymentRefundOrder refundOrder = paymentRefundOrderService.getByRefundNo(refundNo);
        if (refundOrder == null) {
            throw new PaymentException("退款单不存在: " + refundNo);
        }
        return refundOrder;
    }

    private void validateOrderChannel(PaymentOrder order, MerchantChannel channel) {
        if (order.getChannelId() == null || !order.getChannelId().equals(channel.getId())) {
            throw new PaymentException("订单和回调支付方式不匹配");
        }
    }

    private void validateWechatIdentity(MerchantChannel channel, String appId, String merchantNo) {
        if (StringUtils.hasText(channel.getAppId()) && StringUtils.hasText(appId) && !channel.getAppId().equals(appId)) {
            throw new PaymentException("微信支付通知 AppId 不匹配");
        }
        if (StringUtils.hasText(channel.getMerchantNo()) && StringUtils.hasText(merchantNo) && !channel.getMerchantNo().equals(merchantNo)) {
            throw new PaymentException("微信支付通知商户号不匹配");
        }
    }

    private void validateAlipayIdentity(MerchantChannel channel, String appId, String sellerId) {
        if (StringUtils.hasText(channel.getAppId()) && StringUtils.hasText(appId) && !channel.getAppId().equals(appId)) {
            throw new PaymentException("支付宝通知 app_id 不匹配");
        }
        if (StringUtils.hasText(channel.getMerchantNo()) && StringUtils.hasText(sellerId) && !channel.getMerchantNo().equals(sellerId)) {
            throw new PaymentException("支付宝通知 seller_id 不匹配");
        }
    }

    private void validateAmount(BigDecimal expected, BigDecimal actual, String message) {
        if (expected == null || actual == null) {
            return;
        }
        if (expected.compareTo(actual) != 0) {
            throw new PaymentException(message);
        }
    }

    private BigDecimal resolveWechatPaidAmount(TencentWechatPayNotifyPayload payload) {
        if (payload == null) {
            return null;
        }
        Long paidAmountFen = payload.getPayerTotalAmountFen() != null ? payload.getPayerTotalAmountFen() : payload.getTotalAmountFen();
        return paidAmountFen == null ? null : fenToYuan(paidAmountFen);
    }

    private boolean canCancel(String status) {
        return OrderState.PENDING.name().equals(status) || OrderState.PAYING.name().equals(status);
    }

    private TencentWechatNotifyRequest buildWechatNotifyRequest(String serialNumber,
                                                                String timestamp,
                                                                String nonce,
                                                                String signature,
                                                                String signType,
                                                                String body) {
        TencentWechatNotifyRequest request = new TencentWechatNotifyRequest();
        request.setSerialNumber(serialNumber);
        request.setTimestamp(timestamp);
        request.setNonce(nonce);
        request.setSignature(signature);
        request.setSignType(signType);
        request.setBody(body);
        return request;
    }

    private AliyunAlipayNotifyRequest buildAlipayNotifyRequest(Map<String, String> params) {
        AliyunAlipayNotifyRequest request = new AliyunAlipayNotifyRequest();
        request.setParams(params);
        return request;
    }

    private Map<String, String> buildWechatHeaders(String serialNumber,
                                                   String timestamp,
                                                   String nonce,
                                                   String signature,
                                                   String signType) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Wechatpay-Serial", serialNumber);
        headers.put("Wechatpay-Timestamp", timestamp);
        headers.put("Wechatpay-Nonce", nonce);
        headers.put("Wechatpay-Signature", signature);
        headers.put("Wechatpay-Signature-Type", signType);
        return headers;
    }

    private TencentWechatPayProperties buildWechatProperties(MerchantChannel channel) {
        TencentWechatPayProperties properties = new TencentWechatPayProperties();
        properties.setAppId(requiredText(channel.getAppId(), "微信 AppId 不能为空"));
        properties.setMerchantId(requiredText(channel.getMerchantNo(), "微信商户号不能为空"));
        properties.setPrivateKey(requiredText(decryptValue(channel.getPrivateKey()), "微信商户私钥不能为空"));
        properties.setApiV3Key(requiredText(decryptValue(channel.getApiKey()), "微信 APIv3 Key 不能为空"));
        properties.setMerchantSerialNumber(requiredText(extText(channel, "merchantSerialNumber"), "微信商户证书序列号不能为空"));
        properties.setNotifyUrl(channel.getNotifyUrl());
        properties.setSandbox(channel.getSandboxMode() != null && channel.getSandboxMode() == 1);
        properties.setSandboxSignKey(extText(channel, "sandboxSignKey"));
        return properties;
    }

    private AliyunAlipayProperties buildAlipayProperties(MerchantChannel channel) {
        AliyunAlipayProperties properties = new AliyunAlipayProperties();
        properties.setAppId(requiredText(channel.getAppId(), "支付宝应用ID不能为空"));
        properties.setPrivateKey(requiredText(extractBase64Key(decryptValue(channel.getPrivateKey())), "支付宝私钥不能为空"));
        properties.setAlipayPublicKey(requiredText(channel.getPublicKey(), "支付宝公钥不能为空"));
        properties.setSandbox(channel.getSandboxMode() != null && channel.getSandboxMode() == 1);
        properties.setServerUrl(extText(channel, "serverUrl"));
        properties.setCharset(firstNonBlank(extText(channel, "charset"), "UTF-8"));
        properties.setFormat(firstNonBlank(extText(channel, "format"), "json"));
        properties.setSignType(firstNonBlank(extText(channel, "signType"), "RSA2"));
        return properties;
    }
}
