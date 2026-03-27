package com.chua.starter.tenant.server.sync;

import com.chua.common.support.core.utils.MapUtils;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 租户同步 ACK 跟踪器。
 * <p>
 * 当前仅面向平台单机部署，采用内存态缓存跟踪最近发送和确认状态。
 * </p>
 *
 * @author CH
 * @since 2026/03/25
 */
@Component
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
public class TenantSyncAckTracker {

    private final boolean enabled;
    private final int maxPending;
    private final ConcurrentMap<String, PendingAck> pendingAcks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AckRecord> lastAckRecords = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> compensationTimes = new ConcurrentHashMap<>();

    public TenantSyncAckTracker(TenantProperties tenantProperties) {
        TenantProperties.ServerConfig serverConfig = tenantProperties == null ? new TenantProperties.ServerConfig() : tenantProperties.getServer();
        this.enabled = tenantProperties != null && tenantProperties.isServerSyncAckEnabled();
        this.maxPending = serverConfig.getSyncAckMaxPending();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void track(String sessionId, String tenantId, String topic, Map<String, Object> payload) {
        if (!isEnabled()
                || !TenantSyncMessageSupport.hasText(sessionId)
                || !TenantSyncMessageSupport.hasText(tenantId)
                || !TenantSyncMessageSupport.hasText(topic)
                || payload == null
                || payload.isEmpty()) {
            return;
        }

        String messageId = TenantSyncMessageSupport.resolveMessageId(payload);
        if (!TenantSyncMessageSupport.hasText(messageId)) {
            return;
        }

        long sentAt = TenantSyncMessageSupport.resolveTimestamp(payload);
        if (sentAt <= 0) {
            sentAt = System.currentTimeMillis();
        }

        String reason = MapUtils.getString(payload, TenantSyncMessageSupport.SYNC_REASON);
        pendingAcks.put(buildPendingKey(sessionId, messageId), new PendingAck(
                sessionId,
                tenantId,
                topic,
                messageId,
                sentAt,
                reason
        ));
        evictOverflow();
    }

    public AckRecord acknowledge(String sessionId, Map<String, Object> payload) {
        if (!TenantSyncMessageSupport.hasText(sessionId) || payload == null || payload.isEmpty()) {
            return null;
        }

        String tenantId = TenantSyncMessageSupport.resolveTargetTenantId(payload);
        String messageId = MapUtils.getString(payload, TenantSyncMessageSupport.SOURCE_MESSAGE_ID);
        String topic = MapUtils.getString(payload, TenantSyncMessageSupport.SOURCE_TOPIC);
        boolean success = Boolean.TRUE.equals(MapUtils.getBoolean(payload, TenantSyncMessageSupport.ACK_SUCCESS, Boolean.TRUE));
        String status = MapUtils.getString(payload, TenantSyncMessageSupport.ACK_STATUS);
        String message = MapUtils.getString(payload, TenantSyncMessageSupport.ACK_ERROR_MESSAGE);
        long ackAt = TenantSyncMessageSupport.resolveTimestamp(payload);
        if (ackAt <= 0) {
            ackAt = System.currentTimeMillis();
        }

        AckRecord record = new AckRecord(sessionId, tenantId, topic, messageId, success, status, message, ackAt);
        lastAckRecords.put(buildAckKey(sessionId, tenantId, topic), record);
        if (success && TenantSyncMessageSupport.hasText(messageId)) {
            pendingAcks.remove(buildPendingKey(sessionId, messageId));
        }
        evictOverflow();
        return record;
    }

    public List<PendingAck> drainExpired(long timeoutMillis) {
        if (!isEnabled() || timeoutMillis <= 0 || pendingAcks.isEmpty()) {
            return List.of();
        }

        long expireBefore = System.currentTimeMillis() - timeoutMillis;
        List<PendingAck> expired = new ArrayList<>();
        for (Map.Entry<String, PendingAck> entry : pendingAcks.entrySet()) {
            PendingAck pendingAck = entry.getValue();
            if (pendingAck.getSentAt() > expireBefore) {
                continue;
            }
            if (pendingAcks.remove(entry.getKey(), pendingAck)) {
                expired.add(pendingAck);
            }
        }

        expired.sort(Comparator.comparingLong(PendingAck::getSentAt));
        cleanupCompensations(expireBefore);
        return expired;
    }

    public boolean tryMarkCompensation(String sessionId, String tenantId, long cooldownMillis) {
        if (!TenantSyncMessageSupport.hasText(sessionId) || !TenantSyncMessageSupport.hasText(tenantId)) {
            return false;
        }

        long now = System.currentTimeMillis();
        long safeCooldown = Math.max(0L, cooldownMillis);
        String key = buildCompensationKey(sessionId, tenantId);
        Long previous = compensationTimes.get(key);
        if (previous != null && safeCooldown > 0 && now - previous < safeCooldown) {
            return false;
        }

        compensationTimes.put(key, now);
        cleanupCompensations(now - Math.max(safeCooldown, 60000L));
        return true;
    }

    public void clearSession(String sessionId) {
        if (!TenantSyncMessageSupport.hasText(sessionId)) {
            return;
        }

        pendingAcks.entrySet().removeIf(entry -> sessionId.equals(entry.getValue().getSessionId()));
        lastAckRecords.entrySet().removeIf(entry -> entry.getKey().startsWith(sessionId + "|"));
        compensationTimes.keySet().removeIf(key -> key.startsWith(sessionId + "|"));
    }

    private void evictOverflow() {
        if (maxPending <= 0) {
            return;
        }

        trimOldest(pendingAcks, maxPending, PendingAck::getSentAt);
        trimOldest(lastAckRecords, maxPending, AckRecord::getAckAt);
        trimOldest(compensationTimes, maxPending, Long::longValue);
    }

    private <T> void trimOldest(ConcurrentMap<String, T> values, int maxSize, java.util.function.ToLongFunction<T> sorter) {
        if (values.size() <= maxSize) {
            return;
        }

        values.entrySet().stream()
                .sorted(Comparator.comparingLong(entry -> sorter.applyAsLong(entry.getValue())))
                .limit(values.size() - maxSize)
                .map(Map.Entry::getKey)
                .toList()
                .forEach(values::remove);
    }

    private void cleanupCompensations(long expireBefore) {
        compensationTimes.entrySet().removeIf(entry -> entry.getValue() < expireBefore);
    }

    private String buildPendingKey(String sessionId, String messageId) {
        return sessionId + "|" + messageId;
    }

    private String buildAckKey(String sessionId, String tenantId, String topic) {
        return sessionId + "|" + tenantId + "|" + topic;
    }

    private String buildCompensationKey(String sessionId, String tenantId) {
        return sessionId + "|" + tenantId;
    }

    public static final class PendingAck {
        private final String sessionId;
        private final String tenantId;
        private final String topic;
        private final String messageId;
        private final long sentAt;
        private final String reason;

        public PendingAck(String sessionId, String tenantId, String topic, String messageId, long sentAt, String reason) {
            this.sessionId = sessionId;
            this.tenantId = tenantId;
            this.topic = topic;
            this.messageId = messageId;
            this.sentAt = sentAt;
            this.reason = reason;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getTenantId() {
            return tenantId;
        }

        public String getTopic() {
            return topic;
        }

        public String getMessageId() {
            return messageId;
        }

        public long getSentAt() {
            return sentAt;
        }

        public String getReason() {
            return reason;
        }
    }

    public static final class AckRecord {
        private final String sessionId;
        private final String tenantId;
        private final String topic;
        private final String sourceMessageId;
        private final boolean success;
        private final String status;
        private final String message;
        private final long ackAt;

        public AckRecord(String sessionId, String tenantId, String topic, String sourceMessageId, boolean success, String status, String message, long ackAt) {
            this.sessionId = sessionId;
            this.tenantId = tenantId;
            this.topic = topic;
            this.sourceMessageId = sourceMessageId;
            this.success = success;
            this.status = status;
            this.message = message;
            this.ackAt = ackAt;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getTenantId() {
            return tenantId;
        }

        public String getTopic() {
            return topic;
        }

        public String getSourceMessageId() {
            return sourceMessageId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public long getAckAt() {
            return ackAt;
        }
    }
}
