package com.chua.starter.monitor.configuration;

import com.chua.starter.monitor.controller.spider.MonitorSpiderController;
import com.chua.starter.monitor.controller.spider.SpiderAuthController;
import com.chua.starter.monitor.controller.spider.SpiderConsoleController;
import com.chua.starter.monitor.pojo.spider.SpiderDataSourceOption;
import com.chua.starter.monitor.service.spider.MonitorSpiderTaskService;
import com.chua.starter.monitor.service.spider.SpiderOutputTableService;
import com.chua.starter.monitor.service.spider.SpiderSchemaService;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class SpiderAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SpiderAutoConfiguration.class))
            .withUserConfiguration(TestHostConfiguration.class);

    @Test
    void autoConfigurationRegistersSpiderBeansForPlainSpringHost() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(MonitorSpiderController.class);
            assertThat(context).hasSingleBean(SpiderAuthController.class);
            assertThat(context).hasSingleBean(SpiderConsoleController.class);
            assertThat(context).hasSingleBean(MonitorSpiderTaskService.class);
            assertThat(context).hasSingleBean(SpiderSchemaService.class);
            assertThat(context).hasSingleBean(SpiderOutputTableService.class);

            MonitorSpiderController controller = context.getBean(MonitorSpiderController.class);
            assertThat(controller.getDataSourceOptions().getData())
                    .extracting(SpiderDataSourceOption::getName)
                    .containsExactly("当前监控库");
        });
    }

    @Test
    void embeddedAuthModeRegistersSpiderFilter() {
        contextRunner
                .withPropertyValues("plugin.monitor.spider.web-auth.mode=embedded")
                .run(context -> {
                    assertThat(context).hasBean("spiderAuthFilter");
                    assertThat(context.getBean("spiderAuthFilter")).isInstanceOf(FilterRegistrationBean.class);
                });
    }

    @Configuration
    static class TestHostConfiguration {

        @Bean
        DataSource dataSource() {
            return Mockito.mock(DataSource.class);
        }

        @Bean
        JdbcTemplate jdbcTemplate() {
            return Mockito.mock(JdbcTemplate.class);
        }

        @Bean
        SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
            org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
            configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(), dataSource));
            return new SqlSessionFactoryBuilder().build(configuration);
        }
    }
}
