package com.chua.starter.service.configuration.trace.storage;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.lang.json.Json;
import com.chua.spring.support.configuration.SpringBeanUtils;
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
* @since 2026/03/25
* @author CH
 */
@Spi("redis")
public class RedisRequestTraceConfigStorage implements RequestTraceConfigStorage {

    /** Expire */
    private static final Duration EXPIRE = Duration.ofDays(30);

    @Override
    /** 类型 */
    public String type() {
        return "redis";
    }

    @Override
    /** 加载 */
    public RequestTraceStrategyConfig load(String groupCode) {
        StringRedisTemplate template = redisTemplate();
        if (template == null) {
            return null;
        }
        String value = template.opsForValue().get(key(groupCode));
        if (value == null || value.isBlank()) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> values = (Map<String, String>) Json.fromJson(value, (Class) Map.class);
        if (values == null) {
            return null;
        }
        return RequestTraceStrategyConfigCodec.fromMap(values);
    }

    @Override
    /** 保存 */
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

    /**
    * redistemplate
    *
    * @return redisTemplate的结果
     */
    private StringRedisTemplate redisTemplate() {
        try {
            return SpringBeanUtils.getBean(StringRedisTemplate.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
    * 键
    *
    * @param groupCode 群体编码
    * @return 键的结果
     */
    private String key(String groupCode) {
        return "request:trace:config:" + groupCode;
    }
}
