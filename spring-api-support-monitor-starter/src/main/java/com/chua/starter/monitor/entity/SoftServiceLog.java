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
 * 软件服务日志
 *
 * @author CH
 * @since 2025/6/5 9:19
 */
@ApiModel(description = "软件服务日志")
@Schema(description = "软件服务日志")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "soft_service_log")
public class SoftServiceLog extends SysBase {
    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    @ApiModelProperty(value = "日志ID")
    @Schema(description = "日志ID")
    private Integer logId;

    /**
     * 安装ID
     */
    @TableField(value = "install_id")
    @ApiModelProperty(value = "安装ID")
    @Schema(description = "安装ID")
    private Integer installId;

    /**
     * 软件服务ID
     */
    @TableField(value = "soft_service_id")
    @ApiModelProperty(value = "软件服务ID")
    @Schema(description = "软件服务ID")
    private Integer softServiceId;

    /**
     * SSH ID
     */
    @TableField(value = "ssh_id")
    @ApiModelProperty(value = "SSH ID")
    @Schema(description = "SSH ID")
    private String sshId;

    /**
     * 日志类型：1-安装，2-卸载，3-启动，4-停止，5-重启
     */
    @TableField(value = "log_type")
    @ApiModelProperty(value = "日志类型：1-安装，2-卸载，3-启动，4-停止，5-重启")
    @Schema(description = "日志类型：1-安装，2-卸载，3-启动，4-停止，5-重启")
    private Integer logType;

    /**
     * 日志内容
     */
    @TableField(value = "log_content")
    @ApiModelProperty(value = "日志内容")
    @Schema(description = "日志内容")
    private String logContent;

    /**
     * 日志状态：0-进行中，1-成功，2-失败
     */
    @TableField(value = "log_status")
    @ApiModelProperty(value = "日志状态：0-进行中，1-成功，2-失败")
    @Schema(description = "日志状态：0-进行中，1-成功，2-失败")
    private Integer logStatus;

    /**
     * 日志时间
     */
    @TableField(value = "log_time")
    @ApiModelProperty(value = "日志时间")
    @Schema(description = "日志时间")
    private Long logTime;
}