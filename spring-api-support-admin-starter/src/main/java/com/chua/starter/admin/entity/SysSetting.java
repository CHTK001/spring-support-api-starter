package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 系统设置
 */
@ApiModel(description="系统设置")
@Schema(description="系统设置")
@Data
@TableName(value = "sys_setting")
public class SysSetting {
    @TableId(value = "setting_id", type = IdType.AUTO)
    @ApiModelProperty(value="")
    @Schema(description="")
    @NotNull(message = "不能为null")
    private Integer settingId;

    /**
     * 设置名称
     */
    @TableField(value = "setting_name")
    @ApiModelProperty(value="设置名称")
    @Schema(description="设置名称")
    @Size(max = 255,message = "设置名称最大长度要小于 255", groups = AddGroup.class)
    private String settingName;

    /**
     * 设置值
     */
    @TableField(value = "setting_value")
    @ApiModelProperty(value="设置值")
    @Schema(description="设置值")
    @Size(max = 255,message = "设置值最大长度要小于 255")
    private String settingValue;

    /**
     * 设置描述
     */
    @TableField(value = "setting_desc")
    @ApiModelProperty(value="设置描述")
    @Schema(description="设置描述")
    @Size(max = 255,message = "设置描述最大长度要小于 255")
    private String settingDesc;

    /**
     * 是否开启; 0:不开启
     */
    @TableField(value = "setting_enable")
    @ApiModelProperty(value="是否开启; 0:不开启")
    @Schema(description="是否开启; 0:不开启")
    private Integer settingEnable;

    /**
     * 设置版本
     */
    @TableField(value = "setting_version")
    @ApiModelProperty(value="设置版本")
    @Schema(description="设置版本")
    @Size(max = 255,message = "设置版本最大长度要小于 255")
    private String settingVersion;
}