package com.chua.starter.system.configuration;

import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.system.constant.SystemConstant;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 默认缓存Manager配置类，提供相关能力。
 * <p>
 * redis-starter 注册了 7 个 CacheManager 且均无 @Primary，未限定 cacheManager 的
 * {@code @Cacheable} 在解析缓存管理器时会抛 NoUniqueBeanDefinitionException；h2 档由
 * CacheManagerFixConfiguration 兜底，其余档位（如接 MySQL 的部署）由此类兜底。
 * 另外 {@code SysRoleController} 以 cacheWithRedis 指名缓存管理器，容器内并无该名字的
 * bean，故一并别名到 redis-cache-3600 管理器。
 *
 * @author CH
 * @since 1.0.0
 */
@Profile("!h2")
@Configuration
public class DefaultCacheManagerConfiguration {

    /**
     * primary缓存Manager，复用一小时过期的 redis 缓存管理器。
     *
     * @param cacheManager redis 缓存管理器
     * @return 缓存Manager 对象
     */
    @Bean
    @Primary
    public CacheManager defaultCacheManager(@Qualifier(CacheConstant.REDIS_CACHE_HOUR) CacheManager cacheManager) {
        return cacheManager;
    }

    /**
     * cacheWithRedis缓存Manager，供指名该缓存管理器的注解解析，复用一小时过期的 redis 缓存管理器。
     *
     * @param cacheManager redis 缓存管理器
     * @return 缓存Manager 对象
     */
    @Bean(SystemConstant.CACHE_WITH_REDIS)
    public CacheManager cacheWithRedisCacheManager(@Qualifier(CacheConstant.REDIS_CACHE_HOUR) CacheManager cacheManager) {
        return cacheManager;
    }
}
