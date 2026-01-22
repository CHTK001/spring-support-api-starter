package com.chua.starter.video.provider.impl;

import com.chua.starter.mybatis.utils.ReturnPageResultUtils;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.base.converter.Converter;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.core.utils.ObjectUtils;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
@Slf4j
@Spi("bilibili")
public class BilibiliResourceProvider extends AbstractResourceProvider {

    private static final String BILIBILI_SEARCH_API = "https://api.bilibili.com/x/web-interface/search/all/v2";
    private static final String BILIBILI_VIDEO_URL = "https://www.bilibili.com/video/";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RestTemplate restTemplate = new RestTemplate();

    public BilibiliResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }

    @Override
    public ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch) {
        String keyword = videoSearch.getKeyword();
        if (!StringUtils.hasText(keyword)) {
            return ReturnPageResultUtils.error("关键词不能为空");
        }

        log.info("从B站资源检索视频: {}", keyword);
        List<VideoInfoResult> results = new ArrayList<>();

        try {
            // 构建请求URL
            URI uri = UriComponentsBuilder.fromHttpUrl(BILIBILI_SEARCH_API)
                    .queryParam("keyword", keyword)
                    //0=综合（默认），1=视频，2=番剧，3=影视，5=用户，6=专栏，7=直播，8=相簿，9=话题，12=课程
                    .queryParam("search_type", ObjectUtils.defaultIfNull(getSearchType(videoSearch), "1"))
                    .queryParam("page", videoSearch.getPage())
                    .queryParam("order", ObjectUtils.defaultIfNull(videoSearch.getOrder(), "totalrank"))
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
                                VideoInfoResult videoInfo = new VideoInfoResult();
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
                    return ReturnPageResultUtils.error("B站API返回错误: " + message);
                }
            }

            if (results.isEmpty()) {
                return ReturnPageResultUtils.error("B站资源未找到相关视频");
            }

            return ReturnPageResultUtils.ok(results, videoSearch.getPage(), videoSearch.getPageSize());
        } catch (Exception e) {
            log.error("B站资源检索失败", e);
            return ReturnPageResultUtils.error("B站资源检索失败: " + e.getMessage());
        }
    }

    private String getSearchType(VideoSearch videoSearch) {
        //0=综合（默认），1=视频，2=番剧，3=影视，5=用户，6=专栏，7=直播，8=相簿，9=话题，12=课程
        String category = videoSearch.getVideoType();
        if (StringUtils.hasText(category)) {
            return switch (category) {
                case "video" -> "1";
                case "anime" -> "2";
                case "movie" -> "3";
                case "user" -> "5";
                case "column" -> "6";
                case "live" -> "7";
                case "album" -> "8";
                case "topic" -> "9";
                case "course" -> "12";
                default -> "0";
            };
        }
        return "0";
    }
}