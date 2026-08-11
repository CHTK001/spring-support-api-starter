package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.utils.BeanUtils;
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

@Service
public class SysMiniMenuServiceImpl extends ServiceImpl<SysMiniMenuMapper, SysMiniMenu> implements SysMiniMenuService {

    @Override
    public ReturnResult<List<SysMiniMenu>> listManageMenus() {
        return ReturnResult.ok(listAllCached());
    }

    @Override
    public ReturnResult<List<SysMiniMenuVO>> listUserMenus(Set<String> roles) {
        List<SysMiniMenu> visibleMenus = listAllCached().stream()
                .filter(menu -> !Objects.equals(ObjectUtils.defaultIfNull(menu.getSysMiniMenuHidden(), 0), 1))
                .filter(menu -> matchesRoles(menu, roles))
                .toList();
        if (CollectionUtils.isEmpty(visibleMenus)) {
            return ReturnResult.ok(Collections.emptyList());
        }
        return ReturnResult.ok(
                visibleMenus.stream()
                        .map(menu -> BeanUtils.copyProperties(menu, SysMiniMenuVO.class))
                        .toList()
        );
    }

    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:mini-menu:list:all'")
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

    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:mini-menu:list:all'")
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateMenu(Set<String> roles, SysMiniMenu sysMiniMenu) {
        if (null == sysMiniMenu || null == sysMiniMenu.getSysMiniMenuId()) {
            return ReturnResult.error("小程序菜单ID不能为空");
        }
        normalize(sysMiniMenu);
        return ReturnResult.of(updateById(sysMiniMenu));
    }

    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:mini-menu:list:all'")
    public ReturnResult<Boolean> deleteMenu(Long sysMiniMenuId) {
        if (null == sysMiniMenuId) {
            return ReturnResult.illegal("小程序菜单不存在");
        }
        return ReturnResult.of(removeById(sysMiniMenuId));
    }

    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:mini-menu:list:all'")
    public List<SysMiniMenu> listAllCached() {
        return baseMapper.selectList(
                Wrappers.<SysMiniMenu>lambdaQuery()
                        .orderByAsc(SysMiniMenu::getSysMiniMenuSort)
                        .orderByAsc(SysMiniMenu::getSysMiniMenuId)
        );
    }

    private boolean matchesRoles(SysMiniMenu menu, Set<String> roles) {
        if (AuthConstant.isSuperAdmin(roles) || AuthConstant.isAdmin(roles)) {
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
