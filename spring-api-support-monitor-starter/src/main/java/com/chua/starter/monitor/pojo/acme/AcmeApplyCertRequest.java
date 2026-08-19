package com.chua.starter.monitor.pojo.acme;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 申请证书请求，包含账户、域名（主域名与 SAN）、挑战类型、部署模式及 SSH 远程部署信息。
 *
 * @since 2025-10-17
 * @author CH
 */
@Data
@ApiModel(description = "申请证书请求")
@Schema(description = "申请证书请求")
public class AcmeApplyCertRequest {

    /**
     * ACME 账户 ID，关联已保存的 ACME 账户
     */
    @ApiModelProperty("ACME账户ID")
    @Schema(description = "ACME账户ID")
    /** AcmeaccountID */
    private Integer acmeAccountId;

    /**
     * 主域名（必填）
     * <p>域名格式要求：</p>
     * <ul>
     *   <li>普通域名：example.com、www.example.com</li>
     *   <li>通配符域名：*.example.com（仅支持 DNS-01 验证）</li>
     * </ul>
     * <p>域名注册商推荐：</p>
     * <ul>
     *   <li>国内：<a href="https://wanwang.aliyun.com/"阿里云万网</a>、<a href="https://dnspod.cloud.tencent.com/"腾讯云DNSPod</a></li>
     *   <li>国际：<a href="https://www.namesilo.com/">NameSilo</a>、<a href="https://www.cloudflare.com/">Cloudflare</a>、<a href="https://www.godaddy.com/">GoDaddy</a></li>
     * </ul>
     */
    @ApiModelProperty(value = "主域名", notes = "支持普通域名(example.com)和通配符域名(*.example.com)，通配符仅支持DNS-01验证")
    @Schema(description = "主域名。支持普通域名(example.com)和通配符域名(*.example.com)，通配符仅支持DNS-01验证。" +
            "域名注册商推荐: 阿里云万网(https://wanwang.aliyun.com)、腾讯云DNSPod(https://dnspod.cloud.tencent.com)、" +
            "NameSilo(https://www.namesilo.com)、Cloudflare(https://www.cloudflare.com)")
    /** Primarydomain */
    private String primaryDomain;

    /**
     * 备用域名列表（SAN，可选）
     * <p>用于一张证书保护多个域名，格式同主域名</p>
     * <p>示例：["www.example.com", "api.example.com", "admin.example.com"]</p>
     */
    @ApiModelProperty(value = "备用域名列表(SAN)", notes = "可选，用于一张证书保护多个域名，如 www.example.com, api.example.com")
    @Schema(description = "备用域名列表(SAN)，可选。用于一张证书保护多个域名，如 [\"www.example.com\", \"api.example.com\"]")
    /** SANdomains */
    private List<String> sanDomains;

    /**
     * 挑战类型，取值 HTTP-01 或 DNS-01，通配符域名仅支持 DNS-01
     */
    @ApiModelProperty("挑战类型: HTTP-01 或 DNS-01")
    @Schema(description = "挑战类型: HTTP-01 或 DNS-01")
    /** Challenge类型 */
    private String challengeType;

    /**
     * 部署模式，取值 local（本机）/ssh（远程）/manual（手动），默认 manual
     */
    @ApiModelProperty("部署模式: local(本机)/ssh(远程)/manual(手动)，默认manual")
    @Schema(description = "部署模式: local(本机)/ssh(远程)/manual(手动)，默认manual")
    /** Deploy模式 */
    private String deployMode;

    /**
     * webroot 验证路径，如 /var/www/html，本机/SSH 模式时使用
     */
    @ApiModelProperty("webroot路径，如 /var/www/html（ssh模式使用）")
    @Schema(description = "webroot路径，如 /var/www/html（ssh模式使用）")
    /** Webroot路径 */
    private String webrootPath;

    /**
     * SSH 远程部署相关字段：SSH 主机地址
     */
    @ApiModelProperty("SSH主机地址")
    @Schema(description = "SSH主机地址")
    /** SSH主机 */
    private String sshHost;

    /**
     * SSH 端口，默认 22
     */
    @ApiModelProperty("SSH端口，默认22")
    @Schema(description = "SSH端口，默认22")
    /** SSH端口 */
    private Integer sshPort;

    /**
     * SSH 用户名
     */
    @ApiModelProperty("SSH用户名")
    @Schema(description = "SSH用户名")
    /** SSHusername */
    private String sshUsername;

    /**
     * SSH 密码
     */
    @ApiModelProperty("SSH密码")
    @Schema(description = "SSH密码")
    /** SSH密码 */
    private String sshPassword;
}
