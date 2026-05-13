package com.chua.payment.support.service;

import com.chua.payment.support.channel.WithdrawRequest;

/**
 * 钱包打款服务
 */
public interface WalletPayoutService {

    /**
     * 执行支付宝打款，转账到用户支付宝账号
     *
     * @param request 提现请求（含用户支付宝账号信息）
     * @param orderNo 钱包订单号
     * @return true=打款成功, false=打款失败
     */
    boolean payoutToAlipay(WithdrawRequest request, String orderNo);
}
