package com.chua.starter.tenant.sync;

import com.chua.common.support.annotations.Spi;
import com.chua.tenant.support.sync.TenantMetadataConsumer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 管理员账号元数据消费者
 * <p>
 * 接收服务端推送的租户管理员账号信息并更新本地数据
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/03
 */
@Slf4j
@Spi("admin-account")
public class AdminAccountMetadataConsumer implements TenantMetadataConsumer {

    @Override
    public String getName() {
        return "admin-account";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void consumeMetadata(String tenantId, Map<String, Object> metadata) {
        log.info("[租户同步] 开始处理租户 {} 的元数据", tenantId);
        
        // 处理管理员账号信息
        Object adminAccount = metadata.get("adminAccount");
        if (adminAccount != null) {
            try {
                Map<String, Object> accountData = (Map<String, Object>) adminAccount;
                updateAdminAccount(tenantId, accountData);
                log.info("[租户同步] 租户 {} 管理员账号更新成功", tenantId);
            } catch (Exception e) {
                log.error("[租户同步] 租户 {} 管理员账号更新失败", tenantId, e);
            }
        }
        
        // 处理启用的服务列表
        Object services = metadata.get("services");
        if (services != null) {
            try {
                List<String> serviceList = (List<String>) services;
                updateEnabledServices(tenantId, serviceList);
                log.info("[租户同步] 租户 {} 服务列表更新成功，共 {} 个服务", tenantId, serviceList.size());
            } catch (Exception e) {
                log.error("[租户同步] 租户 {} 服务列表更新失败", tenantId, e);
            }
        }
        
        // 处理租户配置
        Object config = metadata.get("config");
        if (config != null) {
            try {
                Map<String, Object> configData = (Map<String, Object>) config;
                updateTenantConfig(tenantId, configData);
                log.info("[租户同步] 租户 {} 配置更新成功", tenantId);
            } catch (Exception e) {
                log.error("[租户同步] 租户 {} 配置更新失败", tenantId, e);
            }
        }
    }

    /**
     * 更新管理员账号信息
     *
     * @param tenantId    租户ID
     * @param accountData 账号数据
     */
    private void updateAdminAccount(String tenantId, Map<String, Object> accountData) {
        // TODO: 实现管理员账号更新逻辑
        // 例如：更新本地缓存、数据库等
        log.debug("[租户同步] 更新租户 {} 管理员账号: {}", tenantId, accountData);
    }

    /**
     * 更新启用的服务列表
     *
     * @param tenantId    租户ID
     * @param serviceList 服务列表
     */
    private void updateEnabledServices(String tenantId, List<String> serviceList) {
        // TODO: 实现服务列表更新逻辑
        // 例如：更新本地缓存、配置等
        log.debug("[租户同步] 更新租户 {} 服务列表: {}", tenantId, serviceList);
    }

    /**
     * 更新租户配置
     *
     * @param tenantId   租户ID
     * @param configData 配置数据
     */
    private void updateTenantConfig(String tenantId, Map<String, Object> configData) {
        // TODO: 实现租户配置更新逻辑
        // 例如：更新本地缓存、配置等
        log.debug("[租户同步] 更新租户 {} 配置: {}", tenantId, configData);
    }
}
