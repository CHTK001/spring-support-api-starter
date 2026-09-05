package com.chua.starter.rpc.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.rpc.Filter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.chua.starter.rpc")
public class RpcTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcTestApplication.class, args);
    }

    @Bean
    CommandLineRunner printDubboFilters() {
        return args -> log.info("[DubboFilter] registered: {}",
                ExtensionLoader.getExtensionLoader(Filter.class).getSupportedExtensions());
    }
}
