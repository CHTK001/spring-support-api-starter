package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
     * 字典子项名称
     */
    @TableField(value = "sys_dict_item_name")
    @ApiModelProperty(value = "字典子项名称")
    @Schema(description = "字典子项名称")
    @Size(max = 255, message = "字典子项名称最大长度要小于 255")
    private String sysDictItemName;


    /**
     * 字典子项名称
     */
    @TableField(value = "sys_dict_item_codec")
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
}