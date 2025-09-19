package com.chua.starter.video.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.video.entity.*;
import com.chua.starter.video.mapper.VideoInfoMapper;
import com.chua.starter.video.mapper.VideoSourceMapper;
import com.chua.starter.video.mapper.VideoSyncConfigRecordMapper;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSyncProcessConfig;
import com.chua.starter.video.provider.SyncProvider;
import com.chua.starter.video.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static com.chua.common.support.utils.StringUtils.format;

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
    private final VideoSourceMapper videoSourceMapper;
    private final VideoPlayAddressService videoPlayAddressService;
    private final VideoPlayAddressChannelService videoPlayAddressChannelService;

    @Override
    public int syncByConfig(VideoSyncConfig config, boolean doYouWantToManuallySynchronize) {
        if (config == null) {
            return 0;
        }

        Integer videoSyncInterval = config.getVideoSyncInterval();
        if (null == videoSyncInterval) {
            videoSyncInterval = 0;
        }

        if (videoSyncInterval == 0) {
            if (!doYouWantToManuallySynchronize) {
                return 0;
            }
        } else {
            LocalDateTime videoSyncConfigLastTime = config.getVideoSyncConfigLastAsyncTime();
            if (null != videoSyncConfigLastTime) {
                if (LocalDateTime.now().isBefore(videoSyncConfigLastTime)) {
                    throw new RuntimeException("正在同步配置[" + config.getVideoSyncConfigName() + "]的视频信息");
                }
            }
        }

        if (!Boolean.TRUE.equals(config.getVideoSyncConfigEnabled())) {
            throw new RuntimeException("配置[" + config.getVideoSyncConfigName() + "]未启用");
        }

        if (SYNC.contains(config.getVideoSyncConfigId())) {
            throw new RuntimeException("正在同步配置[" + config.getVideoSyncConfigName() + "]的视频信息");
        }

        String videoSyncConfigStatus = config.getVideoSyncConfigStatus();
        if (SYNC_STATUS_START.equals(videoSyncConfigStatus) || SYNC_STATUS_PROGRESS.equals(videoSyncConfigStatus)) {
            throw new RuntimeException("正在同步配置[" + config.getVideoSyncConfigName() + "]的视频信息");
        }
        SYNC.add(config.getVideoSyncConfigId());
        config.setVideoSyncConfigStatus(SYNC_STATUS_START);
        videoSyncConfigService.updateConfig(config);

        EXECUTOR_SERVICE.execute(() -> {
            log.info("开始同步配置[{}]的视频信息", config.getVideoSyncConfigName());
            AtomicInteger syncCount = new AtomicInteger(0);

            // 推送同步开始消息
            videoSyncMessageService.sendSyncStartMessage(config.getVideoSyncConfigId(), config.getVideoSyncConfigName());

            try {
                // 根据配置获取视频列表
                config.setVideoSyncConfigStatus(SYNC_STATUS_START);
                fetchVideosFromSource(config, (video, videoSyncProcessConfig) -> {
                    String videoType = videoSyncProcessConfig.getVideoType();
                    int totalPages = videoSyncProcessConfig.getTotalPages();
                    int page = videoSyncProcessConfig.getPage();
                    int totalInPage = videoSyncProcessConfig.getTotalInPage();
                    int currentPageInPage = videoSyncProcessConfig.getCurrentPageInPage();
                    String message = format("开始同步视频({})【{}】, 进度{}/{}, 当页页内进度{}/{}, 当前同步的视频数量:{}",
                            videoType,
                            com.chua.common.support.utils.StringUtils.defaultString(video.getVideoName(), video.getVideoTitle()),
                            page,
                            totalPages,
                            currentPageInPage,
                            totalInPage,
                            syncCount.get());

                    videoSyncMessageService.sendSyncProgressMessage(
                            config.getVideoSyncConfigId(),
                            config.getVideoSyncConfigName(),
                            syncCount.get(),
                            null,
                            message
                    );
                    config.setVideoSyncConfigStatus(SYNC_STATUS_PROGRESS);
                    if (syncCount.get() % 10 == 0) {
                        VideoSyncConfig newConfig = videoSyncConfigService.getConfigById(config.getVideoSyncConfigId());
                        if (null != newConfig) {
                            config.setVideoSyncConfigStatus(newConfig.getVideoSyncConfigStatus());
                        }
                        config.setVideoSyncConfigLastLog(message);
                        videoSyncConfigService.updateConfig(config);
                    }
                    // 设置来源信息
                    video.setVideoPlatform(config.getVideoSourceId());
                    // 保存或更新视频信息
                    boolean saved = saveOrUpdateVideo(video);
                    syncCount.incrementAndGet();

                    if (saved) {
                        // 处理下载地址
                        processDownloadUrls(video);
                        processMarker(video);
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
    private void fetchVideosFromSource(VideoSyncConfig config, BiConsumer<VideoInfoResult, VideoSyncProcessConfig> consumer) {
        String sourceId = config.getVideoSourceId();
        if (!StringUtils.hasText(sourceId)) {
            log.warn("配置[{}]未指定同步来源", config.getVideoSyncConfigName());
            return;
        }

        VideoSource videoSource = videoSourceMapper.selectById(sourceId);
        if (null == videoSource) {
            log.warn("未找到同步来源[{}]", sourceId);
            return;
        }

        // 通过ServiceProvider获取对应的视频源提供者
        List<SyncProvider> providers = ServiceProvider.of(SyncProvider.class).getNewExtensions(videoSource.getVideoSourcePlatform(), videoSource);
        if (CollectionUtils.isEmpty(providers)) {
            log.warn("未找到同步来源[{}]的提供者", sourceId);
            return;
        }

        // 调用提供者获取视频列表
        for (SyncProvider provider : providers) {
            provider.fetchVideos(config, consumer);
        }
    }

    /**
     * /**
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
     * @param video 视频信息
     */
    private void processDownloadUrls(VideoInfoResult video) {
        try {
            processFileDownload(video, video.getDownloadList());
            processPlayAddress(video, video.getPlayAddressesList());
        } catch (Exception e) {
            log.error("处理视频[{}]下载地址失败: {}", video.getVideoTitle(), e.getMessage(), e);
        }
    }

    private void processPlayAddress(VideoInfoResult video, List<VideoPlayAddress> playAddressesList) {
        try {
            if (null == playAddressesList || playAddressesList.isEmpty()) {
                log.info("视频[{}]没有可用的播放地址", video.getVideoTitle());
                return;
            }
            for (VideoPlayAddress playAddress : playAddressesList) {
                playAddress.setVideoId(video.getVideoId());
            }
            videoPlayAddressService.remove(Wrappers.<VideoPlayAddress>lambdaQuery().eq(VideoPlayAddress::getVideoId, video.getVideoId()));
            videoPlayAddressService.saveBatch(playAddressesList);

            List<VideoPlayAddressChannel> tmp = new ArrayList<>();
            for (VideoPlayAddress playAddress : playAddressesList) {
                List<VideoPlayAddressChannel> videoPlayAddressChannels = playAddress.getVideoPlayAddressChannels();
                if (null == videoPlayAddressChannels) {
                    continue;
                }
                for (VideoPlayAddressChannel videoPlayAddressChannel : videoPlayAddressChannels) {
                    videoPlayAddressChannel.setVideoPlayAddressId(playAddress.getVideoPlayAddressId());
                    tmp.add(videoPlayAddressChannel);
                }
            }

            videoPlayAddressChannelService.remove(Wrappers.<VideoPlayAddressChannel>lambdaQuery()
                    .in(VideoPlayAddressChannel::getVideoPlayAddressId, tmp.stream().map(VideoPlayAddressChannel::getVideoPlayAddressId).toList()));

            videoPlayAddressChannelService.saveBatch(tmp);
        } catch (Exception e) {
            log.error("处理视频[{}]播放地址失败: {}", video.getVideoTitle(), e.getMessage(), e);
        }
    }

    /**
     * 处理文件下载
     *
     * @param downloadUrls 下载列表
     * @param video        视频信息
     */
    private void processFileDownload(VideoInfoResult video, List<VideoDownload> downloadUrls) {
        try {
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