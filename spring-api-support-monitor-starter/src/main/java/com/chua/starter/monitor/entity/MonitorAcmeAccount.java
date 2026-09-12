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
* ACME 账户信息，保存 ACME CA（Let's Encrypt / zerossl 等）账户的邮箱、服务端点、私钥与 EAB 扩展。
*
* @since 2025-10-17
* @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_acme_account")
@ApiModel(description = "ACME 账户信息")
@Schema(description = "ACME 账户信息")
public class MonitorAcmeAccount extends SysBase {

    /** 主键标识 */
    @TableId(value = "acme_account_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    /** acmeaccountid */
    private Integer acmeAccountId;

    /** 帐号邮箱 */
    @TableField("acme_account_email")
    @ApiModelProperty("账号邮箱")
    @Schema(description = "账号邮箱")
    /** Acmeaccountemail */
    private String acmeAccountEmail;

    /** ACME 服务端点（例如：https://acme-v02.api.letsencrypt.org/directory） */
    @TableField("acme_account_server")
    @ApiModelProperty("ACME 服务端点")
    @Schema(description = "ACME 服务端点")
    /** Acmeaccount服务器 */
    private String acmeAccountServer;

    /** 证书服务商类型（acme=ACME协议，CAS=阿里云CAS） */
    @TableField("acme_account_provider_type")
    @ApiModelProperty("证书服务商类型(acme/cas)")
    @Schema(description = "证书服务商类型(acme=ACME协议，cas=阿里云CAS)")
    /** 服务商类型 */
    private String acmeAccountProviderType;

    /** 账户状态（valid, deactivated, revoked 等） */
    @TableField("acme_account_status")
    @ApiModelProperty("账户状态")
    @Schema(description = "账户状态")
    /** Acmeaccount状态 */
    private String acmeAccountStatus;

    /** 账户私钥（PEM）- 可选，若外部托管则为空 */
    @TableField("acme_account_private_key_pem")
    @ApiModelProperty("账户私钥（PEM）")
    @Schema(description = "账户私钥（PEM）")
    /** Acmeaccount私有密钥PEM */
    private String acmeAccountPrivateKeyPem;

    /** 备注 */
    @TableField("acme_account_remark")
    @ApiModelProperty("备注")
    @Schema(description = "备注")
    /** Acmeaccountremark */
    private String acmeAccountRemark;

    /** EAB 键 标识 (zerossl 等需要 外部 账户 Binding 的 CA) */
    @TableField("acme_account_eab_kid")
    @ApiModelProperty("EAB Key ID")
    @Schema(description = "EAB Key ID (ZeroSSL 等需要)")
    /** acmeaccounteabkid */
    private String acmeAccountEabKid;

    /** EAB HMAC 键 (基础64 编码) */
    @TableField("acme_account_eab_hmac_key")
    @ApiModelProperty("EAB HMAC Key")
    @Schema(description = "EAB HMAC Key (Base64 编码)")
    /** acmeaccounteabhmac密钥 */
    private String acmeAccountEabHmacKey;
}
