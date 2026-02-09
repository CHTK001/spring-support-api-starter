package com.chua.starter.monitor.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis 会话配置
 *
 * @author CH
 * @version 1.0.0
 * @since 2026/02/06
 */
@Slf4j
@Configuration
public class MybatisSessionConfiguration {

    /**
     * 创建 SqlSessionFactory
     *
     * @param dataSource 数据源
     * @return SqlSessionFactory
     * @throws Exception 创建失败时抛出异常
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        log.info("[MyBatis] 开始创建 SqlSessionFactory");
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*.xml")
        );
        SqlSessionFactory sqlSessionFactory = factoryBean.getObject();
        log.info("[MyBatis] SqlSessionFactory 创建完成");
        return sqlSessionFactory;
    }

    /**
     * 创建 SqlSessionTemplate
     *
     * @param sqlSessionFactory SqlSessionFactory
     * @return SqlSessionTemplate
     */
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        log.info("[MyBatis] 创建 SqlSessionTemplate");
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}

