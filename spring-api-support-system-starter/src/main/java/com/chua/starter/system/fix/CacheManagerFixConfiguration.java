package com.chua.starter.system.fix;

import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 缓存ManagerFix配置类，提供相关能力。
 *
 * @author CH
 * @since 1.0.0
 */
@Profile("h2")
@Configuration
public class CacheManagerFixConfiguration {

    /**
     * primary缓存Manager。
     *
     * @return 缓存Manager 对象
     */
    @Bean
    @Primary
    public CacheManager primaryCacheManager() {
        return new NoOpCacheManager();
    }
}