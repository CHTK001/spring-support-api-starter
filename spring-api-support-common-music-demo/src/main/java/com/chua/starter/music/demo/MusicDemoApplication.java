package com.chua.starter.music.demo;

import com.chua.starter.music.config.MusicConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(excludeName = {
        "com.chua.starter.common.support.configuration.CommonConfiguration",
        "com.chua.starter.common.support.configuration.ProjectConfiguration",
        "com.chua.starter.common.support.api.configuration.ApiConfiguration",
        "com.chua.starter.common.support.api.configuration.ApiFilterConfiguration",
        "com.chua.starter.common.support.configuration.CaptchaConfiguration",
        "com.chua.starter.common.support.configuration.security.RequestFingerprintAutoConfiguration",
        "com.chua.starter.common.support.cache.CacheConfiguration",
        "com.chua.starter.common.support.cache.CacheProxyCachingConfiguration",
        "com.chua.starter.common.support.configuration.AutoControllerRegistrar",
        "com.chua.starter.common.support.configuration.TypeConverterRegisterConfiguration",
        "com.chua.starter.common.support.configuration.ResourceWebMvcConfigurer",
        "com.chua.starter.common.support.configuration.MessageConverterWebMvcConfigurer",
        "com.chua.starter.swagger.support.Knife4jRunner",
        "com.chua.starter.swagger.support.Knife4jWebMvcConfigurer"
})
@Import(MusicConfiguration.class)
public class MusicDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicDemoApplication.class, args);
    }
}
