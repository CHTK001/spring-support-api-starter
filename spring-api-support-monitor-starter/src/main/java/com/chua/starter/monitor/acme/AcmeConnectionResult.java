package com.chua.starter.monitor.acme;

import lombok.Builder;
import lombok.Data;

/**
 * ACME 连接结果
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Builder
public class AcmeConnectionResult {

    /**
     * 是否连接成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 账户私钥 PEM
     */
    private String accountPrivateKeyPem;

    /**
     * 账户 URL
     */
    private String accountUrl;

    /**
     * 创建成功结果
     *
     * @param accountPrivateKeyPem 私钥 PEM
     * @param accountUrl           账户 URL
     * @return 结果
     */
    public static AcmeConnectionResult success(String accountPrivateKeyPem, String accountUrl) {
        return AcmeConnectionResult.builder()
                .success(true)
                .accountPrivateKeyPem(accountPrivateKeyPem)
                .accountUrl(accountUrl)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param errorMessage 错误信息
     * @return 结果
     */
    public static AcmeConnectionResult fail(String errorMessage) {
        return AcmeConnectionResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
