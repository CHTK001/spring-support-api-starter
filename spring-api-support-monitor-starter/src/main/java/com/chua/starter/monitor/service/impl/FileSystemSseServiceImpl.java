package com.chua.starter.monitor.service.impl;

import com.chua.starter.monitor.service.file.FileSystemSseService;
import com.chua.socket.support.session.SocketSessionTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件系统消息推送服务实现
 * 通过 WebSocket 推送文件系统相关的实时消息
 *
 * @author CH
 * @since 2024/12/13
 */
@Slf4j
@Service
public class FileSystemSseServiceImpl implements FileSystemSseService {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    /**
     * WebSocket 事件类型常量
     */
    public static class EventType {
        public static final String UPLOAD_PROGRESS = "fs:upload_progress";
        public static final String UPLOAD_COMPLETED = "fs:upload_completed";
        public static final String UPLOAD_FAILED = "fs:upload_failed";
        public static final String MERGE_PROGRESS = "fs:merge_progress";
        public static final String MERGE_COMPLETED = "fs:merge_completed";
        public static final String MERGE_FAILED = "fs:merge_failed";
        public static final String SYSTEM_STATUS = "fs:system_status";
        public static final String QUEUE_STATUS = "fs:queue_status";
        public static final String STATISTICS_UPDATE = "fs:statistics_update";
        public static final String FILE_CREATED = "fs:file_created";
        public static final String FILE_DELETED = "fs:file_deleted";
        public static final String FILE_UPDATED = "fs:file_updated";
        public static final String ERROR = "fs:error";
    }

    @Autowired(required = false)
    private SocketSessionTemplate socketSessionTemplate;

    @Override
    public void sendUploadProgress(Integer fileId, Double progress, String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileId", fileId);
        data.put("progress", DECIMAL_FORMAT.format(progress));
        data.put("status", status);
        data.put("timestamp", System.currentTimeMillis());

        broadcast(EventType.UPLOAD_PROGRESS, data);
    }

    @Override
    public void sendMergeProgress(Integer fileId, Double progress, String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileId", fileId);
        data.put("progress", DECIMAL_FORMAT.format(progress));
        data.put("status", status);
        data.put("timestamp", System.currentTimeMillis());

        broadcast(EventType.MERGE_PROGRESS, data);
    }

    @Override
    public void sendFileOperationResult(String operation, Integer fileId, Boolean success, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", operation);
        data.put("fileId", fileId);
        data.put("success", success);
        data.put("message", message);
        data.put("timestamp", System.currentTimeMillis());

        String eventType = "fs:" + operation + (success ? "_completed" : "_failed");
        broadcast(eventType, data);
    }

    @Override
    public void sendSystemStatusUpdate(Map<String, Object> status) {
        Map<String, Object> data = new HashMap<>(status);
        data.put("timestamp", System.currentTimeMillis());
        broadcast(EventType.SYSTEM_STATUS, data);
    }

    @Override
    public void sendQueueStatusUpdate(Map<String, Object> queueStatus) {
        Map<String, Object> data = new HashMap<>(queueStatus);
        data.put("timestamp", System.currentTimeMillis());
        broadcast(EventType.QUEUE_STATUS, data);
    }

    @Override
    public void sendStatisticsUpdate(Map<String, Object> statistics) {
        Map<String, Object> data = new HashMap<>(statistics);
        data.put("timestamp", System.currentTimeMillis());
        broadcast(EventType.STATISTICS_UPDATE, data);
    }

    @Override
    public void sendFileCreated(Map<String, Object> fileInfo) {
        Map<String, Object> data = new HashMap<>(fileInfo);
        data.put("timestamp", System.currentTimeMillis());
        broadcast(EventType.FILE_CREATED, data);
    }

    @Override
    public void sendFileDeleted(Integer fileId, String fileName) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileId", fileId);
        data.put("fileName", fileName);
        data.put("timestamp", System.currentTimeMillis());
        broadcast(EventType.FILE_DELETED, data);
    }

    @Override
    public void sendFileUpdated(Map<String, Object> fileInfo) {
        Map<String, Object> data = new HashMap<>(fileInfo);
        data.put("timestamp", System.currentTimeMillis());
        broadcast(EventType.FILE_UPDATED, data);
    }

    @Override
    public void sendError(String error) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", error);
        data.put("timestamp", System.currentTimeMillis());
        broadcast(EventType.ERROR, data);
    }

    @Override
    public void broadcast(String eventType, Object data) {
        if (socketSessionTemplate == null) {
            log.debug("[FileSystem] WebSocket 未启用，跳过消息推送: eventType={}", eventType);
            return;
        }

        try {
            socketSessionTemplate.broadcastObject(eventType, data);
            log.debug("[FileSystem] 消息推送成功: eventType={}", eventType);
        } catch (Exception e) {
            log.error("[FileSystem] 消息推送失败: eventType={}", eventType, e);
        }
    }
}
