package com.chua.starter.rpc.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.chua.starter.rpc")
public class RpcTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcTestApplication.class, args);
    }
}