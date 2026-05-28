package com.chua.starter.datalake.model;

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
public class MonitorStats {

    private int activePipelines;
    private long todayProcessed;
    private long todayErrors;
    private long totalProcessed;
    private int dlqSize;
    private int activeSubscriptions;
    private double throughput;
    private double avgLatency;
}
