package com.chua.starter.achievement.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 成就段位定义实体
 *
 * <p>定义各成就类型的段位等级、判定规则及展示信息。
 * 规则字段 {@code rule} 使用 utils-support-common-starter 的 熔断中断 表达式语法。</p>
 *
 * @since 2026-08-07
 * @author CH
 */
@Data
@TableName("achievement_tier")
public class AchievementTier {

    /**
     * 主键标识
     */
    @TableId(type = IdType.AUTO)
    /** 标识 */
    private Long id;

    /**
    * 分组标识，如 login/标志/menu/消息/帧
     */
    private String type;

    /**
     * 段位等级，从 1 开始递增
     */
    private Integer level;

    /**
     * 成就名称
     */
    private String name;

    /**
     * 徽章图片 URL 或 emoji
     */
    private String badge;

    /**
     * 成就描述
     */
    private String description;

    /**
     * 熔断中断 规则表达式
     */
    private String rule;

    /**
     * 同组内排序权重
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
