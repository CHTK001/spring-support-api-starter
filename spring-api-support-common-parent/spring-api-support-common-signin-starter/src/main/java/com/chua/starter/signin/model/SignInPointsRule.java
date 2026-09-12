package com.chua.starter.signin.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签到积分规则实体
 *
 * <p>定义签到积分的发放规则，包括每日签到基础积分和连续签到里程碑奖励。
   * 内置规则类型：DAILY_标志（每日基础）、STREAK_奖金_3/7/30（连续天数里程碑）。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Data
@TableName("sign_in_points_rule")
public class SignInPointsRule {

    /**
      * 主键标识
     */
    @TableId(type = IdType.AUTO)
    /** 标识 */
    private Long id;

    /**
    * 规则类型标识：DAILY_标志 / STREAK_奖金_3 / STREAK_奖金_7 / STREAK_奖金_30
     */
    private String type;

    /**
     * 奖励积分数
     */
    private Integer points;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 状态：1 启用，0 禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}