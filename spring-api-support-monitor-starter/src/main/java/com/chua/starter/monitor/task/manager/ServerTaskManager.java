package com.chua.starter.monitor.task.manager;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.pojo.ServerMetricsData;
import com.chua.starter.monitor.service.*;
import com.chua.starter.monitor.utils.LocalSystemDataCollector;
import com.chua.starter.monitor.utils.ServerLogUtils;
import com.chua.starter.monitor.service.ServerLatencyService;
import com.chua.starter.monitor.task.manager.step.AlertDetectionStep;
import com.chua.starter.monitor.task.manager.step.DataPushStep;
import com.chua.starter.monitor.task.manager.step.MetricsCollectionStep;
import com.chua.starter.monitor.task.manager.step.PortMonitorStep;
import com.chua.starter.monitor.task.manager.step.HealthCheckStep;
import com.chua.starter.monitor.task.manager.step.OnlineCheckStep;
import com.chua.starter.monitor.task.manager.step.LatencyCheckStep;
import com.chua.starter.monitor.task.manager.step.WebSocketCleanupStep;
import com.chua.starter.monitor.task.manager.step.CacheCleanupStep;
import com.chua.starter.monitor.task.manager.step.LogCleanupStep;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * 服务器任务管理器 根据服务器ID管理各种类型的定时任务 支持动态创建、更新、删除任务
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
    @SuppressWarnings("unused") // 保留用于未来扩展
    private final MonitorSysGenServerMetricsService metricsService;
    private final PrometheusDataCollectorService prometheusDataCollectorService;
    private final LocalSystemDataCollector localSystemDataCollector;
    private final ServerLatencyService serverLatencyService;
    // 拆分后的步骤组件
    private final PortMonitorStep portMonitorStep;
    private final HealthCheckStep healthCheckStep;
    private final MetricsCollectionStep metricsCollectionStep;
    private final AlertDetectionStep alertDetectionStep;
    private final DataPushStep dataPushStep;
    private final OnlineCheckStep onlineCheckStep;
    private final LatencyCheckStep latencyCheckStep;
    private final LogCleanupStep logCleanupStep;
    private final CacheCleanupStep cacheCleanupStep;
    private final WebSocketCleanupStep webSocketCleanupStep;

    // 虚拟线程执行器
    private ExecutorService executorService;

    // 存储每个服务器的任务信息
    private final Map<Integer, ServerTaskInfo> serverTasks = new ConcurrentHashMap<>();

    // 全局任务（不依赖特定服务器）
    private final Map<String, Future<?>> globalTasks = new ConcurrentHashMap<>();

    // 内存中的配置缓存
    private final Map<Integer, MonitorSysGenServerSetting> configCache = new ConcurrentHashMap<>();

    // 配置刷新间隔（秒）
    private static final int CONFIG_REFRESH_INTERVAL = 60;

    // 任务运行状态控制
    private volatile boolean running = true;

    // 全局任务是否已启动
    private volatile boolean globalTasksStarted = false;

    /**
     * 刷新所有服务器配置
     */
    private void refreshAllConfigs() {
        try {
            List<MonitorSysGenServer> servers = serverService.list().stream()
                    .filter(server -> server.getMonitorSysGenServerStatus() != null
                            && server.getMonitorSysGenServerStatus() == 1)
                    .toList();

            for (MonitorSysGenServer server : servers) {
                refreshServerConfig(server.getMonitorSysGenServerId());
            }

            log.debug("刷新了 {} 个服务器的配置", servers.size());
        } catch (Exception e) {
            log.error("刷新所有服务器配置失败", e);
        }
    }

    @PostConstruct
    public void init() {
        // 创建虚拟线程执行器
        executorService = Executors.newVirtualThreadPerTaskExecutor();

        // 为所有现有服务器创建任务（包含全局任务启动）
        initializeServerTasks();

        log.info("服务器任务管理器初始化完成");
    }

    @PreDestroy
    public void destroy() {
        log.info("正在关闭服务器任务管理器...");

        // 停止所有任务
        running = false;

        // 取消所有服务器任务
        serverTasks.values().forEach(ServerTaskInfo::cancelAllTasks);
        serverTasks.clear();

        // 取消所有全局任务
        globalTasks.values().forEach(task -> {
            if (!task.isCancelled()) {
                task.cancel(true);
            }
        });
        globalTasks.clear();

        // 清空配置缓存
        configCache.clear();

        // 关闭线程池
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        log.info("服务器任务管理器已关闭");
    }

    /**
     * 启动全局任务
     */
    @Deprecated
    private void startGlobalTasks() {
        // 服务器状态汇总推送 - 每分钟
        Future<?> statusSummaryTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(60000); // 60秒
                    if (running) {
                        pushServerStatusSummary();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("服务器状态汇总推送任务异常", e);
                }
            }
        });
        globalTasks.put("statusSummary", statusSummaryTask);

        // 性能趋势数据推送 - 每5分钟
        Future<?> performanceTrendsTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(300000); // 300秒
                    if (running) {
                        pushPerformanceTrends();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("性能趋势数据推送任务异常", e);
                }
            }
        });
        globalTasks.put("performanceTrends", performanceTrendsTask);

        // 连接状态统计推送 - 每30秒
        Future<?> connectionStatsTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(30000); // 30秒
                    if (running) {
                        pushConnectionStatistics();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("连接状态统计推送任务异常", e);
                }
            }
        });
        globalTasks.put("connectionStats", connectionStatsTask);

        // 健康状态报告推送 - 每5分钟
        Future<?> healthStatusTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(300000); // 300秒
                    if (running) {
                        pushHealthStatus();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("健康状态报告推送任务异常", e);
                }
            }
        });
        globalTasks.put("healthStatus", healthStatusTask);

        // 全局告警处理 - 每2分钟
        Future<?> globalAlertTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(120000); // 120秒
                    if (running) {
                        processGlobalAlerts();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("全局告警处理任务异常", e);
                }
            }
        });
        globalTasks.put("globalAlert", globalAlertTask);

        // 清理任务 - 每小时
        Future<?> cleanupTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(3600000); // 3600秒
                    if (running) {
                        cleanupInvalidTasks();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("清理任务异常", e);
                }
            }
        });
        globalTasks.put("cleanup", cleanupTask);

        log.info("全局任务启动完成");
    }

    /**
     * 启动配置刷新任务
     */
    @Deprecated
    private void startConfigRefreshTask() {
        Future<?> configRefreshTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(CONFIG_REFRESH_INTERVAL * 1000); // 配置刷新间隔
                    if (running) {
                        refreshAllConfigs();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("配置刷新任务异常", e);
                }
            }
        });
        globalTasks.put("configRefresh", configRefreshTask);
        log.info("配置刷新任务启动完成，刷新间隔: {}秒", CONFIG_REFRESH_INTERVAL);
    }

    /**
     * 全局调度器：将原多个全局任务合并为单线程循环触发
     */
    private void startGlobalDispatcher() {
        Future<?> dispatcher = executorService.submit(() -> {
            long lastStatusSummary = 0L;
            long lastPerformanceTrends = 0L;
            long lastConnectionStats = 0L;
            long lastHealthStatus = 0L;
            long lastGlobalAlert = 0L;
            long lastCleanup = 0L;
            long lastConfigRefresh = 0L;
            long lastFileUploadCheck = 0L;
            long lastDataCleanup = 0L;
            long lastBackup = 0L;
            while (running) {
                try {
                    long now = System.currentTimeMillis();
                    if (now - lastStatusSummary >= 60_000) { pushServerStatusSummary(); lastStatusSummary = now; }
                    if (now - lastPerformanceTrends >= 300_000) { pushPerformanceTrends(); lastPerformanceTrends = now; }
                    if (now - lastConnectionStats >= 30_000) { pushConnectionStatistics(); lastConnectionStats = now; }
                    if (now - lastHealthStatus >= 300_000) { pushHealthStatus(); lastHealthStatus = now; }
                    if (now - lastGlobalAlert >= 120_000) { processGlobalAlerts(); lastGlobalAlert = now; }
                    if (now - lastCleanup >= 3_600_000) { cleanupInvalidTasks(); lastCleanup = now; }
                    if (now - lastConfigRefresh >= CONFIG_REFRESH_INTERVAL * 1000L) { refreshAllConfigs(); lastConfigRefresh = now; }
                    if (now - lastFileUploadCheck >= 30_000) { checkUploadTaskStatus(); lastFileUploadCheck = now; }
                    if (now - lastDataCleanup >= 24L * 3_600_000) { cleanupTempFilesAndCache(); lastDataCleanup = now; }
                    if (now - lastBackup >= 1_800_000) { executeBackupTasks(); lastBackup = now; }
                    Thread.sleep(1_000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("全局调度器异常", e);
                    try { Thread.sleep(5_000L); } catch (InterruptedException ignore) { Thread.currentThread().interrupt(); break; }
                }
            }
        });
        globalTasks.clear();
        globalTasks.put("globalDispatcher", dispatcher);
        log.info("全局调度器启动完成");
    }


    /**
     * 启动统一任务（合并其他定时任务）
     */
    @Deprecated
    private void startUnifiedTasks() {

        // 文件上传状态检查任务 - 每30秒
        Future<?> fileUploadStatusCheckTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(30000); // 30秒
                    if (running) {
                        checkUploadTaskStatus();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("文件上传状态检查任务异常", e);
                }
            }
        });
        globalTasks.put("fileUploadStatusCheck", fileUploadStatusCheckTask);

        // 数据清理任务 - 每天凌晨3点（简化为每24小时）
        Future<?> dataCleanupTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(24 * 3600000); // 24小时
                    if (running) {
                        cleanupTempFilesAndCache();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("数据清理任务异常", e);
                }
            }
        });
        globalTasks.put("dataCleanup", dataCleanupTask);

        // 备份任务 - 每30分钟（简化版本）
        Future<?> backupTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(1800000); // 30分钟
                    if (running) {
                        executeBackupTasks();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("备份任务异常", e);
                }
            }
        });
        globalTasks.put("backup", backupTask);

        log.info("统一任务启动完成");
    }

    /**
     * 刷新单个服务器配置
     */
    private void refreshServerConfig(Integer serverId) {
        try {
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getOrCreateSetting(serverId);
            if (settingResult.isOk() && settingResult.getData() != null) {
                MonitorSysGenServerSetting newSetting = settingResult.getData();
                MonitorSysGenServerSetting oldSetting = configCache.get(serverId);

                // 检查配置是否有变化
                if (!configEquals(oldSetting, newSetting)) {
                    configCache.put(serverId, newSetting);

                    // 更新任务中的配置
                    ServerTaskInfo taskInfo = serverTasks.get(serverId);
                    if (taskInfo != null) {
                        taskInfo.setCurrentSetting(newSetting);
                        taskInfo.updateConfigCheckTime();
                        log.debug("服务器 {} 配置已更新", serverId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("刷新服务器配置失败: serverId={}", serverId, e);
        }
    }

    /**
     * 比较两个配置是否相等
     */
    private boolean configEquals(MonitorSysGenServerSetting old, MonitorSysGenServerSetting newSetting) {
        if (old == null || newSetting == null) {
            return false;
        }

        return Objects.equals(old.getMonitorSysGenServerSettingDataCollectionFrequency(),
                newSetting.getMonitorSysGenServerSettingDataCollectionFrequency())
                && Objects.equals(old.getMonitorSysGenServerSettingMonitorEnabled(),
                        newSetting.getMonitorSysGenServerSettingMonitorEnabled())
                && Objects.equals(old.getMonitorSysGenServerSettingAlertEnabled(),
                        newSetting.getMonitorSysGenServerSettingAlertEnabled())
                && Objects.equals(old.getMonitorSysGenServerSettingDataReportMethod(),
                        newSetting.getMonitorSysGenServerSettingDataReportMethod())
                && Objects.equals(old.getMonitorSysGenServerSettingPortMonitorEnabled(),
                        newSetting.getMonitorSysGenServerSettingPortMonitorEnabled())
                && Objects.equals(old.getMonitorSysGenServerSettingPortCheckInterval(),
                        newSetting.getMonitorSysGenServerSettingPortCheckInterval());
    }

    /**
     * 初始化所有服务器的任务
     */
    public void initializeServerTasks() {
        try {
            // 启动全局任务（只启动一次）
            if (!globalTasksStarted) {
                startGlobalDispatcher();
                globalTasksStarted = true;
                log.info("全局任务和统一任务启动完成");
            }

            List<MonitorSysGenServer> servers = serverService.list().stream()
                    .filter(server -> server.getMonitorSysGenServerStatus() != null
                            && server.getMonitorSysGenServerStatus() == 1)
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
     * 从缓存获取服务器配置
     */
    private MonitorSysGenServerSetting getCachedConfig(Integer serverId) {
        return configCache.get(serverId);
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

            // 获取服务器配置并缓存
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getOrCreateSetting(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                log.warn("获取服务器配置失败，跳过任务创建: serverId={}", serverId);
                return;
            }


            // 启动每服务器的单线程 worker：串行执行该服务器所有步骤
            Future<?> serverWorker = executorService.submit(() -> {
                // 上次执行时间戳
                long lastMetrics = 0L;
                long lastAlerts = 0L;
                long lastHealth = 0L;
                long lastDataPush = 0L;
                long lastPort = 0L;
                long lastOnline = 0L;
                long lastLatency = 0L;
                long lastLogCleanup = 0L;
                long lastCacheCleanup = 0L;
                long lastWsCleanup = 0L;
                while (taskInfo.isTaskRunning() && running) {
                    try {
                        MonitorSysGenServerSetting s = getCachedConfig(server.getMonitorSysGenServerId());
                        if (s == null) { Thread.sleep(5_000L); continue; }
                        long now = System.currentTimeMillis();
                        // 指标采集（非API上报）
                        String rm = s.getMonitorSysGenServerSettingDataReportMethod();
                        if (!"API".equalsIgnoreCase(rm)) {
                            int freq = getEffectiveCollectionFrequency(s);
                            if (now - lastMetrics >= freq * 1000L) { metricsCollectionStep.run(server, s); lastMetrics = now; }
                        }
                        // 告警检测
                        if (s.getMonitorSysGenServerSettingAlertEnabled() != null && s.getMonitorSysGenServerSettingAlertEnabled() == 1) {
                            int ai = getEffectiveAlertInterval(s);
                            if (now - lastAlerts >= ai * 1000L) { alertDetectionStep.run(server, s); lastAlerts = now; }
                        }
                        // 健康检查
                        int hi = getEffectiveHealthCheckInterval(s);
                        if (now - lastHealth >= hi * 1000L) { healthCheckStep.run(server, s); lastHealth = now; }
                        // 数据推送（非API上报）
                        if (!"API".equalsIgnoreCase(rm)) {
                            int freq = getEffectiveCollectionFrequency(s);
                            if (now - lastDataPush >= freq * 1000L) { dataPushStep.run(server, s); lastDataPush = now; }
                        }
                        // 端口监控
                        if (s.getMonitorSysGenServerSettingPortMonitorEnabled() != null && s.getMonitorSysGenServerSettingPortMonitorEnabled() == 1) {
                            Integer itv = s.getMonitorSysGenServerSettingPortCheckInterval();
                            if (itv == null || itv <= 0) itv = 300;
                            if (now - lastPort >= itv * 1000L) { portMonitorStep.run(server, s); lastPort = now; }
                        }
                        // 在线检测
                        if (s.getMonitorSysGenServerSettingOnlineCheckEnabled() != null && s.getMonitorSysGenServerSettingOnlineCheckEnabled() == 1) {
                            Integer itv = s.getMonitorSysGenServerSettingOnlineCheckInterval();
                            if (itv == null || itv <= 0) itv = 30;
                            if (now - lastOnline >= itv * 1000L) { onlineCheckStep.run(server, s); lastOnline = now; }
                        }
                        // 延迟检测
                        if (s.getMonitorSysGenServerSettingLatencyCheckEnabled() != null && s.getMonitorSysGenServerSettingLatencyCheckEnabled() == 1) {
                            Integer itv = s.getMonitorSysGenServerSettingLatencyCheckInterval();
                            if (itv == null || itv <= 0) itv = 60;
                            if (now - lastLatency >= itv * 1000L) { latencyCheckStep.run(server, s); lastLatency = now; }
                        }
                        // 日志清理（每日）
                        if (s.getMonitorSysGenServerSettingLogCleanupEnabled() != null && s.getMonitorSysGenServerSettingLogCleanupEnabled() == 1) {
                            if (now - lastLogCleanup >= 24L * 3_600_000) { logCleanupStep.run(server, s); lastLogCleanup = now; }
                        }
                        // 缓存清理（每小时）
                        if (s.getMonitorSysGenServerSettingTempFileCleanupEnabled() != null && s.getMonitorSysGenServerSettingTempFileCleanupEnabled() == 1) {
                            if (now - lastCacheCleanup >= 3_600_000) { cacheCleanupStep.run(server, s); lastCacheCleanup = now; }
                        }
                        // WebSocket 清理（默认每10分钟）
                        if (s.getMonitorSysGenServerSettingWebSocketCleanupEnabled() != null && s.getMonitorSysGenServerSettingWebSocketCleanupEnabled() == 1) {
                            Integer itv = s.getMonitorSysGenServerSettingWebSocketCleanupInterval();
                            if (itv == null || itv <= 0) itv = 10;
                            if (now - lastWsCleanup >= itv * 60_000L) { webSocketCleanupStep.run(server, s); lastWsCleanup = now; }
                        }
                        Thread.sleep(1_000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("服务器worker异常: serverId={}", server.getMonitorSysGenServerId(), e);
                        try { Thread.sleep(5_000L); } catch (InterruptedException ignore) { Thread.currentThread().interrupt(); break; }
                    }
                }
            });
            taskInfo.setServerWorkerTask(serverWorker);

            MonitorSysGenServerSetting setting = settingResult.getData();
            configCache.put(serverId, setting);

            // 移除旧任务
            removeServerTasks(serverId);

            // 创建新的任务信息
            ServerTaskInfo taskInfo = new ServerTaskInfo(serverId);
            taskInfo.setCurrentSetting(setting);
            taskInfo.updateConfigCheckTime();

            // 创建指标收集任务（仅对非API上报方式）
            if (!"API".equalsIgnoreCase(setting.getMonitorSysGenServerSettingDataReportMethod())) {
                createMetricsCollectionTask(taskInfo, server, setting);
            }

            // 创建告警检测任务
            createAlertDetectionTask(taskInfo, server, setting);

            // 创建健康检查任务
            createHealthCheckTask(taskInfo, server, setting);

            // 创建数据推送任务
            createDataPushTask(taskInfo, server, setting);

            // 创建端口监控任务
            createPortMonitorTask(taskInfo, server, setting);

            // 创建在线状态检测任务
            createOnlineCheckTask(taskInfo, server, setting);

            // 创建延迟检测任务
            createLatencyCheckTask(taskInfo, server, setting);

            // 创建日志清理任务
            createLogCleanupTask(taskInfo, server, setting);

            // 创建缓存清理任务
            createCacheCleanupTask(taskInfo, server, setting);

            // 创建WebSocket清理任务
            createWebSocketCleanupTask(taskInfo, server, setting);

            serverTasks.put(serverId, taskInfo);
            log.info("为服务器 {} 创建任务完成", server.getMonitorSysGenServerName());

        } catch (Exception e) {
            log.error("为服务器创建任务失败: serverId={}", serverId, e);
        }
    }

    /**
     * 创建指标收集任务
     */
    private void createMetricsCollectionTask(ServerTaskInfo taskInfo, MonitorSysGenServer server,
            MonitorSysGenServerSetting setting) {
        if (setting.getMonitorSysGenServerSettingMonitorEnabled() != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 统一由 serverWorker 调度，此处保持兼容逻辑为轻量等待
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        // 合并线程模型后，该任务已纳入 serverWorker 内部调度
    }

    /**
     * 创建告警检测任务
     */
    private void createAlertDetectionTask(ServerTaskInfo taskInfo, MonitorSysGenServer server,
            MonitorSysGenServerSetting setting) {
        if (setting.getMonitorSysGenServerSettingAlertEnabled() != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null || currentSetting.getMonitorSysGenServerSettingAlertEnabled() != 1) {
                        Thread.sleep(120000); // 如果告警被禁用，等待2分钟后重新检查
                        continue;
                    }

                    // 执行告警检测（委派到步骤类）
                    alertDetectionStep.run(server, currentSetting);

                    // 使用配置的告警检测间隔，默认2分钟
                    Integer alertInterval = getEffectiveAlertInterval(currentSetting);

                    // 记录任务执行信息
                    ServerLogUtils.logDebug(server.getMonitorSysGenServerId(),
                            String.format("告警检测任务执行完成，下次执行间隔: %d秒", alertInterval));

                    Thread.sleep(alertInterval * 1000L);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("告警检测任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    ServerLogUtils.logError(server.getMonitorSysGenServerId(), "告警检测任务执行异常", e);
                    try {
                        Thread.sleep(120000); // 异常时等待2分钟
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        // 合并线程模型后，该任务已纳入 serverWorker 内部调度
    }

    /**
     * 创建健康检查任务
     */
    private void createHealthCheckTask(ServerTaskInfo taskInfo, MonitorSysGenServer server,
            MonitorSysGenServerSetting setting) {
        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null) {
                        Thread.sleep(300000); // 如果配置不存在，等待5分钟后重新检查
                        continue;
                    }

                    // 执行健康检查
                    performHealthCheck(server, currentSetting);

                    // 使用配置的健康检查间隔
                    Integer interval = getEffectiveHealthCheckInterval(currentSetting);
                    Thread.sleep(interval * 1000L);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("健康检查任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    try {
                        Integer interval = setting != null ? getEffectiveHealthCheckInterval(setting) : 300;
                        Thread.sleep(interval * 1000L); // 异常时按配置等待
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        taskInfo.setHealthCheckTask(task);
        log.debug("为服务器 {} 创建健康检查任务", server.getMonitorSysGenServerName());
    }

    @SuppressWarnings("unused") // 保留用于未来扩展
    private void pushDailyHealthReport() {
        try {
            serverMetricsWebSocketService.pushDailyHealthReport();
        } catch (Exception e) {
            log.error("推送每日健康报告失败", e);
        }
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
            // 使用统一告警服务检测单个服务器的告警（传入缓存配置，避免DB访问）
            unifiedAlertService.checkAndPushServerAlerts(server, null, setting);
        } catch (Exception e) {
            log.error("检测推送告警失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 执行健康检查
     */
    private void performHealthCheck(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            // 使用拆分后的步骤组件执行
            healthCheckStep.run(server, setting);
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

    private void cleanupInvalidTasks() {
        try {
            // 获取当前活跃的服务器ID
            List<Integer> activeServerIds = serverService.list().stream()
                    .filter(server -> server.getMonitorSysGenServerStatus() != null
                            && server.getMonitorSysGenServerStatus() == 1)
                    .map(MonitorSysGenServer::getMonitorSysGenServerId).toList();

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

    private void processGlobalAlerts() {
        try {
            unifiedAlertService.pushAllServerAlerts();
        } catch (Exception e) {
            log.error("处理全局告警失败", e);
        }
    }

    /**
     * 获取任务统计信息
     */
    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalServerTasks", serverTasks.size());
        stats.put("totalGlobalTasks", globalTasks.size());

        long activeServerTasks = serverTasks.values().stream().mapToLong(taskInfo -> {
            long count = 0;
            if (taskInfo.getMetricsCollectionTask() != null && !taskInfo.getMetricsCollectionTask().isCancelled())
                count++;
            if (taskInfo.getAlertDetectionTask() != null && !taskInfo.getAlertDetectionTask().isCancelled())
                count++;
            if (taskInfo.getHealthCheckTask() != null && !taskInfo.getHealthCheckTask().isCancelled())
                count++;
            return count;
        }).sum();

        long activeGlobalTasks = globalTasks.values().stream().mapToLong(task -> task.isCancelled() ? 0 : 1).sum();

        stats.put("activeServerTasks", activeServerTasks);
        stats.put("activeGlobalTasks", activeGlobalTasks);

        return stats;
    }

    /**
     * 智能更新服务器任务 只有当配置真正发生变化时才重新创建任务
     */
    public void smartUpdateServerTasks(Integer serverId) {
        try {
            ServerTaskInfo taskInfo = serverTasks.get(serverId);
            if (taskInfo == null) {
                // 如果任务不存在，直接创建
                createServerTasks(serverId);
                return;
            }

            // 获取最新的服务器设置
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getOrCreateSetting(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                log.warn("无法获取服务器设置，跳过任务更新: serverId={}", serverId);
                return;
            }

            MonitorSysGenServerSetting newSetting = settingResult.getData();

            // 检查是否需要更新
            if (needsTaskUpdate(taskInfo, newSetting)) {
                log.info("检测到服务器 {} 配置变化，重新创建任务", serverId);

                // 记录配置变更信息
                if (taskInfo.getCurrentSetting() != null) {
                    logConfigurationChange(serverId, taskInfo.getCurrentSetting(), newSetting);
                }

                createServerTasks(serverId);
            } else {
                log.debug("服务器 {} 配置未变化，跳过任务更新", serverId);
            }
        } catch (Exception e) {
            log.error("智能更新服务器任务失败: serverId={}", serverId, e);
        }
    }

    /**
     * 获取有效的数据收集频率 优先级：数据采集频率 > 监控间隔 > 默认值
     */
    private Integer getEffectiveCollectionFrequency(MonitorSysGenServerSetting setting) {
        Integer frequency = setting.getMonitorSysGenServerSettingDataCollectionFrequency();

        // 如果数据采集频率未设置或无效，使用监控间隔
        if (frequency == null || frequency <= 0) {
            frequency = setting.getMonitorSysGenServerSettingMonitorInterval();
        }

        // 如果监控间隔也未设置或无效，使用默认值
        if (frequency == null || frequency <= 0) {
            frequency = 30; // 默认30秒
        }

        // 确保频率在合理范围内
        if (frequency < 10) {
            frequency = 10; // 最小10秒
        } else if (frequency > 3600) {
            frequency = 3600; // 最大1小时
        }

        return frequency;
    }

    /**
     * 获取有效的告警检测间隔 默认2分钟，确保在合理范围内
     */
    private Integer getEffectiveAlertInterval(MonitorSysGenServerSetting setting) {
        // 告警检测间隔通常比数据采集间隔长，默认2分钟
        Integer interval = 120; // 默认2分钟

        // 可以根据数据采集频率动态调整，但不少于1分钟
        Integer collectionFreq = getEffectiveCollectionFrequency(setting);
        if (collectionFreq != null && collectionFreq > 0) {
            // 告警检测间隔至少是数据采集频率的2倍，但不少于60秒
            interval = Math.max(60, collectionFreq * 2);
        }

        // 确保间隔在合理范围内
        if (interval < 60) {
            interval = 60; // 最小1分钟
        } else if (interval > 1800) {
            interval = 1800; // 最大30分钟
        }

        return interval;
    }


    /**
     * 获取有效的健康检查间隔（秒）
     * 优先级：客户端健康检查间隔 > 监控间隔 > 默认300秒
     */
    private Integer getEffectiveHealthCheckInterval(MonitorSysGenServerSetting setting) {
        if (setting == null) {
            return 300;
        }
        Integer interval = setting.getMonitorSysGenServerSettingClientHealthCheckInterval();
        if (interval == null || interval <= 0) {
            interval = setting.getMonitorSysGenServerSettingMonitorInterval();
        }
        if (interval == null || interval <= 0) {
            interval = 300;
        }
        return Math.max(10, Math.min(interval, 3600));
    }

    /**
     * 获取有效的监控间隔
     */
    private Integer getEffectiveMonitorInterval(MonitorSysGenServerSetting setting) {
        Integer interval = setting.getMonitorSysGenServerSettingMonitorInterval();
        if (interval == null || interval <= 0) {
            interval = 120; // 默认2分钟
        }

        // 确保间隔在合理范围内
        if (interval < 30) {
            interval = 30; // 最小30秒
        } else if (interval > 3600) {
            interval = 3600; // 最大1小时
        }

        return interval;
    }

    /**
     * 检查任务配置是否需要更新
     */
    private boolean needsTaskUpdate(ServerTaskInfo taskInfo, MonitorSysGenServerSetting newSetting) {
        if (taskInfo.getCurrentSetting() == null) {
            return true;
        }

        MonitorSysGenServerSetting currentSetting = taskInfo.getCurrentSetting();

        // 检查数据收集频率是否变化
        Integer currentFreq = getEffectiveCollectionFrequency(currentSetting);
        Integer newFreq = getEffectiveCollectionFrequency(newSetting);
        if (!currentFreq.equals(newFreq)) {
            log.info("服务器 {} 数据收集频率变化: {} -> {} 秒", taskInfo.getServerId(), currentFreq, newFreq);
            return true;
        }

        // 检查告警检测间隔是否变化
        Integer currentAlertInterval = getEffectiveAlertInterval(currentSetting);
        Integer newAlertInterval = getEffectiveAlertInterval(newSetting);
        if (!currentAlertInterval.equals(newAlertInterval)) {
            log.info("服务器 {} 告警检测间隔变化: {} -> {} 秒", taskInfo.getServerId(), currentAlertInterval, newAlertInterval);
            return true;
        }

        // 检查监控间隔是否变化
        Integer currentInterval = getEffectiveMonitorInterval(currentSetting);
        Integer newInterval = getEffectiveMonitorInterval(newSetting);
        if (!currentInterval.equals(newInterval)) {
            log.info("服务器 {} 监控间隔变化: {} -> {} 秒", taskInfo.getServerId(), currentInterval, newInterval);
            return true;
        }

        // 检查监控启用状态是否变化
        if (!Objects.equals(currentSetting.getMonitorSysGenServerSettingMonitorEnabled(),
                newSetting.getMonitorSysGenServerSettingMonitorEnabled())) {
            log.info("服务器 {} 监控启用状态变化: {} -> {}", taskInfo.getServerId(),
                    currentSetting.getMonitorSysGenServerSettingMonitorEnabled(),
                    newSetting.getMonitorSysGenServerSettingMonitorEnabled());
            return true;
        }

        // 检查告警启用状态是否变化
        if (!Objects.equals(currentSetting.getMonitorSysGenServerSettingAlertEnabled(),
                newSetting.getMonitorSysGenServerSettingAlertEnabled())) {
            log.info("服务器 {} 告警启用状态变化: {} -> {}", taskInfo.getServerId(),
                    currentSetting.getMonitorSysGenServerSettingAlertEnabled(),
                    newSetting.getMonitorSysGenServerSettingAlertEnabled());
            return true;
        }

        // 检查数据上报方式是否变化
        if (!Objects.equals(currentSetting.getMonitorSysGenServerSettingDataReportMethod(),
                newSetting.getMonitorSysGenServerSettingDataReportMethod())) {
            log.info("服务器 {} 数据上报方式变化: {} -> {}", taskInfo.getServerId(),
                    currentSetting.getMonitorSysGenServerSettingDataReportMethod(),
                    newSetting.getMonitorSysGenServerSettingDataReportMethod());
            return true;
        }

        return false;
    }

    /**
     * 记录配置变更信息
     */
    private void logConfigurationChange(Integer serverId, MonitorSysGenServerSetting oldSetting,
            MonitorSysGenServerSetting newSetting) {
        StringBuilder changeLog = new StringBuilder("服务器配置变更: ");

        // 检查数据采集频率变化
        Integer oldFreq = getEffectiveCollectionFrequency(oldSetting);
        Integer newFreq = getEffectiveCollectionFrequency(newSetting);
        if (!oldFreq.equals(newFreq)) {
            changeLog.append(String.format("数据采集频率: %d秒 -> %d秒; ", oldFreq, newFreq));
        }

        // 检查告警检测间隔变化
        Integer oldAlertInterval = getEffectiveAlertInterval(oldSetting);
        Integer newAlertInterval = getEffectiveAlertInterval(newSetting);
        if (!oldAlertInterval.equals(newAlertInterval)) {
            changeLog.append(String.format("告警检测间隔: %d秒 -> %d秒; ", oldAlertInterval, newAlertInterval));
        }

        // 检查监控启用状态变化
        if (!Objects.equals(oldSetting.getMonitorSysGenServerSettingMonitorEnabled(),
                newSetting.getMonitorSysGenServerSettingMonitorEnabled())) {
            changeLog.append(String.format("监控启用状态: %s -> %s; ",
                    oldSetting.getMonitorSysGenServerSettingMonitorEnabled() == 1 ? "启用" : "禁用",
                    newSetting.getMonitorSysGenServerSettingMonitorEnabled() == 1 ? "启用" : "禁用"));
        }

        // 检查告警启用状态变化
        if (!Objects.equals(oldSetting.getMonitorSysGenServerSettingAlertEnabled(),
                newSetting.getMonitorSysGenServerSettingAlertEnabled())) {
            changeLog.append(String.format("告警启用状态: %s -> %s; ",
                    oldSetting.getMonitorSysGenServerSettingAlertEnabled() == 1 ? "启用" : "禁用",
                    newSetting.getMonitorSysGenServerSettingAlertEnabled() == 1 ? "启用" : "禁用"));
        }

        // 检查数据上报方式变化
        if (!Objects.equals(oldSetting.getMonitorSysGenServerSettingDataReportMethod(),
                newSetting.getMonitorSysGenServerSettingDataReportMethod())) {
            changeLog.append(
                    String.format("数据上报方式: %s -> %s; ", oldSetting.getMonitorSysGenServerSettingDataReportMethod(),
                            newSetting.getMonitorSysGenServerSettingDataReportMethod()));
        }

        // 记录到服务器日志
        if (changeLog.length() > "服务器配置变更: ".length()) {
            ServerLogUtils.logInfo(serverId, changeLog.toString());
        }
    }

    /**
     * 获取单个任务的状态
     */
    private Map<String, Object> getTaskStatus(Future<?> task) {
        Map<String, Object> status = new HashMap<>();
        if (task == null) {
            status.put("exists", false);
            status.put("status", "not_created");
        } else {
            status.put("exists", true);
            status.put("cancelled", task.isCancelled());
            status.put("done", task.isDone());
            status.put("status", task.isCancelled() ? "cancelled" : task.isDone() ? "completed" : "running");
        }
        return status;
    }

    /**
     * 获取服务器任务状态信息
     */
    public Map<String, Object> getServerTaskStatus(Integer serverId) {
        Map<String, Object> status = new HashMap<>();

        ServerTaskInfo taskInfo = serverTasks.get(serverId);
        if (taskInfo == null) {
            status.put("exists", false);
            status.put("message", "任务不存在");
            return status;
        }

        status.put("exists", true);
        status.put("serverId", serverId);

        // 检查各个任务的状态
        Map<String, Object> taskStatus = new HashMap<>();
        taskStatus.put("metricsCollection", getTaskStatus(taskInfo.getMetricsCollectionTask()));
        taskStatus.put("alertDetection", getTaskStatus(taskInfo.getAlertDetectionTask()));
        taskStatus.put("healthCheck", getTaskStatus(taskInfo.getHealthCheckTask()));
        taskStatus.put("dataPush", getTaskStatus(taskInfo.getDataPushTask()));
        taskStatus.put("portMonitor", getTaskStatus(taskInfo.getPortMonitorTask()));
        taskStatus.put("onlineCheck", getTaskStatus(taskInfo.getOnlineCheckTask()));
        taskStatus.put("latencyCheck", getTaskStatus(taskInfo.getLatencyCheckTask()));
        taskStatus.put("logCleanup", getTaskStatus(taskInfo.getLogCleanupTask()));
        taskStatus.put("cacheCleanup", getTaskStatus(taskInfo.getCacheCleanupTask()));
        taskStatus.put("webSocketCleanup", getTaskStatus(taskInfo.getWebSocketCleanupTask()));

        status.put("tasks", taskStatus);

        // 添加当前配置信息
        if (taskInfo.getCurrentSetting() != null) {
            Map<String, Object> config = new HashMap<>();
            config.put("collectionFrequency", getEffectiveCollectionFrequency(taskInfo.getCurrentSetting()));
            config.put("alertInterval", getEffectiveAlertInterval(taskInfo.getCurrentSetting()));
            config.put("monitorInterval", getEffectiveMonitorInterval(taskInfo.getCurrentSetting()));
            config.put("monitorEnabled", taskInfo.getCurrentSetting().getMonitorSysGenServerSettingMonitorEnabled());
            config.put("alertEnabled", taskInfo.getCurrentSetting().getMonitorSysGenServerSettingAlertEnabled());
            config.put("dataReportMethod",
                    taskInfo.getCurrentSetting().getMonitorSysGenServerSettingDataReportMethod());
            config.put("portMonitorEnabled",
                    taskInfo.getCurrentSetting().getMonitorSysGenServerSettingPortMonitorEnabled());
            config.put("onlineCheckEnabled",
                    taskInfo.getCurrentSetting().getMonitorSysGenServerSettingOnlineCheckEnabled());
            config.put("latencyCheckEnabled",
                    taskInfo.getCurrentSetting().getMonitorSysGenServerSettingLatencyCheckEnabled());
            status.put("config", config);
        }

        // 添加任务性能统计
        Map<String, Object> performance = new HashMap<>();
        performance.put("taskCount", taskStatus.size());
        performance.put("runningTasks", taskStatus.values().stream()
                .mapToInt(task -> (Boolean) ((Map<String, Object>) task).get("running") ? 1 : 0).sum());
        performance.put("lastUpdateTime", System.currentTimeMillis());
        status.put("performance", performance);

        return status;
    }

    /**
     * 获取所有任务的概览信息
     */
    public Map<String, Object> getAllTasksOverview() {
        Map<String, Object> overview = new HashMap<>();

        overview.put("totalServers", serverTasks.size());
        overview.put("globalTasks", globalTasks.size());

        // 统计各种状态的任务数量
        int activeServers = 0;
        int inactiveServers = 0;

        for (ServerTaskInfo taskInfo : serverTasks.values()) {
            boolean hasActiveTasks = taskInfo.getMetricsCollectionTask() != null
                    && !taskInfo.getMetricsCollectionTask().isCancelled()
                    && !taskInfo.getMetricsCollectionTask().isDone();

            if (hasActiveTasks) {
                activeServers++;
            } else {
                inactiveServers++;
            }
        }

        overview.put("activeServers", activeServers);
        overview.put("inactiveServers", inactiveServers);

        return overview;
    }

    /**
     * 清理指标推送触发缓存
     */
    @SuppressWarnings("unused") // 保留用于未来扩展
    private void cleanupMetricsTriggerCache() {
        try {
            // 直接调用服务方法，不依赖具体实现类
            log.debug("清理指标推送触发缓存完成");
        } catch (Exception e) {
            log.error("清理指标推送触发缓存失败", e);
        }
    }

    /**
     * 将Prometheus数据转换为标准服务器指标格式
     */
    private MonitorSysGenServerMetrics convertToServerMetrics(MonitorSysGenServer server,
            ServerMetricsData metricsData) {
        try {
            MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();
            metrics.setMonitorSysGenServerId(server.getMonitorSysGenServerId());
            metrics.setMonitorSysGenServerMetricsCollectTime(LocalDateTime.now());

            // 转换基本指标
            if (metricsData.getCpuUsage() != null) {
                metrics.setMonitorSysGenServerMetricsCpuUsage(BigDecimal.valueOf(metricsData.getCpuUsage()));
            }
            if (metricsData.getMemoryUsage() != null) {
                metrics.setMonitorSysGenServerMetricsMemoryUsage(BigDecimal.valueOf(metricsData.getMemoryUsage()));
            }
            if (metricsData.getDiskUsage() != null) {
                metrics.setMonitorSysGenServerMetricsDiskUsage(BigDecimal.valueOf(metricsData.getDiskUsage()));
            }

            return metrics;
        } catch (Exception e) {
            log.error("转换Prometheus数据失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return null;
        }
    }

    /**
     * 创建数据推送任务
     */
    private void createDataPushTask(ServerTaskInfo taskInfo, MonitorSysGenServer server,
            MonitorSysGenServerSetting setting) {
        if (!setting.isEnabled()) {
            return;
        }

        String reportMethod = setting.getMonitorSysGenServerSettingDataReportMethod();
        if (reportMethod == null || "API".equalsIgnoreCase(reportMethod)) {
            return; // API上报不需要推送任务
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null || currentSetting.getMonitorSysGenServerSettingMonitorEnabled() != 1) {
                        Thread.sleep(30000); // 如果监控被禁用，等待30秒后重新检查
                        continue;
                    }

                    String currentReportMethod = currentSetting.getMonitorSysGenServerSettingDataReportMethod();
                    if (currentReportMethod == null || "API".equalsIgnoreCase(currentReportMethod)) {
                        Thread.sleep(30000); // API上报不需要推送，等待30秒后重新检查
                        continue;
                    }

                    Integer frequency = getEffectiveCollectionFrequency(currentSetting);

                    // 执行数据推送（委派到步骤类）
                    dataPushStep.run(server, currentSetting);

                    // 记录任务执行信息
                    ServerLogUtils.logDebug(server.getMonitorSysGenServerId(),
                            String.format("数据推送任务执行完成，推送方式: %s，下次执行间隔: %d秒", currentReportMethod, frequency));

                    // 按配置的频率休眠
                    Thread.sleep(frequency * 1000L);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("数据推送任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    ServerLogUtils.logError(server.getMonitorSysGenServerId(), "数据推送任务执行异常", e);
                    try {
                        Thread.sleep(30000); // 异常时等待30秒
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        // 合并线程模型后，该任务已纳入 serverWorker 内部调度
    }

    /**
     * 创建端口监控任务
     */
    private void createPortMonitorTask(ServerTaskInfo taskInfo, MonitorSysGenServer server,
            MonitorSysGenServerSetting setting) {
        Integer monitorSysGenServerSettingPortMonitorEnabled = setting
                .getMonitorSysGenServerSettingPortMonitorEnabled();
        if (null == monitorSysGenServerSettingPortMonitorEnabled || monitorSysGenServerSettingPortMonitorEnabled != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null
                            || currentSetting.getMonitorSysGenServerSettingPortMonitorEnabled() != 1) {
                        Thread.sleep(300000); // 如果端口监控被禁用，等待5分钟后重新检查
                        continue;
                    }

                    Integer frequency = currentSetting.getMonitorSysGenServerSettingPortCheckInterval();
                    if (frequency == null || frequency <= 0) {
                        frequency = 300; // 默认5分钟
                    }

                    // 执行端口监控（委派到步骤类）
                    portMonitorStep.run(server, currentSetting);

                    // 按配置的频率休眠
                    Thread.sleep(frequency * 1000L);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("端口监控任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    try {
                        Thread.sleep(300000); // 异常时等待5分钟
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        // 合并线程模型后，该任务已纳入 serverWorker 内部调度
    }

    /**
     * 执行数据推送
     */
    private void executeDataPush(MonitorSysGenServer server, MonitorSysGenServerSetting setting, String reportMethod) {
        try {
            if ("LOCAL".equalsIgnoreCase(reportMethod)) {
                executeLocalDataPush(server, setting);
            } else if ("PROMETHEUS".equalsIgnoreCase(reportMethod)) {
                executePrometheusDataPush(server, setting);
            }
        } catch (Exception e) {
            log.error("执行数据推送失败: serverId={}, method={}", server.getMonitorSysGenServerId(), reportMethod, e);
        }
    }

    /**
     * 执行Prometheus数据推送
     */
    private void executePrometheusDataPush(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            // 从Prometheus收集数据
            ServerMetricsData metricsData = prometheusDataCollectorService
                    .collectMetrics(server.getMonitorSysGenServerId(), setting);

            if (metricsData != null) {
                // 将Prometheus数据转换为标准指标格式并推送
                MonitorSysGenServerMetrics metrics = convertToServerMetrics(server, metricsData);
                serverMetricsWebSocketService.pushSingleServerMetrics(server, metrics);
                log.debug("Prometheus数据推送成功，服务器ID: {}", server.getMonitorSysGenServerId());
            }
        } catch (Exception e) {
            log.error("Prometheus数据推送失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 执行本地数据推送
     */
    private void executeLocalDataPush(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        if (server.getMonitorSysGenServerIsLocal() != 1) {
            return; // 非本地服务器跳过
        }

        try {
            // 收集本地系统数据
            MonitorSysGenServerMetrics metrics = collectLocalMetrics(server);
            if (metrics != null) {
                serverMetricsWebSocketService.pushSingleServerMetrics(server, metrics);
                log.debug("本地数据推送成功，服务器ID: {}", server.getMonitorSysGenServerId());
            }
        } catch (Exception e) {
            log.error("本地数据推送失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 执行端口监控
     */
    private void executePortMonitor(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            String portList = setting.getMonitorSysGenServerSettingMonitorPorts();
            if (portList == null || portList.trim().isEmpty()) {
                return;
            }

            String[] ports = portList.split(",");
            Map<String, Object> portStatuses = new HashMap<>();

            for (String portStr : ports) {
                try {
                    int port = Integer.parseInt(portStr.trim());
                    boolean isOpen = checkPortStatus(server.getMonitorSysGenServerHost(), port);

                    Map<String, Object> status = new HashMap<>();
                    status.put("port", port);
                    status.put("status", isOpen ? "OPEN" : "CLOSED");
                    status.put("checkTime", LocalDateTime.now().toString());

                    portStatuses.put(String.valueOf(port), status);
                } catch (NumberFormatException e) {
                    log.warn("无效的端口号: {}", portStr);
                }
            }

            // 推送端口状态到WebSocket
            Map<String, Object> message = new HashMap<>();
            message.put("type", "port_status");
            message.put("serverId", server.getMonitorSysGenServerId());
            message.put("serverName", server.getMonitorSysGenServerName());
            message.put("portStatuses", portStatuses);
            message.put("timestamp", System.currentTimeMillis());

            // 通过WebSocket服务推送端口状态信息
            try {
                serverMetricsWebSocketService.pushSingleServerMetrics(server, null);
            } catch (Exception e) {
                log.warn("推送端口状态信息失败: serverId={}", server.getMonitorSysGenServerId(), e);
            }

            log.debug("端口状态已推送到WebSocket，服务器ID: {}, 端口数量: {}", server.getMonitorSysGenServerId(), portStatuses.size());

        } catch (Exception e) {
            log.error("端口监控失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 创建在线状态检测任务
     */
    private void createOnlineCheckTask(ServerTaskInfo taskInfo, MonitorSysGenServer server,
            MonitorSysGenServerSetting setting) {
        Integer monitorSysGenServerSettingOnlineCheckEnabled = setting
                .getMonitorSysGenServerSettingOnlineCheckEnabled();
        if (null == monitorSysGenServerSettingOnlineCheckEnabled || monitorSysGenServerSettingOnlineCheckEnabled != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null
                            || currentSetting.getMonitorSysGenServerSettingOnlineCheckEnabled() != 1) {
                        Thread.sleep(30000); // 如果在线检测被禁用，等待30秒后重新检查
                        continue;
                    }

                    Integer interval = currentSetting.getMonitorSysGenServerSettingOnlineCheckInterval();
                    if (interval == null || interval <= 0) {
                        interval = 30; // 默认30秒
                    }

                    // 执行在线状态检测（委派到步骤类）
                    onlineCheckStep.run(server, currentSetting);

                    // 按配置的间隔休眠
                    Thread.sleep(interval * 1000L);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("在线状态检测任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    try {
                        Thread.sleep(30000); // 异常时等待30秒
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        taskInfo.setOnlineCheckTask(task);
        log.debug("为服务器 {} 创建在线状态检测任务", server.getMonitorSysGenServerName());
    }

    /**
     * 收集本地系统指标
     */
    private MonitorSysGenServerMetrics collectLocalMetrics(MonitorSysGenServer server) {
        MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();

        try {
            // 使用本地数据收集器收集系统信息
            Map<String, Object> systemData = localSystemDataCollector.collectSystemData();

            metrics.setMonitorSysGenServerId(server.getMonitorSysGenServerId());
            metrics.setMonitorSysGenServerMetricsCollectTime(LocalDateTime.now());

            // 设置CPU信息
            if (systemData.containsKey("cpuUsage")) {
                Double cpuUsage = (Double) systemData.get("cpuUsage");
                metrics.setMonitorSysGenServerMetricsCpuUsage(BigDecimal.valueOf(cpuUsage));
            }
            if (systemData.containsKey("cpuLogicalProcessors")) {
                Integer cpuCores = (Integer) systemData.get("cpuLogicalProcessors");
                metrics.setMonitorSysGenServerMetricsCpuCores(cpuCores);
            }

            // 设置内存信息
            if (systemData.containsKey("totalMemory") && systemData.containsKey("usedMemory")) {
                Long totalMemory = (Long) systemData.get("totalMemory");
                Long usedMemory = (Long) systemData.get("usedMemory");
                Double memoryUsagePercent = (Double) systemData.get("memoryUsagePercent");

                // 转换为GB
                metrics.setMonitorSysGenServerMetricsMemoryTotal(totalMemory / (1024 * 1024 * 1024));
                metrics.setMonitorSysGenServerMetricsMemoryUsed(usedMemory / (1024 * 1024 * 1024));
                metrics.setMonitorSysGenServerMetricsMemoryFree((totalMemory - usedMemory) / (1024 * 1024 * 1024));
                metrics.setMonitorSysGenServerMetricsMemoryUsage(BigDecimal.valueOf(memoryUsagePercent));
            }

            // 设置磁盘信息（从文件系统信息中获取）
            if (systemData.containsKey("fileSystems")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> fileSystems = (List<Map<String, Object>>) systemData.get("fileSystems");
                if (!fileSystems.isEmpty()) {
                    // 使用第一个文件系统的信息
                    Map<String, Object> fs = fileSystems.get(0);
                    Long totalSpace = (Long) fs.get("totalSpace");
                    Long freeSpace = (Long) fs.get("freeSpace");
                    Double diskUsage = (Double) fs.get("usagePercent");

                    if (totalSpace != null && freeSpace != null) {
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

            // 设置系统信息
            if (systemData.containsKey("systemUptime")) {
                Long uptime = (Long) systemData.get("systemUptime");
                metrics.setMonitorSysGenServerMetricsUptime(uptime);
            }
            if (systemData.containsKey("processCount")) {
                Integer processCount = (Integer) systemData.get("processCount");
                metrics.setMonitorSysGenServerMetricsProcessCount(processCount);
            }

            // 设置状态为在线
            metrics.setMonitorSysGenServerMetricsStatus(1);

            Map<String, String> osInfo = new HashMap<>();
            osInfo.put("osName", systemData.get("osName").toString());
            osInfo.put("osArch", systemData.get("osArch").toString());
            osInfo.put("osVersion", systemData.get("osVersion").toString());
            osInfo.put("hostname", InetAddress.getByName(server.getMonitorSysGenServerHost()).getHostName());
            metrics.setMonitorSysGenServerMetricsOsInfo(Json.toJson(osInfo));
            return metrics;

        } catch (Exception e) {
            log.error("收集本地系统指标失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return null;
        }
    }

    /**
     * 检查端口状态
     */
    private boolean checkPortStatus(String host, int port) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 3000); // 3秒超时
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ICMP可达性探测（不抛异常）
     */
    private boolean isHostReachable(String host, int timeoutMs) {
        try {
            if (host == null || host.isEmpty()) {
                return false;
            }
            return java.net.InetAddress.getByName(host).isReachable(timeoutMs);
        } catch (Throwable e) {
            return false;
        }
    }

    // ==================== 新增任务创建方法 ====================

    /**
     * 创建延迟检测任务
     */
    private void createLatencyCheckTask(ServerTaskInfo taskInfo, MonitorSysGenServer server,
            MonitorSysGenServerSetting setting) {
        if (null == setting) {
            return;
        }
        Integer monitorSysGenServerSettingLatencyCheckEnabled = setting
                .getMonitorSysGenServerSettingLatencyCheckEnabled();
        if (null == monitorSysGenServerSettingLatencyCheckEnabled
                || monitorSysGenServerSettingLatencyCheckEnabled != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null
                            || currentSetting.getMonitorSysGenServerSettingLatencyCheckEnabled() != 1) {
                        Thread.sleep(60000); // 如果延迟检测被禁用，等待1分钟后重新检查
                        continue;
                    }

                    Integer interval = currentSetting.getMonitorSysGenServerSettingLatencyCheckInterval();
                    if (interval == null || interval <= 0) {
                        interval = 60; // 默认60秒
                    }

                    // 执行延迟检测（委派到步骤类）
                    latencyCheckStep.run(server, currentSetting);

                    // 按配置的间隔休眠
                    Thread.sleep(interval * 1000L);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("延迟检测任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    try {
                        Thread.sleep(60000); // 异常时等待1分钟
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        taskInfo.setLatencyCheckTask(task);
        log.debug("为服务器 {} 创建延迟检测任务", server.getMonitorSysGenServerName());
    }

    /**
     * 创建日志清理任务
     */
    private void createLogCleanupTask(ServerTaskInfo taskInfo, MonitorSysGenServer server,
            MonitorSysGenServerSetting setting) {
        Integer monitorSysGenServerSettingLogCleanupEnabled = setting.getMonitorSysGenServerSettingLogCleanupEnabled();
        if (null == monitorSysGenServerSettingLogCleanupEnabled || monitorSysGenServerSettingLogCleanupEnabled != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null
                            || currentSetting.getMonitorSysGenServerSettingLogCleanupEnabled() != 1) {
                        Thread.sleep(3600000); // 如果日志清理被禁用，等待1小时后重新检查
                        continue;
                    }

                    // 执行日志清理（委派到步骤类）
                    logCleanupStep.run(server, currentSetting);

                    // 每天执行一次
                    Thread.sleep(24 * 3600000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("日志清理任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    try {
                        Thread.sleep(3600000); // 异常时等待1小时
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        taskInfo.setLogCleanupTask(task);
        log.debug("为服务器 {} 创建日志清理任务", server.getMonitorSysGenServerName());
    }

    /**
     * 创建缓存清理任务
     */
    private void createCacheCleanupTask(ServerTaskInfo taskInfo, MonitorSysGenServer server,
            MonitorSysGenServerSetting setting) {
        if (null == setting) {
            return;
        }
        Integer monitorSysGenServerSettingTempFileCleanupEnabled = setting
                .getMonitorSysGenServerSettingTempFileCleanupEnabled();
        if (null == monitorSysGenServerSettingTempFileCleanupEnabled
                || monitorSysGenServerSettingTempFileCleanupEnabled != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null
                            || currentSetting.getMonitorSysGenServerSettingTempFileCleanupEnabled() != 1) {
                        Thread.sleep(3600000); // 如果缓存清理被禁用，等待1小时后重新检查
                        continue;
                    }

                    // 执行缓存清理（委派到步骤类）
                    cacheCleanupStep.run(server, currentSetting);

                    // 每小时执行一次
                    Thread.sleep(3600000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("缓存清理任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    try {
                        Thread.sleep(3600000); // 异常时等待1小时
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        taskInfo.setCacheCleanupTask(task);
        log.debug("为服务器 {} 创建缓存清理任务", server.getMonitorSysGenServerName());
    }

    /**
     * 创建WebSocket清理任务
     */
    private void createWebSocketCleanupTask(ServerTaskInfo taskInfo, MonitorSysGenServer server,
            MonitorSysGenServerSetting setting) {
        if (null == setting) {
            return;
        }
        Integer monitorSysGenServerSettingWebSocketCleanupEnabled = setting
                .getMonitorSysGenServerSettingWebSocketCleanupEnabled();
        if (null == monitorSysGenServerSettingWebSocketCleanupEnabled
                || monitorSysGenServerSettingWebSocketCleanupEnabled != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null
                            || currentSetting.getMonitorSysGenServerSettingWebSocketCleanupEnabled() != 1) {
                        Thread.sleep(600000); // 如果WebSocket清理被禁用，等待10分钟后重新检查
                        continue;
                    }

                    Integer interval = currentSetting.getMonitorSysGenServerSettingWebSocketCleanupInterval();
                    if (interval == null || interval <= 0) {
                        interval = 10; // 默认10分钟
                    }

                    // 执行WebSocket清理（委派到步骤类）
                    webSocketCleanupStep.run(server, currentSetting);

                    // 按配置的间隔休眠
                    Thread.sleep(interval * 60000L);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("WebSocket清理任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    try {
                        Thread.sleep(600000); // 异常时等待10分钟
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        taskInfo.setWebSocketCleanupTask(task);
        log.debug("为服务器 {} 创建WebSocket清理任务", server.getMonitorSysGenServerName());
    }

    /**
     * 检查文件上传任务状态
     */
    private void checkUploadTaskStatus() {
        try {
            log.debug("检查文件上传任务状态");
            // 这里可以调用相关服务检查上传任务状态
            // 暂时只记录日志
        } catch (Exception e) {
            log.error("检查文件上传任务状态失败", e);
        }
    }

    // ==================== 新增任务执行方法 ====================

    /**
     * 执行在线状态检测
     */
    private void executeOnlineCheck(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            // 优先ICMP可达，其次端口连通
            boolean reachable;
            try {
                reachable = isHostReachable(server.getMonitorSysGenServerHost(), 3000);
            } catch (Throwable ignore) {
                reachable = false;
            }
            boolean online = reachable;
            if (!reachable) {
                Integer port = server.getMonitorSysGenServerPort();
                if (port != null) {
                    online = checkPortStatus(server.getMonitorSysGenServerHost(), port);
                }
            }
            serverService.updateServerOnlineStatus(server.getMonitorSysGenServerId(), online);
            unifiedAlertService.checkAndPushConnectionAlerts(server.getMonitorSysGenServerId(), online, null, setting);
        } catch (Exception e) {
            log.error("在线状态检测失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 执行延迟检测
     */
    private void executeLatencyCheck(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            long start = System.nanoTime();
            boolean reachable = false;
            try {
                reachable = isHostReachable(server.getMonitorSysGenServerHost(), 3000);
            } catch (Throwable ignore) {}
            long latency = (System.nanoTime() - start) / 1_000_000L;

            if (!reachable) {
                // 兜底：端口连接耗时
                Integer port = server.getMonitorSysGenServerPort();
                if (port != null) {
                    start = System.nanoTime();
                    boolean ok = checkPortStatus(server.getMonitorSysGenServerHost(), port);
                    latency = (System.nanoTime() - start) / 1_000_000L;
                    serverService.updateServerOnlineStatus(server.getMonitorSysGenServerId(), ok);
                }
            }

            serverLatencyService.updateServerLatencyToRedis(server.getMonitorSysGenServerId(), latency);
            serverLatencyService.pushLatencyToFrontend(server.getMonitorSysGenServerId(), latency);

            Long threshold = setting.getMonitorSysGenServerSettingResponseTimeAlertThreshold();
            if (threshold != null && threshold > 0 && latency >= threshold) {
                unifiedAlertService.checkAndPushConnectionAlerts(server.getMonitorSysGenServerId(), null, latency, setting);
            }
        } catch (Exception e) {
            log.error("延迟检测失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 执行日志清理
     */
    private void executeLogCleanup(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            log.debug("执行服务器 {} 日志清理", server.getMonitorSysGenServerName());
            Integer retentionDays = setting.getMonitorSysGenServerSettingMetricsRetentionDays();
            if (retentionDays == null || retentionDays <= 0) {
                retentionDays = 30;
            }
            // 使用指标服务清理过期指标（日志相关索引同周期）
            LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);
            try {
                // 调用指标服务清理（通过 MetricsService 接口）
                var rr = metricsService.cleanupExpiredMetrics(expireTime);
                log.debug("指标清理结果: {}", rr);
            } catch (Throwable ignore) {
            }
        } catch (Exception e) {
            log.error("日志清理失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 执行缓存清理
     */
    private void executeCacheCleanup(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            log.debug("执行服务器 {} 缓存清理", server.getMonitorSysGenServerName());
            // 仅调用服务层预留的清理方法，避免直接操作文件系统
            serverService.cleanupTempFiles();
            serverService.cleanupCache();
        } catch (Exception e) {
            log.error("缓存清理失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 执行WebSocket清理
     */
    private void executeWebSocketCleanup(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            log.debug("执行服务器 {} WebSocket清理", server.getMonitorSysGenServerName());
            // 目前WebSocket会话由各Handler管理，这里触发状态统计推送，促使前端刷新
            serverMetricsWebSocketService.pushConnectionStatistics();
        } catch (Exception e) {
            log.error("WebSocket清理失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    // ==================== 统一任务实现方法 ====================

    /**
     * 加载待处理任务到内存队列
     */
    @SuppressWarnings("unused") // 保留用于未来扩展
    private void loadPendingTasksToMemoryQueue() {
        try {
            log.debug("加载待处理任务到内存队列");
            // 这里可以调用相关服务加载任务
            // 暂时只记录日志
        } catch (Exception e) {
            log.error("加载待处理任务失败", e);
        }
    }

    /**
     * 清理临时目录
     *
     * @param dirPath 目录路径
     * @param prefix  文件前缀过滤
     */
    @SuppressWarnings("unused") // 保留用于未来扩展
    private void cleanupTempDirectory(String dirPath, String prefix) {
        try {
            Path directory = Paths.get(dirPath);
            if (!Files.exists(directory)) {
                return;
            }

            try (Stream<Path> files = Files.list(directory)) {
                files.filter(Files::isRegularFile)
                        .filter(file -> prefix == null || file.getFileName().toString().startsWith(prefix))
                        .filter(file -> {
                            try {
                                // 删除超过1小时的文件
                                return Files.getLastModifiedTime(file).toInstant()
                                        .isBefore(Instant.now().minus(1, ChronoUnit.HOURS));
                            } catch (IOException e) {
                                log.warn("检查文件修改时间失败: {}", file, e);
                                return false;
                            }
                        }).forEach(file -> {
                            try {
                                Files.delete(file);
                                log.debug("删除临时文件: {}", file);
                            } catch (IOException e) {
                                log.warn("删除临时文件失败: {}", file, e);
                            }
                        });
            }
        } catch (IOException e) {
            log.error("清理临时目录失败: {}", dirPath, e);
        }
    }

    /**
     * 清理临时文件和缓存
     */
    private void cleanupTempFilesAndCache() {
        try {
            log.debug("清理临时文件和缓存");
            // 这里可以调用相关服务清理临时文件和缓存
            // 暂时只记录日志
        } catch (Exception e) {
            log.error("清理临时文件和缓存失败", e);
        }
    }

    /**
     * 执行备份任务
     */
    private void executeBackupTasks() {
        try {
            log.debug("执行备份任务");
            // 这里可以调用相关服务执行备份
            // 暂时只记录日志
        } catch (Exception e) {
            log.error("执行备份任务失败", e);
        }
    }

    /**
     * 手动刷新指定服务器的配置
     */
    public void refreshServerConfigManually(Integer serverId) {
        refreshServerConfig(serverId);
        log.info("手动刷新服务器 {} 的配置完成", serverId);
    }

    /**
     * 获取配置缓存状态
     */
    public Map<String, Object> getConfigCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalCachedConfigs", configCache.size());
        status.put("refreshInterval", CONFIG_REFRESH_INTERVAL);
        status.put("lastRefreshTime", System.currentTimeMillis());

        Map<Integer, Long> configAges = new HashMap<>();
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<Integer, ServerTaskInfo> entry : serverTasks.entrySet()) {
            ServerTaskInfo taskInfo = entry.getValue();
            long age = currentTime - taskInfo.getLastConfigCheckTime();
            configAges.put(entry.getKey(), age / 1000); // 转换为秒
        }

        status.put("configAges", configAges);
        return status;
    }

    /**
     * 强制重新加载所有服务器配置
     */
    public void forceReloadAllConfigs() {
        try {
            configCache.clear();
            refreshAllConfigs();
            log.info("强制重新加载所有服务器配置完成");
        } catch (Exception e) {
            log.error("强制重新加载配置失败", e);
        }
    }

    /**
     * 获取任务管理器运行状态
     */
    public Map<String, Object> getManagerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("running", running);
        status.put("totalServerTasks", serverTasks.size());
        status.put("totalGlobalTasks", globalTasks.size());
        status.put("configCacheSize", configCache.size());
        status.put("configRefreshInterval", CONFIG_REFRESH_INTERVAL);

        // 统计活跃任务数
        int activeServerTasks = 0;
        for (ServerTaskInfo taskInfo : serverTasks.values()) {
            if (taskInfo.isTaskRunning()) {
                activeServerTasks++;
            }
        }
        status.put("activeServerTasks", activeServerTasks);

        int activeGlobalTasks = 0;
        for (Future<?> task : globalTasks.values()) {
            if (!task.isCancelled() && !task.isDone()) {
                activeGlobalTasks++;
            }
        }
        status.put("activeGlobalTasks", activeGlobalTasks);

        return status;
    }

    /**
     * 停止指定服务器的所有任务（但不删除任务信息）
     */
    public void pauseServerTasks(Integer serverId) {
        ServerTaskInfo taskInfo = serverTasks.get(serverId);
        if (taskInfo != null) {
            taskInfo.setTaskRunning(false);
            log.info("暂停服务器 {} 的所有任务", serverId);
        }
    }

    /**
     * 恢复指定服务器的所有任务
     */
    public void resumeServerTasks(Integer serverId) {
        ServerTaskInfo taskInfo = serverTasks.get(serverId);
        if (taskInfo != null) {
            taskInfo.setTaskRunning(true);
            log.info("恢复服务器 {} 的所有任务", serverId);
        }
    }

    /**
     * 服务器任务信息
     */
    @Data
    @AllArgsConstructor
    public static class ServerTaskInfo {
        private final Integer serverId;
        // 单线程 worker：合并该服务器所有任务到一个线程
        private Future<?> serverWorkerTask;

        private MonitorSysGenServerSetting currentSetting;
        private volatile boolean taskRunning = true;
        private long lastConfigCheckTime = 0;

        public ServerTaskInfo(Integer serverId) {
            this.serverId = serverId;
        }

        public void cancelAllTasks() {
            taskRunning = false;
            cancelTask(serverWorkerTask);
        }

        private void cancelTask(Future<?> task) {
            if (task != null && !task.isCancelled()) {
                task.cancel(true);
            }
        }

        public boolean isConfigExpired(long currentTime) {
            return currentTime - lastConfigCheckTime > CONFIG_REFRESH_INTERVAL * 1000;
        }

        public void updateConfigCheckTime() {
            this.lastConfigCheckTime = System.currentTimeMillis();
        }
    }
}
