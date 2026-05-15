package com.chua.payment.support.channel.support;

import com.chua.payment.support.entity.MerchantChannel;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.service.MerchantChannelService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;

/**
 * 商户支付渠道公共能力
 */
@RequiredArgsConstructor
public abstract class AbstractMerchantPaymentChannel {

    public static final String EXT_PROVIDER_SPI = "providerSpi";
    public static final String DEFAULT_PROVIDER_SPI = "default";

    protected final MerchantChannelService merchantChannelService;
    protected final ObjectMapper objectMapper;

    protected Map<String, Object> extConfig(MerchantChannel channel) {
        if (!StringUtils.hasText(channel.getExtConfig())) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(channel.getExtConfig(), new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new PaymentException("支付方式扩展配置格式错误", e);
        }
    }

    protected String extText(MerchantChannel channel, String key) {
        Object value = extConfig(channel).get(key);
        return value == null ? null : String.valueOf(value);
    }

    protected String providerSpi(MerchantChannel channel) {
        return providerSpi(channel, DEFAULT_PROVIDER_SPI);
    }

    protected String providerSpi(MerchantChannel channel, String fallback) {
        return firstNonBlank(
                extText(channel, EXT_PROVIDER_SPI),
                extText(channel, "providerExtension"),
                extText(channel, "spi"),
                fallback);
    }

    /**
     * 从 PEM 中提取纯 Base64 内容（去掉 BEGIN/END 标记和所有空白）。
     * 支付宝 SDK 内部将整个 PEM 字符串做 Base64 解码（含标记文本），
     * JDK 21+ DER 解析器对冗余字节很严格，会抛 "extra data at the end"。
     * 给支付宝使用的密钥必须先剥离 PEM 标记。
     */
    protected String extractBase64Key(String pem) {
        if (!StringUtils.hasText(pem)) {
            return pem;
        }
        String beginMarker = pem.contains("-----BEGIN RSA PRIVATE KEY-----")
                ? "-----BEGIN RSA PRIVATE KEY-----" : "-----BEGIN PRIVATE KEY-----";
        String endMarker = beginMarker.replace("BEGIN", "END");
        int beginIdx = pem.indexOf(beginMarker);
        int endIdx = pem.indexOf(endMarker);
        if (beginIdx < 0 || endIdx < 0) {
            return pem.trim();
        }
        return pem.substring(beginIdx + beginMarker.length(), endIdx)
                .replaceAll("[\\r\\n\\s]", "");
    }

    protected String decryptValue(String encryptedValue) {
        if (!StringUtils.hasText(encryptedValue)) {
            return null;
        }
        String raw;
        try {
            raw = merchantChannelService.decryptApiKey(encryptedValue);
        } catch (Exception e) {
            raw = encryptedValue;
        }
        return normalizePemKey(raw);
    }

    /**
     * 清理 PEM 私钥：
     * 1. 提取 BEGIN / END 标记之间的 Base64 内容，剔除所有空白字符
     * 2. 按 PKCS8 标准格式重新组装（每 64 字符换行）
     * 3. JDK 21+ PKCS8 解析器对末尾冗余字节很严格，否则抛 "extra data at the end"
     */
    protected String normalizePemKey(String pem) {
        if (!StringUtils.hasText(pem)) {
            return pem;
        }
        String beginMarker = pem.contains("-----BEGIN RSA PRIVATE KEY-----")
                ? "-----BEGIN RSA PRIVATE KEY-----" : "-----BEGIN PRIVATE KEY-----";
        String endMarker = beginMarker.replace("BEGIN", "END");

        int beginIdx = pem.indexOf(beginMarker);
        int endIdx = pem.indexOf(endMarker);
        if (beginIdx < 0 || endIdx < 0) {
            return pem.trim();
        }
        // 提取 Base64 内容，去掉所有空白
        String b64 = pem.substring(beginIdx + beginMarker.length(), endIdx)
                .replaceAll("[\\r\\n\\s]", "");
        if (b64.isEmpty()) {
            return pem.trim();
        }
        // 按 64 字符换行重新组装
        StringBuilder sb = new StringBuilder(b64);
        int idx = 64;
        while (idx < sb.length()) {
            sb.insert(idx, "\n");
            idx += 65; // 64 chars + 1 newline
        }
        return beginMarker + "\n" + sb + "\n" + endMarker;
    }

    protected long yuanToFen(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    protected BigDecimal fenToYuan(Number amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(amount.longValue())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    protected String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    protected String requiredText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new PaymentException(message);
        }
        return value;
    }

    protected String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
}
