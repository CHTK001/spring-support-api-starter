package com.chua.starter.sso.impl;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用户和角色关联表
 *
 * @author haoxr
 * @since 2022/12/17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUserRole {
    private static final long serialVersionUID = 1L;
    @TableId(value = "sys_user_role_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysUserRoleId;

    /**
     * 用户ID
     */
    @TableField(value = "sys_user_id")
    @ApiModelProperty(value = "用户ID")
    @Schema(description = "用户ID")
    private Integer sysUserId;

    /**
     * 角色ID
     */
    @TableField(value = "sys_role_id")
    @ApiModelProperty(value = "角色ID")
    @Schema(description = "角色ID")
    private Integer sysRoleId;
}