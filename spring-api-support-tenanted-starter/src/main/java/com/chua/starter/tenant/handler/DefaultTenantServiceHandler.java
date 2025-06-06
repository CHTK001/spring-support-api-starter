package com.chua.starter.tenant.handler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.service.mapper.SysUserMapper;
import com.chua.starter.system.entity.SysMenu;
import com.chua.starter.system.entity.SysTenant;
import com.chua.starter.system.mapper.SysMenuMapper;
import com.chua.starter.system.mapper.SysTenantMapper;
import com.chua.starter.system.service.SysMenuService;
import com.chua.starter.tenant.TenantServiceHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 默认租户服务处理
 *
 * @author CH
 */
@SpringBootConfiguration
@RequiredArgsConstructor
public class DefaultTenantServiceHandler implements TenantServiceHandler {

    final SysUserMapper sysUserMapper;
    final SysMenuMapper sysMenuMapper;
    final SysTenantMapper sysTenantMapper;
    final SysMenuService sysMenuService;
    final ApplicationContext applicationContext;
    final CustomTenantedKeyGenerator customTenantedKeyGenerator;
    @Override
    public void saveOrUpdate(Integer sysTenantId, List<SysMenu> sysMenus) {
        Long l = sysTenantMapper.selectCount(Wrappers.lambdaQuery());
        if (l > 0) {
            if (!sysTenantMapper.exists(Wrappers.<SysTenant>lambdaQuery().eq(SysTenant::getSysTenantId, sysTenantId))) {
                return;
            }
        }
        customTenantedKeyGenerator.evict(CacheConstant.REDIS_CACHE_ALWAYS, CacheConstant.REDIS_CACHE_ALWAYS, String.valueOf(sysTenantId), "sys:menu:list:all");
        sysMenuMapper.removeAllByTenantId(sysTenantId);
        for (SysMenu sysMenu : sysMenus) {
            sysMenuMapper.saveForTenant(sysMenu, sysTenantId);
        }
    }

    @Override
    public void delete(Integer sysTenantId, List<SysMenu> sysMenus) {
        Set<Integer> menuIds = sysMenus.stream().map(SysMenu::getSysMenuId).collect(Collectors.toSet());
        sysMenuMapper.removeByTenantId(sysTenantId, menuIds);
    }
}
