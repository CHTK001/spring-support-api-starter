package com.chua.starter.video.entity;

import com.chua.common.support.core.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频同步消息实体
 * 用于WebSocket实时推送视频同步状态和进度信息
 *
 * @author CH
 * @version 1.0
 * @since 2024/12/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoSyncMessage {

    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 同步配置ID
     */
    private String videoSyncConfigId;

    /**
     * 同步配置名称
     */
    private String videoSyncConfigName;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 同步进度（0-100）
     */
    private Integer progress;

    /**
     * 已同步视频数量
     */
    private Integer syncCount;

    /**
     * 总视频数量
     */
    private Integer totalCount;

    /**
     * 当前处理的视频信息
     */
    private String currentVideo;

    /**
     * 错误信息（如果有）
     */
    private String error;

    /**
     * 消息时间戳
     */
    private LocalDateTime lastSyncTime;

    /**
     * 额外数据
     */
    private Object data;

    /**
     * 创建同步开始消息
     *
     * @param configId   配置ID
     * @param configName 配置名称
     * @return 同步开始消息
     */
    public static VideoSyncMessage syncStart(String configId, String configName) {
        return VideoSyncMessage.builder()
                .type(MessageType.SYNC_START)
                .videoSyncConfigId(configId)
                .videoSyncConfigName(configName)
                .message("开始同步配置[" + configName + "]的视频信息")
                .progress(0)
                .syncCount(0)
                .lastSyncTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建同步进度消息
     *
     * @param configId      配置ID
     * @param configName    配置名称
     * @param syncedCount   已同步数量
     * @param totalCount    总数量
     * @param messageString
     * @return 同步进度消息
     */
    public static VideoSyncMessage syncProgress(String configId, String configName,
                                                Integer syncedCount, Integer totalCount, String messageString) {
        int progress = totalCount != null && totalCount > 0 ? (syncedCount * 100 / totalCount) : 0;
        return VideoSyncMessage.builder()
                .type(MessageType.SYNC_PROGRESS)
                .videoSyncConfigId(configId)
                .videoSyncConfigName(configName)
                .message(StringUtils.defaultString(messageString, "同步进度: " + syncedCount + "/" + totalCount))
                .progress(progress)
                .syncCount(syncedCount)
                .totalCount(totalCount)
                .lastSyncTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建同步完成消息
     *
     * @param configId    配置ID
     * @param configName  配置名称
     * @param syncedCount 已同步数量
     * @return 同步完成消息
     */
    public static VideoSyncMessage syncComplete(String configId, String configName, Integer syncedCount) {
        return VideoSyncMessage.builder()
                .type(MessageType.SYNC_COMPLETE)
                .videoSyncConfigId(configId)
                .videoSyncConfigName(configName)
                .message("配置[" + configName + "]同步完成，共同步" + syncedCount + "个视频")
                .progress(100)
                .syncCount(syncedCount)
                .lastSyncTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建同步错误消息
     *
     * @param configId   配置ID
     * @param configName 配置名称
     * @param error      错误信息
     * @return 同步错误消息
     */
    public static VideoSyncMessage syncError(String configId, String configName, String error) {
        return VideoSyncMessage.builder()
                .type(MessageType.SYNC_ERROR)
                .videoSyncConfigId(configId)
                .videoSyncConfigName(configName)
                .message("同步配置[" + configName + "]失败")
                .error(error)
                .lastSyncTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建视频处理消息
     *
     * @param configId     配置ID
     * @param configName   配置名称
     * @param currentVideo 当前视频
     * @return 视频处理消息
     */
    public static VideoSyncMessage videoProcessing(String configId, String configName, String currentVideo) {
        return VideoSyncMessage.builder()
                .type(MessageType.VIDEO_PROCESSING)
                .videoSyncConfigId(configId)
                .videoSyncConfigName(configName)
                .message("正在处理视频: " + currentVideo)
                .currentVideo(currentVideo)
                .lastSyncTime(LocalDateTime.now())
                .build();
    }

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        /**
         * 同步开始
         */
        SYNC_START,

        /**
         * 同步进行中
         */
        SYNC_PROGRESS,

        /**
         * 同步完成
         */
        SYNC_COMPLETE,

        /**
         * 同步失败
         */
        SYNC_ERROR,

        /**
         * 视频处理中
         */
        VIDEO_PROCESSING,

        /**
         * 视频处理完成
         */
        VIDEO_PROCESSED,

        /**
         * 系统通知
         */
        SYSTEM_NOTIFICATION
    }
}