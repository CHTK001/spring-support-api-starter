package com.chua.starter.video.service;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;

/**
 * 资源检索管理服务
 *
 * @author CH
 */
public interface ResourceSearchManager {

    /**
     * 从所有可用资源地址检索视频
     *
     * @param videoSearch 关键词
     * @return 视频资源列表
     */
    ReturnPageResult<VideoInfoResult> searchFromAllResources(VideoSearch videoSearch);
}