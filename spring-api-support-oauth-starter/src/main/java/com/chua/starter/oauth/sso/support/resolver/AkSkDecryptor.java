package com.chua.starter.oauth.sso.support.resolver;

import com.chua.common.support.crypto.Codec;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * AK/SK 解密工具
 * <p>
 * 用于解密客户端使用 SK 加密的数据
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/17
 */
@Slf4j
public class AkSkDecryptor {

    private static final String DEFAULT_ENCRYPTION = "aes";

    /**
     * 解密数据
     * <p>
     * 根据 AK 查询 SK，使用 SK 解密数据
     * </p>
     *
     * @param accessKey  AccessKey
     * @param data       加密的数据
     * @param encryption 加密算法
     * @return 解密后的数据，失败返回 null
     */
    public static String decrypt(String accessKey, String data, String encryption) {
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(data)) {
            return null;
        }

        // 获取 SK
        SecretKeyResolver resolver = ServiceProvider.of(SecretKeyResolver.class).getNewExtension("database");
        if (resolver == null) {
            log.error("SecretKeyResolver 未配置");
            return null;
        }

        String secretKey = resolver.getSecretKey(accessKey);
        if (StringUtils.isBlank(secretKey)) {
            log.warn("AK/SK验证失败: ak={}", maskKey(accessKey));
            return null;
        }

        try {
            String enc = StringUtils.defaultString(encryption, DEFAULT_ENCRYPTION);
            return Codec.build(enc, secretKey).decodeHex(data);
        } catch (Exception e) {
            log.error("AK/SK解密失败: ak={}", maskKey(accessKey), e);
            return null;
        }
    }

    /**
     * 加密数据
     * <p>
     * 根据 AK 查询 SK，使用 SK 加密数据
     * </p>
     *
     * @param accessKey  AccessKey
     * @param data       原始数据
     * @param encryption 加密算法
     * @return 加密后的数据，失败返回 null
     */
    public static String encrypt(String accessKey, String data, String encryption) {
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(data)) {
            return null;
        }

        // 获取 SK
        SecretKeyResolver resolver = ServiceProvider.of(SecretKeyResolver.class).getNewExtension("database");
        if (resolver == null) {
            log.error("SecretKeyResolver 未配置");
            return null;
        }

        String secretKey = resolver.getSecretKey(accessKey);
        if (StringUtils.isBlank(secretKey)) {
            return null;
        }

        try {
            String enc = StringUtils.defaultString(encryption, DEFAULT_ENCRYPTION);
            return Codec.build(enc, secretKey).encodeHex(data);
        } catch (Exception e) {
            log.error("AK/SK加密失败: ak={}", maskKey(accessKey), e);
            return null;
        }
    }

    /**
     * 脱敏密钥
     */
    private static String maskKey(String key) {
        if (key == null || key.length() < 8) {
            return "***";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
}
