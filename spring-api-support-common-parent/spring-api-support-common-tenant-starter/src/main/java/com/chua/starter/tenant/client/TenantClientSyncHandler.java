package com.chua.starter.tenant.client;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.sync.support.client.SyncClient;
import com.chua.starter.sync.support.spi.SyncMessageHandler;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.spi.TenantDataSyncProvider;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 租户客户端同步处理器
 * <p>
 * 实现 SyncMessageHandler 接口，监听服务端推送的租户数据并通过 SPI 分发处理
 * </p>
 *
 * @version 1.0.0
 * @since 2024/12/06
 * @author CH
 */
@Component
@Spi("tenant")
@ConditionalOnClass(SyncMessageHandler.class)
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".client", name = "enable", havingValue = "true")
public class TenantClientSyncHandler implements SyncMessageHandler {

    /** 日志 */
    private static final Logger log = LoggerFactory.getLogger(TenantClientSyncHandler.class);

    /**
     * 租户同步主题
     */
    public static final String TOPIC_TENANT = "tenant";
    /** Topic_tenant_update */
    public static final String TOPIC_TENANT_UPDATE = TenantSyncMessageSupport.TOPIC_TENANT_UPDATE;
    /** Topic_tenant_delete */
    public static final String TOPIC_TENANT_DELETE = TenantSyncMessageSupport.TOPIC_TENANT_DELETE;
    /** Topic_tenant_service */
    public static final String TOPIC_TENANT_SERVICE = TenantSyncMessageSupport.TOPIC_TENANT_SERVICE;
    /** Topic_tenant_menu */
    public static final String TOPIC_TENANT_MENU = TenantSyncMessageSupport.TOPIC_TENANT_MENU;

    @Autowired
    /** Tenant属性 */
    private TenantProperties tenantProperties;

    @Autowired(required = false)
    /** 同步providers */
    private List<TenantDataSyncProvider> syncProviders;

    @Autowired
    /** 同步客户端提供者 */
    private ObjectProvider<SyncClient> syncClientProvider;

    /**
     * 已处理消息缓存
     */
    private final ConcurrentMap<String, Long> processedMessages = new ConcurrentHashMap<>();

    /**
     * 每个租户主题最近一次成功应用的消息时间戳。
     */
    private final ConcurrentMap<String, Long> latestAppliedTimestamps = new ConcurrentHashMap<>();

    /**
     * 最近一次收到本租户同步消息的时间戳。
     */
    private final AtomicLong lastReceivedSyncMessageTime = new AtomicLong(0L);

    @Override
    /** 获取Name */
    public String getName() {
        return "tenant-client";
    }

    @Override
    @SuppressWarnings("unchecked")
    /** 处理 */
    public Object handle(String topic, String sourceClientId, Map<String, Object> data) {
        if (data == null) {
            data = new HashMap<>();
        }

        // 客户端模式才处理
        if (!tenantProperties.isClientMode()) {
            return null;
        }

        // 检查是否启用同步
        if (!tenantProperties.getClient().isSyncEnable()) {
            return null;
        }

        log.debug("[租户客户端] 收到消息: topic={}", topic);

        if (shouldIgnoreMessage(data)) {
            return null;
        }

        TenantSyncMessageSupport.VerificationResult verificationResult = verifySignature(data);
        if (!verificationResult.isValid()) {
            log.warn("[租户客户端] 忽略签名校验失败的同步消息: topic={}, reason={}",
                    topic, verificationResult.getMessage());
            return null;
        }

        markSyncMessageReceived();

        Object result;
        if (isDuplicateMessage(data)) {
            result = Map.of("success", true, "duplicate", true);
        } else if (isOutOfOrderMessage(topic, data)) {
            result = Map.of("success", true, "outOfOrder", true);
        } else {
            result = switch (topic) {
                case TOPIC_TENANT_UPDATE -> handleTenantUpdate(data);
                case TOPIC_TENANT_DELETE -> handleTenantDelete(data);
                case TOPIC_TENANT_SERVICE -> handleServiceUpdate(data);
                case TOPIC_TENANT_MENU -> handleMenuUpdate(data);
                default -> handleCustomTopic(topic, data);
            };
        }
        markAppliedMessage(topic, data, result);
        sendAck(topic, data, result);
        return result;
    }

