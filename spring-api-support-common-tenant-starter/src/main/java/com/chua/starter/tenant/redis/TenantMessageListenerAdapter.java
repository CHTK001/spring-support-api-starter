package com.chua.starter.tenant.redis;

import com.chua.starter.tenant.listener.TenantMessageListener;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 租户
 *
 * @author CH
 */
public class TenantMessageListenerAdapter extends MessageListenerAdapter {
    public TenantMessageListenerAdapter(TenantMessageListener listener) {
        super(listener, "listen");
    }
}
