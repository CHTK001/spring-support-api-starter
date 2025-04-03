package com.chua.starter.video.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.video.entity.VideoSyncConfig;
import com.chua.starter.video.mapper.VideoSyncConfigMapper;
import com.chua.starter.video.service.VideoSyncConfigService;
import com.chua.starter.video.service.VideoSyncTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 视频同步配置服务实现
 *
 * @author CH
 * @since 2024/6/21
 */
@Service
@Slf4j
public class VideoSyncConfigServiceImpl extends ServiceImpl<VideoSyncConfigMapper, VideoSyncConfig> implements VideoSyncConfigService {

    @Lazy
    @Resource
    VideoSyncTaskService videoSyncTaskService;

    @Override
    public IPage<VideoSyncConfig> getAllConfigs(Query<VideoSyncConfig> query) {
        return this.page(
                query.createPage(),
                Wrappers.<VideoSyncConfig>lambdaQuery()
                        .eq(VideoSyncConfig::getVideoSyncConfigEnabled, true)
                        .orderByDesc(VideoSyncConfig::getVideoSyncConfigEnabled)
                        .orderByDesc(VideoSyncConfig::getCreateTime)
        );
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
    public ReturnResult<Boolean> syncConfig(String id) {
        VideoSyncConfig videoSyncConfig = getById(id);
        if (null == videoSyncConfig) {
            return ReturnResult.error("同步配置不存在");
        }
        return ReturnResult.preconditioning(videoSyncTaskService.syncByConfig(videoSyncConfig) > 0).toResult();
    }
}