package com.chua.starter.service.configuration;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.DynamicThresholdFilter;
import ch.qos.logback.classic.turbo.MDCFilter;
import ch.qos.logback.classic.turbo.ReconfigureOnChangeFilter;
import com.chua.report.client.starter.service.ReportService;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.redis.support.service.RedisSearchService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 系统配置
 * @author CH
 * @since 2024/9/7
 */
@SpringBootConfiguration
public class SystemConfiguration implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.addTurboFilter(new CustomLogbackFilter(
                applicationContext.getBean(ReportService.class)
        ));
    }
}
