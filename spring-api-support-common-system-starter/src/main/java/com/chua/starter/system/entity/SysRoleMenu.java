package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */

/**
 * 角色 - 菜单关系�?
 */
@ApiModel(description = "角色 - 菜单关系�?)
@Schema(description = "角色 - 菜单关系�?)
@Data
@TableName(value = "sys_role_menu")
public class SysRoleMenu implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "sys_role_menu_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysRoleMenuId;

    /**
     * 角色ID
     */
    @TableField(value = "sys_role_id")
    @ApiModelProperty(value = "角色ID")
    @Schema(description = "角色ID")
    private Integer sysRoleId;

    /**
     * 菜单ID
     */
    @TableField(value = "sys_menu_id")
    @ApiModelProperty(value = "菜单ID")
    @Schema(description = "菜单ID")
    private Integer sysMenuId;
}