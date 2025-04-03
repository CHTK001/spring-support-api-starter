package com.chua.starter.video.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.mapper.VideoDownloadMapper;
import com.chua.starter.video.mapper.VideoInfoMapper;
import com.chua.starter.video.mapper.VideoMarkMapper;
import com.chua.starter.video.service.ResourceSearchManager;
import com.chua.starter.video.service.VideoOnlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 在线服务
 *
 * @author CH
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VideoOnlineServiceImpl implements VideoOnlineService {

    private final VideoInfoMapper videoInfoMapper;
    private final VideoDownloadMapper videoDownloadMapper;
    private final VideoMarkMapper videoMarkMapper;
    private final ResourceSearchManager resourceSearchManager;

    @Override
    public ReturnResult<List<VideoInfo>> findVideoByVideoName(String videoName) {
        if (!StringUtils.hasText(videoName)) {
            return ReturnResult.error("视频名称不能为空");
        }

        return resourceSearchManager.searchFromAllResources(videoName);
    }

    @Override
    public ReturnResult<List<VideoInfo>> searchVideoFromAllResources(String keyword) {
        return resourceSearchManager.searchFromAllResources(keyword);
    }

    @Override
    public ReturnResult<List<VideoInfo>> searchVideoFromResource(String keyword, String providerName) {
        return resourceSearchManager.searchFromResource(keyword, providerName);
    }

    @Override
    public ReturnResult<List<String>> getAvailableResources() {
        List<String> resources = resourceSearchManager.getAvailableResources();
        return ReturnResult.success(resources);
    }
}
