package com.chua.starter.oauth.sso.support.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI配置类
 *
 * @author CH
 */
@Configuration
public class OpenApiConfiguration {

    @Value("${server.servlet.context-path:/oauth}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("统一认证中心API")
                        .version("4.0.0")
                        .description("提供单点登录、第三方登录和认证授权功能的API")
                        .contact(new Contact()
                                .name("CH")
                                .email("admin@example.com"))
                        .license(new License()
                                .name("Private License")
                                .url("https://example.com")))
                .servers(List.of(
                        new Server().url(contextPath).description("默认服务器")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT认证令牌")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
} 