package com.chua.starter.spider.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 为 AI Starter 提供 {@code licenceJdbcTemplate} Bean，复用主数据源。
 */
@Configuration
public class SpiderTestLicenceConfig {

    @Bean
    public JdbcTemplate licenceJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
