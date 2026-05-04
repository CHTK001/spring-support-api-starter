package com.chua.starter.monitor;

import com.chua.starter.monitor.properties.AcmeProperties;
import com.chua.starter.monitor.properties.MonitorProperties;
import com.chua.starter.monitor.properties.ReportGenProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 配置中心服务
 * @author CH
 */
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties({ReportGenProperties.class, MonitorProperties.class})
@MapperScan("com.chua.starter.monitor.mapper")
@ServletComponentScan
@SpringBootApplication
public class MonitorServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorServerApplication.class);
    }

    @Bean
    @ConfigurationProperties(prefix = "monitor.acme")
    public AcmeProperties acmeProperties() {
        return new AcmeProperties();
    }
}
