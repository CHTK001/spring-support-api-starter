package com.chua.starter.service.configuration;

import com.chua.starter.redis.support.CacheConfiguration;
import com.chua.starter.service.constant.SystemConstant;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

/**
 * 缓存配置
 * @author CH
 * @since 2024/8/8
 */
@SpringBootApplication
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnClass({RedisConnectionFactory.class, CacheConfiguration.class})
public class SystemCacheConfiguration {

    /**
     * 默认缓存TTL（秒）
     */
    private static final int DEFAULT_TTL_SECONDS = 3600;

    /**
     * 缓存配置
     * @return CacheManager
     */
    @Bean(SystemConstant.CACHE_WITH_REDIS)
    @Primary
    public CacheManager getDefaultCacheManager(RedisConnectionFactory factory, CacheProperties cacheProperties) {
        int ttl = DEFAULT_TTL_SECONDS;
        if (cacheProperties.getRedis() != null && cacheProperties.getRedis().getTimeToLive() != null) {
            Duration timeToLive = cacheProperties.getRedis().getTimeToLive();
            ttl = (int) timeToLive.getSeconds();
        }
        return CacheConfiguration.createRedisCacheManager(null, factory, ttl);
    }

}
