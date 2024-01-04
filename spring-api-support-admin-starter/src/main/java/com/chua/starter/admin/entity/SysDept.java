package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chua.common.support.datasource.annotation.Column;
import com.chua.common.support.datasource.annotation.Table;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "部门")
@Table(comment = "部门表")
public class SysDept extends SysBase {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long deptId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 部门备注
     */
    @Schema(description = "部门备注")
    private String deptMarker;

    /**
     * 父节点id
     */
    @Schema(description = "父节点id")
    private Long deptParentId;

    /**
     * 父节点id路径
     */
    @Schema(description = "父节点id路径")
    private String deptTreePath;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    private Integer deptSort;

    /**
     * 状态(1:正常;0:禁用)
     */
    @Schema(description = "状态(1:正常;0:禁用)")
    @Column(defaultValue = "1", comment = "状态(1:正常;0:禁用)")
    private Integer deptStatus;

}