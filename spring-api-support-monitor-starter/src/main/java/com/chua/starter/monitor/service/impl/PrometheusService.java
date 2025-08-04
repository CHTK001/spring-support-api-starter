package com.chua.starter.monitor.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

    private ClientHttpRequestInterceptor basicAuthInterceptor(String username, String password) {
        return (request, body, execution) -> {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            request.getHeaders().add("Authorization", authHeader);
            return execution.execute(request, body);
        };
    }
    /**
     * 查询
     *
     * @param promQL promQL
     * @param start  开始时间
     * @param end    结束时间
     * @param step   步长
     * @return 结果
     */
    public JsonNode queryRange(String prometheusAddress, String promQL, Long start, Long end, int step) {
        String encodedQuery = URLEncoder.encode(promQL, StandardCharsets.UTF_8);
        String url = String.format("%s/api/v1/query_range?query=%s&start=%d&end=%d&step=%ds",
                prometheusAddress, encodedQuery, start, end, step);

        String response = restTemplate.getForObject(url, String.class);
        return parseQueryResponse(response);
    }


    /**
     * 解析查询结果
     *
     * @param jsonResponse jsonResponse
     * @return 结果
     */
    private JsonNode parseQueryResponse(String jsonResponse) {
        try {
            return OBJECT_MAPPER.readValue(jsonResponse, new TypeReference<JsonNode>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Prometheus response", e);
        }
    }



}
