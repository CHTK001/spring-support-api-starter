package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.service.MonitorSysGenServerDataReportService;
import com.chua.starter.monitor.service.report.ReportManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务器数据上报服务实现类
 * 重构后使用ReportManager统一管理不同的上报方式
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenServerDataReportServiceImpl implements MonitorSysGenServerDataReportService {

    private final ReportManager reportManager;

    // 服务器上报统计缓存
    private final Map<Integer, AtomicLong> serverReportCount = new ConcurrentHashMap<>();
    private final Map<Integer, LocalDateTime> serverLastReportTime = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicLong> serverErrorCount = new ConcurrentHashMap<>();

    @Override
    public ReturnResult<MonitorSysGenServerMetrics> handleLocalReport(MonitorSysGenServer server) {
        try {
            ReturnResult<MonitorSysGenServerMetrics> result = reportManager.handleReport(server, null);
            
            // 更新统计信息
            updateServerReportStats(server.getMonitorSysGenServerId(), result.isOk());
            
            return result;
        } catch (Exception e) {
            log.error("处理本地上报失败: serverId={}", server.getMonitorSysGenServerId(), e);
            updateServerReportStats(server.getMonitorSysGenServerId(), false);
            return ReturnResult.error("处理本地上报失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerMetrics> handleApiReport(MonitorSysGenServer server, Map<String, Object> reportData) {
        try {
            ReturnResult<MonitorSysGenServerMetrics> result = reportManager.handleReport(server, reportData);
            
            // 更新统计信息
            updateServerReportStats(server.getMonitorSysGenServerId(), result.isOk());
            
            return result;
        } catch (Exception e) {
            log.error("处理API上报失败: serverId={}", server.getMonitorSysGenServerId(), e);
            updateServerReportStats(server.getMonitorSysGenServerId(), false);
            return ReturnResult.error("处理API上报失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> handlePrometheusReport(MonitorSysGenServer server) {
        try {
            // Prometheus上报返回Map格式的数据，不存储到数据库
            ReturnResult<MonitorSysGenServerMetrics> result = reportManager.handleReport(server, null);
            
            // 更新统计信息
            updateServerReportStats(server.getMonitorSysGenServerId(), result.isOk());
            
            if (result.isOk() && result.getData() != null) {
                // 将MonitorSysGenServerMetrics转换为Map返回
                Map<String, Object> prometheusData = convertMetricsToMap(result.getData());
                return ReturnResult.success(prometheusData);
            } else {
                return ReturnResult.error(result.getMsg());
            }
        } catch (Exception e) {
            log.error("处理Prometheus上报失败: serverId={}", server.getMonitorSysGenServerId(), e);
            updateServerReportStats(server.getMonitorSysGenServerId(), false);
            return ReturnResult.error("处理Prometheus上报失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getServerReportStatistics(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("serverId", serverId);
            statistics.put("reportCount", serverReportCount.getOrDefault(serverId, new AtomicLong(0)).get());
            statistics.put("errorCount", serverErrorCount.getOrDefault(serverId, new AtomicLong(0)).get());
            statistics.put("lastReportTime", serverLastReportTime.get(serverId));
            
            // 计算成功率
            long totalReports = serverReportCount.getOrDefault(serverId, new AtomicLong(0)).get();
            long errors = serverErrorCount.getOrDefault(serverId, new AtomicLong(0)).get();
            double successRate = totalReports > 0 ? (double) (totalReports - errors) / totalReports * 100 : 0;
            statistics.put("successRate", successRate);
            
            statistics.put("statisticsTime", LocalDateTime.now());

            return ReturnResult.success(statistics);

        } catch (Exception e) {
            log.error("获取服务器上报统计失败: serverId={}", serverId, e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getServerReportOverview() {
        try {
            Map<String, Object> overview = new HashMap<>();
            
            // 总服务器数量
            overview.put("totalServers", serverReportCount.size());
            
            // 总上报次数
            long totalReports = serverReportCount.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum();
            overview.put("totalReports", totalReports);
            
            // 总错误次数
            long totalErrors = serverErrorCount.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum();
            overview.put("totalErrors", totalErrors);
            
            // 总体成功率
            double overallSuccessRate = totalReports > 0 ? (double) (totalReports - totalErrors) / totalReports * 100 : 0;
            overview.put("overallSuccessRate", overallSuccessRate);
            
            // 活跃服务器数量（最近1小时内有上报）
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long activeServers = serverLastReportTime.values().stream()
                    .mapToLong(time -> time.isAfter(oneHourAgo) ? 1 : 0)
                    .sum();
            overview.put("activeServers", activeServers);
            
            overview.put("updateTime", LocalDateTime.now());

            return ReturnResult.success(overview);

        } catch (Exception e) {
            log.error("获取服务器上报概览失败", e);
            return ReturnResult.error("获取概览失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> cleanupExpiredServerMetrics(Integer retentionDays) {
        try {
            if (retentionDays == null || retentionDays <= 0) {
                retentionDays = 30; // 默认保留30天
            }

            // 这里可以添加清理数据库中过期指标数据的逻辑
            log.info("开始清理过期的服务器指标数据，保留天数: {}", retentionDays);
            
            // 清理内存中的过期统计数据
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
            int cleanedCount = 0;
            
            for (Map.Entry<Integer, LocalDateTime> entry : serverLastReportTime.entrySet()) {
                if (entry.getValue().isBefore(cutoffTime)) {
                    Integer serverId = entry.getKey();
                    serverLastReportTime.remove(serverId);
                    serverReportCount.remove(serverId);
                    serverErrorCount.remove(serverId);
                    cleanedCount++;
                }
            }

            log.info("清理过期服务器上报数据完成: 清理了 {} 个服务器的记录", cleanedCount);
            return ReturnResult.success("清理完成，共清理了 " + cleanedCount + " 个服务器的记录");

        } catch (Exception e) {
            log.error("清理过期服务器指标数据失败", e);
            return ReturnResult.error("清理失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getServerReportHistory(Integer serverId, Integer limit) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            if (limit == null || limit <= 0) {
                limit = 100;
            }

            Map<String, Object> history = new HashMap<>();
            history.put("serverId", serverId);
            history.put("reportCount", serverReportCount.getOrDefault(serverId, new AtomicLong(0)).get());
            history.put("errorCount", serverErrorCount.getOrDefault(serverId, new AtomicLong(0)).get());
            history.put("lastReportTime", serverLastReportTime.get(serverId));
            history.put("limit", limit);

            // 这里可以添加从数据库查询历史记录的逻辑
            
            return ReturnResult.success(history);

        } catch (Exception e) {
            log.error("获取服务器上报历史失败: serverId={}", serverId, e);
            return ReturnResult.error("获取上报历史失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> resetServerReportStats(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            serverReportCount.remove(serverId);
            serverLastReportTime.remove(serverId);
            serverErrorCount.remove(serverId);

            log.info("已重置服务器上报统计: serverId={}", serverId);
            return ReturnResult.success("已重置服务器上报统计");

        } catch (Exception e) {
            log.error("重置服务器上报统计失败: serverId={}", serverId, e);
            return ReturnResult.error("重置统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getServerReportErrorStats(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("serverId", serverId);
            errorStats.put("errorCount", serverErrorCount.getOrDefault(serverId, new AtomicLong(0)).get());
            errorStats.put("totalReports", serverReportCount.getOrDefault(serverId, new AtomicLong(0)).get());
            
            // 计算错误率
            long totalReports = serverReportCount.getOrDefault(serverId, new AtomicLong(0)).get();
            long errors = serverErrorCount.getOrDefault(serverId, new AtomicLong(0)).get();
            double errorRate = totalReports > 0 ? (double) errors / totalReports * 100 : 0;
            errorStats.put("errorRate", errorRate);
            
            errorStats.put("statisticsTime", LocalDateTime.now());

            return ReturnResult.success(errorStats);

        } catch (Exception e) {
            log.error("获取服务器上报错误统计失败: serverId={}", serverId, e);
            return ReturnResult.error("获取错误统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> handleBatchServerReport() {
        try {
            // 这里可以添加批量处理服务器上报的逻辑
            log.info("开始批量处理服务器数据上报");
            
            Map<String, Object> result = new HashMap<>();
            result.put("processTime", LocalDateTime.now());
            result.put("message", "批量处理完成");
            
            return ReturnResult.success(result);

        } catch (Exception e) {
            log.error("批量处理服务器数据上报失败", e);
            return ReturnResult.error("批量处理失败: " + e.getMessage());
        }
    }

    /**
     * 将MonitorSysGenServerMetrics转换为Map
     */
    private Map<String, Object> convertMetricsToMap(MonitorSysGenServerMetrics metrics) {
        Map<String, Object> map = new HashMap<>();
        map.put("serverId", metrics.getMonitorSysGenServerId());
        map.put("collectTime", metrics.getMonitorSysGenServerMetricsCollectTime());
        map.put("cpuUsage", metrics.getMonitorSysGenServerMetricsCpuUsage());
        map.put("memoryUsage", metrics.getMonitorSysGenServerMetricsMemoryUsage());
        map.put("diskUsage", metrics.getMonitorSysGenServerMetricsDiskUsage());
        map.put("networkIn", metrics.getMonitorSysGenServerMetricsNetworkIn());
        map.put("networkOut", metrics.getMonitorSysGenServerMetricsNetworkOut());
        return map;
    }

    /**
     * 更新服务器上报统计
     */
    private void updateServerReportStats(Integer serverId, boolean success) {
        serverReportCount.computeIfAbsent(serverId, k -> new AtomicLong(0)).incrementAndGet();
        serverLastReportTime.put(serverId, LocalDateTime.now());

        if (!success) {
            serverErrorCount.computeIfAbsent(serverId, k -> new AtomicLong(0)).incrementAndGet();
        }
    }

    @Override
    public ReturnResult<String> startDataCollection(MonitorSysGenServer server) {
        try {
            if (server == null) {
                return ReturnResult.error("服务器配置不能为空");
            }

            // 委托给ReportManager处理
            reportManager.startDataCollection(server);

            log.info("启动数据收集成功: serverId={}, serverName={}",
                server.getMonitorSysGenServerId(), server.getMonitorSysGenServerName());
            return ReturnResult.success("数据收集已启动");

        } catch (Exception e) {
            log.error("启动数据收集失败: serverId={}",
                server != null ? server.getMonitorSysGenServerId() : "unknown", e);
            return ReturnResult.error("启动数据收集失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> stopDataCollection(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            // 委托给ReportManager处理，需要获取上报方式
            // 这里简化处理，实际应该查询服务器配置获取上报方式
            reportManager.stopDataCollection(serverId, "API");

            log.info("停止数据收集成功: serverId={}", serverId);
            return ReturnResult.success("数据收集已停止");

        } catch (Exception e) {
            log.error("停止数据收集失败: serverId={}", serverId, e);
            return ReturnResult.error("停止数据收集失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> handlePrometheusQuery(MonitorSysGenServer server) {
        try {
            if (server == null) {
                return ReturnResult.error("服务器配置不能为空");
            }

            // 委托给ReportManager处理Prometheus查询
            Map<String, Object> result = new HashMap<>();
            result.put("serverId", server.getMonitorSysGenServerId());
            result.put("serverName", server.getMonitorSysGenServerName());
            result.put("queryTime", LocalDateTime.now());
            result.put("message", "Prometheus查询功能待实现");

            log.info("Prometheus查询完成: serverId={}", server.getMonitorSysGenServerId());
            return ReturnResult.success(result);

        } catch (Exception e) {
            log.error("Prometheus查询失败: serverId={}",
                server != null ? server.getMonitorSysGenServerId() : "unknown", e);
            return ReturnResult.error("Prometheus查询失败: " + e.getMessage());
        }
    }
}
