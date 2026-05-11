package com.chua.payment.support.service;

import java.util.Map;

/**
 * 第三方支付异步通知服务
 */
public interface PaymentNotifyService {

    void handleWechatPayNotify(Long channelId,
                               String serialNumber,
                               String timestamp,
                               String nonce,
                               String signature,
                               String signType,
                               String body);

    void handleWechatRefundNotify(Long channelId,
                                  String serialNumber,
                                  String timestamp,
                                  String nonce,
                                  String signature,
                                  String signType,
                                  String body);

    void handleAlipayPayNotify(Long channelId, Map<String, String> params);

    /**
     * 处理支付宝钱包异步通知（充值/转账/提现）
     *
     * @param orderType 订单类型：RECHARGE / TRANSFER / WITHDRAW
     * @param orderNo   订单号
     * @param params    支付宝回调参数
     */
    void handleAlipayWalletNotify(String orderType, String orderNo, Map<String, String> params);

    /**
     * 处理微信钱包异步通知（充值/转账/提现）
     *
     * @param orderType     订单类型：RECHARGE / TRANSFER / WITHDRAW
     * @param orderNo       订单号
     * @param serialNumber  微信平台序列号
     * @param timestamp     时间戳
     * @param nonce         随机数
     * @param signature     签名
     * @param signType      签名类型
     * @param body          通知 body（AES-GCM 密文）
     */
    void handleWechatWalletNotify(String orderType, String orderNo,
                                  String serialNumber,
                                  String timestamp,
                                  String nonce,
                                  String signature,
                                  String signType,
                                  String body);
}
