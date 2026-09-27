package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.verify.EmailConnectivityVO;
import com.chua.starter.system.pojo.verify.VerifyCodeSentVO;

/**
 * 账号资料验证服务
 *
 * <p>把通用验证码能力落到具体账号上：投递目标只取当前登录用户自己档案里的字段，
 * 校验通过后回写该字段的已验证标记。换绑（改邮箱）时已验证状态由用户更新流程作废。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
public interface UserVerifyService {

    /**
     * 向当前用户档案中的目标下发验证码
     *
     * @param userId  当前登录用户标识
     * @param channel 渠道标识（EMAIL/SMS）
     * @return 成功时返回有效期与脱敏目标
     */
    ReturnResult<VerifyCodeSentVO> sendCode(Long userId, String channel);

    /**
     * 校验验证码并回写已验证标记
     *
     * @param userId  当前登录用户标识
     * @param channel 渠道标识（EMAIL/SMS）
     * @param code    用户提交的验证码
     * @return 验证结果
     */
    ReturnResult<Boolean> verifyCode(Long userId, String channel, String code);

    /**
     * 探测服务器邮箱配置（SMTP 与 IMAP 各握手一次）
     *
     * @return 两条通道的探测结果
     */
    ReturnResult<EmailConnectivityVO> checkEmailConnectivity();
}
