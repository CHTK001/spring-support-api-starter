package com.chua.starter.monitor.task.manager;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.properties.MonitorProperties;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.task.manager.step.*;
import com.chua.starter.monitor.utils.ServerLogUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 服务器任务管理器
 * <p>
 * 负责管理所有服务器的监控任务，使用 Java 21 虚拟线程实现高并发任务处理。
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/28
 * @see MonitorSysGenServerSetting 服务器配置实体（任务执行依赖的配置项）
 * @see MonitorSysGenServer 服务器实体
 * @see MonitorProperties 监控属性配置
 * @see PortMonitorStep 端口监控步骤
 * @see HealthCheckStep 健康检查步骤
 * @see MetricsCollectionStep 指标采集步骤
 * @see AlertDetectionStep 告警检测步骤
 * @see DataPushStep 数据推送步骤
 * @see OnlineCheckStep 在线检测步骤
 * @see LatencyCheckStep 延迟检测步骤
 * @see DockerStatsPushStep Docker状态推送步骤
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("ALL")
public class ServerTaskManager {

    // ==================== 依赖服务 ====================
    
    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final MonitorProperties monitorProperties;
    
    // ==================== 任务步骤组件 ====================
    
    private final PortMonitorStep portMonitorStep;
    private final HealthCheckStep healthCheckStep;
    private final MetricsCollectionStep metricsCollectionStep;
    private final AlertDetectionStep alertDetectionStep;
    private final DataPushStep dataPushStep;
    private final OnlineCheckStep onlineCheckStep;
    private final LatencyCheckStep latencyCheckStep;
    private final DockerStatsPushStep dockerStatsPushStep;

    // ==================== 执行器和任务存储 ====================
    
    private ExecutorService executorService;
    private final Map<Integer, ServerTaskInfo> serverTasks = new ConcurrentHashMap<>();
    private final Map<Integer, MonitorSysGenServerSetting> configCache = new ConcurrentHashMap<>();

    // ==================== 常量配置 ====================
    
    private static final int CONFIG_REFRESH_INTERVAL = 60;

    // ==================== 运行状态标志 ====================
    
    private volatile boolean running = true;

    /**
     * 刷新所有服务器配置
     * <p>
     * 遍历所有启用状态的服务器，逐个刷新其配置缓存。
     * 此方法由全局调度器定期调用，确保配置缓存与数据库保持同步。
     * </p>
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

    /**
     * 初始化任务管理器
     * <p>
     * 在Spring容器启动后自动调用，完成以下初始化工作：
     * <ul>
     *   <li>检查是否启用后端定时检测推送任务</li>
     *   <li>创建虚拟线程执行器</li>
     *   <li>异步启动所有服务器的监控任务</li>
     * </ul>
     * </p>
     */
    @PostConstruct
    public void init() {
        // 检查是否启用后端定时检测推送任务
        if (!monitorProperties.isEnableScheduledTasks()) {
            log.info("后端定时检测推送任务已禁用");
            return;
        }
        
        // 创建虚拟线程执行器
        executorService = Executors.newVirtualThreadPerTaskExecutor();

        Thread.ofVirtual().start(() -> {
            // 为所有现有服务器创建任务（包含全局任务启动）
            initializeServerTasks();
        });

        log.info("服务器任务管理器初始化完成");
    }

