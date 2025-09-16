package com.chua.starter.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.pojo.VideoSearch;
import com.chua.starter.video.provider.ResourceProvider;

import java.util.List;

/**
 * @author CH
 * @since 2025/9/15 23:13
 */
public interface VideoSourceService extends IService<VideoSource> {


    /**
     * 获取可用的资源
     *
     * @param videoSearch
     * @return
     */
    List<ResourceProvider> getAvailableProviders(VideoSearch videoSearch);
}
