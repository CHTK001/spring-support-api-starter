package com.chua.starter.video.provider;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;

/**
 * 资源提供者接口
 *
 * @author CH
 */
public interface ResourceProvider {

    /**
     * 检索视频资源
     *
     * @param videoSearch 关键词
     * @return 视频资源列表
     */
    ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch);


}