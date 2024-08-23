package com.chua.starter.system.configuration;

import com.chua.starter.common.support.properties.CacheProperties;
import com.chua.starter.redis.support.CacheConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static com.chua.starter.system.constant.SystemConstant.CACHE_WITH_REDIS;

/**
 * 缓存配置
 * @author CH
 * @since 2024/8/8
 */
@SpringBootApplication
public class SystemCacheConfiguration {

    /**
     * 缓存配置
     * @return CacheManager
     */
    @Bean(CACHE_WITH_REDIS)
    @Primary
    public CacheManager getDefaultCacheManager(ObjectMapper om, RedisConnectionFactory factory, CacheProperties cacheProperties) {
        return CacheConfiguration.createRedisCacheManager(om, factory, Math.toIntExact(cacheProperties.getRedis().getTtl()));
    }

}
