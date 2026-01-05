package com.chua.starter.system.configuration;

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
 * <p>
 * 配置Swagger/OpenAPI文档，提供系统管理API的接口文档
 * </p>
 *
 * @author CH
 * @since 2024/12/11
 */
@Configuration
public class OpenApiConfiguration {

    @Value("${server.servlet.context-path:/system/api}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("系统管理服务API")
                        .version("4.0.0")
                        .description("提供用户管理、角色管理、菜单管理、部门管理等系统管理功能的API")
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


