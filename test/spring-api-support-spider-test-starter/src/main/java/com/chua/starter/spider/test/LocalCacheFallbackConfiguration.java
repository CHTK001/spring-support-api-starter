package com.chua.starter.spider.test;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * H2 测试环境缓存降级配置。
 *
 * <p>当 Redis 不可达时，{@code @Cacheable(cacheManager = "redis-cache--1")} 等注解
 * 会因 RedisCacheManager 连接失败而抛出异常。此配置在 h2 profile 下用本地
 * ConcurrentMapCacheManager 覆盖 Redis 缓存管理器，使 AI 等功能在无 Redis 时也能正常工作。</p>
 */
@Configuration
public class LocalCacheFallbackConfiguration {

    @Bean("redis-cache--1")
    public CacheManager localCacheAlways() {
        return new ConcurrentMapCacheManager();
    }

    @Bean("redis-cache-86400")
    public CacheManager localCacheDay() {
        return new ConcurrentMapCacheManager();
    }

    @Bean("redis-cache-3600")
    public CacheManager localCacheHour() {
        return new ConcurrentMapCacheManager();
    }

    @Bean("redis-cache-600")
    public CacheManager localCacheMin() {
        return new ConcurrentMapCacheManager();
    }

    @Bean("redis-cache-60")
    public CacheManager localCacheMinutes() {
        return new ConcurrentMapCacheManager();
    }

    @Bean("redis-cache-30")
    public CacheManager localCacheHalfMinutes() {
        return new ConcurrentMapCacheManager();
    }

    @Bean("redis-cache-10")
    public CacheManager localCacheTenSecond() {
        return new ConcurrentMapCacheManager();
    }
}
