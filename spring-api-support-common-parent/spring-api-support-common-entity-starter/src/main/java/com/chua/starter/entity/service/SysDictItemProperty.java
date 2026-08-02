package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字典配置项表
 *
 * @author CH
 * @since 2024/8/7
 */
@Schema(description = "字典配置项表")
@Data
@TableName(value = "sys_dict_item_property")
public class SysDictItemProperty {

    /**
     * 配置项ID
     */
    @TableId(value = "sys_dict_item_property_id", type = IdType.AUTO)
    @Schema(description = "配置项ID")
    private Integer sysDictItemPropertyId;

    /**
     * 字典子项ID
     */
    @TableField(value = "sys_dict_item_id")
    @Schema(description = "字典子项ID")
    private Integer sysDictItemId;

    /**
     * 配置项分组
     */
    @TableField(value = "sys_dict_item_property_group")
    @Schema(description = "配置项分组")
    private String sysDictItemPropertyGroup;

    /**
     * 配置项名称
     */
    @TableField(value = "sys_dict_item_property_name")
    @Schema(description = "配置项名称")
    private String sysDictItemPropertyName;

    /**
     * 配置项值
     */
    @TableField(value = "sys_dict_item_property_value")
    @Schema(description = "配置项值")
    private String sysDictItemPropertyValue;

    /**
     * 选中类型
     */
    @TableField(value = "sys_dict_item_property_selected_type")
    @Schema(description = "选中类型")
    private String sysDictItemPropertySelectedType;
}
