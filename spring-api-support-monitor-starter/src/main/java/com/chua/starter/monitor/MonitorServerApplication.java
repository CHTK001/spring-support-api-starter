package com.chua.starter.monitor;

import com.chua.starter.monitor.properties.AcmeProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 配置中心服务
 * @author CH
 */
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties({AcmeProperties.class})
@MapperScan("com.chua.starter.monitor.mapper")
@ServletComponentScan
@SpringBootApplication(scanBasePackages = "com.chua.starter.monitor")
public class MonitorServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorServerApplication.class);
    }

}
