package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置组表
 *
 * @author CH
 * @since 2025/9/10
 */
@ApiModel(description = "配置组表")
@Schema(description = "配置组表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_setting_group")
public class SysSettingGroup extends SysBase {

    /**
     * 配置组ID
     */
    @TableId(value = "sys_setting_group_id", type = IdType.AUTO)
    @ApiModelProperty(value = "配置组ID")
    @Schema(description = "配置组ID")
    private Integer sysSettingGroupId;

    /**
     * 配置组名称
     */
    @TableField(value = "sys_setting_group_name")
    @ApiModelProperty(value = "配置组名称")
    @Schema(description = "配置组名称")
    private String sysSettingGroupName;

    /**
     * 配置组唯一编码
     */
    @TableField(value = "sys_setting_group_code")
    @ApiModelProperty(value = "配置组唯一编码")
    @Schema(description = "配置组唯一编码")
    private String sysSettingGroupCode;

    /**
     * 配置组图标
     */
    @TableField(value = "sys_setting_group_icon")
    @ApiModelProperty(value = "配置组图标")
    @Schema(description = "配置组图标")
    private String sysSettingGroupIcon;

    /**
     * 是否启用
     */
    @TableField(value = "sys_setting_group_enable")
    @ApiModelProperty(value = "是否启用")
    @Schema(description = "是否启用")
    private Boolean sysSettingGroupEnable;

    /**
     * 配置组描述
     */
    @TableField(value = "sys_setting_group_remark")
    @ApiModelProperty(value = "配置组描述")
    @Schema(description = "配置组描述")
    private String sysSettingGroupRemark;

    /**
     * 排序
     */
    @TableField(value = "sys_setting_group_sort")
    @ApiModelProperty(value = "排序")
    @Schema(description = "排序")
    private Integer sysSettingGroupSort;

    /**
     * 是否使用项目组接口
     */
    @TableField(value = "sys_setting_group_use_project_interface")
    @ApiModelProperty(value = "是否使用项目组接口")
    @Schema(description = "是否使用项目组接口")
    private Boolean sysSettingGroupUseProjectInterface;
}
