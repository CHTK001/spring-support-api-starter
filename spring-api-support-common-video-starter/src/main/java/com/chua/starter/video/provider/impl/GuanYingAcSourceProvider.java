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
public class GuanYingAcSourceProvider extends GuanYingMvSourceProvider {

    public GuanYingAcSourceProvider() {
        mvUrl = "https://www.gyg.la/res/ac";
        mvCoverUrl = "https://s.tutu.pm/img/ac";
        mvDownloadUrl = "https://www.gyg.la/res/downurl/ac/";
        mvDetailUrl = "https://www.gyg.la/ac/";
        mvType = "ac";
    }
}