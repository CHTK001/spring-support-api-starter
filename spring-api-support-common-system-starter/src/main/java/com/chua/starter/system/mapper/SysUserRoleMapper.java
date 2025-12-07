package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    /**
     * 保存用户角色
     * @param sysUserId 用户ID
     * @param roleCode 角色编码
     */
    void saveUserRole(@Param("sysUserId") Integer sysUserId, @Param("roleCode") String roleCode);

    /**
     * 查询用户角色
     *
     * @param sysTenantId 绉熸埛ID
     * @param sysUserId   用户ID
     * @param sysRoleId   角色ID
     * @return SysUserRole
     */
    SysUserRole selectOneByTenant(@Param("sysTenantId") Integer sysTenantId,
                                  @Param("sysUserId") Integer sysUserId,
                                  @Param("sysRoleId") Integer sysRoleId);

    /**
     * 保存用户角色
     *
     * @param sysUserRole 用户角色
     * @param sysTenantId 绉熸埛ID
     */
    void insertWithTenant(@Param("sysUserRole") SysUserRole sysUserRole, @Param("sysTenantId") Integer sysTenantId);
}