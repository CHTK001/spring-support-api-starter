package com.chua.starter.video.provider.impl;

import com.chua.starter.mybatis.utils.ReturnPageResultUtils;

import com.chua.common.support.base.converter.Converter;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.video.entity.PanResource;
import com.chua.starter.video.entity.VideoDownload;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.enums.PanType;
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

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * PanSou 网盘资源提供者实现
 * 基于 PanSou API 进行网盘资源搜索
 *
 * @author CH
 * @version 1.0
 * @since 2024/12/19
 */
@Slf4j
public class PanSouResourceProvider extends AbstractResourceProvider {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RestTemplate restTemplate = new RestTemplate();

    /**
     * 网盘链接正则表达式模式
     */
    private static final Pattern BAIDU_PATTERN = Pattern.compile("(https?://pan\\.baidu\\.com/s/[A-Za-z0-9_-]+)");
    private static final Pattern ALIYUN_PATTERN = Pattern.compile("(https?://www\\.aliyundrive\\.com/s/[A-Za-z0-9_-]+)");
    private static final Pattern QUARK_PATTERN = Pattern.compile("(https?://pan\\.quark\\.cn/s/[A-Za-z0-9_-]+)");
    private static final Pattern TIANYI_PATTERN = Pattern.compile("(https?://cloud\\.189\\.cn/t/[A-Za-z0-9_-]+)");
    private static final Pattern MAGNET_PATTERN = Pattern.compile("(magnet:\\?xt=urn:[a-z0-9]+:[a-z0-9]{32,40})");

