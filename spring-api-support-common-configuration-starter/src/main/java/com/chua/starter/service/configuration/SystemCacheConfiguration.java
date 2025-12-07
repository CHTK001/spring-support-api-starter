package com.chua.starter.service.configuration;

import com.chua.starter.common.support.cache.CacheProperties;
import com.chua.starter.redis.support.CacheConfiguration;
import com.chua.starter.service.constant.SystemConstant;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

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
    @Bean(SystemConstant.CACHE_WITH_REDIS)
    @Primary
    public CacheManager getDefaultCacheManager(RedisConnectionFactory factory, CacheProperties cacheProperties) {
        return CacheConfiguration.createRedisCacheManager(null, factory, Math.toIntExact(cacheProperties.getRedis().getTtl()));
    }

}
