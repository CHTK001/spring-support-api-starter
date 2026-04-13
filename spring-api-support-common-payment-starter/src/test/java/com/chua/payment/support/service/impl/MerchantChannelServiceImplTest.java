package com.chua.payment.support.service.impl;

import com.chua.payment.support.channel.PaymentChannelRegistry;
import com.chua.payment.support.configuration.PaymentCipherService;
import com.chua.payment.support.configuration.PaymentProviderProperties;
import com.chua.payment.support.dto.ChannelConfigDTO;
import com.chua.payment.support.entity.Merchant;
import com.chua.payment.support.entity.MerchantChannel;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.mapper.MerchantChannelMapper;
import com.chua.payment.support.mapper.MerchantMapper;
import com.chua.payment.support.provider.PaymentProviderGatewayRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MerchantChannelServiceImplTest {

    private final MerchantChannelMapper channelMapper = mock(MerchantChannelMapper.class);
    private final MerchantMapper merchantMapper = mock(MerchantMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PaymentProviderProperties paymentProviderProperties = new PaymentProviderProperties();
    private final PaymentProviderGatewayRegistry providerGatewayRegistry = mock(PaymentProviderGatewayRegistry.class);
    private final PaymentCipherService paymentCipherService =
            new PaymentCipherService(new MockEnvironment().withProperty("plugin.payment.security.key", "0123456789abcdef"));
    private final PaymentChannelRegistry paymentChannelRegistry = mock(PaymentChannelRegistry.class);

    private MerchantChannelServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MerchantChannelServiceImpl(
                channelMapper,
                merchantMapper,
                objectMapper,
                paymentProviderProperties,
                providerGatewayRegistry,
                paymentCipherService,
                paymentChannelRegistry);
        Merchant merchant = new Merchant();
        merchant.setId(9L);
        when(merchantMapper.selectById(9L)).thenReturn(merchant);
        doAnswer(invocation -> {
            MerchantChannel channel = invocation.getArgument(0);
            if (channel.getId() == null) {
                channel.setId(1000L);
            }
            return 1;
        }).when(channelMapper).insert(any(MerchantChannel.class));
    }

    @Test
    void shouldPersistProviderSpiIntoNormalizedExtConfig() throws Exception {
        when(providerGatewayRegistry.availableAliyunExtensions()).thenReturn(Set.of("default", "mock"));
        when(paymentChannelRegistry.supports("ALIPAY", "APP")).thenReturn(true);
        ChannelConfigDTO dto = alipayAppDto();
        dto.setProviderSpi("mock");
        dto.setExtConfig("{\"scene\":\"mobile\"}");

        service.createChannel(dto);

        ArgumentCaptor<MerchantChannel> captor = ArgumentCaptor.forClass(MerchantChannel.class);
        org.mockito.Mockito.verify(channelMapper).insert(captor.capture());
        Map<String, Object> extConfig = objectMapper.readValue(
                captor.getValue().getExtConfig(),
                new TypeReference<Map<String, Object>>() {
                });
        assertEquals("mock", extConfig.get("providerSpi"));
        assertEquals("mobile", extConfig.get("scene"));
    }

    @Test
    void shouldRejectUnknownProviderSpiWhenCreatingChannel() {
        when(providerGatewayRegistry.availableAliyunExtensions()).thenReturn(Set.of("default", "mock"));
        ChannelConfigDTO dto = alipayAppDto();
        dto.setProviderSpi("missing");

        PaymentException exception = assertThrows(PaymentException.class, () -> service.createChannel(dto));

        assertTrue(exception.getMessage().contains("支付 provider SPI 不存在"));
    }

    @Test
    void shouldRejectCompositeRouteTargetWithoutExecutableImplementation() {
        when(paymentChannelRegistry.supports("COMPOSITE", "AGGREGATE_ROUTE")).thenReturn(true);
        when(paymentChannelRegistry.supports("WECHAT", "NATIVE")).thenReturn(false);
        MerchantChannel target = new MerchantChannel();
        target.setId(88L);
        target.setMerchantId(9L);
        target.setChannelType("WECHAT");
        target.setChannelSubType("NATIVE");
        when(channelMapper.selectById(88L)).thenReturn(target);
        ChannelConfigDTO dto = new ChannelConfigDTO();
        dto.setMerchantId(9L);
        dto.setChannelType("COMPOSITE");
        dto.setChannelSubType("AGGREGATE_ROUTE");
        dto.setChannelName("聚合路由");
        dto.setStatus(1);
        dto.setExtConfig("{\"targetChannelId\":88}");

        PaymentException exception = assertThrows(PaymentException.class, () -> service.createChannel(dto));

        assertTrue(exception.getMessage().contains("当前版本不可执行"));
    }

    private ChannelConfigDTO alipayAppDto() {
        ChannelConfigDTO dto = new ChannelConfigDTO();
        dto.setMerchantId(9L);
        dto.setChannelType("ALIPAY");
        dto.setChannelSubType("APP");
        dto.setChannelName("支付宝 APP");
        dto.setAppId("app-001");
        dto.setPrivateKey("private-key");
        dto.setPublicKey("public-key");
        dto.setStatus(1);
        return dto;
    }
}
