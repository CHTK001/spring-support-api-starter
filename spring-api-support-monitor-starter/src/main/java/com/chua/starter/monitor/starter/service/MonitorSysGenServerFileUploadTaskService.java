package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.ServerFileUploadRequest;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerFileUploadTask;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadTaskStatistics;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadCapacity;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadTaskExecutionReport;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadBatchResult;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadTaskQueueStatus;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadQueueOptimizationResult;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadCleanupResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器文件上传任务服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface MonitorSysGenServerFileUploadTaskService {

    /**
     * 创建上传任务
     *
     * @param file    上传文件
     * @param request 上传请求
     * @return 创建结果
     */
    ReturnResult<MonitorSysGenServerFileUploadTask> createUploadTask(MultipartFile file, ServerFileUploadRequest request);

    /**
     * 分页查询上传任务
     *
     * @param page       分页参数
     * @param serverId   服务器ID
     * @param taskName   任务名称
     * @param status     任务状态
     * @param mode       上传模式
     * @param createUser 创建用户
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 分页结果
     */
    ReturnResult<IPage<MonitorSysGenServerFileUploadTask>> getTaskPage(
            Page<MonitorSysGenServerFileUploadTask> page,
            Integer serverId,
            String taskName,
            String status,
            String mode,
            String createUser,
            LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 根据ID查询任务
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    ReturnResult<MonitorSysGenServerFileUploadTask> getTaskById(Long taskId);

    /**
     * 根据服务器ID查询当前活跃任务
     *
     * @param serverId 服务器ID
     * @return 活跃任务
     */
    ReturnResult<MonitorSysGenServerFileUploadTask> getActiveTaskByServerId(Integer serverId);

    /**
     * 启动上传任务
     *
     * @param taskId 任务ID
     * @return 启动结果
     */
    ReturnResult<Boolean> startUploadTask(Long taskId);

    /**
     * 取消上传任务
     *
     * @param taskId 任务ID
     * @return 取消结果
     */
    ReturnResult<Boolean> cancelUploadTask(Long taskId);

    /**
     * 重试上传任务
     *
     * @param taskId 任务ID
     * @return 重试结果
     */
    ReturnResult<Boolean> retryUploadTask(Long taskId);

    /**
     * 删除上传任务
     *
     * @param taskId 任务ID
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteUploadTask(Long taskId);

    /**
     * 更新任务状态
     *
     * @param taskId    任务ID
     * @param status    新状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 更新结果
     */
    ReturnResult<Boolean> updateTaskStatus(Long taskId, String status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据状态查询任务
     *
     * @param status 任务状态
     * @return 任务列表
     */
    ReturnResult<List<MonitorSysGenServerFileUploadTask>> getTasksByStatus(String status);

    /**
     * 查询待处理的定时任务
     *
     * @return 定时任务列表
     */
    ReturnResult<List<MonitorSysGenServerFileUploadTask>> getPendingScheduledTasks();

    /**
     * 查询可重试的失败任务
     *
     * @return 可重试任务列表
     */
    ReturnResult<List<MonitorSysGenServerFileUploadTask>> getRetryableTasks();

    /**
     * 查询超时任务
     *
     * @param timeoutMinutes 超时分钟数
     * @return 超时任务列表
     */
    ReturnResult<List<MonitorSysGenServerFileUploadTask>> getTimeoutTasks(Integer timeoutMinutes);

    /**
     * 获取任务统计信息
     *
     * @param serverId  服务器ID（可选）
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    ReturnResult<MonitorSysGenServerFileUploadTaskStatistics> getTaskStatistics(Integer serverId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取服务器上传能力评估
     *
     * @param serverId 服务器ID
     * @return 上传能力评估
     */
    ReturnResult<MonitorSysGenServerFileUploadCapacity> getServerUploadCapacity(Integer serverId);

    /**
     * 验证服务器是否支持上传
     *
     * @param serverId 服务器ID
     * @return 验证结果
     */
    ReturnResult<Boolean> validateServerForUpload(Integer serverId);

    /**
     * 清理过期任务
     *
     * @param expiredDays 过期天数
     * @return 清理结果
     */
    ReturnResult<MonitorSysGenServerFileUploadCleanupResult> cleanupExpiredTasks(Integer expiredDays);

    /**
     * 获取任务执行报告
     *
     * @param taskId 任务ID
     * @return 执行报告
     */
    ReturnResult<MonitorSysGenServerFileUploadTaskExecutionReport> getTaskExecutionReport(Long taskId);

    /**
     * 批量取消任务
     *
     * @param taskIds 任务ID列表
     * @return 取消结果
     */
    ReturnResult<MonitorSysGenServerFileUploadBatchResult> batchCancelTasks(List<Long> taskIds);

    /**
     * 批量重试任务
     *
     * @param taskIds 任务ID列表
     * @return 重试结果
     */
    ReturnResult<MonitorSysGenServerFileUploadBatchResult> batchRetryTasks(List<Long> taskIds);

    /**
     * 获取任务队列状态
     *
     * @return 队列状态
     */
    ReturnResult<MonitorSysGenServerFileUploadTaskQueueStatus> getTaskQueueStatus();

    /**
     * 优化任务队列
     *
     * @return 优化结果
     */
    ReturnResult<MonitorSysGenServerFileUploadQueueOptimizationResult> optimizeTaskQueue();
}
