package com.chua.starter.monitor.configuration;

import com.chua.starter.monitor.controller.spider.MonitorSpiderController;
import com.chua.starter.monitor.controller.spider.SpiderAuthController;
import com.chua.starter.monitor.controller.spider.SpiderConsoleController;
import com.chua.starter.monitor.filter.SpiderAuthenticationFilter;
import com.chua.starter.monitor.mapper.MonitorSpiderConnectionMapper;
import com.chua.starter.monitor.mapper.MonitorSpiderDataMapper;
import com.chua.starter.monitor.mapper.MonitorSpiderLogMapper;
import com.chua.starter.monitor.mapper.MonitorSpiderNodeMapper;
import com.chua.starter.monitor.mapper.MonitorSpiderTaskMapper;
import com.chua.starter.monitor.properties.SpiderProperties;
import com.chua.starter.monitor.service.spider.MonitorSpiderConnectionService;
import com.chua.starter.monitor.service.spider.MonitorSpiderDataService;
import com.chua.starter.monitor.service.spider.MonitorSpiderLogService;
import com.chua.starter.monitor.service.spider.MonitorSpiderNodeService;
import com.chua.starter.monitor.service.spider.MonitorSpiderTaskService;
import com.chua.starter.monitor.service.spider.SpiderAiService;
import com.chua.starter.monitor.service.spider.SpiderOutputTableService;
import com.chua.starter.monitor.service.spider.SpiderSchemaInitializer;
import com.chua.starter.monitor.service.spider.SpiderSchemaService;
import com.chua.starter.monitor.service.spider.impl.MonitorSpiderConnectionServiceImpl;
import com.chua.starter.monitor.service.spider.impl.MonitorSpiderDataServiceImpl;
import com.chua.starter.monitor.service.spider.impl.MonitorSpiderLogServiceImpl;
import com.chua.starter.monitor.service.spider.impl.MonitorSpiderNodeServiceImpl;
import com.chua.starter.monitor.service.spider.impl.MonitorSpiderTaskServiceImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import com.chua.starter.project.service.BigModelService;

import javax.sql.DataSource;

/**
 * Spider 模块自动装配。
 *
 * 允许普通 Spring 宿主仅通过引入 starter 即可暴露 spider API 和控制台，
 * 同时保留 monitor 主应用已有的组件扫描结果，不重复注册已有 Bean。
 */
