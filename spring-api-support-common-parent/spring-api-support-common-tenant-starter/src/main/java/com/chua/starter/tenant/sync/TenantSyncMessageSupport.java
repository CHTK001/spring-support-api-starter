package com.chua.starter.tenant.sync;

import com.chua.common.support.lang.algorithm.hmac.HMacUtils;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.lang.json.Json;
import com.chua.starter.sync.support.pojo.ClientInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 租户同步消息辅助类。
 *
 * @author CH
 * @since 2026/03/24
 */
public final class TenantSyncMessageSupport {

    public static final String METADATA_TENANT_ID = "tenantId";
    public static final String METADATA_SYNC_ROLE = "syncRole";
    public static final String ROLE_TENANT_CLIENT = "tenant-client";
    public static final String MESSAGE_ID = "messageId";
    public static final String TARGET_TENANT_ID = "targetTenantId";
    public static final String SYNC_MODE = "syncMode";
    public static final String SYNC_REASON = "syncReason";
    public static final String TIMESTAMP = "timestamp";
    public static final String SIGN = "sign";
    public static final String SIGN_ALGORITHM = "signAlgorithm";
    public static final String SIGN_VERSION = "signVersion";
    public static final String SOURCE_MESSAGE_ID = "sourceMessageId";
    public static final String SOURCE_TOPIC = "sourceTopic";
    public static final String ACK_SUCCESS = "ackSuccess";
    public static final String ACK_STATUS = "ackStatus";
    public static final String ACK_ERROR_MESSAGE = "ackErrorMessage";
    public static final String TENANT = "tenant";
    public static final String MENUS = "menus";
    public static final String MENU_IDS = "menuIds";
    public static final String OPERATION = "operation";
    public static final String TOPIC_TENANT_UPDATE = "tenant/update";
    public static final String TOPIC_TENANT_DELETE = "tenant/delete";
    public static final String TOPIC_TENANT_SERVICE = "tenant/service";
    public static final String TOPIC_TENANT_MENU = "tenant/menu";
    public static final String TOPIC_TENANT_SYNC_REQUEST = "tenant/sync/request";
    public static final String TOPIC_TENANT_SYNC_ACK = "tenant/sync/ack";
    public static final String MODE_FULL = "full";
    public static final String MODE_INCREMENTAL = "incremental";
    public static final String OP_REPLACE = "replace";
    public static final String OP_CREATE = "create";
    public static final String OP_UPDATE = "update";
    public static final String OP_DELETE = "delete";
    public static final String HMAC_SHA256 = "HmacSHA256";
    public static final String SIGN_VERSION_V1 = "v1";
    public static final String ACK_STATUS_APPLIED = "applied";
    public static final String ACK_STATUS_DUPLICATE = "duplicate";
    public static final String ACK_STATUS_OUT_OF_ORDER = "out-of-order";
    public static final String ACK_STATUS_FAILED = "failed";

    private TenantSyncMessageSupport() {
    }

    public static Map<String, Object> createEnvelope(String tenantId, String syncMode, String reason) {
        Map<String, Object> message = new HashMap<>();
        message.put(MESSAGE_ID, UUID.randomUUID().toString());
        message.put(TARGET_TENANT_ID, tenantId);
        message.put(SYNC_MODE, syncMode);
        message.put(SYNC_REASON, reason);
        message.put(TIMESTAMP, System.currentTimeMillis());
        return message;
    }

    public static Map<String, Object> createAck(String tenantId,
                                                String sourceTopic,
                                                Map<String, Object> sourceData,
                                                boolean success,
                                                String status,
                                                String errorMessage) {
        Map<String, Object> ack = createEnvelope(tenantId, MODE_INCREMENTAL, "client-ack");
        ack.put(SOURCE_MESSAGE_ID, resolveMessageId(sourceData));
        ack.put(SOURCE_TOPIC, sourceTopic);
        ack.put(ACK_SUCCESS, success);
        ack.put(ACK_STATUS, hasText(status) ? status : (success ? ACK_STATUS_APPLIED : ACK_STATUS_FAILED));
        long sourceTimestamp = resolveTimestamp(sourceData);
        if (sourceTimestamp > 0) {
            ack.put("sourceTimestamp", sourceTimestamp);
        }
        if (hasText(errorMessage)) {
            ack.put(ACK_ERROR_MESSAGE, errorMessage);
        }
        return ack;
    }

    /**
     * 为租户同步消息添加签名。
     *
     * @param data   消息体
     * @param secret 共享密钥
     */
    public static void sign(Map<String, Object> data, String secret) {
        if (data == null || data.isEmpty() || !hasText(secret)) {
            return;
        }

        if (!hasText(resolveMessageId(data))) {
            data.put(MESSAGE_ID, UUID.randomUUID().toString());
        }

        if (resolveTimestamp(data) <= 0) {
            data.put(TIMESTAMP, System.currentTimeMillis());
        }

        data.put(SIGN_ALGORITHM, HMAC_SHA256);
        data.put(SIGN_VERSION, SIGN_VERSION_V1);
        data.put(SIGN, calculateSignature(data, secret));
    }

