package com.chua.starter.video.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.video.entity.VideoSyncConfig;

/**
 * 视频同步配置服务
 *
 * @author CH
 * @since 2024/6/21
 */
public interface VideoSyncConfigService {

    /**
     * 获取所有同步配置
     *
     * @return 同步配置列表
     */
    IPage<VideoSyncConfig> getAllConfigs(Query<VideoSyncConfig> query);

    /**
     * 根据ID获取同步配置
     *
     * @param id 配置ID
     * @return 同步配置
     */
    VideoSyncConfig getConfigById(String id);

    /**
     * 添加同步配置
     *
     * @param config 同步配置
     * @return 是否成功
     */
    boolean addConfig(VideoSyncConfig config);

    /**
     * 更新同步配置
     *
     * @param config 同步配置
     * @return 是否成功
     */
    boolean updateConfig(VideoSyncConfig config);

    /**
     * 删除同步配置
     *
     * @param id 配置ID
     * @return 是否成功
     */
    boolean deleteConfig(String id);

    /**
     * 同步配置
     *
     * @param id 配置ID
     * @return 是否成功
     */
    ReturnResult<Boolean> syncConfig(String id);
}