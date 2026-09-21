package com.chua.starter.signin.dto;

/**
 * 签到积分明细 DTO
 * <p>一次签到中每条积分规则的发放明细，用于前端展示积分构成。</p>
 *
 * @param ruleType 规则类型：DAILY_标志 / STREAK_奖金_3 / STREAK_奖金_7 /
 *                 STREAK_奖金_30
 * @param points 本次积分值
 * @param description 规则描述文字
 *
 * @since 4.0.0.42
 * @author CH
 */
public record SignInPointDTO(
        String ruleType,
        Integer points,
        String description
) {
}