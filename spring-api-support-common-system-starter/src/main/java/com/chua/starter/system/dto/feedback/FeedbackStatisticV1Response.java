package com.chua.starter.system.dto.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 统计反馈信息响应
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0
 */
@Data
@Schema(description = "反馈统计响应")
public class FeedbackStatisticV1Response {

    /**
     * 反馈类型统计（用于饼图）
     */
    @Schema(description = "反馈类型统计")
    private List<TypeStatistic> typeStatistics;

    /**
     * 反馈趋势统计（用于曲线图）
     */
    @Schema(description = "反馈趋势统计")
    private List<TrendStatistic> trendStatistics;

    /**
     * 总反馈数
     */
    @Schema(description = "总反馈数")
    private Long total;

    /**
     * 待处理数
     */
    @Schema(description = "待处理数")
    private Long pending;

    /**
     * 已处理数
     */
    @Schema(description = "已处理数")
    private Long resolved;

    /**
     * 类型统计
     */
    @Data
    @Schema(description = "类型统计")
    public static class TypeStatistic {
        /**
         * 类型名称
         */
        @Schema(description = "类型名称")
        private String name;

        /**
         * 类型编码
         */
        @Schema(description = "类型编码")
        private String type;

        /**
         * 数量
         */
        @Schema(description = "数量")
        private Long value;
    }

    /**
     * 趋势统计
     */
    @Data
    @Schema(description = "趋势统计")
    public static class TrendStatistic {
        /**
         * 日期
         */
        @Schema(description = "日期")
        private String date;

        /**
         * 数量
         */
        @Schema(description = "数量")
        private Long count;
    }
}
