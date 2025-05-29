package com.chua.starter.oauth.sso;

import com.chua.starter.oauth.sso.properties.TenantDataSourceProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 单点登录
 *
 * @author CH
 * @since 2022/7/25 10:59
 */
@SpringBootApplication
@EnableConfigurationProperties(TenantDataSourceProperties.class)
@MapperScan({"com.chua.starter.oauth.sso", "com.chua.starter.service"})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class);
    }
}
