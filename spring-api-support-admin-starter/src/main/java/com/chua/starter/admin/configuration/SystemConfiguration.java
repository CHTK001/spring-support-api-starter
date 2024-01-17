package com.chua.starter.admin.configuration;


import com.chua.starter.admin.properties.SystemProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 系统配置
 * @author Administrator
 * @date 2024/01/17
 */
@Configuration
@EnableConfigurationProperties(SystemProperties.class)
public class SystemConfiguration {
}
