package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @since 2024/11/27
 * @author CH    
 */

/**
 * 短信下发日志表
 */
@ApiModel(description = "短信下发日志表")
@Schema(description = "短信下发日志表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_sms_log")
public class SysSmsLog extends SysBase implements Serializable {
    @TableId(value = "sys_sms_log_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysSmsLogId;

    /**
     * 接收人
     */
    @TableField(value = "sys_sms_log_target")
    @ApiModelProperty(value = "接收人")
    @Schema(description = "接收人")
    @Size(max = 255, message = "接收人最大长度要小于 255")
    private String sysSmsLogTarget;

    /**
     * 模板ID
     */
    @TableField(value = "sys_sms_log_template_id")
    @ApiModelProperty(value = "模板ID")
    @Schema(description = "模板ID")
    @Size(max = 255, message = "模板ID最大长度要小于 255")
    private String sysSmsLogTemplateId;

    /**
     * 参数
     */
    @TableField(value = "sys_sms_log_param")
    @ApiModelProperty(value = "参数")
    @Schema(description = "参数")
    @Size(max = 255, message = "参数最大长度要小于 255")
    private String sysSmsLogParam;

    /**
     * 耗时时间
     */
    @TableField(value = "sys_sms_cost")
    @ApiModelProperty(value = "耗时时间")
    @Schema(description = "耗时时间")
    private BigDecimal sysSmsCost;

    /**
     * 错误码
     */
    @TableField(value = "sys_sms_code")
    @ApiModelProperty(value = "错误码")
    @Schema(description = "错误码")
    @Size(max = 255, message = "错误码最大长度要小于 255")
    private String sysSmsCode;

    /**
     * 错误信息
     */
    @TableField(value = "sys_sms_message")
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    @Size(max = 255, message = "错误信息最大长度要小于 255")
    private String sysSmsMessage;

    /**
     * 短信厂家
     */
    @TableField(value = "sys_sms_app_type")
    @ApiModelProperty(value = "短信厂家")
    @Schema(description = "短信厂家")
    @Size(max = 255, message = "短信厂家最大长度要小于 255")
    private String sysSmsAppType;

    /**
     * 短信密钥
     */
    @TableField(value = "sys_sms_app_id")
    @ApiModelProperty(value = "短信密钥")
    @Schema(description = "短信密钥")
    @Size(max = 255, message = "短信密钥最大长度要小于 255")
    private String sysSmsAppId;

    /**
     * 签名
     */
    @TableField(value = "sys_sms_app_sign")
    @ApiModelProperty(value = "签名")
    @Schema(description = "签名")
    @Size(max = 255, message = "签名最大长度要小于 255")
    private String sysSmsAppSign;
}