package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.network.client.ClientResponse;
import com.chua.common.support.network.client.HttpClientFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 天气接口。
 *
 * <p>基于公开 wttr.in 服务实现（无需 API key）。
 * 命中后本地缓存 10 分钟，避免重复外网请求。
 * 离线 / 超时时返回 {@code cached=false} + 兜底数据。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Tag(name = "天气接口")
@RestController
@RequestMapping("/v1/weather")
@Slf4j
public class WeatherController {

    /** Wttr_base */
    private static final String WTTR_BASE = "https://wttr.in/";
    /** Cache_ttl_ms */
    private static final long CACHE_TTL_MS = 10L * 60 * 1000;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Operation(summary = "根据 IP 定位获取当前天气")
    @GetMapping("address")
    public ReturnResult<Map<String, Object>> getWeather() {
        return ReturnResult.ok(fetchWeather("beijing", null));
    }

    @Operation(summary = "根据城市获取天气")
    @GetMapping("city")
    /**
     * 获取WeatherByCity
     * @param city city
     */
    public ReturnResult<Map<String, Object>> getWeatherByCity(
            @RequestParam(value = "city", required = false) String city) {
        if (city == null || city.isBlank()) {
            return ReturnResult.error("城市名称不能为空");
        }
        return ReturnResult.ok(fetchWeather(city.trim(), null));
    }

    /**
     * 调用 wttr.in 拉取天气，命中本地缓存。
     *
     * @param city  城市名(拼音或英文,wttr.in 支持)
     * @param extra 预留扩展
     * @return 天气数据 {city, temperatureC, humidity, wind, description, observedAt, cached}
     */
    private Map<String, Object> fetchWeather(String city, Object extra) {
        String key = city.toLowerCase();
        CacheEntry cached = cache.get(key);
        long now = System.currentTimeMillis();
        if (cached != null && (now - cached.timestamp) < CACHE_TTL_MS) {
            Map<String, Object> result = new HashMap<>(cached.data);
            result.put("cached", true);
            return result;
        }

        try {
            String encoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = WTTR_BASE + encoded + "?format=j1";
            ClientResponse resp = HttpClientFactory.of(url)
                    .header("User-Agent", "curl/8.0")
                    .accept("application/json")
                    .connectTimeout(3000)
                    .readTimeout(5000)
                    .get();
            if (resp.getStatusCode() != 200) {
                log.warn("wttr.in 返回非 200: status={}", resp.getStatusCode());
                return fallback(city, "上游服务异常 " + resp.getStatusCode());
            }

            Map<String, Object> parsed = parseWttrJson(resp.getBodyString(), city);
            cache.put(key, new CacheEntry(parsed, now));
            return parsed;
        } catch (Exception e) {
            log.warn("获取天气失败 city={} err={}", city, e.getMessage());
            return fallback(city, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseWttrJson(String json, String city) {
        Map<String, Object> out = new HashMap<>();
        out.put("city", city);
        out.put("cached", false);
        out.put("observedAt", System.currentTimeMillis());

        try {
            com.fasterxml.jackson.databind.ObjectMapper om =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> root = om.readValue(json, Map.class);
            Object current = ((java.util.List<?>) root.get("current_condition")).get(0);
            Map<String, Object> cur = (Map<String, Object>) current;
            out.put("temperatureC", Double.parseDouble(cur.get("temp_C").toString()));
            out.put("humidity", cur.get("humidity"));
            out.put("wind", cur.get("windspeedKmph"));
            Object descArr = cur.get("weatherDesc");
            if (descArr instanceof java.util.List<?> && !((java.util.List<?>) descArr).isEmpty()) {
                Object first = ((java.util.List<?>) descArr).get(0);
                if (first instanceof Map<?, ?>) {
                    out.put("description", ((Map<?, ?>) first).get("value"));
                }
            }
        } catch (Exception e) {
            log.warn("解析 wttr.in JSON 失败: {}", e.getMessage());
        }
        return out;
    }

    private Map<String, Object> fallback(String city, String reason) {
        Map<String, Object> out = new HashMap<>();
        out.put("city", city);
        out.put("cached", false);
        out.put("observedAt", System.currentTimeMillis());
        out.put("temperatureC", null);
        out.put("humidity", null);
        out.put("wind", null);
        out.put("description", "暂不可用");
        out.put("error", reason);
        return out;
    }

    private static final class CacheEntry {
        final Map<String, Object> data;
        final long timestamp;
        CacheEntry(Map<String, Object> data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
}
