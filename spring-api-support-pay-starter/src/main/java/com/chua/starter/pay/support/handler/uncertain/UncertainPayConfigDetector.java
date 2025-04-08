package com.chua.starter.pay.support.handler.uncertain;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.pay.support.emuns.TradeType;
import com.chua.starter.pay.support.entity.PayMerchant;
import com.chua.starter.pay.support.handler.PayConfigDetector;

/**
 * 不确定支付配置
 *
 * @author CH
 */
@Spi("uncertain")
public class UncertainPayConfigDetector implements PayConfigDetector {
    @Override
    public ReturnResult check(PayMerchant payMerchant, TradeType tradeType) {
        return ReturnResult.success();
    }
}
