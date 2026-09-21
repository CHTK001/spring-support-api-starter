package com.chua.starter.achievement.dto;

import java.util.Map;

/**
 * 上报统计值请求 DTO
 * <p>前端将自身掌握的行为数据（如菜单点击次数、消息发送次数等）
 * 批量上报到成就模块，写入 kvengine 后触发成就判定。</p>
 *
 * @param userId 用户标识
 * @param stats 统计值映射，键 为统计字段名（如 menuvisited数量），值 为数值
 *
 * @since 4.0.0.42
 * @author CH
 */
public record ReportStatsDTO(
        Long userId,
        Map<String, Object> stats
) {
}