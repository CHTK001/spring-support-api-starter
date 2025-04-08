package com.chua.starter.pay.support.handler.uncertain;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.pay.support.entity.PayMerchantOrder;
import com.chua.starter.pay.support.handler.PayOrderCreator;
import com.chua.starter.pay.support.result.PayOrderResponse;

/**
 * 不确定支付订单创建器
 *
 * @author CH
 */
@Spi("uncertain")
public class UncertainPayOrderCreator implements PayOrderCreator {
    @Override
    public ReturnResult<PayOrderResponse> handle(PayMerchantOrder payMerchantOrder) {
        return ReturnResult.ok(new PayOrderResponse());
    }
}
