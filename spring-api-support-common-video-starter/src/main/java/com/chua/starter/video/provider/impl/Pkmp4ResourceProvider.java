package com.chua.starter.video.provider.impl;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoDownload;
import com.chua.starter.video.entity.VideoPlayAddress;
import com.chua.starter.video.entity.VideoPlayAddressChannel;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
import com.chua.starter.video.pojo.VideoSyncProcessConfig;
import com.chua.starter.video.provider.SyncProvider;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * pkmp4.xyz (七味网) 资源提供者实现
 * <p>基于 MacCMS 架构的影视网站，支持电影、剧集、动漫、综艺等分类爬取</p>
 *
 * @author CH
 * @since 2025/9/16
 */
@Slf4j
@Spi("pkmp4")
public class Pkmp4ResourceProvider extends AbstractResourceProvider implements SyncProvider {

    private static final String BASE_URL = "https://www.pkmp4.xyz";
    private static final String SEARCH_URL = BASE_URL + "/vs/-------------.html";
    private static final int TIMEOUT = 15000;
    private static final int MIN_YEAR = 1960;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * 视频类型到站点分类ID的映射
     */
    private static final Map<String, Integer> TYPE_TO_CATEGORY = new LinkedHashMap<>();

    static {
        TYPE_TO_CATEGORY.put("MV", 1);    // 电影
        TYPE_TO_CATEGORY.put("TV", 2);    // 剧集
        TYPE_TO_CATEGORY.put("VAR", 3);   // 综艺
        TYPE_TO_CATEGORY.put("AC", 4);    // 动漫
        TYPE_TO_CATEGORY.put("DOC", 5);   // 纪录片 (假设)
    }

    /**
     * 站点分类ID到视频类型的映射
     */
    private static final Map<Integer, String> CATEGORY_TO_TYPE = TYPE_TO_CATEGORY.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    /**
     * 视频类型的中文名称
     */
    private static final Map<String, String> TYPE_CN = new LinkedHashMap<>();

    static {
        TYPE_CN.put("MV", "电影");
        TYPE_CN.put("TV", "剧集");
        TYPE_CN.put("VAR", "综艺");
        TYPE_CN.put("AC", "动漫");
    }

    /**
     * 影片详情页路径前缀
     */
    private static final String DETAIL_PATH_PREFIX = "/mv/";

