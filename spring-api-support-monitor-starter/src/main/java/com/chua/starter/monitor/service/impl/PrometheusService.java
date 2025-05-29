package com.chua.starter.monitor.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * prometheus查询服务
 *
 * @author CH
 * @since 2025/5/29 16:08
 */
@Service
@RequiredArgsConstructor
public class PrometheusService {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final RestTemplate restTemplate;

    /**
     * 是否在线
     *
     * @return 是否在线
     */
    public boolean isOnline(String prometheusAddress) {
        String string = restTemplate.getForObject(prometheusAddress + "/-/healthy", String.class);
        return string != null && string.contains("Prometheus Server is Healthy.");
    }


    /**
     * 重新加载
     *
     * @return 是否成功
     */
    public boolean reload(String prometheusAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    prometheusAddress + "/-/reload",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            throw new RuntimeException("Failed to reload Prometheus config", e);
        }
    }


    /**
     * 查询
     *
     * @param promQL promQL
     * @return 结果
     */
    public String queryPrometheus(String prometheusAddress, String promQL) {
        String encodedQuery = URLEncoder.encode(promQL, StandardCharsets.UTF_8);
        String url = String.format("%s/api/v1/query?query=%s", prometheusAddress, encodedQuery);
        return restTemplate.getForObject(url, String.class);
    }


    /**
     * 查询范围
     *
     * @param prometheusAddress prometheus地址
     * @param promQL            promQL
     * @param range             范围
     * @param step              步长
     * @return 结果
     */
    public Map<String, Object> queryRange(String prometheusAddress, String promQL, Duration range, Duration step) {
        long end = System.currentTimeMillis() / 1000;
        long start = end - range.getSeconds();

        String encodedQuery = URLEncoder.encode(promQL, StandardCharsets.UTF_8);
        String url = String.format("%s/api/v1/query_range?query=%s&start=%d&end=%d&step=%ds",
                prometheusAddress, encodedQuery, start, end, step.getSeconds());

        String response = restTemplate.getForObject(url, String.class);
        return parseQueryResponse(response);
    }


    /**
     * 解析查询结果
     *
     * @param jsonResponse jsonResponse
     * @return 结果
     */
    private Map<String, Object> parseQueryResponse(String jsonResponse) {
        try {
            return OBJECT_MAPPER.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Prometheus response", e);
        }
    }
}
