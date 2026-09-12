package com.chua.starter.signin.dto;

import lombok.Data;

import java.util.List;

/**
* 签到结果 DTO
*
* <p>签到操作成功后返回给前端的结果，包含签到状态、积分、连续天数等信息。
* 如果用户当天已签到，{@code alreadySigned} 为 true。</p>
*
* @since 4.0.0.42
* @author CH
 */
@Data
public class SignInResultDTO {

    /**
    * 签到是否成功
     */
    private boolean success;

    /**
    * 是否已经签到过（今日已签到）
     */
    private boolean alreadySigned;

    /**
    * 提示消息
     */
    private String msg;

    /**
    * 本次签到获得的积分
     */
    private Integer todayPoints;

    /**
    * 连续签到天数
     */
    private Integer streak;

    /**
    * 累计总积分
     */
    private Integer totalPoints;

    /**
    * 累计签到天数
     */
    private Integer totalDays;

    /**
    * 积分明细列表
     */
    private List<SignInPointDTO> pointsBreakdown;
}