package com.chua.starter.monitor;

import com.chua.starter.monitor.properties.AcmeProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 配置中心服务（Monitor 模块启动入口），启用缓存、定时任务、MyBatis Mapper 扫描与 ACME 配置。
 *
 * @since 4.0.0.42
 * @author CH
 */
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties({AcmeProperties.class})
@MapperScan("com.chua.starter.monitor.mapper")
@ServletComponentScan
@SpringBootApplication(scanBasePackages = "com.chua.starter.monitor")
public class MonitorServerApplication {

    /**
     * 应用启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MonitorServerApplication.class);
    }

}
