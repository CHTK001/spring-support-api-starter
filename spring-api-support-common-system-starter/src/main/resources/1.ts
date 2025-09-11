package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author CH
 * @since 2025/9/10 16:40
 */

/**
 * 配置组
 */
@ApiModel(description = "配置组")
@Schema(description = "配置组")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_setting_group")
public class SysSettingGroup extends SysBase {
    @TableId(value = "sys_setting_group_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysSettingGroupId;

    /**
     * 名称
     */
    @TableField(value = "sys_setting_group_name")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    private String sysSettingGroupName;

    /**
     * 唯一编码
     */
    @TableField(value = "sys_setting_group_code")
    @ApiModelProperty(value = "唯一编码")
    @Schema(description = "唯一编码")
    private String sysSettingGroupCode;

    /**
     * 图标
     */
    @TableField(value = "sys_setting_group_icon")
    @ApiModelProperty(value = "图标")
    @Schema(description = "图标")
    private String sysSettingGroupIcon;

    /**
     * 是否启用
     */
    @TableField(value = "sys_setting_group_enable")
    @ApiModelProperty(value = "是否启用")
    @Schema(description = "是否启用")
    private Boolean sysSettingGroupEnable;

    /**
     * 描述
     */
    @TableField(value = "sys_setting_group_remark")
    @ApiModelProperty(value = "描述")
    @Schema(description = "描述")
    private String sysSettingGroupRemark;
}