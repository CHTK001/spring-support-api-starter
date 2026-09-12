package com.chua.starter.rpc.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
* rpc测试application类。
*
* @author CH
* @since 4.0.0
 */

@SpringBootApplication(scanBasePackages = "com.chua.starter.rpc")
public class RpcTestApplication {

    /**
    * main。
    * @param args 参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RpcTestApplication.class, args);
    }
}