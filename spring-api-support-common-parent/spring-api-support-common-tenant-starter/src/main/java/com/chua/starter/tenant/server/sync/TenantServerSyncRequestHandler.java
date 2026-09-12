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
* @since 2026/03/25
* @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean({SyncServer.class, TenantServerSyncPublisher.class})
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
public class TenantServerSyncRequestHandler implements SyncMessageHandler {

    /** Tenant属性 */
    private final TenantProperties tenantProperties;
    /** 同步服务器 */
    private final SyncServer syncServer;
    /** 同步publisher */
    private final TenantServerSyncPublisher syncPublisher;

    @Override
    /** 获取名称 */
    public String getName() {
        return "tenant-server-sync-request";
    }

    @Override
    @SuppressWarnings("unchecked")
    /**
    * 处理
    *
    * @param topic topic
    * @param sessionId 会话id
    * @param data 数据
    * @return 处理的结果
     */
    public Object handle(String topic, String sessionId, Map<String, Object> data) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        if (sessionId == null || sessionId.isBlank()) {
            log.warn("[租户服务端] 全量同步请求缺少源会话: topic={}", topic);
            return null;
        }

        ClientInfo clientInfo = syncServer.getAllClientsRaw().get(sessionId);
        String boundTenantId = TenantSyncMessageSupport.resolveTenantId(clientInfo);

        TenantSyncMessageSupport.VerificationResult verificationResult = verifySignature(data);
        if (!verificationResult.isValid()) {
            log.warn("[租户服务端] 拒绝签名无效的全量同步请求: sessionId={}, reason={}",
                    sessionId, verificationResult.getMessage());
            return null;
        }

        if (!TenantSyncMessageSupport.hasText(boundTenantId)) {
            log.warn("[租户服务端] 拒绝未绑定租户的全量同步请求: sessionId={}", sessionId);
            return null;
        }

        String tenantId = TenantSyncMessageSupport.resolveTargetTenantId(data);
        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            tenantId = boundTenantId;
        }

        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            log.warn("[租户服务端] 收到全量同步请求但缺少 tenantId: sessionId={}", sessionId);
            return null;
        }

        if (TenantSyncMessageSupport.hasText(boundTenantId) && !boundTenantId.equals(tenantId)) {
            log.warn("[租户服务端] 拒绝跨租户全量同步请求: sessionId={}, boundTenantId={}, requestedTenantId={}",
                    sessionId, boundTenantId, tenantId);
            return null;
        }

        String reason = MapUtils.getString(data, TenantSyncMessageSupport.SYNC_REASON, "client-request");
        syncPublisher.publishFullSyncToSession(sessionId, tenantId, reason);
        return null;
    }

    /**
    * 验证签名
    *
    * @param data 数据
    * @return 验证签名的结果
     */
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
