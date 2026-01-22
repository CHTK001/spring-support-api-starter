package com.chua.starter.video.provider.impl;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.network.http.HttpClient;
import com.chua.common.support.network.http.HttpResponse;
import com.chua.common.support.network.http.JsoupResponse;
import com.chua.common.support.lang.code.ListReturnResult;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.core.utils.MapUtils;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoDownload;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
import com.chua.starter.video.provider.DownloadLinkProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 小鸡影视资源提供器
 *
 * <p>参考 pansou 对小鸡影视站点的 HTML 结构解析，
 * 将检索结果页和详情页解析为 {@link VideoInfoResult} 及下载列表。</p>
 *
 * @author CH
 * @see <a href="https://github.com/fish2018/pansou/blob/main/plugin/xiaoji/html%E7%BB%93%E6%9E%84%E5%88%86%E6%9E%90.md">pansou xiaoji 解析说明</a>
 * @since 2025/9/19 11:39
 */
@Spi("xiaoji")
public class XiaoJiResourceProvider extends AbstractResourceProvider implements DownloadLinkProvider {
    public XiaoJiResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }

    /**
     * 简单本地调试入口
     */
    public static void main(String[] args) {
        VideoSearch videoSearch = new VideoSearch();
        videoSearch.setKeyword("凡人修仙传");
        XiaoJiResourceProvider cldiResourceProvider = new XiaoJiResourceProvider(new VideoSource());
        cldiResourceProvider.searchResource(videoSearch);
    }

    /**
     * 根据关键字搜索资源列表
     *
     * @param videoSearch 搜索条件
     * @return 分页结果
     */
    @Override
    public ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch) {
        String videoSourceUrl = StringUtils
                .defaultString(videoSource.getVideoSourceUrl(), "https://www.xiaojitv.com/?s=%s")
                .formatted(videoSearch.getKeyword());

        HttpResponse httpResponse = HttpClient.get(videoSourceUrl)
                .userAgent(videoSource.getVideoSourceUserAgent())
                .execute();
        JsoupResponse jsoupResponse = httpResponse.toJsoup(JsoupResponse.Mappings.builder()
                .parentXpath("//*[@class='poster-grid']/article")
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path(".poster-title a")
                        .href()
                        .css()
                        .field("downloadUrls")
                        .build())
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path(".poster-title a")
                        .css()
                        .field("videoTitle")
                        .build())
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path(".poster-link img")
                        .css()
                        .isFirst()
                        .src()
                        .field("videoCover")
                        .build())
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path(".rating-score")
                        .css()
                        .field("videoScore")
                        .build())
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path(".ajaxlistpv")
                        .css()
                        .field("videoViews")
                        .build())
                .addMapping(JsoupResponse.MappingsPath.builder()
                        .path(".poster-category a")
                        .css()
                        .field("videoType")
                        .build())
                .build());

        List<VideoInfoResult> videoInfoResults = jsoupResponse.eval(VideoInfoResult.class);
        for (VideoInfoResult videoInfoResult : videoInfoResults) {
            findDetail(videoInfoResult.getDownloadUrls(), videoInfoResult);
        }
        return ReturnPageResultUtils.ok(videoInfoResults, videoSearch.getPage(), videoSearch.getPageSize(),
                MapUtils.getInteger(jsoupResponse.eval(JsoupResponse.MappingsPath.builder()
                        .path(".tbox .msg .orange")
                        .isLast()
                        .css()
                        .field("total")
                        .build()), "total", videoSearch.getPageSize())
        );
    }

    /**
     * 解析详情页，补充导演、演员、别名、类型等信息
     *
     * @param downloadUrls 详情页地址
     * @param videoInfoResult 待补充的信息对象
     */
    private void findDetail(String downloadUrls, VideoInfoResult videoInfoResult) {
        HttpResponse httpResponse = HttpClient.get(downloadUrls).userAgent(videoSource.getVideoSourceUserAgent()).execute();
        JsoupResponse jsoup = httpResponse.toJsoup(JsoupResponse.Mappings.builder().build());
        videoInfoResult.setVideoDirector(MapUtils.getString(jsoup.eval(JsoupResponse.MappingsPath.builder()
                .css()
                .path(".movie-directors span")
                .field("videoDirector")
                .build()), "videoDirector"));
        videoInfoResult.setVideoDescription(MapUtils.getString(jsoup.eval(JsoupResponse.MappingsPath.builder()
                .css()
                .path(".collapsible-content")
                .field("collapsible-content")
                .build()), "collapsible-content"));
        videoInfoResult.setVideoActor(MapUtils.getString(jsoup.eval(JsoupResponse.MappingsPath.builder()
                .css()
                .path(".movie-cast span")
                .field("videoActor")
                .build()), "videoActor"));
        videoInfoResult.setVideoAliasName(MapUtils.getString(jsoup.eval(JsoupResponse.MappingsPath.builder()
                .css()
                .path(".movie-alias")
                .field("videoAliasName")
                .build()), "videoAliasName").replace("又名：", ""));
        videoInfoResult.setVideoType(MapUtils.getStringSplitter(jsoup.eval(JsoupResponse.MappingsPath.builder()
                .css()
                .path(".movie-genres span")
                .field("videoType")
                .build()), "videoType", "\\s+", ","));

        Document document = jsoup.getDocument();
        String doubanId = document.select(".movie-douban a").text();
        registerDownloadUrl(videoInfoResult, doubanId);
    }

    /**
     * 根据豆瓣 ID 拉取下载资源列表并注册到视频信息中
     *
     * @param videoInfoResult 视频信息
     * @param doubanId        豆瓣 ID
     */
    private void registerDownloadUrl(VideoInfoResult videoInfoResult, String doubanId) {
        HttpResponse httpResponse = HttpClient.post("https://www.xiaojitv.com/wp-admin/admin-ajax.php")
                .formUrlencoded()
                .userAgent(videoSource.getVideoSourceUserAgent())
                .form("action", "douban_get_download_resources")
                .form("douban_id", doubanId)
                .execute();

        List<VideoDownload> videoDownloads = new LinkedList<>();
        DownloadList content = httpResponse.content(DownloadList.class);
        List<DownloadList.DataDTO> data = content.getData();
        for (DownloadList.DataDTO datum : data) {
            registerDownloadUrls(datum, videoDownloads);
        }

        videoInfoResult.setDownloadList(videoDownloads);
    }

    /**
     * 将接口返回的资源列表转换为 {@link VideoDownload} 列表
     */
    private void registerDownloadUrls(DownloadList.DataDTO datum, List<VideoDownload> videoDownloads) {
        List<DownloadList.DataDTO.ResourcesDTO> resources = datum.getResources();
        for (DownloadList.DataDTO.ResourcesDTO resourceItem : resources) {
            String downloadUrl = resourceItem.getDownloadUrl();
            VideoDownload videoDownload = new VideoDownload();
            videoDownload.setVideoDownloadUrl(downloadUrl);
            videoDownload.setVideoDownloadName(resourceItem.getName());
            videoDownload.setVideoDownloadQuality(resourceItem.getClarity().replace("WEB-", ""));
            videoDownload.setVideoDownloadPlatform(resourceItem.getType());
            videoDownload.setVideoDownloadSize(resourceItem.getSize());
            videoDownload.setVideoDownloadShareTime(resourceItem.getTime());

            videoDownloads.add(videoDownload);
        }

    }

    /**
     * 仅返回下载地址列表的轻量查询
     *
     * @param keyword 关键字
     * @return 下载地址列表
     */
    @Override
    public ListReturnResult<String> searchDownloadUrls(String keyword) {
        VideoSearch videoSearch = new VideoSearch();
        videoSearch.setKeyword(keyword);
        ReturnPageResult<VideoInfoResult> videoInfoResultReturnPageResult = searchResource(videoSearch);
        return (ListReturnResult<String>) ListReturnResult.ok(videoInfoResultReturnPageResult.data().getData().stream().map(VideoInfoResult::getDownloadUrls)
                .collect(Collectors.toList()));
    }


    @NoArgsConstructor
    @Data
    static class DownloadList {

        @JsonProperty("success")
        private Boolean success;
        @JsonProperty("data")
        private List<DataDTO> data;

        @NoArgsConstructor
        @Data
        public static class DataDTO {
            @JsonProperty("clarity")
            private String clarity;
            @JsonProperty("resources")
            private List<ResourcesDTO> resources;

            @NoArgsConstructor
            @Data
            public static class ResourcesDTO {
                @JsonProperty("name")
                private String name;
                @JsonProperty("download_url")
                private String downloadUrl;
                @JsonProperty("size")
                private String size;
                @JsonProperty("clarity")
                private String clarity;
                @JsonProperty("time")
                private String time;
                @JsonProperty("type")
                private String type;
                @JsonProperty("pwd")
                private String pwd;
            }
        }
    }
}
