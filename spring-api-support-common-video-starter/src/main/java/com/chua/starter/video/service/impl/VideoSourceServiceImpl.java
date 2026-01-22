package com.chua.starter.video.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.mapper.VideoSourceMapper;
import com.chua.starter.video.pojo.FunctionType;
import com.chua.starter.video.provider.ResourceProvider;
import com.chua.starter.video.service.VideoSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author CH
 * @since 2025/9/15 23:13
 */
@Service
@RequiredArgsConstructor
public class VideoSourceServiceImpl extends ServiceImpl<VideoSourceMapper, VideoSource> implements VideoSourceService {


    @Override
    public List<ResourceProvider> getAvailableProviders(FunctionType functionType) {
        if (null == functionType) {
            return Collections.emptyList();
        }
        List<ResourceProvider> allResourceProviders = new ArrayList<>();
        List<VideoSource> list = list(Wrappers.lambdaQuery(VideoSource.class)
                .eq(VideoSource::getVideoSourceEnable, 1)
                .eq(functionType == FunctionType.SYNC, VideoSource::getVideoSourceSupportSync, 1)
                .eq(functionType == FunctionType.SEARCH, VideoSource::getVideoSourceSupportSearch, 1)
        );
        for (VideoSource videoSource : list) {
            ResourceProvider resourceProvider = ServiceProvider.of(ResourceProvider.class).getNewExtension(videoSource.getVideoSourcePlatform(), videoSource);
            if (null != resourceProvider) {
                allResourceProviders.add(resourceProvider);
            }
        }
        return allResourceProviders;
    }
}
