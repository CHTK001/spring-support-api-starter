package com.chua.starter.datalake.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 监控统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "监控统计")
public class MonitorStats {

    @Schema(description = "活跃 Pipeline 数")
    private int activePipelines;

    @Schema(description = "今日处理量")
    private long todayProcessed;

    @Schema(description = "今日错误量")
    private long todayErrors;

    @Schema(description = "总处理量")
    private long totalProcessed;

    @Schema(description = "死信队列大小")
    private int dlqSize;

    @Schema(description = "活跃订阅数")
    private int activeSubscriptions;

    @Schema(description = "当前吞吐量 (条/秒)")
    private double throughput;

    @Schema(description = "平均延迟 (ms)")
    private double avgLatency;
}
