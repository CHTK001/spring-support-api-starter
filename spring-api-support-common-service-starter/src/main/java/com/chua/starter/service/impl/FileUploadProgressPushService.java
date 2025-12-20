package com.chua.starter.service.impl;

import com.chua.common.support.json.Json;
import com.chua.socket.support.session.SocketSessionTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传进度Socket推送服务
 * 通过WebSocket实时推送上传进度到前端
 *
 * @author CH
 * @since 2024/12/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(SocketSessionTemplate.class)
public class FileUploadProgressPushService {

    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 上传进度推送主题
     */
    private static final String TOPIC_UPLOAD_PROGRESS = "service:file:upload:progress";

    /**
     * 推送上传进度
     *
     * @param taskId         任务ID
     * @param userId         用户ID（用于定向推送）
     * @param totalChunks    总分片数
     * @param uploadedChunks 已上传分片数
     * @param currentChunk   当前上传的分片序号
     * @param status         状态：uploading, merging, completed, error
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

            // 推送主题: service:file:upload:progress@{userId}
            String topic = TOPIC_UPLOAD_PROGRESS + "@" + (userId != null ? userId : 0);
            socketSessionTemplate.send(topic, Json.toJson(data));
            log.debug("上传进度推送成功: taskId={}, progress={}%", taskId, data.get("progress"));
        } catch (Exception e) {
            log.error("上传进度推送失败: taskId={}", taskId, e);
        }
    }

    /**
     * 推送上传完成
     *
     * @param taskId   任务ID
     * @param userId   用户ID
     * @param fileUrl  文件URL
     * @param fileName 文件名
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
     * 推送上传失败
     *
     * @param taskId  任务ID
     * @param userId  用户ID
     * @param message 错误消息
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
     * 推送合并进度
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param status 状态：merging, completed
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
