package com.chua.starter.monitor.starter.task;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServerFileUploadQueue;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerFileUploadTaskService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerFileUploadQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * 文件上传队列定时任务
 * 统一管理所有文件上传相关的队列处理任务
 *
 * @author CH
 * @since 2024/12/26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadQueueTask {

    private final MonitorSysGenServerFileUploadTaskService fileUploadTaskService;
    private final MonitorSysGenServerFileUploadQueueService fileUploadQueueService;

    /**
     * 处理文件上传队列
     * 每10秒执行一次，处理待上传的文件任务
     */
    @Scheduled(fixedRate = 10000) // 10秒
    public void processFileUploadQueue() {
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
     * 检查上传任务状态
     * 每30秒执行一次，检查正在进行的上传任务状态
     */
    @Scheduled(fixedRate = 30000) // 30秒
    public void checkUploadTaskStatus() {
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

            // 检查可重试的失败任务
            var retryableResult = fileUploadTaskService.getRetryableTasks();
            if (retryableResult != null && retryableResult.getData() != null) {
                var retryableTasks = retryableResult.getData();
                for (var task : retryableTasks) {
                    try {
                        // 重试失败的任务 - 使用批量重试方法
                        fileUploadTaskService.batchRetryTasks(
                                Collections.singletonList(task.getMonitorSysGenServerFileUploadTaskId()));
                        log.info("重试上传任务: taskId={}", task.getMonitorSysGenServerFileUploadTaskId());
                    } catch (Exception e) {
                        log.error("重试任务失败: taskId={}",
                                task.getMonitorSysGenServerFileUploadTaskId(), e);
                    }
                }
            }

        } catch (Exception e) {
            log.error("检查上传任务状态失败", e);
        }
    }

    /**
     * 清理上传临时文件
     * 每小时执行一次，清理超过1小时的临时文件
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void cleanupUploadTempFiles() {
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

            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);

            try (Stream<Path> files = Files.list(directory)) {
                files.filter(Files::isRegularFile)
                     .filter(path -> prefix == null || path.getFileName().toString().startsWith(prefix))
                     .filter(path -> {
                         try {
                             return Files.getLastModifiedTime(path).toInstant()
                                     .isBefore(cutoffTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                         } catch (IOException e) {
                             return false;
                         }
                     })
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                             log.debug("删除临时文件: {}", path);
                         } catch (IOException e) {
                             log.warn("删除临时文件失败: {}", path, e);
                         }
                     });
            }

        } catch (IOException e) {
            log.error("清理临时目录失败: {}", dirPath, e);
        }
    }
}
