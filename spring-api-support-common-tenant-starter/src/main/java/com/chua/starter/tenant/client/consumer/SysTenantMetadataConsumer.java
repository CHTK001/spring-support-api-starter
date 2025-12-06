package com.chua.starter.tenant.client.consumer;

import com.chua.common.support.annotations.Spi;
import com.chua.starter.tenant.client.handler.TenantHandler;
import com.chua.starter.tenant.client.handler.TenantServiceHandler;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.tenant.support.sync.TenantMetadataConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 租户元数据消费者
 * <p>
 * 客户端实现，消费从服务端推送的租户元数据，更新本地数据
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Slf4j
@Component
@Spi("sysTenant")
public class SysTenantMetadataConsumer implements TenantMetadataConsumer {

    @Autowired(required = false)
    private List<TenantHandler> tenantHandlers;

    @Autowired(required = false)
    private List<TenantServiceHandler> tenantServiceHandlers;

    @Override
    public String getName() {
        return "sysTenant";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void consumeMetadata(String tenantId, Map<String, Object> metadata) {
        log.info("[租户元数据消费者] 开始处理租户 {} 的元数据", tenantId);

        // 处理管理员账号信息
        Object adminAccount = metadata.get("adminAccount");
        if (adminAccount instanceof Map) {
            processAdminAccount(tenantId, (Map<String, Object>) adminAccount);
        }

        // 处理服务列表
        Object menuIds = metadata.get("menuIds");
        if (menuIds instanceof List) {
            processMenuIds(tenantId, (List<Integer>) menuIds);
        }

        // 处理租户配置
        Object config = metadata.get("config");
        if (config instanceof Map) {
            processConfig(tenantId, (Map<String, Object>) config);
        }

        log.info("[租户元数据消费者] 租户 {} 元数据处理完成", tenantId);
    }

    /**
     * 处理管理员账号信息
     *
     * @param tenantId    租户ID
     * @param accountData 账号数据
     */
    private void processAdminAccount(String tenantId, Map<String, Object> accountData) {
        if (tenantHandlers == null || tenantHandlers.isEmpty()) {
            log.debug("[租户元数据消费者] 无可用的 TenantHandler");
            return;
        }

        try {
            // 构建租户对象
            SysTenant tenant = new SysTenant();
            tenant.setSysTenantCode(tenantId);
            tenant.setSysTenantUsername((String) accountData.get("username"));
            tenant.setSysTenantName((String) accountData.get("tenantName"));
            tenant.setSysTenantEmail((String) accountData.get("email"));
            tenant.setSysTenantPhone((String) accountData.get("phone"));

            Object status = accountData.get("status");
            if (status instanceof Integer) {
                tenant.setSysTenantStatus((Integer) status);
            }

            // 调用处理器
            for (TenantHandler handler : tenantHandlers) {
                try {
                    handler.saveOrUpdate(tenant);
                    log.debug("[租户元数据消费者] TenantHandler {} 处理完成", handler.getClass().getSimpleName());
                } catch (Exception e) {
                    log.error("[租户元数据消费者] TenantHandler {} 处理失败",
                            handler.getClass().getSimpleName(), e);
                }
            }

            log.info("[租户元数据消费者] 租户 {} 管理员账号更新成功", tenantId);

        } catch (Exception e) {
            log.error("[租户元数据消费者] 租户 {} 管理员账号处理失败", tenantId, e);
        }
    }

    /**
     * 处理菜单ID列表
     *
     * @param tenantId 租户ID
     * @param menuIds  菜单ID列表
     */
    private void processMenuIds(String tenantId, List<Integer> menuIds) {
        if (tenantServiceHandlers == null || tenantServiceHandlers.isEmpty()) {
            log.debug("[租户元数据消费者] 无可用的 TenantServiceHandler");
            return;
        }

        try {
            Integer sysTenantId = parseTenantId(tenantId);
            if (sysTenantId == null) {
                log.warn("[租户元数据消费者] 无法解析租户ID: {}", tenantId);
                return;
            }

            // 调用处理器
            for (TenantServiceHandler handler : tenantServiceHandlers) {
                try {
                    handler.saveOrUpdate(sysTenantId, menuIds);
                    log.debug("[租户元数据消费者] TenantServiceHandler {} 处理完成",
                            handler.getClass().getSimpleName());
                } catch (Exception e) {
                    log.error("[租户元数据消费者] TenantServiceHandler {} 处理失败",
                            handler.getClass().getSimpleName(), e);
                }
            }

            log.info("[租户元数据消费者] 租户 {} 服务列表更新成功，共 {} 个菜单",
                    tenantId, menuIds.size());

        } catch (Exception e) {
            log.error("[租户元数据消费者] 租户 {} 服务列表处理失败", tenantId, e);
        }
    }

    /**
     * 处理租户配置
     *
     * @param tenantId   租户ID
     * @param configData 配置数据
     */
    private void processConfig(String tenantId, Map<String, Object> configData) {
        log.debug("[租户元数据消费者] 租户 {} 配置数据: {}", tenantId, configData);
        // 可扩展：根据需要处理配置信息
    }

    /**
     * 解析租户ID
     *
     * @param tenantId 租户ID字符串
     * @return 租户ID整数
     */
    private Integer parseTenantId(String tenantId) {
        try {
            return Integer.parseInt(tenantId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean supports(String metadataType) {
        return true;
    }
}
