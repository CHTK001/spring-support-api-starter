package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 部门负责人关系表
 *
 * @author CH
 * @since 2026/4/5
 */
@Data
@Schema(description = "部门负责人关系表")
@TableName("sys_dept_principal_rel")
public class SysDeptPrincipalRel implements Serializable {

    @TableId(value = "sys_dept_principal_rel_id", type = IdType.AUTO)
    @Schema(description = "关系ID")
    private Integer sysDeptPrincipalRelId;

    @TableField("sys_dept_id")
    @Schema(description = "部门ID")
    private Integer sysDeptId;

    @TableField("sys_user_id")
    @Schema(description = "负责人用户ID")
    private Integer sysUserId;
}
