package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.config.MetricsThresholdConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务器指标配置控制器
 * 提供前端所需的配置信息
 *
 * @author CH
 * @since 2024/12/30
 */
@RestController
@RequestMapping("v1/server/metrics/config")
@Tag(name = "服务器指标配置")
@RequiredArgsConstructor
public class ServerMetricsConfigController {

    private final MetricsThresholdConfig thresholdConfig;

    /**
     * 获取前端配置信息
     *
     * @return 前端配置
     */
    @GetMapping("frontend")
    @Operation(summary = "获取前端配置信息")
    public ReturnResult<Map<String, Object>> getFrontendConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            
            // 阈值配置
            config.put("thresholds", thresholdConfig.getAllThresholds());
            
            // 颜色配置
            Map<String, String> colors = new HashMap<>();
            colors.put("normal", "#67c23a");
            colors.put("warning", "#e6a23c");
            colors.put("critical", "#f56c6c");
            colors.put("info", "#409eff");
            colors.put("offline", "#909399");
            config.put("colors", colors);
            
            // 进度条颜色配置
            Map<String, Object> progressColors = new HashMap<>();
            for (Map.Entry<String, MetricsThresholdConfig.ThresholdLevel> entry : 
                 thresholdConfig.getAllThresholds().entrySet()) {
                String metricType = entry.getKey();
                MetricsThresholdConfig.ThresholdLevel threshold = entry.getValue();
                progressColors.put(metricType, threshold.getProgressColor(0.0));
            }
            config.put("progressColors", progressColors);
            
            // 格式化配置
            Map<String, Object> format = new HashMap<>();
            
            // 数字格式化
            Map<String, Object> numberFormat = new HashMap<>();
            numberFormat.put("decimalPlaces", 2);
            numberFormat.put("thousandsSeparator", ",");
            format.put("number", numberFormat);
            
            // 字节格式化
            Map<String, Object> bytesFormat = new HashMap<>();
            bytesFormat.put("units", new String[]{"B", "KB", "MB", "GB", "TB", "PB"});
            bytesFormat.put("base", 1024);
            format.put("bytes", bytesFormat);
            
            // 网络速率格式化
            Map<String, Object> networkSpeedFormat = new HashMap<>();
            networkSpeedFormat.put("units", new String[]{"B/s", "KB/s", "MB/s", "GB/s"});
            networkSpeedFormat.put("packetUnits", new String[]{"pps", "Kpps", "Mpps"});
            format.put("networkSpeed", networkSpeedFormat);
            
            config.put("format", format);
            
            // 更新频率配置
            Map<String, Integer> updateFrequency = new HashMap<>();
            updateFrequency.put("realtime", 30);
            updateFrequency.put("statusSummary", 60);
            updateFrequency.put("performanceTrends", 300);
            updateFrequency.put("connectionStats", 30);
            updateFrequency.put("healthStatus", 300);
            config.put("updateFrequency", updateFrequency);
            
