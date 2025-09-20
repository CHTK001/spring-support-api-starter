package com.chua.starter.video.service;

import com.chua.starter.video.entity.VideoSyncConfig;
import com.chua.starter.video.pojo.VideoInfoResult;

/**
 * 视频同步任务服务
 *
 * @author CH
 * @since 2024/6/21
 */
public interface VideoSyncTaskService {

    /**
     * 根据同步配置执行同步任务
     *
     * @param config                         同步配置
     * @param doYouWantToManuallySynchronize 是否手动同步
     * @return 同步的视频数量
     */
    int syncByConfig(VideoSyncConfig config, boolean doYouWantToManuallySynchronize);

    /**
     * 保存或更新视频信息
     *
     * @param videoInfoResult 视频信息结果对象，包含视频的详细信息
     *                        示例：{
     *                        "videoId": "123456",
     *                        "title": "示例视频标题",
     *                        "description": "这是一个示例视频描述",
     *                        "duration": 300,
     *                        "url": "http://example.com/video.mp4"
     *                        }
     * @return 是否保存或更新成功
     * @author CH
     * @since 2024/6/21
     */
    boolean saveOrUpdateForVideo(VideoInfoResult videoInfoResult);
    /**
     * 同步所有启用的配置
     *
     * @return 同步的视频总数
     */
    int syncAllEnabledConfigs();
}