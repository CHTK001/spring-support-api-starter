package com.chua.starter.system.entity;

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

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author CH
 * @since 2024/8/8
 */

/**
 * 用户日志�?
 */
@ApiModel(description = "用户日志�?)
@Schema(description = "用户日志�?)
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_log")
public class SysUserLog extends SysBase implements Serializable {
    @TableId(value = "sys_log_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysLogId;

    /**
     * 账号
     */
    @TableField(value = "sys_log_username")
    @ApiModelProperty(value = "账号")
    @Schema(description = "账号")
    @Size(max = 255, message = "账号最大长度要小于 255")
    private String sysLogUsername;
    /**
     * 登录方式
     */
    @TableField(value = "sys_log_login_type")
    @ApiModelProperty(value = "登录方式")
    @Schema(description = "登录方式")
    @Size(max = 255, message = "账号最大长度要小于 255")
    private String sysLogLoginType;

    /**
     * 浏览�?
     */
    @TableField(value = "sys_log_ua")
    @ApiModelProperty(value = "浏览器ua")
    @Schema(description = "浏览器ua")
    @Size(max = 255, message = "浏览器ua最大长度要小于 255")
    private String sysLogUa;

    /**
     * 浏览器指�?
     */
    @TableField(value = "sys_log_fingerprint")
    @ApiModelProperty(value = "浏览器指�?)
    @Schema(description = "浏览器指�?)
    private String sysLogFingerprint;

    /**
     * 浏览�?
     */
    @TableField(value = "sys_log_browser")
    @ApiModelProperty(value = "浏览�?)
    @Schema(description = "浏览�?)
    @Size(max = 255, message = "浏览器最大长度要小于 255")
    private String sysLogBrowser;
    /**
     * 系统
     */
    @TableField(value = "sys_log_system")
    @ApiModelProperty(value = "系统")
    @Schema(description = "系统")
    @Size(max = 255, message = "系统最大长度要小于 255")
    private String sysLogSystem;

    /**
     * 请求IP
     */
    @TableField(value = "sys_log_ip")
    @ApiModelProperty(value = "请求IP")
    @Schema(description = "请求IP")
    @Size(max = 255, message = "请求IP最大长度要小于 255")
    private String sysLogIp;

    /**
     * 请求地址
     */
    @TableField(value = "sys_log_address")
    @ApiModelProperty(value = "请求地址")
    @Schema(description = "请求地址")
    @Size(max = 255, message = "请求地址最大长度要小于 255")
    private String sysLogAddress;
    /**
     * 运营�?
     */
    @TableField(value = "sys_log_isp")
    @ApiModelProperty(value = "运营�?)
    @Schema(description = "运营�?)
    @Size(max = 255, message = "运营商最大长度要小于 255")
    private String sysLogIsp;
    /**
     * 请求地址
     */
    @TableField(value = "sys_log_url")
    @ApiModelProperty(value = "请求地址")
    @Schema(description = "请求地址")
    @Size(max = 255, message = "请求地址最大长度要小于 255")
    private String sysLogUrl;
    /**
     * 应用名称
     */
    @TableField(value = "sys_log_app_name")
    @ApiModelProperty(value = "应用名称")
    @Schema(description = "应用名称")
    private String sysLogAppName;

    /**
     * 请求参数
     */
    @TableField(value = "sys_log_param")
    @ApiModelProperty(value = "请求参数")
    @Schema(description = "请求参数")
    @Size(max = 255, message = "请求参数最大长度要小于 255")
    private String sysLogParam;

    /**
     * 请求模块
     */
    @TableField(value = "sys_log_from")
    @ApiModelProperty(value = "请求模块")
    @Schema(description = "请求模块")
    @Size(max = 255, message = "请求模块最大长度要小于 255")
    private String sysLogFrom;

    /**
     * 请求状�? 0: 失败; 1: 成功
     */
    @TableField(value = "sys_log_status")
    @ApiModelProperty(value = "请求状�? 0: 失败; 1: 成功")
    @Schema(description = "请求状�? 0: 失败; 1: 成功")
    private Integer sysLogStatus;
    /**
     * 请求消息
     */
    @TableField(value = "sys_log_message")
    @ApiModelProperty(value = "请求消息")
    @Schema(description = "请求消息")
    private String sysLogMessage;

    /**
     * 耗时
     */
    @TableField(value = "sys_log_cost")
    @ApiModelProperty(value = "耗时")
    @Schema(description = "耗时")
    private BigDecimal sysLogCost;


    /**
     * 部门ID
     */
    @TableField(value = "sys_dept_id")
    @ApiModelProperty(value = "部门ID")
    @Schema(description = "部门ID")
    private Integer sysDeptId;

    /**
     * 机构名称
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "机构名称")
    @Schema(description = "机构名称")
    private String sysDeptName;

}