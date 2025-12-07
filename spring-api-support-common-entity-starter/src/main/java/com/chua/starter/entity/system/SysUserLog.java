package com.chua.starter.entity.system;

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
 * 用户操作日志表
 *
 * @author CH
 * @since 2024/8/8
 */
@ApiModel(description = "用户操作日志表")
@Schema(description = "用户操作日志表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_log")
public class SysUserLog extends SysBase implements Serializable {

    /**
     * 日志ID
     */
    @TableId(value = "sys_log_id", type = IdType.AUTO)
    @ApiModelProperty(value = "日志ID")
    @Schema(description = "日志ID")
    @NotNull(message = "日志ID不能为空")
    private Integer sysLogId;

    /**
     * 操作账号
     */
    @TableField(value = "sys_log_username")
    @ApiModelProperty(value = "操作账号")
    @Schema(description = "操作账号")
    @Size(max = 255, message = "操作账号最大长度需小于255")
    private String sysLogUsername;

    /**
     * 登录方式
     */
    @TableField(value = "sys_log_login_type")
    @ApiModelProperty(value = "登录方式")
    @Schema(description = "登录方式")
    @Size(max = 255, message = "登录方式最大长度需小于255")
    private String sysLogLoginType;

    /**
     * 浏览器UA
     */
    @TableField(value = "sys_log_ua")
    @ApiModelProperty(value = "浏览器UA")
    @Schema(description = "浏览器UA")
    @Size(max = 255, message = "浏览器UA最大长度需小于255")
    private String sysLogUa;

    /**
     * 浏览器指纹
     */
    @TableField(value = "sys_log_fingerprint")
    @ApiModelProperty(value = "浏览器指纹")
    @Schema(description = "浏览器指纹")
    private String sysLogFingerprint;

    /**
     * 浏览器名称
     */
    @TableField(value = "sys_log_browser")
    @ApiModelProperty(value = "浏览器名称")
    @Schema(description = "浏览器名称")
    @Size(max = 255, message = "浏览器名称最大长度需小于255")
    private String sysLogBrowser;

    /**
     * 操作系统
     */
    @TableField(value = "sys_log_system")
    @ApiModelProperty(value = "操作系统")
    @Schema(description = "操作系统")
    @Size(max = 255, message = "操作系统最大长度需小于255")
    private String sysLogSystem;

    /**
     * 请求IP地址
     */
    @TableField(value = "sys_log_ip")
    @ApiModelProperty(value = "请求IP地址")
    @Schema(description = "请求IP地址")
    @Size(max = 255, message = "请求IP地址最大长度需小于255")
    private String sysLogIp;

    /**
     * 请求地理位置
     */
    @TableField(value = "sys_log_address")
    @ApiModelProperty(value = "请求地理位置")
    @Schema(description = "请求地理位置")
    @Size(max = 255, message = "请求地理位置最大长度需小于255")
    private String sysLogAddress;

    /**
     * 网络运营商
     */
    @TableField(value = "sys_log_isp")
    @ApiModelProperty(value = "网络运营商")
    @Schema(description = "网络运营商")
    @Size(max = 255, message = "网络运营商最大长度需小于255")
    private String sysLogIsp;

    /**
     * 请求URL
     */
    @TableField(value = "sys_log_url")
    @ApiModelProperty(value = "请求URL")
    @Schema(description = "请求URL")
    @Size(max = 255, message = "请求URL最大长度需小于255")
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
    @Size(max = 255, message = "请求参数最大长度需小于255")
    private String sysLogParam;

    /**
     * 请求模块
     */
    @TableField(value = "sys_log_from")
    @ApiModelProperty(value = "请求模块")
    @Schema(description = "请求模块")
    @Size(max = 255, message = "请求模块最大长度需小于255")
    private String sysLogFrom;

    /**
     * 请求状态: 0:失败 1:成功
     */
    @TableField(value = "sys_log_status")
    @ApiModelProperty(value = "请求状态: 0:失败 1:成功")
    @Schema(description = "请求状态: 0:失败 1:成功")
    private Integer sysLogStatus;

    /**
     * 响应消息
     */
    @TableField(value = "sys_log_message")
    @ApiModelProperty(value = "响应消息")
    @Schema(description = "响应消息")
    private String sysLogMessage;

    /**
     * 请求耗时(毫秒)
     */
    @TableField(value = "sys_log_cost")
    @ApiModelProperty(value = "请求耗时(毫秒)")
    @Schema(description = "请求耗时(毫秒)")
    private BigDecimal sysLogCost;

    /**
     * 部门ID
     */
    @TableField(value = "sys_dept_id")
    @ApiModelProperty(value = "部门ID")
    @Schema(description = "部门ID")
    private Integer sysDeptId;

    /**
     * 机构名称(非数据库字段)
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "机构名称")
    @Schema(description = "机构名称(非数据库字段)")
    private String sysDeptName;
}
