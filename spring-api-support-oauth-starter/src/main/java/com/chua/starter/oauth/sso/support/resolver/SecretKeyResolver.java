package com.chua.starter.oauth.sso.support.resolver;

/**
 * SK密钥解析器接口
 * <p>
 * 根据 AccessKey 查询 SecretKey，用于 AK/SK 加密验证
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/17
 */
public interface SecretKeyResolver {

    /**
     * 根据 AccessKey 查询 SecretKey
     *
     * @param accessKey AccessKey
     * @return SecretKey，不存在返回 null
     */
    String getSecretKey(String accessKey);

    /**
     * 验证 AK/SK 是否有效
     *
     * @param accessKey  AccessKey
     * @param secretKey  SecretKey
     * @return 是否有效
     */
    default boolean verify(String accessKey, String secretKey) {
        String sk = getSecretKey(accessKey);
        return sk != null && sk.equals(secretKey);
    }
}
