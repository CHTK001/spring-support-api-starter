package com.chua.starter.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.starter.entity.system.SysUserRole;

/**
 * 用户 - 角色关联琛?
 * @since 2024/8/7
 * @author CH
 */
public interface SysUserRoleService extends IService<SysUserRole> {



    /**
     * 保存用户角色
     *
     * @param sysUserId 用户标识
     * @param roleCode 角色编码
     */
    void saveUserRole(Integer sysUserId, String roleCode);
}
