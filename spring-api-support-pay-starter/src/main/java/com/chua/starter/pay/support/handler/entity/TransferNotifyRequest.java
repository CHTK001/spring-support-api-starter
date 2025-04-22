package com.chua.starter.pay.support.handler.entity;

import lombok.Data;

/**
 * 微信支付转账回调通知请求体
 */
@Data
public class TransferNotifyRequest {
    /**
     * 通知ID - 微信支付系统生成的唯一通知ID
     * 示例值: "EV-2018022511223320873"
     */
    private String id;

    /**
     * 通知创建时间 - RFC 3339格式
     * 示例值: "2018-06-08T10:34:56+08:00"
     */
    private String create_time;

    /**
     * 通知类型 - 固定为"TRANSFER.BATCH.STATUS"
     */
    private String event_type;

    /**
     * 通知数据类型 - 固定为"encrypt-resource"
     */
    private String resource_type;

    /**
     * 通知数据 - 包含加密的转账结果
     */
    private Resource resource;

    /**
     * 加密资源数据
     */
    @Data
    public static class Resource {
        /**
         * 加密算法类型 - 固定为"AEAD_AES_256_GCM"
         */
        private String algorithm;

        /**
         * 数据密文 - Base64编码的加密数据
         */
        private String ciphertext;

        /**
         * 附加数据 - 固定为"transfer"
         */
        private String associated_data;

        /**
         * 原始类型 - 固定为"transferbatch"
         */
        private String original_type;

        /**
         * 随机串 - 加密使用的随机字符串
         */
        private String nonce;
    }
}