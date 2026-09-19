package com.chua.starter.system.util;

import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.StringUtils;

/**
 * 口令摘要算法（全局唯一定义）
 *
 * <p>写入与比对 {@code sys_user_password} 统一为 {@code md5Hex(客户端口令 + 盐)}。
 * 客户端口令指浏览器提交的原始字符串，登录与改密必须提交同一种表示，否则会出现
 * 已改密的账号无法登录。所有读写该字段的代码都必须经由本类，禁止各自拼接。
 *
 * @since 2026-09-18
 * @author CH
 */
public class PasswordUtils {

    /**
     * 工具类不可实例化
     */
    private PasswordUtils() {
    }

    /**
     * 计算口令摘要
     *
     * @param credential 客户端提交的口令
     * @param salt       用户盐值
     * @return 口令摘要
     */
    public static String encrypt(String credential, String salt) {
        return DigestUtils.md5Hex(credential + salt);
    }

    /**
     * 校验口令是否匹配已存摘要
     *
     * @param credential 客户端提交的口令
     * @param salt       用户盐值
     * @param stored     库中已存口令摘要
     * @return 匹配返回 true；口令为空或摘要为空返回 false
     */
    public static boolean matches(String credential, String salt, String stored) {
        if (StringUtils.isEmpty(credential) || StringUtils.isEmpty(stored)) {
            return false;
        }
        return StringUtils.safeEquals(encrypt(credential, salt), stored);
    }
}
