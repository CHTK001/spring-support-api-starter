package com.chua.starter.monitor.service;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.socketio.support.constant.SocketTopics;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import com.chua.starter.monitor.utils.LocalSystemDataCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.InetAddress;
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
    private final MonitorSysGenServerSettingService serverSettingService;
    private final MonitorSysGenServerMetricsService metricsService;
    private final List<ServerConnectionService> serverConnectionServices;
    private final LocalSystemDataCollector localSystemDataCollector;
    private final NetworkSpeedCalculatorService networkSpeedCalculatorService;


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

            socketSessionTemplate.send(SocketTopics.Monitor.Server.METRICS, Json.toJson(message));
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
                        Map<String, Object> data = new HashMap<>();
                        data.put("alerts", alerts);
                        ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                                .messageType("server_alerts")
                                .serverId(server.getMonitorSysGenServerId())
                                .serverName(server.getMonitorSysGenServerName())
                                .data(data)
                                .timestamp(System.currentTimeMillis())
                                .build();

                        socketSessionTemplate.send(SocketTopics.Monitor.Server.METRICS, Json.toJson(message));
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

            socketSessionTemplate.send(SocketTopics.Monitor.Server.METRICS, Json.toJson(message));
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

            socketSessionTemplate.send(SocketTopics.Monitor.Server.METRICS, Json.toJson(message));
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

            socketSessionTemplate.send(SocketTopics.Monitor.Server.METRICS, Json.toJson(message));
            log.info("推送每日健康报告完成");

        } catch (Exception e) {
            log.error("推送每日健康报告失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送单个服务器的指标数据
     * 由设备上报服务调用
     */
    public void pushSingleServerMetrics(MonitorSysGenServer server, MonitorSysGenServerMetrics metrics) {
        try {
            // 兼容空指标：在空时主动收集一份再推送，避免NPE
            if (metrics == null) {
                metrics = collectLatestMetrics(server);
                if (metrics == null) {
                    log.warn("服务器 {} 指标为空，跳过推送", server.getMonitorSysGenServerName());
                    return;
                }
            }
            pushServerMetrics(server, metrics);
        } catch (Exception e) {
            log.error("推送服务器 {} 指标失败: {}", server.getMonitorSysGenServerName(), e.getMessage(), e);
        }
    }

    /**
     * 重载：根据配置优先收集并推送（尽量避免额外DB访问）
     */
    public void pushSingleServerMetrics(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            MonitorSysGenServerMetrics metrics = collectLatestMetrics(server, setting);
            if (metrics == null) {
                log.warn("服务器 {} 指标为空，跳过推送(基于配置)", server.getMonitorSysGenServerName());
                return;
            }
            pushServerMetrics(server, metrics);
        } catch (Exception e) {
            log.error("推送服务器 {} 指标失败(基于配置): {}", server.getMonitorSysGenServerName(), e.getMessage(), e);
        }
    }

    /**
     * 根据服务器配置推送单个服务器的指标数据
     * 由DataPushTask调用，支持基于配置的动态推送
     */
    public void pushSingleServerMetricsByConfig(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            log.debug("开始根据配置推送服务器 {} 指标数据", server.getMonitorSysGenServerName());

            // 收集最新指标数据（优先使用已传入的配置，避免数据库访问）
            MonitorSysGenServerMetrics metrics = collectLatestMetrics(server, setting);
            if (metrics != null) {
                pushServerMetrics(server, metrics);
                log.debug("成功推送服务器 {} 指标数据，收集频率: {}秒",
                        server.getMonitorSysGenServerName(),
                        setting.getMonitorSysGenServerSettingDataCollectionFrequency());
            } else {
                log.warn("服务器 {} 指标数据收集失败，跳过推送", server.getMonitorSysGenServerName());
            }

        } catch (Exception e) {
            log.error("根据配置推送服务器 {} 指标数据失败: {}",
                    server.getMonitorSysGenServerName(), e.getMessage(), e);
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

            socketSessionTemplate.send(SocketTopics.Monitor.Server.METRICS, Json.toJson(message));
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

            ReturnResult<List<MonitorSysGenServerMetrics>> historyResult = metricsService.getHistoryMetrics(serverId, startTime, endTime);
            if (historyResult == null || historyResult.getData() == null) {
                log.warn("服务器 {} 没有历史指标数据", server.getMonitorSysGenServerName());
                return;
            }

            List<MonitorSysGenServerMetrics> historyMetrics = historyResult.getData();

            Map<String, Object> trendsData = new HashMap<>();
            trendsData.put("serverId", serverId);
            trendsData.put("serverName", server.getMonitorSysGenServerName());
            Map<String, Object> timeRange = new HashMap<>();
            timeRange.put("start", startTime);
            timeRange.put("end", endTime);
            trendsData.put("timeRange", timeRange);

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

            socketSessionTemplate.send(SocketTopics.Monitor.Server.METRICS, Json.toJson(message));
            log.debug("推送服务器 {} 趋势数据完成", server.getMonitorSysGenServerName());

        } catch (Exception e) {
            log.error("推送服务器趋势数据失败: serverId={}", serverId, e);
        }
    }

    /**
     * 收集最新指标数据
     * 支持三种数据获取方式：本地上传、接口推送、prometheus
     */
    public MonitorSysGenServerMetrics collectLatestMetrics(MonitorSysGenServer server) {
        try {
            // 根据服务器配置的数据上报方式选择不同的收集策略
            String reportMethod = getServerReportMethod(server);

            switch (reportMethod.toLowerCase()) {
                case "prometheus":
                    return collectMetricsFromPrometheus(server);
                case "api":
                    return collectMetricsFromDatabase(server); // 接口推送的数据已存储在数据库中
                case "local":
                    return collectMetricsFromLocal(server); // 本地采集方式，直接调用oshi
                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("收集服务器 {} 指标数据失败: {}",
                    server.getMonitorSysGenServerName(), e.getMessage(), e);
            return null;
        }
    }
    /**
     * 重载：收集最新指标（尽量避免DB），优先根据传入的配置决定上报方式
     */
    public MonitorSysGenServerMetrics collectLatestMetrics(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            String reportMethod = null;
            if (setting != null && setting.getMonitorSysGenServerSettingDataReportMethod() != null) {
                reportMethod = setting.getMonitorSysGenServerSettingDataReportMethod();
            } else {
                reportMethod = getServerReportMethod(server);
            }
            if (reportMethod == null) {
                reportMethod = "local";
            }
            switch (reportMethod.toLowerCase()) {
                case "prometheus":
                    return collectMetricsFromPrometheus(server);
                case "api":
                    return collectMetricsFromDatabase(server);
                case "local":
                default:
                    return collectMetricsFromLocal(server);
            }
        } catch (Exception e) {
            log.error("收集服务器 {} 指标数据失败(重载): {}", server.getMonitorSysGenServerName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取服务器数据上报方式
     */
    private String getServerReportMethod(MonitorSysGenServer server) {
        try {
            // 从配置表获取上报方式
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

            if (settingResult.isOk() && settingResult.getData() != null) {
                MonitorSysGenServerSetting setting = settingResult.getData();
                String reportMethod = setting.getMonitorSysGenServerSettingDataReportMethod();
                if (reportMethod != null && !reportMethod.trim().isEmpty()) {
                    return reportMethod;
                }
            }
        } catch (Exception e) {
            log.warn("获取服务器上报配置失败: serverId={}, error={}",
                server.getMonitorSysGenServerId(), e.getMessage());
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
            ReturnResult<MonitorSysGenServerMetrics> latestMetrics = metricsService.getLatestMetrics(server.getMonitorSysGenServerId());
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
     * 从本地系统收集指标数据（本地采集方式）
     * 直接使用oshi库收集本地系统指标
     */
    private MonitorSysGenServerMetrics collectMetricsFromLocal(MonitorSysGenServer server) {
        try {
            log.debug("开始收集本地服务器 {} 系统指标", server.getMonitorSysGenServerName());

            MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();

            // 设置基本信息
            metrics.setMonitorSysGenServerId(server.getMonitorSysGenServerId());
            metrics.setMonitorSysGenServerMetricsCollectTime(LocalDateTime.now());

            // 使用LocalSystemDataCollector收集系统数据
            Map<String, Object> systemData = localSystemDataCollector.collectSystemData();

            // 提取CPU信息
            if (systemData.containsKey("cpuUsage")) {
                Double cpuUsage = (Double) systemData.get("cpuUsage");
                metrics.setMonitorSysGenServerMetricsCpuUsage(BigDecimal.valueOf(cpuUsage));
            }

            // 提取CPU核心数
            if (systemData.containsKey("cpuLogicalProcessors")) {
                Integer cpuCores = (Integer) systemData.get("cpuLogicalProcessors");
                metrics.setMonitorSysGenServerMetricsCpuCores(cpuCores);
            }

            // 提取CPU负载
            if (systemData.containsKey("cpuLoad")) {
                Double cpuLoad = (Double) systemData.get("cpuLoad");
                metrics.setMonitorSysGenServerMetricsCpuLoad1m(BigDecimal.valueOf(cpuLoad));
            }

            // 提取内存信息
            if (systemData.containsKey("totalMemory")) {
                Long totalMemory = (Long) systemData.get("totalMemory");
                metrics.setMonitorSysGenServerMetricsMemoryTotal(totalMemory / (1024 * 1024 * 1024)); // 转换为GB
            }

            if (systemData.containsKey("usedMemory")) {
                Long usedMemory = (Long) systemData.get("usedMemory");
                metrics.setMonitorSysGenServerMetricsMemoryUsed(usedMemory / (1024 * 1024 * 1024)); // 转换为GB
            }

            if (systemData.containsKey("availableMemory")) {
                Long availableMemory = (Long) systemData.get("availableMemory");
                metrics.setMonitorSysGenServerMetricsMemoryFree(availableMemory / (1024 * 1024 * 1024)); // 转换为GB
            }

            if (systemData.containsKey("memoryUsagePercent")) {
                Double memoryUsage = (Double) systemData.get("memoryUsagePercent");
                metrics.setMonitorSysGenServerMetricsMemoryUsage(BigDecimal.valueOf(memoryUsage));
            }

            // 提取磁盘信息
            if (systemData.containsKey("diskStores")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> diskStores = (List<Map<String, Object>>) systemData.get("diskStores");

                if (!diskStores.isEmpty()) {
                    // 取第一个磁盘的信息作为主要磁盘信息
                    Map<String, Object> mainDisk = diskStores.get(0);

                    if (mainDisk.containsKey("totalSpace") && mainDisk.containsKey("freeSpace")) {
                        Long totalSpace = (Long) mainDisk.get("totalSpace");
                        Long freeSpace = (Long) mainDisk.get("freeSpace");

                        double diskUsage = totalSpace > 0 ?
                            (double)(totalSpace - freeSpace) / totalSpace * 100 : 0;

                        // 转换为GB
                        metrics.setMonitorSysGenServerMetricsDiskTotal(totalSpace / (1024 * 1024 * 1024));
                        metrics.setMonitorSysGenServerMetricsDiskFree(freeSpace / (1024 * 1024 * 1024));
                        metrics.setMonitorSysGenServerMetricsDiskUsed((totalSpace - freeSpace) / (1024 * 1024 * 1024));
                        metrics.setMonitorSysGenServerMetricsDiskUsage(BigDecimal.valueOf(diskUsage));
                    }
                }
            }

            // 设置网络流量（暂时设为0，可以后续扩展）
            metrics.setMonitorSysGenServerMetricsNetworkIn(0L);
            metrics.setMonitorSysGenServerMetricsNetworkOut(0L);

            // 设置状态为在线
            metrics.setMonitorSysGenServerMetricsStatus(1);

            // 设置操作系统信息
            if (systemData.containsKey("osName")) {
                Map<String, String> osInfo = new HashMap<>();
                osInfo.put("osName", systemData.get("osName").toString());
                osInfo.put("osArch", systemData.get("osArch").toString());
                osInfo.put("osVersion", systemData.get("osVersion").toString());
                osInfo.put("hostname", InetAddress.getByName(server.getMonitorSysGenServerHost()).getHostName());
                metrics.setMonitorSysGenServerMetricsOsInfo(Json.toJson(osInfo));
            }

            // 设置响应时间（本地采集响应时间很快）
            metrics.setMonitorSysGenServerMetricsResponseTime(10L);

            log.debug("本地系统指标收集完成: CPU={}%, Memory={}%, Disk={}%",
                    metrics.getMonitorSysGenServerMetricsCpuUsage(),
                    metrics.getMonitorSysGenServerMetricsMemoryUsage(),
                    metrics.getMonitorSysGenServerMetricsDiskUsage());

            return metrics;

        } catch (Exception e) {
            log.error("收集本地系统指标失败: serverId={}", server.getMonitorSysGenServerId(), e);
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
        data.put("responseTime", metrics.getMonitorSysGenServerMetricsResponseTime());

        // CPU指标
        Map<String, Object> cpu = new HashMap<>();
        cpu.put("usage", metrics.getMonitorSysGenServerMetricsCpuUsage());
        cpu.put("cores", metrics.getMonitorSysGenServerMetricsCpuCores());
        cpu.put("load1m", metrics.getMonitorSysGenServerMetricsCpuLoad1m());
        cpu.put("load5m", metrics.getMonitorSysGenServerMetricsCpuLoad5m());
        cpu.put("load15m", metrics.getMonitorSysGenServerMetricsCpuLoad15m());
        data.put("cpu", cpu);

        // 内存指标
        Map<String, Object> memory = new HashMap<>();
        memory.put("usage", metrics.getMonitorSysGenServerMetricsMemoryUsage());
        memory.put("total", metrics.getMonitorSysGenServerMetricsMemoryTotal());
        memory.put("used", metrics.getMonitorSysGenServerMetricsMemoryUsed());
        memory.put("free", metrics.getMonitorSysGenServerMetricsMemoryFree());
        data.put("memory", memory);

        // 磁盘指标
        Map<String, Object> disk = new HashMap<>();
        disk.put("usage", metrics.getMonitorSysGenServerMetricsDiskUsage());
        disk.put("total", metrics.getMonitorSysGenServerMetricsDiskTotal());
        disk.put("used", metrics.getMonitorSysGenServerMetricsDiskUsed());
        disk.put("free", metrics.getMonitorSysGenServerMetricsDiskFree());

        // 添加磁盘分区信息
        try {
            Map<String, Object> systemData = localSystemDataCollector.collectSystemData();
            if (systemData.containsKey("diskPartitions")) {
                disk.put("partitions", systemData.get("diskPartitions"));
            }
        } catch (Exception e) {
            log.debug("获取磁盘分区信息失败: {}", e.getMessage());
        }

        data.put("disk", disk);

        // 网络指标（包含速率计算）
        Map<String, Object> network = new HashMap<>();
        Long networkIn = metrics.getMonitorSysGenServerMetricsNetworkIn();
        Long networkOut = metrics.getMonitorSysGenServerMetricsNetworkOut();

        if (networkIn != null && networkOut != null) {
            // 解析额外信息中的包统计
            long inPackets = 0;
            long outPackets = 0;
            String extraInfo = metrics.getMonitorSysGenServerMetricsExtraInfo();
            if (extraInfo != null) {
                try {
                    String[] parts = extraInfo.split(",");
                    for (String part : parts) {
                        if (part.startsWith("rx_packets:")) {
                            inPackets = Long.parseLong(part.substring("rx_packets:".length()));
                        } else if (part.startsWith("tx_packets:")) {
                            outPackets = Long.parseLong(part.substring("tx_packets:".length()));
                        }
                    }
                } catch (Exception e) {
                    log.debug("解析网络包统计失败: {}", extraInfo);
                }
            }

            // 计算网络速率
            NetworkSpeedCalculatorService.NetworkSpeedResult speedResult =
                networkSpeedCalculatorService.calculateSpeed(
                    metrics.getMonitorSysGenServerId(),
                    networkIn, networkOut, inPackets, outPackets);

            network.put("in", speedResult.getInSpeed());
            network.put("out", speedResult.getOutSpeed());
            network.put("totalInBytes", speedResult.getTotalInBytes());
            network.put("totalOutBytes", speedResult.getTotalOutBytes());
            network.put("inPacketRate", speedResult.getInPacketRate());
            network.put("outPacketRate", speedResult.getOutPacketRate());
        } else {
            network.put("in", 0);
            network.put("out", 0);
            network.put("totalInBytes", 0);
            network.put("totalOutBytes", 0);
            network.put("inPacketRate", 0);
            network.put("outPacketRate", 0);
        }
        data.put("network", network);

        // 系统信息
        data.put("osInfo", metrics.getMonitorSysGenServerMetricsOsInfo());
        data.put("uptime", metrics.getMonitorSysGenServerMetricsUptime());
        data.put("processCount", metrics.getMonitorSysGenServerMetricsProcessCount());

        // 解析额外信息中的其他系统信息
        String extraInfo = metrics.getMonitorSysGenServerMetricsExtraInfo();
        if (extraInfo != null) {
            String[] parts = extraInfo.split(",");
            for (String part : parts) {
                if (part.startsWith("hostname:")) {
                    data.put("hostname", part.substring("hostname:".length()));
                } else if (part.startsWith("load_avg:")) {
                    data.put("loadAverage", part.substring("load_avg:".length()));
                }
            }
        }

        return data;
    }

    /**
     * 创建服务器状态汇总
     */
    private Map<String, Object> createServerStatusSummary() {
        Map<String, Object> summary = new HashMap<>();

        try {
            List<MonitorSysGenServer> servers = serverService.list();

            int totalServers = servers.size();
            int onlineServers = 0;
            int offlineServers = 0;
            int warningServers = 0;
            int criticalServers = 0;

            for (MonitorSysGenServer server : servers) {
                try {
                    ReturnResult<MonitorSysGenServerMetrics> latestMetrics = metricsService.getLatestMetrics(server.getMonitorSysGenServerId());
                    if (latestMetrics != null && latestMetrics.getData() != null) {
                        MonitorSysGenServerMetrics metrics = latestMetrics.getData();
                        if (metrics.getMonitorSysGenServerMetricsStatus() == 1) {
                            onlineServers++;

                            if (isCriticalState(metrics)) {
                                criticalServers++;
                            } else if (isServerInWarningState(metrics)) {
                                warningServers++;
                            }
                        } else {
                            offlineServers++;
                        }
                    } else {
                        offlineServers++;
                    }
                } catch (Exception e) {
                    offlineServers++;
                }
            }

            summary.put("totalServers", totalServers);
            summary.put("onlineServers", onlineServers);
            summary.put("offlineServers", offlineServers);
            summary.put("warningServers", warningServers);
            summary.put("criticalServers", criticalServers);
            summary.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            log.error("创建服务器状态汇总失败: {}", e.getMessage(), e);
        }

        return summary;
    }

    /**
     * 检查服务器告警
     */
    private List<Map<String, Object>> checkServerAlerts(MonitorSysGenServer server) {
        List<Map<String, Object>> alerts = new java.util.ArrayList<>();

        try {
            ReturnResult<MonitorSysGenServerMetrics> latestMetrics = metricsService.getLatestMetrics(server.getMonitorSysGenServerId());
            if (latestMetrics != null && latestMetrics.getData() != null) {
                MonitorSysGenServerMetrics metrics = latestMetrics.getData();

                // 检查CPU告警
                if (metrics.getMonitorSysGenServerMetricsCpuUsage() != null &&
                    metrics.getMonitorSysGenServerMetricsCpuUsage().doubleValue() > 80.0) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "cpu_high");
                    alert.put("level", metrics.getMonitorSysGenServerMetricsCpuUsage().doubleValue() > 90.0 ? "critical" : "warning");
                    alert.put("message", "CPU使用率过高: " + metrics.getMonitorSysGenServerMetricsCpuUsage() + "%");
                    alert.put("value", metrics.getMonitorSysGenServerMetricsCpuUsage());
                    alerts.add(alert);
                }

                // 检查内存告警
                if (metrics.getMonitorSysGenServerMetricsMemoryUsage() != null &&
                    metrics.getMonitorSysGenServerMetricsMemoryUsage().doubleValue() > 85.0) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "memory_high");
                    alert.put("level", metrics.getMonitorSysGenServerMetricsMemoryUsage().doubleValue() > 95.0 ? "critical" : "warning");
                    alert.put("message", "内存使用率过高: " + metrics.getMonitorSysGenServerMetricsMemoryUsage() + "%");
                    alert.put("value", metrics.getMonitorSysGenServerMetricsMemoryUsage());
                    alerts.add(alert);
                }

                // 检查磁盘告警
                if (metrics.getMonitorSysGenServerMetricsDiskUsage() != null &&
                    metrics.getMonitorSysGenServerMetricsDiskUsage().doubleValue() > 90.0) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "disk_high");
                    alert.put("level", metrics.getMonitorSysGenServerMetricsDiskUsage().doubleValue() > 95.0 ? "critical" : "warning");
                    alert.put("message", "磁盘使用率过高: " + metrics.getMonitorSysGenServerMetricsDiskUsage() + "%");
                    alert.put("value", metrics.getMonitorSysGenServerMetricsDiskUsage());
                    alerts.add(alert);
                }
            }
        } catch (Exception e) {
            log.error("检查服务器告警失败: {}", e.getMessage(), e);
        }

        return alerts;
    }

    /**
     * 判断服务器是否处于警告状态
     */
    private boolean isServerInWarningState(MonitorSysGenServerMetrics metrics) {
        // CPU使用率超过80%
        if (metrics.getMonitorSysGenServerMetricsCpuUsage() != null &&
            metrics.getMonitorSysGenServerMetricsCpuUsage().doubleValue() > 80.0) {
            return true;
        }

        // 内存使用率超过85%
        if (metrics.getMonitorSysGenServerMetricsMemoryUsage() != null &&
            metrics.getMonitorSysGenServerMetricsMemoryUsage().doubleValue() > 85.0) {
            return true;
        }

        // 磁盘使用率超过90%
        return metrics.getMonitorSysGenServerMetricsDiskUsage() != null &&
                metrics.getMonitorSysGenServerMetricsDiskUsage().doubleValue() > 90.0;
    }

    /**
     * 判断是否为严重状态
     */
    private boolean isCriticalState(MonitorSysGenServerMetrics metrics) {
        // CPU使用率超过90%
        if (metrics.getMonitorSysGenServerMetricsCpuUsage() != null &&
            metrics.getMonitorSysGenServerMetricsCpuUsage().doubleValue() > 90.0) {
            return true;
        }

        // 内存使用率超过95%
        if (metrics.getMonitorSysGenServerMetricsMemoryUsage() != null &&
            metrics.getMonitorSysGenServerMetricsMemoryUsage().doubleValue() > 95.0) {
            return true;
        }

        // 磁盘使用率超过95%
        return metrics.getMonitorSysGenServerMetricsDiskUsage() != null &&
                metrics.getMonitorSysGenServerMetricsDiskUsage().doubleValue() > 95.0;
    }

    /**
     * 创建连接状态统计
     */
    private Map<String, Object> createConnectionStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        try {
            List<MonitorSysGenServer> servers = serverService.list();

            int totalServers = servers.size();
            int onlineServers = 0;
            int offlineServers = 0;

            for (MonitorSysGenServer server : servers) {
                try {
                    ReturnResult<MonitorSysGenServerMetrics> latestMetrics = metricsService.getLatestMetrics(server.getMonitorSysGenServerId());
                    if (latestMetrics != null && latestMetrics.getData() != null) {
                        MonitorSysGenServerMetrics metrics = latestMetrics.getData();
                        if (metrics.getMonitorSysGenServerMetricsStatus() == 1) {
                            onlineServers++;
                        } else {
                            offlineServers++;
                        }
                    } else {
                        offlineServers++;
                    }
                } catch (Exception e) {
                    offlineServers++;
                }
            }

            statistics.put("totalServers", totalServers);
            statistics.put("onlineServers", onlineServers);
            statistics.put("offlineServers", offlineServers);
            statistics.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            log.error("创建连接状态统计失败: {}", e.getMessage(), e);
        }

        return statistics;
    }

    /**
     * 创建健康状态报告
     */
    private Map<String, Object> createHealthStatusReport() {
        Map<String, Object> healthStatus = new HashMap<>();

        try {
            List<MonitorSysGenServer> servers = serverService.list();

            int healthyServers = 0;
            int warningServers = 0;
            int criticalServers = 0;
            int unknownServers = 0;

            for (MonitorSysGenServer server : servers) {
                try {
                    ReturnResult<MonitorSysGenServerMetrics> latestMetrics = metricsService.getLatestMetrics(server.getMonitorSysGenServerId());
                    if (latestMetrics != null && latestMetrics.getData() != null) {
                        MonitorSysGenServerMetrics metrics = latestMetrics.getData();

                        if (isCriticalState(metrics)) {
                            criticalServers++;
                        } else if (isServerInWarningState(metrics)) {
                            warningServers++;
                        } else {
                            healthyServers++;
                        }
                    } else {
                        unknownServers++;
                    }
                } catch (Exception e) {
                    unknownServers++;
                }
            }

            healthStatus.put("healthyServers", healthyServers);
            healthStatus.put("warningServers", warningServers);
            healthStatus.put("criticalServers", criticalServers);
            healthStatus.put("unknownServers", unknownServers);
            healthStatus.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            log.error("创建健康状态报告失败: {}", e.getMessage(), e);
        }

        return healthStatus;
    }

    /**
     * 创建每日健康报告
     */
    private Map<String, Object> createDailyHealthReport() {
        Map<String, Object> dailyReport = new HashMap<>();

        try {
            LocalDateTime now = LocalDateTime.now();

            List<MonitorSysGenServer> servers = serverService.list();

            dailyReport.put("reportDate", now.toLocalDate());
            dailyReport.put("totalServers", servers.size());
            dailyReport.put("reportTime", now);

            // 这里可以添加更多的每日统计信息
            dailyReport.put("summary", "每日健康报告生成完成");

        } catch (Exception e) {
            log.error("创建每日健康报告失败: {}", e.getMessage(), e);
        }

        return dailyReport;
    }

    /**
     * 提取CPU趋势数据
     */
    private Map<String, Object> extractCpuTrend(List<MonitorSysGenServerMetrics> historyMetrics) {
        Map<String, Object> cpuTrend = new HashMap<>();

        if (historyMetrics == null || historyMetrics.isEmpty()) {
            return cpuTrend;
        }

        List<Double> values = historyMetrics.stream()
                .map(MonitorSysGenServerMetrics::getMonitorSysGenServerMetricsCpuUsage)
                .filter(java.util.Objects::nonNull)
                .map(Number::doubleValue)
                .collect(java.util.stream.Collectors.toList());

        if (!values.isEmpty()) {
            cpuTrend.put("values", values);
            cpuTrend.put("average", values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
            cpuTrend.put("max", values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
            cpuTrend.put("min", values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0));
        }

        return cpuTrend;
    }

    /**
     * 提取内存趋势数据
     */
    private Map<String, Object> extractMemoryTrend(List<MonitorSysGenServerMetrics> historyMetrics) {
        Map<String, Object> memoryTrend = new HashMap<>();

        if (historyMetrics == null || historyMetrics.isEmpty()) {
            return memoryTrend;
        }

        List<Double> values = historyMetrics.stream()
                .map(MonitorSysGenServerMetrics::getMonitorSysGenServerMetricsMemoryUsage)
                .filter(java.util.Objects::nonNull)
                .map(Number::doubleValue)
                .collect(java.util.stream.Collectors.toList());

        if (!values.isEmpty()) {
            memoryTrend.put("values", values);
            memoryTrend.put("average", values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
            memoryTrend.put("max", values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
            memoryTrend.put("min", values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0));
        }

        return memoryTrend;
    }

    /**
     * 提取磁盘趋势数据
     */
    private Map<String, Object> extractDiskTrend(List<MonitorSysGenServerMetrics> historyMetrics) {
        Map<String, Object> diskTrend = new HashMap<>();

        if (historyMetrics == null || historyMetrics.isEmpty()) {
            return diskTrend;
        }

        List<Double> values = historyMetrics.stream()
                .map(MonitorSysGenServerMetrics::getMonitorSysGenServerMetricsDiskUsage)
                .filter(java.util.Objects::nonNull)
                .map(Number::doubleValue)
                .collect(java.util.stream.Collectors.toList());

        if (!values.isEmpty()) {
            diskTrend.put("values", values);
            diskTrend.put("average", values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
            diskTrend.put("max", values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
            diskTrend.put("min", values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0));
        }

        return diskTrend;
    }

    /**
     * 提取网络趋势数据
     */
    private Map<String, Object> extractNetworkTrend(List<MonitorSysGenServerMetrics> historyMetrics) {
        Map<String, Object> networkTrend = new HashMap<>();

        if (historyMetrics == null || historyMetrics.isEmpty()) {
            return networkTrend;
        }

        List<Long> inValues = historyMetrics.stream()
                .map(MonitorSysGenServerMetrics::getMonitorSysGenServerMetricsNetworkIn)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());

        List<Long> outValues = historyMetrics.stream()
                .map(MonitorSysGenServerMetrics::getMonitorSysGenServerMetricsNetworkOut)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());

        networkTrend.put("inValues", inValues);
        networkTrend.put("outValues", outValues);

        if (!inValues.isEmpty()) {
            networkTrend.put("avgIn", inValues.stream().mapToLong(Long::longValue).average().orElse(0.0));
        }

        if (!outValues.isEmpty()) {
            networkTrend.put("avgOut", outValues.stream().mapToLong(Long::longValue).average().orElse(0.0));
        }

        return networkTrend;
    }
}
