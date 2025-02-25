package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    /**
     * 删除租户菜单
     *
     * @param sysTenantId 租户
     * @param menuIds     可用菜单
     */
    void removeByTenantId(@Param("sysTenantId") Integer sysTenantId, @Param("menuIds") Set<Integer> menuIds);

    /**
     * 保存
     *
     * @param sysMenu     菜单
     * @param sysTenantId 租户
     */
    void saveForTenant(@Param("sysMenu") SysMenu sysMenu, @Param("sysTenantId") Integer sysTenantId);
}