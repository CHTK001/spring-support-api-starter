package com.chua.payment.support.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 支付模块组件扫描器。
 * <p>通过 @Import 被 PaymentAutoConfiguration 引用，确保 @ComponentScan
 * 在 @AutoConfiguration 上下文中生效（Spring Boot 3.x 限制）。</p>
 */
@Configuration
@ComponentScan("com.chua.payment.support")
public class PaymentComponentScanner {
}
