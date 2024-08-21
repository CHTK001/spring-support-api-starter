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
 *
 * @since 2024/8/8
 * @author CH    
 */
/**
 * 用户日志表
 */
@ApiModel(description="用户日志表")
@Schema(description="用户日志表")
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_user_log")
public class SysUserLog extends SysBase implements Serializable {
    @TableId(value = "sys_log_id", type = IdType.AUTO)
    @ApiModelProperty(value="")
    @Schema(description="")
    @NotNull(message = "不能为null")
    private Integer sysLogId;

    /**
     * 账号
     */
    @TableField(value = "sys_log_username")
    @ApiModelProperty(value="账号")
    @Schema(description="账号")
    @Size(max = 255,message = "账号最大长度要小于 255")
    private String sysLogUsername;

    /**
     * 请求IP
     */
    @TableField(value = "sys_log_ip")
    @ApiModelProperty(value="请求IP")
    @Schema(description="请求IP")
    @Size(max = 255,message = "请求IP最大长度要小于 255")
    private String sysLogIp;

    /**
     * 请求地址
     */
    @TableField(value = "sys_log_address")
    @ApiModelProperty(value="请求地址")
    @Schema(description="请求地址")
    @Size(max = 255,message = "请求地址最大长度要小于 255")
    private String sysLogAddress;
    /**
     * 运营商
     */
    @TableField(value = "sys_log_isp")
    @ApiModelProperty(value="运营商")
    @Schema(description="运营商")
    @Size(max = 255,message = "运营商最大长度要小于 255")
    private String sysLogIsp;
    /**
     * 请求地址
     */
    @TableField(value = "sys_log_url")
    @ApiModelProperty(value="请求地址")
    @Schema(description="请求地址")
    @Size(max = 255,message = "请求地址最大长度要小于 255")
    private String sysLogUrl;

    /**
     * 请求参数
     */
    @TableField(value = "sys_log_param")
    @ApiModelProperty(value="请求参数")
    @Schema(description="请求参数")
    @Size(max = 255,message = "请求参数最大长度要小于 255")
    private String sysLogParam;

    /**
     * 请求模块
     */
    @TableField(value = "sys_log_from")
    @ApiModelProperty(value="请求模块")
    @Schema(description="请求模块")
    @Size(max = 255,message = "请求模块最大长度要小于 255")
    private String sysLogFrom;

    /**
     * 请求状态; 0: 失败; 1: 成功
     */
    @TableField(value = "sys_log_status")
    @ApiModelProperty(value="请求状态; 0: 失败; 1: 成功")
    @Schema(description="请求状态; 0: 失败; 1: 成功")
    private Integer sysLogStatus;

    /**
     * 耗时
     */
    @TableField(value = "sys_log_cost")
    @ApiModelProperty(value="耗时")
    @Schema(description="耗时")
    private BigDecimal sysLogCost;

}