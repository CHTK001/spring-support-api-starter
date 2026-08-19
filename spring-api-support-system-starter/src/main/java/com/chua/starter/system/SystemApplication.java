package com.chua.starter.system;

import com.chua.starter.system.properties.SystemProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
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
@EnableAsync
@EnableConfigurationProperties({SystemProperties.class})
@MapperScan({
        "com.chua.starter.system.mapper",
        "com.chua.starter.service.mapper",
        "com.chua.starter.system.achievement.mapper"
})
@ConditionalOnProperty(prefix = "plugin.system.server", name = "enable", havingValue = "true", matchIfMissing = true)
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.system",
        "com.chua.starter.service"
})
@OpenAPIDefinition(
        info = @Info(
                title = "系统管理服务API",
                version = "4.0.0",
                description = "提供用户管理、角色管理、菜单管理、部门管理、成就系统等系统管理功能"
        )
)
/**
 * public class SystemApplication {
 *
 * @author CH
 * @since 4.0.0.42
 */
public class SystemApplication {
    /**
     * 应用启动入口, 启动前补全 MyBatis-Plus 加密器配置。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        if (System.getProperty("mybatis-plus.encryptor.mode") == null) {
            System.setProperty("mybatis-plus.encryptor.mode", "PLAIN");
        }
        if (System.getProperty("mybatis-plus.encryptor.password") == null) {
            System.setProperty("mybatis-plus.encryptor.password", "system-test");
        }
        SpringApplication.run(SystemApplication.class, args);
    }
}
