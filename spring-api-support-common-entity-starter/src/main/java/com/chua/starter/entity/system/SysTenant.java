package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 租户表
 *
 * @author CH
 * @since 2024/12/06
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "租户表")
@Data
@TableName(value = "sys_tenant")
public class SysTenant extends SysBase {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    @TableId(value = "sys_tenant_id", type = IdType.AUTO)
    @Schema(description = "租户ID")
    private Integer sysTenantId;

    /**
     * 名称
     */
    @TableField(value = "sys_tenant_name")
    @Schema(description = "名称")
    private String sysTenantName;

    /**
     * 账号
     */
    @TableField(value = "sys_tenant_username")
    @Schema(description = "账号")
    private String sysTenantUsername;

    /**
     * 密码
     */
    @TableField(value = "sys_tenant_password")
    @Schema(description = "密码")
    private String sysTenantPassword;

    /**
     * 签名
     */
    @TableField(value = "sys_tenant_sign")
    @Schema(description = "签名")
    private String sysTenantSign;

    /**
     * 公司
     */
    @TableField(value = "sys_tenant_corporation")
    @Schema(description = "公司")
    private String sysTenantCorporation;

    /**
     * 是否删除; 0:正常
     */
    @TableField(value = "sys_tenant_delete")
    @Schema(description = "是否删除; 0:正常")
    @TableLogic(value = "0", delval = "1")
    private Integer sysTenantDelete;

    /**
     * 是否禁用; 0:正常
     */
    @TableField(value = "sys_tenant_status")
    @Schema(description = "是否禁用; 0:正常")
    private Integer sysTenantStatus;

    /**
     * 联系人
     */
    @TableField(value = "sys_tenant_contact")
    @Schema(description = "联系人")
    private String sysTenantContact;

    /**
     * 手机号
     */
    @TableField(value = "sys_tenant_phone")
    @Schema(description = "手机号")
    private String sysTenantPhone;

    /**
     * 地址
     */
    @TableField(value = "sys_tenant_address")
    @Schema(description = "地址")
    private String sysTenantAddress;

    /**
     * 邮件
     */
    @TableField(value = "sys_tenant_email")
    @Schema(description = "邮件")
    private String sysTenantEmail;

    /**
     * 唯一编码
     */
    @TableField(value = "sys_tenant_code")
    @Schema(description = "唯一编码")
    private String sysTenantCode;

    /**
     * 租户对于配置唯一ID
     */
    @TableField(value = "sys_tenant_gid")
    @Schema(description = "租户对于配置唯一ID")
    private String sysTenantGid;

    /**
     * 租户访问地址
     */
    @TableField(value = "sys_tenant_home_url")
    @Schema(description = "租户访问地址")
    private String sysTenantHomeUrl;

    /**
     * 备注
     */
    @TableField(value = "sys_tenant_remark")
    @Schema(description = "备注")
    private String sysTenantRemark;
}
