package com.chua.starter.video.provider.impl;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.http.HttpClient;
import com.chua.common.support.http.HttpResponse;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.utils.*;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.*;
import com.chua.starter.video.mapper.VideoSourceMapper;
import com.chua.starter.video.mapper.VideoSyncConfigMapper;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
import com.chua.starter.video.pojo.VideoSyncProcessConfig;
import com.chua.starter.video.provider.SyncProvider;
import com.google.common.base.Joiner;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static com.chua.common.support.constant.CommonConstant.EMPTY_STRING;
import static com.chua.starter.video.provider.impl.GuanYingResourceProvider.EmbeddedDownload.DownlistDTO;
import static com.chua.starter.video.provider.impl.GuanYingResourceProvider.EmbeddedDownload.PanlistDTO;
import static com.chua.starter.video.service.impl.VideoSyncTaskServiceImpl.SYNC_STATUS_ERROR;
import static com.chua.starter.video.service.impl.VideoSyncTaskServiceImpl.SYNC_STATUS_FINISH;

/**
 * 观影视频资源提供者实现类
 *
 * @author CH
 * @since 2025/9/15 23:27
 */
@Slf4j
@Spi("guanying")
public class GuanYingResourceProvider extends AbstractResourceProvider implements SyncProvider {
    /**
     * 视频封面存储路径
     */
    static final String COVER_PATH = "./video/cover";
    /**
     * 最小年份
     */
    static final int MIN_YEAR = 1960;
    /**
     * 标签映射数组，用于将数字索引转换为具体的标签名称
     */
    static String[] TAG = new String[]{
            "美国", "大陆", "日本", "剧情", "科幻", "动作", "喜剧", "爱情", "冒险", "犯罪", "悬疑", "儿童", "歌舞", "音乐", "奇幻", "动画", "恐怖", "惊悚", "丧尸", "战争", "传记", "纪录", "西部", "灾难", "古装", "武侠", "家庭", "短片", "校园", "文艺", "运动", "青春", "同性", "励志", "人性", "美食", "女性", "治愈", "历史", "真人秀", "脱口秀", "萌系", "日常", "热血", "机战", "游戏", "情色", "搞笑", "恋爱", "后宫", "百合", "基腐", "致郁", "异世界", "泡面", "战斗", "加拿大", "香港", "台湾", "韩国", "印度", "德国", "法国", "英国", "意大利", "巴西", "泰国", "澳大利亚", "荷兰", "西班牙", "墨西哥"
    };
    /**
     * 封面存储路径
     */
    File file = null;
    @AutoInject
    private VideoSourceMapper videoSourceMapper;
    @AutoInject
    private VideoSyncConfigMapper videoSyncConfigMapper;

    /**
     * 构造方法
     *
     * @param videoSource 视频源配置信息
     *                    <pre>
     *                                                                                                                                      {@code
     *                                                                                                                                      VideoSource videoSource = new VideoSource();
     *                                                                                                                                      videoSource.setVideoSourcePlatform("GUANYING");
     *                                                                                                                                      videoSource.setVideoSourceToken("your_token_here");
     *                                                                                                                                      GuanYingResourceProvider provider = new GuanYingResourceProvider(videoSource);
     *                                                                                                                                      }
     *                                                                                                                                      </pre>
     */
    public GuanYingResourceProvider(VideoSource videoSource) {
        super(videoSource);
        file = new File(COVER_PATH, "guanying");
        FileUtils.mkdir(file);
    }

