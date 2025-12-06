package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 限流配置表
 * 
 * @author CH
 * @since 2025/9/28
 */
@ApiModel(description = "限流配置表")
@Schema(description = "限流配置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_limit_configuration")
public class SysLimitConfiguration extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(value = "sys_limit_configuration_id", type = IdType.AUTO)
    @ApiModelProperty(value = "限流配置ID")
    @Schema(description = "限流配置ID")
    @NotNull(message = "限流配置ID不能为null", groups = {UpdateGroup.class})
    private Long sysLimitConfigurationId;

    /**
     * 接口路径
     */
    @TableField(value = "sys_limit_path")
    @ApiModelProperty(value = "接口路径")
    @Schema(description = "接口路径")
    @Size(max = 500, message = "接口路径最大长度要小于 500")
    @NotNull(message = "接口路径不能为null", groups = {AddGroup.class})
    private String sysLimitPath;

    /**
     * 限流规则名称
     */
    @TableField(value = "sys_limit_name")
    @ApiModelProperty(value = "限流规则名称")
    @Schema(description = "限流规则名称")
    @Size(max = 255, message = "限流规则名称最大长度要小于 255")
    @NotNull(message = "限流规则名称不能为null", groups = {AddGroup.class})
    private String sysLimitName;

    /**
     * 每个周期的许可数量
     */
    @TableField(value = "sys_limit_for_period")
    @ApiModelProperty(value = "每个周期的许可数量")
    @Schema(description = "每个周期的许可数量")
    private Integer sysLimitForPeriod;

    /**
     * 限制刷新周期（秒）
     */
    @TableField(value = "sys_limit_refresh_period_seconds")
    @ApiModelProperty(value = "限制刷新周期（秒）")
    @Schema(description = "限制刷新周期（秒）")
    private Long sysLimitRefreshPeriodSeconds;

    /**
     * 获取许可的超时时间（毫秒）
     */
    @TableField(value = "sys_limit_timeout_duration_millis")
    @ApiModelProperty(value = "获取许可的超时时间（毫秒）")
    @Schema(description = "获取许可的超时时间（毫秒）")
    private Long sysLimitTimeoutDurationMillis;

    /**
     * 限流维度（GLOBAL, IP, USER, API）
     */
    @TableField(value = "sys_limit_dimension")
    @ApiModelProperty(value = "限流维度（GLOBAL, IP, USER, API）")
    @Schema(description = "限流维度（GLOBAL, IP, USER, API）")
    @Size(max = 50, message = "限流维度最大长度要小于 50")
    private String sysLimitDimension;

    /**
     * 自定义键表达式（SpEL）
     */
    @TableField(value = "sys_limit_key_expression")
    @ApiModelProperty(value = "自定义键表达式（SpEL）")
    @Schema(description = "自定义键表达式（SpEL）")
    @Size(max = 500, message = "自定义键表达式最大长度要小于 500")
    private String sysLimitKeyExpression;

    /**
     * 降级方法名称
     */
    @TableField(value = "sys_limit_fallback_method")
    @ApiModelProperty(value = "降级方法名称")
    @Schema(description = "降级方法名称")
    @Size(max = 255, message = "降级方法名称最大长度要小于 255")
    private String sysLimitFallbackMethod;

    /**
     * 错误消息
     */
    @TableField(value = "sys_limit_message")
    @ApiModelProperty(value = "错误消息")
    @Schema(description = "错误消息")
    @Size(max = 500, message = "错误消息最大长度要小于 500")
    private String sysLimitMessage;

    /**
     * 是否启用; 0: 禁用, 1: 启用
     */
    @TableField(value = "sys_limit_status")
    @ApiModelProperty(value = "是否启用; 0: 禁用, 1: 启用")
    @Schema(description = "是否启用; 0: 禁用, 1: 启用")
    private Integer sysLimitStatus;

    /**
     * 描述信息
     */
    @TableField(value = "sys_limit_description")
    @ApiModelProperty(value = "描述信息")
    @Schema(description = "描述信息")
    @Size(max = 500, message = "描述信息最大长度要小于 500")
    private String sysLimitDescription;

    /**
     * 排序
     */
    @TableField(value = "sys_limit_sort")
    @ApiModelProperty(value = "排序")
    @Schema(description = "排序")
    private Integer sysLimitSort;
    
    /**
     * 是否针对当前登录账号; 0: 否, 1: 是
     */
    @TableField(value = "sys_limit_current_user")
    @ApiModelProperty(value = "是否针对当前登录账号; 0: 否, 1: 是")
    @Schema(description = "是否针对当前登录账号; 0: 否, 1: 是")
    private Integer sysLimitCurrentUser;
    
}