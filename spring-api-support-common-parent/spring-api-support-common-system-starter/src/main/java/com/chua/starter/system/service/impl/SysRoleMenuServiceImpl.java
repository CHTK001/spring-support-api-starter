package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.entity.system.SysRoleMenu;
import com.chua.starter.system.mapper.SysRoleMenuMapper;
import com.chua.starter.system.service.SysRoleMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* 角色-菜单关系服务实现
*
* @since 4.0.0.42
* @author CH
 */
@Slf4j
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    @Override
    /** 列表menu标识by角色id */
    public List<Integer> listMenuIdsByRoleId(Integer roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }
        return list(Wrappers.<SysRoleMenu>lambdaQuery()
                .eq(SysRoleMenu::getSysRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getSysMenuId)
                .collect(Collectors.toList());
    }

    @Override
    /** 列表角色标识bymenuid */
    public List<Integer> listRoleIdsByMenuId(Integer menuId) {
        if (menuId == null) {
            return Collections.emptyList();
        }
        return list(Wrappers.<SysRoleMenu>lambdaQuery()
                .eq(SysRoleMenu::getSysMenuId, menuId))
                .stream()
                .map(SysRoleMenu::getSysRoleId)
                .collect(Collectors.toList());
    }

    @Override
    /** 列表menu标识by用户id */
    public List<Integer> listMenuIdsByUserId(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
 // 1. 查出用户的所有角色 标识
        List<Integer> roleIds = baseMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery())
                .stream()
                .map(SysRoleMenu::getSysRoleId)
                .distinct()
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 2. 查出这些角色的菜单 ID(去重)
        return baseMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery()
                .in(SysRoleMenu::getSysRoleId, roleIds))
                .stream()
                .map(SysRoleMenu::getSysMenuId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
    * assignmenus转为角色
    *
    * @param roleId 角色id
    * @param menuIds menu标识
    * @return assignmenus转为角色的结果
     */
    public boolean assignMenusToRole(Integer roleId, List<Integer> menuIds) {
        if (roleId == null) {
            return false;
        }
        removeAllMenusOfRole(roleId);
        if (menuIds == null || menuIds.isEmpty()) {
            return true;
        }
        return addMenusToRole(roleId, menuIds) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
    * 添加menus转为角色
    *
    * @param roleId 角色id
    * @param menuIds menu标识
    * @return 添加menus转为角色的结果
     */
    public int addMenusToRole(Integer roleId, List<Integer> menuIds) {
        if (roleId == null || menuIds == null || menuIds.isEmpty()) {
            return 0;
        }
        List<SysRoleMenu> list = new ArrayList<>(menuIds.size());
        for (Integer menuId : menuIds) {
            if (menuId == null) {
                continue;
            }
            SysRoleMenu rm = new SysRoleMenu();
            rm.setSysRoleId(roleId);
            rm.setSysMenuId(menuId);
            list.add(rm);
        }
        if (list.isEmpty()) {
            return 0;
        }
        boolean ok = saveBatch(list);
        return ok ? list.size() : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
    * 移除角色menu
    *
    * @param roleId 角色id
    * @param menuId menuId
    * @return 移除角色menu的结果
     */
    public boolean removeRoleMenu(Integer roleId, Integer menuId) {
        if (roleId == null || menuId == null) {
            return false;
        }
        return remove(Wrappers.<SysRoleMenu>lambdaQuery()
                .eq(SysRoleMenu::getSysRoleId, roleId)
                .eq(SysRoleMenu::getSysMenuId, menuId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
    * 移除全部menus的角色
    *
    * @param roleId 角色id
    * @return 移除全部menus的角色的结果
     */
    public int removeAllMenusOfRole(Integer roleId) {
        if (roleId == null) {
            return 0;
        }
        return baseMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery()
                .eq(SysRoleMenu::getSysRoleId, roleId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
    * 移除全部角色的menu
    *
    * @param menuId menuId
    * @return 移除全部角色的menu的结果
     */
    public int removeAllRolesOfMenu(Integer menuId) {
        if (menuId == null) {
            return 0;
        }
        return baseMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery()
                .eq(SysRoleMenu::getSysMenuId, menuId));
    }
}
