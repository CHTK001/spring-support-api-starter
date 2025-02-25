package com.chua.starter.tenant.service;

import com.chua.common.support.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 消息发布者
 *
 * @author CH
 */
public class RedisTenantMessagePublisher implements TenantMessagePublisher {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void publish(String channel, Object message) {
        redisTemplate.convertAndSend(channel, Json.toJson(message));
    }
}
