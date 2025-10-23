package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 应用密钥表（AK/SK管理）
 *
 * @author CH
 * @since 2025/10/23
 */
@ApiModel(description = "应用密钥表")
@Schema(description = "应用密钥表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_app_key")
public class SysAppKey extends SysBase implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "sys_app_key_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    @Schema(description = "主键ID")
    private Integer sysAppKeyId;

    /**
     * 应用ID（AccessKey/AppId）
     */
    @TableField(value = "sys_app_key_app_id")
    @ApiModelProperty(value = "应用ID（AccessKey/AppId）")
    @Schema(description = "应用ID（AccessKey/AppId）")
    @NotEmpty(message = "应用ID不能为空")
    @Size(max = 255, message = "应用ID最大长度要小于 255")
    private String sysAppKeyAppId;

    /**
     * 应用密钥（SecretKey）
     */
    @TableField(value = "sys_app_key_secret")
    @ApiModelProperty(value = "应用密钥（SecretKey）")
    @Schema(description = "应用密钥（SecretKey）")
    @NotEmpty(message = "应用密钥不能为空")
    @Size(max = 255, message = "应用密钥最大长度要小于 255")
    private String sysAppKeySecret;

    /**
     * 应用名称
     */
    @TableField(value = "sys_app_key_name")
    @ApiModelProperty(value = "应用名称")
    @Schema(description = "应用名称")
    @Size(max = 255, message = "应用名称最大长度要小于 255")
    private String sysAppKeyName;

    /**
     * 关联用户ID
     */
    @TableField(value = "sys_user_id")
    @ApiModelProperty(value = "关联用户ID")
    @Schema(description = "关联用户ID")
    private Integer sysUserId;

    /**
     * 状态（0：启用，1：禁用）
     */
    @TableField(value = "sys_app_key_status")
    @ApiModelProperty(value = "状态（0：启用，1：禁用）")
    @Schema(description = "状态（0：启用，1：禁用）")
    private Integer sysAppKeyStatus;

    /**
     * 是否删除（0：正常，1：已删除）
     */
    @TableField(value = "sys_app_key_delete")
    @ApiModelProperty(value = "是否删除（0：正常，1：已删除）")
    @Schema(description = "是否删除（0：正常，1：已删除）")
    @TableLogic
    private Integer sysAppKeyDelete;

    /**
     * 过期时间（Unix时间戳，为空表示永久有效）
     */
    @TableField(value = "sys_app_key_expire_time")
    @ApiModelProperty(value = "过期时间（Unix时间戳）")
    @Schema(description = "过期时间（Unix时间戳）")
    private Long sysAppKeyExpireTime;

    /**
     * IP白名单（多个IP用逗号分隔）
     */
    @TableField(value = "sys_app_key_ip_whitelist")
    @ApiModelProperty(value = "IP白名单")
    @Schema(description = "IP白名单")
    @Size(max = 1000, message = "IP白名单最大长度要小于 1000")
    private String sysAppKeyIpWhitelist;

    /**
     * 备注
     */
    @TableField(value = "sys_app_key_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 500, message = "备注最大长度要小于 500")
    private String sysAppKeyRemark;

    /**
     * 最后使用时间
     */
    @TableField(value = "sys_app_key_last_used_time")
    @ApiModelProperty(value = "最后使用时间")
    @Schema(description = "最后使用时间")
    private Long sysAppKeyLastUsedTime;

    /**
     * 使用次数
     */
    @TableField(value = "sys_app_key_use_count")
    @ApiModelProperty(value = "使用次数")
    @Schema(description = "使用次数")
    private Integer sysAppKeyUseCount;
}

