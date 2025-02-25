package com.chua.starter.tenant.service;

import com.chua.common.support.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 消息发布者
 *
 * @author CH
 */
public class RedisTenantMessageQueue implements TenantMessageQueue {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void publish(String channel, Object message) {
        redisTemplate.opsForList()
                .rightPush(channel, message instanceof String ? (String) message : Json.toJson(message));
    }
}
