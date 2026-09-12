package com.chua.starter.signin.dto;

import lombok.Data;

/**
* 签到统计 DTO
*
* <p>用户签到统计概览，包含总签到天数、当前连续天数、历史最高连续天数和总积分。</p>
*
* @since 4.0.0.42
* @author CH
 */
@Data
public class SignInStatsDTO {

    /**
    * 用户标识
     */
    private Long userId;

    /**
    * 总签到天数
     */
    private Integer totalDays;

    /**
    * 当前连续签到天数
     */
    private Integer currentStreak;

    /**
    * 历史最高连续签到天数
     */
    private Integer maxStreak;

    /**
    * 累计总积分
     */
    private Integer totalPoints;

    /**
    * 今日是否已签到
     */
    private boolean signedToday;
}