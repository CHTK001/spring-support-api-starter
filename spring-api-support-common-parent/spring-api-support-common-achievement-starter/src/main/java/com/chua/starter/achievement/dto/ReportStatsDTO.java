package com.chua.starter.achievement.dto;

import lombok.Data;

import java.util.Map;

/**
 * 上报统计值请求 DTO
 *
 * <p>前端将自身掌握的行为数据（如菜单点击次数、消息发送次数等）
 * 批量上报到成就模块，写入 KvEngine 后触发成就判定。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
public class ReportStatsDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 统计值映射，key 为统计字段名（如 menuVisitedCount），value 为数值
     */
    private Map<String, Object> stats;
}