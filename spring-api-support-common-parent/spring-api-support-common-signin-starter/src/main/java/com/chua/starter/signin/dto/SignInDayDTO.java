package com.chua.starter.signin.dto;

import java.time.LocalDate;

/**
 * 签到日历项 DTO
 * <p>月度日历中每一天的签到状态，用于前端渲染日历网格。</p>
 *
 * @param date 日期
 * @param signed 是否已签到
 * @param points 当天签到获得的积分（仅已签到时有
 *               值）
 *
 * @since 4.0.0.42
 * @author CH
 */
public record SignInDayDTO(
        LocalDate date,
        boolean signed,
        Integer points
) {
}