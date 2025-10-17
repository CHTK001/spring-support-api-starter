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
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoPlayAddress;
import com.chua.starter.video.entity.VideoPlayAddressChannel;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.entity.VideoSyncConfig;
import com.chua.starter.video.mapper.VideoSourceMapper;
import com.chua.starter.video.mapper.VideoSyncConfigMapper;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
import com.chua.starter.video.pojo.VideoSyncProcessConfig;
import com.chua.starter.video.provider.SyncProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static com.chua.starter.video.service.impl.VideoSyncTaskServiceImpl.SYNC_STATUS_ERROR;
import static com.chua.starter.video.service.impl.VideoSyncTaskServiceImpl.SYNC_STATUS_FINISH;

/**
 * @author CH
 * @since 2025/10/12 16:30
 */
@Slf4j
@Spi("yuny")
public class YunYResourceProvider extends AbstractResourceProvider implements SyncProvider {
    static final String URL = "https://www.yuny.tv/videoSearch?key=%s";
    static final String HOME = "https://www.yuny.tv%s";
    /**
     * 封面存储路径
     */
    File file = null;
    @AutoInject
    private VideoSourceMapper videoSourceMapper;
    @AutoInject
    private VideoSyncConfigMapper videoSyncConfigMapper;
    public YunYResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }

    public static void main(String[] args) {
        VideoSearch videoSearch = new VideoSearch();
        videoSearch.setKeyword("毕正明的证明");
        YunYResourceProvider yunYResourceProvider = new YunYResourceProvider(new VideoSource());
        yunYResourceProvider.fetchVideos(new VideoSyncConfig(), (videoInfoResult, videoSyncProcessConfig) -> {
            System.out.println();
        });
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
        String videoSyncConfigStatus = config.getVideoSyncConfigStatus();
        if (SYNC_STATUS_FINISH.equals(videoSyncConfigStatus) ||
                SYNC_STATUS_ERROR.equals(videoSyncConfigStatus)
        ) {
            return;
        }
        String videoSourceId = config.getVideoSourceId();
        if (null == videoSourceId) {
            log.warn("视频源ID为空: {}", config.getVideoSyncConfigName());
            return;
        }
        VideoSource videoSource1 = videoSourceMapper.selectById(videoSourceId);
        if (null == videoSource1 || videoSource1.getVideoSourceEnable() == 0) {
            log.warn("视频源已禁用: {}", config.getVideoSyncConfigName());
            return;
        }

        String videoSyncConfigLastOffset = config.getVideoSyncConfigLastOffset();
        JsonObject jsonObject = Json.getJsonObject(videoSyncConfigLastOffset);
        String[] mvType = createMvType(jsonObject);
        root:
        for (String videoType : mvType) {
            AtomicInteger searchPage = new AtomicInteger(createCount(jsonObject, videoType));
            VideoSearch videoSearch = new VideoSearch();
            videoSearch.setVideoType(videoType.toUpperCase());
            String apiUrl = getApiUrl(videoSearch);

            String mvUrl = apiUrl + "?current=" + searchPage.get();
            HttpResponse httpResponse = HttpClient.get(mvUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .newInvoker()
                    .execute();
            if (httpResponse.code() == 500) {
                config.setVideoSyncConfigStatus(SYNC_STATUS_ERROR);
                config.setVideoSyncConfigMessage(httpResponse.message());
                break;
            }
            JsoupResponse jsoup = httpResponse.toJsoup(
                    JsoupResponse.Mappings.builder()
                            .parentXpath("//div/a[@class='justify-center items-center text-center p-0']")
                            .addMapping(JsoupResponse.MappingsPath.builder().css().path("a").href().field("downloadUrls").build())
                            .build()
            );
            List<VideoInfoResult> videoInfoResult = jsoup.eval(VideoInfoResult.class);
            if (videoInfoResult.isEmpty()) {
                break;
            }
            String logMessage = StringUtils.format("从观影获取视频{}列表，配置: {}, 当前第{}, {}/{}页",
                    videoType,
                    config.getVideoSyncConfigName(),
                    0,
                    searchPage.get(),
                    0);
            log.info(logMessage);


            videoSyncConfigStatus = config.getVideoSyncConfigStatus();
            if (SYNC_STATUS_FINISH.equals(videoSyncConfigStatus) ||
                    SYNC_STATUS_ERROR.equals(videoSyncConfigStatus)
            ) {
                break;
            }

            List<VideoInfoResult> videoInfoResults = new LinkedList<>();
            createVideoInfoResults(videoInfoResults, videoInfoResult);
            searchPage.incrementAndGet();
            JsonObject jsonObject1 = new JsonObject();
            jsonObject1.fluentPut(videoType + "_page", searchPage.get());
            jsonObject1.fluentPut("videoType", videoType);
            config.setVideoSyncConfigLastOffset(jsonObject1.toJSONString());
            config.setVideoSyncConfigLastLog(logMessage);
            videoSyncConfigMapper.updateById(config);
        }
    }

    private void createVideoInfoResults(List<VideoInfoResult> videoInfoResults, List<VideoInfoResult> videoInfoResult) {
        for (VideoInfoResult infoResult : videoInfoResult) {
            registerDetail(infoResult);
        }
        videoInfoResults.addAll(videoInfoResult);
    }


    /**
     * 根据指定的视频类型从JSON对象中获取对应的页码计数，如果不存在则返回默认值1。
     *
     * @param jsonObject 包含视频同步偏移量信息的JSON对象，用于记录不同视频类型的同步进度
     *                   <pre><code>
     *                                                                                                                               JsonObject offset = new JsonObject();
     *                                                                                                                               offset.put("MV", 3);
     *                                                                                                                               int count = provider.createCount(offset, "MV"); // 返回3
     *                                                                                                                               </code></pre>
     * @param videoType  视频类型字符串，例如"MV"、"TV"或"AC"，表示当前正在处理的视频类别
     *                   <pre><code>
     *                                                                                                                               String videoType = "TV";
     *                                                                                                                               int count = provider.createCount(offset, videoType);
     *                                                                                                                               </code></pre>
     * @return 返回对应视频类型的页码计数，若未找到则返回默认值1
     * @author CH
     * @since 2025/9/18 22:15
     */
    private int createCount(JsonObject jsonObject, String videoType) {
        return jsonObject.getIntValue(videoType + "_page", 1);
    }

    /**
     * 根据视频同步配置中的偏移量信息创建视频类型数组
     *
     * @param videoSyncConfigLastOffset 视频同步配置的最后偏移量信息，用于确定当前处理的视频类型及位置
     *                                  <pre>{@code
     *                                                                                                                                                                                                                                        JsonObject offset = new JsonObject();
     *                                                                                                                                                                                                                                        offset.put("videoType", "TV");
     *                                                                                                                                                                                                                                        String[] types = provider.createMvType(offset);
     *                                                                                                                                                                                                                                        }</pre>
     * @return 返回根据当前视频类型确定的后续需要处理的视频类型数组
     * @author CH
     * @since 2025/9/18 14:30
     */
    private String[] createMvType(JsonObject videoSyncConfigLastOffset) {
        String videoType = videoSyncConfigLastOffset.getString("videoType");
        if (StringUtils.isEmpty(videoType)) {
            return videoSource.getVideoSourceTypeForList();
        }

        return switch (videoType.toUpperCase()) {
            case "MV" -> videoSource.getVideoSourceTypeForList();
            case "TV" -> videoSource.getVideoSourceTypeForListAfter("MV");
            case "AC" -> videoSource.getVideoSourceTypeForListAfter("TV");
            default -> new String[0];
        };
    }


    /**
     * 获取API URL
     *
     * @param videoSearch 视频搜索条件
     * @return API URL
     */
    private String getApiUrl(VideoSearch videoSearch) {
        String videoType = videoSearch.getVideoType();
        if ("MV".equalsIgnoreCase(videoType) || "film".equalsIgnoreCase(videoType)) {
            return "https://www.yuny.tv/videoClassify/1";
        }

        if ("TV".equalsIgnoreCase(videoType)) {
            return "https://www.yuny.tv/videoClassify/2";
        }

        if ("AC".equalsIgnoreCase(videoType)) {
            return "https://www.yuny.tv/videoClassify/3";
        }

        if ("SP".equalsIgnoreCase(videoType)) {
            return "https://www.yuny.tv/videoClassify/4";
        }

        if ("VAR".equalsIgnoreCase(videoType)) {
            return "https://www.yuny.tv/videoClassify/5";
        }

        return null;
    }
}
