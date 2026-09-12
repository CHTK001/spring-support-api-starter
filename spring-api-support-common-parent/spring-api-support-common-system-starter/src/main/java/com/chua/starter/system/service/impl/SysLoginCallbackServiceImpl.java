package com.chua.starter.system.service.impl;

import com.chua.starter.entity.system.SysUser;
import com.chua.starter.service.SimpleSysUserService;
import com.chua.starter.system.service.SysLoginCallbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * @since 2024/9/12
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class SysLoginCallbackServiceImpl implements SysLoginCallbackService {

    final SimpleSysUserService sysUserService; // sys用户服务


    @Override
    /** 获取byid */
    public SysUser getById(String userId) {
        return sysUserService.getById(userId);
    }

    @Override
    /** 更新byid */
    public void updateById(SysUser sysUser) {
        sysUserService.updateById(sysUser);
    }
}
