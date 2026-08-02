package com.chua.starter.entity.service;

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

/**
 * 系统日志表
 *
 * @author CH
 * @since 2024/8/7
 */
@Schema(description = "系统日志表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_log")
public class SysLog extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "sys_log_id", type = IdType.AUTO)
    @Schema(description = "日志ID")
    @NotNull(message = "日志ID不能为空")
    private Integer sysLogId;

    /**
     * 账号
     */
    @TableField(value = "sys_log_username")
    @Schema(description = "账号")
    @Size(max = 255, message = "账号最大长度需小于255")
    private String sysLogUsername;

    /**
     * 请求耗时(毫秒)
     */
    @TableField(value = "sys_log_cost")
    @Schema(description = "请求耗时(毫秒)")
    private Long sysLogCost;

    /**
     * 请求IP地址
     */
    @TableField(value = "sys_log_address")
    @Schema(description = "请求IP地址")
    @Size(max = 255, message = "请求IP地址最大长度需小于255")
    private String sysLogAddress;

    /**
     * 请求URL
     */
    @TableField(value = "sys_log_url")
    @Schema(description = "请求URL")
    @Size(max = 255, message = "请求URL最大长度需小于255")
    private String sysLogUrl;

    /**
     * 浏览器信息
     */
    @TableField(value = "sys_log_browser")
    @Schema(description = "浏览器信息")
    private String sysLogBrowser;

    /**
     * 请求描述
     */
    @TableField(value = "sys_log_content")
    @Schema(description = "请求描述")
    private String sysLogContent;

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
}
