package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 部门表
 */
@ApiModel(description = "部门表")
@Schema(description = "部门表")
@Data
@TableName(value = "sys_dept")
public class SysDept {
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

    /**
     * 创建人
     */
    @TableField(value = "create_name")
    @ApiModelProperty(value = "创建人")
    @Schema(description = "创建人")
    @Size(max = 255, message = "创建人最大长度要小于 255")
    private String createName;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    @ApiModelProperty(value = "创建人")
    @Schema(description = "创建人")
    @Size(max = 255, message = "创建人最大长度要小于 255")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 更新人
     */
    @TableField(value = "update_name")
    @ApiModelProperty(value = "更新人")
    @Schema(description = "更新人")
    @Size(max = 255, message = "更新人最大长度要小于 255")
    private String updateName;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    @ApiModelProperty(value = "更新人")
    @Schema(description = "更新人")
    @Size(max = 255, message = "更新人最大长度要小于 255")
    private String updateBy;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    @ApiModelProperty(value = "租户ID")
    @Schema(description = "租户ID")
    private Integer tenantId;
}