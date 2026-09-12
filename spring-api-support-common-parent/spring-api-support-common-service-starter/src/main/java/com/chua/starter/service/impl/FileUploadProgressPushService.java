package com.chua.starter.service.impl;

import com.chua.common.support.lang.json.Json;
import com.chua.socket.support.session.SocketSessionTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
   * 文件上传进度套接字推送服务
 * 通过WebSocket实时推送上传进度到前端
 *
 * @since 2024/12/20
 * @author CH
 */
@Service
@RequiredArgsConstructor
@ConditionalOnClass(name = "com.chua.socket.support.session.SocketSessionTemplate")
@ConditionalOnBean(SocketSessionTemplate.class)
public class FileUploadProgressPushService {

    /** 日志 */
    private static final Logger log = LoggerFactory.getLogger(FileUploadProgressPushService.class);

    /** 套接字会话模板 */
    private final SocketSessionTemplate socketSessionTemplate;

    /** Topic_upload_进步 */
    private static final String TOPIC_UPLOAD_PROGRESS = "service:file:upload:progress";

    /**
      * 推送进步
     * @param taskId 任务id
     * @param userId 用户id
     * @param totalChunks totalchunks
     * @param uploadedChunks uploadedchunks
     * @param currentChunk 当前chunk
     * @param status 状态
     */
    public void pushProgress(String taskId, Long userId, int totalChunks, int uploadedChunks,
                            int currentChunk, String status) {
        try {
            Map<String, Object> data = new HashMap<>(10);
            data.put("taskId", taskId);
            data.put("totalChunks", totalChunks);
            data.put("uploadedChunks", uploadedChunks);
            data.put("currentChunk", currentChunk);
            data.put("progress", totalChunks > 0 ? (int) ((uploadedChunks * 100.0) / totalChunks) : 0);
            data.put("status", status);
            data.put("dataId", userId != null ? String.valueOf(userId) : "0");
            data.put("timestamp", System.currentTimeMillis());

            String topic = TOPIC_UPLOAD_PROGRESS + "@" + (userId != null ? userId : 0);
            socketSessionTemplate.send(topic, Json.toJson(data));
            log.debug("上传进度推送成功: taskId={}, progress={}%", taskId, data.get("progress"));
        } catch (Exception e) {
            log.error("上传进度推送失败: taskId={}", taskId, e);
        }
    }

    /**
     * 推送Completed
     *
     * @param taskId 任务id
     * @param userId 用户id
     * @param fileUrl 文件url
     * @param fileName 文件名称
     */
    public void pushCompleted(String taskId, Long userId, String fileUrl, String fileName) {
        try {
            Map<String, Object> data = new HashMap<>(8);
            data.put("taskId", taskId);
            data.put("status", "completed");
            data.put("progress", 100);
            data.put("fileUrl", fileUrl);
            data.put("fileName", fileName);
            data.put("dataId", userId != null ? String.valueOf(userId) : "0");
            data.put("timestamp", System.currentTimeMillis());

            String topic = TOPIC_UPLOAD_PROGRESS + "@" + (userId != null ? userId : 0);
            socketSessionTemplate.send(topic, Json.toJson(data));
            log.info("上传完成推送成功: taskId={}, fileUrl={}", taskId, fileUrl);
        } catch (Exception e) {
            log.error("上传完成推送失败: taskId={}", taskId, e);
        }
    }

    /**
     * 推送记录错误
     *
     * @param taskId 任务id
     * @param userId 用户id
     * @param message 消息
     */
    public void pushError(String taskId, Long userId, String message) {
        try {
            Map<String, Object> data = new HashMap<>(6);
            data.put("taskId", taskId);
            data.put("status", "error");
            data.put("message", message);
            data.put("dataId", userId != null ? String.valueOf(userId) : "0");
            data.put("timestamp", System.currentTimeMillis());

            String topic = TOPIC_UPLOAD_PROGRESS + "@" + (userId != null ? userId : 0);
            socketSessionTemplate.send(topic, Json.toJson(data));
            log.warn("上传失败推送: taskId={}, message={}", taskId, message);
        } catch (Exception e) {
            log.error("上传失败推送失败: taskId={}", taskId, e);
        }
    }

    /**
     * 推送合并状态
     *
     * @param taskId 任务id
     * @param userId 用户id
     * @param status 状态
     */
    public void pushMergeStatus(String taskId, Long userId, String status) {
        try {
            Map<String, Object> data = new HashMap<>(5);
            data.put("taskId", taskId);
            data.put("status", status);
            data.put("dataId", userId != null ? String.valueOf(userId) : "0");
            data.put("timestamp", System.currentTimeMillis());

            String topic = TOPIC_UPLOAD_PROGRESS + "@" + (userId != null ? userId : 0);
            socketSessionTemplate.send(topic, Json.toJson(data));
            log.debug("合并状态推送: taskId={}, status={}", taskId, status);
        } catch (Exception e) {
            log.error("合并状态推送失败: taskId={}", taskId, e);
        }
    }
}