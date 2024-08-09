package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */

/**
 * 用户 - 角色关系表
 */
@ApiModel(description = "用户 - 角色关系表")
@Schema(description = "用户 - 角色关系表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_role")
public class SysUserRole extends SysBase implements Serializable {
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