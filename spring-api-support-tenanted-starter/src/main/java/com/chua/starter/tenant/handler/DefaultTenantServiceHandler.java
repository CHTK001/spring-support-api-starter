package com.chua.starter.tenant.handler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.bean.BeanUtils;
import com.chua.starter.service.mapper.SysUserMapper;
import com.chua.starter.system.entity.SysMenu;
import com.chua.starter.system.entity.SysTenant;
import com.chua.starter.system.mapper.SysMenuMapper;
import com.chua.starter.system.mapper.SysTenantMapper;
import com.chua.starter.system.service.SysMenuService;
import com.chua.starter.tenant.TenantServiceHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;

import java.util.*;
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

    @Override
    public void saveOrUpdate(Integer sysTenantId, List<SysMenu> sysMenus) {
        Long l = sysTenantMapper.selectCount(Wrappers.lambdaQuery());
        if (l > 0) {
            if (!sysTenantMapper.exists(Wrappers.<SysTenant>lambdaQuery().eq(SysTenant::getSysTenantId, sysTenantId))) {
                return;
            }
        }
        Set<Integer> menuIds = sysMenus.stream().map(SysMenu::getSysMenuId).collect(Collectors.toSet());
        sysMenuMapper.removeByTenantId(sysTenantId, menuIds);
        List<SysMenu> oldAllMenus = sysMenuMapper.selectList(Wrappers.<SysMenu>lambdaQuery().in(SysMenu::getSysMenuId, menuIds));
        Map<Integer, SysMenu> oldMenuMap = oldAllMenus.stream().collect(Collectors.toMap(SysMenu::getSysMenuId, it -> it));
        Map<Integer, SysMenu> newMenuMap = sysMenus.stream().collect(Collectors.toMap(SysMenu::getSysMenuId, it -> it));
        List<SysMenu> newMenu = new ArrayList<>(sysMenus.size());
        List<SysMenu> updateMenu = new ArrayList<>(sysMenus.size());
        for (SysMenu menu : sysMenus) {
            Integer menuId = menu.getSysMenuId();
            if (!oldMenuMap.containsKey(menuId)) {
                // 如果旧菜单中不存在该菜单 ID，则为新增菜单
                newMenu.add(menu);
                continue;
            }
            SysMenu oldMenu = oldMenuMap.get(menuId);
            if (!oldMenu.equals(menu)) {
                // 如果新旧菜单不相等，则为需要更新的菜单
                updateMenu.add(menu);
            }
        }
        sysMenuService.update(Collections.emptySet(), null);
        for (SysMenu sysMenu : newMenu) {
            sysMenuMapper.saveForTenant(sysMenu, sysTenantId);
        }

        for (SysMenu sysMenu : updateMenu) {
            SysMenu sysMenu1 = newMenuMap.get(sysMenu.getSysMenuId());
            BeanUtils.copyProperties(sysMenu1, sysMenu);
            sysMenuMapper.updateById(sysMenu);
        }
    }

    @Override
    public void delete(Integer sysTenantId, List<SysMenu> sysMenus) {
        Set<Integer> menuIds = sysMenus.stream().map(SysMenu::getSysMenuId).collect(Collectors.toSet());
        sysMenuMapper.removeByTenantId(sysTenantId, menuIds);
    }
}
