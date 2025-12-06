package com.chua.starter.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysTenant;
import com.chua.starter.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户租户 Mapper 接口
 * <p>
 * 用于处理租户和用户之间的关联操作
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Mapper
public interface SysUserTenantMapper extends BaseMapper<SysUser> {

    /**
     * 根据租户查询用户
     *
     * @param tenant 租户信息
     * @return 用户信息
     */
    SysUser selectOneByTenant(@Param("tenant") SysTenant tenant);

    /**
     * 插入用户并关联租户
     *
     * @param sysUser 用户信息
     * @param tenant  租户信息
     * @return 影响行数
     */
    int insertTenant(@Param("sysUser") SysUser sysUser, @Param("tenant") SysTenant tenant);
}
