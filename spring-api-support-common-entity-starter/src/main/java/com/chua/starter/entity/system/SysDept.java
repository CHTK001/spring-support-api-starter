package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 组织机构/部门表
 *
 * @author CH
 * @since 2024/8/7
 */
@Schema(description = "组织机构/部门表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dept")
public class SysDept extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 机构ID
     */
    @TableId(value = "sys_dept_id", type = IdType.AUTO)
    @Schema(description = "机构ID")
    @NotNull(message = "机构ID不能为空", groups = UpdateGroup.class)
    private Integer sysDeptId;

    /**
     * 机构名称
     */
    @TableField(value = "sys_dept_name")
    @Schema(description = "机构名称")
    @Size(max = 255, message = "机构名称最大长度需小于255")
    private String sysDeptName;

    /**
     * 部门负责人
     */
    @TableField(value = "sys_dept_principal")
    @Schema(description = "部门负责人")
    @Size(max = 255, message = "部门负责人最大长度需小于255")
    private String sysDeptPrincipal;

    /**
     * 负责人联系方式
     */
    @TableField(value = "sys_dept_contact")
    @Schema(description = "负责人联系方式")
    @Size(max = 255, message = "负责人联系方式最大长度需小于255")
    private String sysDeptContact;

    /**
     * 排序优先级
     */
    @TableField(value = "sys_dept_sort")
    @Schema(description = "排序优先级")
    private Integer sysDeptSort;

    /**
     * 机构编码
     */
    @TableField(value = "sys_dept_code")
    @Schema(description = "机构编码")
    @Size(max = 255, message = "机构编码最大长度需小于255")
    @NotBlank(message = "机构编码不能为空", groups = {AddGroup.class})
    private String sysDeptCode;

    /**
     * 父机构ID
     */
    @TableField(value = "sys_dept_pid")
    @Schema(description = "父机构ID")
    private Integer sysDeptPid;

    /**
     * 机构树路径(系统生成)
     */
    @TableField(value = "sys_dept_tree_id")
    @Schema(description = "机构树路径(系统生成)")
    @Size(max = 255, message = "机构树路径最大长度需小于255")
    private String sysDeptTreeId;

    /**
     * 状态: 是否禁用; 0:启用 1:禁用
     */
    @TableField(value = "sys_dept_status")
    @Schema(description = "状态: 是否禁用; 0:启用 1:禁用")
    private Integer sysDeptStatus;

    /**
     * 备注
     */
    @TableField(value = "sys_dept_remark")
    @Schema(description = "备注")
    private String sysDeptRemark;

    /**
     * 数据权限类型: 1:全部可见 2:本人可见 3:所在部门可见 4:所在部门及子级可见 5:选择的部门可见
     */
    @TableField(value = "sys_dept_data_permission")
    @Schema(description = "数据权限类型: 1:全部可见 2:本人可见 3:所在部门可见 4:所在部门及子级可见 5:选择的部门可见")
    private Integer sysDeptDataPermission;

    /**
     * 数据权限可见的部门ID列表(逗号分隔)
     */
    @TableField(value = "sys_dept_data_permission_dept_id")
    @Schema(description = "数据权限可见的部门ID列表(逗号分隔)")
    private String sysDeptDataPermissionDeptId;
}
