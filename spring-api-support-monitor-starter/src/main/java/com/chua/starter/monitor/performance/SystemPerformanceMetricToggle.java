package com.chua.starter.monitor.performance;

import lombok.Data;

/**
 * 单项性能指标开关描述。
 *
 * @author CH
 * @since 2026/3/28
 */
@Data
public class SystemPerformanceMetricToggle {

    private String key;
    private String label;
    private String description;
    private String settingName;
    private Integer sort;
    private Boolean enabled;
}
