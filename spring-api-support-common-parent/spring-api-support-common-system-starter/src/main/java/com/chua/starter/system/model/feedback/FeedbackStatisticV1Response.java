package com.chua.starter.system.model.feedback;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 统计反馈信息响应
 *
 * @param typeStatistics  反馈类型统计（用于饼图）
 * @param trendStatistics 反馈趋势统计（用于曲线图）
 * @param total           总反馈数
 * @param pending         待处理数
 * @param resolved        已处理数
 * @param todayCount      今日新增数
 * @since 2024/12/10
 * @author CH
 */
@Schema(description = "反馈统计响应")
public record FeedbackStatisticV1Response(
        @Schema(description = "反馈类型统计")
        List<TypeStatistic> typeStatistics,
        @Schema(description = "反馈趋势统计")
        List<TrendStatistic> trendStatistics,
        @Schema(description = "总反馈数")
        Long total,
        @Schema(description = "待处理数")
        Long pending,
        @Schema(description = "已处理数")
        Long resolved,
        @Schema(description = "今日新增数")
        Long todayCount) {

    /**
     * 类型统计
     *
     * @param name  类型名称
     * @param type  类型编码
     * @param value 数量
     * @author CH
     * @since 4.0.0
     */
    @Schema(description = "类型统计")
    public record TypeStatistic(
            @Schema(description = "类型名称")
            String name,
            @Schema(description = "类型编码")
            String type,
            @Schema(description = "数量")
            Long value) {
    }

    /**
     * 趋势统计
     *
     * @param date          日期
     * @param count         数量
     * @param pendingCount  未解决数量
     * @param resolvedCount 已解决数量
     * @author CH
     * @since 4.0.0
     */
    @Schema(description = "趋势统计")
    public record TrendStatistic(
            @Schema(description = "日期")
            String date,
            @Schema(description = "数量")
            Long count,
            @Schema(description = "未解决数量")
            Long pendingCount,
            @Schema(description = "已解决数量")
            Long resolvedCount) {
    }
}
