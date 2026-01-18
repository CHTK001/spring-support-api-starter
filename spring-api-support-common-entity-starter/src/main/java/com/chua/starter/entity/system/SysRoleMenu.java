package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色-菜单关系表
 *
 * @author CH
 * @since 2024/8/7
 */
@Schema(description = "角色-菜单关系表")
@Data
@TableName(value = "sys_role_menu")
public class SysRoleMenu implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "sys_role_menu_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空")
    private Integer sysRoleMenuId;

    /**
     * 角色ID
     */
    @TableField(value = "sys_role_id")
    @Schema(description = "角色ID")
    private Integer sysRoleId;

    /**
     * 菜单ID
     */
    @TableField(value = "sys_menu_id")
    @Schema(description = "菜单ID")
    private Integer sysMenuId;
}
