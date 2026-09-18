package com.chua.starter.geo.service.impl;

import com.chua.starter.geo.model.Bounds;
import com.chua.starter.geo.model.OverpassQueryRequest;
import com.chua.starter.geo.service.OverpassService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Overpass API 查询实现。
 *
 * @see <a href="https://dev.overpass-api.de/">Overpass API Documentation</a>
 * @author CH
 * @since 1.0.0
 */
@Service
@Slf4j
public class OverpassServiceImpl implements OverpassService {

    /** Overpass API 默认地址 */
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    /** 每个 OSM 元素平均大小估计（字节） */
    private static final long BYTES_PER_ELEMENT = 512L;

    private final ObjectMapper objectMapper = new ObjectMapper(); // 对象映射器

    /**
     * 执行 Overpass API 查询并返回结果统计。
     *
     * @param request 查询请求
     * @return 查询结果统计
     */
    @Override
    public OverpassQueryResult query(OverpassQueryRequest request) {
        Bounds bounds = request.getBounds();
        String ql = buildQuery(request.getQuery(), bounds);
        log.info("[Overpass] 执行查询: estimated elements ~{}", estimateElementCount(ql));

        try {
            String formBody = "data=" + URLEncoder.encode(ql, StandardCharsets.UTF_8);
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(OVERPASS_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", "GeoD-Web/1.0")
                    .timeout(Duration.ofSeconds(120))
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = client.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("[Overpass] HTTP {}: {}", response.statusCode(), response.body());
                return new OverpassQueryResult(0, 0);
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode elements = root.path("elements");
            int count = elements.isArray() ? elements.size() : 0;
            double sizeMb = count * BYTES_PER_ELEMENT / (1024.0 * 1024.0);

            log.info("[Overpass] 查询完成: {} 个元素, 预估大小 {} MB", count, String.format("%.2f", sizeMb));
            return new OverpassQueryResult(count, sizeMb);
        } catch (Exception e) {
            log.warn("[Overpass] 查询失败: {}", e.getMessage());
            throw new RuntimeException("Overpass 查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建带 bbox 的 Overpass QL 查询。
     *
     * @param baseQuery 基础查询语句（不含 bbox）
     * @param bounds    地理边界
     * @return 完整的 Overpass QL
     */
    private String buildQuery(String baseQuery, Bounds bounds) {
        if (baseQuery.contains("(bbox")) {
            return baseQuery;
        }
        String bbox = String.format("%.6f,%.6f,%.6f,%.6f",
                bounds.getSouth(), bounds.getWest(),
                bounds.getNorth(), bounds.getEast());
        String modified = baseQuery.replaceFirst(
                "(;\\s*out\\b)",
                ";(nwr[\"\"\"](bbox:" + bbox + "););out center;");
        return modified;
    }

    /**
     * 估算查询结果元素数量。
     *
     * @param ql Overpass QL 查询
     * @return 估算数量
     */
    private long estimateElementCount(String ql) {
        return ql.length() / 10;
    }
}
