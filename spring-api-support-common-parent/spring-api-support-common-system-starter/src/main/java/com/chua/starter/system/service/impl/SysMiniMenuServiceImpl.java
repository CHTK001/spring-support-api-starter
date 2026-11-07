package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.utils.ArrayUtils;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.entity.system.SysMiniMenu;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.mapper.SysMiniMenuMapper;
import com.chua.starter.system.pojo.menu.SysMiniMenuVO;
import com.chua.starter.system.service.SysMiniMenuService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 小程序菜单服务实现类
 *
 * @since 2026/08/03
 * @author CH
 */
@Service
public class SysMiniMenuServiceImpl extends ServiceImpl<SysMiniMenuMapper, SysMiniMenu> implements SysMiniMenuService {

    /**
     * 查询后端管理用的小程序菜单列表。
     *
     * @return 菜单列表结果
     */
    @Override
    public ReturnResult<List<SysMiniMenu>> listManageMenus() {
        return ReturnResult.ok(listAllCached());
    }

    /**
     * 查询当前用户可见的小程序菜单列表。
     *
     * @param roles 用户角色集合
     * @param username 当前登录账号；内置 admin 账号全部可见，其余账号按角色授权
     * @return 菜单列表结果
     */
    @Override
    public ReturnResult<List<SysMiniMenuVO>> listUserMenus(Set<String> roles, String username) {
        List<SysMiniMenu> visibleMenus = listAllCached().stream()
                .filter(menu -> !Objects.equals(ObjectUtils.defaultIfNull(menu.getSysMiniMenuHidden(), 0), 1))
                .filter(menu -> matchesRoles(menu, roles, username))
                .toList();
        if (CollectionUtils.isEmpty(visibleMenus)) {
            return ReturnResult.ok(Collections.emptyList());
        }
        return ReturnResult.ok(
                visibleMenus.stream()
                        .map(menu -> new SysMiniMenuVO(
                                menu.getSysMiniMenuId(),
                                menu.getSysMiniMenuTitle(),
                                menu.getSysMiniMenuSubtitle(),
                                menu.getSysMiniMenuCategory(),
                                menu.getSysMiniMenuPath(),
                                menu.getSysMiniMenuJumpMode(),
                                menu.getSysMiniMenuIcon(),
                                menu.getSysMiniMenuCover(),
                                menu.getSysMiniMenuBadge(),
                                menu.getSysMiniMenuBadgeType(),
                                menu.getSysMiniMenuPerm(),
                                menu.getSysMiniMenuRole(),
                                menu.getSysMiniMenuSort(),
                                menu.getSysMiniMenuKeepAlive(),
                                menu.getSysMiniMenuHidden(),
                                menu.getSysMiniMenuDescription()))
                        .toList()
        );
    }

    /**
     * 保存小程序菜单。
     *
     * @param sysMiniMenu 小程序菜单实体
     * @param roles       用户角色集合
     * @return 保存结果
     */
    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:mini-menu:list:all:' + T(com.chua.s" +
        "tarter.system.support.TenantCacheScope).scope()")
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SysMiniMenu> saveMenu(SysMiniMenu sysMiniMenu, Set<String> roles) {
        if (null == sysMiniMenu) {
            return ReturnResult.error("小程序菜单不存在");
        }
        sysMiniMenu.setSysMiniMenuId(null);
        normalize(sysMiniMenu);
        if (!save(sysMiniMenu)) {
            return ReturnResult.error("保存小程序菜单失败");
        }
        return ReturnResult.ok(getById(sysMiniMenu.getSysMiniMenuId()));
    }

    /**
     * 更新小程序菜单。
     *
     * @param roles       用户角色集合
     * @param sysMiniMenu 小程序菜单实体
     * @return 更新结果
     */
    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:mini-menu:list:all:' + T(com.chua.s" +
        "tarter.system.support.TenantCacheScope).scope()")
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateMenu(Set<String> roles, SysMiniMenu sysMiniMenu) {
        if (null == sysMiniMenu || null == sysMiniMenu.getSysMiniMenuId()) {
            return ReturnResult.error("小程序菜单ID不能为空");
        }
        normalize(sysMiniMenu);
        return ReturnResult.of(updateById(sysMiniMenu));
    }

    /**
     * 删除小程序菜单。
     *
     * @param sysMiniMenuId 菜单标识
     * @return 删除结果
     */
    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:mini-menu:list:all:' + T(com.chua.s" +
        "tarter.system.support.TenantCacheScope).scope()")
    public ReturnResult<Boolean> deleteMenu(Long sysMiniMenuId) {
        if (null == sysMiniMenuId) {
            return ReturnResult.illegal("小程序菜单不存在");
        }
        return ReturnResult.of(removeById(sysMiniMenuId));
    }

    /**
     * 查询全部已缓存的小程序菜单（按排序字段和标识升序）。
     *
     * @return 小程序菜单列表
     */
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:mini-menu:list:all:' + T(com.chua.s" +
        "tarter.system.support.TenantCacheScope).scope()")
    public List<SysMiniMenu> listAllCached() {
        return baseMapper.selectList(
                Wrappers.<SysMiniMenu>lambdaQuery()
                        .orderByAsc(SysMiniMenu::getSysMiniMenuSort)
                        .orderByAsc(SysMiniMenu::getSysMiniMenuId)
        );
    }

    /**
     * 判断菜单是否对指定账号可见。
     *
     * @param menu     小程序菜单
     * @param roles    用户角色集合
     * @param username 当前登录账号
     * @return true 表示可见
     */
    private boolean matchesRoles(SysMiniMenu menu, Set<String> roles, String username) {
        if (AuthConstant.isPrivileged(username, roles)) {
            return true;
        }
        String allowRoles = menu.getSysMiniMenuRole();
        if (StringUtils.isBlank(allowRoles)) {
            return true;
        }
        if (CollectionUtils.isEmpty(roles)) {
            return false;
        }
        String[] targets = allowRoles.split(",");
        return roles.stream().anyMatch(role -> ArrayUtils.containsIgnoreCase(targets, role));
    }

    /**
     * 规范化菜单字段的缺省值。
     *
     * @param sysMiniMenu 小程序菜单实体
     */
    private void normalize(SysMiniMenu sysMiniMenu) {
        sysMiniMenu.setSysMiniMenuSort(ObjectUtils.defaultIfNull(sysMiniMenu.getSysMiniMenuSort(), 1));
        sysMiniMenu.setSysMiniMenuJumpMode(ObjectUtils.defaultIfNull(sysMiniMenu.getSysMiniMenuJumpMode(), 0));
        sysMiniMenu.setSysMiniMenuKeepAlive(ObjectUtils.defaultIfNull(sysMiniMenu.getSysMiniMenuKeepAlive(), 0));
        sysMiniMenu.setSysMiniMenuHidden(ObjectUtils.defaultIfNull(sysMiniMenu.getSysMiniMenuHidden(), 0));
        if (StringUtils.isBlank(sysMiniMenu.getSysMiniMenuBadgeType())) {
            sysMiniMenu.setSysMiniMenuBadgeType("primary");
        }
    }
}
