package com.chua.starter.monitor.starter.task.manager;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.starter.service.ServerMetricsWebSocketService;
import com.chua.starter.monitor.starter.service.UnifiedAlertService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 服务器任务管理器
 * 根据服务器ID管理各种类型的定时任务
 * 支持动态创建、更新、删除任务
 *
 * @author CH
 * @since 2024/12/28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServerTaskManager {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final ServerMetricsWebSocketService serverMetricsWebSocketService;
    private final UnifiedAlertService unifiedAlertService;

    // 定时任务执行器
    private ScheduledExecutorService scheduledExecutorService;
    
    // 存储每个服务器的任务信息
    private final Map<Integer, ServerTaskInfo> serverTasks = new ConcurrentHashMap<>();
    
    // 全局任务（不依赖特定服务器）
    private final Map<String, ScheduledFuture<?>> globalTasks = new ConcurrentHashMap<>();

    /**
     * 服务器任务信息
     */
    @Data
    @AllArgsConstructor
    public static class ServerTaskInfo {
        private final Integer serverId;
        private ScheduledFuture<?> metricsCollectionTask;
        private ScheduledFuture<?> alertDetectionTask;
        private ScheduledFuture<?> healthCheckTask;
        private MonitorSysGenServerSetting currentSetting;

        public ServerTaskInfo(Integer serverId) {
            this.serverId = serverId;
        }

        public void cancelAllTasks() {
            cancelTask(metricsCollectionTask);
            cancelTask(alertDetectionTask);
            cancelTask(healthCheckTask);
        }

        private void cancelTask(ScheduledFuture<?> task) {
            if (task != null && !task.isCancelled()) {
                task.cancel(false);
            }
        }
    }

    @PostConstruct
    public void init() {
        // 创建线程池
        scheduledExecutorService = Executors.newScheduledThreadPool(
            Math.max(4, Runtime.getRuntime().availableProcessors()),
            r -> {
                Thread t = new Thread(r, "ServerTask-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            }
        );

        // 启动全局任务
        startGlobalTasks();
        
        // 为所有现有服务器创建任务
        initializeServerTasks();
        
        log.info("服务器任务管理器初始化完成");
    }

    @PreDestroy
    public void destroy() {
        log.info("正在关闭服务器任务管理器...");
        
        // 取消所有服务器任务
        serverTasks.values().forEach(ServerTaskInfo::cancelAllTasks);
        serverTasks.clear();
        
        // 取消所有全局任务
        globalTasks.values().forEach(task -> {
            if (!task.isCancelled()) {
                task.cancel(false);
            }
        });
        globalTasks.clear();
        
        // 关闭线程池
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            try {
                if (!scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduledExecutorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        log.info("服务器任务管理器已关闭");
    }

    /**
     * 启动全局任务
     */
    private void startGlobalTasks() {
        // 服务器状态汇总推送 - 每分钟
        ScheduledFuture<?> statusSummaryTask = scheduledExecutorService.scheduleAtFixedRate(
            this::pushServerStatusSummary, 30, 60, TimeUnit.SECONDS);
        globalTasks.put("statusSummary", statusSummaryTask);

        // 性能趋势数据推送 - 每5分钟
        ScheduledFuture<?> performanceTrendsTask = scheduledExecutorService.scheduleAtFixedRate(
            this::pushPerformanceTrends, 60, 300, TimeUnit.SECONDS);
        globalTasks.put("performanceTrends", performanceTrendsTask);

        // 连接状态统计推送 - 每30秒
        ScheduledFuture<?> connectionStatsTask = scheduledExecutorService.scheduleAtFixedRate(
            this::pushConnectionStatistics, 10, 30, TimeUnit.SECONDS);
        globalTasks.put("connectionStats", connectionStatsTask);

        // 健康状态报告推送 - 每5分钟
        ScheduledFuture<?> healthStatusTask = scheduledExecutorService.scheduleAtFixedRate(
            this::pushHealthStatus, 120, 300, TimeUnit.SECONDS);
        globalTasks.put("healthStatus", healthStatusTask);

        // 每日健康报告推送 - 每天上午9点（使用cron表达式的简化版本）
        ScheduledFuture<?> dailyReportTask = scheduledExecutorService.scheduleAtFixedRate(
            this::pushDailyHealthReport, calculateInitialDelayForDailyReport(), 24 * 60 * 60, TimeUnit.SECONDS);
        globalTasks.put("dailyReport", dailyReportTask);

        // 全局告警处理 - 每2分钟
        ScheduledFuture<?> globalAlertTask = scheduledExecutorService.scheduleAtFixedRate(
            this::processGlobalAlerts, 90, 120, TimeUnit.SECONDS);
        globalTasks.put("globalAlert", globalAlertTask);

        // 清理任务 - 每小时
        ScheduledFuture<?> cleanupTask = scheduledExecutorService.scheduleAtFixedRate(
            this::cleanupInvalidTasks, 3600, 3600, TimeUnit.SECONDS);
        globalTasks.put("cleanup", cleanupTask);

        log.info("全局任务启动完成");
    }

    /**
     * 计算每日报告的初始延迟（到明天上午9点的秒数）
     */
    private long calculateInitialDelayForDailyReport() {
        // 简化实现：设置为1小时后开始，然后每24小时执行一次
        return 3600; // 1小时
    }

    /**
     * 初始化所有服务器的任务
     */
    private void initializeServerTasks() {
        try {
            List<MonitorSysGenServer> servers = serverService.list().stream()
                .filter(server -> server.getMonitorSysGenServerStatus() != null &&
                        server.getMonitorSysGenServerStatus() == 1)
                .toList();

            for (MonitorSysGenServer server : servers) {
                createServerTasks(server.getMonitorSysGenServerId());
            }

            log.info("为 {} 个服务器初始化任务完成", servers.size());
        } catch (Exception e) {
            log.error("初始化服务器任务失败", e);
        }
    }

    /**
     * 为指定服务器创建任务
     */
    public void createServerTasks(Integer serverId) {
        try {
            // 获取服务器信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null || server.getMonitorSysGenServerStatus() != 1) {
                log.warn("服务器不存在或已禁用，跳过任务创建: serverId={}", serverId);
                return;
            }

            // 获取服务器配置
            ReturnResult<MonitorSysGenServerSetting> settingResult = 
                serverSettingService.getOrCreateSetting(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                log.warn("获取服务器配置失败，跳过任务创建: serverId={}", serverId);
                return;
            }

            MonitorSysGenServerSetting setting = settingResult.getData();

            // 移除旧任务
            removeServerTasks(serverId);

            // 创建新的任务信息
            ServerTaskInfo taskInfo = new ServerTaskInfo(serverId);
            taskInfo.setCurrentSetting(setting);

            // 创建指标收集任务（仅对非API上报方式）
            if (!"API".equalsIgnoreCase(setting.getMonitorSysGenServerSettingDataReportMethod())) {
                createMetricsCollectionTask(taskInfo, server, setting);
            }

            // 创建告警检测任务
            createAlertDetectionTask(taskInfo, server, setting);

            // 创建健康检查任务
            createHealthCheckTask(taskInfo, server, setting);

            serverTasks.put(serverId, taskInfo);
            log.info("为服务器 {} 创建任务完成", server.getMonitorSysGenServerName());

        } catch (Exception e) {
            log.error("为服务器创建任务失败: serverId={}", serverId, e);
        }
    }

    /**
     * 创建指标收集任务
     */
    private void createMetricsCollectionTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        if (setting.getMonitorSysGenServerSettingMonitorEnabled() != 1) {
            return;
        }

        Integer frequency = setting.getMonitorSysGenServerSettingDataCollectionFrequency();
        if (frequency == null || frequency <= 0) {
            frequency = 30; // 默认30秒
        }

        ScheduledFuture<?> task = scheduledExecutorService.scheduleAtFixedRate(
            () -> collectAndPushMetrics(server, setting),
            frequency, frequency, TimeUnit.SECONDS
        );

        taskInfo.setMetricsCollectionTask(task);
        log.debug("为服务器 {} 创建指标收集任务，频率: {}秒", server.getMonitorSysGenServerName(), frequency);
    }

    /**
     * 创建告警检测任务
     */
    private void createAlertDetectionTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        if (setting.getMonitorSysGenServerSettingAlertEnabled() != 1) {
            return;
        }

        // 告警检测频率固定为2分钟
        ScheduledFuture<?> task = scheduledExecutorService.scheduleAtFixedRate(
            () -> detectAndPushAlerts(server, setting),
            120, 120, TimeUnit.SECONDS
        );

        taskInfo.setAlertDetectionTask(task);
        log.debug("为服务器 {} 创建告警检测任务", server.getMonitorSysGenServerName());
    }

    /**
     * 创建健康检查任务
     */
    private void createHealthCheckTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        // 健康检查频率固定为5分钟
        ScheduledFuture<?> task = scheduledExecutorService.scheduleAtFixedRate(
            () -> performHealthCheck(server, setting),
            300, 300, TimeUnit.SECONDS
        );

        taskInfo.setHealthCheckTask(task);
        log.debug("为服务器 {} 创建健康检查任务", server.getMonitorSysGenServerName());
    }

    /**
     * 移除指定服务器的所有任务
     */
    public void removeServerTasks(Integer serverId) {
        ServerTaskInfo taskInfo = serverTasks.remove(serverId);
        if (taskInfo != null) {
            taskInfo.cancelAllTasks();
            log.info("移除服务器 {} 的所有任务", serverId);
        }
    }

    /**
     * 更新服务器任务配置
     */
    public void updateServerTasks(Integer serverId) {
        try {
            // 重新创建任务（会自动移除旧任务）
            createServerTasks(serverId);
            log.info("更新服务器 {} 的任务配置", serverId);
        } catch (Exception e) {
            log.error("更新服务器任务配置失败: serverId={}", serverId, e);
        }
    }

    /**
     * 收集并推送指标数据
     */
    private void collectAndPushMetrics(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            serverMetricsWebSocketService.pushSingleServerMetricsByConfig(server, setting);
        } catch (Exception e) {
            log.error("收集推送指标数据失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 检测并推送告警
     */
    private void detectAndPushAlerts(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            // 使用统一告警服务检测单个服务器的告警
            unifiedAlertService.checkAndPushServerAlerts(server, null);
        } catch (Exception e) {
            log.error("检测推送告警失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 执行健康检查
     */
    private void performHealthCheck(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            // 这里可以实现具体的健康检查逻辑
            log.debug("执行服务器 {} 健康检查", server.getMonitorSysGenServerName());
        } catch (Exception e) {
            log.error("健康检查失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    // 全局任务方法
    private void pushServerStatusSummary() {
        try {
            serverMetricsWebSocketService.pushServerStatusSummary();
        } catch (Exception e) {
            log.error("推送服务器状态汇总失败", e);
        }
    }

    private void pushPerformanceTrends() {
        try {
            serverMetricsWebSocketService.pushPerformanceTrends();
        } catch (Exception e) {
            log.error("推送性能趋势数据失败", e);
        }
    }

    private void pushConnectionStatistics() {
        try {
            serverMetricsWebSocketService.pushConnectionStatistics();
        } catch (Exception e) {
            log.error("推送连接状态统计失败", e);
        }
    }

    private void pushHealthStatus() {
        try {
            serverMetricsWebSocketService.pushHealthStatus();
        } catch (Exception e) {
            log.error("推送健康状态报告失败", e);
        }
    }

    private void pushDailyHealthReport() {
        try {
            serverMetricsWebSocketService.pushDailyHealthReport();
        } catch (Exception e) {
            log.error("推送每日健康报告失败", e);
        }
    }

    private void processGlobalAlerts() {
        try {
            unifiedAlertService.pushAllServerAlerts();
        } catch (Exception e) {
            log.error("处理全局告警失败", e);
        }
    }

    private void cleanupInvalidTasks() {
        try {
            // 获取当前活跃的服务器ID
            List<Integer> activeServerIds = serverService.list().stream()
                .filter(server -> server.getMonitorSysGenServerStatus() != null &&
                        server.getMonitorSysGenServerStatus() == 1)
                .map(MonitorSysGenServer::getMonitorSysGenServerId)
                .toList();

            // 清理不活跃服务器的任务
            int cleanedCount = 0;
            for (Integer serverId : serverTasks.keySet()) {
                if (!activeServerIds.contains(serverId)) {
                    removeServerTasks(serverId);
                    cleanedCount++;
                }
            }

            if (cleanedCount > 0) {
                log.info("清理了 {} 个无效的服务器任务", cleanedCount);
            }
        } catch (Exception e) {
            log.error("清理无效任务失败", e);
        }
    }

    /**
     * 获取任务统计信息
     */
    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalServerTasks", serverTasks.size());
        stats.put("totalGlobalTasks", globalTasks.size());

        long activeServerTasks = serverTasks.values().stream()
            .mapToLong(taskInfo -> {
                long count = 0;
                if (taskInfo.getMetricsCollectionTask() != null && !taskInfo.getMetricsCollectionTask().isCancelled()) count++;
                if (taskInfo.getAlertDetectionTask() != null && !taskInfo.getAlertDetectionTask().isCancelled()) count++;
                if (taskInfo.getHealthCheckTask() != null && !taskInfo.getHealthCheckTask().isCancelled()) count++;
                return count;
            })
            .sum();

        long activeGlobalTasks = globalTasks.values().stream()
            .mapToLong(task -> task.isCancelled() ? 0 : 1)
            .sum();

        stats.put("activeServerTasks", activeServerTasks);
        stats.put("activeGlobalTasks", activeGlobalTasks);

        return stats;
    }
}
