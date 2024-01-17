package com.chua.starter.admin.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.lang.treenode.CustomTreeNode;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.admin.entity.SysMenu;
import com.chua.starter.admin.entity.SysUserSetting;
import com.chua.starter.admin.mapper.SysMenuMapper;
import com.chua.starter.admin.pojo.RouteVO;
import com.chua.starter.admin.result.SysMenuTreeResult;
import com.chua.starter.admin.result.SysUserGridResult;
import com.chua.starter.admin.result.UserMenuResult;
import com.chua.starter.admin.service.SysMenuService;
import com.chua.starter.admin.service.SysUserService;
import com.chua.starter.admin.service.SysUserSettingService;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单业务实现类
 *
 * @author haoxr
 * @since 2020/11/06
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService, Constant {


    @Resource
    private SysUserSettingService sysUserSettingService;
    @Resource
    private SysMenuService sysMenuService;
    @Resource
    private SysUserService sysUserService;
    @Override
    public List<RouteVO> myMenu(List<SysMenu> sysMenus, Set<String> roles) {
        List<SysMenu> tplMenus = sysMenus.stream().filter(it -> {
            if (it.getMenuStatus() == 0L) {
                return false;
            }

            if("button".equalsIgnoreCase(it.getMenuType())) {
                return false;
            }

            if (roles.contains(ADMIN)) {
                return true;
            }

            if (StringUtils.isBlank(it.getMenuPath())) {
                return false;
            }
            return CollectionUtils.contains(it.getRole(), roles);
        }).collect(Collectors.toList());
        return recurRoutes(0L, tplMenus);
    }

    /**
     * 递归生成菜单路由层级列表
     *
     * @param parentId 父级ID
     * @param menuList 菜单列表
     * @return 递归生成菜单路由层级列表
     */
    private List<RouteVO> recurRoutes(Long parentId, List<SysMenu> menuList) {
        return CollectionUtils.emptyIfNull(menuList).stream()
                .filter(menu -> menu.getMenuParentId().equals(parentId))
                .map(menu -> {
                    RouteVO.Meta meta = new RouteVO.Meta();
                    RouteVO routeVO = new RouteVO();
                    meta.setType(menu.getMenuType());
                    routeVO.setName(menu.getMenuName());
                    routeVO.setPath(menu.getMenuPath()); // 根据path路由跳转 this.$router.push({path:xxx})
                    routeVO.setRedirect(menu.getMenuRedirect());
                    routeVO.setHidden(menu.getMenuVisible() == 0L);
                    String component = StringUtils.removeStart(menu.getMenuComponent(), "/");
                    if (StringUtils.isNotEmpty(component)) {
                        if (component.endsWith(".vue")) {
                            routeVO.setComponent(component);
                        } else {
                            routeVO.setComponent(component + "/index");
                        }
                    }

                    meta.setTitle(menu.getMenuName());
                    meta.setIcon(menu.getMenuIcon());
                    meta.setTag(menu.getMenuTag());
                    meta.setColor(menu.getMenuColor());
                    meta.setRoles(Splitter.on(',').splitToList(menu.getRole()));
//                    meta.setHidden(StatusEnum.DISABLE.getValue().equals(menu.getMenuVisible()));
                    meta.setAffix(null != menu.getMenuAffix() && menu.getMenuAffix() == 1);
//                    meta.setKeepAlive(true);
                    routeVO.setMeta(meta);

                    if ("iframe".equalsIgnoreCase(menu.getMenuType())) {
                        RouteVO.Params query = new RouteVO.Params();
                        query.put("src", menu.getMenuPath());
                        routeVO.setPath(menu.getMenuPath()); // 根据path路由跳转 this.$router.push({path:xxx})
                        meta.setParams(query);
                        meta.setTitle(menu.getMenuName());
                    }

                    List<RouteVO> children = recurRoutes(menu.getMenuId(), menuList);
                    routeVO.setChildren(children);
                    return routeVO;
                }).collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'menu'")
    public List<SysMenu> roleMenu() {
        return baseMapper.roleMenu();
    }

    @Override
    @Cacheable(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'tree-menu'")
    public List<SysMenuTreeResult> roleMenuTree() {
        List<SysMenu> sysMenus = roleMenu();
        List<SysMenuTreeResult> tpl = new ArrayList<>(sysMenus.size());
        for (SysMenu sysMenu : sysMenus) {
            if (null != sysMenu.getMenuStatus() && sysMenu.getMenuStatus() == 0) {
                continue;
            }
            SysMenuTreeResult item = new SysMenuTreeResult();
            BeanUtils.copyProperties(sysMenu, item);
            item.setId(sysMenu.getMenuId() + "");
            item.setParentId(sysMenu.getMenuParentId() + "");
            item.setPath(sysMenu.getMenuPath());
            item.setComponent(sysMenu.getMenuComponent());
            SysMenuTreeResult.Meta meta = new SysMenuTreeResult.Meta();
            meta.setIcon(sysMenu.getMenuIcon());
            meta.setStatus(null != sysMenu.getMenuStatus() && sysMenu.getMenuStatus() == 0);
            meta.setTitle(sysMenu.getMenuName());
            meta.setHidden(null != sysMenu.getMenuVisible() && sysMenu.getMenuVisible() == 0);
            meta.setAffix(sysMenu.getMenuAffix());
            meta.setType(sysMenu.getMenuType());
            meta.setSort(sysMenu.getMenuSort());
            meta.setColor(sysMenu.getMenuColor());
            meta.setTag(sysMenu.getMenuTag());
            item.setMeta(meta);
            tpl.add(item);
        }

        CustomTreeNode<SysMenuTreeResult, String> treeNode = new CustomTreeNode<>(SysMenuTreeResult::getId, SysMenuTreeResult::getParentId, "0");
        treeNode.add(tpl);

        return treeNode.transferAll();
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'tree-menu'"),
                    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'menu'")
            }
    )
    public SysMenu saveMenu(SysMenu sysMenu) {
        if(BUTTON.equalsIgnoreCase(sysMenu.getMenuType())) {
            sysMenu.setMenuType(BUTTON.toLowerCase());
            sysMenu.setMenuPath(null);
            sysMenu.setMenuComponent(null);
        }

        if (null == sysMenu.getMenuId()) {
            if(StringUtils.isBlank(sysMenu.getMenuPath())) {
                throw new IllegalArgumentException("路径不能为空");
            }

            baseMapper.insert(sysMenu);
            return sysMenu;
        }
        SysMenu sysMenu1 = baseMapper.selectById(sysMenu.getMenuId());
        if (null != sysMenu1) {
            baseMapper.updateById(sysMenu);
            return sysMenu;
        }
        sysMenu.setMenuId(null);
        if(StringUtils.isBlank(sysMenu.getMenuPath())) {
            throw new IllegalArgumentException("路径不能为空");
        }
        baseMapper.insert(sysMenu);
        return sysMenu;
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'tree-menu'"),
                    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'menu'")
            }
    )
    public boolean updateMenu(SysMenu sysMenu) {
        return baseMapper.updateById(sysMenu) > 1;
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'tree-menu'"),
                    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'menu'")
            }
    )
    public boolean deleteMenu(String menuId) {
        return baseMapper.deleteBatchIds(Arrays.asList(menuId.split(","))) > 0;
    }

    @Override
    public Result<UserMenuResult> myMenu(String userId, String userGrid, String userDashboard, String username, Set<String> roles) {
        if(roles.isEmpty() && !ADMIN.equalsIgnoreCase(username)) {
            return Result.failed("联系管理员分配权限");
        }
        UserMenuResult userMenuResult = new UserMenuResult();
        userMenuResult.setPermissions(AuthClientExecute.getInstance().getUserResult().getPermission());
        List<SysMenu> sysMenus = sysMenuService.roleMenu();
        if (ADMIN.equalsIgnoreCase(username)) {
            roles = new HashSet<>(roles);
            roles.add(ADMIN);
        }

        userMenuResult.setMenu(myMenu(sysMenus, roles));
        userMenuResult.setDashboardGrid(Splitter.on(',').trimResults().omitEmptyStrings().splitToSet(userGrid));
        userMenuResult.setDashboard(userDashboard);
        SysUserGridResult result = new SysUserGridResult();
        SysUserSetting gridByUserId = sysUserSettingService.getGridByUserId(userId);
        try {
            result.setLayout(JSON.parseArray(gridByUserId.getLayout()));
        } catch (Exception e) {
            result.setLayout(new JSONArray());
        }
        try {
            result.setCopmsList(JSON.parseArray(gridByUserId.getCopmsList()));
        } catch (Exception ignored) {
        }
        userMenuResult.setGrid(result);
        return Result.success(userMenuResult);
    }
}
