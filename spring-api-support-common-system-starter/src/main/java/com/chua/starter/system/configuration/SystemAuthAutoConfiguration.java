package com.chua.starter.system.configuration;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
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
        "com.chua.starter.system.auth",
        "com.chua.starter.system.service"
})
@MapperScan(basePackages = "com.chua.starter.system.mapper")
public class SystemAuthAutoConfiguration {

    /**
     * 构造方法
     */
    public SystemAuthAutoConfiguration() {
        log.info("系统统一认证模块已启用");
    }
}
