package com.chua.starter.system;

import com.chua.starter.oauth.sso.support.configuration.AuthServerConfigValidator;
import com.chua.starter.oauth.sso.support.configuration.DefaultAuthService;
import com.chua.starter.oauth.sso.support.configuration.OAuthTraceConfiguration;
import com.chua.starter.oauth.sso.support.configuration.SsoRedisConfiguration;
import com.chua.starter.system.properties.SystemProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 系统管理服务启动类
 */
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties({SystemProperties.class})
@MapperScan({
        "com.chua.starter.system.mapper",
        "com.chua.starter.service.mapper",
        "com.chua.starter.system.achievement.mapper",
        "com.chua.starter.job.support.store",
        "com.chua.starter.oauth.sso.support.mapper"
})
@ConditionalOnProperty(prefix = "plugin.system.server", name = "enable", havingValue = "true", matchIfMissing = true)
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.system",
        "com.chua.starter.service",
        "com.chua.starter.oauth.sso.support.properties",
        "com.chua.starter.oauth.sso.support.check",
        "com.chua.starter.oauth.sso.support.core",
        "com.chua.starter.oauth.sso.support.token",
        "com.chua.starter.oauth.sso.support.admin",
        "com.chua.starter.oauth.sso.support.health",
        "com.chua.starter.oauth.sso.support.service",
        "com.chua.starter.oauth.sso.support.security"
})
@Import({
        AuthServerConfigValidator.class,
        OAuthTraceConfiguration.class,
        SsoRedisConfiguration.class,
        DefaultAuthService.class
})
@OpenAPIDefinition(
        info = @Info(
                title = "系统管理服务API",
                version = "4.0.0",
                description = "提供用户管理、角色管理、菜单管理、部门管理、成就系统等系统管理功能"
        )
)
/**
 * SystemApplication类，提供相关能力。
 *
 * @author CH
 * @since 1.0.0
 */
public class SystemApplication {
    /**
     * 程序入口，运行示例自检。
     *
     * @param args 参数，不允许为 null
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

