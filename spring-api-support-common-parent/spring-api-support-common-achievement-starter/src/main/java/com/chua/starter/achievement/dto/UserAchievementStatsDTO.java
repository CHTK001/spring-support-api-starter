package com.chua.starter.achievement.dto;

import java.util.Map;

/**
 * 用户成就统计概览 DTO
 *
 * @since 2026-08-07
 * @author CH
 */
public record UserAchievementStatsDTO(
        /**
         * 用户标识
         */
        Long userId,
        /**
         * 已解锁成就总数
         */
        Integer totalUnlocked,
        /**
         * 成就总数
         */
        Integer totalAchievements,
        /**
         * 各分组已解锁等级映射
         */
        Map<String, Integer> unlockedByType
) {
}
