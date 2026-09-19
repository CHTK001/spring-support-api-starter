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
 * @since 2024/8/8
 * @author CH
 */
@Schema(description = "用户操作日志表")
@Data
@EqualsAndHashCode(callSuper = false)
public class SysUserLog extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L; // 串行版本uid

    /**
     * 日志标识
     */
    @TableId(value = "sys_log_id", type = IdType.AUTO)
    @Schema(description = "日志ID")
    @NotNull(message = "日志ID不能为空")
    /** SYS日志标识 */
    private Integer sysLogId;

    /**
     * 操作账号
     */
    @TableField(value = "sys_log_username")
    @Schema(description = "操作账号")
    @Size(max = 255, message = "操作账号最大长度需小于255")
    /** SYS日志用户名 */
    private String sysLogUsername;

    /**
     * 登录方式
     */
    @TableField(value = "sys_log_login_type")
    @Schema(description = "登录方式")
    @Size(max = 255, message = "登录方式最大长度需小于255")
    /** SYS日志login类型 */
    private String sysLogLoginType;

    /**
     * 浏览器UA
     */
    @TableField(value = "sys_log_ua")
    @Schema(description = "浏览器UA")
    /** SYS日志UA */
    private String sysLogUa;

    /**
     * 浏览器指纹
     */
    @TableField(value = "sys_log_fingerprint")
    @Schema(description = "浏览器指纹")
    /** SYS日志fingerprint */
    private String sysLogFingerprint;

    /**
     * 浏览器名称
     */
    @TableField(value = "sys_log_browser")
    @Schema(description = "浏览器名称")
    @Size(max = 255, message = "浏览器名称最大长度需小于255")
    /** SYS日志browser */
    private String sysLogBrowser;

    /**
     * 操作系统
     */
    @TableField(value = "sys_log_system")
    @Schema(description = "操作系统")
    @Size(max = 255, message = "操作系统最大长度需小于255")
    /** SYS日志系统 */
    private String sysLogSystem;

    /**
     * 请求IP地址
     */
    @TableField(value = "sys_log_ip")
    @Schema(description = "请求IP地址")
    @Size(max = 255, message = "请求IP地址最大长度需小于255")
    /** SYS日志IP */
    private String sysLogIp;

    /**
     * 请求地理位置
     */
    @TableField(value = "sys_log_address")
    @Schema(description = "请求地理位置")
    /** SYS日志地址 */
    private String sysLogAddress;

    /**
     * 网络运营商
     */
    @TableField(value = "sys_log_isp")
    @Schema(description = "网络运营商")
    @Size(max = 255, message = "网络运营商最大长度需小于255")
    /** SYS日志ISP */
    private String sysLogIsp;

    /**
     * 请求URL
     */
    @TableField(value = "sys_log_url")
    @Schema(description = "请求URL")
    /** SYS日志URL */
    private String sysLogUrl;

    /**
     * 应用名称
     */
    @TableField(value = "sys_log_app_name")
    @Schema(description = "应用名称")
    /** SYS日志APP名称 */
    private String sysLogAppName;

    /**
     * 请求参数
     */
    @TableField(value = "sys_log_param")
    @Schema(description = "请求参数")
    /** SYS日志参数 */
    private String sysLogParam;

    /**
     * 请求模块
     */
    @TableField(value = "sys_log_from")
    @Schema(description = "请求模块")
    @Size(max = 255, message = "请求模块最大长度需小于255")
    /** SYS日志从 */
    private String sysLogFrom;

    /**
     * 请求状态: 0:失败 1:成功
     */
    @TableField(value = "sys_log_status")
    @Schema(description = "请求状态: 0:失败 1:成功")
    /** SYS日志状态 */
    private Integer sysLogStatus;

    /**
     * 响应消息
     */
    @TableField(value = "sys_log_message")
    @Schema(description = "响应消息")
    /** SYS日志消息 */
    private String sysLogMessage;

    /**
     * 请求耗时(毫秒)
     */
    @TableField(value = "sys_log_cost")
    @Schema(description = "请求耗时(毫秒)")
    /** SYS日志cost */
    private BigDecimal sysLogCost;

    /**
     * 部门标识
     */
    @TableField(value = "sys_dept_id")
    @Schema(description = "部门ID")
    /** sysdeptid */
    private Integer sysDeptId;

    /**
     * 机构名称(非数据库字段)
     */
    @TableField(exist = false)
    @Schema(description = "机构名称(非数据库字段)")
    /** sysdept名称 */
    private String sysDeptName;

    /**
     * 账号角色编码列表(非数据库字段)
     */
    @TableField(exist = false)
    @Schema(description = "账号角色编码列表(非数据库字段)")
    /** SYS日志rolecodes */
    private String sysLogRoleCodes;

    /**
     * 账号角色名称列表(非数据库字段)
     */
    @TableField(exist = false)
    @Schema(description = "账号角色名称列表(非数据库字段)")
    /** SYS日志rolenames */
    private String sysLogRoleNames;
}
