package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
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
@Schema(description = "用户操作日志表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_log")
public class SysUserLog extends SysBase implements Serializable {

    /**
     * 日志ID
     */
    @TableId(value = "sys_log_id", type = IdType.AUTO)
    @Schema(description = "日志ID")
    @NotNull(message = "日志ID不能为空")
    private Integer sysLogId;

    /**
     * 操作账号
     */
    @TableField(value = "sys_log_username")
    @Schema(description = "操作账号")
    @Size(max = 255, message = "操作账号最大长度需小于255")
    private String sysLogUsername;

    /**
     * 登录方式
     */
    @TableField(value = "sys_log_login_type")
    @Schema(description = "登录方式")
    @Size(max = 255, message = "登录方式最大长度需小于255")
    private String sysLogLoginType;

    /**
     * 浏览器UA
     */
    @TableField(value = "sys_log_ua")
    @Schema(description = "浏览器UA")
    @Size(max = 255, message = "浏览器UA最大长度需小于255")
    private String sysLogUa;

    /**
     * 浏览器指纹
     */
    @TableField(value = "sys_log_fingerprint")
    @Schema(description = "浏览器指纹")
    private String sysLogFingerprint;

    /**
     * 浏览器名称
     */
    @TableField(value = "sys_log_browser")
    @Schema(description = "浏览器名称")
    @Size(max = 255, message = "浏览器名称最大长度需小于255")
    private String sysLogBrowser;

    /**
     * 操作系统
     */
    @TableField(value = "sys_log_system")
    @Schema(description = "操作系统")
    @Size(max = 255, message = "操作系统最大长度需小于255")
    private String sysLogSystem;

    /**
     * 请求IP地址
     */
    @TableField(value = "sys_log_ip")
    @Schema(description = "请求IP地址")
    @Size(max = 255, message = "请求IP地址最大长度需小于255")
    private String sysLogIp;

    /**
     * 请求地理位置
     */
    @TableField(value = "sys_log_address")
    @Schema(description = "请求地理位置")
    @Size(max = 255, message = "请求地理位置最大长度需小于255")
    private String sysLogAddress;

    /**
     * 网络运营商
     */
    @TableField(value = "sys_log_isp")
    @Schema(description = "网络运营商")
    @Size(max = 255, message = "网络运营商最大长度需小于255")
    private String sysLogIsp;

    /**
     * 请求URL
     */
    @TableField(value = "sys_log_url")
    @Schema(description = "请求URL")
    @Size(max = 255, message = "请求URL最大长度需小于255")
    private String sysLogUrl;

    /**
     * 应用名称
     */
    @TableField(value = "sys_log_app_name")
    @Schema(description = "应用名称")
    private String sysLogAppName;

    /**
     * 请求参数
     */
    @TableField(value = "sys_log_param")
    @Schema(description = "请求参数")
    @Size(max = 255, message = "请求参数最大长度需小于255")
    private String sysLogParam;

    /**
     * 请求模块
     */
    @TableField(value = "sys_log_from")
    @Schema(description = "请求模块")
    @Size(max = 255, message = "请求模块最大长度需小于255")
    private String sysLogFrom;

    /**
     * 请求状态: 0:失败 1:成功
     */
    @TableField(value = "sys_log_status")
    @Schema(description = "请求状态: 0:失败 1:成功")
    private Integer sysLogStatus;

    /**
     * 响应消息
     */
    @TableField(value = "sys_log_message")
    @Schema(description = "响应消息")
    private String sysLogMessage;

    /**
     * 请求耗时(毫秒)
     */
    @TableField(value = "sys_log_cost")
    @Schema(description = "请求耗时(毫秒)")
    private BigDecimal sysLogCost;

    /**
     * 部门ID
     */
    @TableField(value = "sys_dept_id")
    @Schema(description = "部门ID")
    private Integer sysDeptId;

    /**
     * 机构名称(非数据库字段)
     */
    @TableField(exist = false)
    @Schema(description = "机构名称(非数据库字段)")
    private String sysDeptName;
}
