package com.chua.starter.tenant.client;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.utils.MapUtils;
import com.chua.sync.support.spi.SyncMessageHandler;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.spi.TenantDataSyncProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 租户客户端同步处理器
 * <p>
 * 实现 SyncMessageHandler 接口，监听服务端推送的租户数据并通过 SPI 分发处理
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Slf4j
@Component
@Spi("tenant")
@ConditionalOnClass(SyncMessageHandler.class)
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".client", name = "enable", havingValue = "true")
public class TenantClientSyncHandler implements SyncMessageHandler {

    /**
     * 租户同步主题
     */
    public static final String TOPIC_TENANT = "tenant";
    public static final String TOPIC_TENANT_UPDATE = "tenant/update";
    public static final String TOPIC_TENANT_DELETE = "tenant/delete";
    public static final String TOPIC_TENANT_SERVICE = "tenant/service";
    public static final String TOPIC_TENANT_MENU = "tenant/menu";

    @Autowired
    private TenantProperties tenantProperties;

    @Autowired(required = false)
    private List<TenantDataSyncProvider> syncProviders;

    @Override
    public String getName() {
        return "tenant-client";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean supports(String topic) {
        return topic != null && topic.startsWith("tenant");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object handle(String topic, String sessionId, Map<String, Object> data) {
        // 客户端模式才处理
        if (!tenantProperties.isClientMode()) {
            return null;
        }

        // 检查是否启用同步
        if (!tenantProperties.getClient().isSyncEnable()) {
            return null;
        }

        log.debug("[租户客户端] 收到消息: topic={}", topic);

        return switch (topic) {
            case TOPIC_TENANT_UPDATE -> handleTenantUpdate(data);
            case TOPIC_TENANT_DELETE -> handleTenantDelete(data);
            case TOPIC_TENANT_SERVICE -> handleServiceUpdate(data);
            case TOPIC_TENANT_MENU -> handleMenuUpdate(data);
            default -> handleCustomTopic(topic, data);
        };
    }

    /**
     * 获取优先级最高的提供者
     */
    private TenantDataSyncProvider getProvider() {
        if (syncProviders == null || syncProviders.isEmpty()) {
            log.warn("[租户客户端] 未找到 TenantDataSyncProvider 实现");
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
            SysTenant tenant = convertToTenant(data);
            if (tenant == null || tenant.getSysTenantId() == null) {
                log.warn("[租户客户端] 租户数据不完整");
                return Map.of("success", false, "message", "租户数据不完整");
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
            Integer tenantId = MapUtils.getInteger(data, "sysTenantId", 0);
            if (tenantId == 0) {
                return Map.of("success", false, "message", "租户ID为空");
            }

            SysTenant tenant = new SysTenant();
            tenant.setSysTenantId(String.valueOf(tenantId));
            provider.syncTenantUser(tenant, "delete");
            
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
            Integer tenantId = MapUtils.getInteger(data, "tenantId", 0);
            List<Integer> menuIds = (List<Integer>) data.get("menuIds");

            if (tenantId == 0) {
                return Map.of("success", false, "message", "租户ID为空");
            }

            boolean result = provider.syncTenantMenu(tenantId, menuIds);
            log.info("[租户客户端] 租户 {} 服务更新，菜单数: {}", tenantId, menuIds != null ? menuIds.size() : 0);
            return Map.of("success", result);
        } catch (Exception e) {
            log.error("[租户客户端] 处理服务更新失败", e);
            return Map.of("success", false, "message", e.getMessage());
        }
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
            Integer tenantId = MapUtils.getInteger(data, "tenantId", 0);
            String operation = MapUtils.getString(data, "operation");
            List<Map<String, Object>> menus = (List<Map<String, Object>>) data.get("menus");

            if (tenantId == 0) {
                return Map.of("success", false, "message", "租户ID为空");
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
