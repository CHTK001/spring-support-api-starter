package com.chua.starter.video.service;

import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.video.entity.VideoSyncMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 视频同步消息推送服务
 * 负责通过WebSocket向客户端推送视频同步的实时状态和进度信息
 *
 * @author CH
 * @version 1.0
 * @since 2024/12/19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoSyncMessageService {

    /**
     * 推送主题常量
     */
    public static final String TOPIC_VIDEO_SYNC = "/topic/video-sync";
    public static final String TOPIC_VIDEO_SYNC_CONFIG = "/topic/video-sync/config/";
    public static final String TOPIC_VIDEO_SYNC_GLOBAL = "/topic/video-sync/global";
    public static final String QUEUE_VIDEO_SYNC_USER = "/queue/video-sync";
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 向所有订阅者推送视频同步消息
     *
     * @param message 同步消息
     */
    public void sendGlobalMessage(VideoSyncMessage message) {
        try {
            log.debug("推送全局视频同步消息: {}", message.getMessage());
            socketSessionTemplate.send(TOPIC_VIDEO_SYNC_GLOBAL, message);
        } catch (Exception e) {
            log.error("推送全局视频同步消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 向指定配置的订阅者推送消息
     *
     * @param configId 配置ID
     * @param message  同步消息
     */
    public void sendConfigMessage(String configId, VideoSyncMessage message) {
        try {
            String destination = TOPIC_VIDEO_SYNC_CONFIG + configId;
            log.debug("推送配置[{}]视频同步消息: {}", configId, message.getMessage());
            socketSessionTemplate.send(destination, message);
        } catch (Exception e) {
            log.error("推送配置[{}]视频同步消息失败: {}", configId, e.getMessage(), e);
        }
    }

    /**
     * 向指定用户推送私有消息
     *
     * @param userId  用户ID
     * @param message 同步消息
     */
    public void sendUserMessage(String userId, VideoSyncMessage message) {
        try {
            log.debug("推送用户[{}]视频同步消息: {}", userId, message.getMessage());
            socketSessionTemplate.send(userId, QUEUE_VIDEO_SYNC_USER, message);
        } catch (Exception e) {
            log.error("推送用户[{}]视频同步消息失败: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 推送同步开始消息
     *
     * @param configId   配置ID
     * @param configName 配置名称
     */
    public void sendSyncStartMessage(String configId, String configName) {
        VideoSyncMessage message = VideoSyncMessage.syncStart(configId, configName);
        sendGlobalMessage(message);
        sendConfigMessage(configId, message);
    }

    /**
     * 推送同步进度消息
     *
     * @param configId    配置ID
     * @param configName  配置名称
     * @param syncedCount 已同步数量
     * @param totalCount  总数量
     */
    public void sendSyncProgressMessage(String configId, String configName,
                                        Integer syncedCount, Integer totalCount) {
        VideoSyncMessage message = VideoSyncMessage.syncProgress(configId, configName, syncedCount, totalCount);
        sendGlobalMessage(message);
        sendConfigMessage(configId, message);
    }

    /**
     * 推送同步完成消息
     *
     * @param configId    配置ID
     * @param configName  配置名称
     * @param syncedCount 已同步数量
     */
    public void sendSyncCompleteMessage(String configId, String configName, Integer syncedCount) {
        VideoSyncMessage message = VideoSyncMessage.syncComplete(configId, configName, syncedCount);
        sendGlobalMessage(message);
        sendConfigMessage(configId, message);
    }

    /**
     * 推送同步错误消息
     *
     * @param configId   配置ID
     * @param configName 配置名称
     * @param error      错误信息
     */
    public void sendSyncErrorMessage(String configId, String configName, String error) {
        VideoSyncMessage message = VideoSyncMessage.syncError(configId, configName, error);
        sendGlobalMessage(message);
        sendConfigMessage(configId, message);
    }

    /**
     * 推送视频处理消息
     *
     * @param configId     配置ID
     * @param configName   配置名称
     * @param currentVideo 当前处理的视频
     */
    public void sendVideoProcessingMessage(String configId, String configName, String currentVideo) {
        VideoSyncMessage message = VideoSyncMessage.videoProcessing(configId, configName, currentVideo);
        sendConfigMessage(configId, message);
    }

    /**
     * 推送系统通知消息
     *
     * @param message 通知内容
     */
    public void sendSystemNotification(String message) {
        VideoSyncMessage notification = VideoSyncMessage.builder()
                .type(VideoSyncMessage.MessageType.SYSTEM_NOTIFICATION)
                .message(message)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        sendGlobalMessage(notification);
    }
}