    public Pkmp4ResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }

    // ==================== ResourceProvider 实现 ====================

    @Override
    public ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch) {
        String keyword = videoSearch.getKeyword();

        // 如果有分类信息，从分类页面获取数据
        String videoType = videoSearch.getVideoType();
        if (StringUtils.hasText(videoType) && TYPE_TO_CATEGORY.containsKey(videoType.toUpperCase())) {
            log.info("从 pkmp4.xyz 分类[{}]获取视频列表: type={}", videoType, keyword);
            return searchByCategory(videoSearch);
        }

        // 关键词搜索
        if (!StringUtils.hasText(keyword)) {
            return ReturnPageResultUtils.error("关键词不能为空");
        }

        log.info("从 pkmp4.xyz 检索视频: {}", keyword);
        List<VideoInfoResult> results = new ArrayList<>();

        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String searchUrl = SEARCH_URL + "?wd=" + encodedKeyword;

            Document doc = Jsoup.connect(searchUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .header("Referer", BASE_URL + "/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .get();

            // 检测是否触发了验证码
            if (isCaptchaPage(doc)) {
                log.warn("pkmp4.xyz 搜索触发验证码，尝试使用分类浏览方式");
                // 退回到分类浏览模式
                VideoSearch fallbackSearch = new VideoSearch();
                fallbackSearch.setVideoType("MV");
                fallbackSearch.setKeyword(keyword);
                fallbackSearch.setPage(videoSearch.getPage());
                fallbackSearch.setPageSize(videoSearch.getPageSize());
                return searchByCategory(fallbackSearch);
            }

            // 解析搜索结果列表
            Elements items = doc.select("ul.content-list > li");
            if (items.isEmpty()) {
                // 尝试通用选择器
                items = doc.select("li:has(.li-img), li:has(.li-bottom)");
            }

            for (Element item : items) {
                try {
                    VideoInfoResult video = parseListItem(item);
                    if (video != null) {
                        results.add(video);
                        if (results.size() >= 20) break;
                    }
                } catch (Exception e) {
                    log.warn("解析 pkmp4.xyz 搜索结果项失败", e);
                }
            }

            if (results.isEmpty()) {
                return ReturnPageResultUtils.error("pkmp4.xyz 未找到相关视频");
            }

            return ReturnPageResultUtils.ok(results, videoSearch.getPage(), videoSearch.getPageSize());
        } catch (Exception e) {
            log.error("pkmp4.xyz 资源检索失败: {}", e.getMessage(), e);
            // 异常时尝试分类浏览
            VideoSearch fallbackSearch = new VideoSearch();
            fallbackSearch.setVideoType("MV");
            fallbackSearch.setPage(videoSearch.getPage());
            fallbackSearch.setPageSize(videoSearch.getPageSize());
            return searchByCategory(fallbackSearch);
        }
    }

    /**
     * 按分类浏览视频
     */
    private ReturnPageResult<VideoInfoResult> searchByCategory(VideoSearch videoSearch) {
        String videoType = videoSearch.getVideoType();
        Integer categoryId = TYPE_TO_CATEGORY.get(videoType.toUpperCase());
        if (categoryId == null) {
            categoryId = 1; // 默认电影
        }

        int page = videoSearch.getPage() != null && videoSearch.getPage() > 0 ? videoSearch.getPage() : 1;
        String categoryUrl = BASE_URL + "/vt/" + categoryId + "-" + page + ".html";

        log.info("从 pkmp4.xyz 分类页面获取: {}", categoryUrl);
        List<VideoInfoResult> results = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(categoryUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .header("Referer", BASE_URL + "/")
                    .get();

            // 检测验证码
            if (isCaptchaPage(doc)) {
                log.warn("pkmp4.xyz 分类页面触发验证码: {}", categoryUrl);
                return ReturnPageResultUtils.error("触发验证码，请稍后重试");
            }

            Elements items = doc.select("ul.content-list > li");
            if (items.isEmpty()) {
                return ReturnPageResultUtils.ok(results, page, videoSearch.getPageSize());
            }

            for (Element item : items) {
                try {
                    VideoInfoResult video = parseListItem(item);
                    if (video != null) {
                        video.setVideoCategory(videoType.toUpperCase());
                        results.add(video);
                        if (results.size() >= 20) break;
                    }
                } catch (Exception e) {
                    log.warn("解析分类列表项失败", e);
                }
            }

            return ReturnPageResultUtils.ok(results, page, videoSearch.getPageSize());
        } catch (Exception e) {
            log.error("pkmp4.xyz 分类浏览失败: {}", e.getMessage(), e);
            return ReturnPageResultUtils.error("分类浏览失败: " + e.getMessage());
        }
    }

    /**
     * 根据详情页URL获取视频完整信息（播放地址、下载链接、描述等）
     *
     * @param detailUrl 详情页URL
     * @return 完整视频信息
     */
    public VideoInfoResult getVideoDetail(String detailUrl) {
        VideoInfoResult video = new VideoInfoResult();
        video.setVideoUrl(detailUrl);
        video.setVideoOriginId(extractVideoId(detailUrl));
        video.setVideoPlatform("pkmp4");
        enrichFromDetailPage(video, detailUrl);
        return video;
    }

    // ==================== SyncProvider 实现 ====================

    @Override
    public void fetchVideos(
            com.chua.starter.video.entity.VideoSyncConfig config,
            BiConsumer<VideoInfoResult, VideoSyncProcessConfig> consumer) {

        String videoSyncConfigStatus = config.getVideoSyncConfigStatus();
        if ("FINISH".equals(videoSyncConfigStatus) || "ERROR".equals(videoSyncConfigStatus)) {
            return;
        }

        // 获取需要同步的视频类型列表
        String[] videoTypes = getVideoTypes(config);
        int minYear = videoSource != null && videoSource.getVideoSourceMinYear() != null
                ? videoSource.getVideoSourceMinYear() : MIN_YEAR;

        for (String videoType : videoTypes) {
            Integer categoryId = TYPE_TO_CATEGORY.get(videoType.toUpperCase());
            if (categoryId == null) {
                log.warn("跳过不支持的视频类型: {}", videoType);
                continue;
            }

            try {
                crawlCategory(config, videoType, categoryId, minYear, consumer);
            } catch (Exception e) {
                log.error("同步分类[{}]失败: {}", videoType, e.getMessage(), e);
                config.setVideoSyncConfigStatus("ERROR");
                config.setVideoSyncConfigMessage(e.getMessage());
            }
        }
    }

    /**
     * 爬取指定分类的所有视频
     */
    private void crawlCategory(
            com.chua.starter.video.entity.VideoSyncConfig config,
            String videoType,
            int categoryId,
            int minYear,
            BiConsumer<VideoInfoResult, VideoSyncProcessConfig> consumer) {

        int currentPage = 1;
        int maxPages = Integer.MAX_VALUE;
        boolean hasMore = true;

        while (hasMore && currentPage <= maxPages) {
            // 检查同步状态
            String status = config.getVideoSyncConfigStatus();
            if ("FINISH".equals(status) || "ERROR".equals(status)) {
                break;
            }

            String pageUrl = BASE_URL + "/vt/" + categoryId + "-" + currentPage + ".html";
            log.info("同步 pkmp4.xyz [{}}] 第{}页", TYPE_CN.getOrDefault(videoType, videoType), currentPage);

            try {
                Document doc = Jsoup.connect(pageUrl)
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT)
                        .header("Referer", BASE_URL + "/")
                        .get();

                if (isCaptchaPage(doc)) {
                    log.warn("pkmp4.xyz 同步触发验证码，暂停: {}", pageUrl);
                    config.setVideoSyncConfigStatus("ERROR");
                    config.setVideoSyncConfigMessage("触发验证码");
                    break;
                }

                // 解析总页数
                if (currentPage == 1) {
                    maxPages = parseTotalPages(doc);
                }

                Elements items = doc.select("ul.content-list > li");
                if (items.isEmpty()) {
                    log.info("pkmp4.xyz [{}}] 没有更多数据", videoType);
                    break;
                }

                int totalInPage = items.size();
                int pageIdx = 0;

                for (Element item : items) {
                    pageIdx++;
                    String statusCheck = config.getVideoSyncConfigStatus();
                    if ("FINISH".equals(statusCheck) || "ERROR".equals(statusCheck)) {
                        break;
                    }

                    try {
                        VideoInfoResult video = parseListItem(item);
                        if (video == null) continue;

                        video.setVideoCategory(videoType.toUpperCase());
                        video.setVideoPlatform("pkmp4");

                        // 尝试获取详情页的完整信息（下载链接、播放地址等）
                        String detailUrl = video.getVideoUrl();
                        if (StringUtils.hasText(detailUrl) && detailUrl.contains(DETAIL_PATH_PREFIX)) {
                            try {
                                enrichFromDetailPage(video, detailUrl);
                            } catch (Exception e) {
                                log.warn("获取详情页信息失败: {}", detailUrl, e);
                            }
                        }

                        // 反馈给消费者
                        if (consumer != null) {
                            consumer.accept(video, VideoSyncProcessConfig.builder()
                                    .videoType(videoType)
                                    .totalPages(maxPages)
                                    .page(currentPage)
                                    .currentPageInPage(pageIdx)
                                    .totalInPage(totalInPage)
                                    .build());
                        }

                        // 短暂休眠，避免请求过快
                        Thread.sleep(200);
                    } catch (Exception e) {
                        log.warn("处理视频项失败", e);
                    }
                }

                // 更新同步进度
                config.setVideoSyncConfigLastLog("同步 pkmp4.xyz [" + videoType + "] 第" + currentPage + "/" + maxPages + "页");
                config.setVideoSyncConfigLastOffset("{\"type\":\"" + videoType + "\",\"page\":" + currentPage + ",\"categoryId\":" + categoryId + "}");

                currentPage++;
                // 页间延迟
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("同步 pkmp4.xyz [{}}] 第{}页失败: {}", videoType, currentPage, e.getMessage(), e);
                config.setVideoSyncConfigStatus("ERROR");
                config.setVideoSyncConfigMessage("第" + currentPage + "页失败: " + e.getMessage());
                break;
            }
        }

        if (currentPage > maxPages) {
            log.info("pkmp4.xyz [{}}] 同步完成", videoType);
        }
    }

    /**
     * 获取需要同步的视频类型列表
     */
    private String[] getVideoTypes(com.chua.starter.video.entity.VideoSyncConfig config) {
        String offset = config.getVideoSyncConfigLastOffset();
        if (StringUtils.hasText(offset) && offset.contains("\"type\"")) {
            // 从偏移量中提取当前类型，然后返回剩余类型
            String currentType = extractJsonValue(offset, "type");
            if (currentType != null) {
                // 获取该类型之后的所有类型
                return getTypesAfter(currentType.toUpperCase());
            }
        }
        // 默认获取所有启用的类型
        if (videoSource != null) {
            return videoSource.getVideoSourceTypeForList();
        }
        return new String[]{"MV", "TV", "AC"};
    }

    /**
     * 获取指定类型之后的所有类型
     */
    private String[] getTypesAfter(String type) {
        List<String> allTypes = new ArrayList<>(TYPE_TO_CATEGORY.keySet());
        int index = allTypes.indexOf(type.toUpperCase());
        if (index < 0) return allTypes.toArray(new String[0]);
        return allTypes.subList(index, allTypes.size()).toArray(new String[0]);
    }

    // ==================== 页面解析方法 ====================

    /**
     * 解析列表中的单个视频项
     */
    private VideoInfoResult parseListItem(Element item) {
        Element link = item.selectFirst(".li-img a[href]");
        if (link == null) {
            link = item.selectFirst("a[href*=" + DETAIL_PATH_PREFIX + "]");
        }
        if (link == null) return null;

        String href = link.attr("abs:href");
        String title = link.attr("title");
        if (!StringUtils.hasText(title)) {
            title = link.text().trim();
        }
        if (!StringUtils.hasText(title)) return null;

        // 封面图
        Element img = item.selectFirst(".li-img img");
        String cover = img != null ? img.attr("abs:src") : "";

        // 提取视频ID
        String videoId = extractVideoId(href);

        // 评分
        Element scoreEl = item.selectFirst(".li-bottom h3 span");
        String scoreText = scoreEl != null ? scoreEl.text().trim() : "";

        // 元数据标签
        Element tagEl = item.selectFirst(".li-bottom .tag");
        String tagText = tagEl != null ? tagEl.text().trim() : "";
        String[] tagParts = tagText.split("/", 4);
        // tagParts[0] = year, tagParts[1] = region, tagParts[2] = genre, tagParts[3] = language

        VideoInfoResult video = new VideoInfoResult();
        video.setVideoName(title);
        video.setVideoTitle(title);
        video.setVideoAliasName(title);
        video.setVideoCover(cover);
        video.setVideoUrl(href);
        video.setVideoOriginId(videoId);
        video.setVideoPlatform("pkmp4");
        video.setVideoStatus(0);
        video.setCreateTime(LocalDateTime.now());
        video.setUpdateTime(LocalDateTime.now());

        // 评分
        if (StringUtils.hasText(scoreText) && !"N/A".equals(scoreText)) {
            try {
                video.setVideoScore(new BigDecimal(scoreText));
            } catch (Exception ignored) {
            }
        }

        // 年份、地区、类型、语言
        if (tagParts.length >= 1) {
            String yearStr = tagParts[0].trim();
            if (StringUtils.hasText(yearStr)) {
                try {
                    video.setVideoYear(Integer.parseInt(yearStr));
                } catch (Exception ignored) {
                }
            }
        }
        if (tagParts.length >= 2) {
            video.setVideoDistrict(tagParts[1].trim());
        }
        if (tagParts.length >= 3) {
            video.setVideoType(tagParts[2].trim());
            video.setTags(tagParts[2].trim());
        }
        if (tagParts.length >= 4) {
            video.setVideoLanguage(tagParts[3].trim());
        }

        return video;
    }

    /**
     * 从详情页获取完整信息（播放地址、下载链接、描述等）
     */
    private void enrichFromDetailPage(VideoInfoResult video, String detailUrl) {
        try {
            Document doc = Jsoup.connect(detailUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .header("Referer", BASE_URL + "/")
                    .get();

            if (isCaptchaPage(doc)) return;

            // 标题（详情页的标题更完整）
            Element titleEl = doc.selectFirst(".main-ui-meta h1");
            if (titleEl != null) {
                String fullTitle = titleEl.text().trim();
                // 移除年份后缀
                fullTitle = fullTitle.replaceAll("\\(\\d{4}\\)", "").trim();
                video.setVideoTitle(fullTitle);
                video.setVideoName(fullTitle);
            }

            // 年份
            Element yearEl = doc.selectFirst(".main-ui-meta h1 span.year");
            if (yearEl != null && video.getVideoYear() == null) {
                try {
                    String yearText = yearEl.text().trim().replaceAll("[()]", "");
                    video.setVideoYear(Integer.parseInt(yearText));
                } catch (Exception ignored) {
                }
            }

            // 导演
            Element directorEl = doc.selectFirst("div:has(> span:containsOwn(导演：))");
            if (directorEl != null) {
                List<String> directors = directorEl.select("a").eachText();
                video.setVideoDirector(String.join(",", directors));
            }

            // 编剧
            Element writerEl = doc.selectFirst("div:has(> span:containsOwn(编剧：))");
            if (writerEl != null) {
                List<String> writers = writerEl.select("a").eachText();
                video.setVideoWriter(String.join(",", writers));
            }

            // 主演
            Element actorEl = doc.selectFirst("div:has(> span:containsOwn(主演：))");
            if (actorEl != null) {
                List<String> actors = actorEl.select("a").eachText();
                video.setVideoActor(String.join(",", actors));
            }

            // 类型
            Element typeEl = doc.selectFirst("div:has(> span:containsOwn(类型：))");
            if (typeEl != null) {
                List<String> types = typeEl.select("a").eachText();
                String typeStr = String.join(",", types);
                if (StringUtils.hasText(typeStr)) {
                    video.setVideoType(typeStr);
                    video.setTags(typeStr);
                }
            }

            // 地区
            Element regionEl = doc.selectFirst("div:has(> span:containsOwn(地区：))");
            if (regionEl != null) {
                List<String> regions = regionEl.select("a").eachText();
                video.setVideoDistrict(String.join(",", regions));
            }

            // 语言
            Element langEl = doc.selectFirst("div:has(> span:containsOwn(语言：))");
            if (langEl != null) {
                List<String> langs = langEl.select("a").eachText();
                video.setVideoLanguage(String.join(",", langs));
            }

            // 上映日期
            Element releaseEl = doc.selectFirst("div:has(> span:containsOwn(上映：))");
            if (releaseEl != null) {
                String releaseText = releaseEl.text().replace("上映：", "").trim();
                video.setVideoRelease(releaseText);
            }

            // 片长
            Element durationEl = doc.selectFirst("div:has(> span:containsOwn(片长：))");
            if (durationEl != null) {
                String durationText = durationEl.text().replace("片长：", "").trim();
                video.setVideoDuration(parseDuration(durationText));
                video.setVideoSize(durationText);
            }

            // 又名
            Element aliasEl = doc.selectFirst("div:has(> span:containsOwn(又名：))");
            if (aliasEl != null) {
                String aliasText = aliasEl.text().replace("又名：", "").trim();
                if (StringUtils.hasText(aliasText)) {
                    video.setVideoAliasName(aliasText);
                }
            }

            // 简介
            Element descEl = doc.selectFirst(".movie-introduce p.zkjj_a");
            if (descEl != null) {
                String desc = descEl.text().trim();
                // 移除"[展开全部]"
                desc = desc.replace("[展开全部]", "").trim();
                video.setVideoDescription(desc);
            }

            // 评分（豆瓣/IMDB）
            Element doubanEl = doc.selectFirst("a[href*=douban] span");
            if (doubanEl != null) {
                try {
                    String scoreStr = doubanEl.text().trim().replaceAll("[^\\d.]", "");
                    if (StringUtils.hasText(scoreStr)) {
                        video.setVideoScore(new BigDecimal(scoreStr));
                    }
                } catch (Exception ignored) {
                }
            }

            // ============ 解析在线播放地址 ============
            List<VideoPlayAddress> playAddresses = parsePlayAddresses(doc);
            if (!playAddresses.isEmpty()) {
                video.setPlayAddressesList(playAddresses);
            }

            // ============ 解析下载链接 ============
            List<VideoDownload> downloadList = parseDownloadLinks(doc);
            if (!downloadList.isEmpty()) {
                video.setDownloadList(downloadList);
            }

        } catch (Exception e) {
            log.warn("解析详情页失败: {}", detailUrl, e);
        }
    }

    /**
     * 解析在线播放地址
     */
    private List<VideoPlayAddress> parsePlayAddresses(Document doc) {
        List<VideoPlayAddress> result = new ArrayList<>();
        Elements playerTabs = doc.select("ul.player.ckp");

        int playerIndex = 0;
        for (Element playerUl : playerTabs) {
            playerIndex++;
            Elements links = playerUl.select("li a[href]");
            if (links.isEmpty()) continue;

            String playerName = "播放源" + playerIndex;

            VideoPlayAddress playAddress = new VideoPlayAddress();
            playAddress.setVideoPlayAddressName(playerName);
            playAddress.setVideoPlayAddressCode("player_" + playerIndex);

            List<VideoPlayAddressChannel> channels = new ArrayList<>();
            for (Element link : links) {
                String playUrl = link.attr("abs:href");
                String quality = link.text().trim();
                if (!StringUtils.hasText(playUrl)) continue;

                VideoPlayAddressChannel channel = new VideoPlayAddressChannel();
                channel.setVideoPlayAddressChannelName(quality);
                channel.setVideoPlayAddressUrl(playUrl);
                channels.add(channel);
            }

            playAddress.setVideoPlayAddressChannels(channels);
            result.add(playAddress);
        }

        return result;
    }

    /**
     * 解析下载链接（磁力链接）
     */
    private List<VideoDownload> parseDownloadLinks(Document doc) {
        List<VideoDownload> result = new ArrayList<>();

        // 每个下载tab
        Elements tabTitles = doc.select(".down-link .hd .tab-title .title");
        Elements tabContents = doc.select(".down-link .down-list ul.gdt.content");

        int tabIndex = 0;
        for (Element tabContent : tabContents) {
            String quality = "";
            if (tabIndex < tabTitles.size()) {
                quality = tabTitles.get(tabIndex).text().trim();
            }
            tabIndex++;

            Elements items = tabContent.select("li.down-list2");
            for (Element item : items) {
                try {
                    // 文件名
                    Element nameLink = item.selectFirst("p.down-list3 a.folder, p.down-list3 a[title]");
                    String fileName = nameLink != null ? nameLink.attr("title") : "";
                    if (!StringUtils.hasText(fileName)) {
                        fileName = nameLink != null ? nameLink.text().trim() : "";
                    }
                    if (!StringUtils.hasText(fileName)) continue;

                    // 磁力链接
                    Element magnetLink = item.selectFirst("a.url-1[href^=magnet:]");
                    String magnetUrl = magnetLink != null ? magnetLink.attr("href") : "";

                    // 文件大小 - 从文件名中提取 [1.02G] 或 [3.96G]
                    String fileSize = extractFileSize(fileName);

                    VideoDownload download = new VideoDownload();
                    download.setVideoDownloadName(fileName);
                    download.setVideoDownloadUrl(magnetUrl);
                    download.setVideoDownloadMagnetic(magnetUrl);
                    download.setVideoDownloadType("磁力资源");
                    download.setVideoDownloadQuality(quality);
                    download.setVideoDownloadSize(fileSize);
                    download.setVideoDownloadPlatform("pkmp4");
                    download.setVideoDownloadStatus((byte) 1);

                    result.add(download);
                } catch (Exception ignored) {
                }
            }
        }

        return result;
    }

    /**
     * 从列表页的链接中提取视频ID
     */
    private String extractVideoId(String url) {
        if (url == null) return null;
        Pattern p = Pattern.compile("/mv/(\\d+)\\.html");
        Matcher m = p.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    /**
     * 解析总页数
     */
    private int parseTotalPages(Document doc) {
        Elements pageLinks = doc.select("div.pages a");
        int maxPage = 1;
        for (Element link : pageLinks) {
            String text = link.text().trim();
            try {
                int pageNum = Integer.parseInt(text);
                if (pageNum > maxPage) maxPage = pageNum;
            } catch (Exception ignored) {
            }
        }
        return maxPage;
    }

    /**
     * 从文件名中提取文件大小
     */
    private String extractFileSize(String fileName) {
        if (fileName == null) return "";
        Pattern p = Pattern.compile("\\[(\\d+\\.?\\d*[GMK]B?)\\]");
        Matcher m = p.matcher(fileName);
        return m.find() ? m.group(1) : "";
    }

    /**
     * 解析时长字符串为分钟数
     */
    private Integer parseDuration(String durationText) {
        if (!StringUtils.hasText(durationText)) return null;
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(durationText);
        if (m.find()) {
            try {
                int val = Integer.parseInt(m.group(1));
                // 可能是分钟也可能是小时制 "156分钟" → 156, "1小时30分" → 90
                if (durationText.contains("小时")) {
                    int hours = val;
                    int minutes = 0;
                    Matcher mm = Pattern.compile("(\\d+)分钟").matcher(durationText);
                    if (mm.find()) {
                        minutes = Integer.parseInt(mm.group(1));
                    }
                    return hours * 60 + minutes;
                }
                return val;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * JSON字符串中提取指定key的value（简易实现，不依赖JSON库）
     */
    private String extractJsonValue(String json, String key) {
        if (!StringUtils.hasText(json)) return null;
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    /**
     * 检测页面是否为验证码页面
     */
    private boolean isCaptchaPage(Document doc) {
        String title = doc.title().toLowerCase();
        return title.contains("安全验证") || title.contains("verify")
                || doc.select("input[name=verify]").size() > 0;
    }
}
