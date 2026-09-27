package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.verify.VerifyCodeSentVO;

/**
 * 通用验证码服务
 *
 * <p>与具体投递渠道无关：验证码写在 Redis（键含渠道、业务场景与账号），校验只做比对与消费，
 * 过期由 Redis TTL 负责。邮箱、手机号等渠道各自实现 {@code VerifyCodeChannel} 即可复用本服务。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
public interface VerifyCodeService {

    /**
     * 渠道是否可用（服务器已配置且渠道已安装）
     *
     * @param channel 渠道标识，如 EMAIL
     * @return true 表示可下发
     */
    boolean configured(String channel);

    /**
     * 向目标下发验证码
     *
     * @param channel 渠道标识
     * @param bizType 业务场景，决定验证码键的场景段
     * @param target  投递目标（邮箱/手机号）
     * @return 成功时返回有效期与脱敏目标
     */
    ReturnResult<VerifyCodeSentVO> send(String channel, String bizType, String target);

    /**
     * 校验并消费验证码
     *
     * @param channel 渠道标识
     * @param bizType 业务场景
     * @param target  投递目标
     * @param code    用户提交的验证码
     * @return true 表示验证码正确且已被消费
     */
    boolean verify(String channel, String bizType, String target, String code);
}
