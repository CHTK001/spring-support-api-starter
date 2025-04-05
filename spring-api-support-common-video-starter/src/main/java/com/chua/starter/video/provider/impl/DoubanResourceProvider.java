package com.chua.starter.video.provider.impl;

import com.chua.common.support.converter.Converter;
import com.chua.common.support.http.HttpClient;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.provider.ResourceProvider;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 豆瓣资源提供者实现
 *
 * @author CH
 */
@Component
@Slf4j
public class DoubanResourceProvider implements ResourceProvider {

    private static final String DOUBAN_SEARCH_URL = "https://www.douban.com/search?cat=1002&q=";
    private static final int TIMEOUT = 10000; // 10秒超时
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    @Override
    public ReturnResult<List<VideoInfo>> searchResource(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return ReturnResult.error("关键词不能为空");
        }

        log.info("从豆瓣资源检索视频: {}", keyword);
        List<VideoInfo> results = new ArrayList<>();

        try {
            // URL编码关键词
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String searchUrl = DOUBAN_SEARCH_URL + encodedKeyword;

            // 发送HTTP请求获取搜索结果页面
            Document document = Jsoup.connect(searchUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();

            // 解析搜索结果
            Elements items = document.select("div.result");

            for (Element item : items) {
                try {
                    Element titleElement = item.selectFirst("div.title h3 a");
                    if (titleElement == null) continue;

                    String title = titleElement.text().trim();
                    String detailUrl = titleElement.attr("href");

                    Element ratingElement = item.selectFirst("div.rating_nums");
                    String rating = ratingElement != null ? ratingElement.text().trim() : "暂无评分";

                    Element descElement = item.selectFirst("div.content p");
                    String description = descElement != null ? descElement.text().trim() : "";

                    Element coverElement = item.selectFirst("div.pic img");
                    String coverUrl = coverElement != null ? coverElement.attr("src") : "";

                    // 获取详情页面以提取更多信息
                    Document detailDoc = null;
                    try {
                        // 添加延迟，避免请求过快被封
                        TimeUnit.MILLISECONDS.sleep(500);
                        String html = HttpClient
                                .get(detailUrl)
                                .newInvoker()
                                .execute()
                                .content(String.class);
                        String regex = "window\\.location\\.replace\\('(https?://[^']*)'\\)";

                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(html);

                        if (!matcher.find()) {
                            return ReturnResult.ok(Collections.emptyList());
                        }
                        String url = matcher.group(1);
                        detailDoc = Jsoup.connect(url)
                                .userAgent(USER_AGENT)
                                .timeout(TIMEOUT)
                                .get();
                    } catch (Exception e) {
                        log.warn("获取豆瓣详情页面失败: {}", detailUrl, e);
                    }

                    String director = "";
                    String writer = "";
                    String actors = "";
                    String year = "";
                    String type = "";
                    String area = "";
                    String language = "";
                    String alias = "";

                    if (detailDoc != null) {
                        try {
                            rating = detailDoc.expectFirst("#interest_sectl > div.rating_wrap.clearbox > div.rating_self.clearfix > strong").text();

                        } catch (Exception ignored) {
                        }
                        Element infoElement = detailDoc.selectFirst("div#info");
                        if (infoElement != null) {
                            String infoText = infoElement.text();

                            // 提取导演
                            if (infoText.contains("导演:")) {
                                director = infoText.substring(infoText.indexOf("导演:") + 3);
                                if (director.contains("编剧:")) {
                                    director = director.substring(0, director.indexOf("编剧:")).trim();
                                } else if (director.contains("主演:")) {
                                    director = director.substring(0, director.indexOf("主演:")).trim();
                                }
                            }
                            if (infoText.contains("编剧:")) {
                                writer = infoText.substring(infoText.indexOf("编剧:") + 3);
                                if (writer.contains("主演:")) {
                                    writer = writer.substring(0, writer.indexOf("主演:")).trim();
                                }
                            }

                            // 提取主演
                            if (infoText.contains("主演:")) {
                                actors = infoText.substring(infoText.indexOf("主演:") + 3);
                                if (actors.contains("类型:")) {
                                    actors = actors.substring(0, actors.indexOf("类型:")).trim();
                                }
                            }

                            // 提取年份
                            if (infoText.contains("上映日期:")) {
                                year = infoText.substring(infoText.indexOf("上映日期:") + 5);
                                if (year.contains("片长:")) {
                                    year = year.substring(0, year.indexOf("片长:")).trim();
                                }
                                // 提取年份
                                if (year.contains("(")) {
                                    year = year.substring(0, year.indexOf("(")).trim();
                                }
                            }

                            // 提取类型
                            if (infoText.contains("类型:")) {
                                type = infoText.substring(infoText.indexOf("类型:") + 3);
                                if (type.contains("制片国家/地区:")) {
                                    type = type.substring(0, type.indexOf("制片国家/地区:")).trim();
                                }
                            }
                            if (infoText.contains("制片国家/地区:")) {
                                area = infoText.substring(infoText.indexOf("制片国家/地区:") + 8);
                                if (area.contains("语言:")) {
                                    area = area.substring(0, area.indexOf("语言:")).trim();
                                }
                            }

                            if (infoText.contains("语言:")) {
                                int i = infoText.indexOf("语言:");
                                language = infoText.substring(i + 3, infoText.indexOf("上映日期", i + 1));
                            }

                            if (infoText.contains("又名:")) {
                                int i = infoText.indexOf("又名:");
                                alias = infoText.substring(i + 3, infoText.indexOf("IMDb", i + 1));
                            }

                        }
                    }

                    // 创建VideoInfo对象
                    VideoInfo videoInfo = new VideoInfo();
                    videoInfo.setVideoName(title);
                    videoInfo.setVideoAliasName(title);
                    videoInfo.setVideoDirector(Joiner.on(",").join(director.split("/")));
                    videoInfo.setVideoActor(Joiner.on(",").join(actors.split("/")));
                    videoInfo.setVideoType(type);
                    videoInfo.setVideoDistrict(Joiner.on(",").join(area.split("/")));
                    videoInfo.setVideoWriter(Joiner.on(",").join(writer.split("/")));
                    videoInfo.setVideoLanguage(language);
//                    videoInfo.setVideoPlatform("豆瓣");
                    videoInfo.setVideoAliasName(alias);
                    videoInfo.setVideoDescription(description);
                    videoInfo.setVideoScore(Converter.toBigDecimal(rating));
                    videoInfo.setVideoCover(coverUrl);
                    if (year.contains("-")) {
                        videoInfo.setVideoYear(Converter.convertIfNecessary(year, LocalDateTime.class).getYear());
                    } else {
                        videoInfo.setVideoYear(Converter.convertIfNecessary(year, Integer.class));
                    }
                    videoInfo.setVideoRelease(year);
                    videoInfo.setCreateTime(LocalDateTime.now());
                    videoInfo.setUpdateTime(LocalDateTime.now());

                    results.add(videoInfo);

                    // 限制结果数量，避免过多请求
                    if (results.size() >= 10) {
                        break;
                    }
                } catch (Exception e) {
                    log.warn("解析豆瓣搜索结果项失败", e);
                }
            }

            if (results.isEmpty()) {
                return ReturnResult.error("豆瓣资源未找到相关视频");
            }

            return ReturnResult.success(results);
        } catch (IOException e) {
            log.error("豆瓣资源检索失败", e);
            return ReturnResult.error("豆瓣资源检索失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("豆瓣资源检索过程中发生未知错误", e);
            return ReturnResult.error("豆瓣资源检索失败: " + e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "豆瓣资源";
    }

    @Override
    public int getPriority() {
        return 1; // 优先级较高
    }

    @Override
    public boolean isAvailable() {
        try {
            // 简单检查豆瓣网站是否可访问
            Jsoup.connect("https://www.douban.com")
                    .userAgent(USER_AGENT)
                    .timeout(5000)
                    .execute();
            return true;
        } catch (Exception e) {
            log.warn("豆瓣资源不可用", e);
            return false;
        }
    }
} 