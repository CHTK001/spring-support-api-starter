package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ai模型分类表
 */
@ApiModel(description = "ai模型分类表")
@Schema(description = "ai模型分类表")
@Data
@TableName(value = "sys_ai_module")
public class SysAiModule extends SysBase {
    @TableId(value = "sys_ai_module_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Integer sysAiModuleId;

    /**
     * 模型名称
     */
    @TableField(value = "sys_ai_module_name")
    @ApiModelProperty(value = "模型名称")
    @Schema(description = "模型名称")
    private String sysAiModuleName;

    /**
     * 是否存在角色设置
     */
    @TableField(value = "sys_ai_module_role_setting")
    @ApiModelProperty(value = "是否存在角色设置;0:不存在")
    @Schema(description = "是否存在角色设置;0:不存在")
    private Integer sysAiModuleRoleSetting;

    /**
     * 模型编码
     */
    @TableField(value = "sys_ai_module_code")
    @ApiModelProperty(value = "模型编码")
    @Schema(description = "模型编码")
    @NotBlank(message = "模型编码不能为空", groups = {AddGroup.class})
    private String sysAiModuleCode;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @ApiModelProperty(value = "项目ID")
    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空", groups = {AddGroup.class})
    private Integer sysProjectId;


    /**
     * 厂商(字典表1)
     */
    @TableField(value = "sys_api_module_manufacturers")
    @ApiModelProperty(value = "厂商(字典表1)")
    @Schema(description = "厂商(字典表1)")
    @NotNull(message = "厂商不能为空", groups = {AddGroup.class})
    private Integer sysApiModuleManufacturers;
    /**
     * 厂商(字典表1)
     */
    @TableField(exist = false)
    private String sysApiModuleManufacturersLabel;
    /**
     * 是否禁用;0:禁用, 1:可用
     */
    @TableField(value = "sys_ai_module_status")
    @ApiModelProperty(value = "是否禁用;0:禁用, 1:可用")
    @Schema(description = "是否禁用;0:禁用, 1:可用")
    private Integer sysAiModuleStatus;
    /**
     * 排序
     */
    @TableField(value = "sys_ai_module_sort")
    @ApiModelProperty(value = "排序")
    @Schema(description = "排序")
    private Integer sysAiModuleSort;


    /**
     * 版本
     */
    @TableField(value = "sys_ai_module_version")
    @ApiModelProperty(value = "版本")
    @Schema(description = "版本")
    private String sysAiModuleVersion;

    /**
     * 说明
     */
    @TableField(value = "sys_ai_module_remark")
    @ApiModelProperty(value = "说明")
    @Schema(description = "说明")
    private String sysAiModuleRemark;

}