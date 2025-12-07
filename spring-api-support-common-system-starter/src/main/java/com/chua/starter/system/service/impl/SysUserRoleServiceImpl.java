package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.service.entity.SysUserRole;
import com.chua.starter.system.mapper.SysUserRoleMapper;
import com.chua.starter.system.service.SysUserRoleService;
import org.springframework.stereotype.Service;
/**
 *
 * @since 2024/8/7
 * @author CH    
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService{
    @Override
    public void saveUserRole(Integer sysUserId, String roleCode) {
        baseMapper.saveUserRole(sysUserId, roleCode);
    }
}
