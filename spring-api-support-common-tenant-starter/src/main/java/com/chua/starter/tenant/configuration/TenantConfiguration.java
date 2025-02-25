package com.chua.starter.tenant.configuration;


import com.chua.starter.tenant.listener.*;
import com.chua.starter.tenant.redis.ServiceMessageListenerAdapter;
import com.chua.starter.tenant.redis.TenantMessageListenerAdapter;
import com.chua.starter.tenant.service.RedisTenantMessagePublisher;
import com.chua.starter.tenant.service.RedisTenantMessageQueue;
import com.chua.starter.tenant.service.TenantMessagePublisher;
import com.chua.starter.tenant.service.TenantMessageQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 租户配置
 *
 * @author CH
 */
@Configuration
public class TenantConfiguration {

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory,
                                                   @Autowired(required = false) TenantMessageListenerAdapter tenantMessageListenerAdapter,
                                                   @Autowired(required = false) ServiceMessageListenerAdapter serviceMessageListenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(tenantMessageListenerAdapter, new PatternTopic("tenant"));
        container.addMessageListener(serviceMessageListenerAdapter, new PatternTopic("service"));
        return container;
    }


    /**
     * 监听
     *
     * @return
     */
    @Bean
    public RedisTenantQueueListener queueListener(StringRedisTemplate redisTemplate,
                                                  TenantMessageQueue tenantMessageQueue,
                                                  TenantMessageListener tenantMessageListener,
                                                  ServiceMessageListener serviceMessageListener
    ) {
        return new RedisTenantQueueListener(redisTemplate, tenantMessageQueue, tenantMessageListener, serviceMessageListener);
    }


    @Bean
    public TenantMessageQueue queue() {
        return new RedisTenantMessageQueue();
    }

    @Bean
    public TenantMessageListenerAdapter listenerAdapter(TenantMessageListener listener) {
        return new TenantMessageListenerAdapter(listener);
    }
    @Bean
    public ServiceMessageListenerAdapter listenerAdapter2(ServiceMessageListener listener) {
        return new ServiceMessageListenerAdapter(listener);
    }

    @Bean
    public TenantMessageListener tenantMessageListener() {
        return new RedisTenantMessageListener();
    }
    @Bean
    public ServiceMessageListener serviceMessageListener() {
        return new RedisServiceMessageListener();
    }

    @Bean
    public TenantMessagePublisher messagePublisher() {
        return new RedisTenantMessagePublisher();
    }
}
