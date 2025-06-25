package com.chua.starter.monitor.starter.service;

import com.chua.common.support.lang.code.ReturnResult;

import java.util.List;
import java.util.Map;

/**
 * 服务器文件上传错误处理和重试机制服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface MonitorSysGenServerFileUploadErrorHandlerService {

    /**
     * 处理上传错误
     *
     * @param taskId       任务ID
     * @param errorCode    错误代码
     * @param errorMessage 错误信息
     * @param exception    异常对象
     * @return 处理结果
     */
    ReturnResult<Map<String, Object>> handleUploadError(Long taskId, String errorCode, String errorMessage, Throwable exception);

    /**
     * 分析错误类型
     *
     * @param exception 异常对象
     * @return 错误分析结果
     */
    ReturnResult<Map<String, Object>> analyzeError(Throwable exception);

    /**
     * 判断是否可以重试
     *
     * @param taskId    任务ID
     * @param errorCode 错误代码
     * @return 是否可以重试
     */
    ReturnResult<Boolean> canRetry(Long taskId, String errorCode);

    /**
     * 执行重试策略
     *
     * @param taskId 任务ID
     * @return 重试结果
     */
    ReturnResult<Map<String, Object>> executeRetryStrategy(Long taskId);

    /**
     * 计算重试延迟时间
     *
     * @param retryCount 重试次数
     * @param errorCode  错误代码
     * @return 延迟时间(毫秒)
     */
    ReturnResult<Long> calculateRetryDelay(Integer retryCount, String errorCode);

    /**
     * 获取重试配置
     *
     * @param serverId 服务器ID
     * @return 重试配置
     */
    ReturnResult<Map<String, Object>> getRetryConfig(Integer serverId);

    /**
     * 更新重试配置
     *
     * @param serverId     服务器ID
     * @param retryConfig  重试配置
     * @return 更新结果
     */
    ReturnResult<Boolean> updateRetryConfig(Integer serverId, Map<String, Object> retryConfig);

    /**
     * 记录错误日志
     *
     * @param taskId       任务ID
     * @param errorCode    错误代码
     * @param errorMessage 错误信息
     * @param stackTrace   堆栈跟踪
     * @return 记录结果
     */
    ReturnResult<Boolean> logError(Long taskId, String errorCode, String errorMessage, String stackTrace);

    /**
     * 获取错误统计
     *
     * @param serverId 服务器ID（可选）
     * @param days     统计天数
     * @return 错误统计
     */
    ReturnResult<Map<String, Object>> getErrorStatistics(Integer serverId, Integer days);

    /**
     * 获取常见错误列表
     *
     * @param limit 返回数量限制
     * @return 常见错误列表
     */
    ReturnResult<List<Map<String, Object>>> getCommonErrors(Integer limit);

    /**
     * 获取错误趋势分析
     *
     * @param days 分析天数
     * @return 趋势分析
     */
    ReturnResult<Map<String, Object>> getErrorTrendAnalysis(Integer days);

    /**
     * 诊断上传问题
     *
     * @param taskId 任务ID
     * @return 诊断结果
     */
    ReturnResult<Map<String, Object>> diagnoseUploadIssue(Long taskId);

    /**
     * 自动修复常见问题
     *
     * @param taskId 任务ID
     * @return 修复结果
     */
    ReturnResult<Map<String, Object>> autoFixCommonIssues(Long taskId);

    /**
     * 获取错误解决方案
     *
     * @param errorCode 错误代码
     * @return 解决方案
     */
    ReturnResult<Map<String, Object>> getErrorSolution(String errorCode);

    /**
     * 预测上传失败风险
     *
     * @param taskId 任务ID
     * @return 风险评估
     */
    ReturnResult<Map<String, Object>> predictUploadFailureRisk(Long taskId);

    /**
     * 监控系统健康状态
     *
     * @return 健康状态
     */
    ReturnResult<Map<String, Object>> monitorSystemHealth();

    /**
     * 生成错误报告
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 错误报告
     */
    ReturnResult<Map<String, Object>> generateErrorReport(String startTime, String endTime);

    /**
     * 清理错误日志
     *
     * @param retentionDays 保留天数
     * @return 清理结果
     */
    ReturnResult<Map<String, Object>> cleanupErrorLogs(Integer retentionDays);

    /**
     * 设置错误告警
     *
     * @param errorCode   错误代码
     * @param threshold   告警阈值
     * @param timeWindow  时间窗口(分钟)
     * @return 设置结果
     */
    ReturnResult<Boolean> setErrorAlert(String errorCode, Integer threshold, Integer timeWindow);

    /**
     * 获取错误告警配置
     *
     * @return 告警配置列表
     */
    ReturnResult<List<Map<String, Object>>> getErrorAlertConfigs();

    /**
     * 检查错误告警
     *
     * @return 告警检查结果
     */
    ReturnResult<List<Map<String, Object>>> checkErrorAlerts();

    /**
     * 发送错误通知
     *
     * @param errorInfo 错误信息
     * @return 发送结果
     */
    ReturnResult<Boolean> sendErrorNotification(Map<String, Object> errorInfo);

    /**
     * 获取系统恢复建议
     *
     * @return 恢复建议
     */
    ReturnResult<List<Map<String, Object>>> getSystemRecoveryRecommendations();

    /**
     * 执行系统自愈
     *
     * @return 自愈结果
     */
    ReturnResult<Map<String, Object>> executeSelfHealing();

    /**
     * 获取错误模式分析
     *
     * @param days 分析天数
     * @return 模式分析
     */
    ReturnResult<Map<String, Object>> getErrorPatternAnalysis(Integer days);

    /**
     * 优化错误处理策略
     *
     * @return 优化结果
     */
    ReturnResult<Map<String, Object>> optimizeErrorHandlingStrategy();

    /**
     * 测试错误处理机制
     *
     * @param errorType 错误类型
     * @return 测试结果
     */
    ReturnResult<Map<String, Object>> testErrorHandlingMechanism(String errorType);
}
