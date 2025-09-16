package com.chua.starter.video.provider;

import com.chua.starter.video.entity.VideoSyncConfig;
import com.chua.starter.video.pojo.VideoInfoResult;

import java.util.function.Consumer;

/**
 * 视频同步提供者接口
 *
 * @author CH
 * @since 2025-09-16
 */
public interface SyncProvider {


    /**
     * 从视频源获取视频列表
     *
     * @param config   同步配置信息
     *                 <pre><code>
     *                             VideoSyncConfig config = new VideoSyncConfig();
     *                             config.setVideoSyncConfigUrl("https://example.com/api/videos");
     *                             config.setVideoSyncConfigKey("your-access-key");
     *                             </code></pre>
     * @param consumer 视频信息消费处理器，用于处理获取到的每个视频信息
     *                 <pre><code>
     *                             Consumer<VideoInfoResult> consumer = videoInfo -> {
     *                                 System.out.println("处理视频: " + videoInfo.getVideoTitle());
     *                             };
     *                             </code></pre>
     * @throws RuntimeException 当同步过程中发生错误时抛出
     */
    void fetchVideos(VideoSyncConfig config, Consumer<VideoInfoResult> consumer);
}
