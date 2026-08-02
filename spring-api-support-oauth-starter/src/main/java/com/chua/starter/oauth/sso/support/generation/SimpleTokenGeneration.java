package com.chua.starter.oauth.sso.support.generation;

import com.chua.common.support.spi.annotations.Extension;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 令牌生成器
 *
 * @author CH
 */
@Extension("simple")
public class SimpleTokenGeneration implements TokenGeneration {
    @Override
    public String generation(String key) {
        if (key == null) {
            key = java.util.UUID.randomUUID().toString();
        }
        return DigestUtils.sha512Hex(DigestUtils.md5Hex(key));
    }
}
