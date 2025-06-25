package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerMetricsMapper;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器指标数据服务实现类
 *
 * @author CH
 * @since 2024/12/18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerMetricsServiceImpl extends ServiceImpl<MonitorSysGenServerMetricsMapper, MonitorSysGenServerMetrics>
        implements MonitorSysGenServerMetricsService {

    @Override
    public IPage<MonitorSysGenServerMetrics> pageFor(Page<MonitorSysGenServerMetrics> page, MonitorSysGenServerMetrics entity) {
        return this.page(page, Wrappers.<MonitorSysGenServerMetrics>lambdaQuery()
                .eq(entity.getMonitorSysGenServerId() != null, 
                    MonitorSysGenServerMetrics::getMonitorSysGenServerId, entity.getMonitorSysGenServerId())
                .eq(entity.getMonitorSysGenServerMetricsStatus() != null, 
                    MonitorSysGenServerMetrics::getMonitorSysGenServerMetricsStatus, entity.getMonitorSysGenServerMetricsStatus())
                .ge(entity.getMonitorSysGenServerMetricsCollectTime() != null, 
                    MonitorSysGenServerMetrics::getMonitorSysGenServerMetricsCollectTime, entity.getMonitorSysGenServerMetricsCollectTime())
                .orderByDesc(MonitorSysGenServerMetrics::getMonitorSysGenServerMetricsCollectTime));
    }

    @Override
    public ReturnResult<Boolean> saveMetrics(MonitorSysGenServerMetrics metrics) {
        try {
            boolean result = this.save(metrics);
            return ReturnResult.of(result, result ? "保存成功" : "保存失败");
        } catch (Exception e) {
            log.error("保存服务器指标数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("保存失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> batchSaveMetrics(List<MonitorSysGenServerMetrics> metricsList) {
        try {
            boolean result = this.saveBatch(metricsList);
            return ReturnResult.of(result, result ? "批量保存成功" : "批量保存失败");
        } catch (Exception e) {
            log.error("批量保存服务器指标数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("批量保存失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerMetrics> getLatestMetrics(Integer serverId) {
        try {
            MonitorSysGenServerMetrics metrics = baseMapper.selectLatestByServerId(serverId);
            return ReturnResult.ok(metrics);
        } catch (Exception e) {
            log.error("获取最新指标数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取最新指标数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerMetrics>> getHistoryMetrics(Integer serverId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            List<MonitorSysGenServerMetrics> metrics = baseMapper.selectHistoryByServerId(serverId, startTime, endTime);
            return ReturnResult.ok(metrics);
        } catch (Exception e) {
            log.error("获取历史指标数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取历史指标数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getMetricsChartData(Integer serverId, String metricType, 
                                                                LocalDateTime startTime, LocalDateTime endTime, String interval) {
        try {
            Map<String, Object> chartData = new HashMap<>();
            List<Map<String, Object>> data;
            
            switch (metricType.toLowerCase()) {
                case "cpu":
                    data = baseMapper.getCpuUsageChartData(serverId, startTime, endTime, parseInterval(interval));
                    break;
                case "memory":
                    data = baseMapper.getMemoryUsageChartData(serverId, startTime, endTime, parseInterval(interval));
                    break;
                case "disk":
                    data = baseMapper.getDiskUsageChartData(serverId, startTime, endTime, parseInterval(interval));
                    break;
                case "network":
                    data = baseMapper.getNetworkChartData(serverId, startTime, endTime, parseInterval(interval));
                    break;
                default:
                    return ReturnResult.error("不支持的指标类型: " + metricType);
            }
            
            chartData.put("serverId", serverId);
            chartData.put("metricType", metricType);
            chartData.put("data", data);
            chartData.put("startTime", startTime);
            chartData.put("endTime", endTime);
            chartData.put("interval", interval);
            
            return ReturnResult.ok(chartData);
        } catch (Exception e) {
            log.error("获取指标图表数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取指标图表数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getCompareChartData(List<Integer> serverIds, String metricType,
                                                                LocalDateTime startTime, LocalDateTime endTime, String interval) {
        try {
            List<Map<String, Object>> data = baseMapper.getCompareChartData(serverIds, metricType, startTime, endTime);
            
            Map<String, Object> chartData = new HashMap<>();
            chartData.put("serverIds", serverIds);
            chartData.put("metricType", metricType);
            chartData.put("data", data);
            chartData.put("startTime", startTime);
            chartData.put("endTime", endTime);
            chartData.put("interval", interval);
            
            return ReturnResult.ok(chartData);
        } catch (Exception e) {
            log.error("获取对比图表数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取对比图表数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getMetricsStatistics(Integer serverId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Map<String, Object> statistics = baseMapper.getMetricsStatistics(serverId, startTime, endTime);
            return ReturnResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取指标统计数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取指标统计数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getAlertData(Integer serverId, Map<String, Object> thresholds) {
        try {
            // 简化实现，实际应该根据阈值配置进行告警检测
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24); // 检查最近24小时
            
            Double cpuThreshold = getThreshold(thresholds, "cpu", 80.0);
            Double memoryThreshold = getThreshold(thresholds, "memory", 80.0);
            Double diskThreshold = getThreshold(thresholds, "disk", 90.0);
            
            List<MonitorSysGenServerMetrics> anomalyData = baseMapper.getAnomalyData(
                    serverId, startTime, endTime, cpuThreshold, memoryThreshold, diskThreshold);
            
            // 转换为告警格式
            List<Map<String, Object>> alerts = anomalyData.stream()
                    .map(this::convertToAlert)
                    .toList();
            
            return ReturnResult.ok(alerts);
        } catch (Exception e) {
            log.error("获取告警数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取告警数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Integer> deleteExpiredMetrics(Integer serverId, Integer retentionDays) {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);
            int deletedCount = baseMapper.deleteExpiredMetrics(serverId, expireTime);
            return ReturnResult.ok(deletedCount);
        } catch (Exception e) {
            log.error("删除过期指标数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("删除过期指标数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> batchDeleteExpiredMetrics() {
        try {
            // 默认删除30天前的数据
            LocalDateTime expireTime = LocalDateTime.now().minusDays(30);
            int deletedCount = baseMapper.batchDeleteExpiredMetrics(expireTime);
            
            Map<String, Object> result = new HashMap<>();
            result.put("deletedCount", deletedCount);
            result.put("expireTime", expireTime);
            
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("批量删除过期指标数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("批量删除过期指标数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getPerformanceReport(Integer serverId, String reportType,
                                                                 LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Map<String, Object> report = new HashMap<>();
            
            // 获取统计数据
            Map<String, Object> statistics = baseMapper.getMetricsStatistics(serverId, startTime, endTime);
            report.put("statistics", statistics);
            
            // 获取健康状态
            Map<String, Object> healthData = baseMapper.getServerHealthData(serverId);
            report.put("currentHealth", healthData);
            
            report.put("serverId", serverId);
            report.put("reportType", reportType);
            report.put("startTime", startTime);
            report.put("endTime", endTime);
            report.put("generateTime", LocalDateTime.now());
            
            return ReturnResult.ok(report);
        } catch (Exception e) {
            log.error("获取性能报告失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取性能报告失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<byte[]> exportMetricsData(Integer serverId, LocalDateTime startTime, LocalDateTime endTime, String format) {
        try {
            // 简化实现，实际应该根据格式生成对应的文件
            List<MonitorSysGenServerMetrics> metrics = baseMapper.selectHistoryByServerId(serverId, startTime, endTime);
            
            String data = "导出数据: " + metrics.size() + " 条记录";
            return ReturnResult.ok(data.getBytes());
        } catch (Exception e) {
            log.error("导出指标数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("导出指标数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getRealTimeMetrics(Integer serverId) {
        try {
            MonitorSysGenServerMetrics latestMetrics = baseMapper.selectLatestByServerId(serverId);
            
            Map<String, Object> realTimeData = new HashMap<>();
            if (latestMetrics != null) {
                realTimeData.put("serverId", serverId);
                realTimeData.put("cpuUsage", latestMetrics.getMonitorSysGenServerMetricsCpuUsage());
                realTimeData.put("memoryUsage", latestMetrics.getMonitorSysGenServerMetricsMemoryUsage());
                realTimeData.put("diskUsage", latestMetrics.getMonitorSysGenServerMetricsDiskUsage());
                realTimeData.put("networkIn", latestMetrics.getMonitorSysGenServerMetricsNetworkIn());
                realTimeData.put("networkOut", latestMetrics.getMonitorSysGenServerMetricsNetworkOut());
                realTimeData.put("status", latestMetrics.getMonitorSysGenServerMetricsStatus());
                realTimeData.put("collectTime", latestMetrics.getMonitorSysGenServerMetricsCollectTime());
            }
            
            return ReturnResult.ok(realTimeData);
        } catch (Exception e) {
            log.error("获取实时指标数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取实时指标数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getServerHealthStatus(Integer serverId) {
        try {
            Map<String, Object> healthData = baseMapper.getServerHealthData(serverId);

            if (healthData != null) {
                // 根据指标判断健康状态
                String status = determineHealthStatus(healthData);
                healthData.put("healthStatus", status);
            }

            return ReturnResult.ok(healthData);
        } catch (Exception e) {
            log.error("获取服务器健康状态失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取服务器健康状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getCapacityPrediction(Integer serverId, Integer predictDays) {
        try {
            // 简化实现：基于历史数据进行线性预测
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(30); // 使用最近30天的数据进行预测

            Map<String, Object> statistics = baseMapper.getMetricsStatistics(serverId, startTime, endTime);

            Map<String, Object> prediction = new HashMap<>();
            prediction.put("serverId", serverId);
            prediction.put("predictDays", predictDays);
            prediction.put("baseStatistics", statistics);

            // 简单的线性预测（实际应该使用更复杂的算法）
            if (statistics != null) {
                Double avgCpuUsage = (Double) statistics.get("avg_cpu_usage");
                Double avgMemoryUsage = (Double) statistics.get("avg_memory_usage");
                Double avgDiskUsage = (Double) statistics.get("avg_disk_usage");

                if (avgCpuUsage != null) {
                    prediction.put("predictedCpuUsage", Math.min(100.0, avgCpuUsage + (avgCpuUsage * 0.1 * predictDays / 30)));
                }
                if (avgMemoryUsage != null) {
                    prediction.put("predictedMemoryUsage", Math.min(100.0, avgMemoryUsage + (avgMemoryUsage * 0.05 * predictDays / 30)));
                }
                if (avgDiskUsage != null) {
                    prediction.put("predictedDiskUsage", Math.min(100.0, avgDiskUsage + (avgDiskUsage * 0.02 * predictDays / 30)));
                }
            }

            prediction.put("generateTime", LocalDateTime.now());

            return ReturnResult.ok(prediction);
        } catch (Exception e) {
            log.error("获取容量预测失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取容量预测失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getTrendAnalysis(Integer serverId, String metricType, Integer days) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);

            List<Map<String, Object>> data;
            switch (metricType.toLowerCase()) {
                case "cpu":
                    data = baseMapper.getCpuUsageChartData(serverId, startTime, endTime, 60);
                    break;
                case "memory":
                    data = baseMapper.getMemoryUsageChartData(serverId, startTime, endTime, 60);
                    break;
                case "disk":
                    data = baseMapper.getDiskUsageChartData(serverId, startTime, endTime, 60);
                    break;
                default:
                    return ReturnResult.error("不支持的指标类型: " + metricType);
            }

            Map<String, Object> trendAnalysis = new HashMap<>();
            trendAnalysis.put("serverId", serverId);
            trendAnalysis.put("metricType", metricType);
            trendAnalysis.put("days", days);
            trendAnalysis.put("data", data);

            // 简单的趋势分析
            if (!data.isEmpty()) {
                double trend = calculateTrend(data, metricType);
                trendAnalysis.put("trend", trend);
                trendAnalysis.put("trendDirection", trend > 0 ? "上升" : trend < 0 ? "下降" : "平稳");
            }

            trendAnalysis.put("generateTime", LocalDateTime.now());

            return ReturnResult.ok(trendAnalysis);
        } catch (Exception e) {
            log.error("获取趋势分析失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取趋势分析失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getAnomalyDetection(Integer serverId, String sensitivity) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24); // 检查最近24小时

            // 根据敏感度设置阈值
            Map<String, Double> thresholds = getSensitivityThresholds(sensitivity);

            List<MonitorSysGenServerMetrics> anomalyData = baseMapper.getAnomalyData(
                    serverId, startTime, endTime,
                    thresholds.get("cpu"),
                    thresholds.get("memory"),
                    thresholds.get("disk"));

            List<Map<String, Object>> anomalies = anomalyData.stream()
                    .map(metrics -> convertToAnomaly(metrics, thresholds))
                    .toList();

            return ReturnResult.ok(anomalies);
        } catch (Exception e) {
            log.error("获取异常检测结果失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取异常检测结果失败: " + e.getMessage());
        }
    }

    /**
     * 解析时间间隔
     */
    private Integer parseInterval(String interval) {
        if (StringUtils.isEmpty(interval)) {
            return 5; // 默认5分钟
        }

        switch (interval.toLowerCase()) {
            case "5m":
                return 5;
            case "15m":
                return 15;
            case "1h":
                return 60;
            case "1d":
                return 1440;
            default:
                return 5;
        }
    }

    /**
     * 获取阈值
     */
    private Double getThreshold(Map<String, Object> thresholds, String key, Double defaultValue) {
        Object value = thresholds.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * 转换为告警格式
     */
    private Map<String, Object> convertToAlert(MonitorSysGenServerMetrics metrics) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("serverId", metrics.getMonitorSysGenServerId());
        alert.put("collectTime", metrics.getMonitorSysGenServerMetricsCollectTime());
        alert.put("cpuUsage", metrics.getMonitorSysGenServerMetricsCpuUsage());
        alert.put("memoryUsage", metrics.getMonitorSysGenServerMetricsMemoryUsage());
        alert.put("diskUsage", metrics.getMonitorSysGenServerMetricsDiskUsage());
        alert.put("alertType", "threshold_exceeded");
        return alert;
    }

    /**
     * 判断健康状态
     */
    private String determineHealthStatus(Map<String, Object> healthData) {
        try {
            Double cpuUsage = (Double) healthData.get("cpu_usage");
            Double memoryUsage = (Double) healthData.get("memory_usage");
            Double diskUsage = (Double) healthData.get("disk_usage");
            Integer status = (Integer) healthData.get("status");

            if (status == null || status == 0) {
                return "offline";
            }

            if ((cpuUsage != null && cpuUsage > 90) ||
                (memoryUsage != null && memoryUsage > 90) ||
                (diskUsage != null && diskUsage > 95)) {
                return "critical";
            }

            if ((cpuUsage != null && cpuUsage > 70) ||
                (memoryUsage != null && memoryUsage > 80) ||
                (diskUsage != null && diskUsage > 85)) {
                return "warning";
            }

            return "healthy";
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 计算趋势
     */
    private double calculateTrend(List<Map<String, Object>> data, String metricType) {
        if (data.size() < 2) {
            return 0.0;
        }

        // 简单的线性回归计算趋势
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = data.size();

        for (int i = 0; i < n; i++) {
            double x = i;
            Object valueObj = data.get(i).get(getMetricKey(metricType));
            double y = valueObj instanceof Number ? ((Number) valueObj).doubleValue() : 0.0;

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        // 计算斜率
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope;
    }

    /**
     * 获取指标键名
     */
    private String getMetricKey(String metricType) {
        switch (metricType.toLowerCase()) {
            case "cpu":
                return "cpu_usage";
            case "memory":
                return "memory_usage";
            case "disk":
                return "disk_usage";
            default:
                return "cpu_usage";
        }
    }

    /**
     * 根据敏感度获取阈值
     */
    private Map<String, Double> getSensitivityThresholds(String sensitivity) {
        Map<String, Double> thresholds = new HashMap<>();

        switch (sensitivity.toLowerCase()) {
            case "low":
                thresholds.put("cpu", 90.0);
                thresholds.put("memory", 90.0);
                thresholds.put("disk", 95.0);
                break;
            case "medium":
                thresholds.put("cpu", 80.0);
                thresholds.put("memory", 85.0);
                thresholds.put("disk", 90.0);
                break;
            case "high":
                thresholds.put("cpu", 70.0);
                thresholds.put("memory", 80.0);
                thresholds.put("disk", 85.0);
                break;
            default:
                thresholds.put("cpu", 80.0);
                thresholds.put("memory", 85.0);
                thresholds.put("disk", 90.0);
        }

        return thresholds;
    }

    /**
     * 转换为异常格式
     */
    private Map<String, Object> convertToAnomaly(MonitorSysGenServerMetrics metrics, Map<String, Double> thresholds) {
        Map<String, Object> anomaly = new HashMap<>();
        anomaly.put("serverId", metrics.getMonitorSysGenServerId());
        anomaly.put("collectTime", metrics.getMonitorSysGenServerMetricsCollectTime());
        anomaly.put("cpuUsage", metrics.getMonitorSysGenServerMetricsCpuUsage());
        anomaly.put("memoryUsage", metrics.getMonitorSysGenServerMetricsMemoryUsage());
        anomaly.put("diskUsage", metrics.getMonitorSysGenServerMetricsDiskUsage());
        anomaly.put("thresholds", thresholds);
        anomaly.put("anomalyType", "threshold_exceeded");
        return anomaly;
    }

    @Override
    public ReturnResult<Integer> cleanupExpiredMetrics(LocalDateTime expireTime) {
        try {
            log.info("开始清理过期指标数据，过期时间: {}", expireTime);

            int deletedCount = baseMapper.delete(Wrappers.<MonitorSysGenServerMetrics>lambdaQuery()
                    .lt(MonitorSysGenServerMetrics::getMonitorSysGenServerMetricsCollectTime, expireTime));

            log.info("清理过期指标数据完成，删除了 {} 条记录", deletedCount);
            return ReturnResult.ok(deletedCount);

        } catch (Exception e) {
            log.error("清理过期指标数据失败", e);
            return ReturnResult.error("清理过期指标数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Void> optimizeTables() {
        try {
            log.info("开始优化指标数据表");

            // 执行表优化操作
            baseMapper.optimizeTable();

            log.info("指标数据表优化完成");
            return ReturnResult.ok();

        } catch (Exception e) {
            log.error("优化指标数据表失败", e);
            return ReturnResult.error("优化指标数据表失败: " + e.getMessage());
        }
    }
}
