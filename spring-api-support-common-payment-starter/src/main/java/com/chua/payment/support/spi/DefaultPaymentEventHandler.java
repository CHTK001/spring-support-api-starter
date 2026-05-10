package com.chua.payment.support.spi;

import com.chua.payment.support.event.PaymentEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 默认支付事件处理器
 * <p>
 * 将事件序列化为 JSON 并输出日志，便于外部系统订阅消费。
 * 可通过注册自定义 {@link PaymentEventHandler} bean 覆盖此默认行为。
 * </p>
 *
 * @author CH
 */
@Slf4j
@Component
@ConditionalOnMissingBean(PaymentEventHandler.class)
public class DefaultPaymentEventHandler implements PaymentEventHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void onPaymentEvent(PaymentEvent event) {
        try {
            String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(event);
            log.info("[支付事件下发]\n{}", json);
        } catch (JsonProcessingException e) {
            log.warn("[支付事件下发] 序列化失败: eventType={}, error={}", event.getEventType(), e.getMessage());
        }
    }
}
