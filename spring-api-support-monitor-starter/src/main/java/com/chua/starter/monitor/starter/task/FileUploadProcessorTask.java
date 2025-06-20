package com.chua.starter.monitor.starter.task;

import com.chua.starter.monitor.starter.entity.MonitorSysGenFileUploadTask;
import com.chua.starter.monitor.starter.entity.MonitorSysGenFileUploadRecord;
import com.chua.starter.monitor.starter.service.MonitorSysGenFileUploadTaskService;
import com.chua.starter.monitor.starter.service.MonitorSysGenFileUploadRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件上传处理定时任务
 *
 * @author CH
 * @since 2024/12/20
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "monitor.file.upload.processor.enabled", havingValue = "true", matchIfMissing = true)
public class FileUploadProcessorTask {

    private final MonitorSysGenFileUploadTaskService taskService;
    private final MonitorSysGenFileUploadRecordService recordService;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 处理待执行的定时任务
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000) // 1分钟
    public void processPendingScheduledTasks() {
        log.debug("开始处理待执行的定时任务...");
        
        try {
            var tasksResult = taskService.getPendingScheduledTasks();
            if (tasksResult == null || tasksResult.getData() == null || tasksResult.getData().isEmpty()) {
                log.debug("没有找到待执行的定时任务");
                return;
            }

            List<MonitorSysGenFileUploadTask> tasks = tasksResult.getData();
            log.info("找到 {} 个待执行的定时任务", tasks.size());

            // 并行处理定时任务
            List<CompletableFuture<Void>> futures = tasks.stream()
                    .map(task -> CompletableFuture.runAsync(() -> processScheduledTask(task), executorService))
                    .toList();

            // 等待所有任务处理完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> log.debug("所有定时任务处理完成"))
                    .exceptionally(throwable -> {
                        log.error("处理定时任务过程中出现异常", throwable);
                        return null;
                    });

        } catch (Exception e) {
            log.error("处理待执行的定时任务失败", e);
        }
    }

    /**
     * 处理单个定时任务
     *
     * @param task 定时任务
     */
    private void processScheduledTask(MonitorSysGenFileUploadTask task) {
        try {
            log.info("开始处理定时任务: taskId={}, taskName={}", 
                    task.getMonitorSysGenFileUploadTaskId(), 
                    task.getMonitorSysGenFileUploadTaskName());

            // 启动任务
            var startResult = taskService.startUploadTask(task.getMonitorSysGenFileUploadTaskId());
            if (startResult != null && startResult.getData() != null && startResult.getData()) {
                log.info("定时任务启动成功: taskId={}", task.getMonitorSysGenFileUploadTaskId());
                
                // TODO: 实际的文件上传处理逻辑
                processFileUpload(task);
            } else {
                log.warn("定时任务启动失败: taskId={}", task.getMonitorSysGenFileUploadTaskId());
            }

        } catch (Exception e) {
            log.error("处理定时任务失败: taskId={}", task.getMonitorSysGenFileUploadTaskId(), e);
        }
    }

    /**
     * 处理文件上传
     *
     * @param task 上传任务
     */
    private void processFileUpload(MonitorSysGenFileUploadTask task) {
        try {
            // TODO: 实现实际的文件上传逻辑
            log.info("开始处理文件上传: taskId={}, fileName={}", 
                    task.getMonitorSysGenFileUploadTaskId(), 
                    task.getMonitorSysGenFileUploadFileName());

            // 模拟上传处理
            LocalDateTime now = LocalDateTime.now();
            taskService.updateTaskStatus(task.getMonitorSysGenFileUploadTaskId(), 
                    MonitorSysGenFileUploadTask.TaskStatus.COMPLETED.getCode(), 
                    null, now);

            log.info("文件上传处理完成: taskId={}", task.getMonitorSysGenFileUploadTaskId());

        } catch (Exception e) {
            log.error("处理文件上传失败: taskId={}", task.getMonitorSysGenFileUploadTaskId(), e);
            
            // 更新任务状态为失败
            LocalDateTime now = LocalDateTime.now();
            taskService.updateTaskStatus(task.getMonitorSysGenFileUploadTaskId(), 
                    MonitorSysGenFileUploadTask.TaskStatus.FAILED.getCode(), 
                    null, now);
        }
    }

    /**
     * 重试失败的任务
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void retryFailedTasks() {
        log.debug("开始重试失败的任务...");
        
        try {
            var tasksResult = taskService.getRetryableTasks();
            if (tasksResult == null || tasksResult.getData() == null || tasksResult.getData().isEmpty()) {
                log.debug("没有找到可重试的失败任务");
                return;
            }

            List<MonitorSysGenFileUploadTask> tasks = tasksResult.getData();
            log.info("找到 {} 个可重试的失败任务", tasks.size());

            for (MonitorSysGenFileUploadTask task : tasks) {
                try {
                    var retryResult = taskService.retryUploadTask(task.getMonitorSysGenFileUploadTaskId());
                    if (retryResult != null && retryResult.getData() != null && retryResult.getData()) {
                        log.info("任务重试成功: taskId={}", task.getMonitorSysGenFileUploadTaskId());
                    } else {
                        log.warn("任务重试失败: taskId={}", task.getMonitorSysGenFileUploadTaskId());
                    }
                } catch (Exception e) {
                    log.error("重试任务失败: taskId={}", task.getMonitorSysGenFileUploadTaskId(), e);
                }
            }

        } catch (Exception e) {
            log.error("重试失败任务失败", e);
        }
    }

    /**
     * 处理超时任务
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000) // 10分钟
    public void handleTimeoutTasks() {
        log.debug("开始处理超时任务...");
        
        try {
            // 处理30分钟超时的任务
            var tasksResult = taskService.getTimeoutTasks(30);
            if (tasksResult == null || tasksResult.getData() == null || tasksResult.getData().isEmpty()) {
                log.debug("没有找到超时任务");
                return;
            }

            List<MonitorSysGenFileUploadTask> tasks = tasksResult.getData();
            log.info("找到 {} 个超时任务", tasks.size());

            for (MonitorSysGenFileUploadTask task : tasks) {
                try {
                    // 将超时任务标记为失败
                    LocalDateTime now = LocalDateTime.now();
                    taskService.updateTaskStatus(task.getMonitorSysGenFileUploadTaskId(), 
                            MonitorSysGenFileUploadTask.TaskStatus.FAILED.getCode(), 
                            null, now);
                    
                    log.info("超时任务已标记为失败: taskId={}", task.getMonitorSysGenFileUploadTaskId());
                } catch (Exception e) {
                    log.error("处理超时任务失败: taskId={}", task.getMonitorSysGenFileUploadTaskId(), e);
                }
            }

        } catch (Exception e) {
            log.error("处理超时任务失败", e);
        }
    }

    /**
     * 清理长时间上传的记录
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void cleanupLongTimeUploadingRecords() {
        log.debug("开始清理长时间上传的记录...");
        
        try {
            // 清理超过2小时还在上传的记录
            var recordsResult = recordService.getLongTimeUploadingRecords(2);
            if (recordsResult == null || recordsResult.getData() == null || recordsResult.getData().isEmpty()) {
                log.debug("没有找到长时间上传的记录");
                return;
            }

            List<MonitorSysGenFileUploadRecord> records = recordsResult.getData();
            log.info("找到 {} 个长时间上传的记录", records.size());

            for (MonitorSysGenFileUploadRecord record : records) {
                try {
                    // 取消长时间上传的记录
                    recordService.cancelUploadRecord(record.getMonitorSysGenFileUploadRecordId());
                    log.info("长时间上传记录已取消: recordId={}", record.getMonitorSysGenFileUploadRecordId());
                } catch (Exception e) {
                    log.error("取消长时间上传记录失败: recordId={}", record.getMonitorSysGenFileUploadRecordId(), e);
                }
            }

        } catch (Exception e) {
            log.error("清理长时间上传记录失败", e);
        }
    }

    /**
     * 清理过期数据
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredData() {
        log.info("开始清理过期数据...");
        
        try {
            // 清理30天前的已完成任务
            LocalDateTime expiredTime = LocalDateTime.now().minusDays(30);
            
            var taskDeleteResult = taskService.deleteExpiredTasks(expiredTime);
            if (taskDeleteResult != null && taskDeleteResult.getData() != null) {
                log.info("清理过期任务 {} 个", taskDeleteResult.getData());
            }

            var recordDeleteResult = recordService.deleteExpiredRecords(expiredTime);
            if (recordDeleteResult != null && recordDeleteResult.getData() != null) {
                log.info("清理过期记录 {} 个", recordDeleteResult.getData());
            }

        } catch (Exception e) {
            log.error("清理过期数据失败", e);
        }
    }

    /**
     * 优化任务队列
     * 每天上午8点执行
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void optimizeTaskQueue() {
        log.info("开始优化任务队列...");
        
        try {
            var optimizeResult = taskService.optimizeTaskQueue();
            if (optimizeResult != null && optimizeResult.getData() != null) {
                log.info("任务队列优化完成: {}", optimizeResult.getData());
            }

        } catch (Exception e) {
            log.error("优化任务队列失败", e);
        }
    }

    /**
     * 生成上传统计报告
     * 每天上午9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void generateUploadStatisticsReport() {
        log.info("开始生成上传统计报告...");
        
        try {
            // 获取昨天的统计数据
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            LocalDateTime startTime = yesterday.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endTime = yesterday.withHour(23).withMinute(59).withSecond(59);

            var statisticsResult = taskService.getTaskStatistics(startTime, endTime);
            if (statisticsResult != null && statisticsResult.getData() != null) {
                log.info("昨日上传统计: {}", statisticsResult.getData());
                // TODO: 发送统计报告邮件或通知
            }

        } catch (Exception e) {
            log.error("生成上传统计报告失败", e);
        }
    }

    /**
     * 手动触发任务处理
     *
     * @param taskId 任务ID，如果为null则处理所有待处理任务
     */
    public void triggerManualProcess(Long taskId) {
        log.info("手动触发任务处理: taskId={}", taskId);
        
        CompletableFuture.runAsync(() -> {
            try {
                if (taskId != null) {
                    // 处理指定任务
                    MonitorSysGenFileUploadTask task = taskService.getById(taskId);
                    if (task != null) {
                        processScheduledTask(task);
                    }
                } else {
                    // 处理所有待处理任务
                    processPendingScheduledTasks();
                }
            } catch (Exception e) {
                log.error("手动触发任务处理失败: taskId={}", taskId, e);
            }
        }, executorService);
    }

    /**
     * 手动触发重试
     */
    public void triggerManualRetry() {
        log.info("手动触发重试失败任务");
        
        CompletableFuture.runAsync(() -> {
            try {
                retryFailedTasks();
            } catch (Exception e) {
                log.error("手动触发重试失败", e);
            }
        }, executorService);
    }

    /**
     * 手动触发清理
     */
    public void triggerManualCleanup() {
        log.info("手动触发数据清理");
        
        CompletableFuture.runAsync(() -> {
            try {
                cleanupExpiredData();
                cleanupLongTimeUploadingRecords();
            } catch (Exception e) {
                log.error("手动触发清理失败", e);
            }
        }, executorService);
    }
}
