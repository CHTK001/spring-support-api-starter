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
// SysFileUploadProperties 必须在此注册：SysLocalFileStorage 是无条件 @Component 且构造依赖它，
// 而属性类本身只有 @ConfigurationProperties（不是 @Component），组件扫描扫不到 —— 漏注册会让
// 应用启动直接失败（No qualifying bean of type SysFileUploadProperties）。
@EnableConfigurationProperties({FileSystemProperties.class, ApiProperties.class, SysFileUploadProperties.class})
public class ServiceConfiguration {
}
