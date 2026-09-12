package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @since 2024/8/7
* @author CH
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    /**
    * 保存用户角色
    * @param sysUserId 用户标识
    * @param roleCode 角色编码
     */
    void saveUserRole(@Param("sysUserId") Integer sysUserId, @Param("roleCode") String roleCode);

    /**
    * 一次 连接 查询用户全部角色编码，替代多次 选择列表
    *
    * @param sysUserId 用户标识
    * @return 角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("sysUserId") Integer sysUserId);

    /**
    * 查询用户角色
    *
    * @param sysTenantId 绉熸埛标识
    * @param sysUserId   用户标识
    * @param sysRoleId   角色标识
    * @return SysUserRole
     */
    SysUserRole selectOneByTenant(@Param("sysTenantId") Integer sysTenantId,
                                  @Param("sysUserId") Integer sysUserId,
                                  @Param("sysRoleId") Integer sysRoleId);

    /**
    * 保存用户角色
    *
    * @param sysUserRole 用户角色
    * @param sysTenantId 绉熸埛标识
     */
    void insertWithTenant(@Param("sysUserRole") SysUserRole sysUserRole, @Param("sysTenantId") Integer sysTenantId);

    /**
    * 根据角色编码列表查询角色标识
    *
    * @param roleCodes 角色编码列表
    * @return 角色ID列表
     */
    List<Integer> selectRoleIdsByCodes(@Param("roleCodes") List<String> roleCodes);

    /**
    * 查询所有管理员角色标识（含超级管理员）。
    *
    * @return 管理员角色ID列表
     */
    List<Integer> selectAdminRoleIds();
}