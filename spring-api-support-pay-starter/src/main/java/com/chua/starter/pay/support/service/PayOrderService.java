package com.chua.starter.pay.support.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.pay.support.entity.PayMerchantOrder;
import com.chua.starter.pay.support.handler.CallbackNotificationParser;
import com.chua.starter.pay.support.pojo.PayRefundRequest;
import com.chua.starter.pay.support.pojo.WechatOrderCallbackResponse;
import com.chua.starter.pay.support.result.PayRefundResponse;

/**
 * 支付订单
 *
 * @author CH
 * @since 2024/12/30
 */
public interface PayOrderService extends RpcPayOrderService {


    /**
     * 支付订单微信回调
     *
     * @param parser 解析器
     * @return 结果
     */
    WechatOrderCallbackResponse notifyOrder(CallbackNotificationParser parser);


    /**
     * 退款订单微信回调
     *
     * @param wechatOrderCallbackRequest 回调请求
     * @return 结果
     */
    WechatOrderCallbackResponse refundOrder(CallbackNotificationParser wechatOrderCallbackRequest);

    /**
     * 退款
     *
     * @param refundRequest 退款请求
     * @return 结果
     */
    ReturnResult<PayRefundResponse> refund(PayRefundRequest refundRequest);


    /**
     * 关闭订单
     *
     * @param refundRequest 退款请求
     * @return 结果
     */
    ReturnResult<PayRefundResponse> cancel(PayRefundRequest refundRequest);

    /**
     * 根据订单获取订单记录
     *
     * @param payMerchantOrderCode 订单编号
     * @param userId               用户ID
     * @return 结果
     */
    PayMerchantOrder getOne(String payMerchantOrderCode, String userId);

    /**
     * 获取最新的未支付订单
     *
     * @param userId 用户ID
     * @return 结果
     */
    ReturnResult<String> newestNoPayOrder(String userId);
}
