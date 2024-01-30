package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息配置关联表
 */
@ApiModel(description = "消息配置关联表")
@Schema(description = "消息配置关联表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_message_template_config")
public class SysMessageTemplateConfig {
    @TableId(value = "message_config_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer messageConfigId;

    /**
     * 消息ID
     */
    @TableField(value = "message_id")
    @ApiModelProperty(value = "消息ID")
    @Schema(description = "消息ID")
    private Integer messageId;

    /**
     * 配置ID
     */
    @TableField(value = "config_id")
    @ApiModelProperty(value = "配置ID")
    @Schema(description = "配置ID")
    private Integer configId;
}