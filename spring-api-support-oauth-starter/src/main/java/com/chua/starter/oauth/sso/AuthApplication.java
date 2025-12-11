package com.chua.starter.oauth.sso;

import com.chua.starter.oauth.sso.properties.TenantDataSourceProperties;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 单点登录
 *
 * @author CH
 * @since 2022/7/25 10:59
 */
@SpringBootApplication(scanBasePackages = {"com.chua.starter.oauth.sso", "com.chua.starter.service"})
@EnableConfigurationProperties({TenantDataSourceProperties.class, AuthServerProperties.class})
@MapperScan({"com.chua.starter.oauth.sso", "com.chua.starter.service.mapper"})
@ConditionalOnProperty(prefix = "plugin.oauth.server", name = "enable", havingValue = "true", matchIfMissing = false)
@OpenAPIDefinition(
        info = @Info(
                title = "统一认证中心API",
                version = "4.0.0",
                description = "提供单点登录、第三方登录和认证授权功能",
                contact = @Contact(name = "CH", email = "admin@example.com"),
                license = @License(name = "Private License", url = "https://example.com")
        )
)
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class);
    }
}
