package com.chua.starter.video.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.entity.VideoInfo;

import java.util.List;

/**
 * 资源检索管理服务
 *
 * @author CH
 */
public interface ResourceSearchManager {

    /**
     * 从所有可用资源地址检索视频
     *
     * @param keyword 关键词
     * @return 视频资源列表
     */
    ReturnResult<List<VideoInfo>> searchFromAllResources(String keyword);

    /**
     * 从指定资源地址检索视频
     *
     * @param keyword      关键词
     * @param providerName 资源提供者名称
     * @return 视频资源列表
     */
    ReturnResult<List<VideoInfo>> searchFromResource(String keyword, String providerName);

    /**
     * 获取所有可用资源地址
     *
     * @return 资源地址列表
     */
    List<String> getAvailableResources();
} 