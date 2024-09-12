package com.chua.starter.service;

import com.chua.starter.system.entity.SysUser;

/**
 * 登录回调
 * @author CH
 * @since 2024/9/12
 */
public interface SysLoginCallbackService {
    /**
     * 根据用户id获取用户信息
     * @param userId 用户id
     * @return 用户信息
     */
    SysUser getById(String userId);

    /**
     * 更新用户信息
     * @param sysUser 用户信息
     */
    void updateById(SysUser sysUser);
}
