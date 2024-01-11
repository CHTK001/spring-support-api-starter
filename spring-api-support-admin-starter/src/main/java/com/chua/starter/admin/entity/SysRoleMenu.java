package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 角色菜单表
 */
@ApiModel(description = "角色菜单表")
@Schema(description = "角色菜单表")
@Data
@AllArgsConstructor
@TableName(value = "sys_role_menu")
public class SysRoleMenu {
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;

}