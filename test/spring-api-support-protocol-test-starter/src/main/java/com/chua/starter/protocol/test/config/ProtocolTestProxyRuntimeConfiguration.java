package com.chua.starter.protocol.test.config;

import com.chua.starter.proxy.support.ProxyManagementConfiguration;
import com.chua.starter.proxy.support.properties.ProxyManagementProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@Configuration(proxyBeanMethods = false)
@MapperScan("com.chua.starter.proxy.support.mapper")
@ConditionalOnProperty(prefix = ProxyManagementProperties.PREFIX, name = "enable", havingValue = "true")
@ComponentScan(
        basePackages = "com.chua.starter.proxy.support",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {RestController.class, Controller.class}),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.chua\\.starter\\.proxy\\.support\\.controller\\..*"),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ProxyManagementConfiguration.class)
        }
)
public class ProtocolTestProxyRuntimeConfiguration {
}

