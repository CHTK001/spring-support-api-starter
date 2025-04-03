package com.chua.starter.video.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.entity.VideoInfo;

import java.util.List;

/**
 * 在线视频服务
 *
 * @author CH
 */
public interface VideoOnlineService {

    /**
     * 根据视频名称查找视频
     *
     * @param videoName 视频名称
     * @return 视频列表
     */
    ReturnResult<List<VideoInfo>> findVideoByVideoName(String videoName);

    /**
     * 从所有资源检索视频
     *
     * @param keyword 关键词
     * @return 视频列表
     */
    ReturnResult<List<VideoInfo>> searchVideoFromAllResources(String keyword);

    /**
     * 从指定资源检索视频
     *
     * @param keyword      关键词
     * @param providerName 资源提供者名称
     * @return 视频列表
     */
    ReturnResult<List<VideoInfo>> searchVideoFromResource(String keyword, String providerName);

    /**
     * 获取所有可用资源
     *
     * @return 资源名称列表
     */
    ReturnResult<List<String>> getAvailableResources();
}
