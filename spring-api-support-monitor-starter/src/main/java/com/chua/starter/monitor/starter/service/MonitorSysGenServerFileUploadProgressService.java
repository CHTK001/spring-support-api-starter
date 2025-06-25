package com.chua.starter.monitor.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerFileUploadRecord;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadProgress;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadTaskStatistics;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadTaskExecutionReport;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadCleanupResult;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadTaskQueueStatus;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadQueueOptimizationResult;

import java.util.List;

/**
 * 服务器文件上传进度监控服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface MonitorSysGenServerFileUploadProgressService {

    /**
     * 创建上传记录
     *
     * @param taskId   任务ID
     * @param serverId 服务器ID
     * @return 创建结果
     */
    ReturnResult<MonitorSysGenServerFileUploadRecord> createUploadRecord(Long taskId, Integer serverId);

    /**
     * 更新上传进度
     *
     * @param recordId         记录ID
     * @param progress         进度百分比(0-100)
     * @param transferredBytes 已传输字节数
     * @param speed            传输速度(字节/秒)
     * @return 更新结果
     */
    ReturnResult<Boolean> updateProgress(Long recordId, Integer progress, Long transferredBytes, Long speed);

    /**
     * 标记上传开始
     *
     * @param recordId   记录ID
     * @param totalBytes 总字节数
     * @return 更新结果
     */
    ReturnResult<Boolean> markUploadStarted(Long recordId, Long totalBytes);

    /**
     * 标记上传完成
     *
     * @param recordId 记录ID
     * @return 更新结果
     */
    ReturnResult<Boolean> markUploadCompleted(Long recordId);

    /**
     * 标记上传失败
     *
     * @param recordId     记录ID
     * @param errorCode    错误代码
     * @param errorMessage 错误信息
     * @return 更新结果
     */
    ReturnResult<Boolean> markUploadFailed(Long recordId, String errorCode, String errorMessage);

    /**
     * 标记上传取消
     *
     * @param recordId 记录ID
     * @return 更新结果
     */
    ReturnResult<Boolean> markUploadCancelled(Long recordId);

    /**
     * 获取上传记录
     *
     * @param recordId 记录ID
     * @return 上传记录
     */
    ReturnResult<MonitorSysGenServerFileUploadRecord> getUploadRecord(Long recordId);

    /**
     * 根据任务ID获取上传记录
     *
     * @param taskId 任务ID
     * @return 上传记录
     */
    ReturnResult<MonitorSysGenServerFileUploadRecord> getUploadRecordByTaskId(Long taskId);

    /**
     * 获取任务的所有上传记录
     *
     * @param taskId 任务ID
     * @return 上传记录列表
     */
    ReturnResult<List<MonitorSysGenServerFileUploadRecord>> getUploadRecordsByTaskId(Long taskId);

    /**
     * 获取服务器的上传记录
     *
     * @param serverId 服务器ID
     * @param status   状态（可选）
     * @return 上传记录列表
     */
    ReturnResult<List<MonitorSysGenServerFileUploadRecord>> getUploadRecordsByServerId(Integer serverId, String status);

    /**
     * 获取正在上传的记录
     *
     * @return 正在上传的记录列表
     */
    ReturnResult<List<MonitorSysGenServerFileUploadRecord>> getUploadingRecords();

    /**
     * 获取失败的上传记录
     *
     * @return 失败的记录列表
     */
    ReturnResult<List<MonitorSysGenServerFileUploadRecord>> getFailedRecords();

    /**
     * 获取上传统计信息
     *
     * @param serverId 服务器ID（可选）
     * @return 统计信息
     */
    ReturnResult<MonitorSysGenServerFileUploadTaskStatistics> getUploadStatistics(Integer serverId);

    /**
     * 获取实时上传状态
     *
     * @param taskId 任务ID
     * @return 实时状态
     */
    ReturnResult<MonitorSysGenServerFileUploadProgress> getRealTimeUploadStatus(Long taskId);

    /**
     * 计算上传速度
     *
     * @param recordId 记录ID
     * @return 上传速度统计
     */
    ReturnResult<MonitorSysGenServerFileUploadProgress> calculateUploadSpeed(Long recordId);

    /**
     * 估算剩余时间
     *
     * @param recordId 记录ID
     * @return 剩余时间估算
     */
    ReturnResult<MonitorSysGenServerFileUploadProgress> estimateRemainingTime(Long recordId);

    /**
     * 验证文件完整性
     *
     * @param recordId 记录ID
     * @param remoteMd5 远程文件MD5值
     * @return 验证结果
     */
    ReturnResult<Boolean> verifyFileIntegrity(Long recordId, String remoteMd5);

    /**
     * 重试上传记录
     *
     * @param recordId 记录ID
     * @return 重试结果
     */
    ReturnResult<Boolean> retryUploadRecord(Long recordId);

    /**
     * 取消上传记录
     *
     * @param recordId 记录ID
     * @return 取消结果
     */
    ReturnResult<Boolean> cancelUploadRecord(Long recordId);

    /**
     * 清理过期记录
     *
     * @param expiredDays 过期天数
     * @return 清理结果
     */
    ReturnResult<MonitorSysGenServerFileUploadCleanupResult> cleanupExpiredRecords(Integer expiredDays);

    /**
     * 获取上传性能分析
     *
     * @param serverId 服务器ID（可选）
     * @return 性能分析
     */
    ReturnResult<MonitorSysGenServerFileUploadTaskStatistics> getUploadPerformanceAnalysis(Integer serverId);

    /**
     * 获取上传趋势分析
     *
     * @param days 分析天数
     * @return 趋势分析
     */
    ReturnResult<MonitorSysGenServerFileUploadTaskStatistics> getUploadTrendAnalysis(Integer days);

    /**
     * 监控上传异常
     *
     * @return 异常监控结果
     */
    ReturnResult<MonitorSysGenServerFileUploadTaskStatistics> monitorUploadAnomalies();

    /**
     * 生成上传报告
     *
     * @param taskId 任务ID
     * @return 上传报告
     */
    ReturnResult<MonitorSysGenServerFileUploadTaskExecutionReport> generateUploadReport(Long taskId);

    /**
     * 获取上传队列状态
     *
     * @return 队列状态
     */
    ReturnResult<MonitorSysGenServerFileUploadTaskQueueStatus> getUploadQueueStatus();

    /**
     * 暂停上传
     *
     * @param recordId 记录ID
     * @return 暂停结果
     */
    ReturnResult<Boolean> pauseUpload(Long recordId);

    /**
     * 恢复上传
     *
     * @param recordId 记录ID
     * @return 恢复结果
     */
    ReturnResult<Boolean> resumeUpload(Long recordId);

    /**
     * 获取上传历史
     *
     * @param serverId 服务器ID
     * @param days     历史天数
     * @return 上传历史
     */
    ReturnResult<List<MonitorSysGenServerFileUploadTaskExecutionReport>> getUploadHistory(Integer serverId, Integer days);

    /**
     * 检查上传健康状态
     *
     * @return 健康状态
     */
    ReturnResult<MonitorSysGenServerFileUploadTaskStatistics> checkUploadHealth();

    /**
     * 优化上传性能
     *
     * @return 优化结果
     */
    ReturnResult<MonitorSysGenServerFileUploadQueueOptimizationResult> optimizeUploadPerformance();
}
