package com.chua.starter.tenant.server.provider;

import com.chua.common.support.annotations.Spi;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.entity.SysTenantService;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import com.chua.starter.tenant.server.mapper.SysTenantServiceMapper;
import com.chua.tenant.support.sync.TenantMetadataProvider;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租户元数据提供者
 * <p>
 * 服务端实现，提供租户的管理员账号、服务列表等元数据
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Slf4j
@Component
@Spi("sysTenant")
public class SysTenantMetadataProvider implements TenantMetadataProvider {

    @Autowired(required = false)
    private SysTenantMapper sysTenantMapper;

    @Autowired(required = false)
    private SysTenantServiceMapper sysTenantServiceMapper;

    @Override
    public String getName() {
        return "sysTenant";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public Map<String, Object> getMetadata(String tenantId) {
        Map<String, Object> metadata = new HashMap<>();

        if (sysTenantMapper == null) {
            log.warn("[租户元数据提供者] SysTenantMapper 未初始化");
            return metadata;
        }

        try {
            // 查询租户信息
            SysTenant tenant = sysTenantMapper.selectOne(
                    Wrappers.<SysTenant>lambdaQuery()
                            .eq(SysTenant::getSysTenantCode, tenantId)
            );

            if (tenant == null) {
                log.warn("[租户元数据提供者] 未找到租户: {}", tenantId);
                return metadata;
            }

            // 提供管理员账号信息
            Map<String, Object> adminAccount = new HashMap<>();
            adminAccount.put("username", tenant.getSysTenantUsername());
            adminAccount.put("tenantName", tenant.getSysTenantName());
            adminAccount.put("tenantCode", tenant.getSysTenantCode());
            adminAccount.put("email", tenant.getSysTenantEmail());
            adminAccount.put("phone", tenant.getSysTenantPhone());
            adminAccount.put("status", tenant.getSysTenantStatus());
            metadata.put("adminAccount", adminAccount);

            // 提供服务列表
            if (sysTenantServiceMapper != null) {
                List<SysTenantService> services = sysTenantServiceMapper.selectList(
                        Wrappers.<SysTenantService>lambdaQuery()
                                .eq(SysTenantService::getSysTenantId, tenant.getSysTenantId())
                );

                List<Map<String, Object>> serviceList = services.stream()
                        .map(service -> {
                            Map<String, Object> serviceMap = new HashMap<>();
                            serviceMap.put("serviceId", service.getSysServiceId());
                            serviceMap.put("validTime", service.getSysTenantServiceValidTime());
                            return serviceMap;
                        })
                        .collect(Collectors.toList());
                metadata.put("services", serviceList);

                // 提供菜单ID列表
                List<Integer> menuIds = sysTenantServiceMapper.getMenuByTenantId(tenant.getSysTenantId());
                metadata.put("menuIds", menuIds);
            }

            // 提供租户配置
            Map<String, Object> config = new HashMap<>();
            config.put("tenantId", tenant.getSysTenantId());
            config.put("createTime", tenant.getCreateTime());
            metadata.put("config", config);

            log.debug("[租户元数据提供者] 租户 {} 元数据收集完成，共 {} 项", tenantId, metadata.size());

        } catch (Exception e) {
            log.error("[租户元数据提供者] 获取租户 {} 元数据失败", tenantId, e);
        }

        return metadata;
    }

    @Override
    public boolean supports(String tenantId) {
        return tenantId != null && !tenantId.isEmpty();
    }
}
