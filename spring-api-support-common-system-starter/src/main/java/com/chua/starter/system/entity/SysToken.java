package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 系统令牌表
 * 
 * @author CH
 */
@ApiModel(description = "系统令牌表")
@Schema(description = "系统令牌表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_token")
public class SysToken extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(value = "sys_token_id", type = IdType.AUTO)
    @ApiModelProperty(value = "令牌ID")
    @Schema(description = "令牌ID")
    private Long sysTokenId;

    /**
     * 用户ID
     */
    @TableField(value = "sys_user_id")
    @ApiModelProperty(value = "用户ID")
    @Schema(description = "用户ID")
    private String sysUserId;

    /**
     * 令牌
     */
    @TableField(value = "sys_token_value")
    @ApiModelProperty(value = "令牌值")
    @Schema(description = "令牌值")
    private String sysTokenValue;

    /**
     * 持续时间(秒)
     */
    @TableField(value = "sys_token_duration")
    @ApiModelProperty(value = "持续时间(秒)")
    @Schema(description = "持续时间(秒)")
    private Long sysTokenDuration;

    /**
     * 白名单IP，多个IP用逗号分隔
     */
    @TableField(value = "sys_token_whitelist_ips")
    @ApiModelProperty(value = "白名单IP")
    @Schema(description = "白名单IP")
    private String sysTokenWhitelistIps;

    /**
     * 是否开启白名单; 0: 关闭, 1: 开启
     */
    @TableField(value = "sys_token_enable_whitelist")
    @ApiModelProperty(value = "是否开启白名单; 0: 关闭, 1: 开启")
    @Schema(description = "是否开启白名单; 0: 关闭, 1: 开启")
    private Integer sysTokenEnableWhitelist;

    /**
     * 令牌状态; 0: 禁用, 1: 启用
     */
    @TableField(value = "sys_token_status")
    @ApiModelProperty(value = "令牌状态; 0: 禁用, 1: 启用")
    @Schema(description = "令牌状态; 0: 禁用, 1: 启用")
    private Integer sysTokenStatus;

    /**
     * 描述信息
     */
    @TableField(value = "sys_token_description")
    @ApiModelProperty(value = "描述信息")
    @Schema(description = "描述信息")
    private String sysTokenDescription;

    /**
     * 是否删除; 0: 未删除, 1: 已删除
     */
    @TableField(value = "sys_token_deleted")
    @ApiModelProperty(value = "是否删除; 0: 未删除, 1: 已删除")
    @Schema(description = "是否删除; 0: 未删除, 1: 已删除")
    @TableLogic(value = "0", delval = "1")
    private Integer sysTokenDeleted;
}