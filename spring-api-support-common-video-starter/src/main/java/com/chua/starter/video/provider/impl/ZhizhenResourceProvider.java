package com.chua.starter.video.provider.impl;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.video.entity.VideoSource;

/**
 * @author CH
 * @since 2025/9/20 18:32
 */
@Spi("Zhizhen")
public class ZhizhenResourceProvider extends WanouResourceProvider {

    public ZhizhenResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }

    @Override
    protected String getUrl() {
        return StringUtils.defaultString(videoSource.getVideoSourceUrl(), "https://xiaomi666.fun/api.php/provide/vod?ac=detail&wd=%s");
    }

    @Override
    public String transferName(String s) {
        return switch (s.toLowerCase()) {
            case "kuake" -> "夸克网盘";
            case "baidu" -> "百度网盘";
            case "uc" -> "UC网盘";
            default -> s;
        };
    }
}
