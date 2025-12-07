package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.system.entity.SysUserRole;

/**
 * 鐢ㄦ埛 - 瑙掕壊鍏宠仈琛?
 * @author CH
 * @since 2024/8/7
 */
public interface SysUserRoleService extends IService<SysUserRole> {



    /**
     * 淇濆瓨鐢ㄦ埛瑙掕壊
     *
     * @param sysUserId 鐢ㄦ埛ID
     * @param roleCode 瑙掕壊缂栫爜
     */
    void saveUserRole(Integer sysUserId, String roleCode);
}
