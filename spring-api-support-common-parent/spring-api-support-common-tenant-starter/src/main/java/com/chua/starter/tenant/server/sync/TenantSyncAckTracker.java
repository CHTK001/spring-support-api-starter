package com.chua.starter.tenant.server.sync;

import com.chua.common.support.utils.MapUtils;
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
 * @since 2026/03/25
 * @author CH
 */
@Component
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
public class TenantSyncAckTracker {

    /** 是否启用 */
    private final boolean enabled;
    /** 最大值pending */
    private final int maxPending;
    private final ConcurrentMap<String, PendingAck> pendingAcks = new ConcurrentHashMap<>(); // pendingACK
    private final ConcurrentMap<String, AckRecord> lastAckRecords = new ConcurrentHashMap<>(); // 最后一个ACKrecords
    private final ConcurrentMap<String, Long> compensationTimes = new ConcurrentHashMap<>(); // 报酬时间

    /**
     * 创建 tenant同步ACKtracker 实例
     * @param tenantProperties tenant属性
     */
    public TenantSyncAckTracker(TenantProperties tenantProperties) {
        TenantProperties.ServerConfig serverConfig = tenantProperties == null ? new TenantProperties.ServerConfig() : tenantProperties.getServer();
        this.enabled = tenantProperties != null && tenantProperties.isServerSyncAckEnabled();
        this.maxPending = serverConfig.getSyncAckMaxPending();
    }

    /**
     * 是否已启用
     *
     * @return 是否已启用的结果
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Track
     *
     * @param sessionId 会话id
     * @param tenantId tenantId
     * @param topic topic
     * @param payload payload
     */
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

    /**
     * Acknowledge
     *
     * @param sessionId 会话id
     * @param payload payload
     * @return acknowledge的结果
     */
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

    /**
     * drainexpired
     *
     * @param timeoutMillis 超时millis
     * @return drainExpired的结果
     */
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

    /**
     * 尝试标记报酬
     *
     * @param sessionId 会话id
     * @param tenantId tenantId
     * @param cooldownMillis cooldownMillis
     * @return 尝试mark报酬的结果
     */
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

    /**
     * clear会话
     *
     * @param sessionId 会话id
     */
    public void clearSession(String sessionId) {
        if (!TenantSyncMessageSupport.hasText(sessionId)) {
            return;
        }

        pendingAcks.entrySet().removeIf(entry -> sessionId.equals(entry.getValue().getSessionId()));
        lastAckRecords.entrySet().removeIf(entry -> entry.getKey().startsWith(sessionId + "|"));
        compensationTimes.keySet().removeIf(key -> key.startsWith(sessionId + "|"));
    }

    /** evictoverflow */
    private void evictOverflow() {
        if (maxPending <= 0) {
            return;
        }

        trimOldest(pendingAcks, maxPending, PendingAck::getSentAt);
        trimOldest(lastAckRecords, maxPending, AckRecord::getAckAt);
        trimOldest(compensationTimes, maxPending, Long::longValue);
    }

    /**
     * 去空格Oldest
     *
     * @param values 值
     * @param maxSize 最大大小
     * @param sorter 排序
     * @return 修剪旧的结果
     */
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

    /**
     * cleanup报酬
     *
     * @param expireBefore expire之前
     */
    private void cleanupCompensations(long expireBefore) {
        compensationTimes.entrySet().removeIf(entry -> entry.getValue() < expireBefore);
    }

    /**
     * 构建pending键
     *
     * @param sessionId 会话id
     * @param messageId 消息id
     * @return 构建pending键的结果
     */
    private String buildPendingKey(String sessionId, String messageId) {
        return sessionId + "|" + messageId;
    }

    /**
     * 构建ACK键
     *
     * @param sessionId 会话id
     * @param tenantId tenantId
     * @param topic topic
     * @return 构建ACK键的结果
     */
    private String buildAckKey(String sessionId, String tenantId, String topic) {
        return sessionId + "|" + tenantId + "|" + topic;
    }

