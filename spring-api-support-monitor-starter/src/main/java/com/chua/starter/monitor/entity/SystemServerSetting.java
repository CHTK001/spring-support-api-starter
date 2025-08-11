package com.chua.starter.monitor.entity;

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
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统服务器配置表实体类
 *
 * @author CH
 * @since 2025/01/07
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "系统服务器配置表")
@Schema(description = "系统服务器配置表")
@Data
@TableName(value = "system_server_setting")
public class SystemServerSetting extends SysBase {

    /**
     * 系统服务器配置ID
     */
    @TableId(value = "system_server_setting_id", type = IdType.AUTO)
    @ApiModelProperty(value = "系统服务器配置ID")
    @Schema(description = "系统服务器配置ID")
    @NotNull(message = "系统服务器配置ID不能为空", groups = {UpdateGroup.class})
    private Integer systemServerSettingId;

    /**
     * 系统服务器配置名称
     */
    @TableField(value = "system_server_setting_name")
    @ApiModelProperty(value = "系统服务器配置名称")
    @Schema(description = "系统服务器配置名称")
    @NotBlank(message = "系统服务器配置名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 255, message = "系统服务器配置名称最大长度要小于 255")
    private String systemServerSettingName;

    /**
     * 系统服务器配置类型 (SPI使用)
     */
    @TableField(value = "system_server_setting_type")
    @ApiModelProperty(value = "系统服务器配置类型")
    @Schema(description = "系统服务器配置类型")
    @NotBlank(message = "系统服务器配置类型不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "系统服务器配置类型最大长度要小于 100")
    private String systemServerSettingType;

    /**
     * 关联的服务器ID (外键)
     */
    @TableField(value = "system_server_setting_server_id")
    @ApiModelProperty(value = "关联的服务器ID")
    @Schema(description = "关联的服务器ID")
    @NotNull(message = "关联的服务器ID不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private Integer systemServerSettingServerId;

    /**
     * 排序字段 (用于拖拽排序)
     */
    @TableField(value = "system_server_setting_order")
    @ApiModelProperty(value = "排序字段")
    @Schema(description = "排序字段")
    private Integer systemServerSettingOrder;

    /**
     * 配置描述
     */
    @TableField(value = "system_server_setting_description")
    @ApiModelProperty(value = "配置描述")
    @Schema(description = "配置描述")
    @Size(max = 500, message = "配置描述最大长度要小于 500")
    private String systemServerSettingDescription;

    /**
     * 配置状态 0:停用 1:启用
     */
    @TableField(value = "system_server_setting_enabled")
    @ApiModelProperty(value = "配置状态")
    @Schema(description = "配置状态")
    private Boolean systemServerSettingEnabled;

    /**
     * 配置类名 (用于SPI机制)
     */
    @TableField(value = "system_server_setting_class_name")
    @ApiModelProperty(value = "配置类名")
    @Schema(description = "配置类名")
    @Size(max = 500, message = "配置类名最大长度要小于 500")
    private String systemServerSettingClassName;

    /**
     * 配置版本
     */
    @TableField(value = "system_server_setting_version")
    @ApiModelProperty(value = "配置版本")
    @Schema(description = "配置版本")
    @Size(max = 50, message = "配置版本最大长度要小于 50")
    private String systemServerSettingVersion;

    /**
     * 配置JSON数据
     */
    @TableField(value = "system_server_setting_config")
    @ApiModelProperty(value = "配置JSON数据")
    @Schema(description = "配置JSON数据")
    private String systemServerSettingConfig;
    /**
     * 启动的时候生成
     */
    @TableField(value = "system_server_setting_filter_id")
    @ApiModelProperty(value = "启动的时候生成")
    @Schema(description = "启动的时候生成")
    private String systemServerSettingFilterId;
}
