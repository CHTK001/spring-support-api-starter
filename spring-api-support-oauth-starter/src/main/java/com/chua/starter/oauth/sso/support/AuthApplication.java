package com.chua.starter.oauth.sso.support;

import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 单点登录
 *
 * @author CH
 * @since 2022/7/25 10:59
 */
@EnableCaching
@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.chua.starter.oauth.sso", "com.chua.starter.service"})
@EnableConfigurationProperties({AuthServerProperties.class, AuthClientProperties.class})
@MapperScan({
        "com.chua.starter.oauth.sso",
        "com.chua.starter.service.mapper",
        "com.chua.starter.system.mapper"
})
@OpenAPIDefinition(
        info = @Info(
                title = "统一认证中心API",
                version = "4.0.0",
                description = "提供单点登录、第三方登录和认证授权功能"
        )
)
/**
 * public class AuthApplication {
 *
 * @author CH
 * @since 4.0.0.42
 */
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class);
    }
}
