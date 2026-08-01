package com.chua.spider.support.configuration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 爬虫中心自动配置。
 *
 * <p>扫描 {@code com.chua.spider.support} 包下的组件，
 * 并注册 {@code com.chua.spider.support.mapper} 包下的 MyBatis-Plus Mapper。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@AutoConfiguration
@ComponentScan({"com.chua.spider.support"})
@MapperScan({"com.chua.spider.support.mapper"})
public class SpiderAutoConfiguration {
}
