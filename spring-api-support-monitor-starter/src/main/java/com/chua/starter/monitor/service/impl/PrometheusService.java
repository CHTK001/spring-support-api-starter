package com.chua.starter.monitor.service.impl;

import com.chua.starter.monitor.pojo.request.PrometheusGenQueryRequest;
import com.chua.starter.monitor.starter.entity.MonitorSysGen;
import com.chua.starter.monitor.starter.service.MonitorSysGenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
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
    private final MonitorSysGenService monitorSysGenService;

    /**
     * 是否在线
     *
     * @return 是否在线
     */
    public boolean isOnline(String monitorSysGenId) {
        MonitorSysGen sysGenServiceByIdCache = monitorSysGenService.getByIdCache(Integer.valueOf(monitorSysGenId));
        String string = restTemplate.getForObject(sysGenServiceByIdCache.getGenDriverRemoteUrl("http") + "/-/healthy", String.class);
        return string != null && string.contains("Prometheus Server is Healthy.");
    }


    /**
     * 重新加载
     *
     * @return 是否成功
     */
    public boolean reload(String monitorSysGenId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        MonitorSysGen sysGenServiceByIdCache = monitorSysGenService.getByIdCache(Integer.valueOf(monitorSysGenId));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    sysGenServiceByIdCache.getGenDriverRemoteUrl("http") + "/-/reload",
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
    public JsonNode query(PrometheusGenQueryRequest request) {
        MonitorSysGen sysGenServiceByIdCache = monitorSysGenService.getByIdCache(request.getMonitorSysGenId());
        if (null == sysGenServiceByIdCache) {
            return null;
        }
        String promQL = request.getPromQL();
        String prometheusAddress = sysGenServiceByIdCache.getGenDriverRemoteUrl("http");
        Long start = request.getStart();
        Long end = request.getEnd();
        int step = request.getStep();
        String genUser = sysGenServiceByIdCache.getGenUser();
        String genPassword = sysGenServiceByIdCache.getGenPassword();
        HttpHeaders headers = new HttpHeaders();

        Map<String, String> params = new HashMap<>(4);
        params.put("query", request.getPromQL());
        params.put("start", String.valueOf(start));
        params.put("end", String.valueOf(end));
        params.put("step", step + "s");
        RestTemplate restTemplate = new RestTemplate();

        // 设置超时时间（可选）
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5秒
        factory.setReadTimeout(30000);   // 30秒
        restTemplate.setRequestFactory(factory);
        // 构建URI
        URI uri = UriComponentsBuilder.fromHttpUrl(String.format("%s/api/v1/query", prometheusAddress))
                .queryParam("query", "{query}")
                .buildAndExpand(params)
                .toUri();
        if (StringUtils.isNotBlank(genUser) && null != genPassword) {
            // 添加基本认证头
            restTemplate.setInterceptors(Collections.singletonList(
                    basicAuthInterceptor(
                            genUser,
                            genPassword
                    )
            ));
        }
        ResponseEntity<String> exchange = restTemplate.getForEntity(uri, String.class);
        String response = exchange.getBody();
        return parseQueryResponse(response);
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
        String prometheusAddress = sysGenServiceByIdCache.getGenDriverRemoteUrl("http");
        Long start = request.getStart();
        Long end = request.getEnd();
        int step = request.getStep();
        String genUser = sysGenServiceByIdCache.getGenUser();
        String genPassword = sysGenServiceByIdCache.getGenPassword();
        HttpHeaders headers = new HttpHeaders();

        Map<String, String> params = new HashMap<>(4);
        params.put("query", request.getPromQL());
        params.put("start", String.valueOf(start));
        params.put("end", String.valueOf(end));
        params.put("step", step + "s");
        RestTemplate restTemplate = new RestTemplate();

        // 设置超时时间（可选）
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5秒
        factory.setReadTimeout(30000);   // 30秒
        restTemplate.setRequestFactory(factory);
        // 构建URI
        URI uri = UriComponentsBuilder.fromHttpUrl(String.format("%s/api/v1/query_range", prometheusAddress))
                .queryParam("query", "{query}")
                .queryParam("start", "{start}")
                .queryParam("end", "{end}")
                .queryParam("step", "{step}")
                .buildAndExpand(params)
                .toUri();
        if (StringUtils.isNotBlank(genUser) && null != genPassword) {
            // 添加基本认证头
            restTemplate.setInterceptors(Collections.singletonList(
                    basicAuthInterceptor(
                            genUser,
                            genPassword
                    )
            ));
        }
        ResponseEntity<String> exchange = restTemplate.getForEntity(uri, String.class);
        String response = exchange.getBody();
        return parseQueryResponse(response);
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
