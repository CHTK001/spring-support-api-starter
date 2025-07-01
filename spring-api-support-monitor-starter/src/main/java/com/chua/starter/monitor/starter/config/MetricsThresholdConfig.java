package com.chua.starter.monitor.starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 指标阈值配置
 * 用于定义CPU、内存、磁盘等指标的颜色阈值
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Component
@ConfigurationProperties(prefix = "monitor.metrics.threshold")
public class MetricsThresholdConfig {

    /**
     * CPU使用率阈值配置
     */
    private ThresholdLevel cpu = new ThresholdLevel(50.0, 80.0, 90.0);

    /**
     * 内存使用率阈值配置
     */
    private ThresholdLevel memory = new ThresholdLevel(60.0, 80.0, 90.0);

    /**
     * 磁盘使用率阈值配置
     */
    private ThresholdLevel disk = new ThresholdLevel(70.0, 85.0, 95.0);

    /**
     * 温度阈值配置
     */
    private ThresholdLevel temperature = new ThresholdLevel(50.0, 70.0, 85.0);

    /**
     * 网络使用率阈值配置（基于带宽百分比）
     */
    private ThresholdLevel network = new ThresholdLevel(60.0, 80.0, 90.0);

    /**
     * 阈值级别定义
     */
    @Data
    public static class ThresholdLevel {
        /**
         * 正常阈值（绿色）- 低于此值显示绿色
         */
        private Double normal;

        /**
         * 警告阈值（黄色）- 介于normal和critical之间显示黄色
         */
        private Double warning;

        /**
         * 危险阈值（红色）- 高于此值显示红色
         */
        private Double critical;

        public ThresholdLevel() {
        }

        public ThresholdLevel(Double normal, Double warning, Double critical) {
            this.normal = normal;
            this.warning = warning;
            this.critical = critical;
        }

        /**
         * 根据值获取颜色级别
         *
         * @param value 当前值
         * @return 颜色级别：normal, warning, critical
         */
        public String getColorLevel(Double value) {
            if (value == null) {
                return "normal";
            }
            if (value >= critical) {
                return "critical";
            } else if (value >= warning) {
                return "warning";
            } else {
                return "normal";
            }
        }

        /**
         * 根据值获取颜色代码
         *
         * @param value 当前值
         * @return 颜色代码
         */
        public String getColorCode(Double value) {
            String level = getColorLevel(value);
            switch (level) {
                case "critical":
                    return "#f56c6c"; // 红色
                case "warning":
                    return "#e6a23c"; // 黄色
                case "normal":
                default:
                    return "#67c23a"; // 绿色
            }
        }

        /**
         * 根据值获取Element Plus进度条颜色配置
         *
         * @param value 当前值
         * @return 进度条颜色配置
         */
        public Map<String, Object> getProgressColor(Double value) {
            Map<String, Object> colorConfig = new HashMap<>();
            
            // Element Plus 进度条支持多段颜色配置
            colorConfig.put("0%", "#67c23a");    // 绿色
            colorConfig.put(normal + "%", "#67c23a");    // 绿色
            colorConfig.put(warning + "%", "#e6a23c");   // 黄色
            colorConfig.put(critical + "%", "#f56c6c");  // 红色
            colorConfig.put("100%", "#f56c6c");  // 红色
            
            return colorConfig;
        }
    }

    /**
     * 获取所有阈值配置的映射
     *
     * @return 阈值配置映射
     */
    public Map<String, ThresholdLevel> getAllThresholds() {
        Map<String, ThresholdLevel> thresholds = new HashMap<>();
        thresholds.put("cpu", cpu);
        thresholds.put("memory", memory);
        thresholds.put("disk", disk);
        thresholds.put("temperature", temperature);
        thresholds.put("network", network);
        return thresholds;
    }

    /**
     * 根据指标类型获取阈值配置
     *
     * @param metricType 指标类型
     * @return 阈值配置
     */
    public ThresholdLevel getThreshold(String metricType) {
        switch (metricType.toLowerCase()) {
            case "cpu":
                return cpu;
            case "memory":
                return memory;
            case "disk":
                return disk;
            case "temperature":
                return temperature;
            case "network":
                return network;
            default:
                return new ThresholdLevel(50.0, 80.0, 90.0); // 默认阈值
        }
    }

    /**
     * 获取指标的颜色代码
     *
     * @param metricType 指标类型
     * @param value      当前值
     * @return 颜色代码
     */
    public String getMetricColor(String metricType, Double value) {
        return getThreshold(metricType).getColorCode(value);
    }

    /**
     * 获取指标的颜色级别
     *
     * @param metricType 指标类型
     * @param value      当前值
     * @return 颜色级别
     */
    public String getMetricColorLevel(String metricType, Double value) {
        return getThreshold(metricType).getColorLevel(value);
    }

    /**
     * 检查指标是否超过警告阈值
     *
     * @param metricType 指标类型
     * @param value      当前值
     * @return 是否超过警告阈值
     */
    public boolean isWarningLevel(String metricType, Double value) {
        if (value == null) return false;
        ThresholdLevel threshold = getThreshold(metricType);
        return value >= threshold.getWarning();
    }

    /**
     * 检查指标是否超过危险阈值
     *
     * @param metricType 指标类型
     * @param value      当前值
     * @return 是否超过危险阈值
     */
    public boolean isCriticalLevel(String metricType, Double value) {
        if (value == null) return false;
        ThresholdLevel threshold = getThreshold(metricType);
        return value >= threshold.getCritical();
    }
}
