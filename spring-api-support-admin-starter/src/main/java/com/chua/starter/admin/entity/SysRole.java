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
 * 角色表
 */
@ApiModel(description = "角色表")
@Schema(description = "角色表")
@Data
@TableName(value = "sys_role")
public class SysRole extends SysTenantBase {
    /**
     * 主键
     */
    @TableId(value = "role_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    @Schema(description = "主键")
    @NotNull(message = "主键不能为null")
    private Long roleId;

    /**
     * 角色名称
     */
    @TableField(value = "role_name")
    @ApiModelProperty(value = "角色名称")
    @Schema(description = "角色名称")
    @Size(max = 255, message = "角色名称最大长度要小于 255")
    private String roleName;

    /**
     * 角色编码
     */
    @TableField(value = "role_code")
    @ApiModelProperty(value = "角色编码")
    @Schema(description = "角色编码")
    @Size(max = 255, message = "角色编码最大长度要小于 255")
    private String roleCode;

    /**
     * 显示顺序
     */
    @TableField(value = "role_sort")
    @ApiModelProperty(value = "显示顺序")
    @Schema(description = "显示顺序")
    private Integer roleSort;

    /**
     * 角色状态(1-正常；0-停用)
     */
    @TableField(value = "role_status")
    @ApiModelProperty(value = "角色状态(1-正常；0-停用)")
    @Schema(description = "角色状态(1-正常；0-停用)")
    private Integer roleStatus;

    /**
     * 是否系统角色; 0:非系统角色
     */
    @TableField(value = "role_sys")
    @ApiModelProperty(value = "是否系统角色; 0:非系统角色")
    @Schema(description = "是否系统角色; 0:非系统角色")
    private Integer roleSys;

    /**
     * 数据权限; 1: 全部可见<br/>2: 本人可见<br/>3: 所在部门可见<br/>4: 所在部门及子级可见<br/>5: 选择的部门可见<br/>6: 自定义<br/>
     */
    @TableField(value = "role_data_scope")
    @ApiModelProperty(value = "数据权限; 1: 全部可见<br/>2: 本人可见<br/>3: 所在部门可见<br/>4: 所在部门及子级可见<br/>5: 选择的部门可见<br/>6: 自定义<br/>")
    @Schema(description = "数据权限; 1: 全部可见<br/>2: 本人可见<br/>3: 所在部门可见<br/>4: 所在部门及子级可见<br/>5: 选择的部门可见<br/>6: 自定义<br/>")
    @Size(max = 255, message = "数据权限; 1: 全部可见<br/>2: 本人可见<br/>3: 所在部门可见<br/>4: 所在部门及子级可见<br/>5: 选择的部门可见<br/>6: 自定义<br/>最大长度要小于 255")
    private String roleDataScope;

    @TableField(value = "role_data_rule")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @Size(max = 1000, message = "最大长度要小于 1000")
    private String roleDataRule;

    /**
     * 控制台样式
     */
    @TableField(value = "role_dashboard")
    @ApiModelProperty(value = "控制台样式")
    @Schema(description = "控制台样式")
    @Size(max = 255, message = "控制台样式最大长度要小于 255")
    private String roleDashboard;

    /**
     * 组件
     */
    @TableField(value = "role_grid")
    @ApiModelProperty(value = "组件")
    @Schema(description = "组件")
    @Size(max = 255, message = "组件最大长度要小于 255")
    private String roleGrid;

}