package com.chua.starter.service.configuration;

import com.chua.starter.common.support.api.properties.ApiProperties;
import com.chua.starter.service.properties.FileSystemProperties;
import com.chua.starter.service.properties.SysFileUploadProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * @since 2025/9/3 8:42
 * @author CH
 */
@AutoConfiguration
@MapperScan("com.chua.starter.service.mapper")
@ComponentScan("com.chua.starter.service")
@EnableConfigurationProperties({FileSystemProperties.class, SysFileUploadProperties.class, ApiProperties.class})
public class ServiceConfiguration {
}
