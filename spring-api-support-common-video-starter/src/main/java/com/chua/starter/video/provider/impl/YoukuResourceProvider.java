package com.chua.starter.video.provider.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.provider.ResourceProvider;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 优酷资源提供者实现
 *
 * @author CH
 */
@Component
@Slf4j
public class YoukuResourceProvider implements ResourceProvider {

    private static final String YOUKU_SEARCH_URL = "https://so.youku.com/search_video/q_";
    private static final int TIMEOUT = 10000; // 10秒超时
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    @Override
    public ReturnResult<List<VideoInfo>> searchResource(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return ReturnResult.error("关键词不能为空");
        }

        log.info("从优酷资源检索视频: {}", keyword);
        List<VideoInfo> results = new ArrayList<>();

        try {
            // URL编码关键词
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String searchUrl = YOUKU_SEARCH_URL + encodedKeyword;

            // 发送HTTP请求获取搜索结果页面
            Document document = Jsoup.connect(searchUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();

            // 解析搜索结果
            Elements items = document.select("div.sk-mod");

            for (Element item : items) {
                try {
                    Element titleElement = item.selectFirst("div.mod-main div.mod-header a.soku-video-name");
                    if (titleElement == null) continue;

                    String title = titleElement.text().trim();
                    String detailUrl = titleElement.attr("href");
                    if (!detailUrl.startsWith("http")) {
                        detailUrl = "https:" + detailUrl;
                    }

                    Element descElement = item.selectFirst("div.mod-main div.mod-content p.desc");
                    String description = descElement != null ? descElement.text().trim() : "";

                    Element coverElement = item.selectFirst("div.mod-left a.sk-pic img");
                    String coverUrl = coverElement != null ? coverElement.attr("src") : "";
                    if (!coverUrl.startsWith("http") && coverUrl.startsWith("//")) {
                        coverUrl = "https:" + coverUrl;
                    }

                    Element durationElement = item.selectFirst("div.mod-left span.time");
                    String duration = durationElement != null ? durationElement.text().trim() : "";

                    Element infoElement = item.selectFirst("div.mod-main div.mod-content div.info");
                    String infoText = infoElement != null ? infoElement.text().trim() : "";

                    // 提取上传者/导演信息
                    String uploader = "";
                    if (infoText.contains("上传者:")) {
                        uploader = infoText.substring(infoText.indexOf("上传者:") + 4);
                        if (uploader.contains("播放量:")) {
                            uploader = uploader.substring(0, uploader.indexOf("播放量:")).trim();
                        }
                    }

                    // 创建VideoInfo对象
                    VideoInfo videoInfo = new VideoInfo();
                    videoInfo.setVideoName(title);
                    videoInfo.setVideoAliasName(title);
                    videoInfo.setVideoDirector(uploader);
                    videoInfo.setVideoActor("");
//                    videoInfo.setVideoPlatform("优酷视频");
                    videoInfo.setVideoDescription(description);
                    videoInfo.setVideoCover(coverUrl);
                    videoInfo.setCreateTime(LocalDateTime.now());
                    videoInfo.setUpdateTime(LocalDateTime.now());

                    results.add(videoInfo);

                    // 限制结果数量，避免过多请求
                    if (results.size() >= 10) {
                        break;
                    }
                } catch (Exception e) {
                    log.warn("解析优酷搜索结果项失败", e);
                }
            }

            if (results.isEmpty()) {
                return ReturnResult.error("优酷资源未找到相关视频");
            }

            return ReturnResult.success(results);
        } catch (Exception e) {
            log.error("优酷资源检索失败", e);
            return ReturnResult.error("优酷资源检索失败: " + e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "优酷资源";
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public boolean isAvailable() {
        try {
            // 简单检查优酷网站是否可访问
            Jsoup.connect("https://www.youku.com")
                    .userAgent(USER_AGENT)
                    .timeout(5000)
                    .execute();
            return true;
        } catch (Exception e) {
            log.warn("优酷资源不可用", e);
            return false;
        }
    }
} 