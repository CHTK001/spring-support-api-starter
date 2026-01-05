package com.chua.starter.system;

import com.chua.starter.system.properties.SystemProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 系统管理服务启动类
 *
 * <p>
 * 提供完整的系统管理功能，包括用户管理、角色管理、菜单管理、部门管理等基础系统服务。
 * </p>
 *
 * @author CH
 * @since 2024/12/11
 */
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties({SystemProperties.class})
@MapperScan({"com.chua.starter.system.mapper", "com.chua.starter.service.mapper"})
@ConditionalOnProperty(prefix = "plugin.system.server", name = "enable", havingValue = "true", matchIfMissing = true)
@SpringBootApplication(scanBasePackages = {"com.chua.starter.system", "com.chua.starter.service"})
@OpenAPIDefinition(
        info = @Info(
                title = "系统管理服务API",
                version = "4.0.0",
                description = "提供用户管理、角色管理、菜单管理、部门管理等系统管理功能",
                contact = @Contact(name = "CH", email = "admin@example.com"),
                license = @License(name = "Private License", url = "https://example.com")
        )
)
public class SystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}
