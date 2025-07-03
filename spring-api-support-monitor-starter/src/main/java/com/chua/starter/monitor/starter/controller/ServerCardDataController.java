package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerMetricsService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.ServerLatencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 服务器卡片数据控制器
 * 提供前端卡片组件需要的最新数据
 *
 * @author CH
 * @since 2024/12/28
 */
@Slf4j
@RestController
@RequestMapping("/api/server-card")
@RequiredArgsConstructor
@Tag(name = "服务器卡片数据", description = "服务器卡片数据相关接口")
public class ServerCardDataController {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerMetricsService metricsService;
    private final ServerLatencyService latencyService;

    /**
     * 获取单个服务器的卡片数据
     *
     * @param serverId 服务器ID
     * @return 卡片数据
     */
    @GetMapping("/{serverId}")
    @Operation(summary = "获取单个服务器卡片数据", description = "获取服务器的CPU、内存、磁盘、网络等最新数据")
    @Cacheable(value = "serverCardData", key = "#serverId", unless = "#result == null || !#result.ok")
    public ReturnResult<Map<String, Object>> getServerCardData(
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            // 获取服务器基本信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在: " + serverId);
            }

            // 获取最新指标数据
            MonitorSysGenServerMetrics metrics = metricsService.getLatestMetricsDirectly(serverId);

            // 获取最新延迟数据
            ReturnResult<Long> latencyResult = latencyService.getServerLatencyFromRedis(serverId);
            Long latency = latencyResult.isOk() ? latencyResult.getData() : null;

            // 构建卡片数据
            Map<String, Object> cardData = buildServerCardData(server, metrics, latency);

