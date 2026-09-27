package com.chua.starter.achievement.dto;

import java.time.LocalDateTime;

/**
 * 解锁成就响应 DTO
 *
 * @param id 段位定义标识（对应 achievement_tier.标识）
 * @param name 成就名称
 * @param badge 徽章
 * @param description 成就描述
 * @param type 分组标识
 * @param level 段位等级
 * @param unlockedAt 解锁时间
 *
 * @since 2026-08-07
 * @author CH
 */
public record UnlockedAchievementDTO(
        Long id,
        String name,
        String badge,
        String description,
        String type,
        Integer level,
        LocalDateTime unlockedAt
) {
}
