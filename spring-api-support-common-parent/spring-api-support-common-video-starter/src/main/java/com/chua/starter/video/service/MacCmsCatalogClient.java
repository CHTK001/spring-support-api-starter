// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.video.service;

import com.chua.common.support.datasearch.video.model.VideoDownload;
import com.chua.common.support.datasearch.video.model.VideoInfoResult;
import com.chua.common.support.datasearch.video.model.VideoPlayAddress;
import com.chua.common.support.datasearch.video.model.VideoPlayAddressChannel;
import com.chua.starter.video.properties.VideoProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * MacCMS 兼容资源站采集客户端。
 *
 * <p>MacCMS（苹果 CMS）v10 采集协议是影视资源站领域最通用的 JSON 协议，其
 * {@code ac=videolist} 支持无关键词的最新资源分页浏览，正好弥补
 * {@code utils-support-datasearch-starter} 中视频 SPI「必须携带关键词」的空白，
 * 用于首页与频道页的分类浏览；{@code ac=detail} 则用于按 originId 查询详情。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
public class MacCmsCatalogClient {

    /**
     * JSON 解析器。
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 时间解析格式。
     */
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 视频配置。
     */
    private final VideoProperties properties;

    /**
     * HTTP 客户端（复用连接）。
     */
    private final HttpClient httpClient;

    /**
     * 采集接口地址提供者（支持运行时从配置数据库解析）。
     */
    private final Supplier<String> catalogUrlSupplier;

    /**
     * 构造 MacCMS 采集客户端（采集地址取配置）。
     *
     * @param properties 视频服务配置
     */
    public MacCmsCatalogClient(VideoProperties properties) {
        this(properties, properties::getCatalogUrl);
    }

