package com.chua.starter.video.setting;

import com.chua.starter.video.live.service.LiveChannelService;
import com.chua.starter.video.properties.VideoProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 视频/直播配置同步服务。
 *
 * <p>由 job-starter 定时任务驱动（见 {@code com.chua.starter.video.job.VideoConfigSyncJob}），
 * 也可经 {@code POST /video/api/config/sync} 手动触发。同步语义：</p>
 * <ol>
 *   <li>若配置了 {@code spring.video.config-sync-url}，拉取远程配置快照
 *       （{@link VideoConfigSnapshot} JSON）并导入配置库；</li>
 *   <li>随后热加载配置与直播目录，令本节点即时生效（亦便于多实例间通过共享库收敛）。</li>
 * </ol>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
public class VideoConfigSyncService {

    /**
     * JSON 解析器。
     */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * 配置数据库服务。
     */
    private final VideoSettingService settings;

    /**
     * 视频配置。
     */
    private final VideoProperties properties;

    /**
     * 直播目录服务（直播关闭时为空）。
     */
    private final LiveChannelService liveChannelService;

    /**
     * HTTP 客户端。
     */
    private final HttpClient httpClient;

    /**
     * 构造配置同步服务。
     *
     * @param settings           配置数据库服务
     * @param properties         视频配置
     * @param liveChannelService 直播目录服务，可为空
     */
    public VideoConfigSyncService(VideoSettingService settings, VideoProperties properties,
                                  LiveChannelService liveChannelService) {
        this.settings = settings;
        this.properties = properties;
        this.liveChannelService = liveChannelService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, properties.getConnectTimeout())))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * 执行一次配置同步。
     *
     * @return 同步结果摘要
     */
    public Map<String, Object> sync() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("storage", settings.available() ? "db" : "json");
        if (!settings.available()) {
            result.put("pulled", false);
            result.put("message", "配置数据库不可用，跳过同步");
            return result;
        }
        String url = properties.getConfigSyncUrl();
        boolean pulled = false;
        if (url != null && !url.isBlank()) {
            try {
                VideoConfigSnapshot snapshot = fetch(url);
                pulled = snapshot != null
                        && settings.importConfig(snapshot, properties.isConfigSyncOverwrite());
            } catch (Exception e) {
                log.warn("[Video] 配置同步拉取失败 url={}: {}", url, e.getMessage());
                result.put("error", e.getMessage());
            }
        }
        settings.reload();
        if (liveChannelService != null) {
            liveChannelService.reload();
        }
        result.put("url", url);
        result.put("pulled", pulled);
        result.put("liveChannelCount",
                liveChannelService == null ? 0 : liveChannelService.channels(null, null).size());
        return result;
    }

    /**
     * 拉取远程配置快照。
     *
     * @param url 快照地址
     * @return 配置快照
     * @throws Exception 请求或解析失败
     */
    private VideoConfigSnapshot fetch(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(Math.max(1000, properties.getReadTimeout())))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
        return MAPPER.readValue(response.body(), VideoConfigSnapshot.class);
    }
}
