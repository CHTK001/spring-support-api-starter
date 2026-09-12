package com.chua.starter.oauth.sso.support.generation;

/**
* 令牌生成器
*
* @author CH
* @since 4.0.0
 */
public interface TokenGeneration {
    /**
    * 生成令牌
    *
    * @param key 键
    * @return 令牌(128位)
     */
    String generation(String key);
}
