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
 * @since 2024/12/06
 * @author CH
 */
@Configuration
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
@MapperScan("com.chua.starter.tenant.server.mapper")
@ComponentScan(basePackages = {
        "com.chua.starter.tenant.server.controller",
        "com.chua.starter.tenant.server.service",
        "com.chua.starter.tenant.server.sync",
        // support 包承载租户侧的支撑组件（租户口令可逆加解密 TenantPasswordCipher 等）。
        // 漏扫不会报错在启动日志里，而是表现为 SysTenantServiceImpl 构造注入阶段
        // 「No qualifying bean of type TenantPasswordCipher」，整个应用起不来，故必须列出。
        "com.chua.starter.tenant.server.support",
        // 数据源台账的查询口在 support 包（TenantDatasourceLookup），其实现由装了数据源中心的
        // 应用提供；tenant 侧只 `getIfAvailable` 取用，未装时引用型租户会显式失败而非回退主库。
        // provision 包承载「带进度的租户创建」：TenantProvisionService 是 @Service，
        // 漏扫时创建接口 404、进度弹窗永远停在第一步，且不会有任何启动期报错。
        "com.chua.starter.tenant.server.provision"
})
/**
 * Tenant服务端配置类，提供相关能力。
 *
 * @author CH
 * @since 1.0.0
 */
public class TenantServerConfiguration {
}