    /**
     * 获取优先级最高的提供者
     */
    /**
     * 校验同步消息目标租户是否为当前租户实例自身
     * <p>防止平台广播兜底或消息错投时，将其他租户的数据写入本租户库</p>
     *
     * @param tenantId 消息目标租户ID
     * @return true=匹配或未配置自身租户ID(向后兼容)；false=不匹配
     */
    private boolean isSelfTenant(String tenantId) {
        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            return false;
        }
        String selfTenantId = tenantProperties.getClient().getTenantId();
        return !TenantSyncMessageSupport.hasText(selfTenantId) || selfTenantId.equals(tenantId);
    }

    /** 获取Provider */
    private TenantDataSyncProvider getProvider() {
        if (syncProviders == null || syncProviders.isEmpty()) {
            log.warn("[�⻧�ͻ���] δ�ҵ� TenantDataSyncProvider ʵ��");
            return null;
        }
        return syncProviders.stream()
                .min(Comparator.comparingInt(TenantDataSyncProvider::getOrder))
                .orElse(null);
    }

    /**
     * 处理租户更新
     */
    private Object handleTenantUpdate(Map<String, Object> data) {
        TenantDataSyncProvider provider = getProvider();
        if (provider == null) {
            return Map.of("success", false, "message", "未找到同步提供者");
        }

        try {
            SysTenant tenant = convertToTenant(TenantSyncMessageSupport.resolveTenantPayload(data));
            if (tenant == null || tenant.getSysTenantId() == null) {
                log.warn("[�⻧�ͻ���] �⻧���ݲ�����");
                return Map.of("success", false, "message", "�⻧���ݲ�����");
            }
            if (!isSelfTenant(tenant.getSysTenantId())) {
                log.warn("[�⻧�ͻ���] �⻧ID��ƥ�䣬������: tenantId={}", tenant.getSysTenantId());
                return Map.of("success", false, "message", "�⻧ID��ƥ�䣬������");
            }

            provider.syncTenantUser(tenant, "update");
            log.info("[租户客户端] 租户 {} 更新成功", tenant.getSysTenantCode());
            return Map.of("success", true);
        } catch (Exception e) {
            log.error("[租户客户端] 处理租户更新失败", e);
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 处理租户删除
     */
    private Object handleTenantDelete(Map<String, Object> data) {
        TenantDataSyncProvider provider = getProvider();
        if (provider == null) {
            return Map.of("success", false, "message", "未找到同步提供者");
        }

        try {
            String tenantId = TenantSyncMessageSupport.resolveTargetTenantId(data);
            if (!TenantSyncMessageSupport.hasText(tenantId)) {
                return Map.of("success", false, "message", "�⻧IDΪ��");
            }
            if (!isSelfTenant(tenantId)) {
                log.warn("[�⻧�ͻ���] �⻧ID��ƥ�䣬������: tenantId={}", tenantId);
                return Map.of("success", false, "message", "�⻧ID��ƥ�䣬������");
            }

            SysTenant tenant = new SysTenant();
            tenant.setSysTenantId(tenantId);
            provider.syncTenantUser(tenant, "delete");
            provider.syncMenuData(tenantId, List.of(), TenantSyncMessageSupport.OP_REPLACE);
            
            log.info("[租户客户端] 租户 {} 删除成功", tenantId);
            return Map.of("success", true);
        } catch (Exception e) {
            log.error("[租户客户端] 处理租户删除失败", e);
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 处理服务/菜单更新
     */
    @SuppressWarnings("unchecked")
    private Object handleServiceUpdate(Map<String, Object> data) {
        TenantDataSyncProvider provider = getProvider();
        if (provider == null) {
            return Map.of("success", false, "message", "未找到同步提供者");
        }

        try {
            String tenantId = TenantSyncMessageSupport.resolveTargetTenantId(data);
            List<Integer> menuIds = toIntegerList(data.get(TenantSyncMessageSupport.MENU_IDS));
            List<Map<String, Object>> menus = (List<Map<String, Object>>) data.get(TenantSyncMessageSupport.MENUS);
            List<Map<String, Object>> services = (List<Map<String, Object>>) data.get(TenantSyncMessageSupport.SERVICES);
            List<Map<String, Object>> modules = (List<Map<String, Object>>) data.get(TenantSyncMessageSupport.MODULES);
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get(TenantSyncMessageSupport.ITEMS);

            if (!TenantSyncMessageSupport.hasText(tenantId)) {
                return Map.of("success", false, "message", "�⻧IDΪ��");
            }
            if (!isSelfTenant(tenantId)) {
                log.warn("[�⻧�ͻ���] �⻧ID��ƥ�䣬������: tenantId={}", tenantId);
                return Map.of("success", false, "message", "�⻧ID��ƥ�䣬������");
            }

            boolean result = true;
            if (services != null || modules != null || items != null) {
                result = provider.syncServiceData(buildTenant(tenantId), services, modules, items,
                        TenantSyncMessageSupport.OP_REPLACE) && result;
            }
            if (menus != null && !menus.isEmpty()) {
                result = provider.syncMenuData(tenantId, menus, TenantSyncMessageSupport.OP_REPLACE) && result;
            } else {
                result = provider.syncTenantMenu(tenantId, menuIds);
            }

            log.info("[租户客户端] 租户 {} 服务更新，菜单数: {}", tenantId, menuIds != null ? menuIds.size() : 0);
            return Map.of("success", result);
        } catch (Exception e) {
            log.error("[租户客户端] 处理服务更新失败", e);
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 构建租户对象(仅租户ID)
     */
    private SysTenant buildTenant(String tenantId) {
        SysTenant tenant = new SysTenant();
        tenant.setSysTenantId(tenantId);
        return tenant;
    }

    /**
     * 处理菜单数据更新
     */
    @SuppressWarnings("unchecked")
    private Object handleMenuUpdate(Map<String, Object> data) {
        TenantDataSyncProvider provider = getProvider();
        if (provider == null) {
            return Map.of("success", false, "message", "未找到同步提供者");
        }

        try {
            String tenantId = TenantSyncMessageSupport.resolveTargetTenantId(data);
            String operation = MapUtils.getString(data, TenantSyncMessageSupport.OPERATION);
            List<Map<String, Object>> menus = (List<Map<String, Object>>) data.get("menus");

            if (!TenantSyncMessageSupport.hasText(tenantId)) {
                return Map.of("success", false, "message", "�⻧IDΪ��");
            }
            if (!isSelfTenant(tenantId)) {
                log.warn("[�⻧�ͻ���] �⻧ID��ƥ�䣬������: tenantId={}", tenantId);
                return Map.of("success", false, "message", "�⻧ID��ƥ�䣬������");
            }

            boolean result = provider.syncMenuData(tenantId, menus, operation);
            log.info("[租户客户端] 租户 {} 菜单更新，操作: {}, 数量: {}", 
                    tenantId, operation, menus != null ? menus.size() : 0);
            return Map.of("success", result);
        } catch (Exception e) {
            log.error("[租户客户端] 处理菜单更新失败", e);
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 处理自定义主题
     */
    private Object handleCustomTopic(String topic, Map<String, Object> data) {
        TenantDataSyncProvider provider = getProvider();
        if (provider == null) {
            return null;
        }
        return provider.handleRawData(topic, data);
    }

    /**
     * 是否忽略该消息
     */
    private boolean shouldIgnoreMessage(Map<String, Object> data) {
        String localTenantId = tenantProperties.getClient().getTenantId();
        String targetTenantId = TenantSyncMessageSupport.resolveTargetTenantId(data);
        if (!TenantSyncMessageSupport.hasText(localTenantId)) {
            if (TenantSyncMessageSupport.hasText(targetTenantId)) {
                log.warn("[租户客户端] 未配置 tenantId，忽略定向租户消息: targetTenantId={}", targetTenantId);
                return true;
            }
            return false;
        }

        return TenantSyncMessageSupport.hasText(targetTenantId) && !localTenantId.equals(targetTenantId);
    }

    /** 验证Signature */
    private TenantSyncMessageSupport.VerificationResult verifySignature(Map<String, Object> data) {
        if (!tenantProperties.isClientSyncSignEnabled()) {
            return TenantSyncMessageSupport.VerificationResult.skip("client sign 未启用");
        }

        long allowedSkewMillis = Math.max(0L, tenantProperties.getClient().getSyncSignAllowedClockSkewSeconds()) * 1000L;
        return TenantSyncMessageSupport.verifySignature(
                data,
                tenantProperties.getClient().getSyncSignSecret(),
                allowedSkewMillis
        );
    }

    /**
     * 是否重复消息
     */
    private boolean isDuplicateMessage(Map<String, Object> data) {
        if (!tenantProperties.getClient().isSyncDeduplicateMessages()) {
            return false;
        }

        cleanupProcessedMessages();
        cleanupAppliedTimestamps();

        String messageId = TenantSyncMessageSupport.resolveMessageId(data);
        if (!TenantSyncMessageSupport.hasText(messageId)) {
            return false;
        }

        long now = System.currentTimeMillis();
        Long previous = processedMessages.putIfAbsent(messageId, now);
        return previous != null;
    }

    /**
     * 是否为乱序旧消息。
     */
    private boolean isOutOfOrderMessage(String topic, Map<String, Object> data) {
        if (!tenantProperties.getClient().isSyncIgnoreOutOfOrderMessages() || !shouldTrackOrdering(topic)) {
            return false;
        }

        long timestamp = TenantSyncMessageSupport.resolveTimestamp(data);
        if (timestamp <= 0) {
            return false;
        }

        String orderingKey = buildOrderingKey(topic, data);
        Long latestTimestamp = latestAppliedTimestamps.get(orderingKey);
        if (latestTimestamp == null || timestamp >= latestTimestamp) {
            return false;
        }

        log.warn("[租户客户端] 忽略乱序旧消息: topic={}, tenantId={}, timestamp={}, latestTimestamp={}",
                topic, TenantSyncMessageSupport.resolveTargetTenantId(data), timestamp, latestTimestamp);
        return true;
    }

    /** 标记AppliedMessage */
    private void markAppliedMessage(String topic, Map<String, Object> data, Object result) {
        if (!tenantProperties.getClient().isSyncIgnoreOutOfOrderMessages()
                || !shouldTrackOrdering(topic)
                || !isSuccessfulResult(result)) {
            return;
        }

        cleanupAppliedTimestamps();

        long timestamp = TenantSyncMessageSupport.resolveTimestamp(data);
        if (timestamp <= 0) {
            return;
        }

        latestAppliedTimestamps.merge(buildOrderingKey(topic, data), timestamp, Math::max);
    }

    /** CleanupAppliedTimestamps */
    private void cleanupAppliedTimestamps() {
        if (latestAppliedTimestamps.isEmpty()) {
            return;
        }

        long expireBefore = System.currentTimeMillis()
                - tenantProperties.getClient().getSyncDeduplicateWindowSeconds() * 1000L;
        latestAppliedTimestamps.entrySet().removeIf(entry -> entry.getValue() < expireBefore);

        int maxSize = tenantProperties.getClient().getSyncDeduplicateMaxSize();
        if (maxSize > 0 && latestAppliedTimestamps.size() > maxSize) {
            latestAppliedTimestamps.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .limit(latestAppliedTimestamps.size() - maxSize)
                    .map(Map.Entry::getKey)
                    .toList()
                    .forEach(latestAppliedTimestamps::remove);
        }
    }

    /** 是否应该TrackOrdering */
    private boolean shouldTrackOrdering(String topic) {
        return TOPIC_TENANT_UPDATE.equals(topic)
                || TOPIC_TENANT_DELETE.equals(topic)
                || TOPIC_TENANT_SERVICE.equals(topic)
                || TOPIC_TENANT_MENU.equals(topic);
    }

    /** 构建OrderingKey */
    private String buildOrderingKey(String topic, Map<String, Object> data) {
        String tenantId = TenantSyncMessageSupport.resolveTargetTenantId(data);
        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            tenantId = tenantProperties.getClient().getTenantId();
        }
        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            tenantId = "*";
        }
        return topic + ":" + tenantId;
    }

    /** 是否SuccessfulResult */
    private boolean isSuccessfulResult(Object result) {
        if (!(result instanceof Map<?, ?> resultMap)) {
            return true;
        }

        Object success = resultMap.get("success");
        return !Boolean.FALSE.equals(success);
    }

    /** 发送Ack */
    private void sendAck(String topic, Map<String, Object> data, Object result) {
        if (!tenantProperties.isClientSyncAckEnabled() || !shouldTrackOrdering(topic) || data == null || data.isEmpty()) {
            return;
        }

        String sourceMessageId = TenantSyncMessageSupport.resolveMessageId(data);
        if (!TenantSyncMessageSupport.hasText(sourceMessageId)) {
            return;
        }

        SyncClient syncClient = syncClientProvider.getIfAvailable();
        if (syncClient == null || !syncClient.isConnected()) {
            log.debug("[租户客户端] SyncClient 不可用，跳过 ACK: topic={}, messageId={}", topic, sourceMessageId);
            return;
        }

        String tenantId = TenantSyncMessageSupport.resolveTargetTenantId(data);
        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            tenantId = tenantProperties.getClient().getTenantId();
        }

        boolean success = isSuccessfulResult(result);
        String status = resolveAckStatus(result);
        String errorMessage = resolveAckErrorMessage(result, success);
        Map<String, Object> ack = TenantSyncMessageSupport.createAck(
                tenantId,
                topic,
                data,
                success,
                status,
                errorMessage
        );
        if (tenantProperties.isClientSyncSignEnabled()) {
            TenantSyncMessageSupport.sign(ack, tenantProperties.getClient().getSyncSignSecret());
        }

        syncClient.publish(TenantSyncMessageSupport.TOPIC_TENANT_SYNC_ACK, ack);
        log.debug("[租户客户端] 已发送同步 ACK: tenantId={}, topic={}, sourceMessageId={}, success={}, status={}",
                tenantId, topic, sourceMessageId, success, status);
    }

    /** 解析AckStatus */
    private String resolveAckStatus(Object result) {
        if (!(result instanceof Map<?, ?> resultMap)) {
            return TenantSyncMessageSupport.ACK_STATUS_APPLIED;
        }

        if (Boolean.TRUE.equals(resultMap.get("duplicate"))) {
            return TenantSyncMessageSupport.ACK_STATUS_DUPLICATE;
        }
        if (Boolean.TRUE.equals(resultMap.get("outOfOrder"))) {
            return TenantSyncMessageSupport.ACK_STATUS_OUT_OF_ORDER;
        }
        if (Boolean.FALSE.equals(resultMap.get("success"))) {
            return TenantSyncMessageSupport.ACK_STATUS_FAILED;
        }
        return TenantSyncMessageSupport.ACK_STATUS_APPLIED;
    }

    /** 解析Ack记录错误Message */
    private String resolveAckErrorMessage(Object result, boolean success) {
        if (success || !(result instanceof Map<?, ?> resultMap)) {
            return null;
        }
        Object message = resultMap.get("message");
        return message == null ? null : String.valueOf(message);
    }

    /**
     * 清理过期消息缓存
     */
    private void cleanupProcessedMessages() {
        if (processedMessages.isEmpty()) {
            return;
        }

        long expireBefore = System.currentTimeMillis()
                - tenantProperties.getClient().getSyncDeduplicateWindowSeconds() * 1000L;
        processedMessages.entrySet().removeIf(entry -> entry.getValue() < expireBefore);

        int maxSize = tenantProperties.getClient().getSyncDeduplicateMaxSize();
        if (maxSize > 0 && processedMessages.size() > maxSize) {
            processedMessages.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .limit(processedMessages.size() - maxSize)
                    .map(Map.Entry::getKey)
                    .toList()
                    .forEach(processedMessages::remove);
        }
    }

    /** 是否拥有ReceivedSyncMessageSince */
    public boolean hasReceivedSyncMessageSince(long timestamp) {
        return lastReceivedSyncMessageTime.get() >= timestamp;
    }

    /** 标记SyncMessageReceived */
    private void markSyncMessageReceived() {
        lastReceivedSyncMessageTime.set(System.currentTimeMillis());
    }

    @SuppressWarnings("unchecked")
    /** ToIntegerList */
    private List<Integer> toIntegerList(Object rawValue) {
        if (!(rawValue instanceof List<?> values) || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(value -> {
                    if (value instanceof Number number) {
                        return number.intValue();
                    }
                    if (value instanceof String stringValue && !stringValue.isBlank()) {
                        return Integer.parseInt(stringValue);
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * 转换为租户对象
     */
    private SysTenant convertToTenant(Map<String, Object> data) {
        if (data == null) {
            return null;
        }

        SysTenant tenant = new SysTenant();
        tenant.setSysTenantId(MapUtils.getString(data, "sysTenantId"));
        tenant.setSysTenantCode(MapUtils.getString(data, "sysTenantCode"));
        tenant.setSysTenantName(MapUtils.getString(data, "sysTenantName"));
        tenant.setSysTenantUsername(MapUtils.getString(data, "sysTenantUsername"));
        tenant.setSysTenantPassword(MapUtils.getString(data, "sysTenantPassword"));
        tenant.setSysTenantPhone(MapUtils.getString(data, "sysTenantPhone"));
        tenant.setSysTenantEmail(MapUtils.getString(data, "sysTenantEmail"));
        tenant.setSysTenantStatus(MapUtils.getInteger(data, "sysTenantStatus", 0));
        tenant.setSysTenantDelete(MapUtils.getInteger(data, "sysTenantDelete", 0));
        return tenant;
    }
}
