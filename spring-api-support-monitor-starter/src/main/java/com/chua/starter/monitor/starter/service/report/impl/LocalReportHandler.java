package com.chua.starter.monitor.starter.service.report.impl;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerMetricsService;
import com.chua.starter.monitor.starter.service.report.ReportHandler;
import com.chua.starter.monitor.starter.utils.LocalSystemDataCollector;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.common.support.json.Json;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import com.chua.common.support.lang.code.ReturnResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    private final SocketSessionTemplate socketSessionTemplate;
    private final LocalSystemDataCollector dataCollector;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Override
    public String getReportType() {
        return "LOCAL";
    }

    @Override
    public boolean supports(MonitorSysGenServer server) {
        return "LOCAL".equals(server.getMonitorSysGenServerDataReportMethod()) 
                && server.getMonitorSysGenServerIsLocal() != null 
                && server.getMonitorSysGenServerIsLocal() == 1;
    }

    @Override
    public ReturnResult<MonitorSysGenServerMetrics> handleReport(MonitorSysGenServer server, Map<String, Object> reportData) {
        try {
            log.debug("开始处理本地服务器数据上报: serverId={}", server.getMonitorSysGenServerId());
            
            // 收集本地系统指标
            MonitorSysGenServerMetrics metrics = collectLocalMetrics(server);
            
            // 保存到数据库
            metricsService.save(metrics);
            
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
            
            // 停止已存在的任务
            stopDataCollection(serverId);
            
            // 获取监控间隔，默认30秒
            long interval = server.getMonitorSysGenServerMonitorInterval() != null ? 
                server.getMonitorSysGenServerMonitorInterval() : getDefaultMonitorInterval();
            
            // 启动定时收集任务
            ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
                try {
                    handleReport(server, null);
                } catch (Exception e) {
                    log.error("本地数据收集任务执行失败: serverId={}", serverId, e);
                }
            }, 0, interval, TimeUnit.SECONDS);
            
            scheduledTasks.put(serverId, task);
            log.info("本地数据收集任务启动成功: serverId={}, interval={}s", serverId, interval);
            
            return ReturnResult.success(true);
            
        } catch (Exception e) {
            log.error("启动本地数据收集任务失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("启动本地数据收集失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> stopDataCollection(Integer serverId) {
        try {
            ScheduledFuture<?> task = scheduledTasks.remove(serverId);
            if (task != null && !task.isCancelled()) {
                task.cancel(true);
                log.info("本地数据收集任务已停止: serverId={}", serverId);
            }
            return ReturnResult.success(true);
            
        } catch (Exception e) {
            log.error("停止本地数据收集任务失败: serverId={}", serverId, e);
            return ReturnResult.error("停止本地数据收集失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> validateServerConfig(MonitorSysGenServer server) {
        try {
            // 检查是否为本地服务器
            if (server.getMonitorSysGenServerIsLocal() == null || server.getMonitorSysGenServerIsLocal() != 1) {
                return ReturnResult.error("服务器不是本地服务器，无法使用本地上报方式");
            }
            
            // 检查监控间隔
            if (server.getMonitorSysGenServerMonitorInterval() != null && server.getMonitorSysGenServerMonitorInterval() < 5) {
                return ReturnResult.error("监控间隔不能小于5秒");
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
            socketSessionTemplate.send("gen/server", messageJson);
            log.debug("服务器指标数据已广播: serverId={}", serverId);
        } catch (Exception e) {
            log.error("广播服务器指标数据失败: serverId={}", serverId, e);
        }
    }
}
