package com.chua.starter.tenant.configuration;


import com.chua.starter.tenant.listener.MessageListener;
import com.chua.starter.tenant.listener.RedisMessageListener;
import com.chua.starter.tenant.service.MessagePublisher;
import com.chua.starter.tenant.service.RedisMessagePublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 租户配置
 *
 * @author CH
 */
@Configuration
public class TenantConfiguration {

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory, MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("tenant"));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(MessageListener listener) {
        return new MessageListenerAdapter(listener, "listener");
    }


    @Bean
    public MessageListener messageListener() {
        return new RedisMessageListener();
    }

    @Bean
    public MessagePublisher messagePublisher() {
        return new RedisMessagePublisher();
    }
}
