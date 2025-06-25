package com.chua.starter.monitor.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadProgress;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadWebSocketSessionInfo;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadWebSocketMessageStatistics;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadWebSocketConfig;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadTaskExecutionReport;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadErrorInfo;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadQueueStatistics;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadCleanupResult;

import java.util.List;

/**
 * 服务器文件上传WebSocket消息推送服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface MonitorSysGenServerFileUploadWebSocketService {

    /**
     * 推送上传进度消息
     *
     * @param taskId   任务ID
     * @param progress 进度信息
     * @return 推送结果
     */
    ReturnResult<Boolean> pushUploadProgress(Long taskId, MonitorSysGenServerFileUploadProgress progress);

    /**
     * 推送上传状态变更消息
     *
     * @param taskId     任务ID
     * @param oldStatus  旧状态
     * @param newStatus  新状态
     * @param statusInfo 状态信息
     * @return 推送结果
     */
    ReturnResult<Boolean> pushUploadStatusChange(Long taskId, String oldStatus, String newStatus, MonitorSysGenServerFileUploadProgress statusInfo);

    /**
     * 推送上传开始消息
     *
     * @param taskId   任务ID
     * @param taskInfo 任务信息
     * @return 推送结果
     */
    ReturnResult<Boolean> pushUploadStarted(Long taskId, MonitorSysGenServerFileUploadTaskExecutionReport taskInfo);

    /**
     * 推送上传完成消息
     *
     * @param taskId     任务ID
     * @param resultInfo 结果信息
     * @return 推送结果
     */
    ReturnResult<Boolean> pushUploadCompleted(Long taskId, MonitorSysGenServerFileUploadTaskExecutionReport resultInfo);

    /**
     * 推送上传失败消息
     *
     * @param taskId    任务ID
     * @param errorInfo 错误信息
     * @return 推送结果
     */
    ReturnResult<Boolean> pushUploadFailed(Long taskId, MonitorSysGenServerFileUploadErrorInfo errorInfo);

    /**
     * 推送上传取消消息
     *
     * @param taskId     任务ID
     * @param cancelInfo 取消信息
     * @return 推送结果
     */
    ReturnResult<Boolean> pushUploadCancelled(Long taskId, MonitorSysGenServerFileUploadTaskExecutionReport cancelInfo);

    /**
     * 推送队列状态消息
     *
     * @param queueStatus 队列状态
     * @return 推送结果
     */
    ReturnResult<Boolean> pushQueueStatus(MonitorSysGenServerFileUploadQueueStatistics queueStatus);

    /**
     * 推送服务器状态消息
     *
     * @param serverId     服务器ID
     * @param serverStatus 服务器状态
     * @return 推送结果
     */
    ReturnResult<Boolean> pushServerStatus(Integer serverId, MonitorSysGenServerFileUploadProgress serverStatus);

    /**
     * 推送系统告警消息
     *
     * @param alertInfo 告警信息
     * @return 推送结果
     */
    ReturnResult<Boolean> pushSystemAlert(MonitorSysGenServerFileUploadErrorInfo alertInfo);

    /**
     * 推送错误通知消息
     *
     * @param errorNotification 错误通知
     * @return 推送结果
     */
    ReturnResult<Boolean> pushErrorNotification(MonitorSysGenServerFileUploadErrorInfo errorNotification);

    /**
     * 推送统计信息消息
     *
     * @param statistics 统计信息
     * @return 推送结果
     */
    ReturnResult<Boolean> pushStatistics(MonitorSysGenServerFileUploadQueueStatistics statistics);

    /**
     * 推送实时监控数据
     *
     * @param monitorData 监控数据
     * @return 推送结果
     */
    ReturnResult<Boolean> pushRealTimeMonitorData(MonitorSysGenServerFileUploadProgress monitorData);

    /**
     * 订阅任务进度
     *
     * @param sessionId 会话ID
     * @param taskId    任务ID
     * @return 订阅结果
     */
    ReturnResult<Boolean> subscribeTaskProgress(String sessionId, Long taskId);

    /**
     * 取消订阅任务进度
     *
     * @param sessionId 会话ID
     * @param taskId    任务ID
     * @return 取消订阅结果
     */
    ReturnResult<Boolean> unsubscribeTaskProgress(String sessionId, Long taskId);

    /**
     * 订阅服务器状态
     *
     * @param sessionId 会话ID
     * @param serverId  服务器ID
     * @return 订阅结果
     */
    ReturnResult<Boolean> subscribeServerStatus(String sessionId, Integer serverId);

    /**
     * 取消订阅服务器状态
     *
     * @param sessionId 会话ID
     * @param serverId  服务器ID
     * @return 取消订阅结果
     */
    ReturnResult<Boolean> unsubscribeServerStatus(String sessionId, Integer serverId);

    /**
     * 订阅系统告警
     *
     * @param sessionId 会话ID
     * @return 订阅结果
     */
    ReturnResult<Boolean> subscribeSystemAlerts(String sessionId);

    /**
     * 取消订阅系统告警
     *
     * @param sessionId 会话ID
     * @return 取消订阅结果
     */
    ReturnResult<Boolean> unsubscribeSystemAlerts(String sessionId);

    /**
     * 获取在线会话列表
     *
     * @return 在线会话列表
     */
    ReturnResult<List<MonitorSysGenServerFileUploadWebSocketSessionInfo>> getOnlineSessions();

    /**
     * 获取会话订阅信息
     *
     * @param sessionId 会话ID
     * @return 订阅信息
     */
    ReturnResult<MonitorSysGenServerFileUploadWebSocketSessionInfo> getSessionSubscriptions(String sessionId);

    /**
     * 清理无效会话
     *
     * @return 清理结果
     */
    ReturnResult<MonitorSysGenServerFileUploadCleanupResult> cleanupInvalidSessions();

    /**
     * 广播系统消息
     *
     * @param message 系统消息
     * @return 广播结果
     */
    ReturnResult<Boolean> broadcastSystemMessage(String message);

    /**
     * 发送私有消息
     *
     * @param sessionId 会话ID
     * @param message   私有消息
     * @return 发送结果
     */
    ReturnResult<Boolean> sendPrivateMessage(String sessionId, String message);

    /**
     * 获取消息发送统计
     *
     * @return 发送统计
     */
    ReturnResult<MonitorSysGenServerFileUploadWebSocketMessageStatistics> getMessageSendStatistics();

    /**
     * 检查WebSocket连接状态
     *
     * @param sessionId 会话ID
     * @return 连接状态
     */
    ReturnResult<Boolean> checkConnectionStatus(String sessionId);

    /**
     * 重连WebSocket
     *
     * @param sessionId 会话ID
     * @return 重连结果
     */
    ReturnResult<Boolean> reconnectWebSocket(String sessionId);

    /**
     * 设置消息推送频率限制
     *
     * @param messageType 消息类型
     * @param rateLimit   频率限制(每秒消息数)
     * @return 设置结果
     */
    ReturnResult<Boolean> setMessageRateLimit(String messageType, Integer rateLimit);

    /**
     * 获取消息推送配置
     *
     * @return 推送配置
     */
    ReturnResult<MonitorSysGenServerFileUploadWebSocketConfig> getMessagePushConfig();

    /**
     * 更新消息推送配置
     *
     * @param config 推送配置
     * @return 更新结果
     */
    ReturnResult<Boolean> updateMessagePushConfig(MonitorSysGenServerFileUploadWebSocketConfig config);

    /**
     * 测试WebSocket连接
     *
     * @param sessionId 会话ID
     * @return 测试结果
     */
    ReturnResult<MonitorSysGenServerFileUploadWebSocketSessionInfo> testWebSocketConnection(String sessionId);

    /**
     * 获取WebSocket性能指标
     *
     * @return 性能指标
     */
    ReturnResult<MonitorSysGenServerFileUploadWebSocketMessageStatistics> getWebSocketPerformanceMetrics();
}
