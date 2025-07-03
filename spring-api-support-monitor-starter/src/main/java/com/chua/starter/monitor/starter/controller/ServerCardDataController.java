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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ReturnResult<Map<String, Object>> getServerCardData(
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        try {
            // 获取服务器基本信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 获取最新指标数据
            MonitorSysGenServerMetrics metrics = metricsService.getLatestMetricsDirectly(serverId);
            
            // 获取最新延迟数据
            ReturnResult<Long> latencyResult = latencyService.getServerLatencyFromRedis(serverId);
            Long latency = latencyResult.isOk() ? latencyResult.getData() : null;

            // 构建卡片数据
            Map<String, Object> cardData = buildServerCardData(server, metrics, latency);

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
            Map<Integer, Map<String, Object>> result = new HashMap<>();

            // 批量获取延迟数据
            ReturnResult<Map<Integer, Long>> latencyResult = latencyService.getBatchServerLatency(serverIds);
            Map<Integer, Long> latencyMap = latencyResult.isOk() ? latencyResult.getData() : new HashMap<>();

            for (Integer serverId : serverIds) {
                try {
                    // 获取服务器基本信息
                    MonitorSysGenServer server = serverService.getById(serverId);
                    if (server == null) {
                        continue;
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
                    // 继续处理其他服务器
                }
            }

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
    public ReturnResult<Map<Integer, Map<String, Object>>> getAllOnlineServerCardData() {
        try {
            // 获取所有在线服务器
            List<MonitorSysGenServer> onlineServers = serverService.list().stream()
                .filter(server -> server.getMonitorSysGenServerStatus() != null &&
                                server.getMonitorSysGenServerStatus() == 1)
                .toList();

            if (onlineServers.isEmpty()) {
                return ReturnResult.ok(new HashMap<>());
            }

            // 提取服务器ID列表
            List<Integer> serverIds = onlineServers.stream()
                .map(MonitorSysGenServer::getMonitorSysGenServerId)
                .toList();

            // 批量获取卡片数据
            return getBatchServerCardData(serverIds);

        } catch (Exception e) {
            log.error("获取所有在线服务器卡片数据失败", e);
            return ReturnResult.error("获取所有在线服务器卡片数据失败: " + e.getMessage());
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
            cpuData.put("loadAverage", metrics.getMonitorSysGenServerMetricsCpuLoadAverage());
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
            diskData.put("readSpeed", metrics.getMonitorSysGenServerMetricsDiskReadSpeed());
            diskData.put("writeSpeed", metrics.getMonitorSysGenServerMetricsDiskWriteSpeed());
            cardData.put("disk", diskData);

            // 网络数据
            Map<String, Object> networkData = new HashMap<>();
            networkData.put("uploadSpeed", metrics.getMonitorSysGenServerMetricsNetworkUploadSpeed());
            networkData.put("downloadSpeed", metrics.getMonitorSysGenServerMetricsNetworkDownloadSpeed());
            networkData.put("totalUpload", metrics.getMonitorSysGenServerMetricsNetworkTotalUpload());
            networkData.put("totalDownload", metrics.getMonitorSysGenServerMetricsNetworkTotalDownload());
            cardData.put("network", networkData);

            // 系统信息
            Map<String, Object> systemData = new HashMap<>();
            systemData.put("uptime", metrics.getMonitorSysGenServerMetricsUptime());
            systemData.put("processCount", metrics.getMonitorSysGenServerMetricsProcessCount());
            systemData.put("threadCount", metrics.getMonitorSysGenServerMetricsThreadCount());
            systemData.put("responseTime", metrics.getMonitorSysGenServerMetricsResponseTime());
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
}
