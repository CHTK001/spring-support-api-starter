package com.chua.starter.monitor.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerFileUploadQueue;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerFileUploadTask;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadQueueStatistics;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadCleanupResult;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadResetResult;

import java.util.List;

/**
 * 服务器文件上传任务队列服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface MonitorSysGenServerFileUploadQueueService {

    /**
     * 将任务加入队列
     *
     * @param task 上传任务
     * @param serverId 服务器ID
     * @return 入队结果
     */
    ReturnResult<MonitorSysGenServerFileUploadQueue> enqueue(MonitorSysGenServerFileUploadTask task, Integer serverId);

    /**
     * 从队列中取出下一个待处理任务
     *
     * @return 队列项
     */
    ReturnResult<MonitorSysGenServerFileUploadQueue> dequeue();

    /**
     * 从队列中取出指定服务器的下一个待处理任务
     *
     * @param serverId 服务器ID
     * @return 队列项
     */
    ReturnResult<MonitorSysGenServerFileUploadQueue> dequeueByServerId(Integer serverId);

    /**
     * 批量从队列中取出待处理任务
     *
     * @param batchSize 批量大小
     * @return 队列项列表
     */
    ReturnResult<List<MonitorSysGenServerFileUploadQueue>> dequeueBatch(Integer batchSize);

    /**
     * 更新队列项状态
     *
     * @param queueId 队列ID
     * @param status  新状态
     * @return 更新结果
     */
    ReturnResult<Boolean> updateQueueStatus(Long queueId, String status);

    /**
     * 标记队列项开始处理
     *
     * @param queueId 队列ID
     * @return 更新结果
     */
    ReturnResult<Boolean> markProcessing(Long queueId);

    /**
     * 标记队列项处理完成
     *
     * @param queueId 队列ID
     * @return 更新结果
     */
    ReturnResult<Boolean> markCompleted(Long queueId);

    /**
     * 标记队列项处理失败
     *
     * @param queueId      队列ID
     * @param errorMessage 错误信息
     * @return 更新结果
     */
    ReturnResult<Boolean> markFailed(Long queueId, String errorMessage);

    /**
     * 重试失败的队列项
     *
     * @param queueId 队列ID
     * @return 重试结果
     */
    ReturnResult<Boolean> retryQueueItem(Long queueId);

    /**
     * 获取队列统计信息
     *
     * @return 统计信息
     */
    ReturnResult<MonitorSysGenServerFileUploadQueueStatistics> getQueueStatistics();

    /**
     * 清理已完成的队列项
     *
     * @param retentionDays 保留天数
     * @return 清理结果
     */
    ReturnResult<MonitorSysGenServerFileUploadCleanupResult> cleanupCompletedItems(Integer retentionDays);

    /**
     * 重置超时的处理中队列项
     *
     * @param timeoutMinutes 超时分钟数
     * @return 重置结果
     */
    ReturnResult<MonitorSysGenServerFileUploadResetResult> resetTimeoutProcessingItems(Integer timeoutMinutes);
}
