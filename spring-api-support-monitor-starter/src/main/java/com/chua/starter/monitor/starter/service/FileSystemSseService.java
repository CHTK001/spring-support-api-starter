package com.chua.starter.monitor.starter.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * 文件系统SSE服务接口
 * 提供文件系统相关的Server-Sent Events服务
 *
 * @author CH
 * @since 2024/12/13
 */
public interface FileSystemSseService {

    /**
     * 创建SSE连接
     *
     * @param clientId 客户端ID
     * @return SseEmitter
     */
    SseEmitter createConnection(String clientId);

    /**
     * 订阅事件类型
     *
     * @param clientId 客户端ID
     * @param eventTypes 事件类型列表，多个类型用逗号分隔
     */
    void subscribe(String clientId, String eventTypes);

    /**
     * 取消订阅事件类型
     *
     * @param clientId 客户端ID
     * @param eventTypes 事件类型列表，多个类型用逗号分隔
     */
    void unsubscribe(String clientId, String eventTypes);

    /**
     * 断开连接
     *
     * @param clientId 客户端ID
     */
    void disconnect(String clientId);

    /**
     * 获取连接状态
     *
     * @return 连接状态信息
     */
    Map<String, Object> getConnectionStatus();

    /**
     * 发送测试消息
     *
     * @param clientId 客户端ID，如果为null则广播给所有客户端
     * @param message 消息内容
     */
    void sendTestMessage(String clientId, Map<String, Object> message);

    /**
     * 更新心跳时间
     *
     * @param clientId 客户端ID
     */
    void updateHeartbeat(String clientId);

    /**
     * 发送文件上传进度消息
     *
     * @param fileId 文件ID
     * @param progress 上传进度
     * @param status 上传状态
     */
    void sendUploadProgress(Integer fileId, Double progress, String status);

    /**
     * 发送文件合并进度消息
     *
     * @param fileId 文件ID
     * @param progress 合并进度
     * @param status 合并状态
     */
    void sendMergeProgress(Integer fileId, Double progress, String status);

    /**
     * 发送文件操作结果消息
     *
     * @param operation 操作类型
     * @param fileId 文件ID
     * @param success 是否成功
     * @param message 消息内容
     */
    void sendFileOperationResult(String operation, Integer fileId, Boolean success, String message);

    /**
     * 发送系统状态更新消息
     *
     * @param status 系统状态信息
     */
    void sendSystemStatusUpdate(Map<String, Object> status);

    /**
     * 发送队列状态更新消息
     *
     * @param queueStatus 队列状态信息
     */
    void sendQueueStatusUpdate(Map<String, Object> queueStatus);

    /**
     * 发送统计信息更新消息
     *
     * @param statistics 统计信息
     */
    void sendStatisticsUpdate(Map<String, Object> statistics);

    /**
     * 发送文件创建消息
     *
     * @param fileInfo 文件信息
     */
    void sendFileCreated(Map<String, Object> fileInfo);

    /**
     * 发送文件删除消息
     *
     * @param fileId 文件ID
     * @param fileName 文件名
     */
    void sendFileDeleted(Integer fileId, String fileName);

    /**
     * 发送文件更新消息
     *
     * @param fileInfo 文件信息
     */
    void sendFileUpdated(Map<String, Object> fileInfo);

    /**
     * 发送错误消息
     *
     * @param error 错误信息
     */
    void sendError(String error);

    /**
     * 发送自定义消息
     *
     * @param eventType 事件类型
     * @param data 消息数据
     */
    void sendCustomMessage(String eventType, Object data);

    /**
     * 发送消息给指定客户端
     *
     * @param clientId 客户端ID
     * @param eventType 事件类型
     * @param data 消息数据
     */
    void sendMessageToClient(String clientId, String eventType, Object data);

    /**
     * 广播消息给所有客户端
     *
     * @param eventType 事件类型
     * @param data 消息数据
     */
    void broadcastMessage(String eventType, Object data);

    /**
     * 广播消息给订阅了特定事件类型的客户端
     *
     * @param eventType 事件类型
     * @param data 消息数据
     */
    void broadcastToSubscribers(String eventType, Object data);
}
