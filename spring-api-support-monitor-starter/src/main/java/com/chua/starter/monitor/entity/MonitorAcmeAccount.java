package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ACME 账户信息
 *
 * 作者: CH
 * 创建时间: 2025-10-17
 * 版本: 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_acme_account")
@ApiModel(description = "ACME 账户信息")
@Schema(description = "ACME 账户信息")
public class MonitorAcmeAccount extends SysBase {

    /** 主键ID */
    @TableId(value = "acme_account_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    private Integer acmeAccountId;

    /** 帐号邮箱 */
    @TableField("acme_account_email")
    @ApiModelProperty("账号邮箱")
    @Schema(description = "账号邮箱")
    private String acmeAccountEmail;

    /** ACME 服务端点（例如：https://acme-v02.api.letsencrypt.org/directory） */
    @TableField("acme_account_server")
    @ApiModelProperty("ACME 服务端点")
    @Schema(description = "ACME 服务端点")
    private String acmeAccountServer;

    /** 账户状态（valid, deactivated, revoked 等） */
    @TableField("acme_account_status")
    @ApiModelProperty("账户状态")
    @Schema(description = "账户状态")
    private String acmeAccountStatus;

    /** 账户私钥（PEM）- 可选，若外部托管则为空 */
    @TableField("acme_account_private_key_pem")
    @ApiModelProperty("账户私钥（PEM）")
    @Schema(description = "账户私钥（PEM）")
    private String acmeAccountPrivateKeyPem;

    /** 备注 */
    @TableField("acme_account_remark")
    @ApiModelProperty("备注")
    @Schema(description = "备注")
    private String acmeAccountRemark;

    /** EAB Key ID (ZeroSSL 等需要 External Account Binding 的 CA) */
    @TableField("acme_account_eab_kid")
    @ApiModelProperty("EAB Key ID")
    @Schema(description = "EAB Key ID (ZeroSSL 等需要)")
    private String acmeAccountEabKid;

    /** EAB HMAC Key (Base64 编码) */
    @TableField("acme_account_eab_hmac_key")
    @ApiModelProperty("EAB HMAC Key")
    @Schema(description = "EAB HMAC Key (Base64 编码)")
    private String acmeAccountEabHmacKey;
}
