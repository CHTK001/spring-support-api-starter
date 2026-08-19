/**
 * 租户服务端配置
 *
 * @version 1.0.0
 * @author CH
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
public class TenantServerConfiguration