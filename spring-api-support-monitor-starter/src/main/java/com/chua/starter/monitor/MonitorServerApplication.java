package com.chua.starter.monitor;

import cn.keepbx.jpom.JpomAppType;
import cn.keepbx.jpom.Type;
import com.chua.starter.monitor.properties.ReportGenProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 配置中心服务
 * @author CH
 */
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(ReportGenProperties.class)
@MapperScan({"com.chua.starter.**.mapper"})
@JpomAppType(Type.Server)
@ServletComponentScan
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.monitor",
        "com.chua.starter.monitor",
        "com.chua.starter.service.**"
})
public class MonitorServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorServerApplication.class);
    }
}