package com.chua.starter.signin.dto;

/**
 * 签到统计 DTO
 * <p>用户签到统计概览，包含总签到天数、当前连续天数、历史最高连续天数和总积分。</p>
 *
 * @param userId 用户标识
 * @param totalDays 总签到天数
 * @param currentStreak 当前连续签到天数
 * @param maxStreak 历史最高连续签到天数
 * @param totalPoints 累计总积分
 * @param signedToday 今日是否已签到
 *
 * @since 4.0.0.42
 * @author CH
 */
public record SignInStatsDTO(
        Long userId,
        Integer totalDays,
        Integer currentStreak,
        Integer maxStreak,
        Integer totalPoints,
        boolean signedToday
) {
}