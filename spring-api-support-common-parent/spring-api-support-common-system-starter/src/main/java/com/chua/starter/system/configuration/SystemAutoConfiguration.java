/**
 * 系统统一认证自动配置
 * <p>自动扫描认证相关的组件和Mapper</p>
 *
 * @version 1.0.0
 * @author CH
 * @since 2024/12/11
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = {
        "com.chua.starter.system"
})
@MapperScan(basePackages = "com.chua.starter.system.mapper")
@EnableConfigurationProperties(SystemSecurityProperties.class)
public class SystemAutoConfiguration