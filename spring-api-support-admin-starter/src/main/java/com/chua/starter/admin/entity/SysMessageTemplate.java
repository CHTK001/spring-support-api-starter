package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ObjectArraySerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 消息推送模板
 */
@ApiModel(description = "消息推送模板")
@Schema(description = "消息推送模板")
@Data
@TableName(value = "sys_message_template")
public class SysMessageTemplate  extends SysTenantBase {
    @TableId(value = "message_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer messageId;

    /**
     * 模板名称
     */
    @TableField(value = "message_name")
    @ApiModelProperty(value = "模板名称")
    @Schema(description = "模板名称")
    private String messageName;

    /**
     * 模板编号
     */
    @TableField(value = "message_code")
    @ApiModelProperty(value = "模板编号")
    @Schema(description = "模板编号")
    private String messageCode;

    /**
     * 推送配置表ID
     */
    @TableField(value = "message_config_id")
    @ApiModelProperty(value = "推送配置表ID")
    @Schema(description = "推送配置表ID")
    private Integer messageConfigId;

    /**
     * 消息类型
     */
    @TableField(value = "message_type")
    @ApiModelProperty(value = "消息类型")
    @Schema(description = "消息类型")
    private String messageType;

    /**
     * 模板
     */
    @TableField(value = "message_template")
    @ApiModelProperty(value = "模板")
    @Schema(description = "模板")
    private String messageTemplate;


    /**
     * 模板发件人
     */
    @TableField(value = "message_sender")
    @ApiModelProperty(value = "模板发件人")
    @Schema(description = "模板发件人")
    private String messageSender;

    @ApiModelProperty(value = "下发渠道")
    @Schema(description = "下发渠道")
    @TableField(exist = false)
    @JsonSerialize(using = ObjectArraySerializer.class)
    private List<Integer> config;
}