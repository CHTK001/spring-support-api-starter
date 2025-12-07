package com.chua.starter.system.service.impl;

import com.chua.starter.service.entity.SysUser;
import com.chua.starter.system.service.SimpleSysUserService;
import com.chua.starter.system.service.SysLoginCallbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * @author CH
 * @since 2024/9/12
 */
@Service
@RequiredArgsConstructor
public class SysLoginCallbackServiceImpl implements SysLoginCallbackService {

    final SimpleSysUserService sysUserService;


    @Override
    public SysUser getById(String userId) {
        return sysUserService.getById(userId);
    }

    @Override
    public void updateById(SysUser sysUser) {
        sysUserService.updateById(sysUser);
    }
}
