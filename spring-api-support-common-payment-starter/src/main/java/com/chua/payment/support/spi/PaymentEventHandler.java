package com.chua.payment.support.spi;

import com.chua.payment.support.event.PaymentEvent;

/**
 * 支付事件处理器 SPI
 * <p>
 * 所有实现类会在支付事件发生时被逐一通知。
 * 可通过 {@code @Component} 注册自定义实现，框架默认提供 {@link DefaultPaymentEventHandler}。
 * </p>
 *
 * @author CH
 */
@FunctionalInterface
public interface PaymentEventHandler {

    /**
     * 处理支付事件
     *
     * @param event 支付事件（包括支付成功、退款成功、订单创建、回调接收等）
     */
    void onPaymentEvent(PaymentEvent event);
}