    /**
     * 校验租户同步消息签名和时间窗。
     *
     * @param data                   消息体
     * @param secret                 共享密钥
     * @param allowedClockSkewMillis 允许时钟偏移毫秒数
     * @return 校验结果
     */
    public static VerificationResult verifySignature(Map<String, Object> data, String secret, long allowedClockSkewMillis) {
        if (!hasText(secret)) {
            return VerificationResult.skip("sign 未启用");
        }

        if (data == null || data.isEmpty()) {
            return VerificationResult.invalid("消息体为空");
        }

        String signature = MapUtils.getString(data, SIGN);
        if (!hasText(signature)) {
            return VerificationResult.invalid("缺少 sign");
        }

        String algorithm = MapUtils.getString(data, SIGN_ALGORITHM, HMAC_SHA256);
        if (!Objects.equals(HMAC_SHA256, algorithm)) {
            return VerificationResult.invalid("不支持的签名算法: " + algorithm);
        }

        long timestamp = resolveTimestamp(data);
        if (timestamp <= 0) {
            return VerificationResult.invalid("缺少有效 timestamp");
        }

        long skewMillis = Math.max(0L, allowedClockSkewMillis);
        if (skewMillis > 0 && Math.abs(System.currentTimeMillis() - timestamp) > skewMillis) {
            return VerificationResult.invalid("消息已过期或时间偏移过大");
        }

        String expected = calculateSignature(data, secret);
        if (!Objects.equals(expected, signature)) {
            return VerificationResult.invalid("sign 校验失败");
        }

        return VerificationResult.valid();
    }

    /**
     * 计算消息签名。
     *
     * @param data   消息体
     * @param secret 共享密钥
     * @return HMAC-SHA256 十六进制摘要
     */
    public static String calculateSignature(Map<String, Object> data, String secret) {
        if (data == null || data.isEmpty() || !hasText(secret)) {
            return null;
        }
        return HMacUtils.hmacSha256Hex(secret, canonicalPayload(data));
    }

    /**
     * 生成稳定序列化后的 payload，用于签名和指纹计算。
     *
     * @param data 消息体
     * @return 稳定 JSON 字符串
     */
    public static String canonicalPayload(Map<String, Object> data) {
        return Json.toJson(normalizeForSign(data));
    }

    /**
     * 计算消息内容指纹。
     *
     * @param data 消息体
     * @return 指纹
     */
    public static String payloadFingerprint(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        return com.chua.common.support.utils.DigestUtils.sha256(canonicalPayload(data));
    }

    @SuppressWarnings("unchecked")
    public static String resolveTargetTenantId(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        String tenantId = MapUtils.getString(data, TARGET_TENANT_ID);
        if (hasText(tenantId)) {
            return tenantId;
        }

        tenantId = MapUtils.getString(data, "tenantId");
        if (hasText(tenantId)) {
            return tenantId;
        }

        tenantId = MapUtils.getString(data, "sysTenantId");
        if (hasText(tenantId)) {
            return tenantId;
        }

        Object tenant = data.get(TENANT);
        if (tenant instanceof Map<?, ?> tenantMap) {
            return MapUtils.getString((Map<String, Object>) tenantMap, "sysTenantId");
        }
        return null;
    }

    public static String resolveMessageId(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        String messageId = MapUtils.getString(data, MESSAGE_ID);
        if (hasText(messageId)) {
            return messageId;
        }
        return MapUtils.getString(data, "requestId");
    }

    public static long resolveTimestamp(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return 0L;
        }
        return MapUtils.getLong(data, TIMESTAMP, 0L);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> resolveTenantPayload(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        Object tenant = data.get(TENANT);
        if (tenant instanceof Map<?, ?> tenantMap) {
            return (Map<String, Object>) tenantMap;
        }
        return data;
    }

    public static String resolveTenantId(ClientInfo clientInfo) {
        if (clientInfo == null || clientInfo.getClientMetadata() == null) {
            return null;
        }
        Object tenantId = clientInfo.getClientMetadata().get(METADATA_TENANT_ID);
        return tenantId == null ? null : String.valueOf(tenantId);
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static Object normalizeForSign(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map<?, ?> map) {
            TreeMap<String, Object> sorted = new TreeMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (SIGN.equals(key)) {
                    continue;
                }
                sorted.put(key, normalizeForSign(entry.getValue()));
            }
            return new LinkedHashMap<>(sorted);
        }

        if (value instanceof List<?> list) {
            return list.stream().map(TenantSyncMessageSupport::normalizeForSign).toList();
        }

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                values.add(normalizeForSign(Array.get(value, i)));
            }
            return values;
        }

        return value;
    }

    public static final class VerificationResult {

        private static final VerificationResult VALID = new VerificationResult(true, false, null);

        private final boolean valid;
        private final boolean skipped;
        private final String message;

        private VerificationResult(boolean valid, boolean skipped, String message) {
            this.valid = valid;
            this.skipped = skipped;
            this.message = message;
        }

        public static VerificationResult valid() {
            return VALID;
        }

        public static VerificationResult skip(String message) {
            return new VerificationResult(true, true, message);
        }

        public static VerificationResult invalid(String message) {
            return new VerificationResult(false, false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isSkipped() {
            return skipped;
        }

        public String getMessage() {
            return message;
        }
    }
}
