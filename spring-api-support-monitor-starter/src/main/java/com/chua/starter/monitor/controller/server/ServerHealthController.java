package com.chua.starter.monitor.controller.server;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.service.MonitorSysGenServerMetricsService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.ServerHealthScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务器健康评分控制器
 *
 * @author CH
 * @since 2024/12/30
 */
@RestController
@RequestMapping("v1/server/health")
@Tag(name = "服务器健康评分")
@RequiredArgsConstructor
public class ServerHealthController {

    private final ServerHealthScoreService healthScoreService;
    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerMetricsService metricsService;

    /**
     * 获取单个服务器的健康评分
     *
     * @param serverId 服务器ID
     * @return 健康评分结果
     */
    @GetMapping("score/{serverId}")
    @Operation(summary = "获取单个服务器的健康评分")
    public ReturnResult<ServerHealthScoreService.HealthScoreResult> getServerHealthScore(
            @Parameter(name = "服务器ID") @PathVariable Integer serverId) {
        try {
            // 获取最新的指标数据
            MonitorSysGenServerMetrics latestMetrics = metricsService.getLatestMetricsDirectly(serverId);
            if (latestMetrics == null) {
                return ReturnResult.error("未找到服务器指标数据");
            }

            // 计算健康评分
            ServerHealthScoreService.HealthScoreResult healthScore = 
                healthScoreService.calculateHealthScore(latestMetrics);

            return ReturnResult.ok(healthScore);
        } catch (Exception e) {
            return ReturnResult.error("获取服务器健康评分失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有服务器的健康评分概览
     *
     * @return 健康评分概览
     */
    @GetMapping("overview")
    @Operation(summary = "获取所有服务器的健康评分概览")
    public ReturnResult<Map<String, Object>> getHealthOverview() {
        try {
            Map<String, Object> overview = new HashMap<>();
            
            // 获取所有在线服务器
            List<Integer> serverIds = serverService.getAllOnlineServerIds();
            
            if (serverIds.isEmpty()) {
                overview.put("totalServers", 0);
                overview.put("healthDistribution", Map.of());
                overview.put("averageScore", 0);
                overview.put("serverScores", List.of());
                return ReturnResult.ok(overview);
            }

            // 计算每个服务器的健康评分
            List<Map<String, Object>> serverScores = serverIds.stream()
                .map(serverId -> {
                    try {
                        MonitorSysGenServerMetrics metrics = metricsService.getLatestMetricsDirectly(serverId);
                        if (metrics != null) {
                            ServerHealthScoreService.HealthScoreResult healthScore = 
                                healthScoreService.calculateHealthScore(metrics);
                            
                            Map<String, Object> serverScore = new HashMap<>();
                            serverScore.put("serverId", serverId);
                            serverScore.put("serverName", serverService.getServerName(serverId));
                            serverScore.put("score", healthScore.getTotalScore());
                            serverScore.put("level", healthScore.getHealthLevel());
                            serverScore.put("description", healthScore.getHealthDescription());
                            serverScore.put("calculateTime", healthScore.getCalculateTime());
                            return serverScore;
                        }
                    } catch (Exception e) {
                        // 记录错误但继续处理其他服务器
                        Map<String, Object> errorScore = new HashMap<>();
                        errorScore.put("serverId", serverId);
                        errorScore.put("serverName", serverService.getServerName(serverId));
                        errorScore.put("score", 0);
                        errorScore.put("level", "unknown");
                        errorScore.put("description", "评分计算失败");
                        return errorScore;
                    }
                    return null;
                })
                .filter(score -> score != null)
                .collect(Collectors.toList());

            // 统计健康等级分布
            Map<String, Long> healthDistribution = serverScores.stream()
                .collect(Collectors.groupingBy(
                    score -> (String) score.get("level"),
                    Collectors.counting()
                ));

            // 计算平均分
            double averageScore = serverScores.stream()
                .mapToInt(score -> (Integer) score.get("score"))
                .average()
                .orElse(0.0);

            overview.put("totalServers", serverScores.size());
            overview.put("healthDistribution", healthDistribution);
            overview.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
            overview.put("serverScores", serverScores);
            overview.put("updateTime", LocalDateTime.now());

            return ReturnResult.ok(overview);
        } catch (Exception e) {
            return ReturnResult.error("获取健康评分概览失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务器健康趋势
     *
     * @param serverId 服务器ID
     * @param hours    时间范围（小时）
     * @return 健康趋势数据
     */
    @GetMapping("trend/{serverId}")
    @Operation(summary = "获取服务器健康趋势")
    public ReturnResult<Map<String, Object>> getHealthTrend(
            @Parameter(name = "服务器ID") @PathVariable Integer serverId,
            @Parameter(name = "时间范围（小时）") @RequestParam(defaultValue = "24") Integer hours) {
        try {
            Map<String, Object> trend = new HashMap<>();
            
            // 获取指定时间范围内的指标数据
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(hours);
            
            List<MonitorSysGenServerMetrics> metricsList = 
                metricsService.getMetricsByTimeRange(serverId, startTime, endTime);

            if (metricsList.isEmpty()) {
                trend.put("serverId", serverId);
                trend.put("timeRange", hours + "小时");
                trend.put("dataPoints", List.of());
                trend.put("averageScore", 0);
                trend.put("minScore", 0);
                trend.put("maxScore", 0);
                return ReturnResult.ok(trend);
            }

            // 计算每个时间点的健康评分
            List<Map<String, Object>> dataPoints = metricsList.stream()
                .map(metrics -> {
                    ServerHealthScoreService.HealthScoreResult healthScore = 
                        healthScoreService.calculateHealthScore(metrics);
                    
                    Map<String, Object> point = new HashMap<>();
                    point.put("time", metrics.getMonitorSysGenServerMetricsCollectTime());
                    point.put("score", healthScore.getTotalScore());
                    point.put("level", healthScore.getHealthLevel());
                    return point;
                })
                .collect(Collectors.toList());

            // 计算统计信息
            List<Integer> scores = dataPoints.stream()
                .mapToInt(point -> (Integer) point.get("score"))
                .boxed()
                .collect(Collectors.toList());

            double averageScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            int minScore = scores.stream().mapToInt(Integer::intValue).min().orElse(0);
            int maxScore = scores.stream().mapToInt(Integer::intValue).max().orElse(0);

            trend.put("serverId", serverId);
            trend.put("serverName", serverService.getServerName(serverId));
            trend.put("timeRange", hours + "小时");
            trend.put("dataPoints", dataPoints);
            trend.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
            trend.put("minScore", minScore);
            trend.put("maxScore", maxScore);
            trend.put("totalDataPoints", dataPoints.size());

            return ReturnResult.ok(trend);
        } catch (Exception e) {
            return ReturnResult.error("获取健康趋势失败: " + e.getMessage());
        }
    }

    /**
     * 获取健康评分详细分析
     *
     * @param serverId 服务器ID
     * @return 详细分析结果
     */
    @GetMapping("analysis/{serverId}")
    @Operation(summary = "获取健康评分详细分析")
    public ReturnResult<Map<String, Object>> getHealthAnalysis(
            @Parameter(name = "服务器ID") @PathVariable Integer serverId) {
        try {
            // 获取最新的指标数据
            MonitorSysGenServerMetrics latestMetrics = metricsService.getLatestMetricsDirectly(serverId);
            if (latestMetrics == null) {
                return ReturnResult.error("未找到服务器指标数据");
            }

            // 计算健康评分
            ServerHealthScoreService.HealthScoreResult healthScore = 
                healthScoreService.calculateHealthScore(latestMetrics);

            Map<String, Object> analysis = new HashMap<>();
            analysis.put("serverId", serverId);
            analysis.put("serverName", serverService.getServerName(serverId));
            analysis.put("healthScore", healthScore);
            
            // 添加指标详细信息
            Map<String, Object> metricsDetail = new HashMap<>();
            metricsDetail.put("cpu", Map.of(
                "usage", latestMetrics.getMonitorSysGenServerMetricsCpuUsage(),
                "cores", latestMetrics.getMonitorSysGenServerMetricsCpuCores()
            ));
            metricsDetail.put("memory", Map.of(
                "usage", latestMetrics.getMonitorSysGenServerMetricsMemoryUsage(),
                "total", latestMetrics.getMonitorSysGenServerMetricsMemoryTotal(),
                "used", latestMetrics.getMonitorSysGenServerMetricsMemoryUsed()
            ));
            metricsDetail.put("disk", Map.of(
                "usage", latestMetrics.getMonitorSysGenServerMetricsDiskUsage(),
                "total", latestMetrics.getMonitorSysGenServerMetricsDiskTotal(),
                "used", latestMetrics.getMonitorSysGenServerMetricsDiskUsed()
            ));
            metricsDetail.put("network", Map.of(
                "in", latestMetrics.getMonitorSysGenServerMetricsNetworkIn(),
                "out", latestMetrics.getMonitorSysGenServerMetricsNetworkOut()
            ));
            metricsDetail.put("system", Map.of(
                "uptime", latestMetrics.getMonitorSysGenServerMetricsUptime(),
                "responseTime", latestMetrics.getMonitorSysGenServerMetricsResponseTime(),
                "osInfo", latestMetrics.getMonitorSysGenServerMetricsOsInfo()
            ));
            
            analysis.put("metricsDetail", metricsDetail);
            analysis.put("collectTime", latestMetrics.getMonitorSysGenServerMetricsCollectTime());

            return ReturnResult.ok(analysis);
        } catch (Exception e) {
            return ReturnResult.error("获取健康分析失败: " + e.getMessage());
        }
    }

    /**
     * 获取健康等级说明
     *
     * @return 健康等级说明
     */
    @GetMapping("levels")
    @Operation(summary = "获取健康等级说明")
    public ReturnResult<Map<String, Object>> getHealthLevels() {
        Map<String, Object> levels = new HashMap<>();
        
        Map<String, Object> excellent = new HashMap<>();
        excellent.put("name", "优秀");
        excellent.put("score", "90-100");
        excellent.put("color", "#67c23a");
        excellent.put("description", "服务器运行状态优秀，各项指标表现良好");
        
        Map<String, Object> good = new HashMap<>();
        good.put("name", "良好");
        good.put("score", "75-89");
        good.put("color", "#409eff");
        good.put("description", "服务器运行状态良好，整体表现正常");
        
        Map<String, Object> warning = new HashMap<>();
        warning.put("name", "警告");
        warning.put("score", "60-74");
        warning.put("color", "#e6a23c");
        warning.put("description", "服务器运行状态一般，部分指标需要关注");
        
        Map<String, Object> critical = new HashMap<>();
        critical.put("name", "危险");
        critical.put("score", "0-59");
        critical.put("color", "#f56c6c");
        critical.put("description", "服务器运行状态较差，需要及时处理");
        
        levels.put("excellent", excellent);
        levels.put("good", good);
        levels.put("warning", warning);
        levels.put("critical", critical);
        
        return ReturnResult.ok(levels);
    }
}
