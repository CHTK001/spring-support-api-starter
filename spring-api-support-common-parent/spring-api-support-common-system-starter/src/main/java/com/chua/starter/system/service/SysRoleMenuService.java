package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.entity.system.SysRoleMenu;

import java.util.List;

/**
 * 角色-菜单关系服务
 *
 * <p>管理 {@code sys_role_id} 与 {@code sys_menu_id} 的多对多关系,
 * 用于 RBAC 权限控制。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
public interface SysRoleMenuService extends IService<SysRoleMenu> {

    /**
     * 查询指定角色的所有菜单 ID。
     *
     * @param roleId 角色主键
     * @return 菜单 ID 列表
     */
    List<Integer> listMenuIdsByRoleId(Integer roleId);

    /**
     * 查询拥有指定菜单的角色 ID 列表。
     *
     * @param menuId 菜单主键
     * @return 角色 ID 列表
     */
    List<Integer> listRoleIdsByMenuId(Integer menuId);

    /**
     * 查询指定用户的全部菜单 ID(经由角色关联)。
     *
     * @param userId 用户主键
     * @return 菜单 ID 列表
     */
    List<Integer> listMenuIdsByUserId(Integer userId);

    /**
     * 为角色分配菜单(全量替换)。
     *
     * @param roleId  角色主键
     * @param menuIds 新的菜单 ID 集合
     * @return 分配结果
     */
    boolean assignMenusToRole(Integer roleId, List<Integer> menuIds);

    /**
     * 为单个角色批量新增菜单关联。
     *
     * @param roleId  角色主键
     * @param menuIds 菜单 ID 列表
     * @return 新增关联数量
     */
    int addMenusToRole(Integer roleId, List<Integer> menuIds);

    /**
     * 删除角色与菜单的关联。
     *
     * @param roleId 角色主键
     * @param menuId 菜单主键
     * @return 是否删除成功
     */
    boolean removeRoleMenu(Integer roleId, Integer menuId);

    /**
     * 删除角色的所有菜单关联。
     *
     * @param roleId 角色主键
     * @return 删除数量
     */
    int removeAllMenusOfRole(Integer roleId);

    /**
     * 删除菜单的所有角色关联(用于菜单被删除时清理)。
     *
     * @param menuId 菜单主键
     * @return 删除数量
     */
    int removeAllRolesOfMenu(Integer menuId);
}
