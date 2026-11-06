package com.chua.starter.system.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.system.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜单全量快照（带缓存）。
 *
 * <p>存在的唯一原因：{@code @Cacheable} 依赖 Spring AOP 代理，**同类内部调用不会经过代理**。
 * 之前 {@code SysMenuServiceImpl#listAll()} 虽然标了 {@code @Cacheable}，但它是被同类的
 * {@code listMenu()} / {@code list()} / {@code all()} 直接调用的，缓存从未生效 ——
 * 每次进页面都真实查一遍 {@code sys_menu}。</p>
 *
 * <p>把这份数据挪到独立 bean 后，{@code SysMenuServiceImpl} 对它的调用是跨 bean 的，
 * 代理正常生效，菜单列表改为命中 Redis 缓存；写操作仍然通过
 * {@code @CacheEvict(key = "'sys:menu:list:all'")} 失效同一个 key，语义不变。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class SysMenuSnapshotService {

    private final SysMenuMapper sysMenuMapper;

    /**
     * 取全部菜单（按 sort 升序），命中 Redis 缓存。
     *
     * @return 菜单列表
     */
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS,
            cacheNames = CacheConstant.REDIS_CACHE_ALWAYS,
            key = "'sys:menu:list:all:' + T(com.chua.starter.system.support.TenantCacheScope).scope()")
    public List<SysMenu> listAll() {
        return sysMenuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .orderByAsc(SysMenu::getSysMenuSort));
    }
}