    /**
     * 搜索视频资源
     *
     * @param videoSearch 视频搜索条件，包含关键词、年份、分类等信息
     *                    <pre>
     *                                                                                                                   {@code
     *                                                                                                                   VideoSearch videoSearch = new VideoSearch();
     *                                                                                                                   videoSearch.setKeyword("复仇者联盟");
     *                                                                                                                   videoSearch.setYear(2012);
     *                                                                                                                   videoSearch.setCategory("movie");
     *                                                                                                                   ReturnPageResult<VideoInfoResult> result = provider.searchResource(videoSearch);
     *                                                                                                                   }
     *                                                                                                                   </pre>
     * @return 分页结果，包含匹配的视频信息列表
     */
    @Override
    public ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch) {
        String apiUrl = getApiUrl(videoSearch);
        if (null == apiUrl) {
            return ReturnPageResult.empty();
        }

        BaseConfig baseConfig = createBaseConfig(apiUrl, videoSearch);

        EmbeddedVideoInfo embeddedVideoInfo = HttpClient.get(apiUrl + "?year=" + (videoSearch.getYear()) + "&page=" + videoSearch.getPage())
                .cookie(videoSource.getVideoSourceToken())
                .newInvoker()
                .execute()
                .content(EmbeddedVideoInfo.class);
        Integer pages = embeddedVideoInfo.getPage().getPages();
        log.info("从观影获取视频{}列表，配置: {}, 当前第{}, {}/{}页",
                baseConfig.getApiVideoType(),
                videoSource.getVideoSourcePlatform(),
                videoSearch.getYear(),
                videoSearch.getPage() - 1,
                pages);

        if (embeddedVideoInfo.getInlist() == null || embeddedVideoInfo.getInlist().getA() == null || embeddedVideoInfo.getInlist().getA().isEmpty()) {
            return ReturnPageResult.empty();
        }
        List<VideoInfoResult> videoInfoResults = new LinkedList<>();
        createAndSearchVideo(embeddedVideoInfo, videoInfoResults, baseConfig, 1, pages, null);
        return ReturnPageResultUtils.ok(videoInfoResults, videoSearch.getPage(), videoSearch.getPageSize(), pages);
    }

    /**
     * 创建视频信息并搜索
     *
     * @param embeddedVideoInfo 嵌入视频信息对象
     * @param videoInfoResults  视频信息结果列表
     * @param baseConfig        基础配置对象
     * @param page              当前页码
     * @param pages             总页数
     * @param consumer          消费者对象
     */
    private void createAndSearchVideo(EmbeddedVideoInfo embeddedVideoInfo,
                                      List<VideoInfoResult> videoInfoResults,
                                      BaseConfig baseConfig,
                                      int page,
                                      Integer pages,
                                      BiConsumer<VideoInfoResult, VideoSyncProcessConfig> consumer) {
        EmbeddedVideoInfo.InlistDTO inlist = embeddedVideoInfo.getInlist();
        List<String> names = inlist.getT();
        List<Double> readPeople = inlist.getR();
        List<Double> score = inlist.getD();
        List<String> image = inlist.getI();
        List<List<String>> q = inlist.getQ();
        List yearAndType = inlist.getA();
        String apiVideoType = baseConfig.getApiVideoType();
        int totalInPage = names.size();
        for (int i = 0; i < totalInPage; i++) {
            log.info("从观影获取视频{}列表，配置: {},  {}/{}页, 数据{}/{}",
                    apiVideoType,
                    videoSource.getVideoSourcePlatform(),
                    page,
                    pages,
                    i + 1,
                    totalInPage + 1);

            List o = (List) yearAndType.get(i);
            VideoInfoResult video = new VideoInfoResult();
            video.setVideoTitle(names.get(i));
            video.setVideoScore(BigDecimal.valueOf(Optional.ofNullable(score.get(i)).orElse(-1D)));
            video.setVideoYear((Integer.valueOf(o.getFirst().toString())));
            registerDistrict(video, o.get(1));
            registerTag(video, o);
            video.setVideoCategory(apiVideoType.toUpperCase());
            video.setVideoPlatform("GUANYING");
            video.setVideoQuality(Joiner.on(",").join(q.get(i)));
            String dataId = image.get(i);
            registerCover(video, dataId, baseConfig);
            video.setVideoViews(BigDecimal.valueOf(Optional.ofNullable(readPeople.get(i)).orElse(-1D)));
            video.setVideoStatus(0);
            registerDetail(video, dataId, baseConfig);
            registerDownload(video, dataId, baseConfig);

            videoInfoResults.add(video);
            if (null != consumer) {
                consumer.accept(video, VideoSyncProcessConfig.builder()
                        .videoType(apiVideoType)
                        .totalPages(pages)
                        .page(page)
                        .currentPageInPage(i)
                        .totalInPage(totalInPage)
                        .build()
                );
            }
        }
    }

    /**
     * 注册封面信息
     *
     * @param video      视频信息对象
     * @param dataId     数据ID
     * @param baseConfig 基础配置对象
     */
    private void registerCover(VideoInfoResult video, String dataId, BaseConfig baseConfig) {
        String cover = baseConfig.getCoverUrl() + "/%s/%s".formatted(dataId, videoSource.getVideoSourceCoverSuffix());
        video.setVideoCover(cover + ",cover/%s/%s".formatted(dataId, videoSource.getVideoSourceCoverSuffix()));
        try {
            downloadCover(cover, "/%s/%s".formatted(dataId, videoSource.getVideoSourceCoverSuffix()));
        } catch (Exception ignored) {
        }
    }

    /**
     * 注册标签信息
     *
     * @param video 视频信息对象
     * @param o     标签信息对象
     */
    private void registerTag(VideoInfoResult video, List o) {
        List<String> tags = new LinkedList<>();
        for (int j = 2; j < o.size(); j++) {
            Object s = o.get(j);
            if (s instanceof Number n) {
                tags.add(TAG[n.intValue()]);
            } else {
                String value = s.toString();
                if (NumberUtils.isNumber(value)) {
                    tags.add(TAG[Integer.parseInt(value)]);
                } else {
                    tags.add(value);
                }
            }

        }
        video.setVideoType(Joiner.on(",").join(tags));
    }

    /**
     * 注册地区信息
     *
     * @param video 视频信息对象
     * @param o1    地区信息对象
     */
    private void registerDistrict(VideoInfoResult video, Object o1) {
        if (o1 instanceof Number n) {
            video.setVideoDistrict(TAG[n.intValue()]);
            return;
        }
        String value = o1.toString();
        if (NumberUtils.isNumber(value)) {
            video.setVideoDistrict(TAG[Integer.parseInt(value)]);
        } else {
            video.setVideoDistrict(value);
        }
    }

    /**
     * 创建基础配置对象
     *
     * @param apiUrl      API URL
     * @param videoSearch 视频搜索条件
     * @return 基础配置对象
     */
    private BaseConfig createBaseConfig(String apiUrl, VideoSearch videoSearch) {
        String coverUrl = getCoverUrl(videoSearch);
        String downloadUrl = getDownloadUrl(videoSearch);
        String detailUrl = getDetailUrl(videoSearch);
        String mvType = getApiVideoType(videoSearch);
        return new BaseConfig(apiUrl, detailUrl, coverUrl, videoSource.getVideoSourceCoverSuffix(), downloadUrl, mvType);
    }

    /**
     * 获取API视频类型
     *
     * @param videoSearch 视频搜索条件
     * @return API视频类型字符串
     */
    private String getApiVideoType(VideoSearch videoSearch) {
        String videoType = videoSearch.getVideoType();
        if ("MV".equalsIgnoreCase(videoType) || "film".equalsIgnoreCase(videoType)) {
            return "mv";
        }

        if ("TV".equalsIgnoreCase(videoType)) {
            return "tv";
        }

        if ("AC".equalsIgnoreCase(videoType)) {
            return "ac";
        }

        return EMPTY_STRING;
    }

    /**
     * 获取详情页URL
     *
     * @param videoSearch 视频搜索条件
     * @return 详情页URL
     */
    private String getDetailUrl(VideoSearch videoSearch) {
        String videoType = videoSearch.getVideoType();
        if ("MV".equalsIgnoreCase(videoType) || "film".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/mv/";
        }

        if ("TV".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/tv/";
        }

        if ("AC".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/ac/";
        }

        return EMPTY_STRING;
    }

    /**
     * 获取下载URL
     *
     * @param videoSearch 视频搜索条件
     * @return 下载URL
     */
    private String getDownloadUrl(VideoSearch videoSearch) {
        String videoType = videoSearch.getVideoType();
        if ("MV".equalsIgnoreCase(videoType) || "film".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/res/downurl/mv/";
        }

        if ("TV".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/res/downurl/tv/";
        }

        if ("AC".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/res/downurl/ac/";
        }

        return EMPTY_STRING;
    }

    /**
     * 获取封面URL
     *
     * @param videoSearch 视频搜索条件
     * @return 封面URL
     */
    private String getCoverUrl(VideoSearch videoSearch) {
        String videoType = videoSearch.getVideoType();
        if ("MV".equalsIgnoreCase(videoType) || "film".equalsIgnoreCase(videoType)) {
            return "https://s.tutu.pm/img/mv";
        }

        if ("TV".equalsIgnoreCase(videoType)) {
            return "https://s.tutu.pm/img/tv";
        }

        if ("AC".equalsIgnoreCase(videoType)) {
            return "https://s.tutu.pm/img/ac";
        }

        return EMPTY_STRING;
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
            return "https://www.gyg.la/res/mv";
        }

        if ("TV".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/res/tv";
        }

        if ("AC".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/res/ac";
        }

        return null;
    }

    /**
     * 注册下载信息
     *
     * @param video      视频信息结果对象
     * @param dataId     数据ID
     * @param baseConfig 基础配置对象
     */
    private void registerDownload(VideoInfoResult video, String dataId, BaseConfig baseConfig) {
        //https://www.gyg.la/res/downurl/mv/mXr4
        EmbeddedDownload embeddedDownload = HttpClient.get(baseConfig.getDownloadUrl() + dataId)
                .cookie(videoSource.getVideoSourceToken())
                .newInvoker()
                .execute()
                .content(EmbeddedDownload.class);

        List<VideoDownload> videoDownloads = new ArrayList<>();
        List<VideoPlayAddress> playAddresses = new ArrayList<>();
        registerClDownload(videoDownloads, embeddedDownload.getDownlist());
        registerPanDownload(videoDownloads, embeddedDownload.getPanlist());
        registerPlayAddress(playAddresses, embeddedDownload.getPlaylist());
        video.setDownloadList(videoDownloads);
        video.setPlayAddressesList(playAddresses);
    }

    /**
     * 注册播放地址信息
     *
     * @param videoPlayAddresses 视频下载列表
     * @param playlist           播放列表数据传输对象
     */
    private void registerPlayAddress(List<VideoPlayAddress> videoPlayAddresses, List<EmbeddedDownload.PlaylistDTO> playlist) {
        if (null == playlist) {
            return;
        }
        for (EmbeddedDownload.PlaylistDTO playlistDTO : playlist) {
            VideoPlayAddress videoPlayAddress = new VideoPlayAddress();
            videoPlayAddress.setVideoPlayAddressName(playlistDTO.getT());
            videoPlayAddress.setVideoPlayAddressCode(playlistDTO.getI());
            videoPlayAddress.setVideoPlayAddressUnique(playlistDTO.getM());

            List list = playlistDTO.getList();
            List<VideoPlayAddressChannel> videoPlayAddressChannels = new ArrayList<>(list.size());
            for (Object s : list) {
                ThreadUtils.sleep(500);
                if (s instanceof String str) {
                    VideoPlayAddressChannel videoPlayAddressChannel = new VideoPlayAddressChannel();
                    videoPlayAddressChannel.setVideoPlayAddressChannelName(str);
                    videoPlayAddressChannel.setVideoPlayAddressStartIndex(1);
                    videoPlayAddressChannel.setVideoPlayAddressEndIndex(1);
                    videoPlayAddressChannel.setVideoPlayAddressUrl(createVideoPlayAddressUrl(playlistDTO, 1));
                    videoPlayAddressChannels.add(videoPlayAddressChannel);
                    continue;
                }

                if (s instanceof List list1) {
                    Object object = list1.get(1);
                    if (object instanceof List o1) {
                        Integer start = (Integer) o1.get(0);
                        Integer end = (Integer) o1.get(1);
                        IntStream.rangeClosed(start, end).forEach(i -> {
                            VideoPlayAddressChannel videoPlayAddressChannel = new VideoPlayAddressChannel();
                            videoPlayAddressChannel.setVideoPlayAddressChannelName("第%s集".formatted(i));
                            videoPlayAddressChannel.setVideoPlayAddressStartIndex(i);
                            videoPlayAddressChannel.setVideoPlayAddressEndIndex(i);
                            videoPlayAddressChannel.setVideoPlayAddressUrl(createVideoPlayAddressUrl(playlistDTO, i));
                            videoPlayAddressChannels.add(videoPlayAddressChannel);
                        });
                        continue;
                    }
                    if (object instanceof Number number) {
                        VideoPlayAddressChannel videoPlayAddressChannel = new VideoPlayAddressChannel();
                        videoPlayAddressChannel.setVideoPlayAddressChannelName("第%s集".formatted(number));
                        videoPlayAddressChannel.setVideoPlayAddressStartIndex(number.intValue());
                        videoPlayAddressChannel.setVideoPlayAddressEndIndex(number.intValue());
                        videoPlayAddressChannel.setVideoPlayAddressUrl(createVideoPlayAddressUrl(playlistDTO, number.intValue()));
                        videoPlayAddressChannels.add(videoPlayAddressChannel);
                    }
                }
            }

            videoPlayAddress.setVideoPlayAddressChannels(videoPlayAddressChannels);
            videoPlayAddresses.add(videoPlayAddress);
        }

    }

    private String createVideoPlayAddressUrl(EmbeddedDownload.PlaylistDTO playlistDTO, int index) {
        try {
            String s = playlistDTO.getI();

            HttpResponse httpResponse = HttpClient.post(
                            "https://www.gyg.la/py/%s/%s".formatted(s, index)
                    ).header("Content-Type", "application/x-www-form-urlencoded")
                    .cookie(videoSource.getVideoSourceToken())
                    .form("ajax", 1)
                    .execute();
            JsonObject content = httpResponse.content(JsonObject.class);
            return MapUtils.getString(content, "url");
        } catch (Exception e) {
        }

        return null;
    }


    /**
     * 注册网盘下载信息
     *
     * @param videoDownloads 视频下载列表
     * @param panlist        网盘列表数据传输对象
     */
    private void registerPanDownload(List<VideoDownload> videoDownloads, PanlistDTO panlist) {
        if (null == panlist) {
            return;
        }
        try {
            List<String> name = panlist.getName();
            List<String> tname = panlist.getTname();
            List<Integer> type = panlist.getType();
            Map<String, String> typeMap = new LinkedHashMap<>();
            for (Integer i : type) {
                typeMap.put(String.valueOf(i), CollectionUtils.get(tname, i));
            }
            List<String> url = panlist.getUrl();
            List<String> user = panlist.getUser();
            List<String> time = panlist.getTime();
            for (int i = 0; i < name.size(); i++) {
                String s = name.get(i);
                VideoDownload videoDownload = new VideoDownload();
                videoDownload.setVideoDownloadName(s);
                videoDownload.setVideoDownloadUrl(CollectionUtils.get(url, i));
                videoDownload.setCreateName(CollectionUtils.get(user, i));
                videoDownload.setVideoDownloadShareTime(CollectionUtils.get(time, i));
                videoDownload.setVideoDownloadType("网盘资源");
                videoDownload.setVideoDownloadQuality(typeMap.get(String.valueOf(type.get(i))));
                videoDownloads.add(videoDownload);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 注册磁力链接下载信息
     *
     * @param videoDownloads 视频下载列表
     * @param downlist       下载列表数据传输对象
     */
    private void registerClDownload(List<VideoDownload> videoDownloads, DownlistDTO downlist) {
        if (null == downlist) {
            return;
        }
        try {
            DownlistDTO.ListDTO list = downlist.getList();
            DownlistDTO.TypeDTO type = downlist.getType();
            Map<String, String> typeMap = new LinkedHashMap<>();
            List<String> b = type.getB();
            List<String> a = type.getA();
            for (int i = 0; i < b.size(); i++) {
                typeMap.put(b.get(i), a.get(i));
            }

            List<String> names = list.getT();
            List<String> size = list.getS();
            List<String> p = list.getP();
            List<Integer> downloadedSize = list.getE();
            List<String> magnetic = list.getM();
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                VideoDownload videoDownload = new VideoDownload();
                videoDownload.setVideoDownloadType("磁力资源");
                videoDownload.setVideoDownloadName(name);
                videoDownload.setVideoDownloadSize(CollectionUtils.get(size, i));
                videoDownload.setVideoDownloadCount(CollectionUtils.get(downloadedSize, i));
                videoDownload.setVideoDownloadMagnetic(CollectionUtils.get(magnetic, i));
                videoDownload.setVideoDownloadQuality(typeMap.get(CollectionUtils.get(p, i)));
                videoDownload.setVideoDownloadUrl(CollectionUtils.get(magnetic, i));

                videoDownloads.add(videoDownload);
            }
        } catch (Exception ignored) {
        }

    }

    /**
     * 注册详细信息
     *
     * @param video      视频信息结果对象
     * @param dataId     数据ID
     * @param baseConfig 基础配置对象
     */
    private void registerDetail(VideoInfoResult video, String dataId, BaseConfig baseConfig) {
        String detailHtml = HttpClient.get(baseConfig.getDetailUrl() + dataId)
                .cookie(videoSource.getVideoSourceToken())
                .newInvoker()
                .execute()
                .content(String.class);

        int i = detailHtml.indexOf("_obj.d=");
        if (i > 0) {
            registerDetailMoreBase(video, detailHtml.substring(i + 7, detailHtml.indexOf("};", i + 7) + 1));
        }

    }

    /**
     * 注册更多基础详细信息
     *
     * @param video    视频信息结果对象
     * @param baseJson 基础JSON字符串
     */
    private void registerDetailMoreBase(VideoInfoResult video, String baseJson) {
        try {
            BaseDetail baseDetail = Json.fromJson(baseJson, BaseDetail.class);
            video.setVideoName(null == baseDetail.getName() ? null : baseDetail.getName().trim());
            video.setVideoDirector(Joiner.on(",").join(baseDetail.getDaoyan()));
            video.setVideoWriter(Joiner.on(",").join(baseDetail.getBianju()));
            video.setVideoActor(Joiner.on(",").join(baseDetail.getZhuyan()));
            video.setVideoType(Joiner.on(",").join(baseDetail.getLeixing()));
            video.setVideoDistrict(Joiner.on(",").join(baseDetail.getDiqu()));
            video.setVideoLanguage(Joiner.on(",").join(baseDetail.getYuyan()));
            video.setVideoDuration(RegexUtils.getFirstNumber(baseDetail.getTimes()));
            video.setVideoRelease(baseDetail.getStime());
            video.setVideoDescription(baseDetail.getIntroduce());
            video.setVideoAliasName(baseDetail.getEname());
            BaseDetail.PfDTO pf = baseDetail.getPf();
            if (null != pf) {
                video.setVideoDouBanId(String.valueOf(pf.getDb().getId()));
                List<VideoMark> videoMarks = new ArrayList<>();
                VideoMark videoMark = new VideoMark();
                videoMark.setVideoMarkType("豆瓣");
                try {
                    videoMark.setVideoMarkScore(Converter.convertIfNecessary(pf.getDb().getS(), BigDecimal.class));
                } catch (Exception ignored) {
                }
                videoMark.setVideoMarkPeople(pf.getDb().getR());

                videoMarks.add(videoMark);

                VideoMark videoMark1 = new VideoMark();
                videoMark1.setVideoMarkType("IMDb");
                videoMark1.setVideoMarkScore(Converter.convertIfNecessary(pf.getIm().getS(), BigDecimal.class));
                try {
                    videoMark1.setVideoMarkPeople(pf.getIm().getR());
                } catch (Exception ignored) {
                }

                videoMarks.add(videoMark1);
                video.setVideoMarkList(videoMarks);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 下载封面图片
     *
     * @param cover 封面URL
     * @param name  文件名
     */
    private void downloadCover(String cover, String name) {
        File file1 = new File(file, name);
        FileUtils.forceMkdirParent(file1);
        try (InputStream is = new URL(cover).openStream()) {
            FileUtils.writeTo(is, file1);
        } catch (IOException e) {
            throw new RuntimeException("下载封面图片失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void fetchVideos(VideoSyncConfig config, BiConsumer<VideoInfoResult, VideoSyncProcessConfig> consumer) {
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
        int minYear = ObjectUtils.defaultIfNull(videoSource.getVideoSourceMinYear(), MIN_YEAR);
        root:
        for (String videoType : mvType) {
            AtomicInteger searchPage = new AtomicInteger(createCount(jsonObject, videoType));
            AtomicInteger year = createYear(jsonObject, videoType);
            while (year.get() >= minYear) {
                VideoSearch videoSearch = new VideoSearch();
                videoSearch.setVideoType(videoType.toUpperCase());
                String apiUrl = getApiUrl(videoSearch);
                BaseConfig baseConfig = createBaseConfig(apiUrl, videoSearch);

                String mvUrl = baseConfig.getApiUrl() + "?year=" + (year.get()) + "&page=" + searchPage.get();
                HttpResponse httpResponse = HttpClient.get(mvUrl)
                        .cookie(videoSource1.getVideoSourceToken())
                        .newInvoker()
                        .execute();
                if (httpResponse.code() == 500) {
                    break root;
                }
                EmbeddedVideoInfo embeddedVideoInfo = httpResponse.content(EmbeddedVideoInfo.class);
                EmbeddedVideoInfo.PageDTO page = embeddedVideoInfo.getPage();
                if (null == page) {
                    break;
                }
                Integer pages = page.getPages();
                if (searchPage.get() > pages) {
                    countdownYear(year, searchPage);
                    continue;
                }
                String logMessage = StringUtils.format("从观影获取视频{}列表，配置: {}, 当前第{}, {}/{}页",
                        videoType,
                        config.getVideoSyncConfigName(),
                        year.get(),
                        searchPage.get(),
                        pages);
                log.info(logMessage);


                videoSyncConfigStatus = config.getVideoSyncConfigStatus();
                if (SYNC_STATUS_FINISH.equals(videoSyncConfigStatus) ||
                        SYNC_STATUS_ERROR.equals(videoSyncConfigStatus)
                ) {
                    break root;
                }

                List<VideoInfoResult> videoInfoResults = new LinkedList<>();
                createAndSearchVideo(embeddedVideoInfo, videoInfoResults, baseConfig, searchPage.get(), pages, consumer);
                searchPage.incrementAndGet();
                JsonObject jsonObject1 = new JsonObject();
                jsonObject1.fluentPut(videoType + "_page", searchPage.get());
                jsonObject1.fluentPut(videoType + "_year", year.get());
                jsonObject1.fluentPut("videoType", videoType);
                config.setVideoSyncConfigLastOffset(jsonObject1.toJSONString());
                config.setVideoSyncConfigLastLog(logMessage);
                videoSyncConfigMapper.updateById(config);
                if (searchPage.get() > pages) {
                    countdownYear(year, searchPage);
                }
            }
        }
    }

    private void countdownYear(AtomicInteger year, AtomicInteger searchPage) {
        year.decrementAndGet();
        searchPage.set(1);
    }

    /**
     * 创建年份
     *
     * @param jsonObject
     * @param videoType
     * @return
     */
    private AtomicInteger createYear(JsonObject jsonObject, String videoType) {
        return new AtomicInteger(jsonObject.getInteger(videoType + "_year", LocalDateTime.now().getYear()));
    }

    /**
     * 根据指定的视频类型从JSON对象中获取对应的页码计数，如果不存在则返回默认值1。
     *
     * @param jsonObject 包含视频同步偏移量信息的JSON对象，用于记录不同视频类型的同步进度
     *                   <pre><code>
     *                                                                                                             JsonObject offset = new JsonObject();
     *                                                                                                             offset.put("MV", 3);
     *                                                                                                             int count = provider.createCount(offset, "MV"); // 返回3
     *                                                                                                             </code></pre>
     * @param videoType  视频类型字符串，例如"MV"、"TV"或"AC"，表示当前正在处理的视频类别
     *                   <pre><code>
     *                                                                                                             String videoType = "TV";
     *                                                                                                             int count = provider.createCount(offset, videoType);
     *                                                                                                             </code></pre>
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
     *                                                                                                                                                                                                       JsonObject offset = new JsonObject();
     *                                                                                                                                                                                                       offset.put("videoType", "TV");
     *                                                                                                                                                                                                       String[] types = provider.createMvType(offset);
     *                                                                                                                                                                                                       }</pre>
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
     * 基础配置类
     */
    @Data
    @AllArgsConstructor
    static class BaseConfig {
        /**
         * API URL
         */
        String apiUrl;
        /**
         * 详情页URL
         */
        String detailUrl;
        /**
         * 封面URL
         */
        String coverUrl;
        /**
         * 封面后缀
         */
        String coverSuffix;
        /**
         * 下载URL
         */
        String downloadUrl;
        /**
         * API视频类型
         */
        String apiVideoType;
    }

    @NoArgsConstructor
    @Data
    static class EmbeddedDownload {

        @SerializedName("code")
        private Integer code;
        @SerializedName("downlist")
        private DownlistDTO downlist;
        @SerializedName("playlist")
        private List<PlaylistDTO> playlist;
        @SerializedName("panlist")
        private PanlistDTO panlist;

        @NoArgsConstructor
        @Data
        public static class DownlistDTO {
            @SerializedName("imdb")
            private String imdb;
            @SerializedName("type")
            private TypeDTO type;
            @SerializedName("list")
            private ListDTO list;
            @SerializedName("key")
            private String key;
            @SerializedName("sort")
            private List<Integer> sort;

            @NoArgsConstructor
            @Data
            public static class TypeDTO {
                @SerializedName("a")
                private List<String> a;
                @SerializedName("b")
                private List<String> b;
            }

            @NoArgsConstructor
            @Data
            public static class ListDTO {
                @SerializedName("m")
                private List<String> m;
                @SerializedName("t")
                private List<String> t;
                @SerializedName("s")
                private List<String> s;
                @SerializedName("e")
                private List<Integer> e;
                @SerializedName("p")
                private List<String> p;
                @SerializedName("u")
                private List<String> u;
                @SerializedName("k")
                private List<Integer> k;
                @SerializedName("n")
                private List<String> n;
            }
        }

        @NoArgsConstructor
        @Data
        public static class PanlistDTO {
            @SerializedName("id")
            private List<String> id;
            @SerializedName("name")
            private List<String> name;
            @SerializedName("p")
            private List<String> p;
            @SerializedName("url")
            private List<String> url;
            @SerializedName("type")
            private List<Integer> type;
            @SerializedName("user")
            private List<String> user;
            @SerializedName("time")
            private List<String> time;
            @SerializedName("e")
            private List<Integer> e;
            @SerializedName("tname")
            private List<String> tname;
        }

        @NoArgsConstructor
        @Data
        public static class PlaylistDTO {
            @SerializedName("i")
            private String i;
            @SerializedName("t")
            private String t;
            @SerializedName("m")
            private String m;
            @SerializedName("list")
            private List list;
        }
    }

    @NoArgsConstructor
    @Data
    static class BaseDetail {

        @SerializedName("title")
        private String title;
        @SerializedName("name")
        private String name;
        @SerializedName("year")
        private Integer year;
        @SerializedName("dir")
        private String dir;
        @SerializedName("status")
        private String status;
        @SerializedName("fa")
        private Integer fa;
        @SerializedName("daoyan")
        private List<String> daoyan;
        @SerializedName("bianju")
        private List<String> bianju;
        @SerializedName("zhuyan")
        private List<String> zhuyan;
        @SerializedName("leixing")
        private List<String> leixing;
        @SerializedName("diqu")
        private List<String> diqu;
        @SerializedName("yuyan")
        private List<String> yuyan;
        @SerializedName("stime")
        private String stime;
        @SerializedName("times")
        private String times;
        @SerializedName("ename")
        private String ename;
        @SerializedName("pf")
        private PfDTO pf;
        @SerializedName("introduce")
        private String introduce;
        @SerializedName("id")
        private String id;
        @SerializedName("dname")
        private String dname;

        @NoArgsConstructor
        @Data
        public static class PfDTO {
            @SerializedName("db")
            private DbDTO db;
            @SerializedName("im")
            private ImDTO im;
            @SerializedName("ro")
            private RoDTO ro;

            @NoArgsConstructor
            @Data
            public static class DbDTO {
                @SerializedName("id")
                private String id;
                @SerializedName("n")
                private Integer n;
                @SerializedName("s")
                private String s;
                @SerializedName("r")
                private Integer r;
            }

            @NoArgsConstructor
            @Data
            public static class ImDTO {
                @SerializedName("id")
                private String id;
                @SerializedName("s")
                private String s;
                @SerializedName("r")
                private Integer r;
                @SerializedName("n")
                private Integer n;
            }

            @NoArgsConstructor
            @Data
            public static class RoDTO {
                @SerializedName("id")
                private String id;
                @SerializedName("x1")
                private Integer x1;
                @SerializedName("s1")
                private String s1;
                @SerializedName("r1")
                private Integer r1;
                @SerializedName("x2")
                private Integer x2;
                @SerializedName("s2")
                private String s2;
                @SerializedName("r2")
                private Integer r2;
            }
        }
    }

    @NoArgsConstructor
    @Data
    static class EmbeddedVideoInfo {

        @SerializedName("page")
        private PageDTO page;
        @SerializedName("inlist")
        private InlistDTO inlist;
        @SerializedName("footer")
        private FooterDTO footer;

        @NoArgsConstructor
        @Data
        public static class PageDTO {
            @SerializedName("pages")
            private Integer pages;
        }

        @NoArgsConstructor
        @Data
        public static class InlistDTO {
            @SerializedName("a")
            private List<List<String>> a;
            @SerializedName("r")
            private List<Double> r;
            @SerializedName("z")
            private List<Integer> z;
            @SerializedName("d")
            private List<Double> d;
            @SerializedName("i")
            private List<String> i;
            @SerializedName("g")
            private List<String> g;
            @SerializedName("t")
            private List<String> t;
            @SerializedName("q")
            private List<List<String>> q;
            @SerializedName("ty")
            private String ty;
            @SerializedName("load")
            private Integer load;
        }

        @NoArgsConstructor
        @Data
        public static class FooterDTO {
            @SerializedName("t")
            private String t;
        }
    }
}