    /**
     * 销毁任务管理器
     * <p>
     * 在Spring容器关闭前自动调用，完成以下清理工作：
     * <ul>
     *   <li>停止所有运行中的任务</li>
     *   <li>取消所有服务器任务和全局任务</li>
     *   <li>清空配置缓存</li>
     *   <li>关闭线程池</li>
     * </ul>
     * </p>
     */
    @PreDestroy
    public void destroy() {
        log.info("正在关闭服务器任务管理器...");

        // 停止所有任务
        running = false;

        // 取消所有服务器任务
        serverTasks.values().forEach(ServerTaskInfo::cancelAllTasks);
        serverTasks.clear();

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
     * <p>
     * 遍历所有启用状态的服务器，为每个服务器创建监控任务。
     * </p>
     * 
     * @see #createServerTasks(Integer)
     */
    public void initializeServerTasks() {
        try {
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

            // 启动服务器 worker 线程
            Future<?> serverWorker = executorService.submit(() -> runServerWorker(server, taskInfo));
            taskInfo.setServerWorkerTask(serverWorker);

            serverTasks.put(serverId, taskInfo);
            log.info("为服务器 {} 创建任务完成", server.getMonitorSysGenServerName());

        } catch (Exception e) {
            log.error("为服务器创建任务失败: serverId={}", serverId, e);
        }
    }

    /**
     * 服务器 worker 主循环
     * <p>
     * 串行执行该服务器的所有监控步骤，使用动态 sleep 优化轮询效率
     * </p>
     */
    private void runServerWorker(MonitorSysGenServer server, ServerTaskInfo taskInfo) {
        // 上次执行时间戳
        long lastMetrics = 0L;
        long lastAlerts = 0L;
        long lastHealth = 0L;
        long lastDataPush = 0L;
        long lastPort = 0L;
        long lastOnline = 0L;
        long lastLatency = 0L;
        long lastDockerStats = 0L;
        long lastConfigRefresh = System.currentTimeMillis();

        while (taskInfo.isTaskRunning() && running) {
            try {
                long now = System.currentTimeMillis();

                // 定期刷新配置（每 60 秒）
                if (now - lastConfigRefresh >= CONFIG_REFRESH_INTERVAL * 1000L) {
                    refreshServerConfig(server.getMonitorSysGenServerId());
                    lastConfigRefresh = now;
                }

                MonitorSysGenServerSetting s = getCachedConfig(server.getMonitorSysGenServerId());
                if (s == null) {
                    Thread.sleep(5_000L);
                    continue;
                }
                long nextWakeup = Long.MAX_VALUE;

                // 指标采集
                String rm = s.getMonitorSysGenServerSettingDataReportMethod();
                boolean metricsEnabled = s.getMonitorSysGenServerSettingMetricsCollectionEnabled() != null
                        && s.getMonitorSysGenServerSettingMetricsCollectionEnabled() == 1;
                if (metricsEnabled && !"NODE".equalsIgnoreCase(rm)) {
                    int freq = getEffectiveCollectionFrequency(s);
                    long interval = freq * 1000L;
                    if (now - lastMetrics >= interval) {
                        metricsCollectionStep.run(server, s);
                        lastMetrics = now;
                    }
                    nextWakeup = Math.min(nextWakeup, lastMetrics + interval - now);
                }

                // 告警检测
                if (s.getMonitorSysGenServerSettingAlertEnabled() != null && s.getMonitorSysGenServerSettingAlertEnabled() == 1) {
                    int ai = getEffectiveAlertInterval(s);
                    long interval = ai * 1000L;
                    if (now - lastAlerts >= interval) {
                        alertDetectionStep.run(server, s);
                        lastAlerts = now;
                    }
                    nextWakeup = Math.min(nextWakeup, lastAlerts + interval - now);
                }

                // 健康检查
                boolean healthCheckEnabled = s.getMonitorSysGenServerSettingHealthCheckEnabled() != null
                        && s.getMonitorSysGenServerSettingHealthCheckEnabled() == 1;
                if (healthCheckEnabled) {
                    int hi = getEffectiveHealthCheckInterval(s);
                    long interval = hi * 1000L;
                    if (now - lastHealth >= interval) {
                        healthCheckStep.run(server, s);
                        lastHealth = now;
                    }
                    nextWakeup = Math.min(nextWakeup, lastHealth + interval - now);
                }

                // 数据推送
                if (!"NODE".equalsIgnoreCase(rm) && !"API".equalsIgnoreCase(rm)) {
                    int freq = getEffectiveCollectionFrequency(s);
                    long interval = freq * 1000L;
                    if (now - lastDataPush >= interval) {
                        dataPushStep.run(server, s);
                        lastDataPush = now;
                    }
                    nextWakeup = Math.min(nextWakeup, lastDataPush + interval - now);
                }

                // 端口监控
                if (s.getMonitorSysGenServerSettingPortMonitorEnabled() != null && s.getMonitorSysGenServerSettingPortMonitorEnabled() == 1) {
                    Integer itv = s.getMonitorSysGenServerSettingPortCheckInterval();
                    long interval = (itv == null || itv <= 0 ? 300 : itv) * 1000L;
                    if (now - lastPort >= interval) {
                        portMonitorStep.run(server, s);
                        lastPort = now;
                    }
                    nextWakeup = Math.min(nextWakeup, lastPort + interval - now);
                }

                // 在线检测
                if (s.getMonitorSysGenServerSettingOnlineCheckEnabled() != null && s.getMonitorSysGenServerSettingOnlineCheckEnabled() == 1) {
                    Integer itv = s.getMonitorSysGenServerSettingOnlineCheckInterval();
                    long interval = (itv == null || itv <= 0 ? 30 : itv) * 1000L;
                    if (now - lastOnline >= interval) {
                        onlineCheckStep.run(server, s);
                        lastOnline = now;
                    }
                    nextWakeup = Math.min(nextWakeup, lastOnline + interval - now);
                }

                // 延迟检测
                if (s.getMonitorSysGenServerSettingLatencyCheckEnabled() != null && s.getMonitorSysGenServerSettingLatencyCheckEnabled() == 1) {
                    Integer itv = s.getMonitorSysGenServerSettingLatencyCheckInterval();
                    long interval = (itv == null || itv <= 0 ? 60 : itv) * 1000L;
                    if (now - lastLatency >= interval) {
                        latencyCheckStep.run(server, s);
                        lastLatency = now;
                    }
                    nextWakeup = Math.min(nextWakeup, lastLatency + interval - now);
                }

                // Docker 状态推送
                if (s.getMonitorSysGenServerSettingDockerEnabled() != null && s.getMonitorSysGenServerSettingDockerEnabled() == 1) {
                    Integer itv = s.getMonitorSysGenServerSettingDockerStatsInterval();
                    long interval = (itv == null || itv <= 0 ? 10 : itv) * 1000L;
                    if (now - lastDockerStats >= interval) {
                        dockerStatsPushStep.run(server, s);
                        lastDockerStats = now;
                    }
                    nextWakeup = Math.min(nextWakeup, lastDockerStats + interval - now);
                }

                // 动态 sleep：睡眠到下一个任务执行时间，最少 1 秒，最多 30 秒
                long sleepTime = Math.max(1_000L, Math.min(nextWakeup, 30_000L));
                Thread.sleep(sleepTime);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("服务器worker异常: serverId={}", server.getMonitorSysGenServerId(), e);
                try {
                    Thread.sleep(10_000L);
                } catch (InterruptedException ignore) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
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
     * 清理临时文件和缓存（供外部调用）
     */
    public void cleanupTempFilesAndCache() {
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
     * <p>
     * 如果任务已停止，会重新创建任务
     * </p>
     */
    public void resumeServerTasks(Integer serverId) {
        ServerTaskInfo taskInfo = serverTasks.get(serverId);
        if (taskInfo == null) {
            // 任务不存在，创建新任务
            createServerTasks(serverId);
            log.info("为服务器 {} 创建新任务", serverId);
        } else if (!taskInfo.isTaskRunning()) {
            // 任务已暂停，需要重新创建
            createServerTasks(serverId);
            log.info("恢复服务器 {} 的所有任务", serverId);
        } else {
            log.debug("服务器 {} 任务已在运行中", serverId);
        }
    }

    /**
     * 刷新全局配置
     * <p>
     * 当全局配置更新后，调用此方法使配置立即生效
     * </p>
     */
    public void refreshGlobalConfig() {
        try {
            log.info("全局配置已更新，将在下次调度周期生效");
        } catch (Exception e) {
            log.error("刷新全局配置失败", e);
        }
    }

    /**
     * 服务器任务信息
     */
    @Data
    public static class ServerTaskInfo {
        private final Integer serverId;
        private Future<?> serverWorkerTask;
        private MonitorSysGenServerSetting currentSetting;
        private volatile boolean taskRunning = true;
        private long lastConfigCheckTime = 0;

        public ServerTaskInfo(Integer serverId) {
            this.serverId = serverId;
        }

        public void cancelAllTasks() {
            taskRunning = false;
            if (serverWorkerTask != null && !serverWorkerTask.isCancelled()) {
                serverWorkerTask.cancel(true);
            }
        }

        public void updateConfigCheckTime() {
            this.lastConfigCheckTime = System.currentTimeMillis();
        }
    }
}
