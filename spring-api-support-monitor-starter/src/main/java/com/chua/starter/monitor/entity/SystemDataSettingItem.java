package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据管理-配置项
 *
 * 记录某数据源的扩展键值配置。
 *
 * @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("数据管理-配置项")
@Schema(name = "SystemDataSettingItem")
@TableName("system_data_setting_item")
public class SystemDataSettingItem extends SysBase {

    @TableId(value = "system_data_setting_item_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    @NotNull(message = "ID不能为空", groups = { })
    private Integer systemDataSettingItemId;

    /** 归属的数据源ID */
    @Schema(description = "数据源ID")
    @TableField("system_data_setting_item_setting_id")
    private Integer systemDataSettingItemSettingId;

    /** 配置项名称 */
    @Schema(description = "配置项名称")
    @TableField("system_data_setting_item_name")
    @Size(max = 255)
    private String systemDataSettingItemName;

    /** 配置项值 */
    @Schema(description = "配置项值")
    @TableField("system_data_setting_item_value")
    private String systemDataSettingItemValue;

    /** 排序号 */
    @Schema(description = "排序号")
    @TableField("system_data_setting_item_order")
    private Integer systemDataSettingItemOrder;
}


