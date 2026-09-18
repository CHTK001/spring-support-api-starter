package com.chua.starter.tenant.client;

import com.chua.common.support.network.sync.SyncFlowListener;
import com.chua.starter.sync.support.client.SyncClient;
import com.chua.starter.sync.support.spi.SyncClientCustomizer;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 租户客户端同步定制器。
 * <p>
 * 在 同步客户端 建立底层连接前预注册主题和处理器，
 * 避免首次注册后立刻收到全量同步时处理器尚未挂载。
 * </p>
 *
 * @since 2026/03/25
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".client", name = "enable", havingValue = "true")
public class TenantSyncClientCustomizer implements SyncClientCustomizer, DisposableBean {

    /** ReasonML_客户端_连接 */
    private static final String REASON_CLIENT_CONNECT = "client-connect";

    /** Tenant属性 */
    private final TenantProperties tenantProperties;
    /** Tenant客户端同步处理器 */
    private final TenantClientSyncHandler tenantClientSyncHandler;
    /** 调度器 */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "tenant-sync-client-customizer");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    /** 获取订单 */
    public int getOrder() {
        return -100;
    }

    @Override
    /** Customize */
    public void customize(SyncClient syncClient) {
        if (!tenantProperties.isClientMode() || !tenantProperties.getClient().isSyncEnable()) {
            return;
        }

        if (tenantProperties.getClient().isSyncAutoSubscribeTopics()) {
            subscribe(syncClient, TenantClientSyncHandler.TOPIC_TENANT_UPDATE);
            subscribe(syncClient, TenantClientSyncHandler.TOPIC_TENANT_DELETE);
            subscribe(syncClient, TenantClientSyncHandler.TOPIC_TENANT_SERVICE);
            subscribe(syncClient, TenantClientSyncHandler.TOPIC_TENANT_MENU);
            log.info("[租户客户端] 连接前自动注册同步主题完成");
        }

        syncClient.addListener(new SyncFlowListener() {
            @Override
            /** on客户端连接 */
            public void onClientConnected(String clientId) {
                scheduleFullSyncRequest(syncClient);
            }
        });

 // 发起底层连接（幂等），等待平台端 同步-服务端 接受后触发全量同步
        syncClient.connect();
    }

    /**
     * 订阅指定主题
     *
     * @param syncClient 同步客户端
     * @param topic      主题
     */
    private void subscribe(SyncClient syncClient, String topic) {
        syncClient.registerHandler(topic, tenantClientSyncHandler);
    }

    /**
     * 调度完整同步请求
     *
     * @param syncClient 同步客户端
     */
    private void scheduleFullSyncRequest(SyncClient syncClient) {
        if (!tenantProperties.getClient().isSyncRequestFullPushOnConnect()) {
            return;
        }

        long connectedAt = System.currentTimeMillis();
        long delayMillis = Math.max(0L, tenantProperties.getClient().getSyncRequestFullPushDelayMillis());
        scheduler.schedule(
                () -> requestFullSyncIfNecessary(syncClient, connectedAt, REASON_CLIENT_CONNECT),
                delayMillis,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * 请求完整同步ifnecessary
     *
     * @param syncClient 同步客户端
     * @param connectedAt 连接at
     * @param reason ReasonML
     */
    private void requestFullSyncIfNecessary(SyncClient syncClient, long connectedAt, String reason) {
        if (!syncClient.isConnected()) {
            return;
        }

        if (tenantClientSyncHandler.hasReceivedSyncMessageSince(connectedAt)) {
            log.debug("[租户客户端] 连接后已收到租户同步消息，跳过主动全量同步补偿");
            return;
        }

        requestFullSync(syncClient, reason);
    }

    /**
     * 请求完整同步
     *
     * @param syncClient 同步客户端
     * @param reason ReasonML
     */
    private void requestFullSync(SyncClient syncClient, String reason) {
        String tenantId = tenantProperties.getClient().getTenantId();
        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            log.warn("[租户客户端] 未配置 tenantId，跳过主动全量同步请求");
            return;
        }

        Map<String, Object> message = TenantSyncMessageSupport.createEnvelope(
                tenantId,
                TenantSyncMessageSupport.MODE_FULL,
                reason
        );
        if (tenantProperties.isClientSyncSignEnabled()) {
            TenantSyncMessageSupport.sign(message, tenantProperties.getClient().getSyncSignSecret());
        }
        syncClient.publish(TenantSyncMessageSupport.TOPIC_TENANT_SYNC_REQUEST, message);
        log.info("[租户客户端] 已发送全量同步请求: tenantId={}, reason={}", tenantId, reason);
    }

    @Override
    /** 销毁 */
    public void destroy() {
        scheduler.shutdownNow();
    }
}
