package com.chua.starter.tenant.configuration;

import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.server.mapper.*;
import com.chua.starter.tenant.server.service.*;
import com.chua.starter.tenant.server.service.impl.*;
import com.chua.starter.tenant.server.sync.TenantServerSyncPublisher;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 租户服务端配置
 *
 * @版本 1.0.0
 * @since 2024/12/06
 * @author CH
 */
@Configuration
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
@MapperScan("com.chua.starter.tenant.server.mapper")
@ComponentScan(basePackages = {
        "com.chua.starter.tenant.server.controller",
        "com.chua.starter.tenant.server.service",
        "com.chua.starter.tenant.server.sync"
})
/**
 * Tenant服务端配置类，提供相关能力。
 *
 * @author CH
 * @since 1.0.0
 */
public class TenantServerConfiguration {
}