    public PanSouResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }

    @Override
    public ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch) {
        String keyword = videoSearch.getKeyword();
        if (!StringUtils.hasText(keyword)) {
            return ReturnPageResultUtils.error("关键词不能为空");
        }

        log.info("使用PanSou搜索网盘资源: {}", keyword);
        List<VideoInfoResult> results = new ArrayList<>();

        try {
            // 构建搜索请求
            List<PanResource> panResources = searchPanResources(videoSearch);

            // 转换为VideoInfo对象
            for (PanResource panResource : panResources) {
                VideoInfoResult videoInfo = convertToVideoInfo(panResource);
                if (videoInfo != null) {
                    results.add(videoInfo);
                }

                // 限制结果数量
                if (results.size() >= videoSource.getVideoSourceMaxResource()) {
                    break;
                }
            }

            if (results.isEmpty()) {
                return ReturnPageResultUtils.error("未找到相关网盘资源");
            }

            log.info("PanSou搜索完成，找到{}个资源", results.size());
            return ReturnPageResultUtils.ok(results, videoSearch.getPage(), videoSearch.getPageSize());

        } catch (Exception e) {
            log.error("PanSou资源搜索失败", e);
            return ReturnPageResultUtils.error("网盘资源搜索失败: " + e.getMessage());
        }
    }

    /**
     * 搜索网盘资源
     *
     * @param videoSearch 搜索关键词
     * @return 网盘资源列表
     * @throws Exception 搜索异常
     */
    private List<PanResource> searchPanResources(VideoSearch videoSearch) throws Exception {
        List<PanResource> resources = new ArrayList<>();

        // 构建请求URL
        URI uri = UriComponentsBuilder.fromHttpUrl(videoSource.getVideoSourceUrl())
                .queryParam("kw", videoSearch.getKeyword())
                .queryParam("page", 1)
                .queryParam("size", videoSearch.getPageSize())
//                .queryParam("plugins", panSouConfig.getEnabledPluginsString())
//                .queryParam("channels", panSouConfig.getChannelsString())
//                .queryParam("async", panSouConfig.isAsyncEnabled())
                .build()
                .encode()
                .toUri();

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", videoSource.getVideoSourceUserAgent());
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");

        if (StringUtils.hasText(videoSource.getVideoSourceToken())) {
            headers.set("Authorization", "Bearer " + videoSource.getVideoSourceToken());
        }

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
            if (rootNode.has("code") && rootNode.get("code").asInt() == 200) {
                JsonNode dataNode = rootNode.path("data");
                JsonNode resultsNode = dataNode.path("results");

                // 解析搜索结果
                if (resultsNode.isArray()) {
                    for (JsonNode resultNode : resultsNode) {
                        PanResource resource = parseResourceFromJson(resultNode);
                        if (resource != null) {
                            resources.add(resource);
                        }
                    }
                }
            } else {
                String message = rootNode.path("message").asText("未知错误");
                throw new RuntimeException("PanSou API返回错误: " + message);
            }
        }

        return resources;
    }

    /**
     * 从JSON节点解析网盘资源
     *
     * @param jsonNode JSON节点
     * @return 网盘资源对象
     */
    private PanResource parseResourceFromJson(JsonNode jsonNode) {
        try {
            String title = jsonNode.path("title").asText();
            String url = jsonNode.path("url").asText();
            String typeCode = jsonNode.path("type").asText();
            String size = jsonNode.path("size").asText();
            String source = jsonNode.path("source").asText();
            String description = jsonNode.path("description").asText();
            double score = jsonNode.path("score").asDouble(0.0);
            String timeStr = jsonNode.path("time").asText();

            // 确定网盘类型
            PanType panType = determinePanType(url, typeCode);

            // 创建资源对象
            PanResource resource = new PanResource(title, url, panType, size, source);
            resource.setDescription(description);
            resource.setScore(score);

            // 解析时间
            if (StringUtils.hasText(timeStr)) {
                try {
                    LocalDateTime publishTime = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    resource.setPublishTime(publishTime);
                } catch (Exception e) {
                    log.debug("时间解析失败: {}", timeStr);
                }
            }

            return resource;

        } catch (Exception e) {
            log.warn("解析网盘资源失败", e);
            return null;
        }
    }

    /**
     * 确定网盘类型
     *
     * @param url      资源链接
     * @param typeCode 类型代码
     * @return 网盘类型
     */
    private PanType determinePanType(String url, String typeCode) {
        // 优先使用API返回的类型代码
        if (StringUtils.hasText(typeCode)) {
            PanType type = PanType.fromCode(typeCode);
            if (type != PanType.OTHERS) {
                return type;
            }
        }

        // 根据URL模式判断
        if (StringUtils.hasText(url)) {
            if (BAIDU_PATTERN.matcher(url).find()) {
                return PanType.BAIDU;
            } else if (ALIYUN_PATTERN.matcher(url).find()) {
                return PanType.ALIYUN;
            } else if (QUARK_PATTERN.matcher(url).find()) {
                return PanType.QUARK;
            } else if (TIANYI_PATTERN.matcher(url).find()) {
                return PanType.TIANYI;
            } else if (MAGNET_PATTERN.matcher(url).find()) {
                return PanType.MAGNET;
            }
        }

        return PanType.OTHERS;
    }

    /**
     * 将网盘资源转换为VideoInfo对象
     *
     * @param panResource 网盘资源
     * @return VideoInfo对象
     */
    private VideoInfoResult convertToVideoInfo(PanResource panResource) {
        if (panResource == null || !StringUtils.hasText(panResource.getTitle())) {
            return null;
        }

        VideoInfoResult videoInfo = new VideoInfoResult();
        videoInfo.setVideoName(panResource.getTitle());
        videoInfo.setVideoTitle(panResource.getTitle());
        videoInfo.setVideoAliasName(panResource.getTitle());
        videoInfo.setVideoUrl(panResource.getUrl());
        videoInfo.setVideoPlatform(panResource.getPanType().getName());
        videoInfo.setVideoDescription(panResource.getDescription());
        videoInfo.setVideoSize(panResource.getSize());
        videoInfo.setVideoAuthor(panResource.getSource());
        List<VideoDownload> downloadList = createDownloadList(panResource);
        videoInfo.setDownloadList(downloadList);

        // 设置评分
        if (panResource.getScore() != null) {
            videoInfo.setVideoScore(Converter.convertIfNecessary(panResource.getScore(), java.math.BigDecimal.class));
        }

        // 设置发布时间
        if (panResource.getPublishTime() != null) {
            videoInfo.setVideoPublishDate(panResource.getPublishTime());
        }

        // 设置创建和更新时间
        videoInfo.setCreateTime(LocalDateTime.now());
        videoInfo.setUpdateTime(LocalDateTime.now());

        return videoInfo;
    }

    private List<VideoDownload> createDownloadList(PanResource panResource) {
        List<VideoDownload> downloadList = new ArrayList<>();

        VideoDownload download = new VideoDownload();
        download.setVideoDownloadUrl(panResource.getUrl());
        download.setVideoDownloadName(panResource.getTitle());
        download.setVideoDownloadType(panResource.getPanType().getName());
        download.setVideoDownloadQuality("普通");
        download.setVideoDownloadPlatform(panResource.getPanType().getName());
        download.setVideoDownloadSize(panResource.getSize());
        download.setVideoDownloadStatus((byte) 1);
        downloadList.add(download);
        return downloadList;
    }


}