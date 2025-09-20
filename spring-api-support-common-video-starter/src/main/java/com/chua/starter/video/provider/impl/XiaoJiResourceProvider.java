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
 * 参考pansou
 *
 * @author CH
 * @link https://github.com/fish2018/pansou/blob/main/plugin/xiaoji/html%E7%BB%93%E6%9E%84%E5%88%86%E6%9E%90.md
 * @since 2025/9/19 11:39
 */
@Spi("cldi")
public class XiaoJiResourceProvider extends AbstractResourceProvider implements DownloadLinkProvider {
    public XiaoJiResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }

    public static void main(String[] args) {
        VideoSearch videoSearch = new VideoSearch();
        videoSearch.setKeyword("凡人修仙传");
        XiaoJiResourceProvider cldiResourceProvider = new XiaoJiResourceProvider(new VideoSource());
        cldiResourceProvider.searchResource(videoSearch);
    }

    @Override
    public ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch) {
        String videoSourceUrl = StringUtils.defaultString(videoSource.getVideoSourceUrl(), "https://www.xiaojitv.com/?s=%s").formatted(videoSearch.getKeyword());

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

    @Override
    public ListReturnResult<String> searchDownloadUrls(String keyword) {
        VideoSearch videoSearch = new VideoSearch();
        videoSearch.setKeyword(keyword);
        ReturnPageResult<VideoInfoResult> videoInfoResultReturnPageResult = searchResource(videoSearch);
        return (ListReturnResult<String>) ListReturnResult.ok(videoInfoResultReturnPageResult.getData().getData().stream().map(VideoInfoResult::getDownloadUrls)
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
