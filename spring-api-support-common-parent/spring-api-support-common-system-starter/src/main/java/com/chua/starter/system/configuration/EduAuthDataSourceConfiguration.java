package com.chua.starter.system.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 学生档案数据源配置：为「学生登录」提供一条通往 eduai 库的只读通道。
 *
 * <p><b>为什么不把 edu_student 并进主数据源</b>：两库的表结构已各自演进
 * （实测 {@code sys_setting} 列已分叉、行数也不同），合并到同一个库有真实的数据风险；
 * 而临时切换数据源又依赖多数据源框架的具体注解与配置约定。
 * 这里改用<b>独立数据源</b>：语义最直白，与主库完全隔离，
 * 主库的 MyBatis 上下文里也不会出现一张它并不拥有的表。</p>
 *
 * <p><b>配置</b>（{@code plugin.edu-auth.datasource}）：</p>
 * <pre>
 * plugin:
 *   edu-auth:
 *     datasource:
 *       url: jdbc:mysql://127.0.0.1:3307/eduai?...
 *       username: eduai
 *       password: eduai123
 * </pre>
 *
 * <p><b>未配置或不连通时的行为</b>：本配置不做条件装配，而是由使用方的 yml
 * 提供默认值（见 {@code application-dev.yml} 的 {@code plugin.edu-auth.datasource}），
 * 因此 Bean 总会创建。Hikari 是<b>懒连接</b>——池在首次 {@code getConnection}
 * 时才建连接，所以即便 eduai 库暂时不可达，本服务仍能正常启动，
 * 只在学生登录那一刻失败；WEB 等其它登录方式完全不受影响。
 * 这比「启动即失败」更符合「一种登录方式出问题不该拖垮整个认证服务」的取舍。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
@Configuration
public class EduAuthDataSourceConfiguration {

    /**
     * 学生档案数据源名，供注入处按名限定，避免与主数据源冲突。
     */
    public static final String DATA_SOURCE_NAME = "eduAuthDataSource";

    /**
     * 学生档案 JdbcTemplate 名。
     */
    public static final String JDBC_TEMPLATE_NAME = "eduAuthJdbcTemplate";

    /**
     * 创建学生档案数据源。
     *
     * <p>用 Hikari 而非默认的简单池：登录是高频操作，
     * 每次请求新建连接会成为瓶颈；池大小对「只读单表查询」而言不需要大，
     * 默认值即可。</p>
     *
     * @return 数据源
     */
    @Bean(name = DATA_SOURCE_NAME)
    @ConfigurationProperties(prefix = "plugin.edu-auth.datasource")
    public DataSource eduAuthDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    /**
     * 创建学生档案 JdbcTemplate。
     *
     * @param dataSource 学生档案数据源
     * @return JdbcTemplate
     */
    @Bean(name = JDBC_TEMPLATE_NAME)
    public JdbcTemplate eduAuthJdbcTemplate(
            @Qualifier(DATA_SOURCE_NAME) DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