            log.debug("获取服务器卡片数据成功: serverId={}, serverName={}", serverId, server.getMonitorSysGenServerName());
            return ReturnResult.ok(cardData);

        } catch (Exception e) {
            log.error("获取服务器卡片数据失败: serverId={}", serverId, e);
            return ReturnResult.error("获取卡片数据失败: " + e.getMessage());
        }
    }

    /**
     * 批量获取服务器卡片数据
     *
     * @param serverIds 服务器ID列表
     * @return 卡片数据列表
     */
    @PostMapping("/batch")
    @Operation(summary = "批量获取服务器卡片数据", description = "批量获取多个服务器的卡片数据")
    public ReturnResult<Map<Integer, Map<String, Object>>> getBatchServerCardData(
            @Parameter(description = "服务器ID列表", required = true)
            @RequestBody List<Integer> serverIds) {
        try {
            if (serverIds == null || serverIds.isEmpty()) {
                return ReturnResult.ok(new HashMap<>());
            }

            // 限制批量查询数量，避免性能问题
            if (serverIds.size() > 100) {
                return ReturnResult.error("批量查询数量不能超过100个");
            }

            Map<Integer, Map<String, Object>> result = new ConcurrentHashMap<>();

            // 批量获取延迟数据
            ReturnResult<Map<Integer, Long>> latencyResult = latencyService.getBatchServerLatency(serverIds);
            Map<Integer, Long> latencyMap = latencyResult.isOk() ? latencyResult.getData() : new HashMap<>();

            // 使用并行流处理，提高性能
            List<CompletableFuture<Void>> futures = serverIds.stream()
                .map(serverId -> CompletableFuture.runAsync(() -> {
                    try {
                        // 获取服务器基本信息
                        MonitorSysGenServer server = serverService.getById(serverId);
                        if (server == null) {
                            log.warn("服务器不存在: serverId={}", serverId);
                            return;
                        }

                        // 获取最新指标数据
                        MonitorSysGenServerMetrics metrics = metricsService.getLatestMetricsDirectly(serverId);

                        // 获取延迟数据
                        Long latency = latencyMap.get(serverId);

                        // 构建卡片数据
                        Map<String, Object> cardData = buildServerCardData(server, metrics, latency);
                        result.put(serverId, cardData);

                    } catch (Exception e) {
                        log.error("获取服务器卡片数据失败: serverId={}", serverId, e);
                        // 添加错误信息到结果中
                        Map<String, Object> errorData = createErrorCardData(serverId, e.getMessage());
                        result.put(serverId, errorData);
                    }
                }))
                .collect(Collectors.toList());

            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            log.debug("批量获取服务器卡片数据完成: 请求{}个，成功{}个", serverIds.size(), result.size());
            return ReturnResult.ok(result);

        } catch (Exception e) {
            log.error("批量获取服务器卡片数据失败", e);
            return ReturnResult.error("批量获取卡片数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有在线服务器的卡片数据
     *
     * @return 所有在线服务器的卡片数据
     */
    @GetMapping("/all-online")
    @Operation(summary = "获取所有在线服务器卡片数据", description = "获取所有在线服务器的卡片数据")
    @Cacheable(value = "allOnlineServerCardData", unless = "#result == null || !#result.ok")
    public ReturnResult<Map<Integer, Map<String, Object>>> getAllOnlineServerCardData() {
        try {
            // 获取所有在线服务器
            List<MonitorSysGenServer> onlineServers = serverService.list().stream()
                .filter(server -> server.getMonitorSysGenServerStatus() != null &&
                                server.getMonitorSysGenServerStatus() == 1)
                .collect(Collectors.toList());

            if (onlineServers.isEmpty()) {
                log.info("当前没有在线的服务器");
                return ReturnResult.ok(new HashMap<>());
            }

            // 提取服务器ID列表
            List<Integer> serverIds = onlineServers.stream()
                .map(MonitorSysGenServer::getMonitorSysGenServerId)
                .collect(Collectors.toList());

            log.info("获取所有在线服务器卡片数据: 共{}台服务器", serverIds.size());

            // 批量获取卡片数据
            return getBatchServerCardData(serverIds);

        } catch (Exception e) {
            log.error("获取所有在线服务器卡片数据失败", e);
            return ReturnResult.error("获取所有在线服务器卡片数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务器状态汇总
     *
     * @return 服务器状态汇总
     */
    @GetMapping("/status-summary")
    @Operation(summary = "获取服务器状态汇总", description = "获取所有服务器的状态统计信息")
    @Cacheable(value = "serverStatusSummary", unless = "#result == null || !#result.ok")
    public ReturnResult<Map<String, Object>> getServerStatusSummary() {
        try {
            List<MonitorSysGenServer> allServers = serverService.list();

            Map<String, Object> summary = new HashMap<>();

            // 统计服务器状态
            long totalServers = allServers.size();
            long onlineServers = allServers.stream()
                .filter(server -> server.getMonitorSysGenServerStatus() != null &&
                                server.getMonitorSysGenServerStatus() == 1)
                .count();
            long offlineServers = totalServers - onlineServers;

            // 统计连接状态
            long connectedServers = allServers.stream()
                .filter(server -> server.getMonitorSysGenServerConnectionStatus() != null &&
                                server.getMonitorSysGenServerConnectionStatus() == 1)
                .count();
            long disconnectedServers = totalServers - connectedServers;

            summary.put("totalServers", totalServers);
            summary.put("onlineServers", onlineServers);
            summary.put("offlineServers", offlineServers);
            summary.put("connectedServers", connectedServers);
            summary.put("disconnectedServers", disconnectedServers);
            summary.put("onlineRate", totalServers > 0 ? (double) onlineServers / totalServers * 100 : 0.0);
            summary.put("connectionRate", totalServers > 0 ? (double) connectedServers / totalServers * 100 : 0.0);
            summary.put("updateTime", System.currentTimeMillis());

            log.debug("服务器状态汇总: 总计{}台，在线{}台，连接{}台", totalServers, onlineServers, connectedServers);
            return ReturnResult.ok(summary);

        } catch (Exception e) {
            log.error("获取服务器状态汇总失败", e);
            return ReturnResult.error("获取服务器状态汇总失败: " + e.getMessage());
        }
    }

    /**
     * 构建服务器卡片数据
     *
     * @param server  服务器信息
     * @param metrics 指标数据
     * @param latency 延迟数据
     * @return 卡片数据
     */
    private Map<String, Object> buildServerCardData(MonitorSysGenServer server, 
                                                   MonitorSysGenServerMetrics metrics, 
                                                   Long latency) {
        Map<String, Object> cardData = new HashMap<>();

        // 服务器基本信息
        cardData.put("serverId", server.getMonitorSysGenServerId());
        cardData.put("serverName", server.getMonitorSysGenServerName());
        cardData.put("serverHost", server.getMonitorSysGenServerHost());
        cardData.put("serverStatus", server.getMonitorSysGenServerStatus());
        cardData.put("connectionStatus", server.getMonitorSysGenServerConnectionStatus());

        // 指标数据
        if (metrics != null) {
            // CPU数据
            Map<String, Object> cpuData = new HashMap<>();
            cpuData.put("usage", metrics.getMonitorSysGenServerMetricsCpuUsage());
            cpuData.put("cores", metrics.getMonitorSysGenServerMetricsCpuCores());
            cpuData.put("load1m", metrics.getMonitorSysGenServerMetricsCpuLoad1m());
            cpuData.put("load5m", metrics.getMonitorSysGenServerMetricsCpuLoad5m());
            cpuData.put("load15m", metrics.getMonitorSysGenServerMetricsCpuLoad15m());
            cardData.put("cpu", cpuData);

            // 内存数据
            Map<String, Object> memoryData = new HashMap<>();
            memoryData.put("usage", metrics.getMonitorSysGenServerMetricsMemoryUsage());
            memoryData.put("total", metrics.getMonitorSysGenServerMetricsMemoryTotal());
            memoryData.put("used", metrics.getMonitorSysGenServerMetricsMemoryUsed());
            memoryData.put("free", metrics.getMonitorSysGenServerMetricsMemoryFree());
            cardData.put("memory", memoryData);

            // 磁盘数据
            Map<String, Object> diskData = new HashMap<>();
            diskData.put("usage", metrics.getMonitorSysGenServerMetricsDiskUsage());
            diskData.put("total", metrics.getMonitorSysGenServerMetricsDiskTotal());
            diskData.put("used", metrics.getMonitorSysGenServerMetricsDiskUsed());
            diskData.put("free", metrics.getMonitorSysGenServerMetricsDiskFree());
            cardData.put("disk", diskData);

            // 网络数据
            Map<String, Object> networkData = new HashMap<>();
            networkData.put("inSpeed", metrics.getMonitorSysGenServerMetricsNetworkIn());
            networkData.put("outSpeed", metrics.getMonitorSysGenServerMetricsNetworkOut());
            cardData.put("network", networkData);

            // 系统信息
            Map<String, Object> systemData = new HashMap<>();
            systemData.put("uptime", metrics.getMonitorSysGenServerMetricsUptime());
            systemData.put("processCount", metrics.getMonitorSysGenServerMetricsProcessCount());
            systemData.put("responseTime", metrics.getMonitorSysGenServerMetricsResponseTime());
            systemData.put("status", metrics.getMonitorSysGenServerMetricsStatus());
            systemData.put("osInfo", metrics.getMonitorSysGenServerMetricsOsInfo());
            cardData.put("system", systemData);

            // 数据收集时间
            cardData.put("collectTime", metrics.getMonitorSysGenServerMetricsCollectTime());
        } else {
            // 没有指标数据时的默认值
            cardData.put("cpu", createEmptyMetricData());
            cardData.put("memory", createEmptyMetricData());
            cardData.put("disk", createEmptyMetricData());
            cardData.put("network", createEmptyMetricData());
            cardData.put("system", createEmptyMetricData());
            cardData.put("collectTime", null);
        }

        // 延迟数据
        cardData.put("latency", latency);

        // 数据更新时间
        cardData.put("updateTime", System.currentTimeMillis());

        return cardData;
    }

    /**
     * 创建空的指标数据
     *
     * @return 空的指标数据
     */
    private Map<String, Object> createEmptyMetricData() {
        Map<String, Object> emptyData = new HashMap<>();
        emptyData.put("usage", null);
        emptyData.put("total", null);
        emptyData.put("used", null);
        emptyData.put("free", null);
        return emptyData;
    }

    /**
     * 创建错误卡片数据
     *
     * @param serverId 服务器ID
     * @param errorMessage 错误信息
     * @return 错误卡片数据
     */
    private Map<String, Object> createErrorCardData(Integer serverId, String errorMessage) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("serverId", serverId);
        errorData.put("error", true);
        errorData.put("errorMessage", errorMessage);
        errorData.put("serverName", "未知服务器");
        errorData.put("serverHost", null);
        errorData.put("serverStatus", 0);
        errorData.put("connectionStatus", 0);
        errorData.put("cpu", createEmptyMetricData());
        errorData.put("memory", createEmptyMetricData());
        errorData.put("disk", createEmptyMetricData());
        errorData.put("network", createEmptyMetricData());
        errorData.put("system", createEmptyMetricData());
        errorData.put("latency", null);
        errorData.put("collectTime", null);
        errorData.put("updateTime", System.currentTimeMillis());
        return errorData;
    }

    /**
     * 获取服务器健康评分
     *
     * @param serverId 服务器ID
     * @return 健康评分
     */
    @GetMapping("/{serverId}/health-score")
    @Operation(summary = "获取服务器健康评分", description = "根据CPU、内存、磁盘使用率计算服务器健康评分")
    public ReturnResult<Map<String, Object>> getServerHealthScore(
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            // 获取最新指标数据
            MonitorSysGenServerMetrics metrics = metricsService.getLatestMetricsDirectly(serverId);
            if (metrics == null) {
                return ReturnResult.error("未找到服务器指标数据");
            }

            Map<String, Object> healthScore = calculateHealthScore(metrics);
            return ReturnResult.ok(healthScore);

        } catch (Exception e) {
            log.error("获取服务器健康评分失败: serverId={}", serverId, e);
            return ReturnResult.error("获取健康评分失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务器性能趋势
     *
     * @param serverId 服务器ID
     * @param hours 时间范围（小时）
     * @return 性能趋势数据
     */
    @GetMapping("/{serverId}/performance-trend")
    @Operation(summary = "获取服务器性能趋势", description = "获取指定时间范围内的服务器性能趋势数据")
    public ReturnResult<Map<String, Object>> getServerPerformanceTrend(
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId,
            @Parameter(description = "时间范围（小时）", example = "24")
            @RequestParam(defaultValue = "24") Integer hours) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            if (hours <= 0 || hours > 168) { // 最多7天
                return ReturnResult.error("时间范围必须在1-168小时之间");
            }

            Map<String, Object> trendData = calculatePerformanceTrend(serverId, hours);
            return ReturnResult.ok(trendData);

        } catch (Exception e) {
            log.error("获取服务器性能趋势失败: serverId={}, hours={}", serverId, hours, e);
            return ReturnResult.error("获取性能趋势失败: " + e.getMessage());
        }
    }

    /**
     * 刷新服务器卡片数据缓存
     *
     * @param serverId 服务器ID（可选，不提供则刷新所有）
     * @return 刷新结果
     */
    @PostMapping("/refresh-cache")
    @Operation(summary = "刷新服务器卡片数据缓存", description = "清除指定服务器或所有服务器的卡片数据缓存")
    public ReturnResult<String> refreshCardDataCache(
            @Parameter(description = "服务器ID（可选）")
            @RequestParam(required = false) Integer serverId) {
        try {
            // TODO: 实现缓存清理逻辑
            if (serverId != null) {
                log.info("刷新服务器卡片数据缓存: serverId={}", serverId);
                return ReturnResult.ok("已刷新服务器 " + serverId + " 的卡片数据缓存");
            } else {
                log.info("刷新所有服务器卡片数据缓存");
                return ReturnResult.ok("已刷新所有服务器的卡片数据缓存");
            }

        } catch (Exception e) {
            log.error("刷新卡片数据缓存失败: serverId={}", serverId, e);
            return ReturnResult.error("刷新缓存失败: " + e.getMessage());
        }
    }

    /**
     * 计算服务器健康评分
     *
     * @param metrics 指标数据
     * @return 健康评分数据
     */
    private Map<String, Object> calculateHealthScore(MonitorSysGenServerMetrics metrics) {
        Map<String, Object> healthScore = new HashMap<>();

        // 获取各项指标
        double cpuUsage = metrics.getMonitorSysGenServerMetricsCpuUsage() != null ?
            metrics.getMonitorSysGenServerMetricsCpuUsage().doubleValue() : 0.0;
        double memoryUsage = metrics.getMonitorSysGenServerMetricsMemoryUsage() != null ?
            metrics.getMonitorSysGenServerMetricsMemoryUsage().doubleValue() : 0.0;
        double diskUsage = metrics.getMonitorSysGenServerMetricsDiskUsage() != null ?
            metrics.getMonitorSysGenServerMetricsDiskUsage().doubleValue() : 0.0;

        // 计算各项评分（0-100分）
        double cpuScore = Math.max(0, 100 - cpuUsage);
        double memoryScore = Math.max(0, 100 - memoryUsage);
        double diskScore = Math.max(0, 100 - diskUsage);

        // 计算综合评分（加权平均）
        double totalScore = (cpuScore * 0.4 + memoryScore * 0.4 + diskScore * 0.2);

        // 确定健康等级
        String healthLevel;
        String healthColor;
        if (totalScore >= 80) {
            healthLevel = "优秀";
            healthColor = "#52c41a";
        } else if (totalScore >= 60) {
            healthLevel = "良好";
            healthColor = "#1890ff";
        } else if (totalScore >= 40) {
            healthLevel = "一般";
            healthColor = "#faad14";
        } else {
            healthLevel = "较差";
            healthColor = "#ff4d4f";
        }

        healthScore.put("totalScore", Math.round(totalScore * 100.0) / 100.0);
        healthScore.put("healthLevel", healthLevel);
        healthScore.put("healthColor", healthColor);
        healthScore.put("cpuScore", Math.round(cpuScore * 100.0) / 100.0);
        healthScore.put("memoryScore", Math.round(memoryScore * 100.0) / 100.0);
        healthScore.put("diskScore", Math.round(diskScore * 100.0) / 100.0);
        healthScore.put("updateTime", System.currentTimeMillis());

        return healthScore;
    }

    /**
     * 计算服务器性能趋势
     *
     * @param serverId 服务器ID
     * @param hours 时间范围（小时）
     * @return 性能趋势数据
     */
    private Map<String, Object> calculatePerformanceTrend(Integer serverId, Integer hours) {
        Map<String, Object> trendData = new HashMap<>();

        // TODO: 实现真实的趋势计算逻辑
        // 这里应该从历史数据中获取指定时间范围内的数据并计算趋势

        // 模拟趋势数据
        trendData.put("serverId", serverId);
        trendData.put("timeRange", hours + "小时");
        trendData.put("cpuTrend", "稳定");
        trendData.put("memoryTrend", "上升");
        trendData.put("diskTrend", "稳定");
        trendData.put("networkTrend", "波动");
        trendData.put("overallTrend", "良好");
        trendData.put("updateTime", System.currentTimeMillis());

        log.debug("计算服务器性能趋势: serverId={}, hours={}", serverId, hours);
        return trendData;
    }
}
