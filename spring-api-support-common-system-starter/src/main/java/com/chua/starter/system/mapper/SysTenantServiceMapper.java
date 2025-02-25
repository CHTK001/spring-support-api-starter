package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysMenu;
import com.chua.starter.system.entity.SysTenantService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysTenantServiceMapper extends BaseMapper<SysTenantService> {
    /**
     * 根据租户ID获取菜单
     *
     * @param sysTenantId 租户ID
     * @return 菜单
     */
    List<SysMenu> getMenuByTenantId(@Param("sysTenantId") Integer sysTenantId);
}