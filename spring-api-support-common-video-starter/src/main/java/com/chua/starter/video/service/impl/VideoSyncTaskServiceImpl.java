package com.chua.starter.video.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.video.entity.*;
import com.chua.starter.video.mapper.VideoInfoMapper;
import com.chua.starter.video.mapper.VideoSyncConfigRecordMapper;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.provider.SyncProvider;
import com.chua.starter.video.service.*;
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

    public static final String SYNC_STATUS_FINISH = "FINISH";
    public static final String SYNC_STATUS_ERROR = "ERROR";
    private static final String SYNC_STATUS_START = "START";
    private static final String SYNC_STATUS_PROGRESS = "PROGRESS";
    private final VideoSyncConfigService videoSyncConfigService;
    private final VideoInfoMapper videoInfoMapper;
    private final VideoDownloadService videoDownloadService;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newVirtualThreadPerTaskExecutor();
    private final VideoMarkService videoMarkService;
    private final VideoSyncMessageService videoSyncMessageService;
    public static final List<String> SYNC = new CopyOnWriteArrayList<>();
    private final VideoSyncConfigRecordMapper videoSyncConfigRecordMapper;

    @Override
    public int syncByConfig(VideoSyncConfig config, boolean doYouWantToManuallySynchronize) {
        if (config == null) {
            return 0;
        }

        if (!Boolean.TRUE.equals(config.getVideoSyncConfigEnabled())) {
            throw new RuntimeException("配置[" + config.getVideoSyncConfigName() + "]未启用");
        }

        if (SYNC.contains(config.getVideoSyncConfigId())) {
            throw new RuntimeException("正在同步配置[" + config.getVideoSyncConfigName() + "]的视频信息");
        }

        Integer videoSyncInterval = config.getVideoSyncInterval();
        if (null == videoSyncInterval) {
            videoSyncInterval = 0;
        }

        if (videoSyncInterval == 0) {
            if (!doYouWantToManuallySynchronize) {
                throw new RuntimeException("请稍后再试");
            }
        } else {
            LocalDateTime videoSyncConfigLastTime = config.getVideoSyncConfigLastAsyncTime();
            if (null != videoSyncConfigLastTime) {
                if (LocalDateTime.now().isBefore(videoSyncConfigLastTime)) {
                    throw new RuntimeException("正在同步配置[" + config.getVideoSyncConfigName() + "]的视频信息");
                }
            }
        }
        String videoSyncConfigStatus = config.getVideoSyncConfigStatus();
        if (SYNC_STATUS_START.equals(videoSyncConfigStatus) || SYNC_STATUS_PROGRESS.equals(videoSyncConfigStatus)) {
            throw new RuntimeException("正在同步配置[" + config.getVideoSyncConfigName() + "]的视频信息");
        }

        EXECUTOR_SERVICE.execute(() -> {
            log.info("开始同步配置[{}]的视频信息", config.getVideoSyncConfigName());
            AtomicInteger syncCount = new AtomicInteger(0);
            SYNC.add(config.getVideoSyncConfigId());

            // 推送同步开始消息
            videoSyncMessageService.sendSyncStartMessage(config.getVideoSyncConfigId(), config.getVideoSyncConfigName());
            
            try {
                config.setVideoSyncConfigStatus(SYNC_STATUS_START);
                videoSyncConfigService.updateConfig(config);
                // 根据配置获取视频列表
                fetchVideosFromSource(config, video -> {
                    // 推送视频处理消息
                    videoSyncMessageService.sendVideoProcessingMessage(
                            config.getVideoSyncConfigId(),
                            config.getVideoSyncConfigName(),
                            video.getVideoName()
                    );
                    config.setVideoSyncConfigStatus(SYNC_STATUS_PROGRESS);
                    if (syncCount.get() % 10 == 0) {
                        VideoSyncConfig newConfig = videoSyncConfigService.getConfigById(config.getVideoSyncConfigId());
                        if (null != newConfig) {
                            config.setVideoSyncConfigStatus(newConfig.getVideoSyncConfigStatus());
                        }
                        videoSyncConfigService.updateConfig(config);
                    }
                    // 设置来源信息
                    video.setVideoPlatform(config.getVideoSourceId());

                    // 保存或更新视频信息
                    boolean saved = saveOrUpdateVideo(video);

                    if (saved) {
                        // 处理下载地址
                        processDownloadUrls(video);
                        processMarker(video);
                        int currentCount = syncCount.incrementAndGet();

                        // 每处理10个视频推送一次进度消息
                        if (currentCount % 10 == 0) {
                            videoSyncMessageService.sendSyncProgressMessage(
                                    config.getVideoSyncConfigId(),
                                    config.getVideoSyncConfigName(),
                                    currentCount,
                                    null // 总数暂时未知
                            );
                        }
                    }
                });

                log.info("配置[{}]同步完成，共同步{}个视频", config.getVideoSyncConfigName(), syncCount.get());

                // 推送同步完成消息
                videoSyncMessageService.sendSyncCompleteMessage(
                        config.getVideoSyncConfigId(),
                        config.getVideoSyncConfigName(),
                        syncCount.get()
                );
                
            } catch (Exception e) {
                log.error("同步配置[{}]视频信息失败: {}", config.getVideoSyncConfigName(), e.getMessage(), e);

                // 推送同步错误消息
                videoSyncMessageService.sendSyncErrorMessage(
                        config.getVideoSyncConfigId(),
                        config.getVideoSyncConfigName(),
                        e.getMessage()
                );
            }
            SYNC.remove(config.getVideoSyncConfigId());
        });
        VideoSyncConfigRecord videoSyncConfigRecord = new VideoSyncConfigRecord();
        videoSyncConfigRecord.setVideoSyncConfigId(config.getVideoSyncConfigId());
        videoSyncConfigRecord.setVideoSyncConfigResult("SUCCESS");
        videoSyncConfigRecord.setVideoSyncConfigErrorMessage("");
        videoSyncConfigRecord.setVideoSyncConfigRecordTime(LocalDateTime.now());

        videoSyncConfigRecordMapper.insert(videoSyncConfigRecord);
        SYNC.remove(config.getVideoSyncConfigId());

        config.setVideoSyncConfigStatus(SYNC_STATUS_FINISH);
        config.setVideoSyncConfigLastAsyncTime(videoSyncConfigRecord.getVideoSyncConfigRecordTime());
        videoSyncConfigService.updateConfig(config);
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
            int count = syncByConfig(config, false);
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
    private void fetchVideosFromSource(VideoSyncConfig config, Consumer<VideoInfoResult> consumer) {
        String source = config.getVideoSourceId();
        if (!StringUtils.hasText(source)) {
            log.warn("配置[{}]未指定同步来源", config.getVideoSyncConfigName());
            return;
        }

        // 通过ServiceProvider获取对应的视频源提供者
        List<SyncProvider> providers = ServiceProvider.of(SyncProvider.class).getNewExtensions(source);
        if (CollectionUtils.isEmpty(providers)) {
            log.warn("未找到同步来源[{}]的提供者", source);
            return;
        }

        // 调用提供者获取视频列表
        for (SyncProvider provider : providers) {
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
    private void processDownloadUrls(VideoInfoResult video) {
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
    private void processMarker(VideoInfoResult video) {
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