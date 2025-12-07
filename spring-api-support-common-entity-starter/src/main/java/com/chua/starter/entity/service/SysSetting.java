package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 系统设置表
 *
 * @author CH
 * @since 2024/8/9
 */
@ApiModel(description = "系统设置表")
@Schema(description = "系统设置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_setting")
public class SysSetting extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(value = "sys_setting_id", type = IdType.AUTO)
    @ApiModelProperty(value = "配置ID")
    @Schema(description = "配置ID")
    @NotNull(message = "配置ID不能为空", groups = {UpdateGroup.class})
    private Integer sysSettingId;

    /**
     * 配置名称
     */
    @TableField(value = "sys_setting_name")
    @ApiModelProperty(value = "配置名称")
    @Schema(description = "配置名称")
    @Size(max = 255, message = "配置名称最大长度需小于255")
    @NotNull(message = "配置名称不能为空", groups = {AddGroup.class})
    private String sysSettingName;

    /**
     * 配置值
     */
    @TableField(value = "sys_setting_value")
    @ApiModelProperty(value = "配置值")
    @Schema(description = "配置值")
    @Size(max = 255, message = "配置值最大长度需小于255")
    private String sysSettingValue;

    /**
     * 配置参数
     */
    @TableField(value = "sys_setting_config")
    @ApiModelProperty(value = "配置参数")
    @Schema(description = "配置参数")
    @Size(max = 255, message = "配置参数最大长度需小于255")
    private String sysSettingConfig;

    /**
     * 配置备注
     */
    @TableField(value = "sys_setting_remark")
    @ApiModelProperty(value = "配置备注")
    @Schema(description = "配置备注")
    @Size(max = 255, message = "配置备注最大长度需小于255")
    private String sysSettingRemark;

    /**
     * 排序优先级
     */
    @TableField(value = "sys_setting_sort")
    @ApiModelProperty(value = "排序优先级")
    @Schema(description = "排序优先级")
    private Integer sysSettingSort;

    /**
     * 版本号(乐观锁)
     */
    @TableField(value = "sys_setting_version")
    @ApiModelProperty(value = "版本号")
    @Schema(description = "版本号")
    @Version
    private Integer sysSettingVersion;

    /**
     * 是否启用: 0:不启用 1:启用
     */
    @TableField(value = "sys_setting_status")
    @ApiModelProperty(value = "是否启用: 0:不启用 1:启用")
    @Schema(description = "是否启用: 0:不启用 1:启用")
    private Integer sysSettingStatus;

    /**
     * 是否仅管理员生效: 0:否 1:是
     */
    @TableField(value = "sys_setting_admin")
    @ApiModelProperty(value = "是否仅管理员生效: 0:否 1:是")
    @Schema(description = "是否仅管理员生效: 0:否 1:是")
    private Integer sysSettingAdmin;

    /**
     * 配置值类型(前端组件判断用): BOOL-布尔 TEXT-文本 等
     */
    @TableField(value = "sys_setting_value_type")
    @ApiModelProperty(value = "配置值类型(前端组件判断用)")
    @Schema(description = "配置值类型(前端组件判断用): BOOL-布尔 TEXT-文本 等")
    @Size(max = 255, message = "配置值类型最大长度需小于255")
    private String sysSettingValueType;

    /**
     * 配置所属分组
     */
    @TableField(value = "sys_setting_group")
    @ApiModelProperty(value = "配置所属分组")
    @Schema(description = "配置所属分组")
    @Size(max = 255, message = "配置所属分组最大长度需小于255")
    @NotNull(message = "配置所属分组不能为空", groups = {AddGroup.class})
    private String sysSettingGroup;

    /**
     * 是否是配置集合: 0:单个 1:集合
     */
    @TableField(value = "sys_setting_collect")
    @ApiModelProperty(value = "是否是配置集合: 0:单个 1:集合")
    @Schema(description = "是否是配置集合: 0:单个 1:集合")
    private Integer sysSettingCollect;

    /**
     * 是否系统配置: 0:否 1:是
     */
    @TableField(value = "sys_setting_in_system")
    @ApiModelProperty(value = "是否系统配置: 0:否 1:是")
    @Schema(description = "是否系统配置: 0:否 1:是")
    private Integer sysSettingInSystem;

    /**
     * 是否作用于系统启动: 0:否 1:是
     */
    @TableField(value = "sys_setting_app_inner")
    @ApiModelProperty(value = "是否作用于系统启动: 0:否 1:是")
    @Schema(description = "是否作用于系统启动: 0:否 1:是")
    private Integer sysSettingAppInner;
}
