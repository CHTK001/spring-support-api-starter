package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.entity.SysRole;
import com.chua.starter.system.pojo.role.RoleMenuUpdate;

import java.util.Set;

/**
 * 系统角色
 * @author CH
 * @since 2024/8/7
 */
public interface SysRoleService extends IService<SysRole> {
    /**
     * 分页查询角色信息
     *
     * @param query 查询条件封装对象
     * @param sysRole 角色实体对象，用于传递角色数据
     * @param roles 角色集合，用于过滤角色
     * @return 分页结果，包含角色信息
     */
    IPage<SysRole> pageForRole(Query<SysRole> query, SysRole sysRole, Set<String> roles);

    /**
     * 删除角色
     *
     * @param roleId 角色ID，用于确定要删除的角色
     * @return 删除结果，包含操作是否成功的标志
     */
    ReturnResult<Boolean> delete(Long roleId);

    /**
     * 更新角色信息
     *
     * @param roles 角色集合，用于批量操作角色
     * @param sysRole 待更新的角色信息
     * @return 更新结果，包含操作是否成功的标志
     */
    ReturnResult<Boolean> updateRole(Set<String> roles, SysRole sysRole);

    /**
     * 保存角色信息
     *
     * @param sysRole 待保存的角色信息
     * @return 保存结果，包含操作是否成功的标志以及保存的角色信息
     */
    ReturnResult<SysRole> saveRole(SysRole sysRole);

    /**
     * 更新角色菜单
     *
     * @param roles 角色集合，用于批量操作角色菜单
     * @param roleMenuUpdate 角色菜单更新对象，包含角色菜单的变更信息
     * @return 更新结果，包含操作是否成功的标志
     */
    ReturnResult<Boolean> updateMenu(Set<String> roles, RoleMenuUpdate roleMenuUpdate);

    /**
     * 获取角色菜单
     *
     * @param roleMenuUpdate 角色菜单更新对象，包含角色菜单的变更信息
     * @return 更新结果，包含操作是否成功的标志
     */
    ReturnResult<Set<Integer>> getMenu(RoleMenuUpdate roleMenuUpdate);
}
