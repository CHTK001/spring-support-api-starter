package com.chua.starter.system.pojo.menu;

import lombok.Data;

/**
 * 角色-菜单绑定的一行（菜单 id + 角色码）。
 *
 * <p>只用于菜单权限过滤：把「哪些菜单被某个角色绑定了」和「哪些菜单被任意角色绑定过」
 * 两个集合用<strong>一次联表查询</strong>取回来，避免在按用户过滤菜单时查两次
 * {@code sys_role_menu}（全量一次、当前角色一次）。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Data
public class RoleMenuBinding {

    /**
     * 菜单主键（{@code sys_role_menu.sys_menu_id}）
     */
    private Integer menuId;

    /**
     * 角色码（{@code sys_role.sys_role_code}）。
     *
     * <p>角色行被删除后可能为 {@code null}（LEFT JOIN），调用方需容忍。</p>
     */
    private String roleCode;
}
