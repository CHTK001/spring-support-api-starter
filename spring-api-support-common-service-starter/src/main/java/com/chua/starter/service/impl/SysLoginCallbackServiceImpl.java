package com.chua.starter.service.impl;

import com.chua.starter.service.SysLoginCallbackService;
import com.chua.starter.system.entity.SysUser;
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

    final SysUserService sysUserService;


    @Override
    public SysUser getById(String userId) {
        return sysUserService.getById(userId);
    }

    @Override
    public void updateById(SysUser sysUser) {
        sysUserService.updateById(sysUser);
    }
}
