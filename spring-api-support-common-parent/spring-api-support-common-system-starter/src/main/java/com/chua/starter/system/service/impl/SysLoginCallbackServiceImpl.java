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

    final SimpleSysUserService sysUserService;


    @Override
    /** 获取ById */
    public SysUser getById(String userId) {
        return sysUserService.getById(userId);
    }

    @Override
    /** 更新ById */
    public void updateById(SysUser sysUser) {
        sysUserService.updateById(sysUser);
    }
}
