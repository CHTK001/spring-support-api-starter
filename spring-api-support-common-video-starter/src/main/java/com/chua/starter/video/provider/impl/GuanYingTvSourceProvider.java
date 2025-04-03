package com.chua.starter.video.provider.impl;

import com.chua.common.support.annotations.Spi;
import lombok.extern.slf4j.Slf4j;

/**
 * GuanYing视频源提供者
 *
 * @author CH
 * @since 2024/6/21
 */
@Slf4j
@Spi("guanying")
public class GuanYingTvSourceProvider extends GuanYingMvSourceProvider {

    public GuanYingTvSourceProvider() {
        mvUrl = "https://www.gyg.la/res/tv";
        mvCoverUrl = "https://s.tutu.pm/img/tv";
        mvDownloadUrl = "https://www.gyg.la/res/downurl/tv/";
        mvDetailUrl = "https://www.gyg.la/tv/";
        mvType = "tv";
    }
}