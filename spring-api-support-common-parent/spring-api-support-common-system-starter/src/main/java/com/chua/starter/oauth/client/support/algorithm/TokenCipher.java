package com.chua.starter.oauth.client.support.algorithm;

import com.chua.common.support.utils.DigestUtils;
import com.chua.starter.oauth.client.support.user.UserResult;

/**
 * Token 加密算法
 *
 * <p>提供 UserResult -> token 字符串的转换能力。
 * 作为 SPI 占位实现，运行期可被外部 Starter 覆盖。</p>
 *
 * @author CH
 * @since 2026/8/3
 */
public class TokenCipher {

    /**
     * 将 UserResult 加密为 token 字符串
     *
     * @param userResult 用户结果
     * @return 加密后的 token
     */
    public static String encrypt(UserResult userResult) {
        if (userResult == null) {
            return "";
        }
        return DigestUtils.sha1(String.valueOf(userResult));
    }
}
