package com.chua.starter.oauth.sso.support.configuration;

import com.chua.starter.oauth.sso.support.authenticate.AkSkAuthenticator;
import com.chua.starter.oauth.sso.support.authenticate.DatabaseAkSkAuthenticator;
import com.chua.starter.oauth.sso.support.authenticate.MemoryAkSkAuthenticator;
import com.chua.starter.oauth.sso.support.authenticate.RedisAkSkAuthenticator;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * AKSK认证器配置类
 * <p>根据配置自动选择合适的AKSK认证器实现</p>
 *
 * @author CH
 * @since 2024/12/04
 */
@Slf4j
@Configuration
public class AkSkAuthenticatorConfiguration {

    /**
     * Redis AKSK认证器
     * <p>当配置为redis且存在RedisTemplate时启用</p>
     */
    @Bean
    @ConditionalOnProperty(name = "plugin.oauth.authenticate", havingValue = "redis")
    @ConditionalOnBean(RedisTemplate.class)
    public AkSkAuthenticator redisAkSkAuthenticator(RedisTemplate<String, Object> redisTemplate) {
        log.info("启用Redis AKSK认证器");
        return new RedisAkSkAuthenticator(redisTemplate);
    }

    /**
     * 数据库AKSK认证器
     * <p>当配置为database且存在JdbcTemplate时启用</p>
     */
    @Bean
    @ConditionalOnProperty(name = "plugin.oauth.authenticate", havingValue = "database")
    @ConditionalOnBean(JdbcTemplate.class)
    public AkSkAuthenticator databaseAkSkAuthenticator(JdbcTemplate jdbcTemplate) {
        log.info("启用数据库AKSK认证器");
        return new DatabaseAkSkAuthenticator(jdbcTemplate);
    }

    /**
     * 内存AKSK认证器
     * <p>当配置为memory或没有其他认证器时启用</p>
     */
    @Bean
    @ConditionalOnMissingBean(AkSkAuthenticator.class)
    public AkSkAuthenticator memoryAkSkAuthenticator(AuthServerProperties authServerProperties) {
        log.info("启用内存AKSK认证器");
        MemoryAkSkAuthenticator authenticator = new MemoryAkSkAuthenticator();

        // 如果是开发环境，初始化一些默认配置
        if (isDevEnvironment()) {
            authenticator.initDefaultConfigs();
        }

        return authenticator;
    }

    /**
     * 判断是否为开发环境
     */
    private boolean isDevEnvironment() {
        String[] activeProfiles = ((org.springframework.core.env.Environment) (org.springframework.context.ApplicationContextAware) this).getActiveProfiles();

        for (String profile : activeProfiles) {
            if ("dev".equals(profile) || "development".equals(profile) || "test".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
