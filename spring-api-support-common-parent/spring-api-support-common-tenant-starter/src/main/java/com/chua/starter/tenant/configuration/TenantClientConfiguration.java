/**
 * 租户客户端配置
 *
 * @version 1.0.0
 * @author CH
 * @since 2024/12/06
 */
@Configuration
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".client", name = "enable", havingValue = "true")
@ComponentScan(basePackages = {
        "com.chua.starter.tenant.client",
        "com.chua.starter.tenant.spi"
})
public class TenantClientConfiguration