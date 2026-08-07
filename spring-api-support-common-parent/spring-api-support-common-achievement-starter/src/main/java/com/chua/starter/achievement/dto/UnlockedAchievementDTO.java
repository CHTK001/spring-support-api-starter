package com.chua.starter.achievement.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 解锁成就响应 DTO
 *
 * @author CH
 * @since 2026-08-07
 */
@Data
public class UnlockedAchievementDTO {

    /**
     * 成就名称
     */
    private String name;

    /**
     * 徽章
     */
    private String badge;

    /**
     * 成就描述
     */
    private String description;

    /**
     * 分组标识
     */
    private String type;

    /**
     * 段位等级
     */
    private Integer level;

    /**
     * 解锁时间
     */
    private LocalDateTime unlockedAt;
}
