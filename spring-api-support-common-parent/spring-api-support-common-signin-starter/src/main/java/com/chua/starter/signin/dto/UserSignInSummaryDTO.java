package com.chua.starter.signin.dto;

import lombok.Data;

/**
 * 用户签到汇总 DTO（管理员端）
 *
 * <p>按用户聚合签到数据，包含签到天数和总积分，用于管理员查看所有用户的签到概况。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Data
public class UserSignInSummaryDTO {

    /**
     * 用户标识
     */
    private Long userId;

    /**
     * 签到天数
     */
    private Integer signInDays;

    /**
     * 累计总积分
     */
    private Integer totalPoints;
}