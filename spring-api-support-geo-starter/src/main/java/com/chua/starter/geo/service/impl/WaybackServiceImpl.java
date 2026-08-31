package com.chua.starter.geo.service.impl;

import com.chua.starter.geo.model.WaybackVersion;
import com.chua.starter.geo.service.WaybackService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Esri Wayback 历史影像服务实现，调用 Esri Wayback Config API 获取真实版本列表。
 *
 * @see <a href="https://opendata.arcgis.com/">Esri Open Data</a>
 * @author CH
 * @since 1.0.0
 */
@Service
@Slf4j
public class WaybackServiceImpl implements WaybackService {

    /** Wayback 配置 API 地址 */
    private static final String WAYBACK_CONFIG_URL =
            "https://s3-us-west-2.amazonaws.com/config.maptiles.arcgis.com/waybackconfig.json";

    /** JSON 解析器 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取所有历史影像版本列表。
     *
     * @return 版本列表，获取失败返回空列表
     */
    @Override
    public List<WaybackVersion> getVersions() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WAYBACK_CONFIG_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "GeoD-Web/1.0")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Wayback API 返回 HTTP {}", response.statusCode());
                return Collections.emptyList();
            }
            JsonNode root = objectMapper.readTree(response.body());
            List<WaybackVersion> versions = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    WaybackVersion v = new WaybackVersion();
                    v.setId(node.path("itemTitle").asText());
                    v.setTitle(node.path("itemTitle").asText());
                    v.setLayerId(node.path("layerIdentifier").asText());
                    versions.add(v);
                }
            }
            log.info("Wayback 版本列表获取完成: {} 条", versions.size());
            return versions;
        } catch (Exception e) {
            log.warn("获取 Wayback 版本失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
