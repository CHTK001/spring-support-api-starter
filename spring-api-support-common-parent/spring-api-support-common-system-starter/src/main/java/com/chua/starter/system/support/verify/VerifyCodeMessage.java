package com.chua.starter.system.support.verify;

/**
 * 验证码下发载荷
 *
 * @param target        投递目标（邮箱地址、手机号等）
 * @param bizType       业务场景，决定 Redis 键的场景段与文案
 * @param code          验证码明文
 * @param expireMinutes 有效分钟数，同时用于文案提示
 * @author CH
 * @since 4.0.0.50
 */
public record VerifyCodeMessage(String target, String bizType, String code, int expireMinutes) {
}
