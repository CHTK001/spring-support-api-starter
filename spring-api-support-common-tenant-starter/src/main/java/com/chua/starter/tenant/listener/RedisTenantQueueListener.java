package com.chua.starter.tenant.listener;

import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.tenant.service.TenantMessageQueue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 监听队列
 *
 * @author CH
 */
public class RedisTenantQueueListener implements InitializingBean, DisposableBean {


    private static final AtomicBoolean RUNNING = new AtomicBoolean(true);
    private final StringRedisTemplate redisTemplate;
    private final TenantMessageQueue tenantMessageQueue;
    private final TenantMessageListener tenantMessageListener;
    private final ServiceMessageListener serviceMessageListener;
    private Thread tenantThreads = null;
    private Thread tenantServiceThreads = null;
    @Value("${plugin.control.platform.name:system}")
    private String plantfrom;
    public RedisTenantQueueListener(StringRedisTemplate redisTemplate, TenantMessageQueue tenantMessageQueue, TenantMessageListener tenantMessageListener, ServiceMessageListener serviceMessageListener) {
        this.redisTemplate = redisTemplate;
        this.tenantMessageQueue = tenantMessageQueue;
        this.tenantMessageListener = tenantMessageListener;
        this.serviceMessageListener = serviceMessageListener;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ListOperations<String, String> stringStringListOperations = redisTemplate.opsForList();
        tenantThreads = Thread.ofPlatform().daemon().name("tenant-queue-listener").start(() -> {
            while (RUNNING.get()) {
                String message = stringStringListOperations.leftPop("tenant", 5, TimeUnit.SECONDS);
                if (message == null) {
                    continue;
                }
                if (!"tenant".equalsIgnoreCase(plantfrom)) {
                    tenantMessageQueue.publish("tenant", message);
                    return;
                }
                tenantMessageListener.listen(message);
            }
        });
        tenantServiceThreads = Thread.ofPlatform().daemon().name("tenant-service-queue-listener").start(() -> {
            while (RUNNING.get()) {
                String message = stringStringListOperations.leftPop("service", 5, TimeUnit.SECONDS);
                if (message == null) {
                    continue;
                }
                if (!"tenant".equalsIgnoreCase(plantfrom)) {
                    tenantMessageQueue.publish("service", message);
                    return;
                }
                tenantMessageListener.listen(message);
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        RUNNING.set(false);
        ThreadUtils.closeQuietly(tenantThreads);
        ThreadUtils.closeQuietly(tenantServiceThreads);
    }
}
