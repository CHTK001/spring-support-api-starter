package com.chua.starter.system.service;

import com.chua.starter.entity.system.SysUser;

/**
 * 登录回调
 * @since 2024/9/12
 * @author CH
 */
public interface SysLoginCallbackService {
    /**
      * 鏍规嵁用户标识获取用户信息
     * @param userId 用户标识
     * @return 用户信息
     */
    SysUser getById(String userId);

    /**
     * 更新用户信息
     * @param sysUser 用户信息
     */
    void updateById(SysUser sysUser);
}
