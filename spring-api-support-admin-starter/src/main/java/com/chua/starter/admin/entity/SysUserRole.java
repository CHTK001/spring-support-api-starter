package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户角色表
 */
@ApiModel(description = "用户角色表")
@Schema(description = "用户角色表")
@Data
@TableName(value = "sys_user_role")
@AllArgsConstructor
public class SysUserRole extends SysTenantBase {
    @TableField(value = "user_id")
    @ApiModelProperty(value = "用户ID")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField(value = "role_id")
    @ApiModelProperty(value = "角色ID")
    @Schema(description = "角色ID")
    private Long roleId;

}