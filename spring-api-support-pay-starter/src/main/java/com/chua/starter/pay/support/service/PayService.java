package com.chua.starter.pay.support.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.pay.support.pojo.PaySignCreateRequest;
import com.chua.starter.pay.support.pojo.PayUpdateOrderRequest;
import com.chua.starter.pay.support.result.PayOrderResponse;
import com.chua.starter.pay.support.result.PaySignResponse;

/**
 * 支付服务
 *
 * @author CH
 */
public interface PayService extends RpcPayService {

    /**
     * 创建签名
     *
     * @param paySignCreateRequest 订单请求
     * @return 结果
     */
    ReturnResult<PaySignResponse> createSign(PaySignCreateRequest paySignCreateRequest);

    /**
     * 更新订单
     *
     * @param request 订单请求
     * @param userId  用户ID
     * @return 结果
     */
    ReturnResult<PayOrderResponse> updateOrder(PayUpdateOrderRequest request, String userId);
}
