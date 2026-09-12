package com.chua.starter.achievement.dto;

import lombok.Data;

/**
 * 用户成就统计概览 DTO
 *
 * @since 2026-08-07
 * @author CH
 */
@Data
public class UserAchievementStatsDTO {

    /**
      * 用户标识
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
