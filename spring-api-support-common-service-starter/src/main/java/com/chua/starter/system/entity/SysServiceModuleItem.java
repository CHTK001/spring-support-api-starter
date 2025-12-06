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
 * 服务子项
 */
@ApiModel(description = "服务子项")
@Schema(description = "服务子项")
@Data
@TableName(value = "sys_service_module_item")
public class SysServiceModuleItem {
    @TableId(value = "sys_service_item_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysServiceItemId;

    /**
     * 服务子ID
     */
    @TableField(value = "sys_service_id")
    @ApiModelProperty(value = "服务子ID")
    @Schema(description = "服务子ID")
    private Integer sysServiceId;

    /**
     * 模块ID
     */
    @TableField(value = "sys_service_module_id")
    @ApiModelProperty(value = "模块ID")
    @Schema(description = "模块ID")
    private Integer sysServiceModuleId;
}