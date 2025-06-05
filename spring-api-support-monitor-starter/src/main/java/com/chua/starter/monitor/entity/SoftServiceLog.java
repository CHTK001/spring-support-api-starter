package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CH
 * @since 2025/6/5 9:19
 */
@ApiModel(description = "soft_service_log")
@Schema
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "soft_service_log")
public class SoftServiceLog extends SysBase {
    @TableId(value = "soft_service_log_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer softServiceLogId;

    /**
     * 安装的服务器
     */
    @TableField(value = "soft_service_host")
    @ApiModelProperty(value = "安装的服务器")
    @Schema(description = "安装的服务器")
    private String softServiceHost;

    /**
     * 使用的ssh表ID
     */
    @TableField(value = "soft_service_ssh_id")
    @ApiModelProperty(value = "使用的ssh表ID")
    @Schema(description = "使用的ssh表ID")
    private String softServiceSshId;

    /**
     * 安装结果
     */
    @TableField(value = "soft_service_status")
    @ApiModelProperty(value = "安装结果")
    @Schema(description = "安装结果")
    private String softServiceStatus;

    /**
     * 报错信息
     */
    @TableField(value = "soft_service_error_message")
    @ApiModelProperty(value = "报错信息")
    @Schema(description = "报错信息")
    private String softServiceErrorMessage;
}