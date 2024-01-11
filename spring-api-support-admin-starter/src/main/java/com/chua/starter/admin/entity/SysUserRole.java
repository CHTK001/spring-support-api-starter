package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 用户角色表
 */
@ApiModel(description = "用户角色表")
@Schema(description = "用户角色表")
@Data
@TableName(value = "sys_user_role")
public class SysUserRole {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    @TableField(value = "user_id")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Long userId;

    @TableField(value = "role_id")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Long roleId;

    @TableField(value = "tenant_id")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer tenantId;

    @TableField(value = "create_name")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @Size(max = 255, message = "最大长度要小于 255")
    private String createName;

    @TableField(value = "create_by")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer createBy;

    @TableField(value = "update_name")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @Size(max = 255, message = "最大长度要小于 255")
    private String updateName;

    @TableField(value = "update_by")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer updateBy;
}