@AutoConfiguration(afterName = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration",
        "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
        "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({JdbcTemplate.class, SqlSessionFactory.class, MonitorSpiderController.class})
@ConditionalOnProperty(prefix = SpiderProperties.PRE, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SpiderProperties.class)
public class SpiderAutoConfiguration {

    @Bean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean(JdbcTemplate.class)
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnBean(SqlSessionFactory.class)
    @ConditionalOnMissingBean(MonitorSpiderTaskMapper.class)
    public MapperFactoryBean<MonitorSpiderTaskMapper> monitorSpiderTaskMapper(SqlSessionFactory sqlSessionFactory) {
        return mapperFactoryBean(MonitorSpiderTaskMapper.class, sqlSessionFactory);
    }

    @Bean
    @ConditionalOnBean(SqlSessionFactory.class)
    @ConditionalOnMissingBean(MonitorSpiderNodeMapper.class)
    public MapperFactoryBean<MonitorSpiderNodeMapper> monitorSpiderNodeMapper(SqlSessionFactory sqlSessionFactory) {
        return mapperFactoryBean(MonitorSpiderNodeMapper.class, sqlSessionFactory);
    }

    @Bean
    @ConditionalOnBean(SqlSessionFactory.class)
    @ConditionalOnMissingBean(MonitorSpiderConnectionMapper.class)
    public MapperFactoryBean<MonitorSpiderConnectionMapper> monitorSpiderConnectionMapper(SqlSessionFactory sqlSessionFactory) {
        return mapperFactoryBean(MonitorSpiderConnectionMapper.class, sqlSessionFactory);
    }

    @Bean
    @ConditionalOnBean(SqlSessionFactory.class)
    @ConditionalOnMissingBean(MonitorSpiderDataMapper.class)
    public MapperFactoryBean<MonitorSpiderDataMapper> monitorSpiderDataMapper(SqlSessionFactory sqlSessionFactory) {
        return mapperFactoryBean(MonitorSpiderDataMapper.class, sqlSessionFactory);
    }

    @Bean
    @ConditionalOnBean(SqlSessionFactory.class)
    @ConditionalOnMissingBean(MonitorSpiderLogMapper.class)
    public MapperFactoryBean<MonitorSpiderLogMapper> monitorSpiderLogMapper(SqlSessionFactory sqlSessionFactory) {
        return mapperFactoryBean(MonitorSpiderLogMapper.class, sqlSessionFactory);
    }

    @Bean
    @ConditionalOnBean(MonitorSpiderTaskMapper.class)
    @ConditionalOnMissingBean(MonitorSpiderTaskService.class)
    public MonitorSpiderTaskService monitorSpiderTaskService() {
        return new MonitorSpiderTaskServiceImpl();
    }

    @Bean
    @ConditionalOnBean(MonitorSpiderNodeMapper.class)
    @ConditionalOnMissingBean(MonitorSpiderNodeService.class)
    public MonitorSpiderNodeService monitorSpiderNodeService() {
        return new MonitorSpiderNodeServiceImpl();
    }

    @Bean
    @ConditionalOnBean(MonitorSpiderConnectionMapper.class)
    @ConditionalOnMissingBean(MonitorSpiderConnectionService.class)
    public MonitorSpiderConnectionService monitorSpiderConnectionService() {
        return new MonitorSpiderConnectionServiceImpl();
    }

    @Bean
    @ConditionalOnBean(MonitorSpiderDataMapper.class)
    @ConditionalOnMissingBean(MonitorSpiderDataService.class)
    public MonitorSpiderDataService monitorSpiderDataService() {
        return new MonitorSpiderDataServiceImpl();
    }

    @Bean
    @ConditionalOnBean(MonitorSpiderLogMapper.class)
    @ConditionalOnMissingBean(MonitorSpiderLogService.class)
    public MonitorSpiderLogService monitorSpiderLogService() {
        return new MonitorSpiderLogServiceImpl();
    }

    @Bean
    @ConditionalOnBean({DataSource.class, JdbcTemplate.class})
    @ConditionalOnMissingBean(SpiderSchemaService.class)
    public SpiderSchemaService spiderSchemaService(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        return new SpiderSchemaService(dataSource, jdbcTemplate);
    }

    @Bean
    @ConditionalOnBean(SpiderSchemaService.class)
    @ConditionalOnMissingBean(SpiderSchemaInitializer.class)
    public SpiderSchemaInitializer spiderSchemaInitializer(
            SpiderProperties spiderProperties,
            SpiderSchemaService spiderSchemaService) {
        return new SpiderSchemaInitializer(spiderProperties, spiderSchemaService);
    }

    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean(SpiderOutputTableService.class)
    public SpiderOutputTableService spiderOutputTableService(JdbcTemplate jdbcTemplate) {
        return new SpiderOutputTableService(jdbcTemplate);
    }

    @Bean
    @ConditionalOnBean(BigModelService.class)
    @ConditionalOnMissingBean(SpiderAiService.class)
    public SpiderAiService spiderAiService(BigModelService bigModelService, SpiderProperties spiderProperties) {
        return new SpiderAiService(bigModelService, spiderProperties);
    }

    @Bean
    @ConditionalOnMissingBean(SpiderConsoleController.class)
    public SpiderConsoleController spiderConsoleController() {
        return new SpiderConsoleController();
    }

    @Bean
    @ConditionalOnMissingBean(SpiderAuthController.class)
    public SpiderAuthController spiderAuthController(SpiderProperties spiderProperties) {
        return new SpiderAuthController(spiderProperties);
    }

    @Bean
    @ConditionalOnBean({
            MonitorSpiderTaskService.class,
            MonitorSpiderNodeService.class,
            MonitorSpiderConnectionService.class,
            MonitorSpiderDataService.class,
            MonitorSpiderLogService.class,
            SpiderSchemaService.class,
            SpiderOutputTableService.class
    })
    @ConditionalOnMissingBean(MonitorSpiderController.class)
    public MonitorSpiderController monitorSpiderController(
            MonitorSpiderTaskService taskService,
            MonitorSpiderNodeService nodeService,
            MonitorSpiderConnectionService connectionService,
            MonitorSpiderDataService dataService,
            MonitorSpiderLogService logService,
            SpiderProperties spiderProperties,
            SpiderSchemaService spiderSchemaService,
            SpiderOutputTableService spiderOutputTableService,
            @Nullable SpiderAiService spiderAiService) {
        return new MonitorSpiderController(
                taskService,
                nodeService,
                connectionService,
                dataService,
                logService,
                spiderProperties,
                spiderSchemaService,
                spiderOutputTableService,
                spiderAiService
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = SpiderProperties.PRE + ".web-auth", name = "mode", havingValue = "embedded")
    @ConditionalOnMissingBean(name = "spiderAuthFilter")
    public FilterRegistrationBean<SpiderAuthenticationFilter> spiderAuthFilter() {
        FilterRegistrationBean<SpiderAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SpiderAuthenticationFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    private static <T> MapperFactoryBean<T> mapperFactoryBean(Class<T> mapperClass, SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<T> factoryBean = new MapperFactoryBean<>(mapperClass);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
}
