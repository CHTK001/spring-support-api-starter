package com.chua.starter.tenant.configuration;

import com.chua.starter.tenant.properties.TenantProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 租户客户端配置
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Configuration
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".client", name = "enable", havingValue = "true")
@ComponentScan(basePackages = {
        "com.chua.starter.tenant.client",
        "com.chua.starter.tenant.spi"
})
public class TenantClientConfiguration {
}
