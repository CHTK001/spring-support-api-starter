package com.chua.starter.oshi.performance;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统性能快照。
 *
 * @author CH
 * @since 2026/3/28
 */
@Data
public class SystemPerformanceSnapshot {

    private Boolean enabled;
    private Boolean socketPushEnabled;
    private Integer refreshIntervalSeconds;
    private String websocketTopic;
    private Long timestamp;
    private String healthStatus;
    private String message;
    private List<String> activeIndicators = new ArrayList<>();
    private Map<String, Object> sections = new LinkedHashMap<>();
}
