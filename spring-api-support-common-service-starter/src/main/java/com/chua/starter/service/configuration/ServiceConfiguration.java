package com.chua.starter.service.configuration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author CH
 * @since 2025/9/3 8:42
 */
@AutoConfiguration
@MapperScan("com.chua.starter.service.mapper")
@ComponentScan("com.chua.starter.service")
public class ServiceConfiguration {
}
