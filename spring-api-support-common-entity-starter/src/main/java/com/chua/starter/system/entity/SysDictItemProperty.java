package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字典配置项
 */
@ApiModel(description = "字典配置项")
@Schema(description = "字典配置项")
@Data
@TableName(value = "sys_dict_item_property")
public class SysDictItemProperty {
    @TableId(value = "sys_dict_item_property_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysDictItemPropertyId;
    /**
     * 字典子项ID
     */
    @TableField(value = "sys_dict_item_id")
    @ApiModelProperty(value = "字典子项ID")
    @Schema(description = "字典子项ID")
    private Integer sysDictItemId;

    /**
     * 配置项分组
     */
    @TableField(value = "sys_dict_item_property_group")
    @ApiModelProperty(value = "配置项分组")
    @Schema(description = "配置项分组")
    private String sysDictItemPropertyGroup;

    /**
     * 配置项
     */
    @TableField(value = "sys_dict_item_property_name")
    @ApiModelProperty(value = "配置项")
    @Schema(description = "配置项")
    private String sysDictItemPropertyName;

    /**
     * 配置值
     */
    @TableField(value = "sys_dict_item_property_value")
    @ApiModelProperty(value = "配置值")
    @Schema(description = "配置值")
    private String sysDictItemPropertyValue;
    /**
     * 选中类型
     */
    @TableField(value = "sys_dict_item_property_selected_type")
    @ApiModelProperty(value = "选中类型")
    @Schema(description = "选中类型")
    private String sysDictItemPropertySelectedType;
}