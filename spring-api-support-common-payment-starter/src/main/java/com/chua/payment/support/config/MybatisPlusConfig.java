package com.chua.payment.support.config;

import org.springframework.context.annotation.Configuration;

/**
 * 保留历史包路径，实际配置迁移至 PaymentAutoConfiguration。
 */
@Configuration(proxyBeanMethods = false)
public class MybatisPlusConfig {
}
