package com.chua.starter.pay.support.checker;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.pay.support.pojo.PayOrderRequest;

/**
 * 金额校验
 *
 * @author CH
 */
@Spi("uncertain")
public class UncertainMoneyChecker implements MoneyChecker {

    @Override
    public ReturnResult<String> check(PayOrderRequest request) {
        return ReturnResult.SUCCESS;
    }
}
