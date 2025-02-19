package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户表
 */
@ApiModel(description = "租户表")
@Schema(description = "租户表")
@Data
@TableName(value = "sys_tenant")
public class SysTenant {
    @TableId(value = "sys_tenant_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysTenantId;

    /**
     * 名称
     */
    @TableField(value = "sys_tenant_name")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    private String sysTenantName;

    /**
     * 账号
     */
    @TableField(value = "sys_tenant_username")
    @ApiModelProperty(value = "账号")
    @Schema(description = "账号")
    private String sysTenantUsername;

    /**
     * 密码
     */
    @TableField(value = "sys_tenant_password")
    @ApiModelProperty(value = "密码")
    @Schema(description = "密码")
    private String sysTenantPassword;

    /**
     * 签名
     */
    @TableField(value = "sys_tenant_sign")
    @ApiModelProperty(value = "签名")
    @Schema(description = "签名")
    private String sysTenantSign;

    /**
     * 公司
     */
    @TableField(value = "sys_tenant_corporation")
    @ApiModelProperty(value = "公司")
    @Schema(description = "公司")
    private String sysTenantCorporation;

    /**
     * 是否删除; 0:正常
     */
    @TableField(value = "sys_tenant_delete")
    @ApiModelProperty(value = "是否删除; 0:正常")
    @Schema(description = "是否删除; 0:正常")
    private @TableLogic(value = "0", delval = "1") Integer sysTenantDelete;

    /**
     * 是否禁用; 0:正常
     */
    @TableField(value = "sys_tenant_status")
    @ApiModelProperty(value = "是否禁用; 0:正常")
    @Schema(description = "是否禁用; 0:正常")
    private Integer sysTenantStatus;

    /**
     * 联系人
     */
    @TableField(value = "sys_tenant_contact")
    @ApiModelProperty(value = "联系人")
    @Schema(description = "联系人")
    private String sysTenantContact;

    /**
     * 手机号
     */
    @TableField(value = "sys_tenant_phone")
    @ApiModelProperty(value = "手机号")
    @Schema(description = "手机号")
    private String sysTenantPhone;

    /**
     * 地址
     */
    @TableField(value = "sys_tenant_address")
    @ApiModelProperty(value = "地址")
    @Schema(description = "地址")
    private String sysTenantAddress;

    /**
     * 邮件
     */
    @TableField(value = "sys_tenant_email")
    @ApiModelProperty(value = "邮件")
    @Schema(description = "邮件")
    private String sysTenantEmail;

    /**
     * 唯一编码
     */
    @TableField(value = "sys_tenant_code")
    @ApiModelProperty(value = "唯一编码")
    @Schema(description = "唯一编码")
    private String sysTenantCode;

    /**
     * 租户对于配置唯一ID
     */
    @TableField(value = "sys_tenant_gid")
    @ApiModelProperty(value = "租户对于配置唯一ID")
    @Schema(description = "租户对于配置唯一ID")
    private String sysTenantGid;

    /**
     * 创建人姓名
     */
    @TableField(value = "create_name")
    @ApiModelProperty(value = "创建人姓名")
    @Schema(description = "创建人姓名")
    private String createName;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    @ApiModelProperty(value = "创建人")
    @Schema(description = "创建人")
    private Integer createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 更新人姓名
     */
    @TableField(value = "update_name")
    @ApiModelProperty(value = "更新人姓名")
    @Schema(description = "更新人姓名")
    private String updateName;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    @ApiModelProperty(value = "更新人")
    @Schema(description = "更新人")
    private Integer updateBy;
}