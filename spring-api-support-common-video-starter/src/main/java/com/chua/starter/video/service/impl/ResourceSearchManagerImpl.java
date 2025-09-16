package com.chua.starter.video.service.impl;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
import com.chua.starter.video.provider.ResourceProvider;
import com.chua.starter.video.service.ResourceSearchManager;
import com.chua.starter.video.service.VideoSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    @Override
    public ReturnPageResult<VideoInfoResult> searchFromAllResources(VideoSearch videoSearch) {
        String keyword = videoSearch.getKeyword();
        if (!StringUtils.hasText(keyword)) {
            return ReturnPageResult.error("关键词不能为空");
        }

        List<VideoInfoResult> result = new ArrayList<>();
        List<ResourceProvider> availableProviders = videoSourceService.getAvailableProviders(videoSearch);

        if (availableProviders.isEmpty()) {
            return ReturnPageResult.error("没有可用的资源提供者");
        }

        CountDownLatch latch = new CountDownLatch(availableProviders.size());
        // 按优先级排序后依次检索
        for (ResourceProvider provider : availableProviders) {
            Thread.ofVirtual()
                    .start(() -> {
                        try {
                            ReturnPageResult<VideoInfoResult> searchResult = provider.searchResource(videoSearch);
                            if (searchResult.isOk() && !searchResult.isEmpty()) {
                                result.addAll(searchResult.getData().getData());
                                // 找到结果后可以选择是否继续检索其他资源
                                // 如果需要合并所有资源结果，则不break
                                // 如果只需要第一个有结果的资源，则可以break
                                // break;
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

        return ReturnPageResult.ok(result);
    }


} 