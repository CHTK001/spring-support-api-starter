package com.chua.starter.gen.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 配置中心服务
 * @author CH
 */
@SpringBootApplication(scanBasePackages = "com.chua.starter.gen.support")
public class GenServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenServerApplication.class);
    }
}
