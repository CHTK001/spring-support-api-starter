package com.chua.starter.monitor.demo;

import com.chua.starter.monitor.configuration.SpiderAutoConfiguration;
import com.chua.starter.monitor.demo.config.SpiderDemoConfiguration;
import com.chua.starter.monitor.demo.controller.SpiderDemoPageController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 爬虫模块最小独立宿主。
 *
 * 只引入爬虫控制器和演示所需的轻量 Bean，不依赖完整 monitor 系统。
 */
@SpringBootApplication(
        scanBasePackageClasses = SpiderDemoApplication.class,
        excludeName = {
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration",
                "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration",
                "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
                "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration",
                "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
                "org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration",
                "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
                "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
                "com.baomidou.mybatisplus.autoconfigure.IdentifierGeneratorAutoConfiguration",
                "com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration",
                "com.chua.starter.project.configuration.ProjectConfiguration",
                "com.chua.starter.system.configuration.SystemAutoConfiguration",
                "com.chua.starter.service.configuration.ServiceConfiguration",
                "com.chua.starter.service.configuration.ConfigurationServiceConfiguration",
                "com.chua.starter.tenant.configuration.TenantAutoConfiguration",
                "com.chua.starter.device.configuration.DeviceConfiguration",
                "com.chua.starter.oauth.sso.support.configuration.EnableAuthServerConfiguration",
                "com.chua.starter.oauth.sso.support.configuration.EnableRedisTokenManagerConfiguration",
                "com.chua.starter.redis.support.RedisConfiguration",
                "com.chua.starter.sync.support.configuration.SyncAutoConfiguration",
                "com.chua.starter.sync.data.support.SyncDataConfiguration",
                "com.chua.starter.datasource.configuration.DataSourceScriptConfiguration",
                "com.chua.starter.common.support.api.configuration.ApiConfiguration"
        }
)
@Import({
        SpiderAutoConfiguration.class,
        SpiderDemoConfiguration.class,
        SpiderDemoPageController.class
})
public class SpiderDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpiderDemoApplication.class, args);
    }
}
