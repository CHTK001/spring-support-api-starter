package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.system.entity.SysMenu;
import com.chua.starter.system.entity.SysTenantService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysTenantServiceService extends IService<SysTenantService> {


    /**
     * 根据租户ID获取菜单
     *
     * @param sysTenantId 租户ID
     * @return 菜单
     */
    List<SysMenu> getMenuIds(@Param("sysTenantId") Integer sysTenantId);
}
