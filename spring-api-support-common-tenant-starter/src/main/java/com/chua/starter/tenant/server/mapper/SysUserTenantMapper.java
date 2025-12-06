package com.chua.starter.tenant.server.mapper;

import com.chua.starter.tenant.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户租户关联 Mapper
 * 处理用户与租户关联的数据库操作
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Mapper
public interface SysUserTenantMapper {

    /**
     * 根据租户信息查询用户
     *
     * @param tenant 租户信息
     * @return 用户信息
     */
    SysUser selectOneByTenant(@Param("tenant") SysTenant tenant);

    /**
     * 插入用户信息和租户信息
     *
     * @param sysUser 用户信息
     * @param tenant  租户信息
     */
    void insertTenant(@Param("sysUser") SysUser sysUser, @Param("tenant") SysTenant tenant);
}
