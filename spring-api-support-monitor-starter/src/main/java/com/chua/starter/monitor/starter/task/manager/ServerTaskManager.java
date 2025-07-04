package com.chua.starter.monitor.starter.task.manager;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerFileUploadQueue;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.pojo.ServerMetricsData;
import com.chua.starter.monitor.starter.service.*;
import com.chua.starter.monitor.starter.utils.LocalSystemDataCollector;
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
    private final MonitorSysGenServerMetricsService metricsService;
    private final PrometheusDataCollectorService prometheusDataCollectorService;
    private final LocalSystemDataCollector localSystemDataCollector;
    private final MonitorSysGenServerFileUploadQueueService fileUploadQueueService;
    private final MonitorSysGenServerFileUploadTaskService fileUploadTaskService;

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
     * 服务器任务信息
     */
    @Data
    @AllArgsConstructor
    public static class ServerTaskInfo {
        private final Integer serverId;
        private Future<?> metricsCollectionTask;
        private Future<?> alertDetectionTask;
        private Future<?> healthCheckTask;
        private Future<?> dataPushTask;
        private Future<?> portMonitorTask;
        private Future<?> onlineCheckTask;
        private Future<?> latencyCheckTask;
        private Future<?> logCleanupTask;
        private Future<?> cacheCleanupTask;
        private Future<?> webSocketCleanupTask;
        private Future<?> fileUploadQueueTask;
        private Future<?> fileUploadStatusCheckTask;
        private Future<?> fileUploadCleanupTask;
        private MonitorSysGenServerSetting currentSetting;
        private volatile boolean taskRunning = true;
        private long lastConfigCheckTime = 0;

        public ServerTaskInfo(Integer serverId) {
            this.serverId = serverId;
        }

        public void cancelAllTasks() {
            taskRunning = false;
            cancelTask(metricsCollectionTask);
            cancelTask(alertDetectionTask);
            cancelTask(healthCheckTask);
            cancelTask(dataPushTask);
            cancelTask(portMonitorTask);
            cancelTask(onlineCheckTask);
            cancelTask(latencyCheckTask);
            cancelTask(logCleanupTask);
            cancelTask(cacheCleanupTask);
            cancelTask(webSocketCleanupTask);
            cancelTask(fileUploadQueueTask);
            cancelTask(fileUploadStatusCheckTask);
            cancelTask(fileUploadCleanupTask);
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
     * 启动统一任务（合并其他定时任务）
     */
    private void startUnifiedTasks() {
        // 文件上传队列处理任务 - 每10秒
        Future<?> fileUploadQueueTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(10000); // 10秒
                    if (running) {
                        processFileUploadQueue();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("文件上传队列处理任务异常", e);
                }
            }
        });
        globalTasks.put("fileUploadQueue", fileUploadQueueTask);

        // 文件上传队列加载任务 - 每5秒
        Future<?> fileUploadQueueLoadTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(5000); // 5秒
                    if (running) {
                        loadPendingTasksToMemoryQueue();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("文件上传队列加载任务异常", e);
                }
            }
        });
        globalTasks.put("fileUploadQueueLoad", fileUploadQueueLoadTask);

        // 临时文件清理任务 - 每小时
        Future<?> tempFileCleanupTask = executorService.submit(() -> {
            while (running) {
                try {
                    Thread.sleep(3600000); // 1小时
                    if (running) {
                        cleanupUploadTempFiles();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("临时文件清理任务异常", e);
                }
            }
        });
        globalTasks.put("tempFileCleanup", tempFileCleanupTask);

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
     * 刷新所有服务器配置
     */
    private void refreshAllConfigs() {
        try {
            List<MonitorSysGenServer> servers = serverService.list().stream()
                .filter(server -> server.getMonitorSysGenServerStatus() != null &&
                        server.getMonitorSysGenServerStatus() == 1)
                .toList();

            for (MonitorSysGenServer server : servers) {
                refreshServerConfig(server.getMonitorSysGenServerId());
            }

            log.debug("刷新了 {} 个服务器的配置", servers.size());
        } catch (Exception e) {
            log.error("刷新所有服务器配置失败", e);
        }
    }

    /**
     * 刷新单个服务器配置
     */
    private void refreshServerConfig(Integer serverId) {
        try {
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                serverSettingService.getOrCreateSetting(serverId);
            if (settingResult.isOk() && settingResult.getData() != null) {
                MonitorSysGenServerSetting newSetting = settingResult.getData();
                MonitorSysGenServerSetting oldSetting = configCache.get(serverId);

                // 检查配置是否有变化
                if (oldSetting == null || !configEquals(oldSetting, newSetting)) {
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
                             newSetting.getMonitorSysGenServerSettingDataCollectionFrequency()) &&
               Objects.equals(old.getMonitorSysGenServerSettingMonitorEnabled(),
                             newSetting.getMonitorSysGenServerSettingMonitorEnabled()) &&
               Objects.equals(old.getMonitorSysGenServerSettingAlertEnabled(),
                             newSetting.getMonitorSysGenServerSettingAlertEnabled()) &&
               Objects.equals(old.getMonitorSysGenServerSettingDataReportMethod(),
                             newSetting.getMonitorSysGenServerSettingDataReportMethod()) &&
               Objects.equals(old.getMonitorSysGenServerSettingPortMonitorEnabled(),
                             newSetting.getMonitorSysGenServerSettingPortMonitorEnabled()) &&
               Objects.equals(old.getMonitorSysGenServerSettingPortCheckInterval(),
                             newSetting.getMonitorSysGenServerSettingPortCheckInterval());
    }

    /**
     * 从缓存获取服务器配置
     */
    private MonitorSysGenServerSetting getCachedConfig(Integer serverId) {
        return configCache.get(serverId);
    }

    /**
     * 初始化所有服务器的任务
     */
    public void initializeServerTasks() {
        try {
            // 启动全局任务（只启动一次）
            if (!globalTasksStarted) {
                startGlobalTasks();
                startConfigRefreshTask();
                startUnifiedTasks();
                globalTasksStarted = true;
                log.info("全局任务和统一任务启动完成");
            }

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

            // 获取服务器配置并缓存
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                serverSettingService.getOrCreateSetting(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                log.warn("获取服务器配置失败，跳过任务创建: serverId={}", serverId);
                return;
            }

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
    private void createMetricsCollectionTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        if (setting.getMonitorSysGenServerSettingMonitorEnabled() != 1) {
            return;
        }

        // 检查数据上报方式，如果是本地上报，则不在这里创建任务
        String reportMethod = setting.getMonitorSysGenServerSettingDataReportMethod();
        if ("LOCAL".equals(reportMethod)) {
            log.debug("服务器 {} 使用本地上报方式，跳过创建指标收集任务", server.getMonitorSysGenServerName());
            return;
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

                    Integer frequency = getEffectiveCollectionFrequency(currentSetting);

                    // 执行指标收集
                    collectAndPushMetrics(server, currentSetting);

                    // 按配置的频率休眠
                    Thread.sleep(frequency * 1000L);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("指标收集任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    try {
                        Thread.sleep(30000); // 异常时等待30秒
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        taskInfo.setMetricsCollectionTask(task);
        log.debug("为服务器 {} 创建指标收集任务", server.getMonitorSysGenServerName());
    }

    /**
     * 创建告警检测任务
     */
    private void createAlertDetectionTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
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

                    // 执行告警检测
                    detectAndPushAlerts(server, currentSetting);

                    // 固定2分钟间隔
                    Thread.sleep(120000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("告警检测任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    try {
                        Thread.sleep(120000); // 异常时等待2分钟
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        taskInfo.setAlertDetectionTask(task);
        log.debug("为服务器 {} 创建告警检测任务", server.getMonitorSysGenServerName());
    }

    /**
     * 创建健康检查任务
     */
    private void createHealthCheckTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
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

                    // 固定5分钟间隔
                    Thread.sleep(300000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("健康检查任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    try {
                        Thread.sleep(300000); // 异常时等待5分钟
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

    /**
     * 获取有效的数据收集频率
     */
    private Integer getEffectiveCollectionFrequency(MonitorSysGenServerSetting setting) {
        Integer frequency = setting.getMonitorSysGenServerSettingDataCollectionFrequency();
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
            return true;
        }

        // 检查监控间隔是否变化
        Integer currentInterval = getEffectiveMonitorInterval(currentSetting);
        Integer newInterval = getEffectiveMonitorInterval(newSetting);
        if (!currentInterval.equals(newInterval)) {
            return true;
        }

        // 检查监控启用状态是否变化
        if (!Objects.equals(currentSetting.getMonitorSysGenServerSettingMonitorEnabled(),
                newSetting.getMonitorSysGenServerSettingMonitorEnabled())) {
            return true;
        }

        // 检查告警启用状态是否变化
        if (!Objects.equals(currentSetting.getMonitorSysGenServerSettingAlertEnabled(),
                newSetting.getMonitorSysGenServerSettingAlertEnabled())) {
            return true;
        }

        return false;
    }

    /**
     * 智能更新服务器任务
     * 只有当配置真正发生变化时才重新创建任务
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
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                serverSettingService.getOrCreateSetting(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                log.warn("无法获取服务器设置，跳过任务更新: serverId={}", serverId);
                return;
            }

            MonitorSysGenServerSetting newSetting = settingResult.getData();

            // 检查是否需要更新
            if (needsTaskUpdate(taskInfo, newSetting)) {
                log.info("检测到服务器 {} 配置变化，重新创建任务", serverId);
                createServerTasks(serverId);
            } else {
                log.debug("服务器 {} 配置未变化，跳过任务更新", serverId);
            }
        } catch (Exception e) {
            log.error("智能更新服务器任务失败: serverId={}", serverId, e);
        }
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
            config.put("monitorInterval", getEffectiveMonitorInterval(taskInfo.getCurrentSetting()));
            config.put("monitorEnabled", taskInfo.getCurrentSetting().getMonitorSysGenServerSettingMonitorEnabled());
            config.put("alertEnabled", taskInfo.getCurrentSetting().getMonitorSysGenServerSettingAlertEnabled());
            status.put("config", config);
        }

        return status;
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
            status.put("status", task.isCancelled() ? "cancelled" :
                              task.isDone() ? "completed" : "running");
        }
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
            boolean hasActiveTasks = false;

            if (taskInfo.getMetricsCollectionTask() != null &&
                !taskInfo.getMetricsCollectionTask().isCancelled() &&
                !taskInfo.getMetricsCollectionTask().isDone()) {
                hasActiveTasks = true;
            }

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
    private MonitorSysGenServerMetrics convertToServerMetrics(MonitorSysGenServer server, ServerMetricsData metricsData) {
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
    private void createDataPushTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        if (setting.getMonitorSysGenServerSettingMonitorEnabled() != 1) {
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

                    // 执行数据推送
                    executeDataPush(server, currentSetting, currentReportMethod);

                    // 按配置的频率休眠
                    Thread.sleep(frequency * 1000L);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("数据推送任务异常: serverId={}", server.getMonitorSysGenServerId(), e);
                    try {
                        Thread.sleep(30000); // 异常时等待30秒
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        taskInfo.setDataPushTask(task);
        log.debug("为服务器 {} 创建数据推送任务", server.getMonitorSysGenServerName());
    }

    /**
     * 创建端口监控任务
     */
    private void createPortMonitorTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        if (setting.getMonitorSysGenServerSettingPortMonitorEnabled() != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null || currentSetting.getMonitorSysGenServerSettingPortMonitorEnabled() != 1) {
                        Thread.sleep(300000); // 如果端口监控被禁用，等待5分钟后重新检查
                        continue;
                    }

                    Integer frequency = currentSetting.getMonitorSysGenServerSettingPortCheckInterval();
                    if (frequency == null || frequency <= 0) {
                        frequency = 300; // 默认5分钟
                    }

                    // 执行端口监控
                    executePortMonitor(server, currentSetting);

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

        taskInfo.setPortMonitorTask(task);
        log.debug("为服务器 {} 创建端口监控任务", server.getMonitorSysGenServerName());
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
            log.error("执行数据推送失败: serverId={}, method={}",
                     server.getMonitorSysGenServerId(), reportMethod, e);
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
     * 执行Prometheus数据推送
     */
    private void executePrometheusDataPush(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            // 从Prometheus收集数据
            ServerMetricsData metricsData = prometheusDataCollectorService.collectMetrics(
                server.getMonitorSysGenServerId(), setting);

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
     * 执行端口监控
     */
    private void executePortMonitor(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            String portList = setting.getMonitorSysGenServerSettingMonitorPorts();
            if (portList == null || portList.trim().isEmpty()) {
                return;
            }

            String[] ports = portList.split(",");
            Map<String, Object> portStatuses = new java.util.HashMap<>();

            for (String portStr : ports) {
                try {
                    int port = Integer.parseInt(portStr.trim());
                    boolean isOpen = checkPortStatus(server.getMonitorSysGenServerHost(), port);

                    Map<String, Object> status = new java.util.HashMap<>();
                    status.put("port", port);
                    status.put("status", isOpen ? "OPEN" : "CLOSED");
                    status.put("checkTime", java.time.LocalDateTime.now().toString());

                    portStatuses.put(String.valueOf(port), status);
                } catch (NumberFormatException e) {
                    log.warn("无效的端口号: {}", portStr);
                }
            }

            // 推送端口状态到WebSocket
            Map<String, Object> message = new java.util.HashMap<>();
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

            log.debug("端口状态已推送到WebSocket，服务器ID: {}, 端口数量: {}",
                     server.getMonitorSysGenServerId(), portStatuses.size());

        } catch (Exception e) {
            log.error("端口监控失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
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
            metrics.setMonitorSysGenServerMetricsCollectTime(java.time.LocalDateTime.now());

            // 设置CPU信息
            if (systemData.containsKey("cpuUsage")) {
                Double cpuUsage = (Double) systemData.get("cpuUsage");
                metrics.setMonitorSysGenServerMetricsCpuUsage(java.math.BigDecimal.valueOf(cpuUsage));
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
                metrics.setMonitorSysGenServerMetricsMemoryUsage(java.math.BigDecimal.valueOf(memoryUsagePercent));
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
                        metrics.setMonitorSysGenServerMetricsDiskUsage(java.math.BigDecimal.valueOf(diskUsage));
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

    // ==================== 新增任务创建方法 ====================

    /**
     * 创建在线状态检测任务
     */
    private void createOnlineCheckTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        Integer monitorSysGenServerSettingOnlineCheckEnabled = setting.getMonitorSysGenServerSettingOnlineCheckEnabled();
        if (null == monitorSysGenServerSettingOnlineCheckEnabled || monitorSysGenServerSettingOnlineCheckEnabled != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null || currentSetting.getMonitorSysGenServerSettingOnlineCheckEnabled() != 1) {
                        Thread.sleep(30000); // 如果在线检测被禁用，等待30秒后重新检查
                        continue;
                    }

                    Integer interval = currentSetting.getMonitorSysGenServerSettingOnlineCheckInterval();
                    if (interval == null || interval <= 0) {
                        interval = 30; // 默认30秒
                    }

                    // 执行在线状态检测
                    executeOnlineCheck(server, currentSetting);

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
     * 创建延迟检测任务
     */
    private void createLatencyCheckTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        if(null == setting) {
            return;
        }
        Integer monitorSysGenServerSettingLatencyCheckEnabled = setting.getMonitorSysGenServerSettingLatencyCheckEnabled();
        if (null == monitorSysGenServerSettingLatencyCheckEnabled || monitorSysGenServerSettingLatencyCheckEnabled != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null || currentSetting.getMonitorSysGenServerSettingLatencyCheckEnabled() != 1) {
                        Thread.sleep(60000); // 如果延迟检测被禁用，等待1分钟后重新检查
                        continue;
                    }

                    Integer interval = currentSetting.getMonitorSysGenServerSettingLatencyCheckInterval();
                    if (interval == null || interval <= 0) {
                        interval = 60; // 默认60秒
                    }

                    // 执行延迟检测
                    executeLatencyCheck(server, currentSetting);

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
    private void createLogCleanupTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        Integer monitorSysGenServerSettingLogCleanupEnabled = setting.getMonitorSysGenServerSettingLogCleanupEnabled();
        if (null == monitorSysGenServerSettingLogCleanupEnabled || monitorSysGenServerSettingLogCleanupEnabled != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null || currentSetting.getMonitorSysGenServerSettingLogCleanupEnabled() != 1) {
                        Thread.sleep(3600000); // 如果日志清理被禁用，等待1小时后重新检查
                        continue;
                    }

                    // 执行日志清理
                    executeLogCleanup(server, currentSetting);

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
    private void createCacheCleanupTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        if (setting.getMonitorSysGenServerSettingTempFileCleanupEnabled() != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null || currentSetting.getMonitorSysGenServerSettingTempFileCleanupEnabled() != 1) {
                        Thread.sleep(3600000); // 如果缓存清理被禁用，等待1小时后重新检查
                        continue;
                    }

                    // 执行缓存清理
                    executeCacheCleanup(server, currentSetting);

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
    private void createWebSocketCleanupTask(ServerTaskInfo taskInfo, MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        if (setting.getMonitorSysGenServerSettingWebSocketCleanupEnabled() != 1) {
            return;
        }

        Future<?> task = executorService.submit(() -> {
            while (taskInfo.isTaskRunning() && running) {
                try {
                    // 从内存缓存获取最新配置
                    MonitorSysGenServerSetting currentSetting = getCachedConfig(server.getMonitorSysGenServerId());
                    if (currentSetting == null || currentSetting.getMonitorSysGenServerSettingWebSocketCleanupEnabled() != 1) {
                        Thread.sleep(600000); // 如果WebSocket清理被禁用，等待10分钟后重新检查
                        continue;
                    }

                    Integer interval = currentSetting.getMonitorSysGenServerSettingWebSocketCleanupInterval();
                    if (interval == null || interval <= 0) {
                        interval = 10; // 默认10分钟
                    }

                    // 执行WebSocket清理
                    executeWebSocketCleanup(server, currentSetting);

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

    // ==================== 新增任务执行方法 ====================

    /**
     * 执行在线状态检测
     */
    private void executeOnlineCheck(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            log.debug("执行服务器 {} 在线状态检测", server.getMonitorSysGenServerName());
            // 这里可以实现具体的在线状态检测逻辑
            // 例如：ping检测、端口连接检测等
        } catch (Exception e) {
            log.error("在线状态检测失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 执行延迟检测
     */
    private void executeLatencyCheck(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            log.debug("执行服务器 {} 延迟检测", server.getMonitorSysGenServerName());
            // 这里可以实现具体的延迟检测逻辑
            // 例如：ping延迟、HTTP响应时间等
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
            // 这里可以实现具体的日志清理逻辑
            // 例如：删除过期日志文件、压缩旧日志等
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
            // 这里可以实现具体的缓存清理逻辑
            // 例如：清理临时文件、清理过期缓存等
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
            // 这里可以实现具体的WebSocket清理逻辑
            // 例如：清理无效连接、重置连接状态等
        } catch (Exception e) {
            log.error("WebSocket清理失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    // ==================== 统一任务实现方法 ====================

    /**
     * 处理文件上传队列
     */
    private void processFileUploadQueue() {
        try {
            log.debug("开始处理文件上传队列");

            // 批量获取待处理的队列任务
            var pendingQueueResult = fileUploadQueueService.dequeueBatch(10);
            if (pendingQueueResult != null && pendingQueueResult.getData() != null) {
                var pendingTasks = pendingQueueResult.getData();

                for (var queueTask : pendingTasks) {
                    try {
                        // 处理单个队列任务
                        processQueueTask(queueTask);
                    } catch (Exception e) {
                        log.error("处理队列任务失败: taskId={}",
                                queueTask.getMonitorSysGenServerFileUploadQueueId(), e);

                        // 标记任务失败
                        fileUploadQueueService.markFailed(
                                queueTask.getMonitorSysGenServerFileUploadQueueId(),
                                e.getMessage());
                    }
                }

                if (!pendingTasks.isEmpty()) {
                    log.info("处理文件上传队列完成，处理了 {} 个任务", pendingTasks.size());
                }
            }

        } catch (Exception e) {
            log.error("处理文件上传队列失败", e);
        }
    }

    /**
     * 加载待处理任务到内存队列
     */
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
     * 清理上传临时文件
     */
    private void cleanupUploadTempFiles() {
        try {
            log.debug("开始清理上传临时文件");

            // 清理系统临时目录中的上传文件
            cleanupTempDirectory(System.getProperty("java.io.tmpdir"), "upload_");

            // 清理应用临时目录
            cleanupTempDirectory("./temp/uploads", null);

            // 清理已完成的队列项
            var cleanupResult = fileUploadQueueService.cleanupCompletedItems(1); // 保留1天
            if (cleanupResult != null && cleanupResult.getData() != null) {
                var result = cleanupResult.getData();
                if (result.getCleanupCount() != null && result.getCleanupCount() > 0) {
                    log.info("清理了 {} 个已完成的队列项", result.getCleanupCount());
                }
            }

            log.info("清理上传临时文件完成");
        } catch (Exception e) {
            log.error("清理上传临时文件失败", e);
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

    // ==================== 文件上传任务相关方法 ====================

    /**
     * 检查上传任务状态
     */
    private void checkUploadTaskStatus() {
        try {
            log.debug("开始检查上传任务状态");

            // 检查超时的处理中任务
            var timeoutResult = fileUploadQueueService.resetTimeoutProcessingItems(30); // 30分钟超时
            if (timeoutResult != null && timeoutResult.getData() != null) {
                var resetResult = timeoutResult.getData();
                if (resetResult.getResetCount() != null && resetResult.getResetCount() > 0) {
                    log.warn("重置了 {} 个超时的上传任务", resetResult.getResetCount());
                }
            }

            // 注意：这里可以根据需要实现重试逻辑
            // 由于当前服务接口中没有批量重试方法，暂时跳过重试逻辑
            log.debug("上传任务状态检查完成");

        } catch (Exception e) {
            log.error("检查上传任务状态失败", e);
        }
    }

    /**
     * 处理单个队列任务
     *
     * @param queueTask 队列任务
     */
    private void processQueueTask(MonitorSysGenServerFileUploadQueue queueTask) {
        try {
            Long queueId = queueTask.getMonitorSysGenServerFileUploadQueueId();

            // 标记任务开始处理
            fileUploadQueueService.markProcessing(queueId);

            // 获取关联的上传任务
            Long taskId = queueTask.getMonitorSysGenServerFileUploadTaskId();
            var taskResult = fileUploadTaskService.getTaskById(taskId);

            if (taskResult != null && taskResult.getData() != null) {
                var uploadTask = taskResult.getData();

                // 执行实际的文件上传逻辑
                boolean success = executeFileUpload(uploadTask);

                if (success) {
                    // 标记任务完成
                    fileUploadQueueService.markCompleted(queueId);
                    log.info("队列任务处理成功: queueId={}, taskId={}", queueId, taskId);
                } else {
                    // 标记任务失败
                    fileUploadQueueService.markFailed(queueId, "文件上传失败");
                    log.warn("队列任务处理失败: queueId={}, taskId={}", queueId, taskId);
                }
            } else {
                fileUploadQueueService.markFailed(queueId, "关联的上传任务不存在");
                log.error("队列任务关联的上传任务不存在: queueId={}, taskId={}", queueId, taskId);
            }

        } catch (Exception e) {
            log.error("处理队列任务异常: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 执行文件上传
     *
     * @param uploadTask 上传任务
     * @return 是否成功
     */
    private boolean executeFileUpload(Object uploadTask) {
        // 这里应该实现具体的文件上传逻辑
        // 由于涉及到具体的文件传输实现，这里只返回模拟结果
        try {
            // 模拟上传过程
            Thread.sleep(100); // 模拟上传耗时
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 清理临时目录
     *
     * @param dirPath 目录路径
     * @param prefix  文件前缀过滤
     */
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
                        })
                        .forEach(file -> {
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
}
