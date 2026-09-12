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
* @since 2026/4/5
* @author CH
 */
@Data
@Schema(description = "部门负责人关系表")
@TableName("sys_dept_principal_rel")
public class SysDeptPrincipalRel implements Serializable {

    private static final long serialVersionUID = 1L; // 串行版本uid

    @TableId(value = "sys_dept_principal_rel_id", type = IdType.AUTO)
    @Schema(description = "关系ID")
    /** sysdeptprincipalrelid */
    private Integer sysDeptPrincipalRelId;

    @TableField("sys_dept_id")
    @Schema(description = "部门ID")
    /** sysdeptid */
    private Integer sysDeptId;

    @TableField("sys_user_id")
    @Schema(description = "负责人用户ID")
    /** SYS用户标识 */
    private Integer sysUserId;
}
