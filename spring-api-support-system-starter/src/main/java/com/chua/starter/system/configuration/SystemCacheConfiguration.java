package com.chua.starter.system.configuration;

import com.chua.common.support.converter.Converter;
import com.chua.common.support.utils.ClassUtils;
import com.chua.starter.common.support.properties.CacheProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.chua.starter.system.constant.SystemConstant.CACHE_WITH_REDIS;

/**
 * 缓存配置
 * @author CH
 * @since 2024/8/8
 */
@SpringBootApplication
public class SystemCacheConfiguration {


    /**
     * 创建jackson对象
     * @return JacksonObjectWriter
     */
    public JacksonObjectWriter createJacksonObjectWriter() {
        return (mapper, source) -> {
            Map<String, Object> map = new HashMap<>(2);
            map.put("type", source.getClass().getTypeName());
            map.put("data", source);
            return mapper.writeValueAsBytes(map);
        };
    }

    /**
     * 创建jackson对象
     * @return JacksonObjectReader
     */
    public JacksonObjectReader createJacksonObjectReader() {
        return (mapper, source, type) -> {
            Object object = mapper.readValue(source, 0, source.length, type);
            if(type.getRawClass() != Object.class) {
                return object;
            }
            if(object instanceof Map map) {
                Object type1 = map.get("type");
                if(null == type1) {
                    return map;
                }
                Object object1 = map.get("data");
                return Converter.convertIfNecessary(object1, ClassUtils.toType(type1));
            }
            return object;
        };
    }
    /**
     * 缓存配置
     * @return CacheManager
     */
    @Bean(CACHE_WITH_REDIS)
    @Primary
    public CacheManager getDefaultCacheManager(ObjectMapper om, RedisConnectionFactory factory, CacheProperties cacheProperties) {
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(om, TypeFactory.defaultInstance().constructType(Object.class), createJacksonObjectReader(), createJacksonObjectWriter());
        // 配置序列化（解决乱码的问题）,过期时间600秒
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(cacheProperties.getRedis().getTtl()))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                .prefixCacheNameWith("CACHE:")
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

}
