package com.chua.starter.monitor.acme;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ACME 证书申请结果
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Builder
public class AcmeCertificateResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 证书 PEM（含证书链）
     */
    private String certificatePem;

    /**
     * 私钥 PEM
     */
    private String privateKeyPem;

    /**
     * 证书有效期开始时间
     */
    private LocalDateTime notBefore;

    /**
     * 证书有效期结束时间
     */
    private LocalDateTime notAfter;

    /**
     * 创建成功结果
     *
     * @param certificatePem 证书 PEM
     * @param privateKeyPem  私钥 PEM
     * @param notBefore      有效期开始
     * @param notAfter       有效期结束
     * @return 结果
     */
    public static AcmeCertificateResult success(String certificatePem, String privateKeyPem,
                                                 LocalDateTime notBefore, LocalDateTime notAfter) {
        return AcmeCertificateResult.builder()
                .success(true)
                .certificatePem(certificatePem)
                .privateKeyPem(privateKeyPem)
                .notBefore(notBefore)
                .notAfter(notAfter)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param errorMessage 错误信息
     * @return 结果
     */
    public static AcmeCertificateResult fail(String errorMessage) {
        return AcmeCertificateResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
