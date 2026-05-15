package com.chua.payment.support.configuration;

import com.chua.payment.support.exception.PaymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 支付敏感字段加解密服务。
 * 支持新配置密钥，同时兼容历史固定密钥，避免旧数据无法解密。
 */
@Component
public class PaymentCipherService {

    private static final Logger log = LoggerFactory.getLogger(PaymentCipherService.class);
    static final String LEGACY_DEFAULT_KEY = "PaymentSystem16";

    private final Environment environment;

    public PaymentCipherService(Environment environment) {
        this.environment = environment;
    }

    public String encrypt(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return plainText;
        }
        return doCipher(plainText, requiredEncryptionKey(), Cipher.ENCRYPT_MODE);
    }

    public String decrypt(String encryptedText) {
        if (!StringUtils.hasText(encryptedText)) {
            return encryptedText;
        }
        PaymentException lastException = null;
        List<String> keys = candidateDecryptionKeys();
        log.debug("[CipherService] decrypt: inputLen={}, candidateKeys={}", encryptedText.length(), keys.size());
        for (String key : keys) {
            try {
                String result = doCipher(encryptedText, key, Cipher.DECRYPT_MODE);
                // AES-ECB 误检测校验：解密结果含有大量控制字符或替换字符说明密钥错误
                if (isLikelyGarbage(result)) {
                    log.debug("[CipherService] decrypt produced garbage with key prefix: {}, contentPreview={}",
                            key.substring(0, Math.min(key.length(), 4)),
                            result.substring(0, Math.min(result.length(), 60)));
                    lastException = new PaymentException("解密结果校验失败（内容异常）");
                    continue;
                }
                log.debug("[CipherService] decrypt SUCCESS with key prefix: {}", key.substring(0, Math.min(key.length(), 4)));
                return result;
            } catch (PaymentException e) {
                log.debug("[CipherService] decrypt FAILED with key prefix: {}, error: {}", key.substring(0, Math.min(key.length(), 4)), e.getMessage());
                lastException = e;
            }
        }
        throw new PaymentException("解密失败，请检查 payment/encryption 或 plugin.payment.security 密钥配置", lastException);
    }

    /**
     * 检查解密结果是否为 AES-ECB 误检测（假阳性）产生的乱码。
     * 条件：替换字符 � 出现，或控制字符占比超过 30%。
     */
    private boolean isLikelyGarbage(String text) {
        if (!StringUtils.hasText(text)) {
            return true;
        }
        int controlCount = 0;
        int total = text.length();
        for (int i = 0; i < total; i++) {
            char c = text.charAt(i);
            if (c == '�') {
                return true;
            }
            if (c < 0x20 && c != '\t' && c != '\n' && c != '\r') {
                controlCount++;
            }
        }
        return (double) controlCount / total > 0.3;
    }

    List<String> candidateDecryptionKeys() {
        Set<String> keys = new LinkedHashSet<>();
        if (StringUtils.hasText(configuredEncryptionKey())) {
            keys.add(configuredEncryptionKey());
        }
        keys.addAll(splitKeys(property(
                "plugin.payment.security.legacy-keys",
                "plugin.payment.security.legacyKeys",
                "payment.encryption.legacy-keys",
                "payment.encryption.legacyKeys")));
        keys.add(LEGACY_DEFAULT_KEY);
        return new ArrayList<>(keys);
    }

    String requiredEncryptionKey() {
        String key = configuredEncryptionKey();
        return StringUtils.hasText(key) ? key : LEGACY_DEFAULT_KEY;
    }

    private String configuredEncryptionKey() {
        return property(
                "plugin.payment.security.key",
                "plugin.payment.security.encryption-key",
                "plugin.payment.security.encryptionKey",
                "payment.encryption.key",
                "payment.encryption.encryption-key",
                "payment.encryption.encryptionKey");
    }

    private String property(String... keys) {
        for (String key : keys) {
            String value = environment.getProperty(key);
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private List<String> splitKeys(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        List<String> keys = new ArrayList<>();
        for (String item : value.split(",")) {
            if (StringUtils.hasText(item)) {
                keys.add(item.trim());
            }
        }
        return keys;
    }

    private String doCipher(String value, String keyText, int mode) {
        try {
            SecretKeySpec key = new SecretKeySpec(normalizeKey(keyText), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, key);
            if (mode == Cipher.ENCRYPT_MODE) {
                byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(encrypted);
            }
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(value));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new PaymentException(mode == Cipher.ENCRYPT_MODE ? "加密失败" : "解密失败", e);
        }
    }

    private byte[] normalizeKey(String keyText) {
        if (!StringUtils.hasText(keyText)) {
            throw new PaymentException("支付加密密钥不能为空");
        }
        byte[] source = keyText.getBytes(StandardCharsets.UTF_8);
        byte[] normalized = new byte[16];
        int length = Math.min(source.length, normalized.length);
        System.arraycopy(source, 0, normalized, 0, length);
        return normalized;
    }
}
