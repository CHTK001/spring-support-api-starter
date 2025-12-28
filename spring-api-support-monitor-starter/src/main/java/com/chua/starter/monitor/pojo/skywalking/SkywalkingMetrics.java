package com.chua.starter.monitor.pojo.skywalking;

import lombok.Data;

import java.util.List;

/**
 * SkyWalking 指标数据
 *
 * @author CH
 * @since 2024-12-28
 */
@Data
public class SkywalkingMetrics {

    /**
     * 服务指标概览
     */
    @Data
    public static class ServiceOverview {
        /**
         * 每分钟调用次数
         */
        private Long cpm;
        /**
         * SLA百分比 (x100)
         */
        private Long sla;
        /**
         * 平均响应时间 ms
         */
        private Long respTime;
        /**
         * Apdex 分数 (x10000)
         */
        private Long apdex;
        /**
         * 百分位数: p50, p75, p90, p95, p99
         */
        private List<Long> percentile;
    }

    /**
     * 服务指标趋势
     */
    @Data
    public static class ServiceTrend {
        /**
         * 时间戳列表
         */
        private List<String> timestamps;
        /**
         * CPM 趋势
         */
        private List<Long> cpm;
        /**
         * 响应时间趋势
         */
        private List<Long> respTime;
        /**
         * SLA 趋势
         */
        private List<Long> sla;
        /**
         * Apdex 趋势
         */
        private List<Long> apdex;
    }

    /**
     * 全局指标概览
     */
    @Data
    public static class GlobalOverview {
        /**
         * 服务总数
         */
        private Integer totalServices;
        /**
         * 端点总数
         */
        private Integer totalEndpoints;
        /**
         * 总 CPM
         */
        private Long totalCpm;
        /**
         * 平均响应时间
         */
        private Long avgRespTime;
        /**
         * 平均 SLA
         */
        private Long avgSla;
    }

    /**
     * 排名项
     */
    @Data
    public static class RankItem {
        /**
         * ID
         */
        private String id;
        /**
         * 名称
         */
        private String name;
        /**
         * 值
         */
        private Long value;
    }
}
