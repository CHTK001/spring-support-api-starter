package com.chua.starter.video.provider.impl;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.base.converter.Converter;
import com.chua.common.support.network.http.HttpClient;
import com.chua.common.support.network.http.HttpResponse;
import com.chua.common.support.network.http.JsoupResponse;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.text.json.JsonArray;
import com.chua.common.support.text.json.JsonObject;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.core.utils.StringUtils;
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
 * 云影（YunY）资源提供器
 *
 * <p>负责：</p>
 * <ul>
 *     <li>根据关键字从云影站点搜索资源</li>
 *     <li>解析详情页面和 JSON 数据，构建播放地址及频道信息</li>
 *     <li>根据同步配置批量拉取不同类型（MV/TV/AC 等）的视频资源</li>
 * </ul>
 *
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

    /**
     * 简单本地调试入口
     */
    public static void main(String[] args) {
        VideoSearch videoSearch = new VideoSearch();
        videoSearch.setKeyword("毕正明的证明");
        YunYResourceProvider yunYResourceProvider = new YunYResourceProvider(new VideoSource());
        yunYResourceProvider.fetchVideos(new VideoSyncConfig(), (videoInfoResult, videoSyncProcessConfig) -> {
            System.out.println();
        });
    }

    /**
     * 根据关键字搜索云影资源
     *
     * @param videoSearch 搜索条件
     * @return 分页结果
     */
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

    /**
     * 解析详情页 JSON 数据并补充视频基础信息及播放线路
     */
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

    /**
     * 解析在线资源列表并注册到 {@link VideoInfoResult}
     */
    private void registerVideoResources(VideoInfoResult videoInfoResult, JsonArray onlineVideoResources, JsonArray jsonArray) {
        List<VideoPlayAddress> playAddressesList = new ArrayList<>();
        onlineVideoResources.forEach(onlineVideoResource -> {
            JsonObject detail = jsonArray.getJsonObject((Integer) onlineVideoResource);
            registerVideoResources(videoInfoResult, detail, jsonArray, playAddressesList);
        });
        videoInfoResult.setPlayAddressesList(playAddressesList);
    }

    /**
     * 根据单条线路配置构建播放地址及频道列表
     */
    private void registerVideoResources(VideoInfoResult videoInfoResult, JsonObject detail, JsonArray jsonArray, List<VideoPlayAddress> playAddressesList) {
        int lineSourceNameLine = detail.getIntValue("lineSourceName", 0);
        int selectsLine = detail.getIntValue("selects", 0);
        String lineSourceName = Converter.convertIfNecessary(jsonArray.get(lineSourceNameLine), String.class);
        VideoPlayAddress videoPlayAddress = new VideoPlayAddress();
        videoPlayAddress.setVideoPlayAddressUnique(String.valueOf(selectsLine));
        videoPlayAddress.setVideoPlayAddressName(lineSourceName);
        videoPlayAddress.setVideoPlayAddressCode(lineSourceName);
        videoPlayAddress.setVideoPlayAddressChannels(createChannel(selectsLine, jsonArray));

        playAddressesList.add(videoPlayAddress);
    }

    /**
     * 创建某条线路下的所有频道
     */
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

    /**
     * 按照同步配置批量抓取视频数据
     *
     * <p>根据偏移量信息，依次抓取多个视频类型（MV/TV/AC 等）的分页数据，
     * 并通过回调 {@link BiConsumer} 将结果交给上层处理。</p>
     */
    @Override
    public void fetchVideos(VideoSyncConfig config, BiConsumer<VideoInfoResult, VideoSyncProcessConfig> consumer) {
        // https://www.yuny.tv/videoClassify/1?current=2
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
    /**
     * 根据视频类型获取对应的分类 API 地址
     *
     * @param videoSearch 视频搜索条件（仅使用其中的 videoType 字段）
     * @return 分类 API 地址，未知类型时返回 {@code null}
     */
    private String getApiUrl(VideoSearch videoSearch) {
        String videoType = videoSearch.getVideoType();
        if (StringUtils.isEmpty(videoType)) {
            return null;
        }

        return switch (videoType.toUpperCase()) {
            case "MV", "FILM" -> "https://www.yuny.tv/videoClassify/1";
            case "TV" -> "https://www.yuny.tv/videoClassify/2";
            case "AC" -> "https://www.yuny.tv/videoClassify/3";
            case "SP" -> "https://www.yuny.tv/videoClassify/4";
            case "VAR" -> "https://www.yuny.tv/videoClassify/5";
            default -> null;
        };
    }
}
