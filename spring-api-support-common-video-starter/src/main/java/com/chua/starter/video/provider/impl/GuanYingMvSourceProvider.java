package com.chua.starter.video.provider.impl;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.http.HttpClient;
import com.chua.common.support.json.Json;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.NumberUtils;
import com.chua.common.support.utils.RegexUtils;
import com.chua.starter.video.entity.VideoDownload;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.entity.VideoMark;
import com.chua.starter.video.entity.VideoSyncConfig;
import com.chua.starter.video.provider.VideoSourceProvider;
import com.google.common.base.Joiner;
import com.google.gson.annotations.SerializedName;
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

/**
 * GuanYing视频源提供者
 *
 * @author CH
 * @since 2024/6/21
 */
@Slf4j
@Spi("guanying")
public class GuanYingMvSourceProvider implements VideoSourceProvider {

    static File file = null;
    static String[] TAG = new String[]{
            "美国", "大陆", "日本", "剧情", "科幻", "动作", "喜剧", "爱情", "冒险", "犯罪", "悬疑", "儿童", "歌舞", "音乐", "奇幻", "动画", "恐怖", "惊悚", "丧尸", "战争", "传记", "纪录", "西部", "灾难", "古装", "武侠", "家庭", "短片", "校园", "文艺", "运动", "青春", "同性", "励志", "人性", "美食", "女性", "治愈", "历史", "真人秀", "脱口秀", "萌系", "日常", "热血", "机战", "游戏", "情色", "搞笑", "恋爱", "后宫", "百合", "基腐", "致郁", "异世界", "泡面", "战斗", "加拿大", "香港", "台湾", "韩国", "印度", "德国", "法国", "英国", "意大利", "巴西", "泰国", "澳大利亚", "荷兰", "西班牙", "墨西哥"
    };

    protected String mvUrl = "https://www.gyg.la/res/mv";
    protected String mvCoverUrl = "https://s.tutu.pm/img/mv";
    protected String mvDownloadUrl = "https://www.gyg.la/res/downurl/mv/";
    protected String mvDetailUrl = "https://www.gyg.la/mv/";
    protected String mvType = "mv";
    protected int minYear = 1960;

    static {
        file = new File(COVER_PATH, "guanying");
        FileUtils.mkdir(file);
    }
    @Override
    public String getProviderName() {
        return "guanying";
    }

    @Override
    public void fetchVideos(VideoSyncConfig config, Consumer<VideoInfo> consumer) {
        log.info("从观影获取视频列表，配置: {}", config.getVideoSyncConfigName());

        try {
            AtomicInteger year = new AtomicInteger(LocalDateTime.now().getYear());
            while (year.get() >= minYear) {
                AtomicInteger count = new AtomicInteger(1);
                page:
                while (true) {
                    EmbeddedVideoInfo embeddedVideoInfo = HttpClient.get(mvUrl + "?year=" + year.getAndDecrement() + "&page=" + count.getAndIncrement())
                            .cookie(config.getVideoSyncConfigCookie())
                            .newInvoker()
                            .execute()
                            .content(EmbeddedVideoInfo.class);
                    Integer pages = embeddedVideoInfo.getPage().getPages();
                    log.info("从观影获取视频{}列表，配置: {}, 当前第{}/{}页", mvType, config.getVideoSyncConfigName(), count.get() - 1, pages);

                    if (embeddedVideoInfo == null || embeddedVideoInfo.getInlist() == null || embeddedVideoInfo.getInlist().getA() == null || embeddedVideoInfo.getInlist().getA().isEmpty()) {
                        break;
                    }
                    EmbeddedVideoInfo.InlistDTO inlist = embeddedVideoInfo.getInlist();
                    List<String> names = inlist.getT();
                    List<Double> readPeople = inlist.getR();
                    List<Double> score = inlist.getD();
                    List<String> image = inlist.getI();
                    List<List<String>> q = inlist.getQ();
                    List yearAndType = inlist.getA();
                    for (int i = 0; i < names.size(); i++) {
                        log.info("从观影获取视频{}列表，配置: {}, 当前第{}/{}页", mvType, config.getVideoSyncConfigName(), count.get() - 1, pages);

                        List o = (List) yearAndType.get(i);
                        VideoInfo video = new VideoInfo();
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
                        video.setVideoCategory(mvType.toUpperCase());
                        video.setVideoPlatform(getProviderName());
                        video.setVideoQuality(Joiner.on(",").join(q.get(i)));
                        String dataId = image.get(i);
                        String cover = mvCoverUrl + "/%s/220.avif".formatted(dataId);
                        video.setVideoCover(cover + ",cover/%s/220.avif".formatted(dataId));
                        downloadCover(cover, "/%s/220.avif".formatted(dataId));
                        video.setVideoViews(BigDecimal.valueOf(Optional.ofNullable(readPeople.get(i)).orElse(-1D)));
                        video.setVideoStatus(0);
                        registerDetail(video, dataId, config);
                        registerDownload(video, dataId, config);
                        consumer.accept(video);
                    }
                }
            }
        } catch (Exception e) {
            log.error("从观影获取视频列表失败: {}", e.getMessage(), e);
        }

    }

