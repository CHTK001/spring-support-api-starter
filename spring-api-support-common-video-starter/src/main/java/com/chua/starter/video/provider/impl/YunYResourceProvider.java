package com.chua.starter.video.provider.impl;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.http.HttpClient;
import com.chua.common.support.http.HttpResponse;
import com.chua.common.support.http.JsoupResponse;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonArray;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoPlayAddress;
import com.chua.starter.video.entity.VideoPlayAddressChannel;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.entity.VideoSyncConfig;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
import com.chua.starter.video.pojo.VideoSyncProcessConfig;
import com.chua.starter.video.provider.SyncProvider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author CH
 * @since 2025/10/12 16:30
 */
@Spi("yuny")
public class YunYResourceProvider extends AbstractResourceProvider implements SyncProvider {
    static final String URL = "https://www.yuny.tv/videoSearch?key=%s";
    static final String HOME = "https://www.yuny.tv%s";

    public YunYResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }

    public static void main(String[] args) {
        VideoSearch videoSearch = new VideoSearch();
        videoSearch.setKeyword("毕正明的证明");
        YunYResourceProvider yunYResourceProvider = new YunYResourceProvider(new VideoSource());
        yunYResourceProvider.searchResource(videoSearch);
    }

    @Override
    public ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch) {
        HttpResponse httpResponse = HttpClient.get(URL.formatted(videoSearch.getKeyword()))
                .execute();
        JsoupResponse jsoupResponse = httpResponse.toJsoup(
                JsoupResponse.Mappings.builder()
                        .parentXpath("//*[@class='search_result_list_item']")
                        .addMapping(JsoupResponse.MappingsPath.builder()
                                .path(" .search_result_list_item_content_title").css().field("videoTitle")
                                .build())
                        .addMapping(JsoupResponse.MappingsPath.builder()
                                .path(" .search_result_list_item_content_tag").css().field("tags")
                                .build())
                        .addMapping(JsoupResponse.MappingsPath.builder()
                                .path("a.search_result_list_item")
                                .href()
                                .css()
                                .field("downloadUrls")
                                .build())
                        .build()
        );
        List<VideoInfoResult> videoInfoResults = jsoupResponse.eval(VideoInfoResult.class);
        for (VideoInfoResult videoInfoResult : videoInfoResults) {
            String tags = videoInfoResult.getTags();
            String[] split = tags.split("/");
            if (split[0].contains("剧集")) {
                videoInfoResult.setVideoCategory("TV");
            } else {
                videoInfoResult.setVideoCategory("MV");
            }
            if (split.length == 2) {
                videoInfoResult.setVideoYear(Converter.createInteger(split[1]));
            }
            registerDetail(videoInfoResult);
        }
        return ReturnPageResultUtils.ok(videoInfoResults, videoSearch.getPage(), videoSearch.getPageSize(),
                videoInfoResults.size()
        );
    }

    private void registerDetail(VideoInfoResult videoInfoResult) {
        String downloadUrls = videoInfoResult.getDownloadUrls();
        if (null == downloadUrls) {
            return;
        }
        String detailUrl = String.format(HOME, downloadUrls);
        HttpResponse httpResponse = HttpClient.get(detailUrl).execute();
        String content = httpResponse.content(String.class);
        String beforeSpit = "<script type=\"application/json\" data-nuxt-data=\"nuxt-app\" data-ssr=\"true\" id=\"__NUXT_DATA__\">";
        String beforeContent = content.substring(content.indexOf(beforeSpit) + beforeSpit.length());
        String realContent = beforeContent.substring(0, beforeContent.indexOf("</script>"));
        JsoupResponse jsoup = httpResponse.toJsoup(JsoupResponse.Mappings.builder().build());
        JsonArray jsonArray = Json.getJsonArray(realContent);
        JsonObject jsonObject1 = jsonArray.getJsonObject(5);
        videoInfoResult.setVideoYear(Converter.createInteger(jsonArray.get(jsonObject1.getInteger("years"))));
        videoInfoResult.setVideoLanguage(Converter.convertIfNecessary(jsonArray.get(jsonObject1.getInteger("language")), String.class));
        videoInfoResult.setVideoDistrict(Converter.convertIfNecessary(jsonArray.get(jsonObject1.getInteger("area")), String.class));
        videoInfoResult.setVideoScore(Converter.convertIfNecessary(jsonArray.get(jsonObject1.getInteger("score")), BigDecimal.class));
        videoInfoResult.setVideoDirector(Converter.convertIfNecessary(jsonArray.get(jsonObject1.getInteger("director")), String.class));
        videoInfoResult.setVideoDescription(Converter.convertIfNecessary(jsonArray.get(jsonObject1.getInteger("description")), String.class));
        videoInfoResult.setVideoActor(Converter.convertIfNecessary(jsonArray.get(jsonObject1.getInteger("performer")), String.class));
        videoInfoResult.setVideoCover(Converter.convertIfNecessary(jsonArray.get(jsonObject1.getInteger("cover")), String.class));
        videoInfoResult.setVideoOriginId(Converter.convertIfNecessary(jsonArray.get(jsonObject1.getInteger("id")), String.class));
        //在线资源
        JsonArray onlineVideoResources = jsonArray.getJsonArray(jsonObject1.getInteger("lines"));
        registerVideoResources(videoInfoResult, onlineVideoResources, jsonArray);
    }

    private void registerVideoResources(VideoInfoResult videoInfoResult, JsonArray onlineVideoResources, JsonArray jsonArray) {
        List<VideoPlayAddress> playAddressesList = new ArrayList<>();
        onlineVideoResources.forEach(onlineVideoResource -> {
            JsonObject detail = jsonArray.getJsonObject((Integer) onlineVideoResource);
            registerVideoResources(videoInfoResult, detail, jsonArray, playAddressesList);
        });
        videoInfoResult.setPlayAddressesList(playAddressesList);
    }

    private void registerVideoResources(VideoInfoResult videoInfoResult, JsonObject detail, JsonArray jsonArray, List<VideoPlayAddress> playAddressesList) {
        int lineSourceNameLine = detail.getIntValue("lineSourceName");
        int selectsLine = detail.getIntValue("selects");
        String lineSourceName = Converter.convertIfNecessary(jsonArray.get(lineSourceNameLine), String.class);
        VideoPlayAddress videoPlayAddress = new VideoPlayAddress();
        videoPlayAddress.setVideoPlayAddressUnique(String.valueOf(selectsLine));
        videoPlayAddress.setVideoPlayAddressName(lineSourceName);
        videoPlayAddress.setVideoPlayAddressCode(lineSourceName);
        videoPlayAddress.setVideoPlayAddressChannels(createChannel(selectsLine, jsonArray));

        playAddressesList.add(videoPlayAddress);
    }

    private List<VideoPlayAddressChannel> createChannel(int selectsLine, JsonArray jsonArray) {
        List<VideoPlayAddressChannel> videoPlayAddressChannels = new ArrayList<>();
        JsonArray selects = jsonArray.getJsonArray(selectsLine);
        for (Object select : selects) {
            JsonObject jsonObject = jsonArray.getJsonObject((Integer) select);
            VideoPlayAddressChannel videoPlayAddressChannel = new VideoPlayAddressChannel();
            videoPlayAddressChannel.setVideoPlayAddressChannelName(Converter.convertIfNecessary(jsonArray.get(jsonObject.getInteger("series")), String.class));
            videoPlayAddressChannel.setVideoPlayAddressId(Converter.convertIfNecessary(jsonArray.get(jsonObject.getInteger("id")), Integer.class));
            videoPlayAddressChannel.setVideoPlayAddressUrl(Converter.convertIfNecessary(jsonArray.get(jsonObject.getInteger("resource")), String.class));
            videoPlayAddressChannel.setVideoPlayAddressStartIndex(Converter.convertIfNecessary(jsonArray.get(jsonObject.getInteger("resourceType")), Integer.class));
            videoPlayAddressChannel.setVideoPlayAddressEndIndex(videoPlayAddressChannel.getVideoPlayAddressStartIndex());
            videoPlayAddressChannels.add(videoPlayAddressChannel);
        }

        return videoPlayAddressChannels;

    }

    @Override
    public void fetchVideos(VideoSyncConfig config, BiConsumer<VideoInfoResult, VideoSyncProcessConfig> consumer) {
        //https://www.yuny.tv/videoClassify/1?current=2
    }
}
