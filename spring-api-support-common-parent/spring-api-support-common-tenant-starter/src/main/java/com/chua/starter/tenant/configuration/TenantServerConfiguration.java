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
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Configuration
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
@MapperScan("com.chua.starter.tenant.server.mapper")
@ComponentScan(basePackages = {
        "com.chua.starter.tenant.server.controller",
        "com.chua.starter.tenant.server.service",
        "com.chua.starter.tenant.server.sync"
})
public class TenantServerConfiguration {
}
