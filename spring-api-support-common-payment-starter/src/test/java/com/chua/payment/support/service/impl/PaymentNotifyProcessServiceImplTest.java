package com.chua.payment.support.service.impl;

import com.chua.payment.support.entity.PaymentNotifyLog;
import com.chua.payment.support.mapper.PaymentNotifyErrorMapper;
import com.chua.payment.support.mapper.PaymentNotifyLogMapper;
import com.chua.payment.support.service.PaymentNotifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentNotifyProcessServiceImplTest {

    private final PaymentNotifyLogMapper notifyLogMapper = mock(PaymentNotifyLogMapper.class);
    private final PaymentNotifyErrorMapper notifyErrorMapper = mock(PaymentNotifyErrorMapper.class);
    private final PaymentNotifyService paymentNotifyService = mock(PaymentNotifyService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private PaymentNotifyProcessServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentNotifyProcessServiceImpl(
                notifyLogMapper,
                notifyErrorMapper,
                paymentNotifyService,
                eventPublisher);
        doAnswer(invocation -> 1).when(notifyLogMapper).updateById(any(PaymentNotifyLog.class));
    }

    @Test
    void shouldDispatchWechatPayNotifyFromPersistedLog() {
        PaymentNotifyLog log = paymentLog(100L, "PAYMENT", "WECHAT");
        log.setRequestHeaders("""
                Wechatpay-Serial: serial-001
                Wechatpay-Timestamp: 1710000000
                Wechatpay-Nonce: nonce-abc
                Wechatpay-Signature: sign-xyz
                Wechatpay-Signature-Type: WECHATPAY2-SHA256-RSA2048
                """);
        log.setRequestBody("{\"id\":\"wx-pay-body\"}");
        when(notifyLogMapper.selectById(100L)).thenReturn(log);

        service.processPaymentNotify(log);

        verify(paymentNotifyService).handleWechatPayNotify(
                88L,
                "serial-001",
                "1710000000",
                "nonce-abc",
                "sign-xyz",
                "WECHATPAY2-SHA256-RSA2048",
                "{\"id\":\"wx-pay-body\"}");
        verify(paymentNotifyService, never()).handleWechatRefundNotify(any(), any(), any(), any(), any(), any(), any());
        verify(paymentNotifyService, never()).handleAlipayPayNotify(any(), any());
    }

    @Test
    void shouldDispatchWechatRefundNotifyFromPersistedLog() {
        PaymentNotifyLog log = paymentLog(101L, "REFUND", "WECHAT");
        log.setRequestHeaders("""
                Wechatpay-Serial: serial-002
                Wechatpay-Timestamp: 1710000001
                Wechatpay-Nonce: nonce-refund
                Wechatpay-Signature: sign-refund
                Wechatpay-Signature-Type: WECHATPAY2-SHA256-RSA2048
                """);
        log.setRequestBody("{\"id\":\"wx-refund-body\"}");
        when(notifyLogMapper.selectById(101L)).thenReturn(log);

        service.processRefundNotify(log);

        verify(paymentNotifyService).handleWechatRefundNotify(
                88L,
                "serial-002",
                "1710000001",
                "nonce-refund",
                "sign-refund",
                "WECHATPAY2-SHA256-RSA2048",
                "{\"id\":\"wx-refund-body\"}");
        verify(paymentNotifyService, never()).handleWechatPayNotify(any(), any(), any(), any(), any(), any(), any());
        verify(paymentNotifyService, never()).handleAlipayPayNotify(any(), any());
    }

    @Test
    void shouldDispatchAlipayPayNotifyFromPersistedParams() {
        PaymentNotifyLog log = paymentLog(102L, "PAYMENT", "ALIPAY");
        log.setRequestParams("trade_status=TRADE_SUCCESS&out_trade_no=ORDER-001&trade_no=ALI-001&");
        when(notifyLogMapper.selectById(102L)).thenReturn(log);

        service.processPaymentNotify(log);

        verify(paymentNotifyService).handleAlipayPayNotify(eq(88L), eq(java.util.Map.of(
                "trade_status", "TRADE_SUCCESS",
                "out_trade_no", "ORDER-001",
                "trade_no", "ALI-001"
        )));
        verify(paymentNotifyService, never()).handleWechatPayNotify(any(), any(), any(), any(), any(), any(), any());
        verify(paymentNotifyService, never()).handleWechatRefundNotify(any(), any(), any(), any(), any(), any(), any());
    }

    private PaymentNotifyLog paymentLog(Long id, String notifyType, String channelType) {
        PaymentNotifyLog log = new PaymentNotifyLog();
        log.setId(id);
        log.setChannelId(88L);
        log.setMerchantId(9L);
        log.setNotifyType(notifyType);
        log.setChannelType(channelType);
        log.setProcessStatus("PENDING");
        log.setRetryCount(0);
        log.setReceivedAt(LocalDateTime.now());
        return log;
    }
}
