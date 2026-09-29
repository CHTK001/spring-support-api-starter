package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
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
import com.chua.starter.system.support.MenuVisibilityEvaluator;
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

    final SysRoleMenuService sysRoleMenuService; // sys角色menu服务
    final ApplicationEventPublisher eventPublisher; // 事件发布
    final MenuVisibilityEvaluator menuVisibilityEvaluator; // 菜单动态显示条件求值器
    @Override
    /**
     * 列表menu
    */
    public ReturnResult<List<SysMenuSettingVO>> listMenu(SysMenu sysMenu, Set<String> roles, String username) {
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
        sysMenus = filterMenusByVisibility(sysMenus);

        List<SysMenu> filterMenus = Collections.emptyList();
        if (AuthConstant.isPrivileged(username, roles)) {
            filterMenus = sysMenus;
        } else {
            filterMenus = sysMenus.stream()
                    .filter(sysMenu1 -> list.stream().anyMatch(sysRoleMenu -> sysRoleMenu.getSysMenuId().equals(sysMenu1.getSysMenuId())))
                    .filter(sysMenu1 -> null != sysMenu1.getSysMenuHidden() && sysMenu1.getSysMenuHidden() == SystemConstant.NOT_DELETED)
                    .toList();
            if(CollectionUtils.isEmpty(filterMenus)) {
                // 未被授权任何菜单是合法状态：返回空集合而非报错，前端据此渲染「无菜单」
                return ReturnResult.ok(Collections.emptyList());
            }
        }


        return ReturnResult.ok(buildMenuSettingTree(filterMenus));
    }

    @Override
    /**
     * 全部
    */
    public ReturnResult<List<SysMenu>> all(SysMenu sysMenu, Set<String> roles) {
        return ReturnResult.ok(filterMenusByEngine(
                listAll(),
                null == sysMenu ? null : sysMenu.getSysMenuEngine()
        ));
    }

    /**
     * 构建menusetting树
     *
     * @param menus menus
     * @return 构建menusetting树的结果
     */
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

    /**
     * 排序menusetting树
     *
     * @param nodes 节点
     * @param comparator comparator
     */
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
    /**
     * 列表
    */
    public ReturnResult<List<SysMenuVO>> list(SysMenu sysMenu, Set<String> roles, int ignoreType, String username) {
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
        sysMenus = filterMenusByVisibility(sysMenus);
        if(ignoreType > -1) {
            // sys_menu_type 建表允许为 NULL（DEFAULT NULL），直接用 != 比较会拆箱抛 NPE 导致整个菜单接口 500
            sysMenus = sysMenus.stream()
                    .filter(sysMenu1 -> !Objects.equals(sysMenu1.getSysMenuType(), ignoreType))
                    .toList();
        }

        // 内置 admin 账号（username='admin'）与超管一样拥有全部菜单；
        // 其它账号（即使被授予 ADMIN 角色）一律按角色菜单关联(sys_role_menu)授权，受角色配置控制
        boolean isBuiltinAdminAccount = AuthConstant.isBuiltinAdmin(username);

        List<SysMenu> filterMenus = Collections.emptyList();
        if(AuthConstant.isSuperAdmin(roles) || isBuiltinAdminAccount) {
            filterMenus = sysMenus;
        } else {
            filterMenus = sysMenus.stream()
                    .filter(sysMenu1 -> list.stream().anyMatch(sysRoleMenu -> sysRoleMenu.getSysMenuId().equals(sysMenu1.getSysMenuId())))
                    .filter(sysMenu1 -> null != sysMenu1.getSysMenuHidden() && sysMenu1.getSysMenuHidden() == SystemConstant.NOT_DELETED)
                    .toList();
            if(CollectionUtils.isEmpty(filterMenus)) {
                // 未被授权任何菜单是合法状态：返回空集合而非报错，前端据此渲染「无菜单」
                return ReturnResult.ok(Collections.emptyList());
            }
        }


        // 确定哪些节点是目录（被其他节点引用为父节点）；脱离菜单控制的行不参与目录判定，避免父级变成空目录
        Set<Integer> directoryIds = filterMenus.stream()
                .filter(item -> !isDetachedFromMenu(item))
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

 // koi 前端契约字段：扁平 + 父id 关联
                    sysMenuVO.setParentId(sysMenu1.getSysMenuPid());
                    sysMenuVO.setMenuId(sysMenu1.getSysMenuId());
                    sysMenuVO.setMenuName(sysMenu1.getSysMenuTitle());
                    boolean directory = directoryIds.contains(sysMenu1.getSysMenuId());
                    sysMenuVO.setMenuType(directory ? "1" : "0");
                    sysMenuVO.setIsVisible(Objects.equals(ObjectUtils.defaultIfNull(sysMenu1.getSysMenuHidden(), 0), 0) ? "1" : "0");
                    sysMenuVO.setIsKeepAlive(Objects.equals(ObjectUtils.defaultIfNull(sysMenu1.getSysMenuKeepAlive(), 0), 1) ? "1" : "0");
                    sysMenuVO.setRemind(isDetachedFromMenu(sysMenu1));
                    // 菜单位置透传：standard(侧边栏) / top(顶栏按钮)，布局据此渲染顶栏入口
                    sysMenuVO.setSysMenuPosition(sysMenu1.getSysMenuPosition());
                    // 单子菜单时是否显示父级菜单透传：0 显示父级；1/未配置 隐藏父级（子菜单直接显示）
                    sysMenuVO.setSysMenuShowParent(sysMenu1.getSysMenuShowParent());
                    sysMenuVO.setIcon(sysMenu1.getSysMenuIcon());
                    // 菜单徽标三字段透传（缺省不开启）
                    sysMenuVO.setIsTag(ObjectUtils.defaultIfNull(sysMenu1.getSysMenuIsTag(), 0));
                    sysMenuVO.setTagType(sysMenu1.getSysMenuTagType());
                    sysMenuVO.setTagName(sysMenu1.getSysMenuTagName());
                    return sysMenuVO;
                })
                .toList();

        return ReturnResult.ok(result);
    }

    /**
     * 该菜单行是否脱离菜单控制（sys_menu_remind = 1）
     * <p>
     * 脱离后仍会下发给前端注册路由，但不再被当作菜单处理（侧边栏/目录判定都不参与）。
     *
     * @param sysMenu 菜单行
     * @return true 表示不作为菜单处理
     */
    private static boolean isDetachedFromMenu(SysMenu sysMenu) {
        return null != sysMenu && Objects.equals(ObjectUtils.defaultIfNull(sysMenu.getSysMenuRemind(), 0), 1);
    }

    @Override
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:menu:list:all'")
    /**
     * 列表全部
     *
     * @return 列表全部的结果
     */
    public List<SysMenu> listAll() {
        return baseMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .orderByAsc(SysMenu::getSysMenuSort));
    }


    @Override
    /**
     * pageformenu
    */
    public IPage<SysMenu> pageForMenu(Query<SysMenu> query, SysMenu sysMenu, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysMenu>lambdaQuery()
                .like(StringUtils.isNotBlank(sysMenu.getSysMenuName()), SysMenu::getSysMenuName, sysMenu.getSysMenuName())
                .eq(null != sysMenu.getSysMenuEngine(), SysMenu::getSysMenuEngine, sysMenu.getSysMenuEngine())
        );
    }

    /**
     * 过滤menusbyengine
     *
     * @param sysMenus sysMenus
     * @param sysMenuEngine sysMenuEngine
     * @return 过滤器menusbyengine的结果
     */
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

    /**
     * 按动态显示条件过滤菜单。
     *
     * <p>条件未配置（ALWAYS）的菜单恒显示；条件明确为假的菜单被剔除。
     * 求值器内部对异常采取「放行」策略，所以这里不会因为一条脏配置把整棵树清空。</p>
     *
     * @param sysMenus 菜单
     * @return 过滤后的菜单
     */
    private List<SysMenu> filterMenusByVisibility(List<SysMenu> sysMenus) {
        if (null == sysMenus || sysMenus.isEmpty() || null == menuVisibilityEvaluator) {
            return null == sysMenus ? Collections.emptyList() : sysMenus;
        }
        return sysMenus.stream()
                .filter(menu -> menuVisibilityEvaluator.visible(menu))
                .toList();
    }

    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:menu:list:all'")
    /**
     * 删除
     *
     * @param menuId menuId
     * @return 删除的结果
     */
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
    /**
     * 更新
     *
     * @param roles 角色
     * @param sysMenu sysMenu
     * @return 更新的结果
     */
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
    /**
     * 保存
     *
     * @param sysMenu sysMenu
     * @param roles 角色
     * @return 保存的结果
     */
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

    /**
     * 解析menu树id
     *
     * @param menuId menuId
     * @param parentMenuId 父menuid
     * @param validateSelfReference 校验self引用
     * @return resolvemenu树id的结果
     */
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

    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:menu:list:all'")
    public ReturnResult<Boolean> refreshCache() {
        return ReturnResult.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, allEntries = true)
    /**
     * 批量保存按钮
    */
    public ReturnResult<Boolean> batchSaveButtons(List<SysMenu> menus) {
        if (CollectionUtils.isEmpty(menus)) {
            return ReturnResult.illegal("按钮列表不能为空");
        }
        for (SysMenu menu : menus) {
            // 按钮类型固定为 3(button), 权限标识为空时自动生成
            if (null == menu.getSysMenuType()) {
                menu.setSysMenuType(3);
            }
            if (StringUtils.isBlank(menu.getSysMenuPerm())) {
                menu.setSysMenuPerm(generatePerm(menu));
            }
            if (null == menu.getSysMenuId()) {
                baseMapper.insert(menu);
            } else {
                baseMapper.updateById(menu);
            }
        }
        return ReturnResult.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, allEntries = true)
    /**
     * 批量删除按钮
    */
    public ReturnResult<Boolean> batchDeleteButtons(List<Integer> menuIds) {
        if (CollectionUtils.isEmpty(menuIds)) {
            return ReturnResult.illegal("按钮ID不能为空");
        }
        return ReturnResult.of(baseMapper.deleteBatchIds(menuIds) > 0);
    }

    /**
     * 根据父菜单 path + 菜单名称自动生成权限标识, 如 system:user:add
     * @param menu 方法入参 menu
     * @return 结果字符串
     */
    private String generatePerm(SysMenu menu) {
        String base = "";
        if (null != menu.getSysMenuPid() && menu.getSysMenuPid() > 0) {
            SysMenu parent = baseMapper.selectById(menu.getSysMenuPid());
            if (null != parent && StringUtils.isNotBlank(parent.getSysMenuPath())) {
                base = parent.getSysMenuPath().replaceAll("^/+", "").replace('/', ':');
            }
        }
        String name = StringUtils.isNotBlank(menu.getSysMenuName()) ? menu.getSysMenuName()
                : (StringUtils.isNotBlank(menu.getSysMenuTitle()) ? menu.getSysMenuTitle() : "");
        if (StringUtils.isBlank(base)) {
            return name;
        }
        return base + ":" + name;
    }

}
