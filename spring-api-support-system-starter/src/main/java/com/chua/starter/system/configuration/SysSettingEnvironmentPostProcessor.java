package com.chua.starter.system.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 自定义系统配置
 * @author CH
 * @since 2024/8/7
 */
@SpringBootConfiguration
public class SysSettingEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        System.out.println();
    }
}
