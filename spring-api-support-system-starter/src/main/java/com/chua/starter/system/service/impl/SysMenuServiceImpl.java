package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.function.Joiner;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.treenode.TreeNode;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.TreeUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysMenu;
import com.chua.starter.system.entity.SysRoleMenu;
import com.chua.starter.system.mapper.SysMenuMapper;
import com.chua.starter.system.pojo.menu.SysMenuSettingVO;
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
    public ReturnResult<List<SysMenuSettingVO>> listMenu(SysMenu sysMenu, Set<String> roles) {
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
            SysMenuSettingVO sysMenuSettingVO = new SysMenuSettingVO();
            BeanUtils.copyProperties(sysMenu1, sysMenuSettingVO);

            TreeNode<SysMenuSettingVO> treeNode = new TreeNode<>();
            treeNode.setId(sysMenuSettingVO.getSysMenuId().toString());
            treeNode.setLabel(sysMenuSettingVO.getSysMenuName());
            treeNode.setValue(sysMenuSettingVO.getSysMenuName());
            treeNode.setPid(sysMenuSettingVO.getSysMenuPid().toString());
            treeNode.setExt(sysMenuSettingVO);
            treeNode.setChildrenConsumer(sysMenuSettingVO::setChildren);

            return treeNode;
        }));
    }

    @Override
    public ReturnResult<List<SysMenu>> all(SysMenu sysMenu, Set<String> roles) {
        return ReturnResult.ok(listAll());
    }

    @Override
    public ReturnResult<List<SysMenuVO>> list(SysMenu sysMenu, Set<String> roles, int ignoreType) {
        List<SysRoleMenu> list = sysRoleMenuService.list(Wrappers.<SysRoleMenu>lambdaQuery()
                .in(SysRoleMenu::getSysRoleId, roles)
                .select(SysRoleMenu::getSysMenuId)
        );
        List<SysMenu> sysMenus = listAll();
        if(ignoreType > -1) {
            sysMenus = sysMenus.stream().filter(sysMenu1 -> sysMenu1.getSysMenuType() != ignoreType).toList();
        }

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
            sysMenuVO.setRedirect(sysMenu1.getSysMenuRedirect());
            sysMenuVO.setRedirect(sysMenu1.getSysMenuEnterTransition());
            sysMenuVO.setLeaveTransition(sysMenu1.getSysMenuLeaveTransition());
            sysMenuVO.setEnterTransition(sysMenu1.getSysMenuEnterTransition());
            SysMenuVO.Meta meta = new SysMenuVO.Meta();
            meta.setIcon(sysMenu1.getSysMenuIcon());
            meta.setExtraIcon(sysMenu1.getSysMenuExtraIcon());
            meta.setTitle(sysMenu1.getSysMenuTitle());
            meta.setI18nKey(sysMenu1.getSysMenuI18n());
            meta.setFrameSrc(sysMenu1.getSysMenuFrameSrc());
            meta.setActivePath(sysMenu1.getSysMenuActivePath());
            meta.setAuths(Splitter.on(",").trimResults().omitEmptyStrings().split(sysMenu1.getSysMenuPerm()));
            meta.setShowLink(ObjectUtils.equal(ObjectUtils.defaultIfNull(sysMenu1.getSysMenuHidden(), 0), 0));
            meta.setShowParent(ObjectUtils.equal(ObjectUtils.defaultIfNull(sysMenu1.getSysMenuShowParent(), 0), 0));
            meta.setKeepAlive(ObjectUtils.equal(ObjectUtils.defaultIfNull(sysMenu1.getSysMenuKeepAlive(), 0), 1));
            meta.setHiddenTag(ObjectUtils.equal(ObjectUtils.defaultIfNull(sysMenu1.getSysMenuHiddenTag(), 0), 1));
            meta.setFixedTag(ObjectUtils.equal(ObjectUtils.defaultIfNull(sysMenu1.getSysMenuFixedTag(), 0), 1));
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
        return baseMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .orderByAsc(SysMenu::getSysMenuSort));
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
        if(null == sysMenu.getSysMenuPid() || sysMenu.getSysMenuPid() == 0) {
            sysMenu.setSysMenuPid(0);
            menuTreeId = "0," + sysMenu.getSysMenuId();
        } else {
            SysMenu parentSysMenu = baseMapper.selectById(sysMenu.getSysMenuPid());
            if(null == parentSysMenu) {
                return ReturnResult.error("父级菜单不存在");
            }

            if(StringUtils.transArrayContains(parentSysMenu.getSysMenuTreeId(), String.valueOf(sysMenu.getSysMenuId()))) {
                return ReturnResult.error("不能选择自己作为父级菜单");
            }

            menuTreeId = Joiner.on(",").join( parentSysMenu.getSysMenuTreeId(), sysMenu.getSysMenuId());
        }
        sysMenu.setSysMenuTreeId(menuTreeId);
        return ReturnResult.of(baseMapper.updateById(sysMenu) > 0);
    }

    @Override
    public ReturnResult<SysMenu> save(SysMenu sysMenu, Set<String> roles) {
        String menuTreeId = "";
        if(null == sysMenu.getSysMenuPid() || sysMenu.getSysMenuPid() == 0) {
            sysMenu.setSysMenuPid(0);
            menuTreeId = "0," + sysMenu.getSysMenuId();
        } else {
            SysMenu parentSysMenu = baseMapper.selectById(sysMenu.getSysMenuPid());
            if(null == parentSysMenu) {
                return ReturnResult.error("父级菜单不存在");
            }
            menuTreeId = Joiner.on(",").join( parentSysMenu.getSysMenuTreeId(), sysMenu.getSysMenuId());
        }
        sysMenu.setSysMenuTreeId(menuTreeId);
        baseMapper.insert(sysMenu);
        return ReturnResult.guess(sysMenu);
    }


}
