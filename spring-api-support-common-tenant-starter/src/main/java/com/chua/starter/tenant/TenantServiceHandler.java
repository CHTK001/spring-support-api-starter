package com.chua.starter.tenant;

import com.chua.starter.system.entity.SysMenu;

import java.util.List;

/**
 * 租户服务处理器
 *
 * @author CH
 */
public interface TenantServiceHandler {
    /**
     * 保存或者更新
     *
     * @param sysTenantId 租户ID
     * @param menus       菜单
     */
    void saveOrUpdate(Integer sysTenantId, List<SysMenu> menus);

    /**
     * 删除
     *
     * @param sysTenantId 租户
     * @param menus       菜单
     */
    void delete(Integer sysTenantId, List<SysMenu> menus);
}
