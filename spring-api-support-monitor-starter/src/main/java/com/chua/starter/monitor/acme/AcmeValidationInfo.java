package com.chua.starter.monitor.acme;

import lombok.Builder;
import lombok.Data;

/**
 * ACME 域名验证信息
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Builder
public class AcmeValidationInfo {

    /**
     * 域名
     */
    private String domain;

    /**
     * 挑战类型（HTTP-01 / DNS-01）
     */
    private String challengeType;

    /**
     * 令牌
     */
    private String token;

    /**
     * 密钥授权
     */
    private String keyAuthorization;

    /**
     * HTTP 挑战路径（HTTP-01）
     * 例如：/.well-known/acme-challenge/{token}
     */
    private String httpPath;

    /**
     * HTTP 挑战内容（HTTP-01）
     * 需要在 httpPath 返回此内容
     */
    private String httpContent;

    /**
     * DNS 记录名称（DNS-01）
     * 例如：_acme-challenge.example.com
     */
    private String dnsRecordName;

    /**
     * DNS 记录值（DNS-01）
     * TXT 记录的值
     */
    private String dnsRecordValue;

    /**
     * 验证状态
     */
    private String status;
}
