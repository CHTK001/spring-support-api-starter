package com.chua.starter.monitor.service.impl;

import com.chua.starter.monitor.pojo.request.PrometheusGenQueryRequest;
import com.chua.starter.monitor.starter.entity.MonitorSysGen;
import com.chua.starter.monitor.starter.service.MonitorSysGenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
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
    private final MonitorSysGenService monitorSysGenService;

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
     * 查询
     *
     * @param request request
     * @return 结果
     */
    public JsonNode queryRange(PrometheusGenQueryRequest request) {
        MonitorSysGen sysGenServiceByIdCache = monitorSysGenService.getByIdCache(request.getMonitorSysGenId());
        if (null == sysGenServiceByIdCache) {
            return null;
        }
        String promQL = request.getPromQL();
        String prometheusAddress = sysGenServiceByIdCache.getGenDriverRemoteUrl();
        Long start = request.getStart();
        Long end = request.getEnd();
        int step = request.getStep();
        String genUser = sysGenServiceByIdCache.getGenUser();
        String genPassword = sysGenServiceByIdCache.getGenPassword();
        HttpHeaders headers = new HttpHeaders();
        if (null != genUser && null != genPassword) {
            // 添加基本认证头
            String auth = genUser + ":" + genPassword;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String encodedQuery = URLEncoder.encode(promQL, StandardCharsets.UTF_8);
        String url = String.format("%s/api/v1/query_range?query=%s&start=%d&end=%d&step=%ds",
                prometheusAddress, encodedQuery, start, end, step);

        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String response = exchange.getBody();
        return parseQueryResponse(response);
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
