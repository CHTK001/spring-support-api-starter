package com.chua.payment.support.service.impl;

import com.chua.payment.support.channel.PaymentChannelRegistry;
import com.chua.payment.support.entity.MerchantChannel;
import com.chua.payment.support.enums.ChannelStatus;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.mapper.MerchantChannelMapper;
import com.chua.payment.support.mapper.MerchantMapper;
import com.chua.payment.support.mapper.PaymentOrderMapper;
import com.chua.payment.support.service.MerchantPaymentConfigService;
import com.chua.payment.support.service.PaymentGlobalConfigService;
import com.chua.payment.support.service.OrderStateLogService;
import com.chua.payment.support.service.OrderStateMachineService;
import com.chua.payment.support.service.PaymentRefundOrderService;
import com.chua.payment.support.service.TransactionRecordService;
import com.chua.payment.support.service.UserCouponService;
import com.chua.payment.support.service.WalletAccountService;
import com.chua.payment.support.spi.UserOpenIdResolver;
import com.chua.starter.strategy.template.IdempotentTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentOrderServiceImplTest {

    private final PaymentOrderMapper orderMapper = mock(PaymentOrderMapper.class);
    private final MerchantMapper merchantMapper = mock(MerchantMapper.class);
    private final MerchantChannelMapper merchantChannelMapper = mock(MerchantChannelMapper.class);
    private final OrderStateMachineService stateMachineService = mock(OrderStateMachineService.class);
    private final OrderStateLogService orderStateLogService = mock(OrderStateLogService.class);
    private final TransactionRecordService transactionRecordService = mock(TransactionRecordService.class);
    private final PaymentChannelRegistry paymentChannelRegistry = mock(PaymentChannelRegistry.class);
    private final PaymentRefundOrderService paymentRefundOrderService = mock(PaymentRefundOrderService.class);
    private final MerchantPaymentConfigService merchantPaymentConfigService = mock(MerchantPaymentConfigService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserOpenIdResolver userOpenIdResolver = mock(UserOpenIdResolver.class);
    private final PaymentGlobalConfigService paymentGlobalConfigService = mock(PaymentGlobalConfigService.class);
    private final IdempotentTemplate idempotentTemplate = mock(IdempotentTemplate.class);
    private final UserCouponService userCouponService = mock(UserCouponService.class);
    private final WalletAccountService walletAccountService = mock(WalletAccountService.class);

    private PaymentOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentOrderServiceImpl(
                orderMapper,
                merchantMapper,
                merchantChannelMapper,
                stateMachineService,
                orderStateLogService,
                transactionRecordService,
                paymentChannelRegistry,
                paymentRefundOrderService,
                merchantPaymentConfigService,
                eventPublisher,
                objectMapper,
                userOpenIdResolver,
                paymentGlobalConfigService,
                idempotentTemplate,
                userCouponService,
                walletAccountService);
    }

    @Test
    void shouldResolveCompositeRouteToExecutableTargetChannel() {
        MerchantChannel composite = routeChannel(100L, "{\"targetChannelId\":200}");
        MerchantChannel target = executableChannel(200L, 9L, "WECHAT", "APP", ChannelStatus.ENABLED.getCode());
        when(merchantChannelMapper.selectById(200L)).thenReturn(target);
        when(paymentChannelRegistry.supports("WECHAT", "APP")).thenReturn(true);

        MerchantChannel resolved = ReflectionTestUtils.invokeMethod(service, "resolveEffectiveChannel", composite, 9L);

        assertSame(target, resolved);
    }

    @Test
    void shouldRejectCompositeRouteToDisabledTargetChannel() {
        MerchantChannel composite = routeChannel(100L, "{\"targetChannelId\":200}");
        MerchantChannel target = executableChannel(200L, 9L, "WECHAT", "APP", ChannelStatus.DISABLED.getCode());
        when(merchantChannelMapper.selectById(200L)).thenReturn(target);

        assertThrows(
                PaymentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "resolveEffectiveChannel", composite, 9L));
    }

    private MerchantChannel routeChannel(Long id, String extConfig) {
        MerchantChannel channel = executableChannel(id, 9L, "COMPOSITE", "AGGREGATE_ROUTE", ChannelStatus.ENABLED.getCode());
        channel.setExtConfig(extConfig);
        return channel;
    }

    private MerchantChannel executableChannel(Long id, Long merchantId, String channelType, String channelSubType, Integer status) {
        MerchantChannel channel = new MerchantChannel();
        channel.setId(id);
        channel.setMerchantId(merchantId);
        channel.setChannelType(channelType);
        channel.setChannelSubType(channelSubType);
        channel.setStatus(status);
        return channel;
    }
}
