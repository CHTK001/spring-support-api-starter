package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.SelectGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 字典项子项表
 *
 * @author CH
 * @since 2024/8/7
 */
@Schema(description = "字典项子项表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dict_item")
public class SysDictItem extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字典项ID
     */
    @TableId(value = "sys_dict_item_id", type = IdType.AUTO)
    @Schema(description = "字典项ID")
    @NotNull(message = "字典项ID不能为空")
    private Integer sysDictItemId;

    /**
     * 字典ID
     */
    @TableField(value = "sys_dict_id")
    @Schema(description = "字典ID")
    @NotNull(message = "字典ID不能为空", groups = {UpdateGroup.class, AddGroup.class, SelectGroup.class})
    private Integer sysDictId;

    /**
     * 字典编码(非数据库字段)
     */
    @TableField(exist = false)
    @Schema(description = "字典编码")
    private String sysDictCode;

    /**
     * 字典子项名称
     */
    @TableField(value = "sys_dict_item_name")
    @Schema(description = "字典子项名称")
    @Size(max = 255, message = "字典子项名称最大长度需小于255")
    private String sysDictItemName;

    /**
     * 字典子项图标
     */
    @TableField(value = "sys_dict_item_icon")
    @Schema(description = "字典子项图标")
    private String sysDictItemIcon;

    /**
     * 字典项国际化key
     */
    @TableField(value = "sys_dict_item_i18n")
    @Schema(description = "字典项国际化key")
    @Size(max = 255, message = "字典项国际化key最大长度需小于255")
    private String sysDictItemI18n;

    /**
     * 字典子项编码
     */
    @TableField(value = "sys_dict_item_code")
    @Schema(description = "字典子项编码")
    @NotNull(message = "字典子项编码不能为空", groups = {AddGroup.class})
    @Size(max = 255, message = "字典子项编码最大长度需小于255")
    private String sysDictItemCode;

    /**
     * 描述
     */
    @TableField(value = "sys_dict_item_remark")
    @Schema(description = "描述")
    @Size(max = 255, message = "描述最大长度需小于255")
    private String sysDictItemRemark;

    /**
     * 状态: 0:隐藏 1:显示
     */
    @TableField(value = "sys_dict_item_status")
    @Schema(description = "状态: 0:隐藏 1:显示")
    private Integer sysDictItemStatus;

    /**
     * 是否删除: 0:未删除 1:已删除
     */
    @TableField(value = "sys_dict_item_delete")
    @Schema(description = "是否删除: 0:未删除 1:已删除")
    @TableLogic(value = "0", delval = "1")
    private Integer sysDictItemDelete;

    /**
     * 排序优先级
     */
    @TableField(value = "sys_dict_item_sort")
    @Schema(description = "排序优先级")
    private Integer sysDictItemSort;

    /**
     * 可配置项(非数据库字段)
     */
    @TableField(exist = false)
    @Schema(description = "可配置项")
    private List<SysDictItemProperty> property;
}
