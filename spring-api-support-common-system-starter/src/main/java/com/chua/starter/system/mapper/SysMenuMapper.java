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
     * 鍒犻櫎绉熸埛鑿滃崟
     *
     * @param sysTenantId 绉熸埛
     * @param menuIds     鍙敤鑿滃崟
     */
    void removeByTenantId(@Param("sysTenantId") Integer sysTenantId, @Param("menuIds") Set<Integer> menuIds);

    /**
     * 鍒犻櫎鎵€鏈?
     *
     * @param sysTenantId 绉熸埛
     */
    void removeAllByTenantId(@Param("sysTenantId") Integer sysTenantId);
    /**
     * 淇濆瓨
     *
     * @param sysMenu     鑿滃崟
     * @param sysTenantId 绉熸埛
     */
    void saveForTenant(@Param("sysMenu") SysMenu sysMenu, @Param("sysTenantId") Integer sysTenantId);

}