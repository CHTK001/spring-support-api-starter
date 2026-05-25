package com.chua.starter.monitor.configuration;

import com.chua.starter.monitor.controller.loki.LokiController;
import com.chua.starter.monitor.service.loki.LokiService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Loki 日志管理自动装配
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class LokiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LokiService lokiService() {
        return new LokiService();
    }

    @Bean
    @ConditionalOnMissingBean
    public LokiController lokiController(LokiService lokiService) {
        return new LokiController(lokiService);
    }
}
