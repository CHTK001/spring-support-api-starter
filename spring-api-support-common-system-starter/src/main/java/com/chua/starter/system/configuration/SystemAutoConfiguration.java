package com.chua.starter.system.configuration;

import com.chua.starter.system.properties.SystemSecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 系统统一认证自动配置
 * <p>自动扫描认证相关的组件和Mapper</p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = {
        "com.chua.starter.system"
})
@MapperScan(basePackages = "com.chua.starter.system.mapper")
@EnableConfigurationProperties(SystemSecurityProperties.class)
public class SystemAutoConfiguration {

    /**
     * 构造方法
     */
    public SystemAutoConfiguration() {
        log.info("系统统一认证模块已启用");
    }
}
