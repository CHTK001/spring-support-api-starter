package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenFileUploadRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件上传记录管理服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface MonitorSysGenFileUploadRecordService extends IService<MonitorSysGenFileUploadRecord> {

    /**
     * 分页查询文件上传记录
     *
     * @param page      分页参数
     * @param taskId    任务ID
     * @param serverId  服务器ID
     * @param status    上传状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    ReturnResult<IPage<MonitorSysGenFileUploadRecord>> getRecordPage(Page<MonitorSysGenFileUploadRecord> page,
                                                                     Long taskId,
                                                                     Integer serverId,
                                                                     String status,
                                                                     LocalDateTime startTime,
                                                                     LocalDateTime endTime);

    /**
     * 根据任务ID查询上传记录
     *
     * @param taskId 任务ID
     * @return 上传记录列表
     */
    ReturnResult<List<MonitorSysGenFileUploadRecord>> getRecordsByTaskId(Long taskId);

    /**
     * 根据服务器ID查询上传记录
     *
     * @param serverId 服务器ID
     * @param limit    限制数量
     * @return 上传记录列表
     */
    ReturnResult<List<MonitorSysGenFileUploadRecord>> getRecordsByServerId(Integer serverId, Integer limit);

    /**
     * 根据状态查询上传记录
     *
     * @param status 上传状态
     * @return 上传记录列表
     */
    ReturnResult<List<MonitorSysGenFileUploadRecord>> getRecordsByStatus(String status);

    /**
     * 查询正在上传的记录
     *
     * @return 正在上传的记录列表
     */
    ReturnResult<List<MonitorSysGenFileUploadRecord>> getUploadingRecords();

    /**
     * 查询失败的上传记录
     *
     * @param limit 限制数量
     * @return 失败的上传记录列表
     */
    ReturnResult<List<MonitorSysGenFileUploadRecord>> getFailedRecords(Integer limit);

    /**
     * 创建上传记录
     *
     * @param record 上传记录
     * @return 创建结果
     */
    ReturnResult<MonitorSysGenFileUploadRecord> createUploadRecord(MonitorSysGenFileUploadRecord record);

    /**
     * 批量创建上传记录
     *
     * @param records 上传记录列表
     * @return 创建结果
     */
    ReturnResult<List<MonitorSysGenFileUploadRecord>> createBatchUploadRecords(List<MonitorSysGenFileUploadRecord> records);

    /**
     * 更新上传进度
     *
     * @param recordId         记录ID
     * @param progress         进度
     * @param transferredBytes 已传输字节数
     * @param speed            上传速度
     * @return 更新结果
     */
    ReturnResult<Boolean> updateUploadProgress(Long recordId, Integer progress, Long transferredBytes, Long speed);

    /**
     * 更新上传状态
     *
     * @param recordId     记录ID
     * @param status       状态
     * @param errorMessage 错误信息
     * @param errorCode    错误代码
     * @param endTime      结束时间
     * @param durationMs   耗时
     * @return 更新结果
     */
    ReturnResult<Boolean> updateUploadStatus(Long recordId, String status, String errorMessage, String errorCode, 
                                           LocalDateTime endTime, Long durationMs);

    /**
     * 更新验证状态
     *
     * @param recordId           记录ID
     * @param verificationStatus 验证状态
     * @param remoteMd5          远程文件MD5
     * @return 更新结果
     */
    ReturnResult<Boolean> updateVerificationStatus(Long recordId, String verificationStatus, String remoteMd5);

    /**
     * 增加重试次数
     *
     * @param recordId 记录ID
     * @return 更新结果
     */
    ReturnResult<Boolean> incrementRetryCount(Long recordId);

    /**
     * 获取任务的上传统计
     *
     * @param taskId 任务ID
     * @return 上传统计
     */
    ReturnResult<Map<String, Object>> getTaskUploadStatistics(Long taskId);

    /**
     * 获取服务器上传统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 服务器上传统计
     */
    ReturnResult<Map<String, Object>> getServerUploadStatistics(Integer serverId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取上传速度排行
     *
     * @param limit 限制数量
     * @return 上传速度排行
     */
    ReturnResult<List<Map<String, Object>>> getUploadSpeedRanking(Integer limit);

    /**
     * 获取上传时间分布
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 上传时间分布
     */
    ReturnResult<List<Map<String, Object>>> getUploadTimeDistribution(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询长时间上传的记录
     *
     * @param hours 小时数
     * @return 长时间上传的记录列表
     */
    ReturnResult<List<MonitorSysGenFileUploadRecord>> getLongTimeUploadingRecords(Integer hours);

    /**
     * 删除过期记录
     *
     * @param expiredTime 过期时间
     * @return 删除结果
     */
    ReturnResult<Integer> deleteExpiredRecords(LocalDateTime expiredTime);

    /**
     * 取消上传记录
     *
     * @param recordId 记录ID
     * @return 取消结果
     */
    ReturnResult<Boolean> cancelUploadRecord(Long recordId);

    /**
     * 重试上传记录
     *
     * @param recordId 记录ID
     * @return 重试结果
     */
    ReturnResult<Boolean> retryUploadRecord(Long recordId);

    /**
     * 验证文件完整性
     *
     * @param recordId 记录ID
     * @return 验证结果
     */
    ReturnResult<Boolean> verifyFileIntegrity(Long recordId);

    /**
     * 获取上传进度详情
     *
     * @param taskId 任务ID
     * @return 上传进度详情
     */
    ReturnResult<Map<String, Object>> getUploadProgressDetails(Long taskId);

    /**
     * 获取实时上传状态
     *
     * @param recordIds 记录ID列表
     * @return 实时上传状态
     */
    ReturnResult<List<Map<String, Object>>> getRealTimeUploadStatus(List<Long> recordIds);

    /**
     * 生成上传报告
     *
     * @param taskId 任务ID
     * @return 上传报告
     */
    ReturnResult<Map<String, Object>> generateUploadReport(Long taskId);

    /**
     * 清理上传记录
     *
     * @param cleanupDays 清理天数
     * @return 清理结果
     */
    ReturnResult<Map<String, Object>> cleanupUploadRecords(Integer cleanupDays);

    /**
     * 获取上传性能分析
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 性能分析
     */
    ReturnResult<Map<String, Object>> getUploadPerformanceAnalysis(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取服务器上传能力评估
     *
     * @param serverId 服务器ID
     * @return 上传能力评估
     */
    ReturnResult<Map<String, Object>> getServerUploadCapacityAssessment(Integer serverId);
}
