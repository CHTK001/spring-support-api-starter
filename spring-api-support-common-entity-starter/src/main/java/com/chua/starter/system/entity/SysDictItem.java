package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.SelectGroup;
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
import java.util.List;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */

/**
 * 字典项子项
 */
@ApiModel(description = "字典项子项")
@Schema(description = "字典项子项")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dict_item")
public class SysDictItem extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "sys_dict_item_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysDictItemId;

    /**
     * 字典ID
     */
    @TableField(value = "sys_dict_id")
    @ApiModelProperty(value = "字典ID")
    @Schema(description = "字典ID")
    @NotNull(message = "字典ID不能为null", groups = {UpdateGroup.class, AddGroup.class, SelectGroup.class})
    private Integer sysDictId;
    /**
     * 字典ID
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "字典code")
    @Schema(description = "字典code")
    private String sysDictCode;

    /**
     * 字典子项名称
     */
    @TableField(value = "sys_dict_item_name")
    @ApiModelProperty(value = "字典子项名称")
    @Schema(description = "字典子项名称")
    @Size(max = 255, message = "字典子项名称最大长度要小于 255")
    private String sysDictItemName;


    /**
     * 字典子项图标
     */
    @TableField(value = "sys_dict_item_icon")
    @ApiModelProperty(value = "字典子项图标")
    @Schema(description = "字典子项图标")
    private String sysDictItemIcon;
    /**
     * 字典i18n
     */
    @TableField(value = "sys_dict_item_i18n")
    @ApiModelProperty(value = "字典i18n, 唯一")
    @Schema(description = "字典i18n, 唯一")
    @Size(max = 255, message = "字典i18n, 唯一最大长度要小于 255")
    private String sysDictItemI18n;

    /**
     * 字典子项名称
     */
    @TableField(value = "sys_dict_item_code")
    @ApiModelProperty(value = "字典子项编码")
    @Schema(description = "字典子项编码")
    @NotNull(message = "字典子项编码不能为null", groups = {AddGroup.class})
    @Size(max = 255, message = "字典子项编码最大长度要小于 255")
    private String sysDictItemCode;

    /**
     * 描述
     */
    @TableField(value = "sys_dict_item_remark")
    @ApiModelProperty(value = "描述")
    @Schema(description = "描述")
    @Size(max = 255, message = "描述最大长度要小于 255")
    private String sysDictItemRemark;

    /**
     * 状态;0: 隐藏
     */
    @TableField(value = "sys_dict_item_status")
    @ApiModelProperty(value = "状态;0: 隐藏")
    @Schema(description = "状态;0: 隐藏")
    private Integer sysDictItemStatus;


    /**
     * 是否删除0:未删除
     */
    @TableField(value = "sys_dict_item_delete")
    @ApiModelProperty(value = "是否删除0:未删除")
    @Schema(description = "是否删除0:未删除")
    @TableLogic(value = "0", delval = "1")
    private Integer sysDictItemDelete;
    /**
     * 优先级
     */
    @TableField(value = "sys_dict_item_sort")
    @ApiModelProperty(value = "优先级")
    @Schema(description = "优先级")
    private Integer sysDictItemSort;

    /**
     * 可配置项
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "可配置项")
    @Schema(description = "可配置项")
    private List<SysDictItemProperty> property;
}