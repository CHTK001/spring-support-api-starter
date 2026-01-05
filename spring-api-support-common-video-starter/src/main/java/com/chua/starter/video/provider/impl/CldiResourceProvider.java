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
 * Cldi 资源搜索提供器
 *
 * <p>参考 pansou 项目的 HTML 结构解析实现，
 * 通过关键字在 Cldi 站点搜索资源并封装为 {@link VideoInfoResult} 分页结果。</p>
 *
 * @author CH
 * @see <a href="https://github.com/fish2018/pansou/blob/main/plugin/cldi/html%E7%BB%93%E6%9E%84%E5%88%86%E6%9E%90.md">pansou cldi 解析说明</a>
 * @since 2025/9/19 11:39
 */
@Spi("cldi")
public class CldiResourceProvider extends AbstractResourceProvider implements DownloadLinkProvider {
    public CldiResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }

    /**
     * 简单本地调试入口
     */
    public static void main(String[] args) {
        VideoSearch videoSearch = new VideoSearch();
        videoSearch.setKeyword("凡人修仙传");
        CldiResourceProvider cldiResourceProvider = new CldiResourceProvider(new VideoSource());
        cldiResourceProvider.searchResource(videoSearch);
    }

    /**
     * 根据关键字和分页条件搜索资源
     *
     * @param videoSearch 搜索条件
     * @return 分页结果
     */
    @Override
    public ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch) {
        String videoSourceUrl = StringUtils.defaultString(videoSource.getVideoSourceUrl(), "https://wvmzbxki.1122132.xyz");
        // https://[域名]/search-[关键词]-[分类]-[排序]-[页码].html
        String encodedKeyword = URLEncoder.encode(StringUtils.defaultString(videoSearch.getKeyword(), ""), StandardCharsets.UTF_8);
        String url = videoSourceUrl + "/search-" + encodedKeyword
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
