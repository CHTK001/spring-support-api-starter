package com.chua.starter.entity.system;

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
 * 用户-角色关系表
 *
 * @author CH
 * @since 2024/8/7
 */
@ApiModel(description = "用户-角色关系表")
@Schema(description = "用户-角色关系表")
@Data
@TableName(value = "sys_user_role")
public class SysUserRole implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "sys_user_role_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空")
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
