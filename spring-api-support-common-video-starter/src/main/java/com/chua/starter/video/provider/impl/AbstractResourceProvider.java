package com.chua.starter.video.provider.impl;

import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.provider.ResourceProvider;

/**
 * @author CH
 * @since 2025/9/15 23:21
 */
public abstract class AbstractResourceProvider implements ResourceProvider {

    protected VideoSource videoSource;

    public AbstractResourceProvider(VideoSource videoSource) {
        this.videoSource = videoSource;
    }
}