    /**
     * 构造 MacCMS 采集客户端。
     *
     * @param properties         视频服务配置
     * @param catalogUrlSupplier 采集接口地址提供者
     */
    public MacCmsCatalogClient(VideoProperties properties, Supplier<String> catalogUrlSupplier) {
        this.properties = properties;
        this.catalogUrlSupplier = catalogUrlSupplier == null ? properties::getCatalogUrl : catalogUrlSupplier;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, properties.getConnectTimeout())))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * 是否已配置可用的采集接口地址。
     *
     * @return 已配置返回 true
     */
    public boolean available() {
        String url = catalogUrlSupplier.get();
        return url != null && !url.isBlank();
    }

    /**
     * 分类浏览（无关键词时拉取最新资源）。
     *
     * @param keyword 关键词，可空
     * @param page    页码，从 1 开始
     * @return 视频条目列表，失败返回空列表
     */
    public List<VideoInfoResult> list(String keyword, int page) {
        if (!available()) {
            return List.of();
        }
        try {
            String url = buildListUrl(keyword, page);
            JsonNode root = get(url);
            JsonNode listNode = root.path("list");
            List<VideoInfoResult> results = new ArrayList<>();
            if (listNode.isArray()) {
                for (JsonNode item : listNode) {
                    results.add(toInfo(item));
                }
            }
            return results;
        } catch (Exception e) {
            log.warn("[Video] MacCMS 采集失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 按 originId 查询视频详情（含播放地址与下载地址）。
     *
     * @param id 视频 originId
     * @return 视频详情，未命中返回 null
     */
    public VideoInfoResult detail(String id) {
        if (!available() || id == null || id.isBlank()) {
            return null;
        }
        try {
            String url = normalizeBase(catalogUrlSupplier.get())
                    + "?ac=detail&ids=" + URLEncoder.encode(id, StandardCharsets.UTF_8);
            JsonNode root = get(url);
            JsonNode listNode = root.path("list");
            if (listNode.isArray() && !listNode.isEmpty()) {
                return toInfo(listNode.get(0));
            }
        } catch (Exception e) {
            log.warn("[Video] MacCMS 详情查询失败 id={}: {}", id, e.getMessage());
        }
        return null;
    }

    /**
     * 构建列表请求地址。
     *
     * @param keyword 关键词，可空
     * @param page    页码
     * @return 完整请求地址
     */
    private String buildListUrl(String keyword, int page) {
        StringBuilder url = new StringBuilder(normalizeBase(catalogUrlSupplier.get()))
                .append("?ac=videolist")
                .append("&pg=").append(Math.max(1, page));
        if (keyword != null && !keyword.isBlank()) {
            url.append("&wd=").append(URLEncoder.encode(keyword, StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    /**
     * 规范化采集接口地址（去除末尾问号）。
     *
     * @param base 原始地址
     * @return 规范化地址
     */
    private String normalizeBase(String base) {
        String url = base == null ? "" : base.trim();
        while (url.endsWith("?")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * 发起 GET 请求并解析 JSON。
     *
     * @param url 请求地址
     * @return JSON 根节点
     * @throws Exception 请求或解析失败
     */
    private JsonNode get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(Math.max(1000, properties.getReadTimeout())))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return OBJECT_MAPPER.readTree(response.body());
    }

    /**
     * MacCMS 条目 → {@link VideoInfoResult} 映射。
     *
     * @param item MacCMS 单条 JSON
     * @return 视频信息
     */
    private VideoInfoResult toInfo(JsonNode item) {
        VideoInfoResult info = new VideoInfoResult();
        String id = item.path("vod_id").asText("");
        info.setVideoId(parseInt(id));
        info.setVideoOriginId(id);
        info.setVideoTitle(item.path("vod_name").asText(""));
        info.setVideoName(item.path("vod_name").asText(""));
        info.setVideoAliasName(item.path("vod_sub").asText(""));
        info.setVideoCover(item.path("vod_pic").asText(""));
        info.setVideoThumbnail(item.path("vod_pic_thumb").asText(""));
        info.setVideoLanguage(item.path("vod_lang").asText(""));
        info.setVideoDirector(item.path("vod_director").asText(""));
        info.setVideoActor(item.path("vod_actor").asText(""));
        info.setVideoWriter(item.path("vod_writer").asText(""));
        info.setVideoDescription(item.path("vod_content").asText(""));
        info.setVideoDistrict(item.path("vod_area").asText(""));
        info.setVideoYear(parseYear(item.path("vod_year").asText("")));
        info.setVideoScore(parseScore(item.path("vod_score").asText("")));
        info.setVideoRelease(item.path("vod_pubdate").asText(""));
        info.setVideoRemark(item.path("vod_remarks").asText(""));
        info.setVideoPlatform("maccms");
        info.setVideoPublishDate(parseTime(item.path("vod_time").asText("")));
        String typeName = item.path("type_name").asText(item.path("vod_class").asText(""));
        info.setVideoType(typeName);
        info.setVideoCategory(mapCategory(typeName));
        info.setTags(item.path("vod_class").asText(typeName));

        // 播放地址：vod_play_from 与 vod_play_url 以 $$$ 分隔多线路，线路内 # 分隔剧集
        List<VideoPlayAddress> playAddresses = parsePlayAddresses(
                item.path("vod_play_from").asText(""),
                item.path("vod_play_url").asText(""));
        info.setPlayAddressesList(playAddresses);
        info.setVideoUrl(firstPlayUrl(playAddresses));

        // 下载地址：vod_down_from 与 vod_down_url 以 $$$ 分隔多平台
        info.setDownloadList(parseDownloads(
                item.path("vod_down_from").asText(""),
                item.path("vod_down_url").asText("")));
        return info;
    }

    /**
     * 解析播放地址线路。
     *
     * @param from 线路名串（$$$ 分隔）
     * @param play 播放地址串（$$$ 分隔线路，# 分隔剧集）
     * @return 播放线路列表
     */
    private List<VideoPlayAddress> parsePlayAddresses(String from, String play) {
        List<VideoPlayAddress> result = new ArrayList<>();
        if (play == null || play.isBlank()) {
            return result;
        }
        String[] lines = play.split("\\$\\$\\$");
        String[] names = from == null || from.isBlank() ? new String[0] : from.split("\\$\\$\\$");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line == null || line.isBlank()) {
                continue;
            }
            VideoPlayAddress address = new VideoPlayAddress();
            address.setVideoPlayAddressName(i < names.length && !names[i].isBlank()
                    ? names[i] : "线路" + (i + 1));
            address.setVideoPlayAddressCode("line" + (i + 1));
            List<VideoPlayAddressChannel> episodes = new ArrayList<>();
            for (String episode : line.split("#")) {
                if (episode == null || episode.isBlank()) {
                    continue;
                }
                int idx = episode.indexOf('$');
                VideoPlayAddressChannel channel = new VideoPlayAddressChannel();
                if (idx >= 0) {
                    channel.setVideoPlayAddressChannelName(episode.substring(0, idx).trim());
                    channel.setVideoPlayAddressUrl(episode.substring(idx + 1).trim());
                } else {
                    channel.setVideoPlayAddressChannelName("正片");
                    channel.setVideoPlayAddressUrl(episode.trim());
                }
                episodes.add(channel);
            }
            address.setVideoPlayAddressChannels(episodes);
            result.add(address);
        }
        return result;
    }

    /**
     * 取首个可播放地址。
     *
     * @param addresses 播放线路
     * @return 首个地址，无则空串
     */
    private String firstPlayUrl(List<VideoPlayAddress> addresses) {
        for (VideoPlayAddress address : addresses) {
            List<VideoPlayAddressChannel> channels = address.getVideoPlayAddressChannels();
            if (channels == null) {
                continue;
            }
            for (VideoPlayAddressChannel channel : channels) {
                String url = channel.getVideoPlayAddressUrl();
                if (url != null && !url.isBlank()) {
                    return url;
                }
            }
        }
        return "";
    }

    /**
     * 解析下载地址。
     *
     * @param from 平台名串（$$$ 分隔）
     * @param down 下载地址串（$$$ 分隔平台，平台内 # 或换行分隔条目）
     * @return 下载项列表
     */
    private List<VideoDownload> parseDownloads(String from, String down) {
        List<VideoDownload> result = new ArrayList<>();
        if (down == null || down.isBlank()) {
            return result;
        }
        String[] groups = down.split("\\$\\$\\$");
        String[] platforms = from == null || from.isBlank() ? new String[0] : from.split("\\$\\$\\$");
        for (int i = 0; i < groups.length; i++) {
            String group = groups[i];
            if (group == null || group.isBlank()) {
                continue;
            }
            String platform = i < platforms.length && !platforms[i].isBlank()
                    ? platforms[i] : "直链";
            for (String entry : group.split("[#\\n]")) {
                if (entry == null || entry.isBlank()) {
                    continue;
                }
                int idx = entry.indexOf('$');
                VideoDownload download = new VideoDownload();
                download.setVideoDownloadPlatform(platform);
                download.setVideoDownloadType("pan");
                if (idx >= 0) {
                    download.setVideoDownloadName(entry.substring(0, idx).trim());
                    download.setVideoDownloadUrl(entry.substring(idx + 1).trim());
                } else {
                    download.setVideoDownloadName(platform);
                    download.setVideoDownloadUrl(entry.trim());
                }
                result.add(download);
            }
        }
        return result;
    }

    /**
     * 分类名映射为标准分类码（与前端 movie/tv/anime/variety/documentary 对齐）。
     *
     * @param typeName 资源站原始分类
     * @return 标准分类码
     */
    private String mapCategory(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            return "movie";
        }
        if (typeName.contains("剧") || typeName.contains("连续")) {
            return "tv";
        }
        if (typeName.contains("动漫") || typeName.contains("动画") || typeName.contains("番")) {
            return "anime";
        }
        if (typeName.contains("综艺") || typeName.contains("真人秀")) {
            return "variety";
        }
        if (typeName.contains("纪录")) {
            return "documentary";
        }
        return "movie";
    }

    /**
     * 解析年份。
     *
     * @param text 年份文本
     * @return 年份，解析失败返回 null
     */
    private Integer parseYear(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String digits = text.replaceAll("\\D", "");
        if (digits.length() >= 4) {
            try {
                return Integer.parseInt(digits.substring(0, 4));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * 解析评分。
     *
     * @param text 评分文本
     * @return 评分，解析失败返回 null
     */
    private BigDecimal parseScore(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * 解析整数。
     *
     * @param text 数字文本
     * @return 整数，解析失败返回 null
     */
    private Integer parseInt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * 解析发布时间。
     *
     * @param text 时间文本
     * @return 时间，解析失败返回 null
     */
    private LocalDateTime parseTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text.trim(), TIME_FORMAT);
        } catch (Exception ignored) {
            return null;
        }
    }
}