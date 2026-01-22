package com.chua.starter.video.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.IdUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.entity.VideoSyncConfig;
import com.chua.starter.video.mapper.VideoSyncConfigMapper;
import com.chua.starter.video.mapper.VideoSyncConfigRecordMapper;
import com.chua.starter.video.service.VideoInfoService;
import com.chua.starter.video.service.VideoSyncConfigService;
import com.chua.starter.video.service.VideoSyncTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.chua.starter.video.service.impl.VideoSyncTaskServiceImpl.SYNC;
import static com.chua.starter.video.service.impl.VideoSyncTaskServiceImpl.SYNC_STATUS_FINISH;

/**
 * 视频同步配置服务实现
 *
 * @author CH
 * @since 2024/6/21
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VideoSyncConfigServiceImpl extends ServiceImpl<VideoSyncConfigMapper, VideoSyncConfig> implements VideoSyncConfigService {

    @Lazy
    @Autowired
    VideoSyncTaskService videoSyncTaskService;
    @Lazy
    @Autowired
    VideoSyncConfigRecordMapper videoSyncConfigRecordMapper;

    final VideoInfoService videoInfoService;

    @Override
    public IPage<VideoSyncConfig> getAllConfigs(Query<VideoSyncConfig> query) {
        Page<VideoSyncConfig> page = this.page(
                query.createPage(),
                Wrappers.<VideoSyncConfig>lambdaQuery()
                        .orderByDesc(VideoSyncConfig::getVideoSyncConfigEnabled)
                        .orderByDesc(VideoSyncConfig::getCreateTime)
        );

        List<VideoSyncConfig> records = page.getRecords();
        if (records == null || records.isEmpty()) {
            return page;
        }

        Set<String> collect = records.stream().map(VideoSyncConfig::getVideoSyncConfigId).collect(Collectors.toSet());
        Map<String, Integer> count = videoSyncConfigRecordMapper.countByConfigId(collect);
        if(null == count) {
            return page;
        }
        records.forEach(record -> {
            record.setSyncCount(count.getOrDefault(record.getVideoSyncConfigId(), 0));
            record.setSyncVideoCount(videoInfoService.count(Wrappers.lambdaQuery(VideoInfo.class).eq(VideoInfo::getVideoPlatform, record.getVideoSourceId())));
        });

        return page;
    }

    @Override
    public VideoSyncConfig getConfigById(String id) {
        return this.getById(id);
    }

    @Override
    public boolean addConfig(VideoSyncConfig config) {
        if (config == null) {
            return false;
        }

        config.setVideoSyncConfigId(String.valueOf(IdUtils.createSnowflakeId()));
        // 设置默认值
        if (config.getVideoSyncConfigEnabled() == null) {
            config.setVideoSyncConfigEnabled(true);
        }

        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());

        return this.save(config);
    }

    @Override
    public boolean updateConfig(VideoSyncConfig config) {
        if (config == null || config.getVideoSyncConfigId() == null) {
            return false;
        }

        config.setUpdateTime(LocalDateTime.now());
        return this.updateById(config);
    }

    @Override
    public boolean deleteConfig(String id) {
        if (id == null) {
            return false;
        }

        return this.removeById(id);
    }

    @Override
    public ReturnResult<Boolean> syncConfig(String id, boolean doYouWantToManuallySynchronize) {
        VideoSyncConfig videoSyncConfig = getById(id);
        if (null == videoSyncConfig) {
            return ReturnResult.error("同步配置不存在");
        }
        return ReturnResult.guess(videoSyncTaskService.syncByConfig(videoSyncConfig, doYouWantToManuallySynchronize) > 0);
    }

    @Override
    public ReturnResult<Boolean> toggleStatus(String id) {
        VideoSyncConfig videoSyncConfig = getById(id);
        if (null == videoSyncConfig) {
            return ReturnResult.error("同步配置不存在");
        }
        Boolean videoSyncConfigEnabled = videoSyncConfig.getVideoSyncConfigEnabled();
        if (null == videoSyncConfigEnabled) {
            videoSyncConfigEnabled = true;
        }
        videoSyncConfig.setVideoSyncConfigEnabled(!videoSyncConfigEnabled);
        return ReturnResult.of(updateById(videoSyncConfig));
    }

    @Override
    public ReturnResult<String> getSyncStatus(String id) {
        VideoSyncConfig videoSyncConfig = getById(id);
        if (null == videoSyncConfig) {
            return ReturnResult.error("同步配置不存在");
        }
        return ReturnResult.success(videoSyncConfig.getVideoSyncConfigStatus());
    }

    @Override
    public ReturnResult<Boolean> stopSync(String id, boolean doYouWantToManuallySynchronize) {
        VideoSyncConfig videoSyncConfig = getById(id);
        if (null == videoSyncConfig) {
            return ReturnResult.error("同步配置不存在");
        }
        videoSyncConfig.setVideoSyncConfigStatus(SYNC_STATUS_FINISH);
        SYNC.remove(videoSyncConfig.getVideoSyncConfigId());
        return ReturnResult.of(updateById(videoSyncConfig));
    }
}
