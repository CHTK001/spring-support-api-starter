package com.chua.starter.monitor.controller.alert;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.configuration.MetricsThresholdConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 指标阈值配置控制器
 *
 * @author CH
 * @since 2024/12/30
 */
@RestController
@RequestMapping("v1/metrics/threshold")
@Tag(name = "指标阈值配置")
@RequiredArgsConstructor
public class MetricsThresholdController {

    private final MetricsThresholdConfig thresholdConfig;

    /**
     * 获取所有阈值配置
     *
     * @return 阈值配置
     */
    @GetMapping("all")
    @Operation(summary = "获取所有阈值配置")
    public ReturnResult<Map<String, Object>> getAllThresholds() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("thresholds", thresholdConfig.getAllThresholds());
            
            // 添加颜色映射
            Map<String, String> colors = new HashMap<>();
            colors.put("normal", "#67c23a");
            colors.put("warning", "#e6a23c");
            colors.put("critical", "#f56c6c");
            result.put("colors", colors);
            
            return ReturnResult.ok(result);
        } catch (Exception e) {
            return ReturnResult.error("获取阈值配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定指标的阈值配置
     *
     * @param metricType 指标类型
     * @return 阈值配置
     */
    @GetMapping("{metricType}")
    @Operation(summary = "获取指定指标的阈值配置")
    public ReturnResult<MetricsThresholdConfig.ThresholdLevel> getThreshold(
            @Parameter(name = "指标类型") @PathVariable String metricType) {
        try {
            MetricsThresholdConfig.ThresholdLevel threshold = thresholdConfig.getThreshold(metricType);
            return ReturnResult.ok(threshold);
        } catch (Exception e) {
            return ReturnResult.error("获取阈值配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标的颜色信息
     *
     * @param metricType 指标类型
     * @param value      当前值
     * @return 颜色信息
     */
    @GetMapping("color")
    @Operation(summary = "获取指标的颜色信息")
    public ReturnResult<Map<String, Object>> getMetricColor(
            @Parameter(name = "指标类型") @RequestParam String metricType,
            @Parameter(name = "当前值") @RequestParam Double value) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("colorCode", thresholdConfig.getMetricColor(metricType, value));
            result.put("colorLevel", thresholdConfig.getMetricColorLevel(metricType, value));
            result.put("isWarning", thresholdConfig.isWarningLevel(metricType, value));
            result.put("isCritical", thresholdConfig.isCriticalLevel(metricType, value));
            
            return ReturnResult.ok(result);
        } catch (Exception e) {
            return ReturnResult.error("获取颜色信息失败: " + e.getMessage());
        }
    }

    /**
     * 批量获取多个指标的颜色信息
     *
     * @param metrics 指标数据
     * @return 颜色信息
     */
    @PostMapping("colors")
    @Operation(summary = "批量获取多个指标的颜色信息")
    public ReturnResult<Map<String, Object>> getMetricsColors(
            @RequestBody Map<String, Double> metrics) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            for (Map.Entry<String, Double> entry : metrics.entrySet()) {
                String metricType = entry.getKey();
                Double value = entry.getValue();
                
                Map<String, Object> colorInfo = new HashMap<>();
                colorInfo.put("colorCode", thresholdConfig.getMetricColor(metricType, value));
                colorInfo.put("colorLevel", thresholdConfig.getMetricColorLevel(metricType, value));
                colorInfo.put("isWarning", thresholdConfig.isWarningLevel(metricType, value));
                colorInfo.put("isCritical", thresholdConfig.isCriticalLevel(metricType, value));
                
                result.put(metricType, colorInfo);
            }
            
            return ReturnResult.ok(result);
        } catch (Exception e) {
            return ReturnResult.error("获取颜色信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取进度条颜色配置
     *
     * @param metricType 指标类型
     * @return 进度条颜色配置
     */
    @GetMapping("progress-color/{metricType}")
    @Operation(summary = "获取进度条颜色配置")
    public ReturnResult<Map<String, Object>> getProgressColor(
            @Parameter(name = "指标类型") @PathVariable String metricType) {
        try {
            MetricsThresholdConfig.ThresholdLevel threshold = thresholdConfig.getThreshold(metricType);
            Map<String, Object> progressColor = threshold.getProgressColor(0.0);
            return ReturnResult.ok(progressColor);
        } catch (Exception e) {
            return ReturnResult.error("获取进度条颜色配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有指标的进度条颜色配置
     *
     * @return 进度条颜色配置
     */
    @GetMapping("progress-colors")
    @Operation(summary = "获取所有指标的进度条颜色配置")
    public ReturnResult<Map<String, Object>> getAllProgressColors() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            for (Map.Entry<String, MetricsThresholdConfig.ThresholdLevel> entry : 
                 thresholdConfig.getAllThresholds().entrySet()) {
                String metricType = entry.getKey();
                MetricsThresholdConfig.ThresholdLevel threshold = entry.getValue();
                result.put(metricType, threshold.getProgressColor(0.0));
            }
            
            return ReturnResult.ok(result);
        } catch (Exception e) {
            return ReturnResult.error("获取进度条颜色配置失败: " + e.getMessage());
        }
    }
}
