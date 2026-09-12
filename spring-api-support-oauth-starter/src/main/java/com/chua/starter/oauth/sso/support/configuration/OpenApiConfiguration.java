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
   * 打开api配置类
 *
 * @author CH
 * @since 4.0.0
 */
@Configuration
public class OpenApiConfiguration {

    @Value("${server.servlet.context-path:/oauth}")
    /** 上下文路径 */
    private String contextPath;

    @Bean
    /**
     * 习俗打开API
     *
     * @return 习俗打开api的结果
     */
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