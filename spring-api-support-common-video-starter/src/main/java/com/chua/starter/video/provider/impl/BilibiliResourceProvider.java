package com.chua.starter.video.provider.impl;

import com.chua.common.support.converter.Converter;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.provider.ResourceProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * B站资源提供者实现
 *
 * @author CH
 */
@Component
@Slf4j
public class BilibiliResourceProvider implements ResourceProvider {

    private static final String BILIBILI_SEARCH_API = "https://api.bilibili.com/x/web-interface/search/all/v2";
    private static final String BILIBILI_VIDEO_URL = "https://www.bilibili.com/video/";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ReturnResult<List<VideoInfo>> searchResource(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return ReturnResult.error("关键词不能为空");
        }

        log.info("从B站资源检索视频: {}", keyword);
        List<VideoInfo> results = new ArrayList<>();

        try {
            // 构建请求URL
            URI uri = UriComponentsBuilder.fromHttpUrl(BILIBILI_SEARCH_API)
                    .queryParam("keyword", keyword)
                    .queryParam("search_type", "video")  // 搜索类型为视频
                    .queryParam("page", 1)
                    .queryParam("order", "totalrank")    // 按综合排序
                    .queryParam("platform", "pc")
                    .build()
                    .encode()
                    .toUri();

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            headers.set("Referer", "https://www.bilibili.com/");

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            // 解析响应
            String responseBody = response.getBody();
            if (responseBody != null) {
                JsonNode rootNode = objectMapper.readTree(responseBody);

                // 检查响应状态
                int code = rootNode.path("code").asInt(-1);
                if (code == 0) {
                    JsonNode resultNode = rootNode.path("data").path("result");

                    // 遍历视频结果
                    for (JsonNode typeNode : resultNode) {
                        if ("video".equals(typeNode.path("result_type").asText())) {
                            JsonNode videoList = typeNode.path("data");

                            for (JsonNode video : videoList) {
                                String title = video.path("title").asText();
                                // 去除HTML标签
                                title = title.replaceAll("</?em.*?>", "");

                                String bvid = video.path("bvid").asText();
                                String author = video.path("author").asText();
                                String description = video.path("description").asText();
                                String coverUrl = video.path("pic").asText();
                                String duration = video.path("duration").asText();
                                String pubDate = video.path("pubdate").asText();

                                // 创建VideoInfo对象
                                VideoInfo videoInfo = new VideoInfo();
                                videoInfo.setVideoName(title);
                                videoInfo.setVideoAliasName(title);
                                videoInfo.setVideoDirector(author);
                                videoInfo.setVideoActor(author);
//                                videoInfo.setVideoPlatform("B站视频");
                                videoInfo.setVideoDescription(description);
                                videoInfo.setVideoScore(Converter.convertIfNecessary(video.path("score").asText(), BigDecimal.class));
                                videoInfo.setVideoCover(coverUrl);
                                videoInfo.setVideoYear(Converter.convertIfNecessary(pubDate, Integer.class));
                                videoInfo.setCreateTime(LocalDateTime.now());
                                videoInfo.setUpdateTime(LocalDateTime.now());

                                results.add(videoInfo);

                                // 限制结果数量
                                if (results.size() >= 10) {
                                    break;
                                }
                            }
                            break;
                        }
                    }
                } else {
                    String message = rootNode.path("message").asText("未知错误");
                    return ReturnResult.error("B站API返回错误: " + message);
                }
            }

            if (results.isEmpty()) {
                return ReturnResult.error("B站资源未找到相关视频");
            }

            return ReturnResult.success(results);
        } catch (Exception e) {
            log.error("B站资源检索失败", e);
            return ReturnResult.error("B站资源检索失败: " + e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "B站资源";
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public boolean isAvailable() {
        try {
            // 检查B站API是否可用
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "https://api.bilibili.com/x/web-interface/nav",
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String body = response.getBody();
                if (body != null) {
                    JsonNode rootNode = objectMapper.readTree(body);
                    return rootNode.path("code").asInt(-1) == 0;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("B站资源不可用", e);
            return false;
        }
    }
} 