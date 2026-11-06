package com.chua.starter.tenant.configuration;

import com.chua.starter.system.properties.SystemSecurityProperties;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.server.support.TenantPasswordCipher;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 租户客户端配置
 *
 * @since 2024/12/06
 * @author CH
 */
@Configuration
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".client", name = "enable", havingValue = "true")
// 客户端模式下同样要扫服务端 mapper 包：{@code spi} 包里的 DefaultTenantDataSyncProvider
// 负责把平台推来的租户/服务/菜单数据写进本租户库，它注入的 SysServiceMapper 等都在这个包，
// 只在 server 模式扫描时租户端应用会「No qualifying bean of type SysServiceMapper」直接起不来。
@MapperScan("com.chua.starter.tenant.server.mapper")
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

    /**
     * 租户口令加解密器。
     *
     * <p>{@link TenantPasswordCipher} 定义在 {@code server.support} 包，只有 server 模式的
     * 组件扫描会带上它；而客户端侧的数据同步提供者下发口令前必须解密，
     * 所以这里显式补一个 bean。server 模式已存在时不再重复注册。</p>
     *
     * @param securityProperties 系统安全属性
     * @return 租户口令加解密器
     */
    @Bean
    @ConditionalOnMissingBean(TenantPasswordCipher.class)
    public TenantPasswordCipher tenantPasswordCipher(SystemSecurityProperties securityProperties) {
        return new TenantPasswordCipher(securityProperties);
    }
}
