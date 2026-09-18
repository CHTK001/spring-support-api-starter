package com.chua.starter.tenant.configuration;

import com.chua.starter.tenant.properties.TenantProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
* 租户客户端配置
*
* @版本 1.0.0
* @since 2024/12/06
* @author CH
 */
@Configuration
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".client", name = "enable", havingValue = "true")
@ComponentScan(basePackages = {
        "com.chua.starter.tenant.client",
        "com.chua.starter.tenant.spi"
})
/**
 * Tenant客户端配置类，提供相关能力。
 *
 * @author CH
 * @since 1.0.0
 */
public class TenantClientConfiguration {
}
