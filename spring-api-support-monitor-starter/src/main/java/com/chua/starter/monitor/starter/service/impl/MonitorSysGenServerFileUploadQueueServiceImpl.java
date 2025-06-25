package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerFileUploadQueue;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerFileUploadTask;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadQueueStatistics;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadCleanupResult;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadResetResult;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerFileUploadQueueMapper;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerFileUploadQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 服务器文件上传任务队列服务实现类
 *
 * @author CH
 * @since 2024/12/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenServerFileUploadQueueServiceImpl
        extends ServiceImpl<MonitorSysGenServerFileUploadQueueMapper, MonitorSysGenServerFileUploadQueue>
        implements MonitorSysGenServerFileUploadQueueService {

    // 队列处理状态
    private final AtomicBoolean queueProcessingEnabled = new AtomicBoolean(true);

    // 内存队列 - 全局队列（按优先级排序）
    private final BlockingQueue<MonitorSysGenServerFileUploadQueue> memoryQueue = new LinkedBlockingQueue<>();

    // 内存队列 - 按服务器ID分组的队列
    private final ConcurrentHashMap<Integer, BlockingQueue<MonitorSysGenServerFileUploadQueue>> serverQueues = new ConcurrentHashMap<>();

    // 队列同步锁
    private final ReentrantLock queueLock = new ReentrantLock();

    // 队列加载状态
    private final AtomicBoolean queueLoaded = new AtomicBoolean(false);

    /**
     * 定时从数据库加载待处理任务到内存队列
     * 每5秒执行一次
     */
    @Scheduled(fixedRate = 5000)
    public void loadPendingTasksToMemoryQueue() {
        try {
            queueLock.lock();

            // 查询数据库中的待处理任务
            LambdaQueryWrapper<MonitorSysGenServerFileUploadQueue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PENDING.getCode())
                       .orderByAsc(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueuePriority)
                       .orderByAsc(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueCreateTime);

            List<MonitorSysGenServerFileUploadQueue> pendingTasks = list(queryWrapper);

            if (pendingTasks != null && !pendingTasks.isEmpty()) {
                // 清空现有内存队列
                memoryQueue.clear();
                serverQueues.clear();

                // 将任务添加到内存队列
                for (MonitorSysGenServerFileUploadQueue task : pendingTasks) {
                    // 添加到全局队列
                    memoryQueue.offer(task);

                    // 添加到服务器专用队列
                    Integer serverId = task.getMonitorSysGenServerId();
                    serverQueues.computeIfAbsent(serverId, k -> new LinkedBlockingQueue<>()).offer(task);
                }

                log.debug("已加载 {} 个待处理任务到内存队列", pendingTasks.size());
                queueLoaded.set(true);
            } else if (!queueLoaded.get()) {
                // 首次加载时，即使没有任务也标记为已加载
                queueLoaded.set(true);
                log.debug("内存队列初始化完成，当前无待处理任务");
            }

        } catch (Exception e) {
            log.error("加载待处理任务到内存队列失败", e);
        } finally {
            queueLock.unlock();
        }
    }

    @Override
    @Transactional
    public ReturnResult<MonitorSysGenServerFileUploadQueue> enqueue(MonitorSysGenServerFileUploadTask task, Integer serverId) {
        try {
            // 检查是否已存在相同任务的队列项
            LambdaQueryWrapper<MonitorSysGenServerFileUploadQueue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadTaskId, 
                           task.getMonitorSysGenServerFileUploadTaskId())
                       .in(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PENDING.getCode(),
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PROCESSING.getCode());
            
            MonitorSysGenServerFileUploadQueue existingItem = getOne(queryWrapper);
            if (existingItem != null) {
                log.warn("任务已在队列中: taskId={}", task.getMonitorSysGenServerFileUploadTaskId());
                return ReturnResult.success(existingItem);
            }

            // 创建新的队列项
            MonitorSysGenServerFileUploadQueue queueItem = new MonitorSysGenServerFileUploadQueue();
            queueItem.setMonitorSysGenServerFileUploadTaskId(task.getMonitorSysGenServerFileUploadTaskId());
            queueItem.setMonitorSysGenServerId(serverId);
            queueItem.setMonitorSysGenServerFileUploadQueuePriority(task.getMonitorSysGenFileUploadPriority());
            queueItem.setMonitorSysGenServerFileUploadQueueStatus(MonitorSysGenServerFileUploadQueue.QueueStatus.PENDING.getCode());
            queueItem.setMonitorSysGenServerFileUploadQueueRetryCount(0);
            queueItem.setMonitorSysGenServerFileUploadQueueCreateTime(LocalDateTime.now());

            boolean saved = save(queueItem);
            if (saved) {
                // 同时添加到内存队列
                try {
                    queueLock.lock();
                    memoryQueue.offer(queueItem);
                    serverQueues.computeIfAbsent(serverId, k -> new LinkedBlockingQueue<>()).offer(queueItem);
                } finally {
                    queueLock.unlock();
                }

                log.info("任务已加入队列: taskId={}, queueId={}",
                        task.getMonitorSysGenServerFileUploadTaskId(),
                        queueItem.getMonitorSysGenServerFileUploadQueueId());
                return ReturnResult.success(queueItem);
            } else {
                return ReturnResult.error("任务入队失败");
            }
        } catch (Exception e) {
            log.error("任务入队失败", e);
            return ReturnResult.error("任务入队失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<MonitorSysGenServerFileUploadQueue> dequeue() {
        try {
            if (!queueProcessingEnabled.get()) {
                return ReturnResult.error("队列处理已暂停");
            }

            // 等待队列加载完成
            if (!queueLoaded.get()) {
                return ReturnResult.success(null);
            }

            MonitorSysGenServerFileUploadQueue queueItem = null;

            try {
                queueLock.lock();
                // 从内存队列中获取任务
                queueItem = memoryQueue.poll();

                if (queueItem != null) {
                    // 同时从服务器队列中移除
                    Integer serverId = queueItem.getMonitorSysGenServerId();
                    BlockingQueue<MonitorSysGenServerFileUploadQueue> serverQueue = serverQueues.get(serverId);
                    if (serverQueue != null) {
                        serverQueue.remove(queueItem);
                    }
                }
            } finally {
                queueLock.unlock();
            }

            if (queueItem == null) {
                return ReturnResult.success(null);
            }

            // 使用乐观锁更新数据库状态
            LambdaUpdateWrapper<MonitorSysGenServerFileUploadQueue> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueId,
                           queueItem.getMonitorSysGenServerFileUploadQueueId())
                        .eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PENDING.getCode())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PROCESSING.getCode())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueProcessTime,
                           LocalDateTime.now());

            boolean updated = update(updateWrapper);
            if (updated) {
                queueItem.setMonitorSysGenServerFileUploadQueueStatus(MonitorSysGenServerFileUploadQueue.QueueStatus.PROCESSING.getCode());
                queueItem.setMonitorSysGenServerFileUploadQueueProcessTime(LocalDateTime.now());

                log.debug("从内存队列中取出任务: queueId={}, taskId={}",
                         queueItem.getMonitorSysGenServerFileUploadQueueId(),
                         queueItem.getMonitorSysGenServerFileUploadTaskId());
                return ReturnResult.success(queueItem);
            } else {
                // 数据库更新失败，将任务重新放回内存队列
                try {
                    queueLock.lock();
                    memoryQueue.offer(queueItem);
                    Integer serverId = queueItem.getMonitorSysGenServerId();
                    serverQueues.computeIfAbsent(serverId, k -> new LinkedBlockingQueue<>()).offer(queueItem);
                } finally {
                    queueLock.unlock();
                }
                return ReturnResult.error("队列项状态更新失败");
            }
        } catch (Exception e) {
            log.error("从队列中取出任务失败", e);
            return ReturnResult.error("出队失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<MonitorSysGenServerFileUploadQueue> dequeueByServerId(Integer serverId) {
        try {
            if (!queueProcessingEnabled.get()) {
                return ReturnResult.error("队列处理已暂停");
            }

            // 等待队列加载完成
            if (!queueLoaded.get()) {
                return ReturnResult.success(null);
            }

            MonitorSysGenServerFileUploadQueue queueItem = null;

            try {
                queueLock.lock();
                // 从指定服务器的内存队列中获取任务
                BlockingQueue<MonitorSysGenServerFileUploadQueue> serverQueue = serverQueues.get(serverId);
                if (serverQueue != null) {
                    queueItem = serverQueue.poll();

                    if (queueItem != null) {
                        // 同时从全局队列中移除
                        memoryQueue.remove(queueItem);
                    }
                }
            } finally {
                queueLock.unlock();
            }

            if (queueItem == null) {
                return ReturnResult.success(null);
            }

            // 使用乐观锁更新数据库状态
            LambdaUpdateWrapper<MonitorSysGenServerFileUploadQueue> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueId,
                           queueItem.getMonitorSysGenServerFileUploadQueueId())
                        .eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PENDING.getCode())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PROCESSING.getCode())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueProcessTime,
                           LocalDateTime.now());

            boolean updated = update(updateWrapper);
            if (updated) {
                queueItem.setMonitorSysGenServerFileUploadQueueStatus(MonitorSysGenServerFileUploadQueue.QueueStatus.PROCESSING.getCode());
                queueItem.setMonitorSysGenServerFileUploadQueueProcessTime(LocalDateTime.now());

                log.debug("从服务器内存队列中取出任务: serverId={}, queueId={}, taskId={}",
                         serverId, queueItem.getMonitorSysGenServerFileUploadQueueId(),
                         queueItem.getMonitorSysGenServerFileUploadTaskId());
                return ReturnResult.success(queueItem);
            } else {
                // 数据库更新失败，将任务重新放回内存队列
                try {
                    queueLock.lock();
                    memoryQueue.offer(queueItem);
                    serverQueues.computeIfAbsent(serverId, k -> new LinkedBlockingQueue<>()).offer(queueItem);
                } finally {
                    queueLock.unlock();
                }
                return ReturnResult.error("队列项状态更新失败");
            }
        } catch (Exception e) {
            log.error("从服务器队列中取出任务失败: serverId={}", serverId, e);
            return ReturnResult.error("出队失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<List<MonitorSysGenServerFileUploadQueue>> dequeueBatch(Integer batchSize) {
        try {
            if (!queueProcessingEnabled.get()) {
                return ReturnResult.error("队列处理已暂停");
            }

            // 等待队列加载完成
            if (!queueLoaded.get()) {
                return ReturnResult.success(List.of());
            }

            if (batchSize == null || batchSize <= 0) {
                batchSize = 10;
            }

            List<MonitorSysGenServerFileUploadQueue> queueItems = new ArrayList<>();

            try {
                queueLock.lock();
                // 从内存队列中批量获取任务
                for (int i = 0; i < batchSize && !memoryQueue.isEmpty(); i++) {
                    MonitorSysGenServerFileUploadQueue queueItem = memoryQueue.poll();
                    if (queueItem != null) {
                        queueItems.add(queueItem);

                        // 同时从服务器队列中移除
                        Integer serverId = queueItem.getMonitorSysGenServerId();
                        BlockingQueue<MonitorSysGenServerFileUploadQueue> serverQueue = serverQueues.get(serverId);
                        if (serverQueue != null) {
                            serverQueue.remove(queueItem);
                        }
                    }
                }
            } finally {
                queueLock.unlock();
            }

            if (queueItems.isEmpty()) {
                return ReturnResult.success(queueItems);
            }

            // 批量更新数据库状态为处理中
            List<Long> queueIds = queueItems.stream()
                    .map(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueId)
                    .toList();

            LambdaUpdateWrapper<MonitorSysGenServerFileUploadQueue> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueId, queueIds)
                        .eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PENDING.getCode())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PROCESSING.getCode())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueProcessTime,
                           LocalDateTime.now());

            boolean updated = update(updateWrapper);
            if (updated) {
                // 更新内存中的对象状态
                LocalDateTime now = LocalDateTime.now();
                queueItems.forEach(item -> {
                    item.setMonitorSysGenServerFileUploadQueueStatus(MonitorSysGenServerFileUploadQueue.QueueStatus.PROCESSING.getCode());
                    item.setMonitorSysGenServerFileUploadQueueProcessTime(now);
                });

                log.debug("批量从内存队列中取出任务: count={}", queueItems.size());
                return ReturnResult.success(queueItems);
            } else {
                // 数据库更新失败，将任务重新放回内存队列
                try {
                    queueLock.lock();
                    for (MonitorSysGenServerFileUploadQueue queueItem : queueItems) {
                        memoryQueue.offer(queueItem);
                        Integer serverId = queueItem.getMonitorSysGenServerId();
                        serverQueues.computeIfAbsent(serverId, k -> new LinkedBlockingQueue<>()).offer(queueItem);
                    }
                } finally {
                    queueLock.unlock();
                }
                return ReturnResult.error("批量队列项状态更新失败");
            }
        } catch (Exception e) {
            log.error("批量从队列中取出任务失败", e);
            return ReturnResult.error("批量出队失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> updateQueueStatus(Long queueId, String status) {
        try {
            LambdaUpdateWrapper<MonitorSysGenServerFileUploadQueue> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueId, queueId)
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus, status);

            boolean updated = update(updateWrapper);
            if (updated) {
                log.debug("队列项状态已更新: queueId={}, status={}", queueId, status);
                return ReturnResult.success(true);
            } else {
                return ReturnResult.error("队列项状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新队列项状态失败: queueId={}, status={}", queueId, status, e);
            return ReturnResult.error("状态更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> markProcessing(Long queueId) {
        try {
            LambdaUpdateWrapper<MonitorSysGenServerFileUploadQueue> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueId, queueId)
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PROCESSING.getCode())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueProcessTime,
                           LocalDateTime.now());

            boolean updated = update(updateWrapper);
            return ReturnResult.success(updated);
        } catch (Exception e) {
            log.error("标记队列项处理中失败: queueId={}", queueId, e);
            return ReturnResult.error("标记处理中失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> markCompleted(Long queueId) {
        try {
            LambdaUpdateWrapper<MonitorSysGenServerFileUploadQueue> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueId, queueId)
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.COMPLETED.getCode())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueCompleteTime,
                           LocalDateTime.now());

            boolean updated = update(updateWrapper);
            if (updated) {
                log.debug("队列项已标记为完成: queueId={}", queueId);
            }
            return ReturnResult.success(updated);
        } catch (Exception e) {
            log.error("标记队列项完成失败: queueId={}", queueId, e);
            return ReturnResult.error("标记完成失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> markFailed(Long queueId, String errorMessage) {
        try {
            LambdaUpdateWrapper<MonitorSysGenServerFileUploadQueue> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueId, queueId)
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.FAILED.getCode())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueCompleteTime,
                           LocalDateTime.now())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueErrorMessage,
                           errorMessage);

            boolean updated = update(updateWrapper);
            if (updated) {
                log.debug("队列项已标记为失败: queueId={}, error={}", queueId, errorMessage);
            }
            return ReturnResult.success(updated);
        } catch (Exception e) {
            log.error("标记队列项失败失败: queueId={}", queueId, e);
            return ReturnResult.error("标记失败失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> retryQueueItem(Long queueId) {
        try {
            // 先获取队列项信息
            MonitorSysGenServerFileUploadQueue queueItem = getById(queueId);
            if (queueItem == null) {
                return ReturnResult.error("队列项不存在");
            }

            // 将失败的队列项重新设置为待处理状态
            LambdaUpdateWrapper<MonitorSysGenServerFileUploadQueue> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueId, queueId)
                        .eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.FAILED.getCode())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PENDING.getCode())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueRetryCount,
                           0) // 重置重试次数
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueErrorMessage,
                           null); // 清除错误信息

            boolean updated = update(updateWrapper);
            if (updated) {
                // 更新内存中的状态并重新加入内存队列
                queueItem.setMonitorSysGenServerFileUploadQueueStatus(MonitorSysGenServerFileUploadQueue.QueueStatus.PENDING.getCode());
                queueItem.setMonitorSysGenServerFileUploadQueueRetryCount(0);
                queueItem.setMonitorSysGenServerFileUploadQueueErrorMessage(null);

                try {
                    queueLock.lock();
                    memoryQueue.offer(queueItem);
                    Integer serverId = queueItem.getMonitorSysGenServerId();
                    serverQueues.computeIfAbsent(serverId, k -> new LinkedBlockingQueue<>()).offer(queueItem);
                } finally {
                    queueLock.unlock();
                }

                log.debug("队列项已重新加入队列: queueId={}", queueId);
            }
            return ReturnResult.success(updated);
        } catch (Exception e) {
            log.error("重试队列项失败: queueId={}", queueId, e);
            return ReturnResult.error("重试失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerFileUploadQueueStatistics> getQueueStatistics() {
        try {
            MonitorSysGenServerFileUploadQueueStatistics statistics = new MonitorSysGenServerFileUploadQueueStatistics();

            // 统计各状态的队列项数量
            LambdaQueryWrapper<MonitorSysGenServerFileUploadQueue> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                             MonitorSysGenServerFileUploadQueue.QueueStatus.PENDING.getCode());
            long pendingCount = count(pendingWrapper);

            LambdaQueryWrapper<MonitorSysGenServerFileUploadQueue> processingWrapper = new LambdaQueryWrapper<>();
            processingWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                                MonitorSysGenServerFileUploadQueue.QueueStatus.PROCESSING.getCode());
            long processingCount = count(processingWrapper);

            LambdaQueryWrapper<MonitorSysGenServerFileUploadQueue> completedWrapper = new LambdaQueryWrapper<>();
            completedWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                               MonitorSysGenServerFileUploadQueue.QueueStatus.COMPLETED.getCode());
            long completedCount = count(completedWrapper);

            LambdaQueryWrapper<MonitorSysGenServerFileUploadQueue> failedWrapper = new LambdaQueryWrapper<>();
            failedWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                            MonitorSysGenServerFileUploadQueue.QueueStatus.FAILED.getCode());
            long failedCount = count(failedWrapper);

            statistics.setPendingCount(pendingCount);
            statistics.setProcessingCount(processingCount);
            statistics.setCompletedCount(completedCount);
            statistics.setFailedCount(failedCount);
            statistics.setQueueProcessingEnabled(queueProcessingEnabled.get());

            // 计算总数和成功率
            statistics.calculateTotalCount();
            statistics.calculateSuccessRate();

            return ReturnResult.success(statistics);
        } catch (Exception e) {
            log.error("获取队列统计信息失败", e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<MonitorSysGenServerFileUploadCleanupResult> cleanupCompletedItems(Integer retentionDays) {
        try {
            if (retentionDays == null || retentionDays <= 0) {
                retentionDays = 7; // 默认保留7天
            }

            LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);

            // 删除指定天数前的已完成队列项
            LambdaQueryWrapper<MonitorSysGenServerFileUploadQueue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.COMPLETED.getCode())
                       .lt(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueCompleteTime,
                          cutoffTime);

            long countBeforeCleanup = count(queryWrapper);
            boolean removed = remove(queryWrapper);
            LocalDateTime endTime = LocalDateTime.now();

            MonitorSysGenServerFileUploadCleanupResult result = new MonitorSysGenServerFileUploadCleanupResult();
            result.setCleanupType(MonitorSysGenServerFileUploadCleanupResult.CleanupType.COMPLETED.getCode());
            result.setCleanupCount(removed ? countBeforeCleanup : 0);
            result.setRetentionDays(retentionDays);
            result.setCutoffTime(cutoffTime);
            result.setStartTime(startTime);
            result.setEndTime(endTime);
            result.setSuccess(removed);
            result.calculateDuration();

            if (removed && countBeforeCleanup > 0) {
                log.info("清理了 {} 个已完成的队列项，保留天数: {}", countBeforeCleanup, retentionDays);
                result.setDetails(String.format("成功清理了 %d 个已完成的队列项", countBeforeCleanup));
            } else {
                result.setDetails("没有需要清理的已完成队列项");
            }

            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("清理已完成队列项失败: retentionDays={}", retentionDays, e);

            MonitorSysGenServerFileUploadCleanupResult errorResult = new MonitorSysGenServerFileUploadCleanupResult();
            errorResult.setCleanupType(MonitorSysGenServerFileUploadCleanupResult.CleanupType.COMPLETED.getCode());
            errorResult.setRetentionDays(retentionDays);
            errorResult.setSuccess(false);
            errorResult.setErrorMessage(e.getMessage());

            return ReturnResult.error("清理失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<MonitorSysGenServerFileUploadResetResult> resetTimeoutProcessingItems(Integer timeoutMinutes) {
        try {
            if (timeoutMinutes == null || timeoutMinutes <= 0) {
                timeoutMinutes = 30; // 默认30分钟超时
            }

            LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(timeoutMinutes);

            // 查找超时的处理中队列项
            LambdaQueryWrapper<MonitorSysGenServerFileUploadQueue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PROCESSING.getCode())
                       .lt(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueProcessTime,
                          timeoutTime);

            List<MonitorSysGenServerFileUploadQueue> timeoutItems = list(queryWrapper);
            long countBeforeReset = timeoutItems.size();

            // 重置超时的处理中队列项为待处理状态
            LambdaUpdateWrapper<MonitorSysGenServerFileUploadQueue> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PROCESSING.getCode())
                        .lt(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueProcessTime,
                           timeoutTime)
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueStatus,
                           MonitorSysGenServerFileUploadQueue.QueueStatus.PENDING.getCode())
                        .set(MonitorSysGenServerFileUploadQueue::getMonitorSysGenServerFileUploadQueueProcessTime,
                           null);

            boolean updated = update(updateWrapper);

            // 如果更新成功，将重置的任务重新加入内存队列
            if (updated && !timeoutItems.isEmpty()) {
                try {
                    queueLock.lock();
                    for (MonitorSysGenServerFileUploadQueue timeoutItem : timeoutItems) {
                        timeoutItem.setMonitorSysGenServerFileUploadQueueStatus(MonitorSysGenServerFileUploadQueue.QueueStatus.PENDING.getCode());
                        timeoutItem.setMonitorSysGenServerFileUploadQueueProcessTime(null);

                        memoryQueue.offer(timeoutItem);
                        Integer serverId = timeoutItem.getMonitorSysGenServerId();
                        serverQueues.computeIfAbsent(serverId, k -> new LinkedBlockingQueue<>()).offer(timeoutItem);
                    }
                } finally {
                    queueLock.unlock();
                }
            }
            LocalDateTime endTime = LocalDateTime.now();

            MonitorSysGenServerFileUploadResetResult result = new MonitorSysGenServerFileUploadResetResult();
            result.setResetType(MonitorSysGenServerFileUploadResetResult.ResetType.TIMEOUT.getCode());
            result.setResetCount(updated ? countBeforeReset : 0);
            result.setTimeoutMinutes(timeoutMinutes);
            result.setTimeoutTime(timeoutTime);
            result.setStartTime(startTime);
            result.setEndTime(endTime);
            result.setSuccess(updated);
            result.calculateDuration();

            if (updated && countBeforeReset > 0) {
                log.info("重置了 {} 个超时的处理中队列项，超时时间: {} 分钟", countBeforeReset, timeoutMinutes);
                result.setDetails(String.format("成功重置了 %d 个超时的处理中队列项", countBeforeReset));
            } else {
                result.setDetails("没有需要重置的超时队列项");
            }

            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("重置超时处理中队列项失败: timeoutMinutes={}", timeoutMinutes, e);

            MonitorSysGenServerFileUploadResetResult errorResult = new MonitorSysGenServerFileUploadResetResult();
            errorResult.setResetType(MonitorSysGenServerFileUploadResetResult.ResetType.TIMEOUT.getCode());
            errorResult.setTimeoutMinutes(timeoutMinutes);
            errorResult.setSuccess(false);
            errorResult.setErrorMessage(e.getMessage());

            return ReturnResult.error("重置失败: " + e.getMessage());
        }
    }
}
