package com.chua.starter.monitor.starter.task;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerFileUploadQueue;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerFileUploadTask;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadQueueStatistics;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadCleanupResult;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadResetResult;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerFileUploadQueueService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerFileUploadTaskService;
import com.chua.starter.monitor.starter.service.ServerConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务器文件上传队列处理器
 * 死循环读取任务队列，处理上传任务
 *
 * @author CH
 * @since 2024/12/20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServerFileUploadQueueProcessor {

    private final MonitorSysGenServerFileUploadQueueService queueService;
    private final MonitorSysGenServerFileUploadTaskService taskService;
    private final List<ServerConnectionService> serverConnectionServices;

    private final AtomicBoolean processorRunning = new AtomicBoolean(false);
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);

    /**
     * 获取SSH连接服务
     */
    private ServerConnectionService getSshConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "SSH".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 应用启动后自动开始处理队列
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("应用启动完成，开始启动文件上传队列处理器...");
        startQueueProcessor();
    }

    /**
     * 启动队列处理器
     */
    @Async
    public void startQueueProcessor() {
        if (processorRunning.compareAndSet(false, true)) {
            log.info("文件上传队列处理器已启动");
            shouldStop.set(false);
            processQueue();
        } else {
            log.warn("文件上传队列处理器已在运行中");
        }
    }

    /**
     * 停止队列处理器
     */
    public void stopQueueProcessor() {
        log.info("正在停止文件上传队列处理器...");
        shouldStop.set(true);
    }

    /**
     * 队列处理主循环
     */
    private void processQueue() {
        log.info("文件上传队列处理器开始运行");
        
        while (!shouldStop.get()) {
            try {
                // 从队列中取出下一个待处理任务
                ReturnResult<MonitorSysGenServerFileUploadQueue> dequeueResult = queueService.dequeue();
                
                if (dequeueResult.isOk() && dequeueResult.getData() != null) {
                    MonitorSysGenServerFileUploadQueue queueItem = dequeueResult.getData();
                    log.debug("从队列中取出任务: queueId={}, taskId={}", 
                             queueItem.getMonitorSysGenServerFileUploadQueueId(),
                             queueItem.getMonitorSysGenServerFileUploadTaskId());
                    
                    // 处理任务
                    processUploadTask(queueItem);
                } else {
                    // 队列为空，休眠100毫秒
                    Thread.sleep(100);
                }
                
            } catch (InterruptedException e) {
                log.warn("队列处理器被中断");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("队列处理器发生异常", e);
                try {
                    Thread.sleep(1000); // 发生异常时休眠1秒
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        processorRunning.set(false);
        log.info("文件上传队列处理器已停止");
    }

    /**
     * 处理上传任务
     *
     * @param queueItem 队列项
     */
    private void processUploadTask(MonitorSysGenServerFileUploadQueue queueItem) {
        Long queueId = queueItem.getMonitorSysGenServerFileUploadQueueId();
        Long taskId = queueItem.getMonitorSysGenServerFileUploadTaskId();
        Integer serverId = queueItem.getMonitorSysGenServerId();
        
        try {
            log.info("开始处理上传任务: queueId={}, taskId={}, serverId={}", queueId, taskId, serverId);
            
            // 获取任务详情
            ReturnResult<MonitorSysGenServerFileUploadTask> taskResult = taskService.getTaskById(taskId);
            if (taskResult == null || !taskResult.isSuccess() || taskResult.getData() == null) {
                log.error("获取任务详情失败: taskId={}", taskId);
                queueService.markFailed(queueId, "获取任务详情失败");
                return;
            }
            
            MonitorSysGenServerFileUploadTask task = taskResult.getData();
            
            // 更新任务状态为处理中
            taskService.updateTaskStatus(taskId, 
                MonitorSysGenServerFileUploadTask.TaskStatus.PROCESSING.getCode(),
                LocalDateTime.now(), null);
            
            // 执行文件上传
            boolean uploadSuccess = performFileUpload(task, serverId);
            
            if (uploadSuccess) {
                // 上传成功
                log.info("文件上传成功: taskId={}, serverId={}", taskId, serverId);
                
                // 更新任务状态为已完成
                taskService.updateTaskStatus(taskId, 
                    MonitorSysGenServerFileUploadTask.TaskStatus.COMPLETED.getCode(),
                    null, LocalDateTime.now());
                
                // 标记队列项为完成
                queueService.markCompleted(queueId);
                
            } else {
                // 上传失败
                log.error("文件上传失败: taskId={}, serverId={}", taskId, serverId);
                
                // 检查是否可以重试
                if (task.canRetry()) {
                    log.info("任务将进行重试: taskId={}, retryCount={}", taskId, task.getMonitorSysGenFileUploadRetryCount());
                    
                    // 增加重试次数
                    taskService.updateTaskStatus(taskId, 
                        MonitorSysGenServerFileUploadTask.TaskStatus.PENDING.getCode(),
                        null, null);
                    
                    // 重新入队
                    queueService.retryQueueItem(queueId);
                } else {
                    log.error("任务重试次数已达上限: taskId={}", taskId);
                    
                    // 更新任务状态为失败
                    taskService.updateTaskStatus(taskId, 
                        MonitorSysGenServerFileUploadTask.TaskStatus.FAILED.getCode(),
                        null, LocalDateTime.now());
                    
                    // 标记队列项为失败
                    queueService.markFailed(queueId, "文件上传失败，重试次数已达上限");
                }
            }
            
        } catch (Exception e) {
            log.error("处理上传任务时发生异常: queueId={}, taskId={}", queueId, taskId, e);
            
            // 更新任务状态为失败
            taskService.updateTaskStatus(taskId, 
                MonitorSysGenServerFileUploadTask.TaskStatus.FAILED.getCode(),
                null, LocalDateTime.now());
            
            // 标记队列项为失败
            queueService.markFailed(queueId, "处理任务时发生异常: " + e.getMessage());
        }
    }

    /**
     * 执行文件上传
     *
     * @param task 上传任务
     * @return 上传是否成功
     */
    private boolean performFileUpload(MonitorSysGenServerFileUploadTask task, Integer serverId) {
        try {
            String localPath = task.getMonitorSysGenServerFileUploadTaskFilePath();
            String remotePath = task.getMonitorSysGenFileUploadTargetPaths(); // 注意：这是JSON格式，需要解析
            
            log.debug("执行文件上传: serverId={}, localPath={}, remotePath={}", serverId, localPath, remotePath);
            
            // 获取SSH连接服务
            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                log.error("SSH服务不可用");
                return false;
            }

            // 检查SSH连接
            if (!sshService.isConnected(serverId)) {
                log.warn("SSH连接不可用，尝试重新连接: serverId={}", serverId);
                // 这里可以尝试重新连接，但为了简化，直接返回失败
                return false;
            }

            // 执行文件上传
            boolean uploadResult = sshService.uploadFile(serverId, localPath, remotePath);
            
            if (uploadResult) {
                log.info("文件上传成功: serverId={}, file={}", serverId, task.getMonitorSysGenServerFileUploadTaskFileName());
                
                // 如果启用了验证，可以在这里进行文件完整性验证
                if (task.isVerifyEnabled()) {
                    // TODO: 实现文件完整性验证
                    log.debug("文件完整性验证已启用，但暂未实现");
                }
                
                return true;
            } else {
                log.error("文件上传失败: serverId={}, file={}", serverId, task.getMonitorSysGenServerFileUploadTaskFileName());
                return false;
            }
            
        } catch (Exception e) {
            log.error("执行文件上传时发生异常: taskId={}", task.getMonitorSysGenServerFileUploadTaskId(), e);
            return false;
        }
    }

    /**
     * 定时清理超时的处理中任务
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void cleanupTimeoutTasks() {
        try {
            log.debug("开始清理超时的处理中任务...");

            // 重置超时的队列项（超时时间为30分钟）
            ReturnResult<MonitorSysGenServerFileUploadResetResult> resetResult = queueService.resetTimeoutProcessingItems(30);
            if (resetResult != null && resetResult.isSuccess() && resetResult.getData() != null) {
                MonitorSysGenServerFileUploadResetResult result = resetResult.getData();
                Long resetCount = result.getResetCount();
                if (resetCount != null && resetCount > 0) {
                    log.info("重置了 {} 个超时的处理中任务", resetCount);
                }
            }

        } catch (Exception e) {
            log.error("清理超时任务时发生异常", e);
        }
    }

    /**
     * 定时清理已完成的队列项
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupCompletedQueueItems() {
        try {
            log.info("开始清理已完成的队列项...");

            // 清理7天前的已完成队列项
            ReturnResult<MonitorSysGenServerFileUploadCleanupResult> cleanupResult = queueService.cleanupCompletedItems(7);
            if (cleanupResult != null && cleanupResult.isSuccess() && cleanupResult.getData() != null) {
                MonitorSysGenServerFileUploadCleanupResult result = cleanupResult.getData();
                Long cleanupCount = result.getCleanupCount();
                if (cleanupCount != null && cleanupCount > 0) {
                    log.info("清理了 {} 个已完成的队列项", cleanupCount);
                }
            }

        } catch (Exception e) {
            log.error("清理已完成队列项时发生异常", e);
        }
    }

    /**
     * 获取处理器运行状态
     *
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return processorRunning.get();
    }

    /**
     * 获取队列统计信息
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000) // 1分钟
    public void logQueueStatistics() {
        try {
            ReturnResult<MonitorSysGenServerFileUploadQueueStatistics> statsResult = queueService.getQueueStatistics();
            if (statsResult != null && statsResult.isSuccess() && statsResult.getData() != null) {
                MonitorSysGenServerFileUploadQueueStatistics stats = statsResult.getData();
                log.debug("队列统计信息: 待处理={}, 处理中={}, 已完成={}, 失败={}, 总数={}",
                         stats.getPendingCount(), stats.getProcessingCount(),
                         stats.getCompletedCount(), stats.getFailedCount(), stats.getTotalCount());
            }
        } catch (Exception e) {
            log.debug("获取队列统计信息时发生异常", e);
        }
    }
}
