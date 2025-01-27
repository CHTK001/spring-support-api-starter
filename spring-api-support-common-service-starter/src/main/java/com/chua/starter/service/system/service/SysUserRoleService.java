package com.chua.starter.service.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.system.entity.SysUserRole;

/**
 * 用户 - 角色关联表
 * @author CH
 * @since 2024/8/7
 */
public interface SysUserRoleService extends IService<SysUserRole> {



    /**
     * 保存用户角色
     *
     * @param sysUserId 用户ID
     * @param roleCode 角色编码
     */
    void saveUserRole(Integer sysUserId, String roleCode);
}
