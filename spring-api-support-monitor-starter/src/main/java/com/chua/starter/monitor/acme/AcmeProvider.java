package com.chua.starter.monitor.acme;

import com.chua.common.support.annotations.Spi;

import java.util.List;

/**
 * ACME 证书服务提供者接口
 * 通过 SPI 机制加载不同的 ACME 客户端实现
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-09
 */
@Spi("acme4j")
public interface AcmeProvider {

    /**
     * 连接到 ACME 服务器
     *
     * @param serverUrl ACME 服务器地址
     * @param email     账户邮箱
     * @param privateKeyPem 私钥 PEM（可选，为空则自动生成）
     * @return 连接结果
     */
    AcmeConnectionResult connect(String serverUrl, String email, String privateKeyPem);

    /**
     * 获取域名验证信息
     *
     * @param domains       域名列表
     * @param challengeType 挑战类型（HTTP-01 / DNS-01）
     * @return 验证信息列表
     */
    List<AcmeValidationInfo> getValidationInfo(List<String> domains, String challengeType);

    /**
     * 申请证书
     *
     * @param domains       域名列表
     * @param challengeType 挑战类型
     * @return 证书结果
     */
    AcmeCertificateResult requestCertificate(List<String> domains, String challengeType);

    /**
     * 续签证书
     *
     * @param domains       域名列表
     * @param challengeType 挑战类型
     * @return 证书结果
     */
    AcmeCertificateResult renewCertificate(List<String> domains, String challengeType);

    /**
     * 吊销证书
     *
     * @param certificatePem 证书 PEM
     * @return 是否成功
     */
    boolean revokeCertificate(String certificatePem);

    /**
     * 获取账户私钥 PEM
     *
     * @return 私钥 PEM
     */
    String getAccountPrivateKeyPem();

    /**
     * 关闭连接
     */
    void close();
}
