/**
 * 租户自动配置
 *
 * @version 1.0.0
 * @author CH
 * @since 2024/12/06
 */
@Configuration
@EnableConfigurationProperties(TenantProperties.class)
@Import({
        TenantMybatisConfiguration.class,
        TenantServerConfiguration.class,
        TenantClientConfiguration.class
})
/**
 * public class TenantAutoConfiguration
 *
 * @author CH
 * @since 4.0.0.42
 */
public class TenantAutoConfiguration