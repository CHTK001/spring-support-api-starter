package com.chua.starter.tenant.configuration;

import com.chua.starter.tenant.properties.TenantProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
* 租户自动配置
*
* @版本 1.0.0
* @since 2024/12/06
* @author CH
 */
@Configuration
@EnableConfigurationProperties(TenantProperties.class)
@Import({
        TenantMybatisConfiguration.class,
        TenantServerConfiguration.class,
        TenantClientConfiguration.class
})
/**
 * TenantAuto配置类，提供相关能力。
 *
 * @author CH
 * @since 1.0.0
 */
public class TenantAutoConfiguration {
}
