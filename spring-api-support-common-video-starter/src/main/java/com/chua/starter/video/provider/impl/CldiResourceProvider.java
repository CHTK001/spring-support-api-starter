package com.chua.starter.video.provider.impl;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.http.HttpClient;
import com.chua.common.support.http.HttpResponse;
import com.chua.common.support.http.JsoupResponse;
import com.chua.common.support.lang.code.ListReturnResult;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
import com.chua.starter.video.provider.DownloadLinkProvider;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 参考pansou
 *
 * @author CH
 * @link https://github.com/fish2018/pansou/blob/main/plugin/cldi/html%E7%BB%93%E6%9E%84%E5%88%86%E6%9E%90.md
 * @since 2025/9/19 11:39
 */
@Spi("cldi")
public class CldiResourceProvider extends AbstractResourceProvider implements DownloadLinkProvider {
    public CldiResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }

    public static void main(String[] args) {
        VideoSearch videoSearch = new VideoSearch();
        videoSearch.setKeyword("凡人修仙传");
        CldiResourceProvider cldiResourceProvider = new CldiResourceProvider(new VideoSource());
        cldiResourceProvider.searchResource(videoSearch);
    }

    @Override
    public ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch) {
        String videoSourceUrl = StringUtils.defaultString(videoSource.getVideoSourceUrl(), "https://wvmzbxki.1122132.xyz");
        //https://[域名]/search-[关键词]-[分类]-[排序]-[页码].html
        String url = videoSourceUrl + "/search-" + (URLEncoder.encode(StringUtils.defaultString(videoSearch.getKeyword(), ""), StandardCharsets.UTF_8))
                + "-1"
                + "-2"
                + "-" + videoSearch.getPage() + ".html";

        HttpResponse httpResponse = HttpClient.get(url).execute();
        JsoupResponse jsoupResponse = httpResponse.toJsoup(JsoupResponse.Mappings.builder()
                .parentXpath("//*[@class='ssbox']")
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path(".title h3").css().field("videoTitle")
                        .build())
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path(".slist .lightColor")
                        .css()
                        .isFirst()
                        .field("videoSize")
                        .build())
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path(".sbar a")
                        .href()
                        .css()
                        .field("downloadUrls")
                        .build())
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path(".sbar b")
                        .isLast()
                        .css()
                        .field("videoPopularity")
                        .build())
                .build());

        List<VideoInfoResult> videoInfoResults = jsoupResponse.eval(VideoInfoResult.class);
        return ReturnPageResultUtils.ok(videoInfoResults, videoSearch.getPage(), videoSearch.getPageSize(),
                MapUtils.getInteger(jsoupResponse.eval(JsoupResponse.MappingsPath.builder()
                        .path(".tbox .msg .orange")
                        .isLast()
                        .css()
                        .field("total")
                        .build()), "total", videoSearch.getPageSize())
        );
    }

    @Override
    public ListReturnResult<String> searchDownloadUrls(String keyword) {
        VideoSearch videoSearch = new VideoSearch();
        videoSearch.setKeyword(keyword);
        ReturnPageResult<VideoInfoResult> videoInfoResultReturnPageResult = searchResource(videoSearch);
        return (ListReturnResult<String>) ListReturnResult.ok(videoInfoResultReturnPageResult.getData().getData().stream().map(VideoInfoResult::getDownloadUrls)
                .collect(Collectors.toList()));
    }
}
