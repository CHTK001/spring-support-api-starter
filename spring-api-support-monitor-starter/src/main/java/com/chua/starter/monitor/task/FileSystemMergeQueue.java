package com.chua.starter.monitor.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文件系统合并任务队列
 * 
 * 负责管理待合并的文件ID队列，减少数据库查询频率
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Component
public class FileSystemMergeQueue {

    // 待合并文件ID队列
    private final BlockingQueue<Integer> mergeQueue = new LinkedBlockingQueue<>();

    // 队列统计信息
    private final AtomicInteger totalEnqueued = new AtomicInteger(0);
    private final AtomicInteger totalProcessed = new AtomicInteger(0);

    /**
     * 添加文件到合并队列
     * 
     * @param fileId 文件ID
     * @return 是否添加成功
     */
    public boolean enqueue(Integer fileId) {
        if (fileId == null) {
            log.warn("尝试添加空的文件ID到合并队列");
            return false;
        }

        // 检查队列中是否已存在该文件ID，避免重复
        if (mergeQueue.contains(fileId)) {
            log.debug("文件ID {} 已在合并队列中，跳过重复添加", fileId);
            return false;
        }

        boolean success = mergeQueue.offer(fileId);
        if (success) {
            totalEnqueued.incrementAndGet();
            log.debug("文件ID {} 已添加到合并队列，当前队列大小: {}", fileId, mergeQueue.size());
        } else {
            log.error("添加文件ID {} 到合并队列失败，队列可能已满", fileId);
        }

        return success;
    }

    /**
     * 从队列中获取下一个待合并的文件ID
     * 
     * @return 文件ID，如果队列为空则返回null
     */
    public Integer dequeue() {
        Integer fileId = mergeQueue.poll();
        if (fileId != null) {
            totalProcessed.incrementAndGet();
            log.debug("从合并队列中取出文件ID: {}, 剩余队列大小: {}", fileId, mergeQueue.size());
        }
        return fileId;
    }

    /**
     * 批量从队列中获取待合并的文件ID
     * 
     * @param maxSize 最大获取数量
     * @return 文件ID数组
     */
    public Integer[] dequeueBatch(int maxSize) {
        if (maxSize <= 0) {
            return new Integer[0];
        }

        int actualSize = Math.min(maxSize, mergeQueue.size());
        if (actualSize == 0) {
            return new Integer[0];
        }

        Integer[] fileIds = new Integer[actualSize];
        for (int i = 0; i < actualSize; i++) {
            fileIds[i] = mergeQueue.poll();
            if (fileIds[i] != null) {
                totalProcessed.incrementAndGet();
            }
        }

        log.debug("批量从合并队列中取出 {} 个文件ID，剩余队列大小: {}", actualSize, mergeQueue.size());
        return fileIds;
    }

    /**
     * 获取队列当前大小
     * 
     * @return 队列大小
     */
    public int size() {
        return mergeQueue.size();
    }

    /**
     * 检查队列是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return mergeQueue.isEmpty();
    }

    /**
     * 查看队列中的前几个元素（不移除）
     *
     * @param maxSize 最大查看数量
     * @return 文件ID列表
     */
    public List<Integer> peek(int maxSize) {
        List<Integer> result = new ArrayList<>();
        if (maxSize <= 0 || mergeQueue.isEmpty()) {
            return result;
        }

        // 将队列转换为数组来查看元素
        Integer[] queueArray = mergeQueue.toArray(new Integer[0]);
        int actualSize = Math.min(maxSize, queueArray.length);

        for (int i = 0; i < actualSize; i++) {
            if (queueArray[i] != null) {
                result.add(queueArray[i]);
            }
        }

        return result;
    }

    /**
     * 清空队列
     */
    public void clear() {
        int clearedCount = mergeQueue.size();
        mergeQueue.clear();
        log.info("合并队列已清空，清除了 {} 个待处理项", clearedCount);
    }

    /**
     * 获取队列统计信息
     * 
     * @return 统计信息字符串
     */
    public String getStatistics() {
        return String.format("合并队列统计 - 当前大小: %d, 总入队: %d, 总处理: %d", mergeQueue.size(), totalEnqueued.get(),
                totalProcessed.get());
    }

    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        totalEnqueued.set(0);
        totalProcessed.set(0);
        log.info("合并队列统计信息已重置");
    }

    /**
     * 检查队列健康状态
     * 
     * @return 健康状态描述
     */
    public String getHealthStatus() {
        int currentSize = mergeQueue.size();
        int capacity = ((LinkedBlockingQueue<Integer>) mergeQueue).remainingCapacity() + currentSize;

        String status;
        if (currentSize == 0) {
            status = "IDLE";
        } else if (currentSize < capacity * 0.5) {
            status = "NORMAL";
        } else if (currentSize < capacity * 0.8) {
            status = "BUSY";
        } else {
            status = "OVERLOADED";
        }

        return String.format("状态: %s, 使用率: %.1f%% (%d/%d)", status, (double) currentSize / capacity * 100, currentSize,
                capacity);
    }
}
