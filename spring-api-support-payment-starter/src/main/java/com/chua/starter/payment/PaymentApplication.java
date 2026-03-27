package com.chua.starter.payment;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 独立支付服务启动类。
 */
@EnableCaching
@EnableScheduling
@ConditionalOnProperty(prefix = "plugin.payment.server", name = "enable", havingValue = "true", matchIfMissing = true)
@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "支付服务 API",
                version = "4.0.0",
                description = "独立承载商户、渠道、订单、退款、钱包、微信支付分与运营能力"
        )
)
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
