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
 * 用户-角色关系表
 *
 * @since 2024/8/7
 * @author CH
 */
@Schema(description = "用户-角色关系表")
@Data
@TableName(value = "sys_user_role")
public class SysUserRole implements Serializable {
    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
      * 主键标识
     */
    @TableId(value = "sys_user_role_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空")
    /** SYS用户角色id */
    private Integer sysUserRoleId;

    /**
      * 用户标识
     */
    @TableField(value = "sys_user_id")
    @Schema(description = "用户ID")
    /** SYS用户标识 */
    private Integer sysUserId;

    /**
      * 角色标识
     */
    @TableField(value = "sys_role_id")
    @Schema(description = "角色ID")
    /** sysroleid */
    private Integer sysRoleId;
}
