package com.chua.starter.system.entity;

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
 *
 * @since 2024/8/9
 * @author CH    
 */

/**
 * 系统设置
 */
@ApiModel(description = "系统设置")
@Schema(description = "系统设置")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_setting")
public class SysSetting extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "sys_setting_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "配置ID不能为null", groups = {UpdateGroup.class})
    private Integer sysSettingId;

    /**
     * 配置名称
     */
    @TableField(value = "sys_setting_name")
    @ApiModelProperty(value = "配置名称")
    @Schema(description = "配置名称")
    @Size(max = 255, message = "配置名称最大长度要小于 255")
    @NotNull(message = "配置名称不能为null", groups = {AddGroup.class})
    private String sysSettingName;

    /**
     * 配置�?
     */
    @TableField(value = "sys_setting_value")
    @ApiModelProperty(value = "配置�?)
    @Schema(description = "配置�?)
    @Size(max = 255, message = "配置值最大长度要小于 255")
    private String sysSettingValue;
    /**
     * 配置参数
     */
    @TableField(value = "sys_setting_config")
    @ApiModelProperty(value = "配置参数")
    @Schema(description = "配置参数")
    @Size(max = 255, message = "配置参数最大长度要小于 255")
    private String sysSettingConfig;
    /**
     * 配置备注
     */
    @TableField(value = "sys_setting_remark")
    @ApiModelProperty(value = "配置备注")
    @Schema(description = "配置备注")
    @Size(max = 255, message = "配置备注最大长度要小于 255")
    private String sysSettingRemark;

    /**
     * 优先�?
     */
    @TableField(value = "sys_setting_sort")
    @ApiModelProperty(value = "优先�?)
    @Schema(description = "优先�?)
    private Integer sysSettingSort;
    /**
     * 是否启用; 0:不启�?
     */
    @TableField(value = "sys_setting_version")
    @ApiModelProperty(value = "版本")
    @Schema(description = "版本")
    @Version
    private Integer sysSettingVersion;
    /**
     * 是否启用; 0:不启�?
     */
    @TableField(value = "sys_setting_status")
    @ApiModelProperty(value = "是否启用; 0:不启�?)
    @Schema(description = "是否启用; 0:不启�?)
    private Integer sysSettingStatus;

    /**
     * 是否只有管理员生�?
     */
    @TableField(value = "sys_setting_admin")
    @ApiModelProperty(value = "是否只有管理员生�? 0:不生�?)
    @Schema(description = "是否只有管理员生�? 0:不生�?)
    private Integer sysSettingAdmin;

    /**
     * 配置类型; 用于前端组件判断; e.g.BOOL：布�?
     */
    @TableField(value = "sys_setting_value_type")
    @ApiModelProperty(value = "配置类型; 用于前端组件判断; e.g.BOOL：布�?)
    @Schema(description = "配置类型; 用于前端组件判断; e.g.BOOL：布�?)
    @Size(max = 255, message = "配置类型; 用于前端组件判断; e.g.BOOL：布尔最大长度要小于 255")
    private String sysSettingValueType;

    /**
     * 配置所属分�?
     */
    @TableField(value = "sys_setting_group")
    @ApiModelProperty(value = "配置所属分�?)
    @Schema(description = "配置所属分�?)
    @Size(max = 255, message = "配置所属分组最大长度要小于 255")
    @NotNull(message = "配置名称不能为null", groups = {AddGroup.class})
    private String sysSettingGroup;

    /**
     * 配置所属来�?
     */
    @TableField(value = "sys_setting_collect")
    @ApiModelProperty(value = "是否是多个配置集�?0:单个")
    @Schema(description = "是否是多个配置集�?0:单个")
    @Size(max = 255, message = "是否是多个配置集�?0:单个最大长度要小于 255")
    private Integer sysSettingCollect;

    /**
     * 是否系统配置; 0:非系统配�?
     */
    @TableField(value = "sys_setting_in_system")
    @ApiModelProperty(value = "是否系统配置; 0:非系统配�?)
    @Schema(description = "是否系统配置; 0:非系统配�?)
    private Integer sysSettingInSystem;

    /**
     * 配置是否作用域系统启�?
     */
    @TableField(value = "sys_setting_app_inner")
    @ApiModelProperty(value = "配置是否作用域系统启�?)
    @Schema(description = "配置是否作用域系统启�?)
    private Integer sysSettingAppInner;
}