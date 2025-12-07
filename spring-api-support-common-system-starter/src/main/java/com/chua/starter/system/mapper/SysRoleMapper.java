package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.service.model.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 根据用户id查询角色
     *
     * @param userIds 用户id
     * @return 角色
     */
    List<SysRole> listByUserId(@Param("userIds") Set<Integer> userIds);

    /**
     * 根据租户id查询管理员角�?
     *
     * @param sysTenantId 租户id
     * @param sysRoleCode 角色编码
     * @return 角色
     */
    SysRole selectOneByTenant(@Param("sysTenantId") Integer sysTenantId, @Param("sysRoleCode") String sysRoleCode);

    /**
     * 根据租户id查询管理员角�?
     *
     * @param sysTenantId 租户id
     * @param sysTenantId 租户ID
     */
    void insertWithTenant(@Param("sysRole") SysRole sysRole, @Param("sysTenantId") Integer sysTenantId);
}