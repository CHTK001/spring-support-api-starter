package com.chua.starter.oauth.sso.support.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis令牌管理
 *
 * @author CH
 * @since 4.0.0
 */
@Slf4j
@ConditionalOnProperty(prefix = "plugin.oauth.server", name = {"tokenManagement", "token-management"}, havingValue = "redis", matchIfMissing = false)
public class EnableRedisTokenManagerConfiguration {


    /**
     * redistemplate
     *
     * @param redisConnectionFactory 连接工厂
     * @return redisTemplate
     */
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;

    }

    /**
     * 监听器Container。
     *
     * @param connectionFactory 连接工厂，不允许为 null
     * @return Redis消息监听器Container 对象
     */
    @Bean
    @SuppressWarnings("all")
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    RedisMessageListenerContainer listenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory);
        return listenerContainer;
    }

    /**
     * 注册Redis键过期监听器
     * @param listenerContainer 监听器Container，不允许为 null
     * @return 键ExpirationEvent消息监听器 对象
     */
    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    KeyExpirationEventMessageListener redisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        return new RedisKeyExpirationListener(listenerContainer);
    }

    /**
     * 字符串redistemplate
     *
     * @param redisConnectionFactory 连接工厂
     * @return StringRedisTemplate
     * @author CH
     * @since 4.0.0
     */
    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    @SuppressWarnings("all")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;

    }

    public static class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
        /**
         * 创建 新 {@link MessageListener} for {@code __keyevent@*__:expired} 消息.
         *
         * @param listenerContainer must not be {@literal null}.
         */
        public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
            super(listenerContainer);
        }

        @Override
        /**
         * on消息
        */
        public void onMessage(Message message, byte[] pattern) {
 // 获取过期的键
            String expireKey = message.toString();
            //信息打印
            log.info("key : {} 失效", expireKey);
        }
    }
}
