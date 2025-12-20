package com.chua.starter.monitor.service.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.task.sync.*;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.job.support.JobProperties;
import com.chua.starter.job.support.log.JobLogDetailService;
import com.chua.starter.job.support.scheduler.JobConfig;
import com.chua.starter.job.support.scheduler.JobTriggerPoolHelper;
import com.chua.starter.monitor.entity.MonitorSyncConnection;
import com.chua.starter.monitor.entity.MonitorSyncNode;
import com.chua.starter.monitor.entity.MonitorSyncTask;
import com.chua.starter.monitor.entity.MonitorSyncTaskLog;
import com.chua.starter.monitor.mapper.MonitorSyncConnectionMapper;
import com.chua.starter.monitor.mapper.MonitorSyncNodeMapper;
import com.chua.starter.monitor.mapper.MonitorSyncTaskLogMapper;
import com.chua.starter.monitor.mapper.MonitorSyncTaskMapper;
import com.chua.starter.monitor.service.sync.MonitorSyncTaskExecutor;
import com.chua.starter.monitor.service.sync.SyncTaskWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 同步任务执行器实现类
 *
 * @author CH
 * @since 2024/12/19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSyncTaskExecutorImpl implements MonitorSyncTaskExecutor {

    private final MonitorSyncTaskMapper taskMapper;
    private final MonitorSyncNodeMapper nodeMapper;
    private final MonitorSyncConnectionMapper connectionMapper;
    private final MonitorSyncTaskLogMapper logMapper;
    private final SyncTaskWebSocketService webSocketService;

    /**
     * Job日志详情服务（可选，集成spring-job时使用）
     */
    @Autowired(required = false)
    private JobLogDetailService jobLogDetailService;

    /**
     * 运行中的 SyncFlow 实例
     */
    private final Map<Long, SyncFlow> runningFlows = new ConcurrentHashMap<>();

    /**
     * 定时任务调度器
     */
    private ThreadPoolTaskScheduler taskScheduler;

    /**
     * 定时任务 Future 映射
     */
    private final Map<Long, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        taskScheduler.setThreadNamePrefix("sync-task-scheduler-");
        taskScheduler.initialize();
        log.info("同步任务执行器初始化完成");

        // 应用启动时自动恢复运行中的任务
        recoverRunningTasks();
    }

    /**
     * 恢复运行中的任务
     * 应用重启后自动恢复状态为 RUNNING 的定时任务
     */
    private void recoverRunningTasks() {
        try {
            List<MonitorSyncTask> runningTasks = taskMapper.selectRunningTasks();
            if (runningTasks == null || runningTasks.isEmpty()) {
                log.info("没有需要恢复的运行中任务");
                return;
            }

            log.info("开始恢复 {} 个运行中的任务", runningTasks.size());
            for (MonitorSyncTask task : runningTasks) {
                try {
                    // 只恢复有定时配置的任务
                    if (StringUtils.isNotEmpty(task.getSyncTaskCron()) 
                            || (task.getSyncTaskSyncInterval() != null && task.getSyncTaskSyncInterval() > 0)) {
                        start(task.getSyncTaskId());
                        log.info("任务恢复成功, taskId: {}, taskName: {}", 
                                task.getSyncTaskId(), task.getSyncTaskName());
                    }
                } catch (Exception e) {
                    log.error("任务恢复失败, taskId: {}, taskName: {}", 
                            task.getSyncTaskId(), task.getSyncTaskName(), e);
                }
            }
            log.info("任务恢复完成");
        } catch (Exception e) {
            log.error("恢复运行中任务失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        stopAll();
        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
    }

    @Override
    public void start(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("任务ID不能为空");
        }

        if (runningFlows.containsKey(taskId)) {
            log.warn("任务已在运行中, taskId: {}", taskId);
            return;
        }

        MonitorSyncTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        log.info("启动同步任务, taskId: {}, taskName: {}", taskId, task.getSyncTaskName());

        try {
            // 构建 SyncFlow
            SyncFlow syncFlow = buildSyncFlow(task);
            runningFlows.put(taskId, syncFlow);

            // 如果有 CRON 表达式，设置定时调度
            if (StringUtils.isNotEmpty(task.getSyncTaskCron())) {
                ScheduledFuture<?> future = taskScheduler.schedule(
                        () -> doExecute(taskId, "SCHEDULE"),
                        new CronTrigger(task.getSyncTaskCron())
                );
                scheduledFutures.put(taskId, future);
                log.info("已设置定时调度, taskId: {}, cron: {}", taskId, task.getSyncTaskCron());
            } else if (task.getSyncTaskSyncInterval() != null && task.getSyncTaskSyncInterval() > 0) {
                // 按间隔执行
                ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(
                        () -> doExecute(taskId, "SCHEDULE"),
                        Duration.ofMillis(task.getSyncTaskSyncInterval())
                );
                scheduledFutures.put(taskId, future);
                log.info("已设置间隔调度, taskId: {}, interval: {}ms", taskId, task.getSyncTaskSyncInterval());
            }

            log.info("同步任务启动成功, taskId: {}", taskId);
        } catch (Exception e) {
            log.error("启动同步任务失败, taskId: {}", taskId, e);
            runningFlows.remove(taskId);
            throw new RuntimeException("启动任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop(Long taskId) {
        if (taskId == null) {
            return;
        }

        log.info("停止同步任务, taskId: {}", taskId);

        // 取消定时调度
        ScheduledFuture<?> future = scheduledFutures.remove(taskId);
        if (future != null) {
            future.cancel(false);
        }

        // 停止 SyncFlow
        SyncFlow syncFlow = runningFlows.remove(taskId);
        if (syncFlow != null) {
            try {
                syncFlow.stop();
            } catch (Exception e) {
                log.error("停止 SyncFlow 失败, taskId: {}", taskId, e);
            }
        }

        log.info("同步任务已停止, taskId: {}", taskId);
    }

    @Override
    public Long executeOnce(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("任务ID不能为空");
        }

        log.info("手动执行同步任务, taskId: {}", taskId);
        return doExecute(taskId, "MANUAL");
    }

    @Override
    public boolean isRunning(Long taskId) {
        return runningFlows.containsKey(taskId);
    }

    @Override
    public void stopAll() {
        log.info("停止所有同步任务, 当前运行数: {}", runningFlows.size());

        for (Long taskId : new ArrayList<>(runningFlows.keySet())) {
            stop(taskId);
        }
    }

    /**
     * 执行同步任务
     */
    private Long doExecute(Long taskId, String triggerType) {
        MonitorSyncTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.error("任务不存在, taskId: {}", taskId);
            return null;
        }

        // 创建执行日志
        MonitorSyncTaskLog taskLog = new MonitorSyncTaskLog();
        taskLog.setSyncTaskId(taskId);
        taskLog.setSyncLogStatus("RUNNING");
        taskLog.setSyncLogTriggerType(triggerType);
        taskLog.setSyncLogStartTime(LocalDateTime.now());
        taskLog.setSyncLogReadCount(0L);
        taskLog.setSyncLogWriteCount(0L);
        taskLog.setSyncLogSuccessCount(0L);
        taskLog.setSyncLogFailCount(0L);
        taskLog.setSyncLogRetryCount(0L);
        taskLog.setSyncLogDeadLetterCount(0L);
        taskLog.setSyncLogFilterCount(0L);
        logMapper.insert(taskLog);

        // 推送任务开始执行
        webSocketService.pushTaskStarted(task, taskLog);

        // 记录详细日志（集成spring-job）
        logDetail(taskLog.getSyncLogId().intValue(), task.getSyncTaskId().intValue(), 
                "INFO", "同步任务开始执行: " + task.getSyncTaskName(), "START", 0);

        long startTime = System.currentTimeMillis();

        try {
            // 获取或构建 SyncFlow
            SyncFlow syncFlow = runningFlows.get(taskId);
            if (syncFlow == null) {
                syncFlow = buildSyncFlow(task);
            }

            // 执行同步
            syncFlow.start();

            // 等待完成（这里简化处理，实际可能需要更复杂的逻辑）
            // SyncFlow 内部会处理数据同步

            // 更新日志
            long cost = System.currentTimeMillis() - startTime;
            taskLog.setSyncLogStatus("SUCCESS");
            taskLog.setSyncLogEndTime(LocalDateTime.now());
            taskLog.setSyncLogCost(cost);
            taskLog.setSyncLogMessage("执行成功");

            // 获取指标数据（如果 SyncFlow 支持的话）
            SyncMetrics metrics = syncFlow.getMetrics();
            if (metrics != null) {
                taskLog.setSyncLogReadCount(metrics.getInputCount().get());
                taskLog.setSyncLogWriteCount(metrics.getOutputCount().get());
                taskLog.setSyncLogSuccessCount(metrics.getSuccessCount().get());
                taskLog.setSyncLogFailCount(metrics.getFailCount().get());

                if (metrics.getSuccessCount().get() > 0 && cost > 0) {
                    taskLog.setSyncLogThroughput((double) metrics.getSuccessCount().get() / (cost / 1000.0));
                    taskLog.setSyncLogAvgProcessTime((double) cost / metrics.getSuccessCount().get());
                }
            }

            logMapper.updateById(taskLog);

            // 记录详细日志
            logDetail(taskLog.getSyncLogId().intValue(), task.getSyncTaskId().intValue(),
                    "INFO", String.format("同步任务执行成功, 读取: %d, 写入: %d, 成功: %d, 失败: %d, 耗时: %dms",
                            taskLog.getSyncLogReadCount(), taskLog.getSyncLogWriteCount(),
                            taskLog.getSyncLogSuccessCount(), taskLog.getSyncLogFailCount(), cost),
                    "END", 100);

            // 更新任务统计
            task.setSyncTaskLastRunTime(LocalDateTime.now());
            task.setSyncTaskLastRunStatus("SUCCESS");
            task.setSyncTaskRunCount(task.getSyncTaskRunCount() + 1);
            task.setSyncTaskSuccessCount(task.getSyncTaskSuccessCount() + 1);
            taskMapper.updateById(task);

            // 推送任务完成
            webSocketService.pushTaskCompleted(task, taskLog);

            log.info("同步任务执行成功, taskId: {}, 耗时: {}ms", taskId, cost);
            return taskLog.getSyncLogId();

        } catch (Exception e) {
            log.error("同步任务执行失败, taskId: {}", taskId, e);

            // 记录错误详细日志
            logDetailError(taskLog.getSyncLogId().intValue(), task.getSyncTaskId().intValue(),
                    "同步任务执行失败", e);

            // 更新日志
            long cost = System.currentTimeMillis() - startTime;
            taskLog.setSyncLogStatus("FAIL");
            taskLog.setSyncLogEndTime(LocalDateTime.now());
            taskLog.setSyncLogCost(cost);
            taskLog.setSyncLogMessage("执行失败: " + e.getMessage());
            taskLog.setSyncLogStackTrace(getStackTrace(e));
            logMapper.updateById(taskLog);

            // 更新任务统计
            task.setSyncTaskLastRunTime(LocalDateTime.now());
            task.setSyncTaskLastRunStatus("FAIL");
            task.setSyncTaskRunCount(task.getSyncTaskRunCount() + 1);
            task.setSyncTaskFailCount(task.getSyncTaskFailCount() + 1);
            task.setSyncTaskStatus("ERROR");
            taskMapper.updateById(task);

            // 推送任务失败
            webSocketService.pushTaskCompleted(task, taskLog);
            webSocketService.pushError(taskId, taskLog.getSyncLogId(), null, "任务执行失败", e);

            return taskLog.getSyncLogId();
        }
    }

    /**
     * 构建 SyncFlow
     */
    private SyncFlow buildSyncFlow(MonitorSyncTask task) {
        Long taskId = task.getSyncTaskId();

        // 获取节点和连线
        List<MonitorSyncNode> nodes = nodeMapper.selectByTaskId(taskId);
        List<MonitorSyncConnection> connections = connectionMapper.selectByTaskId(taskId);

        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("任务没有配置节点");
        }

        // 按类型分类节点
        List<MonitorSyncNode> inputNodes = new ArrayList<>();
        List<MonitorSyncNode> outputNodes = new ArrayList<>();
        List<MonitorSyncNode> filterNodes = new ArrayList<>();
        List<MonitorSyncNode> dataCenterNodes = new ArrayList<>();

        for (MonitorSyncNode node : nodes) {
            if (node.getSyncNodeEnabled() == null || node.getSyncNodeEnabled() != 1) {
                continue;
            }
            switch (node.getSyncNodeType()) {
                case "INPUT":
                    inputNodes.add(node);
                    break;
                case "OUTPUT":
                    outputNodes.add(node);
                    break;
                case "FILTER":
                    filterNodes.add(node);
                    break;
                case "DATA_CENTER":
                    dataCenterNodes.add(node);
                    break;
            }
        }

        if (inputNodes.isEmpty()) {
            throw new IllegalArgumentException("必须至少有一个输入节点");
        }
        if (outputNodes.isEmpty()) {
            throw new IllegalArgumentException("必须至少有一个输出节点");
        }

        // 创建 SyncFlow
        SyncFlow.SyncFlowBuilder builder = SyncFlow.builder();

        // 设置输入
        for (MonitorSyncNode inputNode : inputNodes) {
            Input input = createInput(inputNode);
            builder.input(input);
        }

        // 设置输出
        for (MonitorSyncNode outputNode : outputNodes) {
            Output output = createOutput(outputNode);
            builder.output(output);
        }

        // 设置数据中心（如果有）
        if (!dataCenterNodes.isEmpty()) {
            MonitorSyncNode dataCenterNode = dataCenterNodes.get(0);
            DataCenter dataCenter = createDataCenter(dataCenterNode);
            builder.dataCenter(dataCenter);
        }

        // 设置过滤器
        for (MonitorSyncNode filterNode : filterNodes) {
            DataFilter filter = createFilter(filterNode);
            builder.filter(filter);
        }

        // 设置任务参数
        if (task.getSyncTaskBatchSize() != null) {
            builder.batchSize(task.getSyncTaskBatchSize());
        }
        if (task.getSyncTaskConsumeTimeout() != null) {
            builder.consumeTimeout(task.getSyncTaskConsumeTimeout());
        }
        if (task.getSyncTaskRetryCount() != null) {
            builder.retryCount(task.getSyncTaskRetryCount());
        }
        if (task.getSyncTaskRetryInterval() != null) {
            builder.retryInterval(task.getSyncTaskRetryInterval());
        }
        if (task.getSyncTaskAckEnabled() != null && task.getSyncTaskAckEnabled() == 1) {
            builder.ackEnabled(true);
        }

        return builder.build();
    }

    /**
     * 创建 Input
     */
    @SuppressWarnings("unchecked")
    private Input createInput(MonitorSyncNode node) {
        String spiName = node.getSyncNodeSpiName();
        Map<String, Object> config = parseConfig(node.getSyncNodeConfig());

        ServiceProvider<Input> provider = ServiceProvider.of(Input.class);
        return provider.getNewExtension(spiName, config);
    }

    /**
     * 创建 Output
     */
    @SuppressWarnings("unchecked")
    private Output createOutput(MonitorSyncNode node) {
        String spiName = node.getSyncNodeSpiName();
        Map<String, Object> config = parseConfig(node.getSyncNodeConfig());

        ServiceProvider<Output> provider = ServiceProvider.of(Output.class);
        return provider.getNewExtension(spiName, config);
    }

    /**
     * 创建 DataCenter
     */
    @SuppressWarnings("unchecked")
    private DataCenter createDataCenter(MonitorSyncNode node) {
        String spiName = node.getSyncNodeSpiName();
        Map<String, Object> config = parseConfig(node.getSyncNodeConfig());

        ServiceProvider<DataCenter> provider = ServiceProvider.of(DataCenter.class);
        return provider.getNewExtension(spiName, config);
    }

    /**
     * 创建 DataFilter
     */
    @SuppressWarnings("unchecked")
    private DataFilter createFilter(MonitorSyncNode node) {
        String spiName = node.getSyncNodeSpiName();
        Map<String, Object> config = parseConfig(node.getSyncNodeConfig());

        ServiceProvider<DataFilter> provider = ServiceProvider.of(DataFilter.class);
        return provider.getNewExtension(spiName, config);
    }

    /**
     * 解析配置JSON
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfig(String configJson) {
        if (StringUtils.isEmpty(configJson)) {
            return new HashMap<>();
        }
        try {
            return Json.fromJson(configJson, Map.class);
        } catch (Exception e) {
            log.warn("解析配置JSON失败: {}", configJson);
            return new HashMap<>();
        }
    }

    /**
     * 获取异常堆栈
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            if (sb.length() > 4000) {
                sb.append("...(truncated)");
                break;
            }
        }
        return sb.toString();
    }

    /**
     * 记录详细日志（集成spring-job模块）
     *
     * @param logId    日志ID
     * @param taskId   任务ID
     * @param level    日志级别
     * @param content  日志内容
     * @param phase    执行阶段
     * @param progress 执行进度
     */
    private void logDetail(Integer logId, Integer taskId, String level, String content, 
                           String phase, Integer progress) {
        if (jobLogDetailService != null) {
            try {
                jobLogDetailService.log(logId, taskId, level, content, phase, progress);
            } catch (Exception e) {
                log.warn("记录详细日志失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 记录错误详细日志
     */
    private void logDetailError(Integer logId, Integer taskId, String content, Throwable e) {
        if (jobLogDetailService != null) {
            try {
                jobLogDetailService.error(logId, taskId, content, e);
            } catch (Exception ex) {
                log.warn("记录错误详细日志失败: {}", ex.getMessage());
            }
        }
    }
}
