package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysMenu;
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
     * 删除绉熸埛菜单
     *
     * @param sysTenantId 绉熸埛
     * @param menuIds     鍙敤菜单
     */
    void removeByTenantId(@Param("sysTenantId") Integer sysTenantId, @Param("menuIds") Set<Integer> menuIds);

    /**
     * 删除所有"
     *
     * @param sysTenantId 绉熸埛
     */
    void removeAllByTenantId(@Param("sysTenantId") Integer sysTenantId);
    /**
     * 保存
     *
     * @param sysMenu     菜单
     * @param sysTenantId 绉熸埛
     */
    void saveForTenant(@Param("sysMenu") SysMenu sysMenu, @Param("sysTenantId") Integer sysTenantId);

}