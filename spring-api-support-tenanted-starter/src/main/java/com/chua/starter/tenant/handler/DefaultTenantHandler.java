package com.chua.starter.tenant.handler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.RandomUtils;
import com.chua.starter.service.mapper.SysUserMapper;
import com.chua.starter.tenant.mapper.SysUserTenantMapper;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.entity.system.SysTenant;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserRole;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.mapper.SysTenantMapper;
import com.chua.starter.system.mapper.SysUserRoleMapper;
import com.chua.starter.tenant.TenantHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;

/**
 * 默认租户处理器
 *
 * @author CH
 */
@SpringBootConfiguration
@RequiredArgsConstructor
public class DefaultTenantHandler implements TenantHandler {

    final SysTenantMapper sysTenantMapper;
    final SysUserMapper sysUserMapper;
    final SysUserTenantMapper sysUserTenantMapper;
    final SysRoleMapper sysRoleMapper;
    final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public void saveOrUpdate(SysTenant tenant) {
        if (null == tenant) {
            return;
        }
        Long l = sysTenantMapper.selectCount(Wrappers.lambdaQuery());
        if (l > 0) {
            if (!sysTenantMapper.exists(Wrappers.<SysTenant>lambdaQuery().eq(SysTenant::getSysTenantId, tenant.getSysTenantId()))) {
                return;
            }
        }
        SysUser sysUser = registerUser(tenant);
        registerSysRoleAndLink(sysUser);
    }

    private void registerSysRoleAndLink(SysUser sysUser) {
        SysRole sysRole = registerSysRole(sysUser);
        SysUserRole sysUserRole = sysUserRoleMapper.selectOneByTenant(sysUser.getSysTenantId(), sysUser.getSysUserId(), sysRole.getSysRoleId());
        if (null == sysUserRole) {
            sysUserRole = new SysUserRole();
            sysUserRole.setSysUserId(sysUser.getSysUserId());
            sysUserRole.setSysRoleId(sysRole.getSysRoleId());
            sysUserRoleMapper.insertWithTenant(sysUserRole, sysUser.getSysTenantId());
        }
    }

    /**
     * 注册角色
     *
     * @param sysUser
     * @return
     */
    private SysRole registerSysRole(SysUser sysUser) {
        SysRole sysRole = sysRoleMapper.selectOneByTenant(sysUser.getSysTenantId(), "admin");
        if (null == sysRole) {
            sysRole = new SysRole();
            sysRole.setSysRoleName("admin");
            sysRole.setSysRoleCode("ADMIN");
            sysRole.setSysRoleStatus(1);
            sysRole.setSysRoleInSystem(1);
            sysRoleMapper.insertWithTenant(sysRole, sysUser.getSysTenantId());
            return sysRole;
        }
        return sysRole;
    }

    private SysUser registerUser(SysTenant tenant) {
        SysUser sysUser = sysUserTenantMapper.selectOneByTenant(tenant);
        if (null == sysUser) {
            sysUser = new SysUser();
            sysUser.setSysUserUsername(tenant.getSysTenantUsername());
            sysUser.setSysUserSalt(RandomUtils.randomString(14));
            sysUser.setSysUserPassword(DigestUtils.md5Hex(tenant.getSysTenantPassword() + sysUser.getSysUserSalt()));
            sysUser.setSysUserInSystem(1);
            sysUser.setSysUserStatus(1);
            sysUser.setSysUserDelete(0);
            sysUser.setSysUserPhone(tenant.getSysTenantPhone());
            sysUserTenantMapper.insertTenant(sysUser, tenant);
            return sysUser;
        }
        if (null != sysUser.getSysUserPassword()) {
            sysUser.setSysUserSalt(RandomUtils.randomString(14));
            sysUser.setSysUserPassword(DigestUtils.md5Hex(DigestUtils.md5Hex(tenant.getSysTenantPassword()) + sysUser.getSysUserSalt()));
        }

        sysUser.setSysUserPhone(tenant.getSysTenantPhone());
        sysUserMapper.updateById(sysUser);
        return sysUser;
    }

    @Override
    public void delete(SysTenant tenant) {
        SysUser sysUser = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getSysUserUsername, tenant.getSysTenantUsername())
        );
        if (null == sysUser) {
            return;
        }

        sysUserMapper.deleteById(sysUser.getSysUserId());
    }
}
