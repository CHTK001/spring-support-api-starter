package com.chua.starter.tenant.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 租户服务删除 Mapper 接口
 *
 * @version 1.0.0
 * @since 2025/1/1
 * @author CH
 */
@Mapper
public interface TenantServiceMapper {

    /**
     * 按租户ID删除服务记录
     *
     * @param tenantId 租户ID
     */
    void deleteByTenantId(@Param("tenantId") String tenantId);

    /**
     * 按租户ID删除服务模块记录
     *
     * @param tenantId 租户ID
     */
    void deleteModuleByTenantId(@Param("tenantId") String tenantId);

    /**
     * 按租户ID删除服务模块项记录
     *
     * @param tenantId 租户ID
     */
    void deleteModuleItemByTenantId(@Param("tenantId") String tenantId);
}
