package com.chua.starter.video.service.impl;

import com.chua.common.support.lang.code.PageResult;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
import com.chua.starter.video.provider.ResourceProvider;
import com.chua.starter.video.service.ResourceSearchManager;
import com.chua.starter.video.service.VideoSourceService;
import com.chua.starter.video.service.VideoSyncTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.chua.starter.video.pojo.FunctionType.SEARCH;

/**
 * 资源检索管理服务实现
 *
 * @author CH
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceSearchManagerImpl implements ResourceSearchManager {

    final VideoSourceService videoSourceService;
    final VideoSyncTaskService videoSyncTaskService;

    @Override
    public ReturnPageResult<VideoInfoResult> searchFromAllResources(VideoSearch videoSearch) {
        String keyword = videoSearch.getKeyword();
        if (!StringUtils.hasText(keyword)) {
            return ReturnPageResult.error("关键词不能为空");
        }

        List<VideoInfoResult> result = new ArrayList<>();
        List<ResourceProvider> availableProviders = videoSourceService.getAvailableProviders(SEARCH);

        if (availableProviders.isEmpty()) {
            return ReturnPageResult.error("没有可用的资源提供者");
        }

        CountDownLatch latch = new CountDownLatch(availableProviders.size());
        AtomicLong counter = new AtomicLong(0);
        VideoSearch newVideoSearch = new VideoSearch();
        newVideoSearch.setKeyword(videoSearch.getKeyword());
        newVideoSearch.setPlatform(videoSearch.getPlatform());
        newVideoSearch.setVideoType(videoSearch.getVideoType());
        newVideoSearch.setCategory(videoSearch.getCategory());
        newVideoSearch.setYear(videoSearch.getYear());
        newVideoSearch.setOrder(videoSearch.getOrder());
        newVideoSearch.setPage(videoSearch.getPage());
        newVideoSearch.setPageSize(videoSearch.getPageSize() / availableProviders.size());
        newVideoSearch.setProp(videoSearch.getProp());

        // 按优先级排序后依次检索
        for (ResourceProvider provider : availableProviders) {
            Thread.ofVirtual()
                    .start(() -> {
                        try {
                            ReturnPageResult<VideoInfoResult> searchResult = provider.searchResource(newVideoSearch);
                            if (searchResult.isOk() && !searchResult.isEmpty()) {
                                List<VideoInfoResult> data = searchResult.getData().getData();
                                registerIntoDatabase(data);
                                result.addAll(data);
                                counter.addAndGet(searchResult.getData().getTotal());
                            }
                        } catch (Exception e) {
                            log.error("从资源 [{}] 检索视频时发生错误", videoSearch.getPlatform(), e);
                        } finally {
                            latch.countDown();
                        }
                    });
        }

        boolean await = false;
        try {
            await = latch.await(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!await) {
            log.warn("等待检索结果超时");
        }

        if (result.isEmpty()) {
            return ReturnPageResult.error("未找到相关视频资源");
        }
        ReturnPageResult<VideoInfoResult> returnPageResult = ReturnPageResult.ok(result);
        PageResult<VideoInfoResult> data = returnPageResult.getData();
        data.setTotal(counter.get());

        return returnPageResult;
    }

    /**
     * 注册数据库
     *
     * @param data 数据
     */
    private void registerIntoDatabase(List<VideoInfoResult> data) {
        for (VideoInfoResult datum : data) {
            videoSyncTaskService.saveOrUpdateForVideo(datum);
        }
    }


} 