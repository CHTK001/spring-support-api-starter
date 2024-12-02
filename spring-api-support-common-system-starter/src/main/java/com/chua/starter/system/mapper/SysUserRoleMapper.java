package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysUserRole;
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
}