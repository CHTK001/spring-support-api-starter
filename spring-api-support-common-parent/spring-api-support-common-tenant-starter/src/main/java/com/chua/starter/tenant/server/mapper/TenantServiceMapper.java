package com.chua.starter.tenant.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 租户服务删除 映射器 接口
 *
 * @since 2025/1/1
 * @author CH
 */
@Mapper
public interface TenantServiceMapper {

    /**
     * 按租户标识删除服务记录
     *
     * @param tenantId 租户标识
     */
    void deleteByTenantId(@Param("tenantId") String tenantId);

    /**
     * 按租户标识删除服务模块记录
     *
     * @param tenantId 租户标识
     */
    void deleteModuleByTenantId(@Param("tenantId") String tenantId);

    /**
     * 按租户标识删除服务模块项记录
     *
     * @param tenantId 租户标识
     */
    void deleteModuleItemByTenantId(@Param("tenantId") String tenantId);
}
