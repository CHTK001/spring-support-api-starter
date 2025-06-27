package com.chua.starter.monitor.starter.service;

import com.chua.common.support.json.Json;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器指标WebSocket推送服务
 * 提供WebSocket推送功能，不包含定时任务
 * 定时任务由task包中的相关类负责
 *
 * @author CH
 * @since 2024/12/20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ServerMetricsWebSocketService {

    private final SocketSessionTemplate socketSessionTemplate;
    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerMetricsService metricsService;
    private final List<ServerConnectionService> serverConnectionServices;

    /**
     * WebSocket主题常量
     */
    private static final String TOPIC_SERVER_METRICS = "gen/server";

    /**
     * 推送实时指标数据
     * 由task包中的定时任务调用
     */
    public void pushRealTimeMetrics() {
        try {
            List<MonitorSysGenServer> servers = serverService.list().stream()
                    .filter(server -> server.getMonitorSysGenServerMonitorEnabled() != null &&
                            server.getMonitorSysGenServerMonitorEnabled() == 1)
                    .toList();

            for (MonitorSysGenServer server : servers) {
                try {
                    MonitorSysGenServerMetrics metrics = collectLatestMetrics(server);
                    if (metrics != null) {
                        pushServerMetrics(server, metrics);
                    }
                } catch (Exception e) {
                    log.error("推送服务器 {} 指标数据失败: {}", 
                            server.getMonitorSysGenServerName(), e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            log.error("推送实时指标数据失败", e);
        }
    }

    /**
     * 推送服务器状态汇总
     * 由task包中的定时任务调用
     */
    public void pushServerStatusSummary() {
        try {
            Map<String, Object> summary = createServerStatusSummary();

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("server_status_summary")
                    .data(summary)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_SERVER_METRICS, Json.toJson(message));
            log.debug("推送服务器状态汇总完成");

        } catch (Exception e) {
            log.error("推送服务器状态汇总失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送性能趋势数据
     * 由task包中的定时任务调用
     */
    public void pushPerformanceTrends() {
        try {
            List<MonitorSysGenServer> servers = serverService.list();

            for (MonitorSysGenServer server : servers) {
                try {
                    pushServerTrends(server.getMonitorSysGenServerId());
                } catch (Exception e) {
                    log.error("推送服务器 {} 趋势数据失败: {}", 
                            server.getMonitorSysGenServerName(), e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            log.error("推送性能趋势数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送告警信息
     * 由task包中的定时任务调用
     */
    public void pushAlertMessages() {
        try {
            List<MonitorSysGenServer> servers = serverService.list();

            for (MonitorSysGenServer server : servers) {
                try {
                    List<Map<String, Object>> alerts = checkServerAlerts(server);

                    if (!alerts.isEmpty()) {
                        ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                                .messageType("server_alerts")
                                .serverId(server.getMonitorSysGenServerId())
                                .serverName(server.getMonitorSysGenServerName())
                                .data(Map.of("alerts", alerts))
                                .timestamp(System.currentTimeMillis())
                                .build();

                        socketSessionTemplate.send(TOPIC_SERVER_METRICS, Json.toJson(message));
                    }

                } catch (Exception e) {
                    log.error("推送服务器 {} 告警信息失败: {}",
                            server.getMonitorSysGenServerName(), e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            log.error("推送告警信息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送连接状态统计
     * 由task包中的定时任务调用
     */
    public void pushConnectionStatistics() {
        try {
            Map<String, Object> statistics = createConnectionStatistics();

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("connection_statistics")
                    .data(statistics)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_SERVER_METRICS, Json.toJson(message));
            log.debug("推送连接状态统计完成");

        } catch (Exception e) {
            log.error("推送连接状态统计失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送健康状态报告
     * 由task包中的定时任务调用
     */
    public void pushHealthStatus() {
        try {
            Map<String, Object> healthStatus = createHealthStatusReport();

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("health_status")
                    .data(healthStatus)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_SERVER_METRICS, Json.toJson(message));
            log.debug("推送健康状态报告完成");

        } catch (Exception e) {
            log.error("推送健康状态报告失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送每日健康报告
     * 由task包中的定时任务调用
     */
    public void pushDailyHealthReport() {
        try {
            Map<String, Object> dailyReport = createDailyHealthReport();

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("daily_health_report")
                    .data(dailyReport)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_SERVER_METRICS, Json.toJson(message));
            log.info("推送每日健康报告完成");

        } catch (Exception e) {
            log.error("推送每日健康报告失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送服务器指标数据
     */
    private void pushServerMetrics(MonitorSysGenServer server, MonitorSysGenServerMetrics metrics) {
        try {
            Map<String, Object> metricsData = convertMetricsToMap(metrics);
            metricsData.put("serverName", server.getMonitorSysGenServerName());
            metricsData.put("serverHost", server.getMonitorSysGenServerHost());
            metricsData.put("serverPort", server.getMonitorSysGenServerPort());

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("server_metrics")
                    .serverId(server.getMonitorSysGenServerId())
                    .serverName(server.getMonitorSysGenServerName())
                    .data(metricsData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_SERVER_METRICS, Json.toJson(message));
            log.debug("推送服务器 {} 指标数据完成", server.getMonitorSysGenServerName());

        } catch (Exception e) {
            log.error("推送服务器指标数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送服务器趋势数据
     * 由task包中的定时任务调用
     */
    public void pushServerTrends(Integer serverId) {
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                log.warn("服务器不存在: {}", serverId);
                return;
            }

            // 获取最近24小时的指标数据
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            
            var historyResult = metricsService.getHistoryMetrics(serverId, startTime, endTime);
            if (historyResult == null || historyResult.getData() == null) {
                log.warn("服务器 {} 没有历史指标数据", server.getMonitorSysGenServerName());
                return;
            }

            List<MonitorSysGenServerMetrics> historyMetrics = historyResult.getData();
            
            Map<String, Object> trendsData = new HashMap<>();
            trendsData.put("serverId", serverId);
            trendsData.put("serverName", server.getMonitorSysGenServerName());
            trendsData.put("timeRange", Map.of("start", startTime, "end", endTime));
            
            // 提取趋势数据
            trendsData.put("cpuTrend", extractCpuTrend(historyMetrics));
            trendsData.put("memoryTrend", extractMemoryTrend(historyMetrics));
            trendsData.put("diskTrend", extractDiskTrend(historyMetrics));
            trendsData.put("networkTrend", extractNetworkTrend(historyMetrics));

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("server_trends")
                    .serverId(serverId)
                    .serverName(server.getMonitorSysGenServerName())
                    .data(trendsData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_SERVER_METRICS, Json.toJson(message));
            log.debug("推送服务器 {} 趋势数据完成", server.getMonitorSysGenServerName());

        } catch (Exception e) {
            log.error("推送服务器趋势数据失败: serverId={}", serverId, e);
        }
    }

    /**
     * 收集最新指标数据
     * 支持三种数据获取方式：本地上传、接口推送、prometheus
     */
    private MonitorSysGenServerMetrics collectLatestMetrics(MonitorSysGenServer server) {
        try {
            // 根据服务器配置的数据上报方式选择不同的收集策略
            String reportMethod = getServerReportMethod(server);

            switch (reportMethod.toLowerCase()) {
                case "prometheus":
                    return collectMetricsFromPrometheus(server);
                case "api":
                case "push":
                    return collectMetricsFromDatabase(server); // 接口推送的数据已存储在数据库中
                case "local":
                case "ssh":
                case "rdp":
                case "vnc":
                default:
                    return collectMetricsFromConnection(server); // 本地上传方式
            }
        } catch (Exception e) {
            log.error("收集服务器 {} 指标数据失败: {}",
                    server.getMonitorSysGenServerName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取服务器数据上报方式
     */
    private String getServerReportMethod(MonitorSysGenServer server) {
        // 检查服务器是否配置了特定的上报方式
        String reportMethod = server.getMonitorSysGenServerDataReportMethod();
        if (reportMethod != null && !reportMethod.trim().isEmpty()) {
            return reportMethod;
        }

        // 根据协议类型推断上报方式
        String protocol = server.getMonitorSysGenServerProtocol();
        if ("PROMETHEUS".equalsIgnoreCase(protocol)) {
            return "prometheus";
        } else if ("API".equalsIgnoreCase(protocol) || "HTTP".equalsIgnoreCase(protocol)) {
            return "api";
        } else {
            return "local"; // SSH, RDP, VNC 等使用本地连接方式
        }
    }

    /**
     * 从 Prometheus 收集指标数据
     */
    private MonitorSysGenServerMetrics collectMetricsFromPrometheus(MonitorSysGenServer server) {
        try {
            log.debug("从 Prometheus 收集服务器 {} 指标数据", server.getMonitorSysGenServerName());

            // 创建基本的指标对象，实际数据通过 PrometheusReportHandler 直接推送
            MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();
            metrics.setMonitorSysGenServerId(server.getMonitorSysGenServerId());
            metrics.setMonitorSysGenServerMetricsCollectTime(java.time.LocalDateTime.now());
            metrics.setMonitorSysGenServerMetricsStatus(1); // 假设在线

            return metrics;
        } catch (Exception e) {
            log.error("从 Prometheus 收集指标失败", e);
            return null;
        }
    }

    /**
     * 从数据库收集指标数据（接口推送方式）
     */
    private MonitorSysGenServerMetrics collectMetricsFromDatabase(MonitorSysGenServer server) {
        try {
            // 获取最新的指标数据（客户端推送的数据已存储在数据库中）
            var latestMetrics = metricsService.getLatestMetrics(server.getMonitorSysGenServerId());
            if (latestMetrics != null && latestMetrics.getData() != null) {
                log.debug("从数据库获取服务器 {} 最新指标数据", server.getMonitorSysGenServerName());
                return latestMetrics.getData();
            }

            log.warn("服务器 {} 没有找到最新的指标数据", server.getMonitorSysGenServerName());
            return null;
        } catch (Exception e) {
            log.error("从数据库收集指标失败", e);
            return null;
        }
    }

    /**
     * 从连接服务收集指标数据（本地上传方式）
     */
    private MonitorSysGenServerMetrics collectMetricsFromConnection(MonitorSysGenServer server) {
        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                log.warn("不支持的协议类型: {}", server.getMonitorSysGenServerProtocol());
                return null;
            }

            log.debug("通过连接服务收集服务器 {} 指标数据", server.getMonitorSysGenServerName());
            return connectionService.collectMetrics(server);
        } catch (Exception e) {
            log.error("通过连接服务收集指标失败", e);
            return null;
        }
    }

    /**
     * 获取连接服务
     */
    private ServerConnectionService getConnectionService(String protocol) {
        return serverConnectionServices.stream()
                .filter(service -> service.getProtocol().equalsIgnoreCase(protocol))
                .findFirst()
                .orElse(null);
    }

    /**
     * 将指标对象转换为Map
     */
    private Map<String, Object> convertMetricsToMap(MonitorSysGenServerMetrics metrics) {
        Map<String, Object> data = new HashMap<>();

        data.put("serverId", metrics.getMonitorSysGenServerId());
        data.put("collectTime", metrics.getMonitorSysGenServerMetricsCollectTime());
        data.put("status", metrics.getMonitorSysGenServerMetricsStatus());
        data.put("cpuUsage", metrics.getMonitorSysGenServerMetricsCpuUsage());
        data.put("memoryUsage", metrics.getMonitorSysGenServerMetricsMemoryUsage());
        data.put("memoryTotal", metrics.getMonitorSysGenServerMetricsMemoryTotal());
        data.put("memoryUsed", metrics.getMonitorSysGenServerMetricsMemoryUsed());
        data.put("diskUsage", metrics.getMonitorSysGenServerMetricsDiskUsage());
        data.put("diskTotal", metrics.getMonitorSysGenServerMetricsDiskTotal());
        data.put("diskUsed", metrics.getMonitorSysGenServerMetricsDiskUsed());
        data.put("networkIn", metrics.getMonitorSysGenServerMetricsNetworkIn());
        data.put("networkOut", metrics.getMonitorSysGenServerMetricsNetworkOut());
        data.put("responseTime", metrics.getMonitorSysGenServerMetricsResponseTime());
        data.put("osInfo", metrics.getMonitorSysGenServerMetricsOsInfo());

        return data;
    }
