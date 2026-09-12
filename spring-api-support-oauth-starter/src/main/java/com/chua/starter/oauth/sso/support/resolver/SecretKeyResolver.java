package com.chua.starter.oauth.sso.support.resolver;

/**
 * SK密钥解析器接口
 * <p>
   * 根据 访问密钥 查询 密钥，用于 AK/SK 加密验证
 * </p>
 *
   * @版本 1.0.0
 * @since 2024/12/17
 * @author CH
 */
public interface SecretKeyResolver {

    /**
      * 根据 访问密钥 查询 密钥
     *
     * @param accessKey 访问密钥
     * @return SecretKey，不存在返回 空
     */
    String getSecretKey(String accessKey);

    /**
     * 验证 AK/SK 是否有效
     *
     * @param accessKey  访问密钥
     * @param secretKey  密钥
     * @return 是否有效
     */
    default boolean verify(String accessKey, String secretKey) {
        String sk = getSecretKey(accessKey);
        return sk != null && sk.equals(secretKey);
    }
}
