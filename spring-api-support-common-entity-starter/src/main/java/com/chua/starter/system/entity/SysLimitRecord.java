package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 限流记录表
 * 
 * @author CH
 * @since 2025/9/28
 */
@ApiModel(description = "限流记录表")
@Schema(description = "限流记录表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_limit_record")
public class SysLimitRecord extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(value = "sys_limit_record_id", type = IdType.AUTO)
    @ApiModelProperty(value = "限流记录ID")
    @Schema(description = "限流记录ID")
    private Long sysLimitRecordId;

    /**
     * 限流配置ID
     */
    @TableField(value = "sys_limit_configuration_id")
    @ApiModelProperty(value = "限流配置ID")
    @Schema(description = "限流配置ID")
    private Long sysLimitConfigurationId;

    /**
     * 接口路径
     */
    @TableField(value = "sys_limit_path")
    @ApiModelProperty(value = "接口路径")
    @Schema(description = "接口路径")
    private String sysLimitPath;

    /**
     * 限流规则名称
     */
    @TableField(value = "sys_limit_name")
    @ApiModelProperty(value = "限流规则名称")
    @Schema(description = "限流规则名称")
    private String sysLimitName;

    /**
     * 限流维度（GLOBAL, IP, USER, API）
     */
    @TableField(value = "sys_limit_dimension")
    @ApiModelProperty(value = "限流维度（GLOBAL, IP, USER, API）")
    @Schema(description = "限流维度（GLOBAL, IP, USER, API）")
    private String sysLimitDimension;

    /**
     * 限流键值（如IP地址、用户ID等）
     */
    @TableField(value = "sys_limit_key")
    @ApiModelProperty(value = "限流键值（如IP地址、用户ID等）")
    @Schema(description = "限流键值（如IP地址、用户ID等）")
    private String sysLimitKey;

    /**
     * 限流时间
     */
    @TableField(value = "sys_limit_time")
    @ApiModelProperty(value = "限流时间")
    @Schema(description = "限流时间")
    private Date sysLimitTime;

    /**
     * 用户ID
     */
    @TableField(value = "sys_user_id")
    @ApiModelProperty(value = "用户ID")
    @Schema(description = "用户ID")
    private Long sysUserId;

    /**
     * 用户名
     */
    @TableField(value = "sys_user_name")
    @ApiModelProperty(value = "用户名")
    @Schema(description = "用户名")
    private String sysUserName;

    /**
     * 客户端IP
     */
    @TableField(value = "client_ip")
    @ApiModelProperty(value = "客户端IP")
    @Schema(description = "客户端IP")
    private String clientIp;

    /**
     * 请求URL
     */
    @TableField(value = "request_url")
    @ApiModelProperty(value = "请求URL")
    @Schema(description = "请求URL")
    private String requestUrl;

    /**
     * 请求方法
     */
    @TableField(value = "request_method")
    @ApiModelProperty(value = "请求方法")
    @Schema(description = "请求方法")
    private String requestMethod;

    /**
     * 用户代理
     */
    @TableField(value = "user_agent")
    @ApiModelProperty(value = "用户代理")
    @Schema(description = "用户代理")
    private String userAgent;
    
    /**
     * 是否针对当前登录账号; 0: 否, 1: 是
     */
    @TableField(value = "sys_limit_current_user")
    @ApiModelProperty(value = "是否针对当前登录账号; 0: 否, 1: 是")
    @Schema(description = "是否针对当前登录账号; 0: 否, 1: 是")
    private Integer sysLimitCurrentUser;
}