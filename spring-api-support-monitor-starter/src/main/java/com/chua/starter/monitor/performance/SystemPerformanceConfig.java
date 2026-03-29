package com.chua.starter.monitor.performance;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统性能监控配置。
 *
 * @author CH
 * @since 2026/3/28
 */
@Data
public class SystemPerformanceConfig {

    private Boolean enabled;
    private Boolean socketPushEnabled;
    private Integer refreshIntervalSeconds;
    private String websocketTopic;
    private List<SystemPerformanceMetricToggle> indicators = new ArrayList<>();
}
