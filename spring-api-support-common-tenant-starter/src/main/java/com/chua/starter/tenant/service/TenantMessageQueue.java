package com.chua.starter.tenant.service;

/**
 * 消息发布者
 *
 * @author CH
 */
public interface TenantMessageQueue {


    /**
     * 发布队列消息
     *
     * @param channel 频道
     * @param message 消息
     */
    void publish(String channel, Object message);

}
