package com.chua.starter.system;

import com.chua.starter.system.properties.SystemProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动器
 *
 * @author CH
 */
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(SystemProperties.class)
@MapperScan({"com.chua.starter.system.mapper"})
@ConditionalOnProperty(prefix = "plugin.system.server", name = "enable", havingValue = "true", matchIfMissing = false)
@SpringBootApplication
public class SystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}
