package com.chua.starter.tenant;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 多租户 SaaS 平台启动入口。
 *
 * <p>依赖以下 starter:</p>
 * <ul>
 *   <li>{@code spring-api-support-common-tenant-starter} - 多租户核心能力</li>
 *   <li>{@code spring-api-support-common-system-starter} - 系统管理(用户/角色/菜单)</li>
 *   <li>{@code spring-api-support-common-service-starter} - 文件系统/字典/通用业务</li>
 * </ul>
 *
 * <p>提供多租户切换、独立数据库、租户级权限分配、SaaS 服务编排等能力。
 * 可独立部署或被嵌入到其他业务系统。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@EnableAsync
@EnableScheduling
@EnableCaching
@MapperScan({"com.chua.starter.**.mapper"})
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.system",
        "com.chua.starter.tenant",
        "com.chua.starter.service"
})
@OpenAPIDefinition(
        info = @Info(
                title = "Tenant SaaS Platform API",
                version = "4.0.0.42",
                description = "多租户 SaaS 平台接口文档",
                contact = @Contact(name = "CH", email = "dev@chua.local"),
                license = @License(name = "Apache 2.0")
        ),
        servers = {
                @Server(url = "/", description = "当前服务"),
                @Server(url = "https://tenant.chua.local", description = "生产环境")
        }
)
/**
 * public class TenantApplication extends SpringBootServletInitializer {
 *
 * @author CH
 * @since 4.0.0.42
 */
public class TenantApplication extends SpringBootServletInitializer {

    /**
     * 应用启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplicationBuilder()
                .sources(TenantApplication.class)
                .bannerMode(org.springframework.boot.Banner.Mode.LOG)
                .build();
        app.run(args);
    }

    /**
     * 配置 Servlet 容器。
     *
     * @param builder Spring 应用构建器
     * @return 配置后的构建器
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(TenantApplication.class);
    }
}
