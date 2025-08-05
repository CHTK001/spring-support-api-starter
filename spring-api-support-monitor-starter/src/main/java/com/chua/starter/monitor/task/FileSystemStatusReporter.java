package com.chua.starter.monitor.task;

import com.chua.starter.monitor.service.FileSystemService;
import com.chua.starter.monitor.service.FileSystemSseService;
import com.chua.starter.monitor.task.FileSystemMergeQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件系统状态报告器 定期通过SSE发送系统状态和统计信息
 *
 * @author CH
 * @since 2024/12/13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileSystemStatusReporter {

    private final FileSystemService fileSystemService;
    private final FileSystemSseService fileSystemSseService;
    private final FileSystemMergeQueue mergeQueue;

    /**
     * 发送系统状态更新 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void reportSystemStatus() {
        try {
            Map<String, Object> systemStatus = new HashMap<>();

            // 获取文件系统统计信息
            Map<String, Object> statistics = fileSystemService.getStorageStatistics();
            systemStatus.put("storage", statistics);

            // 获取队列状态
            Map<String, Object> queueStatus = getQueueStatus();
            systemStatus.put("queue", queueStatus);

            // 系统运行时间
            systemStatus.put("uptime", System.currentTimeMillis());
            systemStatus.put("timestamp", System.currentTimeMillis());

            // 发送系统状态更新
            fileSystemSseService.sendSystemStatusUpdate(systemStatus);

            log.debug("发送文件系统状态更新: {}", systemStatus);
        } catch (Exception e) {
            log.error("发送系统状态更新失败", e);
        }
    }

    /**
     * 发送队列状态更新 每10秒执行一次
     */
    @Scheduled(fixedRate = 10000)
    public void reportQueueStatus() {
        try {
            Map<String, Object> queueStatus = getQueueStatus();

            // 发送队列状态更新
            fileSystemSseService.sendQueueStatusUpdate(queueStatus);

            log.debug("发送队列状态更新: {}", queueStatus);
        } catch (Exception e) {
            log.error("发送队列状态更新失败", e);
        }
    }

    /**
     * 发送统计信息更新 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void reportStatistics() {
        try {
            Map<String, Object> statistics = fileSystemService.getStorageStatistics();

            // 发送统计信息更新
            fileSystemSseService.sendStatisticsUpdate(statistics);

            log.debug("发送统计信息更新: {}", statistics);
        } catch (Exception e) {
            log.error("发送统计信息更新失败", e);
        }
    }

    /**
     * 获取队列状态信息
     *
     * @return 队列状态信息
     */
    private Map<String, Object> getQueueStatus() {
        Map<String, Object> queueStatus = new HashMap<>();

        try {
            // 获取队列大小
            int queueSize = mergeQueue.size();
            queueStatus.put("queueSize", queueSize);
            queueStatus.put("isEmpty", mergeQueue.isEmpty());

            // 获取队列中的文件ID列表（最多显示前10个）
            queueStatus.put("pendingFiles", mergeQueue.peek(10));

            // 队列状态
            queueStatus.put("status", queueSize > 0 ? "processing" : "idle");

        } catch (Exception e) {
            log.error("获取队列状态失败", e);
            queueStatus.put("error", "获取队列状态失败: " + e.getMessage());
        }

        queueStatus.put("timestamp", System.currentTimeMillis());
        return queueStatus;
    }
}
