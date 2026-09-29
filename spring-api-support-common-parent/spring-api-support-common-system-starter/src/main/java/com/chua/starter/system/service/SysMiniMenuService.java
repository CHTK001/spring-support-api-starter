package com.chua.starter.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysMiniMenu;
import com.chua.starter.system.pojo.menu.SysMiniMenuVO;

import java.util.List;
import java.util.Set;

/**
 * 小程序菜单服务接口
 *
 * @since 2026/08/03
 * @author CH
 */
public interface SysMiniMenuService extends IService<SysMiniMenu> {

    /**
     * 查询后端管理用的小程序菜单列表。
     *
     * @return 菜单列表结果
     */
    ReturnResult<List<SysMiniMenu>> listManageMenus();

    /**
     * 查询当前用户可见的小程序菜单列表。
     *
     * @param roles 用户角色集合
     * @param username 当前登录账号；内置 admin 账号全部可见，其余账号按角色授权
     * @return 菜单列表结果
     */
    ReturnResult<List<SysMiniMenuVO>> listUserMenus(Set<String> roles, String username);

    /**
     * 保存小程序菜单。
     *
     * @param sysMiniMenu 小程序菜单实体
     * @param roles       用户角色集合
     * @return 保存结果
     */
    ReturnResult<SysMiniMenu> saveMenu(SysMiniMenu sysMiniMenu, Set<String> roles);

    /**
     * 更新小程序菜单。
     *
     * @param roles       用户角色集合
     * @param sysMiniMenu 小程序菜单实体
     * @return 更新结果
     */
    ReturnResult<Boolean> updateMenu(Set<String> roles, SysMiniMenu sysMiniMenu);

    /**
     * 删除小程序菜单。
     *
     * @param sysMiniMenuId 菜单标识
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteMenu(Long sysMiniMenuId);
}