    /**
     * 构建报酬键
     *
     * @param sessionId 会话id
     * @param tenantId tenantId
     * @return 构建报酬键的结果
     */
    private String buildCompensationKey(String sessionId, String tenantId) {
        return sessionId + "|" + tenantId;
    }

    public static final class PendingAck {
        /** 会话标识 */
        private final String sessionId;
        /** tenantid */
        private final String tenantId;
        /** Topic */
        private final String topic;
        /** 消息标识 */
        private final String messageId;
        /** sentat */
        private final long sentAt;
        /** ReasonML */
        private final String reason;

        /**
         * 创建 pendingACK 实例
         * @param sessionId 会话id
         * @param tenantId tenantid
         * @param topic topic
         * @param messageId 消息id
         * @param sentAt sentat
         * @param reason ReasonML
         * @return pendingACK的结果
         */
        public PendingAck(String sessionId, String tenantId, String topic, String messageId, long sentAt, String reason) {
            this.sessionId = sessionId;
            this.tenantId = tenantId;
            this.topic = topic;
            this.messageId = messageId;
            this.sentAt = sentAt;
            this.reason = reason;
        }

        /**
         * 获取会话id
         *
         * @return 获取会话id的结果
         */
        public String getSessionId() {
            return sessionId;
        }

        /**
         * 获取tenantid
         *
         * @return 获取tenantid的结果
         */
        public String getTenantId() {
            return tenantId;
        }

        /**
         * 获取Topic
         *
         * @return 获取topic的结果
         */
        public String getTopic() {
            return topic;
        }

        /**
         * 获取消息id
         *
         * @return 获取消息id的结果
         */
        public String getMessageId() {
            return messageId;
        }

        /**
         * 获取sentat
         *
         * @return 获取sentat的结果
         */
        public long getSentAt() {
            return sentAt;
        }

        /**
         * 获取ReasonML
         *
         * @return 获取ReasonML的结果
         */
        public String getReason() {
            return reason;
        }
    }

    public static final class AckRecord {
        /** 会话标识 */
        private final String sessionId;
        /** tenantid */
        private final String tenantId;
        /** Topic */
        private final String topic;
        /** 来源消息标识 */
        private final String sourceMessageId;
        /** 成功 */
        private final boolean success;
        /** 状态 */
        private final String status;
        /** 消息 */
        private final String message;
        /** ACKAT */
        private final long ackAt;

        /**
         * 创建 ACKrecord 实例
         * @param sessionId 会话id
         * @param tenantId tenantid
         * @param topic topic
         * @param sourceMessageId 源消息id
         * @param success 成功
         * @param status 状态
         * @param message 消息
         * @param ackAt ACKat
         * @return ACKrecord的结果
         */
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

        /**
         * 获取会话id
         *
         * @return 获取会话id的结果
         */
        public String getSessionId() {
            return sessionId;
        }

        /**
         * 获取tenantid
         *
         * @return 获取tenantid的结果
         */
        public String getTenantId() {
            return tenantId;
        }

        /**
         * 获取Topic
         *
         * @return 获取topic的结果
         */
        public String getTopic() {
            return topic;
        }

        /**
         * 获取源消息id
         *
         * @return 获取源消息id的结果
         */
        public String getSourceMessageId() {
            return sourceMessageId;
        }

        /**
         * 是否成功
         *
         * @return 是否成功的结果
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * 获取状态
         *
         * @return 获取状态的结果
         */
        public String getStatus() {
            return status;
        }

        /**
         * 获取消息
         *
         * @return 获取消息的结果
         */
        public String getMessage() {
            return message;
        }

        /**
         * 获取ACKat
         *
         * @return 获取ACKat的结果
         */
        public long getAckAt() {
            return ackAt;
        }
    }
}
