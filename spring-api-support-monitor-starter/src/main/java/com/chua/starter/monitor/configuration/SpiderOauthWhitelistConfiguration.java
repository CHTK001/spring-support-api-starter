package com.chua.starter.monitor.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 将 Spider 控制台接口加入宿主 OAuth 白名单，交给模块内置 Session 认证处理。
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "com.chua.starter.oauth.client.support.properties.AuthClientProperties")
public class SpiderOauthWhitelistConfiguration {

    private static final List<String> SPIDER_WHITELIST_PATTERNS = List.of(
            "/v1/spider/**",
            "/api/v1/spider/**",
            "/monitor/api/v1/spider/**",
            "/system/api/v1/spider/**",
            "/spider-console/**",
            "/api/spider-console/**",
            "/monitor/api/spider-console/**",
            "/system/api/spider-console/**"
    );

    @Bean
    public SmartInitializingSingleton spiderOauthWhitelistInitializer(ApplicationContext applicationContext) {
        return () -> {
            try {
                Class<?> propertiesType =
                        Class.forName("com.chua.starter.oauth.client.support.properties.AuthClientProperties");
                Object authClientProperties = applicationContext.getBean(propertiesType);
                Method getWhitelist = propertiesType.getMethod("getWhitelist");
                Method setWhitelist = propertiesType.getMethod("setWhitelist", List.class);

                @SuppressWarnings("unchecked")
                List<String> whitelist = (List<String>) getWhitelist.invoke(authClientProperties);
                List<String> updatedWhitelist = whitelist == null ? new ArrayList<>() : new ArrayList<>(whitelist);

                boolean changed = false;
                for (String pattern : SPIDER_WHITELIST_PATTERNS) {
                    if (!updatedWhitelist.contains(pattern)) {
                        updatedWhitelist.add(pattern);
                        changed = true;
                        log.info("[Spider][OAuth] 已将路径加入外层白名单: {}", pattern);
                    }
                }

                if (changed) {
                    setWhitelist.invoke(authClientProperties, updatedWhitelist);
                }
            } catch (Exception e) {
                log.warn("[Spider][OAuth] 注入外层白名单失败: {}", e.getMessage());
            }
        };
    }
}
