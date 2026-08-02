package com.chua.starter.tenant.server.sync;

import com.chua.common.support.network.sync.SyncMessageHandler;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.sync.support.pojo.ClientInfo;
import com.chua.starter.sync.support.server.SyncServer;
import com.chua.starter.sync.support.server.SyncServerInstance;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 处理租户客户端主动发起的全量同步请求。
 *
 * @author CH
 * @since 2026/03/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean({SyncServer.class, TenantServerSyncPublisher.class})
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
public class TenantServerSyncRequestHandler implements SyncMessageHandler {

    private final TenantProperties tenantProperties;
    private final SyncServer syncServer;
    private final TenantServerSyncPublisher syncPublisher;

    @Override
    @SuppressWarnings("unchecked")
    public void handle(String topic, Object message) {
        Map<String, Object> data = new HashMap<>();
        if (message instanceof Map<?, ?> rawMap) {
            rawMap.forEach((key, value) -> data.put(String.valueOf(key), value));
        } else {
            data.put("data", message);
        }

        String sessionId = MapUtils.getString(data, SyncServerInstance.SYNC_MESSAGE_SOURCE_CLIENT_ID);
        if (sessionId == null || sessionId.isBlank()) {
            log.warn("[租户服务端] 全量同步请求缺少源会话: topic={}", topic);
            return;
        }

        ClientInfo clientInfo = syncServer.getAllClientsRaw().get(sessionId);
        String boundTenantId = TenantSyncMessageSupport.resolveTenantId(clientInfo);

        TenantSyncMessageSupport.VerificationResult verificationResult = verifySignature(data);
        if (!verificationResult.isValid()) {
            log.warn("[租户服务端] 拒绝签名无效的全量同步请求: sessionId={}, reason={}",
                    sessionId, verificationResult.getMessage());
            return;
        }

        if (!TenantSyncMessageSupport.hasText(boundTenantId)) {
            log.warn("[租户服务端] 拒绝未绑定租户的全量同步请求: sessionId={}", sessionId);
            return;
        }

        String tenantId = TenantSyncMessageSupport.resolveTargetTenantId(data);
        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            tenantId = boundTenantId;
        }

        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            log.warn("[租户服务端] 收到全量同步请求但缺少 tenantId: sessionId={}", sessionId);
            return;
        }

        if (TenantSyncMessageSupport.hasText(boundTenantId) && !boundTenantId.equals(tenantId)) {
            log.warn("[租户服务端] 拒绝跨租户全量同步请求: sessionId={}, boundTenantId={}, requestedTenantId={}",
                    sessionId, boundTenantId, tenantId);
            return;
        }

        String reason = MapUtils.getString(data, TenantSyncMessageSupport.SYNC_REASON, "client-request");
        syncPublisher.publishFullSyncToSession(sessionId, tenantId, reason);
    }

    private TenantSyncMessageSupport.VerificationResult verifySignature(Map<String, Object> data) {
        if (!tenantProperties.isServerSyncSignEnabled()) {
            return TenantSyncMessageSupport.VerificationResult.skip("server sign 未启用");
        }

        long allowedSkewMillis = Math.max(0L, tenantProperties.getServer().getSyncSignAllowedClockSkewSeconds()) * 1000L;
        return TenantSyncMessageSupport.verifySignature(
                data,
                tenantProperties.getServer().getSyncSignSecret(),
                allowedSkewMillis
        );
    }
}
