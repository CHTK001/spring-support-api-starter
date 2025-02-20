package com.chua.starter.tenant.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 消息发布者
 *
 * @author CH
 */
public class RedisMessagePublisher implements MessagePublisher {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void publish(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }
}
