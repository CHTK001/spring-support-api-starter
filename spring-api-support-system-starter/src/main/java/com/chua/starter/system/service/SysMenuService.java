package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysMenu;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Set;

import static com.chua.starter.system.constant.SystemConstant.CACHE_WITH_REDIS;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */
public interface SysMenuService extends IService<SysMenu>{


    List<SysMenu> list(SysMenu sysMenu, Set<String> roles);

    @Cacheable(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'sys:menu:list:all'")
    List<SysMenu> listAll();

    IPage<SysMenu> pageForMenu(Query<SysMenu> query, SysMenu sysMenu, Set<String> roles);

    ReturnResult<Boolean> delete(Long menuId);

    ReturnResult<Boolean> update(Set<String> roles, SysMenu sysMenu);

    ReturnResult<SysMenu> save(SysMenu sysMenu, Set<String> roles);
}
