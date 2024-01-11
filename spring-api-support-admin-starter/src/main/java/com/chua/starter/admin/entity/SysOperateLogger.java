package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 系统日志信息表
 */
@ApiModel(description = "系统日志信息表")
@Schema(description = "系统日志信息表")
@Data
@TableName(value = "sys_operate_logger")
public class SysOperateLogger {
    /**
     * 主键
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    @Schema(description = "主键")
    @NotNull(message = "主键不能为null")
    private Integer logId;

    /**
     * 日志编号
     */
    @TableField(value = "log_code")
    @ApiModelProperty(value = "日志编号")
    @Schema(description = "日志编号")
    @Size(max = 255, message = "日志编号最大长度要小于 255")
    private String logCode;

    /**
     * 日志名称
     */
    @TableField(value = "log_name")
    @ApiModelProperty(value = "日志名称")
    @Schema(description = "日志名称")
    @Size(max = 255, message = "日志名称最大长度要小于 255")
    private String logName;

    /**
     * 耗时
     */
    @TableField(value = "log_cost")
    @ApiModelProperty(value = "耗时")
    @Schema(description = "耗时")
    private Integer logCost;

    /**
     * 日志请求地址
     */
    @TableField(value = "log_mapping")
    @ApiModelProperty(value = "日志请求地址")
    @Schema(description = "日志请求地址")
    @Size(max = 255, message = "日志请求地址最大长度要小于 255")
    private String logMapping;

    /**
     * 日志内容
     */
    @TableField(value = "log_content")
    @ApiModelProperty(value = "日志内容")
    @Schema(description = "日志内容")
    @Size(max = 255, message = "日志内容最大长度要小于 255")
    private String logContent;

    /**
     * 请求地址
     */
    @TableField(value = "client_ip")
    @ApiModelProperty(value = "请求地址")
    @Schema(description = "请求地址")
    @Size(max = 255, message = "请求地址最大长度要小于 255")
    private String clientIp;

    /**
     * 请求地址位置
     */
    @TableField(value = "client_ip_position")
    @ApiModelProperty(value = "请求地址位置")
    @Schema(description = "请求地址位置")
    @Size(max = 255, message = "请求地址位置最大长度要小于 255")
    private String clientIpPosition;

    /**
     * 请求链路
     */
    @TableField(value = "log_watch")
    @ApiModelProperty(value = "请求链路")
    @Schema(description = "请求链路")
    private String logWatch;

    /**
     * 查询参数
     */
    @TableField(value = "log_param")
    @ApiModelProperty(value = "查询参数")
    @Schema(description = "查询参数")
    private String logParam;

    /**
     * 操作模块
     */
    @TableField(value = "log_module")
    @ApiModelProperty(value = "操作模块")
    @Schema(description = "操作模块")
    @Size(max = 255, message = "操作模块最大长度要小于 255")
    private String logModule;

    /**
     * 状态; 1:成功;
     */
    @TableField(value = "log_status")
    @ApiModelProperty(value = "状态; 1:成功;")
    @Schema(description = "状态; 1:成功;")
    private Integer logStatus;

    /**
     * 时间(天)
     */
    @TableField(value = "log_day")
    @ApiModelProperty(value = "时间(天)")
    @Schema(description = "时间(天)")
    private Date logDay;

    /**
     * 创建人
     */
    @TableField(value = "create_name")
    @ApiModelProperty(value = "创建人")
    @Schema(description = "创建人")
    @Size(max = 255, message = "创建人最大长度要小于 255")
    private String createName;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    @ApiModelProperty(value = "创建人")
    @Schema(description = "创建人")
    @Size(max = 255, message = "创建人最大长度要小于 255")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 更新人
     */
    @TableField(value = "update_name")
    @ApiModelProperty(value = "更新人")
    @Schema(description = "更新人")
    @Size(max = 255, message = "更新人最大长度要小于 255")
    private String updateName;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    @ApiModelProperty(value = "更新人")
    @Schema(description = "更新人")
    @Size(max = 255, message = "更新人最大长度要小于 255")
    private String updateBy;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    @ApiModelProperty(value = "租户ID")
    @Schema(description = "租户ID")
    private Integer tenantId;
}