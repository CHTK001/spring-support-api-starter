package com.chua.starter.signin.dto;

/**
 * 用户签到汇总 DTO（管理员端）
 *
 * <p>按用户聚合签到数据，包含签到天数和总积分，用于管理员查看所有用户的签到概况。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
public record UserSignInSummaryDTO(
        /**
         * 用户标识
         */
        Long userId,
        /**
         * 签到天数
         */
        Integer signInDays,
        /**
         * 累计总积分
         */
        Integer totalPoints
) {
}
