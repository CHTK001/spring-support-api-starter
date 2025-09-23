package com.chua.starter.monitor.task.manager;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.properties.MonitorProperties;
import com.chua.starter.monitor.service.*;
import com.chua.starter.monitor.task.manager.step.*;
import com.chua.starter.monitor.task.manager.step.DockerStatsPushStep;
import com.chua.starter.monitor.utils.LocalSystemDataCollector;
import com.chua.starter.monitor.utils.ServerLogUtils;
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
@SuppressWarnings("ALL")
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
    private final MonitorProperties monitorProperties;
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
    private final DockerStatsPushStep dockerStatsPushStep;

    // 虚拟线程执行器
    private ExecutorService executorService;

    // 存储每个服务器的任务信息
    private static final Map<Integer, ServerTaskInfo> serverTasks = new ConcurrentHashMap<>();

    // 全局任务（不依赖特定服务器）
    private static final Map<String, Future<?>> globalTasks = new ConcurrentHashMap<>();

    // 内存中的配置缓存
    private static final Map<Integer, MonitorSysGenServerSetting> configCache = new ConcurrentHashMap<>();

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
        // 检查是否启用后端定时检测推送任务
        if (!monitorProperties.isEnableScheduledTasks()) {
            log.info("后端定时检测推送任务已禁用");
            return;
        }
        
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
                    if (now - lastStatusSummary >= 60_000) {
                        pushServerStatusSummary();
                        lastStatusSummary = now;
                    }
                    if (now - lastPerformanceTrends >= 300_000) {
                        pushPerformanceTrends();
                        lastPerformanceTrends = now;
                    }
                    if (now - lastConnectionStats >= 30_000) {
                        pushConnectionStatistics();
                        lastConnectionStats = now;
                    }
                    if (now - lastHealthStatus >= 300_000) {
                        pushHealthStatus();
                        lastHealthStatus = now;
                    }
                    if (now - lastGlobalAlert >= 120_000) {
                        processGlobalAlerts();
                        lastGlobalAlert = now;
                    }
                    if (now - lastCleanup >= 3_600_000) {
                        cleanupInvalidTasks();
                        lastCleanup = now;
                    }
                    if (now - lastConfigRefresh >= CONFIG_REFRESH_INTERVAL * 1000L) {
                        refreshAllConfigs();
                        lastConfigRefresh = now;
                    }
                    if (now - lastFileUploadCheck >= 30_000) {
                        checkUploadTaskStatus();
                        lastFileUploadCheck = now;
                    }
                    if (now - lastDataCleanup >= 24L * 3_600_000) {
                        cleanupTempFilesAndCache();
                        lastDataCleanup = now;
                    }
                    if (now - lastBackup >= 1_800_000) {
                        executeBackupTasks();
                        lastBackup = now;
                    }
                    Thread.sleep(1_000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("全局调度器异常", e);
                    try {
                        Thread.sleep(5_000L);
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        globalTasks.clear();
        globalTasks.put("globalDispatcher", dispatcher);
        log.info("全局调度器启动完成");
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


            // 先移除旧任务并创建任务信息与缓存
            removeServerTasks(serverId);
            MonitorSysGenServerSetting setting = settingResult.getData();
            configCache.put(serverId, setting);
            ServerTaskInfo taskInfo = new ServerTaskInfo(serverId);
            taskInfo.setCurrentSetting(setting);
            taskInfo.updateConfigCheckTime();


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
                long lastDockerStats = 0L; // 添加Docker状态推送时间戳
                while (taskInfo.isTaskRunning() && running) {
                    try {
                        MonitorSysGenServerSetting s = getCachedConfig(server.getMonitorSysGenServerId());
                        if (s == null) {
                            Thread.sleep(5_000L);
                            continue;
                        }
                        long now = System.currentTimeMillis();
                        // 指标采集（非API上报）
                        String rm = s.getMonitorSysGenServerSettingDataReportMethod();
                        if (!"API".equalsIgnoreCase(rm)) {
                            int freq = getEffectiveCollectionFrequency(s);
                            if (now - lastMetrics >= freq * 1000L) {
                                metricsCollectionStep.run(server, s);
                                lastMetrics = now;
                            }
                        }
                        // 告警检测
                        if (s.getMonitorSysGenServerSettingAlertEnabled() != null && s.getMonitorSysGenServerSettingAlertEnabled() == 1) {
                            int ai = getEffectiveAlertInterval(s);
                            if (now - lastAlerts >= ai * 1000L) {
                                alertDetectionStep.run(server, s);
                                lastAlerts = now;
                            }
                        }
                        // 健康检查
                        int hi = getEffectiveHealthCheckInterval(s);
                        if (now - lastHealth >= hi * 1000L) {
                            healthCheckStep.run(server, s);
                            lastHealth = now;
                        }
                        // 数据推送（非API上报）
                        if (!"API".equalsIgnoreCase(rm)) {
                            int freq = getEffectiveCollectionFrequency(s);
                            if (now - lastDataPush >= freq * 1000L) {
                                dataPushStep.run(server, s);
                                lastDataPush = now;
                            }
                        }
                        // 端口监控
                        if (s.getMonitorSysGenServerSettingPortMonitorEnabled() != null && s.getMonitorSysGenServerSettingPortMonitorEnabled() == 1) {
                            Integer itv = s.getMonitorSysGenServerSettingPortCheckInterval();
                            if (itv == null || itv <= 0) {
                                itv = 300;
                            }
                            if (now - lastPort >= itv * 1000L) {
                                portMonitorStep.run(server, s);
                                lastPort = now;
                            }
                        }
                        // 在线检测
                        if (s.getMonitorSysGenServerSettingOnlineCheckEnabled() != null && s.getMonitorSysGenServerSettingOnlineCheckEnabled() == 1) {
                            Integer itv = s.getMonitorSysGenServerSettingOnlineCheckInterval();
                            if (itv == null || itv <= 0) {
                                itv = 30;
                            }
                            if (now - lastOnline >= itv * 1000L) {
                                onlineCheckStep.run(server, s);
                                lastOnline = now;
                            }
                        }
                        // 延迟检测
                        if (s.getMonitorSysGenServerSettingLatencyCheckEnabled() != null && s.getMonitorSysGenServerSettingLatencyCheckEnabled() == 1) {
                            Integer itv = s.getMonitorSysGenServerSettingLatencyCheckInterval();
                            if (itv == null || itv <= 0) {
                                itv = 60;
                            }
                            if (now - lastLatency >= itv * 1000L) {
                                latencyCheckStep.run(server, s);
                                lastLatency = now;
                            }
                        }
                        // 日志清理（每日）
                        if (s.getMonitorSysGenServerSettingLogCleanupEnabled() != null && s.getMonitorSysGenServerSettingLogCleanupEnabled() == 1) {
                            if (now - lastLogCleanup >= 24L * 3_600_000) {
                                logCleanupStep.run(server, s);
                                lastLogCleanup = now;
                            }
                        }
                        // 缓存清理（每小时）
                        if (s.getMonitorSysGenServerSettingTempFileCleanupEnabled() != null && s.getMonitorSysGenServerSettingTempFileCleanupEnabled() == 1) {
                            if (now - lastCacheCleanup >= 3_600_000) {
                                cacheCleanupStep.run(server, s);
                                lastCacheCleanup = now;
                            }
                        }
                        // WebSocket 清理（默认每10分钟）
                        if (s.getMonitorSysGenServerSettingWebSocketCleanupEnabled() != null && s.getMonitorSysGenServerSettingWebSocketCleanupEnabled() == 1) {
                            Integer itv = s.getMonitorSysGenServerSettingWebSocketCleanupInterval();
                            if (itv == null || itv <= 0) itv = 10;
                            if (now - lastWsCleanup >= itv * 60_000L) {
                                webSocketCleanupStep.run(server, s);
                                lastWsCleanup = now;
                            }
                        }

                        // Docker状态推送
                        if (s.getMonitorSysGenServerSettingDockerEnabled() != null &&
                                s.getMonitorSysGenServerSettingDockerEnabled() == 1) {
                            Integer itv = s.getMonitorSysGenServerSettingDockerStatsInterval();
                            // 如果未设置间隔，则使用默认值10秒
                            if (itv == null || itv <= 0) {
                                itv = 10;
                            }
                            if (now - lastDockerStats >= itv * 1000L) {
                                dockerStatsPushStep.run(server, s);
                                lastDockerStats = now;
                            }
                        }

                        Thread.sleep(5_000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("服务器worker异常: serverId={}", server.getMonitorSysGenServerId(), e);
                        try {
                            Thread.sleep(20_000L);
                        } catch (InterruptedException ignore) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            });
            taskInfo.setServerWorkerTask(serverWorker);

            serverTasks.put(serverId, taskInfo);
            log.info("为服务器 {} 创建任务完成", server.getMonitorSysGenServerName());

        } catch (Exception e) {
            log.error("为服务器创建任务失败: serverId={}", serverId, e);
        }
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
            // 获取默认配置的临时文件保留时间
            MonitorSysGenServerSetting defaultSetting = MonitorSysGenServerSetting.getDefaultSetting(null);
            Integer tempFileRetentionHours = defaultSetting.getMonitorSysGenServerSettingTempFileRetentionHours();

            if (tempFileRetentionHours != null && tempFileRetentionHours > 0) {
                serverService.cleanupTempFiles(tempFileRetentionHours);
            } else {
                serverService.cleanupTempFiles();
            }

            serverService.cleanupCache();
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
        // 添加Docker状态推送时间戳
        private long lastDockerStatsTime = 0;

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

        public long getLastDockerStatsTime() {
            return lastDockerStatsTime;
        }

        public void updateLastDockerStatsTime() {
            this.lastDockerStatsTime = System.currentTimeMillis();
        }
    }
}
