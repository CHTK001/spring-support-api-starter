package com.chua.starter.tenant.server.sync;

import com.chua.common.support.utils.MapUtils;
import com.chua.starter.sync.support.pojo.ClientInfo;
import com.chua.starter.sync.support.server.SyncServer;
import com.chua.starter.sync.support.spi.SyncMessageHandler;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

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
    public String getName() {
        return "tenant-server-sync-request";
    }

    @Override
    public Object handle(String topic, String sessionId, Map<String, Object> data) {
        ClientInfo clientInfo = syncServer.getAllClientsRaw().get(sessionId);
        String boundTenantId = TenantSyncMessageSupport.resolveTenantId(clientInfo);

        TenantSyncMessageSupport.VerificationResult verificationResult = verifySignature(data);
        if (!verificationResult.isValid()) {
            log.warn("[租户服务端] 拒绝签名无效的全量同步请求: sessionId={}, reason={}",
                    sessionId, verificationResult.getMessage());
            return Map.of("success", false, "message", verificationResult.getMessage(), "invalidSignature", true);
        }

        if (!TenantSyncMessageSupport.hasText(boundTenantId)) {
            log.warn("[租户服务端] 拒绝未绑定租户的全量同步请求: sessionId={}", sessionId);
            return Map.of("success", false, "message", "当前会话未绑定 tenantId");
        }

        String tenantId = TenantSyncMessageSupport.resolveTargetTenantId(data);
        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            tenantId = boundTenantId;
        }

        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            log.warn("[租户服务端] 收到全量同步请求但缺少 tenantId: sessionId={}", sessionId);
            return Map.of("success", false, "message", "tenantId 为空");
        }

        if (TenantSyncMessageSupport.hasText(boundTenantId) && !boundTenantId.equals(tenantId)) {
            log.warn("[租户服务端] 拒绝跨租户全量同步请求: sessionId={}, boundTenantId={}, requestedTenantId={}",
                    sessionId, boundTenantId, tenantId);
            return Map.of("success", false, "message", "tenantId 与当前会话不匹配");
        }

        String reason = MapUtils.getString(data, TenantSyncMessageSupport.SYNC_REASON, "client-request");
        boolean result = syncPublisher.publishFullSyncToSession(sessionId, tenantId, reason);
        return Map.of(
                "success", result,
                "tenantId", tenantId,
                "topic", TenantSyncMessageSupport.TOPIC_TENANT_SYNC_REQUEST
        );
    }

    @Override
    public boolean supports(String topic) {
        return TenantSyncMessageSupport.TOPIC_TENANT_SYNC_REQUEST.equals(topic);
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
