package com.chua.starter.payment.configuration;

import com.chua.starter.payment.properties.PaymentConsoleProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付控制台基础配置。
 */
@Configuration
@EnableConfigurationProperties(PaymentConsoleProperties.class)
@ConditionalOnProperty(prefix = "plugin.payment.ops.console", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PaymentConsoleConfiguration {
}
