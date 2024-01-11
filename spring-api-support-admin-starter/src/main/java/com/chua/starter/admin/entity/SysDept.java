package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 部门表
 */
@ApiModel(description = "部门表")
@Schema(description = "部门表")
@Data
@TableName(value = "sys_dept")
public class SysDept extends SysTenantBase {
    /**
     * 主键
     */
    @TableId(value = "dept_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    @Schema(description = "主键")
    @NotNull(message = "主键不能为null")
    private Long deptId;

    /**
     * 部门名称
     */
    @TableField(value = "dept_name")
    @ApiModelProperty(value = "部门名称")
    @Schema(description = "部门名称")
    @Size(max = 255, message = "部门名称最大长度要小于 255")
    private String deptName;

    /**
     * 部门备注
     */
    @TableField(value = "dept_marker")
    @ApiModelProperty(value = "部门备注")
    @Schema(description = "部门备注")
    @Size(max = 255, message = "部门备注最大长度要小于 255")
    private String deptMarker;

    /**
     * 父节点id
     */
    @TableField(value = "dept_parent_id")
    @ApiModelProperty(value = "父节点id")
    @Schema(description = "父节点id")
    private Long deptParentId;

    /**
     * 父节点id路径
     */
    @TableField(value = "dept_tree_path")
    @ApiModelProperty(value = "父节点id路径")
    @Schema(description = "父节点id路径")
    @Size(max = 255, message = "父节点id路径最大长度要小于 255")
    private String deptTreePath;

    /**
     * 显示顺序
     */
    @TableField(value = "dept_sort")
    @ApiModelProperty(value = "显示顺序")
    @Schema(description = "显示顺序")
    private Integer deptSort;

    /**
     * 状态(1:正常;0:禁用)
     */
    @TableField(value = "dept_status")
    @ApiModelProperty(value = "状态(1:正常;0:禁用)")
    @Schema(description = "状态(1:正常;0:禁用)")
    private Integer deptStatus;

}