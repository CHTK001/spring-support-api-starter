package com.chua.starter.service.configuration.trace.storage;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.json.Json;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.service.configuration.trace.RequestTraceConfigStorage;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfig;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfigCodec;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * Redis 版配置存储。
 *
 * @author CH
 * @since 2026/03/25
 */
@Spi("redis")
public class RedisRequestTraceConfigStorage implements RequestTraceConfigStorage {

    private static final Duration EXPIRE = Duration.ofDays(30);

    @Override
    public String type() {
        return "redis";
    }

    @Override
    public RequestTraceStrategyConfig load(String groupCode) {
        StringRedisTemplate template = redisTemplate();
        if (template == null) {
            return null;
        }
        String value = template.opsForValue().get(key(groupCode));
        if (value == null || value.isBlank()) {
            return null;
        }
        Map<String, String> values = Json.fromJson(value, Map.class);
        if (values == null) {
            return null;
        }
        return RequestTraceStrategyConfigCodec.fromMap(values);
    }

    @Override
    public RequestTraceStrategyConfig save(String groupCode, RequestTraceStrategyConfig config) {
        RequestTraceStrategyConfig normalized = RequestTraceStrategyConfigCodec.normalize(config);
        StringRedisTemplate template = redisTemplate();
        if (template == null) {
            return normalized.copy();
        }
        template.opsForValue().set(
                key(groupCode),
                Json.toJson(RequestTraceStrategyConfigCodec.toMap(normalized)),
                EXPIRE
        );
        return normalized.copy();
    }

    private StringRedisTemplate redisTemplate() {
        try {
            return SpringBeanUtils.getBean(StringRedisTemplate.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String key(String groupCode) {
        return "request:trace:config:" + groupCode;
    }
}
