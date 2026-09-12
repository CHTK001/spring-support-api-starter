package com.chua.starter.service.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
* 自定义系统配置
* @since 2024/8/7
* @author CH
 */
@SpringBootConfiguration
public class SysSettingEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    /** Post处理环境 */
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    }
}