            return ReturnResult.ok(config);
        } catch (Exception e) {
            return ReturnResult.error("获取前端配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标显示配置
     *
     * @return 指标显示配置
     */
    @GetMapping("metrics-display")
    @Operation(summary = "获取指标显示配置")
    public ReturnResult<Map<String, Object>> getMetricsDisplayConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            
            // CPU指标显示配置
            Map<String, Object> cpuConfig = new HashMap<>();
            cpuConfig.put("title", "CPU使用率");
            cpuConfig.put("icon", "ri:cpu-line");
            cpuConfig.put("unit", "%");
            cpuConfig.put("threshold", thresholdConfig.getCpu());
            cpuConfig.put("details", new String[]{"核心数", "负载"});
            config.put("cpu", cpuConfig);
            
            // 内存指标显示配置
            Map<String, Object> memoryConfig = new HashMap<>();
            memoryConfig.put("title", "内存使用率");
            memoryConfig.put("icon", "ri:database-line");
            memoryConfig.put("unit", "%");
            memoryConfig.put("threshold", thresholdConfig.getMemory());
            memoryConfig.put("details", new String[]{"已用", "总计"});
            config.put("memory", memoryConfig);
            
            // 磁盘指标显示配置
            Map<String, Object> diskConfig = new HashMap<>();
            diskConfig.put("title", "磁盘使用率");
            diskConfig.put("icon", "ri:hard-drive-line");
            diskConfig.put("unit", "%");
            diskConfig.put("threshold", thresholdConfig.getDisk());
            diskConfig.put("details", new String[]{"已用", "总计"});
            config.put("disk", diskConfig);
            
            // 网络指标显示配置
            Map<String, Object> networkConfig = new HashMap<>();
            networkConfig.put("title", "网络流量");
            networkConfig.put("icon", "ri:wifi-line");
            networkConfig.put("unit", "B/s");
            networkConfig.put("threshold", thresholdConfig.getNetwork());
            networkConfig.put("details", new String[]{"入包", "出包"});
            config.put("network", networkConfig);
            
            // 系统信息显示配置
            Map<String, Object> systemConfig = new HashMap<>();
            systemConfig.put("title", "系统信息");
            systemConfig.put("icon", "ri:information-line");
            systemConfig.put("fields", new String[]{
                "操作系统", "系统版本", "主机名", "运行时间", "负载平均", "进程数"
            });
            config.put("system", systemConfig);
            
            // 温度指标显示配置
            Map<String, Object> temperatureConfig = new HashMap<>();
            temperatureConfig.put("title", "温度");
            temperatureConfig.put("icon", "ri:temp-hot-line");
            temperatureConfig.put("unit", "°C");
            temperatureConfig.put("threshold", thresholdConfig.getTemperature());
            config.put("temperature", temperatureConfig);
            
            return ReturnResult.ok(config);
        } catch (Exception e) {
            return ReturnResult.error("获取指标显示配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取图表配置
     *
     * @return 图表配置
     */
    @GetMapping("charts")
    @Operation(summary = "获取图表配置")
    public ReturnResult<Map<String, Object>> getChartsConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            
            // 默认图表配置
            Map<String, Object> defaultChart = new HashMap<>();
            defaultChart.put("height", 200);
            defaultChart.put("animation", true);
            defaultChart.put("grid", Map.of(
                "top", 20,
                "right", 20,
                "bottom", 40,
                "left", 60
            ));
            config.put("default", defaultChart);
            
            // CPU图表配置
            Map<String, Object> cpuChart = new HashMap<>();
            cpuChart.put("yAxis", Map.of(
                "min", 0,
                "max", 100,
                "unit", "%"
            ));
            cpuChart.put("color", "#409eff");
            config.put("cpu", cpuChart);
            
            // 内存图表配置
            Map<String, Object> memoryChart = new HashMap<>();
            memoryChart.put("yAxis", Map.of(
                "min", 0,
                "max", 100,
                "unit", "%"
            ));
            memoryChart.put("color", "#67c23a");
            config.put("memory", memoryChart);
            
            // 磁盘图表配置
            Map<String, Object> diskChart = new HashMap<>();
            diskChart.put("yAxis", Map.of(
                "min", 0,
                "max", 100,
                "unit", "%"
            ));
            diskChart.put("color", "#e6a23c");
            config.put("disk", diskChart);
            
            // 网络图表配置
            Map<String, Object> networkChart = new HashMap<>();
            networkChart.put("yAxis", Map.of(
                "min", 0,
                "unit", "B/s"
            ));
            networkChart.put("series", new String[]{"入站", "出站"});
            networkChart.put("colors", new String[]{"#409eff", "#67c23a"});
            config.put("network", networkChart);
            
            return ReturnResult.ok(config);
        } catch (Exception e) {
            return ReturnResult.error("获取图表配置失败: " + e.getMessage());
        }
    }
}
