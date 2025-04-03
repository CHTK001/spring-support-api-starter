package com.chua.starter.video.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.video.entity.VideoDownload;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.entity.VideoMark;
import com.chua.starter.video.entity.VideoSyncConfig;
import com.chua.starter.video.mapper.VideoInfoMapper;
import com.chua.starter.video.provider.VideoSourceProvider;
import com.chua.starter.video.service.VideoDownloadService;
import com.chua.starter.video.service.VideoMarkService;
import com.chua.starter.video.service.VideoSyncConfigService;
import com.chua.starter.video.service.VideoSyncTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 视频同步任务服务实现
 *
 * @author CH
 * @since 2024/6/21
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VideoSyncTaskServiceImpl implements VideoSyncTaskService {

    private final VideoSyncConfigService videoSyncConfigService;
    private final VideoInfoMapper videoInfoMapper;
    private final VideoDownloadService videoDownloadService;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newVirtualThreadPerTaskExecutor();
    private final VideoMarkService videoMarkService;
    private static final List<String> SYNC = new CopyOnWriteArrayList<>();

    @Override
    public int syncByConfig(VideoSyncConfig config) {
        if (config == null || !Boolean.TRUE.equals(config.getVideoSyncConfigEnabled())) {
            return 0;
        }

        if (SYNC.contains(config.getVideoSyncConfigId())) {
            throw new RuntimeException("正在同步配置[" + config.getVideoSyncConfigName() + "]的视频信息");
        }

        EXECUTOR_SERVICE.execute(() -> {
            log.info("开始同步配置[{}]的视频信息", config.getVideoSyncConfigName());
            AtomicInteger syncCount = new AtomicInteger(0);
            SYNC.add(config.getVideoSyncConfigId());
            try {
                // 根据配置获取视频列表
                fetchVideosFromSource(config, video -> {
                    // 设置来源信息
                    video.setVideoPlatform(config.getVideoSyncConfigSource());

                    // 保存或更新视频信息
                    boolean saved = saveOrUpdateVideo(video);

                    if (saved) {
                        // 处理下载地址
                        processDownloadUrls(video);
                        processMarker(video);
                        syncCount.incrementAndGet();
                    }
                });

                log.info("配置[{}]同步完成，共同步{}个视频", config.getVideoSyncConfigName(), syncCount);
            } catch (Exception e) {
                log.error("同步配置[{}]视频信息失败: {}", config.getVideoSyncConfigName(), e.getMessage(), e);
            }
            SYNC.remove(config.getVideoSyncConfigId());
        });
        return 1;
    }


    @Override
    public int syncAllEnabledConfigs() {
        int totalCount = 0;
        Query<VideoSyncConfig> query = new Query<>();
        query.setPageSize(10000);
        List<VideoSyncConfig> configs = videoSyncConfigService.getAllConfigs(query).getRecords();

        log.info("开始同步所有启用的配置，共{}个配置", configs.size());

        for (VideoSyncConfig config : configs) {
            int count = syncByConfig(config);
            totalCount += count;
        }

        log.info("所有配置同步完成，共同步{}个视频", totalCount);

        return totalCount;
    }

    /**
     * 从数据源获取视频列表
     *
     * @param config 同步配置
     * @return 视频列表
     */
    private void fetchVideosFromSource(VideoSyncConfig config, Consumer<VideoInfo> consumer) {
        String source = config.getVideoSyncConfigSource();
        if (!StringUtils.hasText(source)) {
            log.warn("配置[{}]未指定同步来源", config.getVideoSyncConfigName());
            return;
        }

        // 通过ServiceProvider获取对应的视频源提供者
        List<VideoSourceProvider> providers = ServiceProvider.of(VideoSourceProvider.class).getNewExtensions(source);
        if (CollectionUtils.isEmpty(providers)) {
            log.warn("未找到同步来源[{}]的提供者", source);
            return;
        }

        // 调用提供者获取视频列表
        for (VideoSourceProvider provider : providers) {
            provider.fetchVideos(config, consumer);
        }
    }

    /**

    /**
     * 保存或更新视频信息
     *
     * @param video 视频信息
     * @return 是否成功
     */
    private boolean saveOrUpdateVideo(VideoInfo video) {
        try {
            // 检查视频是否已存在
            VideoInfo existingVideo = videoInfoMapper.selectOne(
                    Wrappers.<VideoInfo>lambdaQuery()
                            .eq(VideoInfo::getVideoName, video.getVideoName())
                            .eq(VideoInfo::getVideoActor, video.getVideoActor())
                            .eq(VideoInfo::getVideoCategory, video.getVideoCategory())
                    , false);

            if (existingVideo != null) {
                // 更新视频信息
                video.setUpdateTime(LocalDateTime.now());
                return videoInfoMapper.updateById(video) > 0;
            } else {
                // 新增视频信息
                video.setCreateTime(LocalDateTime.now());
                video.setUpdateTime(LocalDateTime.now());
                return videoInfoMapper.insert(video) > 0;
            }
        } catch (Exception e) {
            log.error("保存或更新视频信息失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 处理下载地址
     *
     * @param video  视频信息
     */
    private void processDownloadUrls(VideoInfo video) {
        try {
            // 获取下载地址
            List<VideoDownload> downloadUrls = video.getDownloadList();

            if (null == downloadUrls || downloadUrls.isEmpty()) {
                log.info("视频[{}]没有可用的下载地址", video.getVideoTitle());
                return;
            }

            // 保存下载信息
            for (VideoDownload download : downloadUrls) {
                download.setVideoId(video.getVideoId());
            }
            videoDownloadService.remove(Wrappers.<VideoDownload>lambdaQuery().eq(VideoDownload::getVideoId, video.getVideoId()));
            videoDownloadService.saveBatch(downloadUrls);
            log.info("视频[{}]的下载地址处理完成，共{}个地址", video.getVideoTitle(), downloadUrls.size());
        } catch (Exception e) {
            log.error("处理视频[{}]下载地址失败: {}", video.getVideoTitle(), e.getMessage(), e);
        }
    }
    /**
     * 处理标记
     *
     * @param video 视频信息
     */
    private void processMarker(VideoInfo video) {
        try {
            // 获取下载地址
            List<VideoMark> videoMarkList = video.getVideoMarkList();

            if (null == videoMarkList || videoMarkList.isEmpty()) {
                log.info("视频[{}]没有可用的标签", video.getVideoTitle());
                return;
            }

            // 保存下载信息
            for (VideoMark videoMark : videoMarkList) {
                videoMark.setVideoId(video.getVideoId());
            }
            videoMarkService.remove(Wrappers.<VideoMark>lambdaQuery().eq(VideoMark::getVideoId, video.getVideoId()));
            videoMarkService.saveBatch(videoMarkList);
            log.info("视频[{}]的标签处理完成，共{}个地址", video.getVideoTitle(), videoMarkList.size());
        } catch (Exception e) {
            log.error("处理视频[{}]标签失败: {}", video.getVideoTitle(), e.getMessage(), e);
        }
    }
}