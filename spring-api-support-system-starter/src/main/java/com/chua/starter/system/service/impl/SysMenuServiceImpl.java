package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.treenode.TreeNode;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.TreeUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysMenu;
import com.chua.starter.system.entity.SysRoleMenu;
import com.chua.starter.system.mapper.SysMenuMapper;
import com.chua.starter.system.pojo.menu.SysMenuVO;
import com.chua.starter.system.service.SysMenuService;
import com.chua.starter.system.service.SysRoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    public ReturnResult<List<SysMenuVO>> list(SysMenu sysMenu, Set<String> roles) {
        List<SysRoleMenu> list = sysRoleMenuService.list(Wrappers.<SysRoleMenu>lambdaQuery()
                .in(SysRoleMenu::getSysRoleId, roles)
                .select(SysRoleMenu::getSysMenuId)
        );
        List<SysMenu> sysMenus = listAll();

        List<SysMenu> filterMenus = Collections.emptyList();
        if(AuthConstant.isSuperAdmin(roles)) {
            filterMenus = sysMenus;
        } else {
            filterMenus = sysMenus.stream()
                    .filter(sysMenu1 -> list.stream().anyMatch(sysRoleMenu -> sysRoleMenu.getSysMenuId().equals(sysMenu1.getSysMenuId())))
                    .filter(sysMenu1 -> null != sysMenu1.getSysMenuHidden() && sysMenu1.getSysMenuHidden() == 0)
                    .toList();
            if(CollectionUtils.isEmpty(filterMenus)) {
                return ReturnResult.illegal("当前帐号权限不足");
            }
        }


        return ReturnResult.ok(TreeUtils.toTree(filterMenus, sysMenu1 -> {
            SysMenuVO sysMenuVO = new SysMenuVO();
            sysMenuVO.setName(sysMenu1.getSysMenuName());
            sysMenuVO.setPath(sysMenu1.getSysMenuPath());
            sysMenuVO.setSysMenuId(sysMenu1.getSysMenuId());
            sysMenuVO.setSysMenuPid(sysMenu1.getSysMenuPid());
            sysMenuVO.setComponent(sysMenu1.getSysMenuComponent());
            SysMenuVO.Meta meta = new SysMenuVO.Meta();
            meta.setIcon(sysMenu1.getSysMenuIcon());
            meta.setTitle(sysMenu1.getSysMenuTitle());
            meta.setI18nKey(sysMenu1.getSysMenuI18n());
            sysMenuVO.setMeta(meta);

            TreeNode<SysMenuVO> treeNode = new TreeNode<>();
            treeNode.setId(sysMenuVO.getSysMenuId().toString());
            treeNode.setLabel(sysMenuVO.getName());
            treeNode.setValue(sysMenuVO.getName());
            treeNode.setPid(sysMenuVO.getSysMenuPid().toString());
            treeNode.setExt(sysMenuVO);
            treeNode.setChildrenConsumer(sysMenuVO::setChildren);

            return treeNode;
        }));
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
        String menuTreeId = "";
        if(null == sysMenu.getSysMenuPid()) {
            sysMenu.setSysMenuPid(0);
            menuTreeId = "0," + sysMenu.getSysMenuId();
        } else {
            SysMenu parentSysMenu = baseMapper.selectById(sysMenu.getSysMenuPid());
            if(null == parentSysMenu) {
                return ReturnResult.error("父级菜单不存在");
            }
            menuTreeId = parentSysMenu.getSysMenuTreeId() + "," + sysMenu.getSysMenuId();
        }
        sysMenu.setSysMenuTreeId(menuTreeId);
        return ReturnResult.of(baseMapper.updateById(sysMenu) > 0);
    }

    @Override
    public ReturnResult<SysMenu> save(SysMenu sysMenu, Set<String> roles) {
        String menuTreeId = "";
        if(null == sysMenu.getSysMenuPid()) {
            sysMenu.setSysMenuPid(0);
            menuTreeId = "0," + sysMenu.getSysMenuId();
        } else {
            SysMenu parentSysMenu = baseMapper.selectById(sysMenu.getSysMenuPid());
            if(null == parentSysMenu) {
                return ReturnResult.error("父级菜单不存在");
            }
            menuTreeId = parentSysMenu.getSysMenuTreeId() + "," + sysMenu.getSysMenuId();
        }
        sysMenu.setSysMenuTreeId(menuTreeId);
        baseMapper.insert(sysMenu);
        return ReturnResult.guess(sysMenu);
    }
}
