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
 * ACME 证书信息
 *
 * 作者: CH
 * 创建时间: 2025-10-17
 * 版本: 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_acme_certificate")
@ApiModel(description = "ACME 证书信息")
@Schema(description = "ACME 证书信息")
public class MonitorAcmeCertificate extends SysBase {

    /** 主键ID */
    @TableId(value = "acme_cert_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    private Integer acmeCertId;

    /** 账户ID */
    @TableField("acme_account_id")
    @ApiModelProperty("账户ID")
    @Schema(description = "账户ID")
    private Integer acmeAccountId;

    /** 主域名 */
    @TableField("acme_cert_primary_domain")
    @ApiModelProperty("主域名")
    @Schema(description = "主域名")
    private String acmeCertPrimaryDomain;

    /** 备用域名(SAN)，逗号分隔 */
    @TableField("acme_cert_san")
    @ApiModelProperty("备用域名(SAN)，逗号分隔")
    @Schema(description = "备用域名(SAN)，逗号分隔")
    private String acmeCertSan;

    /** 挑战类型（HTTP-01/DNS-01） */
    @TableField("acme_cert_challenge_type")
    @ApiModelProperty("挑战类型（HTTP-01/DNS-01）")
    @Schema(description = "挑战类型（HTTP-01/DNS-01）")
    private String acmeCertChallengeType;

    /** 证书状态（pending, valid, expired, revoked, failed） */
    @TableField("acme_cert_status")
    @ApiModelProperty("证书状态")
    @Schema(description = "证书状态")
    private String acmeCertStatus;

    /** 有效期起 */
    @TableField("acme_cert_not_before")
    @ApiModelProperty("有效期起")
    @Schema(description = "有效期起")
    private String acmeCertNotBefore;

    /** 有效期止 */
    @TableField("acme_cert_not_after")
    @ApiModelProperty("有效期止")
    @Schema(description = "有效期止")
    private String acmeCertNotAfter;

    /** 计划续签日期（到期前N天） */
    @TableField("acme_cert_renew_at")
    @ApiModelProperty("计划续签日期")
    @Schema(description = "计划续签日期")
    private String acmeCertRenewAt;

    /** 证书PEM（含链） */
    @TableField("acme_cert_chain_pem")
    @ApiModelProperty("证书PEM（含链）")
    @Schema(description = "证书PEM（含链）")
    private String acmeCertChainPem;

    /** 私钥PEM */
    @TableField("acme_cert_key_pem")
    @ApiModelProperty("私钥PEM")
    @Schema(description = "私钥PEM")
    private String acmeCertKeyPem;

    /** 最近错误 */
    @TableField("acme_cert_last_error")
    @ApiModelProperty("最近错误")
    @Schema(description = "最近错误")
    private String acmeCertLastError;

    /** 部署模式：local(本机自动)/manual(手动) */
    @TableField("acme_cert_deploy_mode")
    @ApiModelProperty("部署模式: local(本机自动)/manual(手动)")
    @Schema(description = "部署模式: local(本机自动)/manual(手动)")
    private String acmeCertDeployMode;

    /** 本机模式 webroot 路径 */
    @TableField("acme_cert_webroot_path")
    @ApiModelProperty("本机模式webroot路径")
    @Schema(description = "本机模式webroot路径")
    private String acmeCertWebrootPath;

    /**
     * 状态样式类型（前端 Tag 组件使用）
     */
    @ApiModelProperty("状态样式类型")
    @Schema(description = "状态样式类型: success/warning/danger/info")
    public String getStatusType() {
        if (acmeCertStatus == null) {
            return "";
        }
        return switch (acmeCertStatus) {
            case "pending", "validating" -> "warning";  // 待检验/检验中 - 黄色
            case "valid" -> "success";                  // 有效 - 绿色
            case "expired", "failed" -> "danger";       // 过期/失败 - 红色
            case "revoked" -> "info";                   // 吐销 - 灰色
            default -> "";
        };
    }
}
