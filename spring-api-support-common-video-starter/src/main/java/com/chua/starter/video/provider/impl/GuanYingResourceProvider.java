package com.chua.starter.video.provider.impl;

import com.chua.common.support.http.HttpClient;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.NumberUtils;
import com.chua.common.support.utils.RegexUtils;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoDownload;
import com.chua.starter.video.entity.VideoMark;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.entity.VideoSyncConfig;
import com.chua.starter.video.mapper.VideoSourceMapper;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
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
import java.util.function.Consumer;

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

    /**
     * 构造方法
     *
     * @param videoSource 视频源配置信息
     *                    <pre>
     *                                       {@code
     *                                       VideoSource videoSource = new VideoSource();
     *                                       videoSource.setVideoSourcePlatform("GUANYING");
     *                                       videoSource.setVideoSourceToken("your_token_here");
     *                                       GuanYingResourceProvider provider = new GuanYingResourceProvider(videoSource);
     *                                       }
     *                                       </pre>
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
     *                    {@code
     *                    VideoSearch videoSearch = new VideoSearch();
     *                    videoSearch.setKeyword("复仇者联盟");
     *                    videoSearch.setYear(2012);
     *                    videoSearch.setCategory("movie");
     *                    ReturnPageResult<VideoInfoResult> result = provider.searchResource(videoSearch);
     *                    }
     *                    </pre>
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
        createAndSearchVideo(embeddedVideoInfo, videoInfoResults, baseConfig, videoSearch, pages);
        return ReturnPageResultUtils.ok(videoInfoResults, videoSearch.getPage(), videoSearch.getPageSize(), pages);
    }

    /**
     * 创建视频信息并搜索
     *
     * @param embeddedVideoInfo 嵌入视频信息对象
     * @param videoInfoResults  视频信息结果列表
     * @param baseConfig        基础配置对象
     * @param videoSearch       视频搜索条件对象
     * @param pages             总页数
     */
    private void createAndSearchVideo(EmbeddedVideoInfo embeddedVideoInfo,
                                      List<VideoInfoResult> videoInfoResults,
                                      BaseConfig baseConfig,
                                      VideoSearch videoSearch, Integer pages) {
        EmbeddedVideoInfo.InlistDTO inlist = embeddedVideoInfo.getInlist();
        List<String> names = inlist.getT();
        List<Double> readPeople = inlist.getR();
        List<Double> score = inlist.getD();
        List<String> image = inlist.getI();
        List<List<String>> q = inlist.getQ();
        List yearAndType = inlist.getA();
        for (int i = 0; i < names.size(); i++) {
            log.info("从观影获取视频{}列表，配置: {}, 当前第{}, {}/{}页, 数据{}/{}",
                    baseConfig.getApiVideoType(),
                    videoSource.getVideoSourcePlatform(),
                    videoSearch.getYear(),
                    videoSearch.getPage() - 1,
                    pages,
                    i + 1,
                    names.size() + 1);

            List o = (List) yearAndType.get(i);
            VideoInfoResult video = new VideoInfoResult();
            video.setVideoTitle(names.get(i));
            video.setVideoScore(BigDecimal.valueOf(Optional.ofNullable(score.get(i)).orElse(-1D)));
            video.setVideoYear((Integer.valueOf(o.getFirst().toString())));
            Object o1 = o.get(1);
            if (o1 instanceof Number n) {
                video.setVideoDistrict(TAG[n.intValue()]);
            } else {
                String value = o1.toString();
                if (NumberUtils.isNumber(value)) {
                    video.setVideoDistrict(TAG[Integer.parseInt(value)]);
                } else {
                    video.setVideoDistrict(value);
                }
            }
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
            video.setVideoCategory(baseConfig.getApiVideoType().toUpperCase());
            video.setVideoPlatform("GUANYING");
            video.setVideoQuality(Joiner.on(",").join(q.get(i)));
            String dataId = image.get(i);
            String cover = baseConfig.getCoverUrl() + "/%s/220.avif".formatted(dataId);
            video.setVideoCover(cover + ",cover/%s/220.avif".formatted(dataId));
            downloadCover(cover, "/%s/220.avif".formatted(dataId));
            video.setVideoViews(BigDecimal.valueOf(Optional.ofNullable(readPeople.get(i)).orElse(-1D)));
            video.setVideoStatus(0);
            registerDetail(video, dataId, baseConfig);
            registerDownload(video, dataId, baseConfig);

            videoInfoResults.add(video);
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
        return new BaseConfig(apiUrl, detailUrl, coverUrl, downloadUrl, mvType);
    }

    /**
     * 获取API视频类型
     *
     * @param videoSearch 视频搜索条件
     * @return API视频类型字符串
     */
    private String getApiVideoType(VideoSearch videoSearch) {
        String videoType = videoSearch.getVideoType();
        if ("movie".equalsIgnoreCase(videoType) || "film".equalsIgnoreCase(videoType)) {
            return "mv";
        }

        if ("tv".equalsIgnoreCase(videoType)) {
            return "tv";
        }

        if ("anime".equalsIgnoreCase(videoType)) {
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
        if ("movie".equalsIgnoreCase(videoType) || "film".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/mv/";
        }

        if ("tv".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/tv/";
        }

        if ("anime".equalsIgnoreCase(videoType)) {
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
        if ("movie".equalsIgnoreCase(videoType) || "film".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/res/downurl/mv/";
        }

        if ("tv".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/res/downurl/tv/";
        }

        if ("anime".equalsIgnoreCase(videoType)) {
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
        if ("movie".equalsIgnoreCase(videoType) || "film".equalsIgnoreCase(videoType)) {
            return "https://s.tutu.pm/img/mv";
        }

        if ("tv".equalsIgnoreCase(videoType)) {
            return "https://s.tutu.pm/img/tv";
        }

        if ("anime".equalsIgnoreCase(videoType)) {
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
        if ("movie".equalsIgnoreCase(videoType) || "film".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/res/mv";
        }

        if ("tv".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/res/tv";
        }

        if ("anime".equalsIgnoreCase(videoType)) {
            return "https://www.gyg.la/res/ac";
        }

        return null;
    }

    /**
     * 注册下载信息
     *
     * @param video    视频信息结果对象
     * @param dataId   数据ID
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
        registerClDownload(videoDownloads, embeddedDownload.getDownlist());
        registerPanDownload(videoDownloads, embeddedDownload.getPanlist());

        video.setDownloadList(videoDownloads);
    }

    /**
     * 注册网盘下载信息
     *
     * @param videoDownloads 视频下载列表
     * @param panlist        网盘列表数据传输对象
     */
    private void registerPanDownload(List<VideoDownload> videoDownloads, PanlistDTO panlist) {
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
     * @param video     视频信息结果对象
     * @param baseJson  基础JSON字符串
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
                videoMark.setVideoMarkScore(new BigDecimal(pf.getDb().getS()));
                videoMark.setVideoMarkPeople(pf.getDb().getR());

                videoMarks.add(videoMark);

                VideoMark videoMark1 = new VideoMark();
                videoMark1.setVideoMarkType("IMDb");
                videoMark1.setVideoMarkScore(new BigDecimal(pf.getIm().getS()));
                videoMark1.setVideoMarkPeople(pf.getIm().getR());

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
    public void fetchVideos(VideoSyncConfig config, Consumer<VideoInfoResult> consumer) {
        AtomicInteger year = new AtomicInteger(LocalDateTime.now().getYear());
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

        String[] mvType = new String[]{"film", "tv", "ac"};
        while (year.get() >= MIN_YEAR) {
            AtomicInteger count = new AtomicInteger(1);
            for (String s : mvType) {
                VideoSearch videoSearch = new VideoSearch();
                videoSearch.setVideoType(s);
                String apiUrl = getApiUrl(videoSearch);
                BaseConfig baseConfig = createBaseConfig(apiUrl, videoSearch);

                while (true) {
                    String mvUrl = baseConfig.getApiUrl() + "?year=" + (year.get()) + "&page=" + count.get();
                    EmbeddedVideoInfo embeddedVideoInfo = HttpClient.get(mvUrl)
                            .cookie(videoSource1.getVideoSourceToken())
                            .newInvoker()
                            .execute()
                            .content(EmbeddedVideoInfo.class);
                    Integer pages = embeddedVideoInfo.getPage().getPages();
                    log.info("从观影获取视频{}列表，配置: {}, 当前第{}, {}/{}页", mvType, config.getVideoSyncConfigName(), year.get(), count.get() - 1, pages);

                    String videoSyncConfigStatus = config.getVideoSyncConfigStatus();
                    if (SYNC_STATUS_FINISH.equals(videoSyncConfigStatus) ||
                            SYNC_STATUS_ERROR.equals(videoSyncConfigStatus)
                    ) {
                        break;
                    }

                    List<VideoInfoResult> videoInfoResults = new LinkedList<>();
                    createAndSearchVideo(embeddedVideoInfo, videoInfoResults, baseConfig, videoSearch, pages);
                    videoInfoResults.forEach(consumer);
                }
            }
        }
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
            private List<String> list;
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