    private void registerDownload(VideoInfo video, String dataId, VideoSyncConfig config) {
        //https://www.gyg.la/res/downurl/mv/mXr4
        EmbeddedDownload embeddedDownload = HttpClient.get(mvDownloadUrl + dataId)
                .cookie(config.getVideoSyncConfigCookie())
                .newInvoker()
                .execute()
                .content(EmbeddedDownload.class);

        List<VideoDownload> videoDownloads = new ArrayList<>();
        registerClDownload(videoDownloads, embeddedDownload.getDownlist());
        registerPanDownload(videoDownloads, embeddedDownload.getPanlist());

        video.setDownloadList(videoDownloads);
    }

    private void registerPanDownload(List<VideoDownload> videoDownloads, EmbeddedDownload.PanlistDTO panlist) {
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

    private void registerClDownload(List<VideoDownload> videoDownloads, EmbeddedDownload.DownlistDTO downlist) {
        try {
            EmbeddedDownload.DownlistDTO.ListDTO list = downlist.getList();
            EmbeddedDownload.DownlistDTO.TypeDTO type = downlist.getType();
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

    private void registerDetail(VideoInfo video, String dataId, VideoSyncConfig config) {
        String detailHtml = HttpClient.get(mvDetailUrl + dataId)
                .cookie(config.getVideoSyncConfigCookie())
                .newInvoker()
                .execute()
                .content(String.class);

        int i = detailHtml.indexOf("_obj.d=");
        if (i > 0) {
            registerDetailMoreBase(video, detailHtml.substring(i + 7, detailHtml.indexOf("};", i + 7) + 1));
        }

    }

    private void registerDetailMoreBase(VideoInfo video, String baseJson) {
        try {
            BaseDetail baseDetail = Json.fromJson(baseJson, BaseDetail.class);
            video.setVideoName(null == baseDetail.getName() ? null : baseDetail.getName().trim());
            video.setVideoDirector(Joiner.on(",").join(baseDetail.getDaoyan()));
            video.setVideoWriter(Joiner.on(",").join(baseDetail.getBianju()));
            video.setVideoActor(Joiner.on(",").join(baseDetail.getZhuyan()));
            video.setVideoCategory(Joiner.on(",").join(baseDetail.getLeixing()));
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

    private void downloadCover(String cover, String name) {
        File file1 = new File(file, name);
        FileUtils.forceMkdirParent(file1);
        try (InputStream is = new URL(cover).openStream()) {
            FileUtils.writeTo(is, file1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getDownloadUrls(VideoInfo videoInfo, VideoSyncConfig config) {
        log.info("获取YouTube视频下载地址: {}", videoInfo.getVideoTitle());
        List<String> downloadUrls = new ArrayList<>();

        try {
            // 实际实现中应该调用YouTube API获取下载地址
            // 这里使用模拟数据
            String videoUrl = videoInfo.getVideoUrl();
            if (videoUrl != null && !videoUrl.isEmpty()) {
                // 模拟不同清晰度的下载地址
                downloadUrls.add(videoUrl + "?quality=standard&source=youtube");  // 标清
                downloadUrls.add(videoUrl + "?quality=high&source=youtube");      // 高清
                downloadUrls.add(videoUrl + "?quality=super&source=youtube");     // 超清
            }
        } catch (Exception e) {
            log.error("获取YouTube视频下载地址失败: {}", e.getMessage(), e);
        }

        return downloadUrls;
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