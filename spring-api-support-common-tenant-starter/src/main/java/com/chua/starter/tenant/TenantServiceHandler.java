package com.chua.starter.tenant;

import com.chua.starter.system.entity.SysMenu;

import java.util.List;

/**
 * 租户服务处理器接口
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
public interface TenantServiceHandler {

    /**
     * 保存或更新租户服务
     *
     * @param sysTenantId 租户ID
     * @param sysMenus    菜单列表
     */
    void saveOrUpdate(Integer sysTenantId, List<SysMenu> sysMenus);

    /**
     * 删除租户服务
     *
     * @param sysTenantId 租户ID
     * @param sysMenus    菜单列表
     */
    void delete(Integer sysTenantId, List<SysMenu> sysMenus);
}
