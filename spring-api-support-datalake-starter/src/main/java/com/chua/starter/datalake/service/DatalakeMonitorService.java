package com.chua.starter.datalake.service;

import com.chua.starter.datalake.model.MonitorStats;

import java.util.*;

/**
 * 监控指标采集服务
 */
public class DatalakeMonitorService {

    private final PipelineService pipelineService;
    private final DatalakeDataService dataService;

    public DatalakeMonitorService(PipelineService pipelineService, DatalakeDataService dataService) {
        this.pipelineService = pipelineService;
        this.dataService = dataService;
    }

    /** 吞吐量时间序列 (模拟) */
    private final List<Map<String, Object>> throughputHistory = new ArrayList<>();
    /** 错误率时间序列 (模拟) */
    private final List<Map<String, Object>> errorHistory = new ArrayList<>();

    public MonitorStats stats() {
        int active = 0;
        for (var g : pipelineService.list()) {
            var lc = pipelineService.getLifecycle(g.getId());
            if (lc == com.chua.datalake.support.engine.PipelineLifecycle.RUNNING) {
                active++;
            }
        }

        return MonitorStats.builder()
                .activePipelines(active)
                .todayProcessed(12580)
                .todayErrors(23)
                .totalProcessed(1587920)
                .dlqSize(0)
                .activeSubscriptions(0)
                .throughput(156.8)
                .avgLatency(12.5)
                .build();
    }

    public List<Map<String, Object>> throughput() {
        if (throughputHistory.isEmpty()) {
            // 生成模拟数据
            long now = System.currentTimeMillis();
            for (int i = 23; i >= 0; i--) {
                Map<String, Object> point = new HashMap<>();
                point.put("time", new Date(now - i * 3600_000L));
                point.put("value", 100 + Math.random() * 200);
                throughputHistory.add(point);
            }
        }
        return throughputHistory;
    }

    public List<Map<String, Object>> errors() {
        if (errorHistory.isEmpty()) {
            long now = System.currentTimeMillis();
            for (int i = 23; i >= 0; i--) {
                Map<String, Object> point = new HashMap<>();
                point.put("time", new Date(now - i * 3600_000L));
                point.put("value", Math.random() * 5);
                errorHistory.add(point);
            }
        }
        return errorHistory;
    }
}
