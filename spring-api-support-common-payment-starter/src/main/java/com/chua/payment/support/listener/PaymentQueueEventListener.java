package com.chua.payment.support.listener;

import com.chua.payment.support.event.*;
import com.chua.starter.queue.annotation.QueueEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 支付队列事件监听器示例
 * 从队列接收支付事件并处理
 *
 * 使用场景：
 * 1. 跨服务事件传递
 * 2. 异步事件处理
 * 3. 事件持久化和重试
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.payment.queue", name = "enabled", havingValue = "true")
public class PaymentQueueEventListener {

    /**
     * 从队列接收订单创建事件
     */
    @QueueEventListener(
        value = "payment.order.created",
        eventType = OrderCreatedEvent.class,
        group = "payment-service",
        concurrency = 3
    )
    public void handleOrderCreatedFromQueue(OrderCreatedEvent event) {
        log.info("[Queue] 收到订单创建事件: orderNo={}, userId={}, amount={}",
            event.getOrderNo(), event.getUserId(), event.getOrderAmount());

        // 处理业务逻辑，例如：
        // - 发送通知
        // - 更新统计
        // - 触发其他业务流程
    }

    /**
     * 从队列接收支付成功事件
     */
    @QueueEventListener(
        value = "payment.order.paid",
        eventType = PaymentSuccessEvent.class,
        group = "payment-service",
        concurrency = 5
    )
    public void handlePaymentSuccessFromQueue(PaymentSuccessEvent event) {
        log.info("[Queue] 收到支付成功事件: orderNo={}, userId={}, paidAmount={}",
            event.getOrderNo(), event.getUserId(), event.getPaidAmount());

        // 处理业务逻辑，例如：
        // - 发送支付成功通知
        // - 更新订单状态
        // - 触发发货流程
    }

    /**
     * 从队列接收退款成功事件
     */
    @QueueEventListener(
        value = "payment.refund.success",
        eventType = RefundSuccessEvent.class,
        group = "payment-service"
    )
    public void handleRefundSuccessFromQueue(RefundSuccessEvent event) {
        log.info("[Queue] 收到退款成功事件: orderNo={}, refundNo={}, refundAmount={}",
            event.getOrderNo(), event.getRefundNo(), event.getRefundAmount());

        // 处理业务逻辑，例如：
        // - 发送退款成功通知
        // - 更新订单状态
        // - 触发退货流程
    }

    /**
     * 从队列接收回调失败事件（手动确认模式）
     */
    @QueueEventListener(
        value = "payment.notify.failed",
        eventType = PaymentNotifyFailedEvent.class,
        group = "payment-service",
        autoAck = false
    )
    public void handleNotifyFailedFromQueue(PaymentNotifyFailedEvent event,
                                           com.chua.starter.queue.Acknowledgment ack) {
        try {
            log.error("[Queue] 收到回调失败事件: notifyType={}, orderNo={}, errorType={}, errorMessage={}",
                event.getNotifyType(), event.getOrderNo(), event.getErrorType(), event.getErrorMessage());

            // 处理业务逻辑，例如：
            // - 发送告警通知
            // - 记录错误日志
            // - 触发人工介入流程

            ack.acknowledge();
        } catch (Exception e) {
            log.error("[Queue] 处理回调失败事件异常", e);
            ack.nack(true); // 重新入队
        }
    }
}
