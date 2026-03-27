package com.chua.payment.support.task;

import com.chua.payment.support.service.PaymentOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 支付订单自动关单任务。
 * <p>
 * 该类仅在 payment 使用 internal 调度引擎时启用；job 模式下由 job-starter 统一负责调度。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.payment.scheduler", name = "engine", havingValue = "internal", matchIfMissing = true)
public class PaymentOrderAutoCancelTask {

    private final PaymentOrderService paymentOrderService;

    @Scheduled(cron = "${payment.order.auto-cancel-cron:0 0/1 * * * ?}")
    public void autoCancelTimeoutOrders() {
        try {
            paymentOrderService.autoCancelTimeoutOrders();
        } catch (Exception e) {
            log.error("支付订单自动关单任务执行失败", e);
        }
    }
}
