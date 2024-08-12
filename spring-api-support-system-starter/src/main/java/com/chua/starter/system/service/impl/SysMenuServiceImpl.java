package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysMenu;
import com.chua.starter.system.entity.SysRoleMenu;
import com.chua.starter.system.mapper.SysMenuMapper;
import com.chua.starter.system.service.SysMenuService;
import com.chua.starter.system.service.SysRoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService{

    final SysRoleMenuService sysRoleMenuService;


    @Override
    public List<SysMenu> list(SysMenu sysMenu, Set<String> roles) {
        List<SysRoleMenu> list = sysRoleMenuService.list(Wrappers.<SysRoleMenu>lambdaQuery()
                .in(SysRoleMenu::getSysRoleId, roles)
                .select(SysRoleMenu::getSysMenuId)
        );
        List<SysMenu> sysMenus = listAll();

        return sysMenus.stream()
                .filter(sysMenu1 -> list.stream().anyMatch(sysRoleMenu -> sysRoleMenu.getSysMenuId().equals(sysMenu1.getSysMenuId())))
                .filter(sysMenu1 -> null != sysMenu1.getSysMenuHidden() && sysMenu1.getSysMenuHidden() == 0)
                .toList();
    }

    @Override
    public List<SysMenu> listAll() {
        return baseMapper.selectList(Wrappers.lambdaQuery());
    }


    @Override
    public IPage<SysMenu> pageForMenu(Query<SysMenu> query, SysMenu sysMenu, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysMenu>lambdaQuery()
                .like(StringUtils.isNotBlank(sysMenu.getSysMenuName()), SysMenu::getSysMenuName, sysMenu.getSysMenuName())
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long menuId) {
        return ReturnResult.guess(baseMapper.deleteById(menuId) > 0);
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysMenu sysMenu) {
        sysMenu.setSysMenuTreeId(StringUtils.join(",", sysMenu.getSysMenuTreeId(), sysMenu.getSysMenuName()));
        return ReturnResult.of(baseMapper.updateById(sysMenu) > 0);
    }

    @Override
    public ReturnResult<SysMenu> save(SysMenu sysMenu, Set<String> roles) {
        sysMenu.setSysMenuTreeId(StringUtils.join(",", sysMenu.getSysMenuTreeId(), sysMenu.getSysMenuName()));
        baseMapper.insert(sysMenu);
        return ReturnResult.guess(sysMenu);
    }
}
