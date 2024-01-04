package com.chua.starter.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.admin.entity.SysMenu;
import com.chua.starter.admin.pojo.RouteVO;
import com.chua.starter.admin.result.SysMenuTreeResult;

import java.util.List;
import java.util.Set;

/**
 * 菜单业务接口
 *
 * @author haoxr
 * @since 2020/11/06
 */
public interface SysMenuService extends IService<SysMenu> {
    /**
     * 根据角色获取菜单
     *
     * @param sysMenus 菜单信息
     * @param roles    角色
     * @return 菜单
     */
    List<RouteVO> myMenu(List<SysMenu> sysMenus, Set<String> roles);

    /**
     * 所有菜单
     *
     * @return 所有菜单
     */
    List<SysMenu> roleMenu();
    /**
     * 所有树形结构菜单
     *
     * @return 所有树形结构菜单
     */
    List<SysMenuTreeResult> roleMenuTree();

    /**
     * 保存菜单
     * @param sysMenu 菜单
     * @return 结果
     */
    SysMenu saveMenu(SysMenu sysMenu);
    /**
     * 更新菜单
     * @param sysMenu 菜单
     * @return 结果
     */
    boolean updateMenu(SysMenu sysMenu);
    /**
     * 删除菜单
     * @param menuId 菜单ID
     * @return 结果
     */
    boolean deleteMenu(String menuId);
}
