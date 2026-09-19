package com.chua.starter.system.configuration;

import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.system.constant.SystemConstant;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 缓存管理器兜底配置。
 * <p>
 * redis-starter 注册了 7 个 CacheManager 且均无 @Primary，未限定 cacheManager 的
 * {@code @Cacheable} 在解析缓存管理器时会抛 NoUniqueBeanDefinitionException。凡带
 * {@code @EnableCaching} 且扫描 {@code com.chua.starter.system} 的应用都需要这份兜底，
 * 因此放在 common-system-starter 而非某个具体服务端模块。
 * </p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Configuration
public class DefaultCacheManagerConfiguration {

    /**
     * h2 档位：内存库通常不伴生 Redis，用空实现避免缓存注解触发真实连接。
     */
    @Configuration
    @Profile("h2")
    public static class EmbeddedCacheManagerConfiguration {

        /**
         * primary缓存Manager。
         *
         * @return 空实现的缓存管理器
         */
        @Bean
        @Primary
        public CacheManager primaryCacheManager() {
            return new NoOpCacheManager();
        }
    }

    /**
     * 非 h2 档位：复用一小时过期的 Redis 缓存管理器作为主管理器。
     */
    @Configuration
    @Profile("!h2")
    public static class RedisCacheManagerConfiguration {

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
}
