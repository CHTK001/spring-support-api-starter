package com.chua.starter.pay.support.listen;

import com.chua.starter.pay.support.annotations.OnPayListener;
import com.chua.starter.pay.support.entity.PayMerchantOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;

/**
 * 订单监听
 *
 * @author CH
 */
@Slf4j
@SpringBootConfiguration
public class OrderListener {


    /**
     * 订单
     *
     * @param payMerchantOrder 订单
     */
    @OnPayListener("*")
    public void order(PayMerchantOrder payMerchantOrder) {
        log.info("订单：{}", payMerchantOrder.getPayMerchantCode());
    }
}
