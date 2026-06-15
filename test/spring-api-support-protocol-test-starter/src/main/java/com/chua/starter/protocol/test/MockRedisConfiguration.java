package com.chua.starter.protocol.test;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * 测试环境下 Mock Redis 连接（使用 Lettuce lazy 模式，不真正连接 Redis）
 */
@Configuration(proxyBeanMethods = false)
public class MockRedisConfiguration {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("127.0.0.1", 6379);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setValidateConnection(false);
        factory.setShareNativeConnection(false);
        // lettuce 默认 lazy connect, 不会在启动时校验
        return factory;
    }
}
