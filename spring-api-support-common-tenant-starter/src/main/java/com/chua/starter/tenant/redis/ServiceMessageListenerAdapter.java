package com.chua.starter.tenant.redis;

import com.chua.starter.tenant.listener.ServiceMessageListener;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 租户服务
 *
 * @author CH
 */
public class ServiceMessageListenerAdapter extends MessageListenerAdapter {
    public ServiceMessageListenerAdapter(ServiceMessageListener listener) {
        super(listener, "listen");
    }
}
