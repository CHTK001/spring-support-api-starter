package com.chua.starter.signin.dto;

import lombok.Data;

import java.time.LocalDate;

/**
* 签到日历项 DTO
*
* <p>月度日历中每一天的签到状态，用于前端渲染日历网格。</p>
*
* @since 4.0.0.42
* @author CH
 */
@Data
public class SignInDayDTO {

    /**
    * 日期
     */
    private LocalDate date;

    /**
    * 是否已签到
     */
    private boolean signed;

    /**
    * 当天签到获得的积分（仅已签到时有值）
     */
    private Integer points;
}