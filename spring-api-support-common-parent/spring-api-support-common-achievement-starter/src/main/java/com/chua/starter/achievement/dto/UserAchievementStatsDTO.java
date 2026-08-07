package com.chua.starter.achievement.dto;

import lombok.Data;

/**
 * 用户成就统计概览 DTO
 *
 * @author CH
 * @since 2026-08-07
 */
@Data
public class UserAchievementStatsDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 已解锁成就总数
     */
    private Integer totalUnlocked;

    /**
     * 成就总数
     */
    private Integer totalAchievements;

    /**
     * 各分组已解锁等级映射
     */
    private java.util.Map<String, Integer> unlockedByType;
}
