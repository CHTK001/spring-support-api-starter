package com.chua.starter.entity.project;

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
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ai模型分类表
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "ai模型分类表")
@Data
@TableName(value = "sys_ai_module")
public class SysAiModule extends SysBase {
    @TableId(value = "sys_ai_module_id", type = IdType.AUTO)
    @Schema(description = "")
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Integer sysAiModuleId;

    /**
     * 模型名称
     */
    @TableField(value = "sys_ai_module_name")
    @Schema(description = "模型名称")
    private String sysAiModuleName;

    /**
     * 是否存在角色设置
     */
    @TableField(value = "sys_ai_module_role_setting")
    @Schema(description = "是否存在角色设置;0:不存在")
    private Integer sysAiModuleRoleSetting;

    /**
     * 模型编码
     */
    @TableField(value = "sys_ai_module_code")
    @Schema(description = "模型编码")
    @NotBlank(message = "模型编码不能为空", groups = {AddGroup.class})
    private String sysAiModuleCode;
    /**
     * 模型类型
     */
    @TableField(value = "sys_ai_module_type")
    @Schema(description = "模型类型")
    @NotBlank(message = "模型类型不能为空", groups = {AddGroup.class})
    private String sysAiModuleType;
    /**
     * 模型地址
     */
    @TableField(value = "sys_ai_module_url")
    @Schema(description = "模型地址")
    private String sysAiModuleUrl;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空", groups = {AddGroup.class})
    private Integer sysProjectId;
    /**
     * 模型版本
     */
    @TableField(value = "sys_ai_module_vlm")
    @Schema(description = "是否是vlm模型;0:否")
    private Integer sysAiModuleVlm;
    /**
     * 是否本地模型
     */
    @TableField(value = "sys_ai_module_location")
    @Schema(description = "是否本地模型")
    private Boolean sysAiModuleLocation;
    /**
     * 项目名称
     */
    @TableField(exist = false)
    @Schema(description = "项目名称")
    private String sysProjectName;
    /**
     * 项目图标
     */
    @TableField(exist = false)
    @Schema(description = "项目图标")
    private String sysProjectIcon;


    /**
     * 厂商(字典表1)
     */
    @TableField(value = "sys_ai_module_manufacturers")
    @Schema(description = "厂商(字典表1)")
    @NotNull(message = "厂商不能为空", groups = {AddGroup.class})
    private Integer sysAiModuleManufacturers;
    /**
     * 厂商(字典表1)
     */
    @TableField(exist = false)
    private String sysApiModuleManufacturersLabel;
    /**
     * 是否禁用;0:禁用, 1:可用
     */
    @TableField(value = "sys_ai_module_status")
    @Schema(description = "是否禁用;0:禁用, 1:可用")
    private Integer sysAiModuleStatus;
    /**
     * 排序
     */
    @TableField(value = "sys_ai_module_sort")
    @Schema(description = "排序")
    private Integer sysAiModuleSort;


    /**
     * 版本
     */
    @TableField(value = "sys_ai_module_version")
    @Schema(description = "版本")
    private String sysAiModuleVersion;

    /**
     * 说明
     */
    @TableField(value = "sys_ai_module_remark")
    @Schema(description = "说明")
    private String sysAiModuleRemark;
    /**
     * 是否支持异步任务;0:不支持
     */
    @TableField(exist = false)
    @Schema(description = "是否支持异步任务;0:不支持")
    private Integer sysAiVincentSupportAsync;

    /**
     * 文生图模型配置
     */
    @TableField(exist = false)
    @Schema(description = "文生图模型配置")
    private SysAiVincentSetting vincentSetting;
}
