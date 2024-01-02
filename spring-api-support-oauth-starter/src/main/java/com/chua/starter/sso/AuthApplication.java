package com.chua.starter.sso;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * sso
 *
 * @author CH
 * @since 2022/7/25 10:59
 */
@SpringBootApplication
@MapperScan("com.chua.starter.sso")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class);
    }
}
