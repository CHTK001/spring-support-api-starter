package com.chua.starter.oauth.sso.support.generation;

import com.chua.common.support.spi.annotations.Spi;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 令牌生成器
 *
 * @author CH
 * @since 4.0.0
 */
@Spi("simple")
public class SimpleTokenGeneration implements TokenGeneration {
    @Override
    /**
     * Generation
    */
    public String generation(String key) {
        if (key == null) {
            key = java.util.UUID.randomUUID().toString();
        }
        return DigestUtils.sha512Hex(DigestUtils.md5Hex(key));
    }
}
