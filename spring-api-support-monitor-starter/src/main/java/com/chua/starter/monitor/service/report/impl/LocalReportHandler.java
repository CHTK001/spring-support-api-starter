package com.chua.starter.monitor.service.report.impl;

import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.constant.MonitorSocketTopics;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.MonitorSysGenServer;

import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.sync.message.ServerWebSocketMessage;
import com.chua.starter.monitor.service.server.MonitorSysGenServerMetricsService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.service.alert.UnifiedAlertService;
import com.chua.starter.monitor.service.report.ReportHandler;
import com.chua.starter.monitor.utils.LocalSystemDataCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 本地上报处理器
 * 用于本地服务器直接获取系统指标数据
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalReportHandler implements ReportHandler {

    private final MonitorSysGenServerMetricsService metricsService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final SocketSessionTemplate socketSessionTemplate;
    private final LocalSystemDataCollector dataCollector;
    @Autowired
    @Lazy
    private UnifiedAlertService unifiedAlertService;

    // 移除定时任务相关字段，数据收集由DataCollectionTask统一管理

    @Override
    public String getReportType() {
        return "LOCAL";
    }

    @Override
    public boolean supports(MonitorSysGenServer server) {
        try {
            // 从配置表获取上报方式
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

            if (settingResult.isOk() && settingResult.getData() != null) {
                MonitorSysGenServerSetting setting = settingResult.getData();
                return "LOCAL".equals(setting.getMonitorSysGenServerSettingDataReportMethod())
                        && server.getMonitorSysGenServerIsLocal() != null
                        && server.getMonitorSysGenServerIsLocal() == 1;
            }
        } catch (Exception e) {
            log.warn("获取服务器配置失败: serverId={}, error={}",
                server.getMonitorSysGenServerId(), e.getMessage());
        }
        return false;
    }

    @Override
    public ReturnResult<MonitorSysGenServerMetrics> handleReport(MonitorSysGenServer server, Map<String, Object> reportData) {
        try {
            log.debug("开始处理本地服务器数据上报: serverId={}", server.getMonitorSysGenServerId());

            // 收集本地系统指标
            MonitorSysGenServerMetrics metrics = collectLocalMetrics(server);

            // 保存到数据库
            metricsService.saveMetrics(metrics);

            // 进行告警检测
            try {
                // 使用统一告警服务检测和推送告警
                ReturnResult<Boolean> alertResult =
                    unifiedAlertService.checkAndPushServerAlerts(server, metrics);

                if (alertResult.isOk() && alertResult.getData() != null && alertResult.getData()) {
                    log.info("本地采集数据检测到告警并已推送: serverId={}", server.getMonitorSysGenServerId());
                }
            } catch (Exception e) {
                // 告警检测失败不影响数据收集的成功
                log.error("本地采集数据告警检测失败: serverId={}", server.getMonitorSysGenServerId(), e);
            }

            // 通过WebSocket推送到前端
            broadcastServerMetrics(server.getMonitorSysGenServerId(), metrics);

            log.debug("本地服务器数据上报处理完成: serverId={}", server.getMonitorSysGenServerId());
            return ReturnResult.success(metrics);

        } catch (Exception e) {
            log.error("处理本地服务器数据上报失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("本地数据上报失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> startDataCollection(MonitorSysGenServer server) {
        try {
            Integer serverId = server.getMonitorSysGenServerId();
            log.info("本地数据收集配置已启用: serverId={}, 数据收集由DataCollectionTask统一管理", serverId);
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("启动本地数据收集配置失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("启动数据收集配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> stopDataCollection(Integer serverId) {
        try {
            log.info("本地数据收集配置已停用: serverId={}", serverId);
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("停止本地数据收集配置失败: serverId={}", serverId, e);
            return ReturnResult.error("停止数据收集配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> validateServerConfig(MonitorSysGenServer server) {
        try {
            // 检查是否为本地服务器
            if (server.getMonitorSysGenServerIsLocal() == null || server.getMonitorSysGenServerIsLocal() != 1) {
                return ReturnResult.error("服务器不是本地服务器，无法使用本地上报方式");
            }



            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("验证本地服务器配置失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("验证配置失败: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "本地上报 - 直接获取本地系统指标数据";
    }

    @Override
    public int getPriority() {
        return 10; // 本地上报优先级最高
    }

    @Override
    public boolean requiresScheduledTask() {
        return true;
    }

    @Override
    public int getDefaultMonitorInterval() {
        return 30;
    }

    /**
     * 收集本地系统指标
     * 使用oshi库获取准确的系统信息
     *
     * @param server 服务器配置
     * @return 系统指标
     */
    private MonitorSysGenServerMetrics collectLocalMetrics(MonitorSysGenServer server) {
        MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();

        try {
            // 设置基本信息
            metrics.setMonitorSysGenServerId(server.getMonitorSysGenServerId());
            metrics.setMonitorSysGenServerMetricsCollectTime(LocalDateTime.now());

            // 使用LocalSystemDataCollector收集系统数据
            Map<String, Object> systemData = dataCollector.collectSystemData();

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

            // 提取内存信息
            if (systemData.containsKey("memoryTotal") && systemData.containsKey("memoryUsed")) {
                Long memoryTotal = (Long) systemData.get("memoryTotal");
                Long memoryUsed = (Long) systemData.get("memoryUsed");
                Double memoryUsage = (Double) systemData.get("memoryUsage");

                // 转换为MB
                metrics.setMonitorSysGenServerMetricsMemoryTotal(memoryTotal / (1024 * 1024));
                metrics.setMonitorSysGenServerMetricsMemoryUsed(memoryUsed / (1024 * 1024));
                metrics.setMonitorSysGenServerMetricsMemoryFree((memoryTotal - memoryUsed) / (1024 * 1024));
                metrics.setMonitorSysGenServerMetricsMemoryUsage(BigDecimal.valueOf(memoryUsage));
            }

            // 提取系统负载
            if (systemData.containsKey("systemLoadAverage")) {
                Double systemLoad = (Double) systemData.get("systemLoadAverage");
                metrics.setMonitorSysGenServerMetricsCpuLoad1m(BigDecimal.valueOf(systemLoad));
            }

            // 提取运行时间
            if (systemData.containsKey("systemUptime")) {
                Long uptime = (Long) systemData.get("systemUptime");
                metrics.setMonitorSysGenServerMetricsUptime(uptime);
            }

            // 提取进程数量
            if (systemData.containsKey("processCount")) {
                Integer processCount = (Integer) systemData.get("processCount");
                metrics.setMonitorSysGenServerMetricsProcessCount(processCount);
            }

            // 提取磁盘使用情况（取第一个文件系统的使用率）
            if (systemData.containsKey("fileSystems")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> fileSystems = (List<Map<String, Object>>) systemData.get("fileSystems");
                if (!fileSystems.isEmpty()) {
                    Map<String, Object> firstFs = fileSystems.get(0);
                    if (firstFs.containsKey("usagePercent") && firstFs.containsKey("totalSpace")) {
                        Double diskUsage = (Double) firstFs.get("usagePercent");
                        Long totalSpace = (Long) firstFs.get("totalSpace");
                        Long freeSpace = (Long) firstFs.get("freeSpace");

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

            log.debug("本地系统指标收集完成: CPU={}%, Memory={}%, Load={}",
                    metrics.getMonitorSysGenServerMetricsCpuUsage(),
                    metrics.getMonitorSysGenServerMetricsMemoryUsage(),
                    metrics.getMonitorSysGenServerMetricsCpuLoad1m());

        } catch (Exception e) {
            log.error("收集本地系统指标失败", e);
            throw new RuntimeException("收集本地系统指标失败", e);
        }

        return metrics;
    }

    /**
     * 广播服务器指标数据到前端
     */
    private void broadcastServerMetrics(Integer serverId, MonitorSysGenServerMetrics metrics) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.createServerMetricsMessage(
                    serverId, "Server-" + serverId, metrics);
            String messageJson = Json.toJson(message);
            socketSessionTemplate.send(MonitorSocketTopics.Monitor.Server.METRICS, messageJson);
            log.debug("服务器指标数据已广播: serverId={}", serverId);
        } catch (Exception e) {
            log.error("广播服务器指标数据失败: serverId={}", serverId, e);
        }
    }
}
