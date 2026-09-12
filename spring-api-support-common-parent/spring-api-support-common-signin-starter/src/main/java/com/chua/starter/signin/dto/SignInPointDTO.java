package com.chua.starter.signin.dto;

import lombok.Data;

/**
 * 签到积分明细 DTO
 *
 * <p>一次签到中每条积分规则的发放明细，用于前端展示积分构成。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Data
public class SignInPointDTO {

    /**
      * 规则类型：DAILY_标志 / STREAK_奖金_3 / STREAK_奖金_7 / STREAK_奖金_30
     */
    private String ruleType;

    /**
     * 本次积分值
     */
    private Integer points;

    /**
     * 规则描述文字
     */
    private String description;

    /**
     * 无参构造
     */
    public SignInPointDTO() {
    }

    /**
     * 全参构造
     *
     * @param ruleType    规则类型
     * @param points      积分值
     * @param description 规则描述
     */
    public SignInPointDTO(String ruleType, Integer points, String description) {
        this.ruleType = ruleType;
        this.points = points;
        this.description = description;
    }
}