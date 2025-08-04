package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.indicator.TimeIndicator;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerMetricsService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.starter.service.ServerMetricsWebSocketService;
import com.chua.starter.redis.support.service.TimeSeriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 服务器指标数据服务实现类
 *
 * @author CH
 * @since 2024/12/18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerMetricsServiceImpl implements MonitorSysGenServerMetricsService {

    private final TimeSeriesService timeSeriesService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final MonitorSysGenServerService serverService;
    @Autowired
    @Lazy
    private ServerMetricsWebSocketService serverMetricsWebSocketService;

    private static final String REDIS_METRICS_PREFIX = "SERVER:server:metrics:";
    private static final String REDIS_LATEST_PREFIX = "SERVER:server:latest:";

    // 防重复触发推送的缓存，记录最近触发推送的服务器ID和时间
    private final Map<Integer, Long> recentTriggerCache = new ConcurrentHashMap<>();
    // 触发推送的最小间隔时间（毫秒），默认30秒
    private static final long TRIGGER_INTERVAL_MS = 30 * 1000;

    @Override
    public IPage<MonitorSysGenServerMetrics> pageFor(Page<MonitorSysGenServerMetrics> page, MonitorSysGenServerMetrics entity) {
        // 基于Redis的分页查询实现
        try {
            if (entity == null || entity.getMonitorSysGenServerId() == null) {
                log.warn("分页查询参数不完整");
                return page;
            }

            // 从Redis获取指标数据
            Integer monitorSysGenServerId = entity.getMonitorSysGenServerId();
            String serverId = String.valueOf(monitorSysGenServerId);
            long endTime = System.currentTimeMillis();
            long startTime = endTime - (24 * 60 * 60 * 1000); // 默认查询最近24小时

            // 计算分页参数
            long offset = (page.getCurrent() - 1) * page.getSize();
            long count = page.getSize();

            var result = timeSeriesService.range(
                REDIS_METRICS_PREFIX + serverId,
                startTime,
                endTime,
                false,
                (int) count
            );

            if (result.isSuccess() && result.getData() != null) {
                List<MonitorSysGenServerMetrics> metricsList = result.getData().stream()
                    .map(it -> convertTimeIndicatorToMetrics(it, monitorSysGenServerId))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                page.setRecords(metricsList);
                page.setTotal(metricsList.size());
            }

            return page;
        } catch (Exception e) {
            log.error("分页查询服务器指标数据失败: {}", e.getMessage(), e);
            return page;
        }
    }

    @Override
    public ReturnResult<Boolean> saveMetrics(MonitorSysGenServerMetrics metrics) {
        try {
            // 检查服务器是否存在
            if (metrics.getMonitorSysGenServerId() == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            if (metrics.getMonitorSysGenServerMetricsCollectTime() == null) {
                metrics.setMonitorSysGenServerMetricsCollectTime(LocalDateTime.now());
            }

            // 将指标数据存储到Redis
            String serverId = String.valueOf(metrics.getMonitorSysGenServerId());
            long timestamp = metrics.getMonitorSysGenServerMetricsCollectTime().toEpochSecond(ZoneOffset.UTC) * 1000;

            // 构建指标数据Map
            Map<String, Object> metricsData = buildMetricsDataMap(metrics);

            // 1. 存储最新指标数据到Hash（用于快速获取最新数据）
            Map<String, String> hashData = new HashMap<>();
            for (Map.Entry<String, Object> entry : metricsData.entrySet()) {
                hashData.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            hashData.put("timestamp", String.valueOf(timestamp));

            // 保存最新数据到Redis Hash - 逐个字段保存
            for (Map.Entry<String, String> entry : hashData.entrySet()) {
                timeSeriesService.hSet(REDIS_LATEST_PREFIX + serverId, entry.getKey(), entry.getValue());
            }

            // 2. 存储历史指标数据到时序数据库（用于历史查询）
            saveMetricsToTimeSeries(serverId, timestamp, metrics);

            log.info("保存服务器指标数据到Redis: serverId={}, timestamp={}", serverId, timestamp);
            log.debug("CPU使用率: {}", metrics.getMonitorSysGenServerMetricsCpuUsage());
            log.debug("内存使用率: {}", metrics.getMonitorSysGenServerMetricsMemoryUsage());
            log.debug("磁盘使用率: {}", metrics.getMonitorSysGenServerMetricsDiskUsage());

            // 设置过期时间
            setMetricsExpireTime(metrics.getMonitorSysGenServerId(), REDIS_LATEST_PREFIX + serverId);

            // 这里简化实现，实际应该使用时序数据库
            log.info("保存服务器指标数据到Redis: serverId={}, timestamp={}", serverId, timestamp);
            log.debug("CPU使用率: {}", metrics.getMonitorSysGenServerMetricsCpuUsage());
            log.debug("内存使用率: {}", metrics.getMonitorSysGenServerMetricsMemoryUsage());
            log.debug("磁盘使用率: {}", metrics.getMonitorSysGenServerMetricsDiskUsage());

            return ReturnResult.ok(true, "保存成功");
        } catch (Exception e) {
            log.error("保存服务器指标数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("保存失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> batchSaveMetrics(List<MonitorSysGenServerMetrics> metricsList) {
        try {
            if (metricsList == null || metricsList.isEmpty()) {
                return ReturnResult.ok(true, "没有数据需要保存");
            }

            int successCount = 0;
            for (MonitorSysGenServerMetrics metrics : metricsList) {
                var result = saveMetrics(metrics);
                if (result.isOk()) {
                    successCount++;
                }
            }

            boolean allSuccess = successCount == metricsList.size();
            String message = String.format("批量保存完成，成功: %d/%d", successCount, metricsList.size());

            return ReturnResult.of(allSuccess, message);
        } catch (Exception e) {
            log.error("批量保存服务器指标数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("批量保存失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerMetrics> getLatestMetrics(Integer serverId) {
        try {
            String key = REDIS_LATEST_PREFIX + serverId;
            Map<String, String> latestData = timeSeriesService.hGet(key);

            if (latestData == null || latestData.isEmpty()) {
                // 当指标数据为空时，触发指标推送
                triggerMetricsPushForEmptyData(serverId);
                return ReturnResult.ok(null);
            }

            MonitorSysGenServerMetrics metrics = convertHashToMetrics(latestData, serverId);
            return ReturnResult.ok(metrics);
        } catch (Exception e) {
            log.error("获取最新指标数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取最新指标数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerMetrics>> getHistoryMetrics(Integer serverId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            String key = REDIS_METRICS_PREFIX + serverId;
            long start = startTime.toEpochSecond(ZoneOffset.UTC) * 1000;
            long end = endTime.toEpochSecond(ZoneOffset.UTC) * 1000;

            var result = timeSeriesService.range(key, start, end, false, 1000);

            if (result.isOk() && result.getData() != null) {
                List<MonitorSysGenServerMetrics> metricsList = result.getData().stream()
                    .map(indicator -> convertTimeIndicatorToMetrics(indicator, serverId))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                return ReturnResult.ok(metricsList);
            }

            return ReturnResult.ok(List.of());
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
            
            // TODO: 实现基于Redis的图表数据查询
            log.warn("getMetricsChartData方法暂未实现Redis查询逻辑: serverId={}, metricType={}", serverId, metricType);
            data = List.of();
            
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
            // TODO: 实现基于Redis的对比图表数据查询
            log.warn("getCompareChartData方法暂未实现Redis查询逻辑: serverIds={}, metricType={}", serverIds, metricType);

            Map<String, Object> chartData = new HashMap<>();
            chartData.put("serverIds", serverIds);
            chartData.put("metricType", metricType);
            chartData.put("data", List.of());
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
            // TODO: 实现基于Redis的指标统计查询
            log.warn("getMetricsStatistics方法暂未实现Redis查询逻辑");
            return ReturnResult.ok(Map.of());
        } catch (Exception e) {
            log.error("获取指标统计数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取指标统计数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getAlertData(Integer serverId, Map<String, Object> thresholds) {
        try {
            // 基于Redis的告警检测实现
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24); // 检查最近24小时

            Double cpuThreshold = getThreshold(thresholds, "cpu", 80.0);
            Double memoryThreshold = getThreshold(thresholds, "memory", 80.0);
            Double diskThreshold = getThreshold(thresholds, "disk", 90.0);

            // 获取历史数据进行告警检测
            var historyResult = getHistoryMetrics(serverId, startTime, endTime);
            List<Map<String, Object>> alerts = new ArrayList<>();

            if (historyResult.isOk() && historyResult.getData() != null) {
                for (MonitorSysGenServerMetrics metrics : historyResult.getData()) {
                    if (isAnomalyData(metrics, cpuThreshold, memoryThreshold, diskThreshold)) {
                        alerts.add(convertToAlert(metrics));
                    }
                }
            }

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
            // 基于Redis的删除实现
            log.warn("Redis时序数据删除功能暂未实现");
            int deletedCount = 0;
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
            // 基于Redis的批量删除实现
            log.warn("Redis时序数据批量删除功能暂未实现");
            int deletedCount = 0;

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

            // 基于Redis的统计数据获取
            var historyResult = getHistoryMetrics(serverId, startTime, endTime);
            Map<String, Object> statistics = new HashMap<>();

            if (historyResult.isSuccess() && historyResult.getData() != null) {
                List<MonitorSysGenServerMetrics> metricsList = historyResult.getData();
                statistics = calculateStatistics(metricsList);
            }

            report.put("statistics", statistics);

            // 获取健康状态
            var healthResult = getServerHealthStatus(serverId);
            Map<String, Object> healthData = healthResult.isOk() ? healthResult.getData() : new HashMap<>();
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
            // 基于Redis的数据导出
            var historyResult = getHistoryMetrics(serverId, startTime, endTime);
            List<MonitorSysGenServerMetrics> metrics = historyResult.isSuccess() ?
                historyResult.getData() : new ArrayList<>();

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
            var latestResult = getLatestMetrics(serverId);

            Map<String, Object> realTimeData = new HashMap<>();
            if (latestResult.isSuccess() && latestResult.getData() != null) {
                MonitorSysGenServerMetrics latestMetrics = latestResult.getData();
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
            // 基于Redis的健康状态获取
            var latestResult = getLatestMetrics(serverId);
            Map<String, Object> healthData = new HashMap<>();

            if (latestResult.isOk() && latestResult.getData() != null) {
                MonitorSysGenServerMetrics metrics = latestResult.getData();
                healthData.put("cpu_usage", metrics.getMonitorSysGenServerMetricsCpuUsage());
                healthData.put("memory_usage", metrics.getMonitorSysGenServerMetricsMemoryUsage());
                healthData.put("disk_usage", metrics.getMonitorSysGenServerMetricsDiskUsage());
                healthData.put("status", metrics.getMonitorSysGenServerMetricsStatus());

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

            // 基于Redis的统计数据计算
            var historyResult = getHistoryMetrics(serverId, startTime, endTime);
            Map<String, Object> statistics = new HashMap<>();

            if (historyResult.isOk() && historyResult.getData() != null) {
                statistics = calculateStatistics(historyResult.getData());
            }

            Map<String, Object> prediction = new HashMap<>();
            prediction.put("serverId", serverId);
            prediction.put("predictDays", predictDays);
            prediction.put("baseStatistics", statistics);

            // 简单的线性预测（实际应该使用更复杂的算法）
            if (!statistics.isEmpty()) {
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

            // 基于Redis的趋势分析
            var historyResult = getHistoryMetrics(serverId, startTime, endTime);
            List<Map<String, Object>> data = new ArrayList<>();

            if (historyResult.isSuccess() && historyResult.getData() != null) {
                data = convertMetricsToChartData(historyResult.getData(), metricType);
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

            // 基于Redis的异常检测
            var historyResult = getHistoryMetrics(serverId, startTime, endTime);
            List<Map<String, Object>> anomalies = new ArrayList<>();

            if (historyResult.isOk() && historyResult.getData() != null) {
                for (MonitorSysGenServerMetrics metrics : historyResult.getData()) {
                    if (isAnomalyData(metrics, thresholds.get("cpu"), thresholds.get("memory"), thresholds.get("disk"))) {
                        anomalies.add(convertToAnomaly(metrics, thresholds));
                    }
                }
            }

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

            // 基于Redis的清理实现
            // 这里简化实现，实际应该遍历所有服务器的时序数据进行清理
            int deletedCount = 0;
            log.warn("Redis时序数据清理功能暂未实现");

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

            // Redis不需要表优化
            log.info("Redis存储无需表优化");
            return ReturnResult.ok();

        } catch (Exception e) {
            log.error("优化指标数据表失败", e);
            return ReturnResult.error("优化指标数据表失败: " + e.getMessage());
        }
    }

    /**
     * 构建指标数据Map
     */
    private Map<String, Object> buildMetricsDataMap(MonitorSysGenServerMetrics metrics) {
        Map<String, Object> data = new HashMap<>();
        data.put("serverId", metrics.getMonitorSysGenServerId());
        data.put("collectTime", metrics.getMonitorSysGenServerMetricsCollectTime());
        data.put("cpuUsage", metrics.getMonitorSysGenServerMetricsCpuUsage());
        data.put("cpuCores", metrics.getMonitorSysGenServerMetricsCpuCores());
        data.put("cpuLoad1m", metrics.getMonitorSysGenServerMetricsCpuLoad1m());
        data.put("cpuLoad5m", metrics.getMonitorSysGenServerMetricsCpuLoad5m());
        data.put("cpuLoad15m", metrics.getMonitorSysGenServerMetricsCpuLoad15m());
        data.put("memoryUsage", metrics.getMonitorSysGenServerMetricsMemoryUsage());
        data.put("memoryTotal", metrics.getMonitorSysGenServerMetricsMemoryTotal());
        data.put("memoryUsed", metrics.getMonitorSysGenServerMetricsMemoryUsed());
        data.put("memoryFree", metrics.getMonitorSysGenServerMetricsMemoryFree());
        data.put("diskUsage", metrics.getMonitorSysGenServerMetricsDiskUsage());
        data.put("diskTotal", metrics.getMonitorSysGenServerMetricsDiskTotal());
        data.put("diskUsed", metrics.getMonitorSysGenServerMetricsDiskUsed());
        data.put("diskFree", metrics.getMonitorSysGenServerMetricsDiskFree());
        data.put("networkIn", metrics.getMonitorSysGenServerMetricsNetworkIn());
        data.put("networkOut", metrics.getMonitorSysGenServerMetricsNetworkOut());
        data.put("uptime", metrics.getMonitorSysGenServerMetricsUptime());
        data.put("processCount", metrics.getMonitorSysGenServerMetricsProcessCount());
        data.put("status", metrics.getMonitorSysGenServerMetricsStatus());
        data.put("responseTime", metrics.getMonitorSysGenServerMetricsResponseTime());
        data.put("osInfo", metrics.getMonitorSysGenServerMetricsOsInfo());
        return data;
    }

    /**
     * 将TimeIndicator转换为MonitorSysGenServerMetrics
     */
    private MonitorSysGenServerMetrics convertTimeIndicatorToMetrics(TimeIndicator indicator, Integer serverId) {
        try {
            MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();
            metrics.setMonitorSysGenServerId(serverId);

            // 从TimeIndicator中提取数据
            if (indicator.getTimestamp() > 0) {
                metrics.setMonitorSysGenServerMetricsCollectTime(
                    LocalDateTime.ofEpochSecond(indicator.getTimestamp() / 1000, 0, ZoneOffset.UTC)
                );
            }

            // 这里需要根据实际的TimeIndicator结构来解析数据
            // 暂时返回基本结构
            return metrics;
        } catch (Exception e) {
            log.error("转换TimeIndicator失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 计算统计数据
     */
    private Map<String, Object> calculateStatistics(List<MonitorSysGenServerMetrics> metricsList) {
        Map<String, Object> statistics = new HashMap<>();

        if (metricsList == null || metricsList.isEmpty()) {
            return statistics;
        }

        double totalCpu = 0, totalMemory = 0, totalDisk = 0;
        int count = 0;

        for (MonitorSysGenServerMetrics metrics : metricsList) {
            if (metrics.getMonitorSysGenServerMetricsCpuUsage() != null) {
                totalCpu += metrics.getMonitorSysGenServerMetricsCpuUsage().doubleValue();
            }
            if (metrics.getMonitorSysGenServerMetricsMemoryUsage() != null) {
                totalMemory += metrics.getMonitorSysGenServerMetricsMemoryUsage().doubleValue();
            }
            if (metrics.getMonitorSysGenServerMetricsDiskUsage() != null) {
                totalDisk += metrics.getMonitorSysGenServerMetricsDiskUsage().doubleValue();
            }
            count++;
        }

        if (count > 0) {
            statistics.put("avg_cpu_usage", totalCpu / count);
            statistics.put("avg_memory_usage", totalMemory / count);
            statistics.put("avg_disk_usage", totalDisk / count);
            statistics.put("count", count);
        }

        return statistics;
    }

    /**
     * 将指标数据转换为图表数据
     */
    private List<Map<String, Object>> convertMetricsToChartData(List<MonitorSysGenServerMetrics> metricsList, String metricType) {
        List<Map<String, Object>> chartData = new ArrayList<>();

        for (MonitorSysGenServerMetrics metrics : metricsList) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("timestamp", metrics.getMonitorSysGenServerMetricsCollectTime());

            switch (metricType.toLowerCase()) {
                case "cpu":
                    dataPoint.put("cpu_usage", metrics.getMonitorSysGenServerMetricsCpuUsage());
                    break;
                case "memory":
                    dataPoint.put("memory_usage", metrics.getMonitorSysGenServerMetricsMemoryUsage());
                    break;
                case "disk":
                    dataPoint.put("disk_usage", metrics.getMonitorSysGenServerMetricsDiskUsage());
                    break;
                default:
                    continue;
            }

            chartData.add(dataPoint);
        }

        return chartData;
    }

    /**
     * 检查是否为异常数据
     */
    private boolean isAnomalyData(MonitorSysGenServerMetrics metrics, Double cpuThreshold, Double memoryThreshold, Double diskThreshold) {
        if (metrics.getMonitorSysGenServerMetricsCpuUsage() != null &&
            metrics.getMonitorSysGenServerMetricsCpuUsage().doubleValue() > cpuThreshold) {
            return true;
        }

        if (metrics.getMonitorSysGenServerMetricsMemoryUsage() != null &&
            metrics.getMonitorSysGenServerMetricsMemoryUsage().doubleValue() > memoryThreshold) {
            return true;
        }

        if (metrics.getMonitorSysGenServerMetricsDiskUsage() != null &&
            metrics.getMonitorSysGenServerMetricsDiskUsage().doubleValue() > diskThreshold) {
            return true;
        }

        return false;
    }

    /**
     * 将Hash数据转换为MonitorSysGenServerMetrics
     */
    private MonitorSysGenServerMetrics convertHashToMetrics(Map<String, String> hashData, Integer serverId) {
        try {
            MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();
            metrics.setMonitorSysGenServerId(serverId);

            if (hashData.containsKey("timestamp")) {
                long timestamp = Long.parseLong(hashData.get("timestamp"));
                metrics.setMonitorSysGenServerMetricsCollectTime(
                    LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC)
                );
            }

            if (hashData.containsKey("cpuUsage")) {
                metrics.setMonitorSysGenServerMetricsCpuUsage(new BigDecimal(hashData.get("cpuUsage")));
            }

            if (hashData.containsKey("memoryUsage")) {
                metrics.setMonitorSysGenServerMetricsMemoryUsage(new BigDecimal(hashData.get("memoryUsage")));
            }

            if (hashData.containsKey("diskUsage")) {
                metrics.setMonitorSysGenServerMetricsDiskUsage(new BigDecimal(hashData.get("diskUsage")));
            }

            if (hashData.containsKey("networkIn")) {
                metrics.setMonitorSysGenServerMetricsNetworkIn(Long.parseLong(hashData.get("networkIn")));
            }

            if (hashData.containsKey("networkOut")) {
                metrics.setMonitorSysGenServerMetricsNetworkOut(Long.parseLong(hashData.get("networkOut")));
            }

            if (hashData.containsKey("status")) {
                metrics.setMonitorSysGenServerMetricsStatus(Integer.parseInt(hashData.get("status")));
            }

            if (hashData.containsKey("responseTime")) {
                metrics.setMonitorSysGenServerMetricsResponseTime(Long.parseLong(hashData.get("responseTime")));
            }

            return metrics;
        } catch (Exception e) {
            log.error("转换Hash数据失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置指标数据的过期时间
     */
    private void setMetricsExpireTime(Integer serverId, String redisKey) {
        try {
            // 获取服务器配置
            var settingResult = serverSettingService.getOrCreateSetting(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                log.warn("获取服务器配置失败，使用默认过期时间: serverId={}", serverId);
                // TODO: 使用默认30天过期时间 - 需要实现Redis过期时间设置
                // timeSeriesService.expire(redisKey, 30 * 24 * 60 * 60);
                log.debug("暂时跳过设置过期时间，需要实现Redis过期功能: key={}", redisKey);
                return;
            }

            MonitorSysGenServerSetting setting = settingResult.getData();

            // 获取指标存储时间配置，优先使用具体指标的配置，其次使用通用配置
            Integer retentionDays = setting.getMonitorSysGenServerSettingMetricsRetentionDays();

            // 检查是否有更细粒度的配置
            if (setting.getMonitorSysGenServerSettingCpuRetentionDays() != null) {
                retentionDays = setting.getMonitorSysGenServerSettingCpuRetentionDays();
            }

            // 如果没有配置，使用默认30天
            if (retentionDays == null || retentionDays <= 0) {
                retentionDays = 30;
            }

            // 设置过期时间（转换为秒）
            int expireSeconds = retentionDays * 24 * 60 * 60;
            // TODO: 实现Redis过期时间设置
            // timeSeriesService.expire(redisKey, expireSeconds);

            log.debug("配置指标数据过期时间: key={}, days={}, seconds={} (暂未实现)", redisKey, retentionDays, expireSeconds);

        } catch (Exception e) {
            log.error("设置指标数据过期时间失败: key={}, serverId={}", redisKey, serverId, e);
            // TODO: 出错时使用默认30天过期时间 - 需要实现Redis过期功能
            log.debug("暂时跳过设置默认过期时间，需要实现Redis过期功能: key={}", redisKey);
        }
    }

    @Override
    public MonitorSysGenServerMetrics getLatestMetricsDirectly(Integer serverId) {
        try {
            ReturnResult<MonitorSysGenServerMetrics> result = getLatestMetrics(serverId);
            return result.isOk() ? result.getData() : null;
        } catch (Exception e) {
            log.error("获取最新指标数据失败: serverId={}, error={}", serverId, e.getMessage());
            return null;
        }
    }

    /**
     * 获取最新指标数据（不触发推送版本）
     * 用于定时任务等内部调用，避免循环触发推送
     *
     * @param serverId 服务器ID
     * @return 最新指标数据
     */
    public ReturnResult<MonitorSysGenServerMetrics> getLatestMetricsWithoutTrigger(Integer serverId) {
        try {
            String key = REDIS_LATEST_PREFIX + serverId;
            Map<String, String> latestData = timeSeriesService.hGet(key);

            if (latestData == null || latestData.isEmpty()) {
                // 不触发推送，直接返回空数据
                return ReturnResult.ok(null);
            }

            MonitorSysGenServerMetrics metrics = convertHashToMetrics(latestData, serverId);
            return ReturnResult.ok(metrics);
        } catch (Exception e) {
            log.error("获取最新指标数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取最新指标数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新指标数据（不触发推送版本，直接返回对象）
     * 用于定时任务等内部调用，避免循环触发推送
     *
     * @param serverId 服务器ID
     * @return 最新指标数据，如果不存在返回null
     */
    public MonitorSysGenServerMetrics getLatestMetricsDirectlyWithoutTrigger(Integer serverId) {
        try {
            ReturnResult<MonitorSysGenServerMetrics> result = getLatestMetricsWithoutTrigger(serverId);
            return result.isOk() ? result.getData() : null;
        } catch (Exception e) {
            log.error("获取最新指标数据失败: serverId={}, error={}", serverId, e.getMessage());
            return null;
        }
    }

    /**
     * 当指标数据为空时触发指标推送
     *
     * @param serverId 服务器ID
     */
    private void triggerMetricsPushForEmptyData(Integer serverId) {
        try {
            // 检查是否在最小间隔时间内已经触发过
            long currentTime = System.currentTimeMillis();
            Long lastTriggerTime = recentTriggerCache.get(serverId);

            if (lastTriggerTime != null && (currentTime - lastTriggerTime) < TRIGGER_INTERVAL_MS) {
                log.debug("服务器 {} 在 {}ms 内已触发过指标推送，跳过本次触发",
                    serverId, currentTime - lastTriggerTime);
                return;
            }

            log.info("服务器 {} 指标数据为空，触发指标推送", serverId);

            // 获取服务器信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                log.warn("服务器不存在，无法触发指标推送: serverId={}", serverId);
                return;
            }

            // 获取服务器配置
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                serverSettingService.getOrCreateSetting(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                log.warn("获取服务器配置失败，无法触发指标推送: serverId={}", serverId);
                return;
            }

            MonitorSysGenServerSetting setting = settingResult.getData();

            // 检查监控是否启用
            if (setting.getMonitorSysGenServerSettingMonitorEnabled() != 1) {
                log.debug("服务器 {} 监控未启用，跳过指标推送", serverId);
                return;
            }

            // 记录触发时间，防止重复触发
            recentTriggerCache.put(serverId, currentTime);

            // 异步触发指标推送，避免阻塞当前请求
            CompletableFuture.runAsync(() -> {
                try {
                    // 使用WebSocket服务推送指标数据
                    serverMetricsWebSocketService.pushSingleServerMetricsByConfig(server, setting);
                    log.info("成功触发服务器 {} 指标推送", serverId);
                } catch (Exception e) {
                    log.error("触发服务器 {} 指标推送失败: {}", serverId, e.getMessage(), e);
                    // 推送失败时移除缓存记录，允许下次重试
                    recentTriggerCache.remove(serverId);
                }
            });

        } catch (Exception e) {
            log.error("触发指标推送时发生异常: serverId={}", serverId, e);
        }
    }

    /**
     * 清理过期的触发缓存记录
     * 定期调用此方法清理超过间隔时间的缓存记录
     */
    public void cleanupExpiredTriggerCache() {
        try {
            long currentTime = System.currentTimeMillis();
            int removedCount = 0;

            Iterator<Map.Entry<Integer, Long>> iterator = recentTriggerCache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Long> entry = iterator.next();
                if (currentTime - entry.getValue() > TRIGGER_INTERVAL_MS) {
                    iterator.remove();
                    removedCount++;
                }
            }

            if (removedCount > 0) {
                log.debug("清理了 {} 个过期的触发缓存记录", removedCount);
            }
        } catch (Exception e) {
            log.error("清理触发缓存时发生异常", e);
        }
    }

    @Override
    public List<MonitorSysGenServerMetrics> getMetricsByTimeRange(Integer serverId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            ReturnResult<List<MonitorSysGenServerMetrics>> result = getHistoryMetrics(serverId, startTime, endTime);
            return result.isOk() && result.getData() != null ? result.getData() : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取时间范围内指标数据失败: serverId={}, startTime={}, endTime={}, error={}",
                    serverId, startTime, endTime, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 保存指标数据到时序数据库
     */
    private void saveMetricsToTimeSeries(String serverId, long timestamp, MonitorSysGenServerMetrics metrics) {
        try {
            // 获取保留时间配置（天数转换为秒）
            long retentionPeriod = 30 * 24 * 60 * 60; // 默认30天

            // 构建标签
            LinkedHashMap<String, String> labels = new LinkedHashMap<>();
            labels.put("serverId", serverId);
            labels.put("serverName", "server_" + serverId); // 可以从数据库获取真实名称

            // 保存各种指标到时序数据库
            if (metrics.getMonitorSysGenServerMetricsCpuUsage() != null) {
                timeSeriesService.save(
                    REDIS_METRICS_PREFIX + serverId + ":cpu_usage",
                    timestamp,
                    metrics.getMonitorSysGenServerMetricsCpuUsage().doubleValue(),
                    labels,
                    retentionPeriod
                );
            }

            if (metrics.getMonitorSysGenServerMetricsMemoryUsage() != null) {
                timeSeriesService.save(
                    REDIS_METRICS_PREFIX + serverId + ":memory_usage",
                    timestamp,
                    metrics.getMonitorSysGenServerMetricsMemoryUsage().doubleValue(),
                    labels,
                    retentionPeriod
                );
            }

            if (metrics.getMonitorSysGenServerMetricsDiskUsage() != null) {
                timeSeriesService.save(
                    REDIS_METRICS_PREFIX + serverId + ":disk_usage",
                    timestamp,
                    metrics.getMonitorSysGenServerMetricsDiskUsage().doubleValue(),
                    labels,
                    retentionPeriod
                );
            }

            if (metrics.getMonitorSysGenServerMetricsNetworkOut() != null) {
                timeSeriesService.save(
                    REDIS_METRICS_PREFIX + serverId + ":network_out",
                    timestamp,
                    metrics.getMonitorSysGenServerMetricsNetworkOut().doubleValue(),
                    labels,
                    retentionPeriod
                );
            }

            if (metrics.getMonitorSysGenServerMetricsNetworkIn() != null) {
                timeSeriesService.save(
                    REDIS_METRICS_PREFIX + serverId + ":network_in",
                    timestamp,
                    metrics.getMonitorSysGenServerMetricsNetworkIn().doubleValue(),
                    labels,
                    retentionPeriod
                );
            }

            log.debug("指标数据已保存到时序数据库: serverId={}", serverId);

        } catch (Exception e) {
            log.error("保存指标数据到时序数据库失败: serverId={}, error={}", serverId, e.getMessage(), e);
        }
    }
}
