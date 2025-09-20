package com.chua.starter.video.provider.impl;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.http.HttpClient;
import com.chua.common.support.http.HttpResponse;
import com.chua.common.support.http.JsoupResponse;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.RegexUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;

import java.util.List;
import java.util.Map;

/**
 * @author CH
 * @since 2025/9/20 18:32
 */
@Spi("wuji")
public class WuJiResourceProvider extends AbstractResourceProvider {
    public WuJiResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }
    public static void main(String[] args) {
        VideoSearch videoSearch = new VideoSearch();
        videoSearch.setKeyword("凡人修仙传");
        WuJiResourceProvider cldiResourceProvider = new WuJiResourceProvider(new VideoSource());
        cldiResourceProvider.searchResource(videoSearch);
    }
    /**
     * 获取地址
     *
     * @return
     */
    protected String getUrl() {
        return StringUtils.defaultString(videoSource.getVideoSourceUrl(), "https://xcili.net/search?q=%s");
    }

    @Override
    public ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch) {
        String url = getUrl();
        url = String.format(url, videoSearch.getKeyword());
        HttpResponse httpResponse = HttpClient.get(url)
                .connectTimeout(videoSource.getVideoSourceConnectTimeout())
                .userAgent(videoSource.getVideoSourceUserAgent())
                .execute();
        JsoupResponse jsoupResponse = httpResponse.toJsoup(JsoupResponse.Mappings.builder()
                .parentXpath("//*[@class='file-list']//tr")
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path("a")
                        .href()
                        .css()
                        .field("downloadUrls")
                        .build())
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path(".td-size")
                        .css()
                        .field("videoSize")
                        .build())
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path("a")
                        .css()
                        .isFirst()
                        .field("videoTitle")
                        .build())
                .build());

        List<VideoInfoResult> videoInfoResults = jsoupResponse.eval(VideoInfoResult.class);
        Map<String, Object> total = jsoupResponse.eval(JsoupResponse.MappingsPath.builder()
                .path(".container p")
                .isLast()
                .css()
                .field("total")
                .build());
        String string = MapUtils.getString(total, "total");
        Integer firstNumber = RegexUtils.getFirstNumber(string);
        return ReturnPageResultUtils.ok(videoInfoResults, videoSearch.getPage(), videoSearch.getPageSize(),firstNumber
        );
    }
}
