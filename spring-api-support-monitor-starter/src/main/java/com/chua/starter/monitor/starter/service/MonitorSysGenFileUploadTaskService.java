package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenFileUploadTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件上传任务管理服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface MonitorSysGenFileUploadTaskService extends IService<MonitorSysGenFileUploadTask> {

    /**
     * 分页查询文件上传任务
     *
     * @param page       分页参数
     * @param taskName   任务名称
     * @param status     任务状态
     * @param mode       上传模式
     * @param type       上传类型
     * @param createUser 创建用户
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 分页结果
     */
    ReturnResult<IPage<MonitorSysGenFileUploadTask>> getTaskPage(Page<MonitorSysGenFileUploadTask> page,
                                                                 String taskName,
                                                                 String status,
                                                                 String mode,
                                                                 String type,
                                                                 String createUser,
                                                                 LocalDateTime startTime,
                                                                 LocalDateTime endTime);

    /**
     * 创建文件上传任务
     *
     * @param task 上传任务
     * @return 创建结果
     */
    ReturnResult<MonitorSysGenFileUploadTask> createUploadTask(MonitorSysGenFileUploadTask task);

    /**
     * 创建批量上传任务
     *
     * @param tasks 上传任务列表
     * @return 创建结果
     */
    ReturnResult<List<MonitorSysGenFileUploadTask>> createBatchUploadTasks(List<MonitorSysGenFileUploadTask> tasks);

    /**
     * 启动上传任务
     *
     * @param taskId 任务ID
     * @return 启动结果
     */
    ReturnResult<Boolean> startUploadTask(Long taskId);

    /**
     * 暂停上传任务
     *
     * @param taskId 任务ID
     * @return 暂停结果
     */
    ReturnResult<Boolean> pauseUploadTask(Long taskId);

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
     * 批量操作任务
     *
     * @param taskIds   任务ID列表
     * @param operation 操作类型(start/pause/cancel/retry)
     * @return 操作结果
     */
    ReturnResult<Map<String, Object>> batchOperateTasks(List<Long> taskIds, String operation);

    /**
     * 根据状态查询任务
     *
     * @param status 任务状态
     * @return 任务列表
     */
    ReturnResult<List<MonitorSysGenFileUploadTask>> getTasksByStatus(String status);

    /**
     * 查询待处理的定时任务
     *
     * @return 待处理的定时任务列表
     */
    ReturnResult<List<MonitorSysGenFileUploadTask>> getPendingScheduledTasks();

    /**
     * 查询可重试的失败任务
     *
     * @return 可重试的失败任务列表
     */
    ReturnResult<List<MonitorSysGenFileUploadTask>> getRetryableTasks();

    /**
     * 查询超时的处理中任务
     *
     * @param timeoutMinutes 超时分钟数
     * @return 超时的处理中任务列表
     */
    ReturnResult<List<MonitorSysGenFileUploadTask>> getTimeoutTasks(Integer timeoutMinutes);

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
     * 增加重试次数
     *
     * @param taskId 任务ID
     * @return 更新结果
     */
    ReturnResult<Boolean> incrementRetryCount(Long taskId);

    /**
     * 获取任务统计信息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getTaskStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据用户查询任务统计
     *
     * @param createUser 创建用户
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 用户任务统计
     */
    ReturnResult<Map<String, Object>> getUserTaskStatistics(String createUser, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询热门文件类型
     *
     * @param limit 限制数量
     * @return 热门文件类型统计
     */
    ReturnResult<List<Map<String, Object>>> getPopularFileTypes(Integer limit);

    /**
     * 查询大文件任务
     *
     * @param minSizeMB 最小文件大小(MB)
     * @param limit     限制数量
     * @return 大文件任务列表
     */
    ReturnResult<List<MonitorSysGenFileUploadTask>> getLargeFileTasks(Integer minSizeMB, Integer limit);

    /**
     * 查询最近的任务
     *
     * @param limit 限制数量
     * @return 最近的任务列表
     */
    ReturnResult<List<MonitorSysGenFileUploadTask>> getRecentTasks(Integer limit);

    /**
     * 根据文件MD5查询任务
     *
     * @param fileMd5 文件MD5
     * @return 任务列表
     */
    ReturnResult<List<MonitorSysGenFileUploadTask>> getTasksByFileMd5(String fileMd5);

    /**
     * 查询队列中的任务数量
     *
     * @return 队列中的任务数量
     */
    ReturnResult<Long> getQueueTaskCount();

    /**
     * 查询今日任务统计
     *
     * @return 今日任务统计
     */
    ReturnResult<Map<String, Object>> getTodayTaskStatistics();

    /**
     * 删除过期任务
     *
     * @param expiredTime 过期时间
     * @return 删除结果
     */
    ReturnResult<Integer> deleteExpiredTasks(LocalDateTime expiredTime);

    /**
     * 验证任务参数
     *
     * @param task 上传任务
     * @return 验证结果
     */
    ReturnResult<Map<String, Object>> validateTask(MonitorSysGenFileUploadTask task);

    /**
     * 复制任务
     *
     * @param taskId 源任务ID
     * @return 复制结果
     */
    ReturnResult<MonitorSysGenFileUploadTask> duplicateTask(Long taskId);

    /**
     * 导出任务列表
     *
     * @param taskIds 任务ID列表
     * @param format  导出格式
     * @return 导出结果
     */
    ReturnResult<String> exportTasks(List<Long> taskIds, String format);

    /**
     * 导入任务列表
     *
     * @param taskData 任务数据
     * @param format   数据格式
     * @return 导入结果
     */
    ReturnResult<List<MonitorSysGenFileUploadTask>> importTasks(String taskData, String format);

    /**
     * 获取任务执行报告
     *
     * @param taskId 任务ID
     * @return 执行报告
     */
    ReturnResult<Map<String, Object>> getTaskExecutionReport(Long taskId);

    /**
     * 清理任务数据
     *
     * @param cleanupDays 清理天数
     * @return 清理结果
     */
    ReturnResult<Map<String, Object>> cleanupTaskData(Integer cleanupDays);

    /**
     * 获取任务队列状态
     *
     * @return 队列状态
     */
    ReturnResult<Map<String, Object>> getTaskQueueStatus();

    /**
     * 优化任务队列
     *
     * @return 优化结果
     */
    ReturnResult<Map<String, Object>> optimizeTaskQueue();
}
