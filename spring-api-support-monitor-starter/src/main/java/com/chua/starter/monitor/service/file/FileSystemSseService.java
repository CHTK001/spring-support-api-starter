package com.chua.starter.monitor.service.file;

import java.util.Map;

/**
 * 文件系统消息推送服务接口
 * 通过 WebSocket 推送文件系统相关的实时消息
 *
 * @author CH
 * @since 2024/12/13
 */
public interface FileSystemSseService {

    /**
     * 发送文件上传进度消息
     *
     * @param fileId   文件ID
     * @param progress 上传进度
     * @param status   上传状态
     */
    void sendUploadProgress(Integer fileId, Double progress, String status);

    /**
     * 发送文件合并进度消息
     *
     * @param fileId   文件ID
     * @param progress 合并进度
     * @param status   合并状态
     */
    void sendMergeProgress(Integer fileId, Double progress, String status);

    /**
     * 发送文件操作结果消息
     *
     * @param operation 操作类型
     * @param fileId    文件ID
     * @param success   是否成功
     * @param message   消息内容
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
     * @param fileId   文件ID
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
     * 广播消息
     *
     * @param eventType 事件类型
     * @param data      消息数据
     */
    void broadcast(String eventType, Object data);
}
