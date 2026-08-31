package com.chua.starter.geo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Geo 地理空间数据下载服务启动类。
 *
 * @author CH
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.chua.starter.geo")
@EnableAsync
@EnableScheduling
@OpenAPIDefinition(info = @Info(title = "GeoDownloader API", version = "1.0.0", description = "地理空间数据下载服务"))
public class GeoApplication {
    /**
     * 应用入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GeoApplication.class, args);
    }
}
