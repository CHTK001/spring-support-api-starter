package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.utils.BeanUtils;
import com.google.common.base.Joiner;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.*;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.entity.system.SysRoleMenu;
import com.chua.starter.system.event.SysMenuChangeEvent;
import com.chua.starter.system.mapper.SysMenuMapper;
import com.chua.starter.system.pojo.menu.SysMenuSettingVO;
import com.chua.starter.system.pojo.menu.SysMenuVO;
import com.chua.starter.system.service.SysMenuService;
import com.chua.starter.system.service.SysRoleMenuService;
import com.github.yulichang.toolkit.MPJWrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 * <p>提供菜单的增删改查及树形结构转换功能</p>
 *
 * @since 2024/8/7
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService{

    final SysRoleMenuService sysRoleMenuService;
    final ApplicationEventPublisher eventPublisher;
    @Override
    /** ListMenu */
    public ReturnResult<List<SysMenuSettingVO>> listMenu(SysMenu sysMenu, Set<String> roles) {
        List<SysRoleMenu> list;
        if(!roles.isEmpty()) {
            list = CollectionUtils.isEmpty(roles) ? Collections.emptyList() : sysRoleMenuService.list(
                    MPJWrappers.<SysRoleMenu>lambdaJoin()
                            .select(SysRoleMenu::getSysMenuId)
                            .leftJoin(SysRole.class, SysRole::getSysRoleId, SysRole::getSysRoleId)
                            .in(SysRole::getSysRoleCode, roles)
            );
        } else {
            list = Collections.emptyList();
        }
        List<SysMenu> sysMenus = filterMenusByEngine(
                listAll(),
                null == sysMenu ? null : sysMenu.getSysMenuEngine()
        );

        List<SysMenu> filterMenus = Collections.emptyList();
        if (AuthConstant.isSuperAdmin(roles) || AuthConstant.isAdmin(roles)) {
            filterMenus = sysMenus;
        } else {
            filterMenus = sysMenus.stream()
                    .filter(sysMenu1 -> list.stream().anyMatch(sysRoleMenu -> sysRoleMenu.getSysMenuId().equals(sysMenu1.getSysMenuId())))
                    .filter(sysMenu1 -> null != sysMenu1.getSysMenuHidden() && sysMenu1.getSysMenuHidden() == SystemConstant.NOT_DELETED)
                    .toList();
            if(CollectionUtils.isEmpty(filterMenus)) {
                return ReturnResult.illegal("当前角色权限不足");
            }
        }


        return ReturnResult.ok(buildMenuSettingTree(filterMenus));
    }

    @Override
    /** All */
    public ReturnResult<List<SysMenu>> all(SysMenu sysMenu, Set<String> roles) {
        return ReturnResult.ok(filterMenusByEngine(
                listAll(),
                null == sysMenu ? null : sysMenu.getSysMenuEngine()
        ));
    }

    /** 构建MenuSettingTree */
    private List<SysMenuSettingVO> buildMenuSettingTree(List<SysMenu> menus) {
        if (CollectionUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }

        Map<Integer, SysMenuSettingVO> nodeMap = new LinkedHashMap<>();
        menus.forEach(menu -> {
            SysMenuSettingVO settingVO = new SysMenuSettingVO();
            BeanUtils.copyProperties(menu, settingVO);
            settingVO.setChildren(new ArrayList<>());
            nodeMap.put(settingVO.getSysMenuId(), settingVO);
        });

        List<SysMenuSettingVO> roots = new ArrayList<>();
        nodeMap.values().forEach(node -> {
            Integer parentId = node.getSysMenuPid();
            SysMenuSettingVO parent = null == parentId ? null : nodeMap.get(parentId);
            if (null != parent && !Objects.equals(parent.getSysMenuId(), node.getSysMenuId())) {
                parent.getChildren().add(node);
                return;
            }
            roots.add(node);
        });

        Comparator<SysMenuSettingVO> comparator = Comparator
                .comparing((SysMenuSettingVO item) -> ObjectUtils.defaultIfNull(item.getSysMenuSort(), Integer.MAX_VALUE))
                .thenComparing(item -> ObjectUtils.defaultIfNull(item.getSysMenuId(), Integer.MAX_VALUE));
        sortMenuSettingTree(roots, comparator);
        return roots;
    }

    /** 排序MenuSettingTree */
    private void sortMenuSettingTree(List<SysMenuSettingVO> nodes, Comparator<SysMenuSettingVO> comparator) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        nodes.sort(comparator);
        nodes.forEach(node -> {
            if (CollectionUtils.isEmpty(node.getChildren())) {
                node.setChildren(Collections.emptyList());
                return;
            }
            sortMenuSettingTree(node.getChildren(), comparator);
        });
    }

    @Override
    /** List */
    public ReturnResult<List<SysMenuVO>> list(SysMenu sysMenu, Set<String> roles, int ignoreType) {
        List<SysRoleMenu> list = CollectionUtils.isEmpty(roles) ? Collections.emptyList() : sysRoleMenuService.list(
                MPJWrappers.<SysRoleMenu>lambdaJoin()
                        .select(SysRoleMenu::getSysMenuId)
                        .leftJoin(SysRole.class, SysRole::getSysRoleId, SysRole::getSysRoleId)
                        .in(SysRole::getSysRoleCode, roles)
        );
        List<SysMenu> sysMenus = filterMenusByEngine(
                listAll(),
                null == sysMenu ? null : sysMenu.getSysMenuEngine()
        );
        if(ignoreType > -1) {
            sysMenus = sysMenus.stream().filter(sysMenu1 -> sysMenu1.getSysMenuType() != ignoreType).toList();
        }

        List<SysMenu> filterMenus = Collections.emptyList();
        if(AuthConstant.isSuperAdmin(roles)) {
            filterMenus = sysMenus;
        } else if(AuthConstant.isAdmin(roles)) {
            filterMenus = sysMenus.stream().filter(it -> {
                Integer sysMenuType = it.getSysMenuType();
                if(null == sysMenuType || sysMenuType == SystemConstant.PERMISSION_ENABLED) {
                    return true;
                }
                String sysMenuRole = it.getSysMenuRole();
                return StringUtils.isEmpty(sysMenuRole) || ArrayUtils.containsIgnoreCase(it.getSysMenuRole().split(","), AuthConstant.ADMIN);
            }).toList();
        } else {
            filterMenus = sysMenus.stream()
                    .filter(sysMenu1 -> list.stream().anyMatch(sysRoleMenu -> sysRoleMenu.getSysMenuId().equals(sysMenu1.getSysMenuId())))
                    .filter(sysMenu1 -> null != sysMenu1.getSysMenuHidden() && sysMenu1.getSysMenuHidden() == SystemConstant.NOT_DELETED)
                    .toList();
            if(CollectionUtils.isEmpty(filterMenus)) {
                return ReturnResult.illegal("当前角色权限不足");
            }
        }


        // 确定哪些节点是目录（被其他节点引用为父节点）
        Set<Integer> directoryIds = filterMenus.stream()
                .map(SysMenu::getSysMenuPid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 排序后输出扁平结构，字段对齐 koi 前端菜单契约
        List<SysMenuVO> result = filterMenus.stream()
                .sorted(Comparator
                        .comparing((SysMenu item) -> ObjectUtils.defaultIfNull(item.getSysMenuSort(), Integer.MAX_VALUE))
                        .thenComparing(item -> ObjectUtils.defaultIfNull(item.getSysMenuId(), Integer.MAX_VALUE)))
                .map(sysMenu1 -> {
                    SysMenuVO sysMenuVO = new SysMenuVO();
                    sysMenuVO.setName(sysMenu1.getSysMenuName());
                    sysMenuVO.setPath(sysMenu1.getSysMenuPath());
                    sysMenuVO.setSysMenuId(sysMenu1.getSysMenuId());
                    sysMenuVO.setSysMenuPid(sysMenu1.getSysMenuPid());
                    sysMenuVO.setSysMenuType(sysMenu1.getSysMenuType());
                    sysMenuVO.setSysMenuEngine(sysMenu1.getSysMenuEngine());
                    sysMenuVO.setComponent(sysMenu1.getSysMenuComponent());
                    sysMenuVO.setRedirect(sysMenu1.getSysMenuRedirect());
                    sysMenuVO.setLeaveTransition(sysMenu1.getSysMenuLeaveTransition());
                    sysMenuVO.setEnterTransition(sysMenu1.getSysMenuEnterTransition());
                    sysMenuVO.setSysMenuTitle(sysMenu1.getSysMenuTitle());
                    sysMenuVO.setSysMenuIcon(sysMenu1.getSysMenuIcon());
                    sysMenuVO.setSysMenuName(sysMenu1.getSysMenuName());

                    // koi 前端契约字段：扁平 + parentId 关联
                    sysMenuVO.setParentId(sysMenu1.getSysMenuPid());
                    sysMenuVO.setMenuId(sysMenu1.getSysMenuId());
                    sysMenuVO.setMenuName(sysMenu1.getSysMenuTitle());
                    boolean directory = directoryIds.contains(sysMenu1.getSysMenuId());
                    sysMenuVO.setMenuType(directory ? "1" : "0");
                    sysMenuVO.setIsVisible(Objects.equals(ObjectUtils.defaultIfNull(sysMenu1.getSysMenuHidden(), 0), 0) ? "1" : "0");
                    sysMenuVO.setIsKeepAlive(Objects.equals(ObjectUtils.defaultIfNull(sysMenu1.getSysMenuKeepAlive(), 0), 1) ? "1" : "0");
                    sysMenuVO.setIcon(sysMenu1.getSysMenuIcon());
                    return sysMenuVO;
                })
                .toList();

        return ReturnResult.ok(result);
    }

    @Override
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:menu:list:all'")
    /** ListAll */
    public List<SysMenu> listAll() {
        return baseMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .orderByAsc(SysMenu::getSysMenuSort));
    }


    @Override
    /** PageForMenu */
    public IPage<SysMenu> pageForMenu(Query<SysMenu> query, SysMenu sysMenu, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysMenu>lambdaQuery()
                .like(StringUtils.isNotBlank(sysMenu.getSysMenuName()), SysMenu::getSysMenuName, sysMenu.getSysMenuName())
                .eq(null != sysMenu.getSysMenuEngine(), SysMenu::getSysMenuEngine, sysMenu.getSysMenuEngine())
        );
    }

    /** 过滤MenusByEngine */
    private List<SysMenu> filterMenusByEngine(List<SysMenu> sysMenus, Integer sysMenuEngine) {
        if (CollectionUtils.isEmpty(sysMenus)) {
            return Collections.emptyList();
        }
        if (null == sysMenuEngine) {
            return sysMenus;
        }
        return sysMenus.stream()
                .filter(menu -> Objects.equals(ObjectUtils.defaultIfNull(menu.getSysMenuEngine(), 0), sysMenuEngine))
                .toList();
    }

    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:menu:list:all'")
    /** 删除 */
    public ReturnResult<Boolean> delete(Long menuId) {
        boolean deleted = baseMapper.deleteById(menuId) > 0;
        if (deleted) {
            eventPublisher.publishEvent(new SysMenuChangeEvent(this, menuId.intValue(), "delete"));
        }
        return ReturnResult.of(deleted);
    }

    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:menu:list:all'")
    @Transactional(rollbackFor = Exception.class)
    /** 更新 */
    public ReturnResult<Boolean> update(Set<String> roles, SysMenu sysMenu) {
        if (null == sysMenu) {
            return ReturnResult.error("菜单不存在");
        }
        if (null == sysMenu.getSysMenuId()) {
            return ReturnResult.error("菜单ID不能为空");
        }
        String menuTreeId;
        try {
            menuTreeId = resolveMenuTreeId(sysMenu.getSysMenuId(), sysMenu.getSysMenuPid(), true);
        } catch (IllegalArgumentException e) {
            return ReturnResult.error(e.getMessage());
        }
        if (null == menuTreeId) {
            return ReturnResult.error("父级菜单不存在");
        }
        sysMenu.setSysMenuTreeId(menuTreeId);
        boolean updated = baseMapper.updateById(sysMenu) > 0;
        if (updated) {
            eventPublisher.publishEvent(new SysMenuChangeEvent(this, sysMenu.getSysMenuId(), "update"));
        }
        return ReturnResult.of(updated);
    }

    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:menu:list:all'")
    @Transactional(rollbackFor = Exception.class)
    /** 保存 */
    public ReturnResult<SysMenu> save(SysMenu sysMenu, Set<String> roles) {
        if (null == sysMenu) {
            return ReturnResult.error("菜单不存在");
        }
        sysMenu.setSysMenuId(null);
        if (null == sysMenu.getSysMenuPid()) {
            sysMenu.setSysMenuPid(SystemConstant.ROOT_NODE_ID);
        }
        baseMapper.insert(sysMenu);
        String menuTreeId;
        try {
            menuTreeId = resolveMenuTreeId(sysMenu.getSysMenuId(), sysMenu.getSysMenuPid(), false);
        } catch (IllegalArgumentException e) {
            return ReturnResult.error(e.getMessage());
        }
        if (null == menuTreeId) {
            return ReturnResult.error("父级菜单不存在");
        }
        sysMenu.setSysMenuTreeId(menuTreeId);
        if (baseMapper.updateById(sysMenu) <= 0) {
            return ReturnResult.error("保存菜单失败");
        }
        SysMenu saved = baseMapper.selectById(sysMenu.getSysMenuId());
        eventPublisher.publishEvent(new SysMenuChangeEvent(this, saved.getSysMenuId(), "create"));
        return ReturnResult.of(saved);
    }

    /** 解析MenuTreeId */
    private String resolveMenuTreeId(Integer menuId, Integer parentMenuId, boolean validateSelfReference) {
        if (null == parentMenuId || parentMenuId == SystemConstant.ROOT_NODE_ID) {
            return Joiner.on(",").join(SystemConstant.ROOT_NODE_ID, menuId);
        }
        SysMenu parentSysMenu = baseMapper.selectById(parentMenuId);
        if (null == parentSysMenu) {
            return null;
        }
        if (validateSelfReference && StringUtils.transArrayContains(parentSysMenu.getSysMenuTreeId(), String.valueOf(menuId))) {
            throw new IllegalArgumentException("不能选择自身作为父级菜单");
        }
        return Joiner.on(",").join(parentSysMenu.getSysMenuTreeId(), menuId);
    }

}
