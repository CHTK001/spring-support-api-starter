package com.chua.starter.video.service;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;

/**
 * 在线视频服务
 *
 * @author CH
 */
public interface VideoOnlineService {

    /**
     * 根据视频名称查找视频
     *
     * @param videoSearch 视频名称
     * @return 视频列表
     */
    ReturnPageResult<VideoInfoResult> findVideoByKeyword(VideoSearch videoSearch);
}
