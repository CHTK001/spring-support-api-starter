package com.chua.payment.support.service.impl;

import com.chua.payment.support.entity.PaymentOrder;
import com.chua.payment.support.event.PaymentSuccessEvent;
import com.chua.payment.support.event.RefundSuccessEvent;
import com.chua.payment.support.mapper.PaymentOrderMapper;
import com.chua.payment.support.service.PaymentRefundOrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 商户回调通知处理器
 * <p>
 * 监听支付成功事件，向商户的 notifyUrl 发送 HTTP POST 回调通知。
 * </p>
 *
 * @author CH
 * @since 2026-05-10
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.payment.merchant-notify", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MerchantNotifyEventHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final PaymentOrderMapper orderMapper;
    private final PaymentRefundOrderService paymentRefundOrderService;

    @EventListener
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        PaymentOrder order = orderMapper.selectById(event.getOrderId());
        if (order == null) {
            log.warn("[商户回调] 订单不存在: orderId={}", event.getOrderId());
            return;
        }
        String notifyUrl = order.getNotifyUrl();
        if (notifyUrl == null || notifyUrl.isBlank()) {
            log.debug("[商户回调] notifyUrl 为空，跳过回调: orderNo={}", order.getOrderNo());
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderNo", order.getOrderNo());
        payload.put("businessOrderNo", order.getBusinessOrderNo());
        payload.put("merchantId", order.getMerchantId());
        payload.put("userId", order.getUserId());
        payload.put("amount", order.getOrderAmount());
        payload.put("paidAmount", order.getPaidAmount());
        payload.put("thirdPartyOrderNo", order.getThirdPartyOrderNo());
        payload.put("channelType", order.getChannelType());
        payload.put("status", order.getStatus());
        payload.put("notifyType", "PAY_SUCCESS");
        payload.put("timestamp", System.currentTimeMillis());

        sendNotify(notifyUrl, payload, order.getOrderNo());
    }

    @EventListener
    public void handleRefundSuccess(RefundSuccessEvent event) {
        var refundOrder = paymentRefundOrderService.getById(event.getRefundId());
        if (refundOrder == null) {
            log.warn("[商户回调-退款] 退款单不存在: refundId={}", event.getRefundId());
            return;
        }

        PaymentOrder order = orderMapper.selectById(event.getOrderId());
        if (order == null) {
            log.warn("[商户回调-退款] 订单不存在: orderId={}", event.getOrderId());
            return;
        }

        String notifyUrl = order.getNotifyUrl();
        if (notifyUrl == null || notifyUrl.isBlank()) {
            log.debug("[商户回调-退款] notifyUrl 为空，跳过回调: orderNo={}", order.getOrderNo());
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderNo", order.getOrderNo());
        payload.put("businessOrderNo", order.getBusinessOrderNo());
        payload.put("merchantId", order.getMerchantId());
        payload.put("userId", order.getUserId());
        payload.put("amount", order.getOrderAmount());
        payload.put("paidAmount", order.getPaidAmount());
        payload.put("refundAmount", event.getRefundAmount());
        payload.put("refundNo", event.getRefundNo());
        payload.put("thirdPartyRefundNo", event.getThirdPartyRefundNo());
        payload.put("channelType", order.getChannelType());
        payload.put("status", order.getStatus());
        payload.put("notifyType", "REFUND_SUCCESS");
        payload.put("timestamp", System.currentTimeMillis());

        // 虚拟线程异步执行回调通知
        Thread.ofVirtual().name("refund-notify-" + event.getRefundNo()).start(() ->
                sendNotify(notifyUrl, payload, order.getOrderNo()));
    }

    private void sendNotify(String notifyUrl, Map<String, Object> payload, String orderNo) {
        try {
            byte[] body = MAPPER.writeValueAsBytes(payload);
            URI uri = URI.create(notifyUrl);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "Payment-Merchant-Notify/1.0");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                log.info("[商户回调] 通知成功: orderNo={}, notifyUrl={}, responseCode={}",
                        orderNo, maskUrl(notifyUrl), responseCode);
            } else {
                log.warn("[商户回调] 通知失败: orderNo={}, notifyUrl={}, responseCode={}",
                        orderNo, maskUrl(notifyUrl), responseCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            log.warn("[商户回调] 通知异常: orderNo={}, notifyUrl={}, error={}",
                    orderNo, maskUrl(notifyUrl), e.getMessage());
        }
    }

    private static String maskUrl(String url) {
        if (url == null) return null;
        int atIndex = url.indexOf('@');
        if (atIndex > 0) {
            int schemeEnd = url.indexOf("://");
            int start = schemeEnd > 0 ? schemeEnd + 3 : 0;
            return url.substring(0, start) + "***" + url.substring(atIndex);
        }
        return url;
    }
}
