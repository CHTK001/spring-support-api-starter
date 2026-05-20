package com.chua.starter.video.service.impl;

import com.chua.starter.mybatis.utils.ReturnPageResultUtils;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.video.model.VideoInfoResult;
import com.chua.common.support.video.model.VideoSearch;
import com.chua.starter.video.service.ResourceSearchManager;
import com.chua.starter.video.service.VideoOnlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 在线服务
 *
 * @author CH
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VideoOnlineServiceImpl implements VideoOnlineService {

    private final ResourceSearchManager resourceSearchManager;


    @Override
    public ReturnPageResult<VideoInfoResult> findVideoByKeyword(VideoSearch videoSearch) {
        if (!StringUtils.hasText(videoSearch.getKeyword())) {
            return ReturnPageResultUtils.error("视频名称不能为空");
        }

        return resourceSearchManager.searchFromAllResources(videoSearch);
    }


}
