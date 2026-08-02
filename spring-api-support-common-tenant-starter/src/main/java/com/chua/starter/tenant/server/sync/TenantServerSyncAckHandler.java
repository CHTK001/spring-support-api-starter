package com.chua.starter.tenant.server.sync;

import com.chua.common.support.utils.MapUtils;
import com.chua.starter.sync.support.pojo.ClientInfo;
import com.chua.starter.sync.support.server.SyncServer;
import com.chua.starter.sync.support.spi.SyncMessageHandler;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 处理租户客户端上报的同步 ACK。
 *
 * @author CH
 * @since 2026/03/25
 */
@Component
@RequiredArgsConstructor
@ConditionalOnBean({SyncServer.class, TenantSyncAckTracker.class})
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
public class TenantServerSyncAckHandler implements SyncMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(TenantServerSyncAckHandler.class);

    private final TenantProperties tenantProperties;
    private final SyncServer syncServer;
    private final TenantSyncAckTracker ackTracker;

    @Override
    public String getName() {
        return "tenant-server-sync-ack";
    }

    @Override
    public Object handle(String topic, String sessionId, Map<String, Object> data) {
        ClientInfo clientInfo = syncServer.getAllClientsRaw().get(sessionId);
        String boundTenantId = TenantSyncMessageSupport.resolveTenantId(clientInfo);

        TenantSyncMessageSupport.VerificationResult verificationResult = verifySignature(data);
        if (!verificationResult.isValid()) {
            log.warn("[租户服务端] 拒绝签名无效的 ACK: sessionId={}, reason={}",
                    sessionId, verificationResult.getMessage());
            return Map.of("success", false, "message", verificationResult.getMessage(), "invalidSignature", true);
        }

        if (!TenantSyncMessageSupport.hasText(boundTenantId)) {
            log.warn("[租户服务端] 收到未绑定租户会话的 ACK: sessionId={}", sessionId);
            return Map.of("success", false, "message", "当前会话未绑定 tenantId");
        }

        String tenantId = TenantSyncMessageSupport.resolveTargetTenantId(data);
        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            tenantId = boundTenantId;
        }
        if (!boundTenantId.equals(tenantId)) {
            log.warn("[租户服务端] 拒绝跨租户 ACK: sessionId={}, boundTenantId={}, ackTenantId={}",
                    sessionId, boundTenantId, tenantId);
            return Map.of("success", false, "message", "tenantId 与当前会话不匹配");
        }

        String sourceMessageId = MapUtils.getString(data, TenantSyncMessageSupport.SOURCE_MESSAGE_ID);
        if (!TenantSyncMessageSupport.hasText(sourceMessageId)) {
            return Map.of("success", false, "message", "sourceMessageId 为空");
        }

        TenantSyncAckTracker.AckRecord ackRecord = ackTracker.acknowledge(sessionId, data);
        boolean ackSuccess = ackRecord != null && ackRecord.isSuccess();
        String ackStatus = ackRecord == null ? null : ackRecord.getStatus();
        if (ackRecord != null && !ackRecord.isSuccess()) {
            log.warn("[租户服务端] 收到失败 ACK: sessionId={}, tenantId={}, topic={}, messageId={}, status={}, message={}",
                    sessionId, tenantId, ackRecord.getTopic(), ackRecord.getSourceMessageId(),
                    ackRecord.getStatus(), ackRecord.getMessage());
        }
        return Map.of(
                "success", true,
                "tenantId", tenantId,
                "ackSuccess", ackSuccess,
                "ackStatus", ackStatus == null ? "" : ackStatus,
                "sourceMessageId", sourceMessageId
        );
    }

    @Override
    public boolean supports(String topic) {
        return TenantSyncMessageSupport.TOPIC_TENANT_SYNC_ACK.equals(topic);
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
