package com.chua.starter.system.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 学生档案数据源配置：为「学生登录」提供一条通往 eduai 库的只读通道。
 *
 * <p><b>为什么把 edu_student 并进主数据源</b>：两库的表结构已各自演进
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
 * <p><b>条件装配</b>：当使用方没有提供 {@code plugin.edu-auth} 配置时（比如
 * 租户端），本配置不创建任何 Bean — 这很关键：如果没有 {@code eduAuthJdbcTemplate}，
 * Spring Boot 默认的 {@code JdbcTemplateAutoConfiguration}
 * 会创建默认的 {@code jdbcTemplate}（走 {@code spring.datasource}），
 * 注意这条与 {@code DefaultTenantDataSyncProvider} 这类类
 * （它们 {@code @RequiredArgsConstructor} 注入未限定名的 {@code JdbcTemplate}）
 * 直接相关；而 {@code @ConditionalOnMissingBean} 在此时才能通过——因此只用 {@code plugin.edu-auth}
 * 配置时的 Hikari 池会在首次 {@code getConnection} 时
 * 抛出 {@code IllegalArgumentException: dataSource or dataSourceClassName or jdbcUrl is required}，
 * 回到「所有同步数据落库都失败」的故障上。</p>
 *
 * <p><b>未配置时的行为</b>：没有 {@code eduAuthDataSource}/{@code eduAuthJdbcTemplate}，
 * 因此 {@code StudentLoginService}（{@link #StudentLoginService}）也不会出现，学生登录
 * （学号/家长手机号）不可用；WEB / TENANT / WECHAT 等其余登录方式完全不受影响。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
@Configuration
@ConditionalOnProperty(prefix = "plugin.edu-auth", name = "datasource.url", matchIfMissing = false)
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
     * 学生档案数据源参数（ {@code plugin.edu-auth.datasource.*} ）。
     *
     * <p>单独暴露为一个 Bean 再传给 {@link #eduAuthDataSource(EduAuthDataSourceProperties)}：
     * {@code plugin.edu-auth.datasource} 只能绑定到这个属性对象上（它有 {@code setUrl}），
     * 直接绑定到 {@code HikariDataSource} 上则没有 {@code setUrl} 可以接收（Spring Boot 4.1
     * 的 {@code HikariConfig} 只提供 {@code setJdbcUrl}），参数因此被静默忽略。</p>
     *
     * @return 数据源参数
     */
    @Bean
    @ConfigurationProperties(prefix = "plugin.edu-auth.datasource")
    public EduAuthDataSourceProperties eduAuthDataSourceProperties() {
        return new EduAuthDataSourceProperties();
    }

    /**
     * 创建学生档案数据源。
     *
     * <p>参数从 {@code plugin.edu-auth.datasource} 读回，显式装入 {@code HikariDataSource}：
     * Spring Boot 4.1 起 {@code HikariConfig} 只提供 {@code setJdbcUrl}（没有 {@code setUrl}），
     * 之前把 {@code plugin.edu-auth.datasource} 直接绑定给 {@code DataSourceBuilder} 产出的
     * {@code HikariDataSource}， {@code url} 找不到 setter，结果 {@code jdbcUrl} 为空，
     * 启动时报 {@code HikariPool-2 - jdbcUrl is required}（{@code /actuator/health} 因此 DOWN），
     * 还连带让 {@code DataSourceScriptBeanPostProcessor} 没有这个数据源可执行建表脚本。
     * 现在读回参数后通过 {@code setJdbcUrl}/{@code setUsername}/{@code setPassword}/{@code setDriverClassName}
     * 拼装，确保池能正常建立连接。</p>
     *
     * @param props 学生档案数据源参数
     * @return 数据源
     */
    @Bean(name = DATA_SOURCE_NAME)
    public DataSource eduAuthDataSource(EduAuthDataSourceProperties props) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(props.getUrl());
        dataSource.setUsername(props.getUsername());
        dataSource.setPassword(props.getPassword());
        dataSource.setDriverClassName(props.getDriverClassName());
        return dataSource;
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

    /**
     * 学生档案数据源参数，对应 {@code plugin.edu-auth.datasource} 下的属性。
     */
    public static class EduAuthDataSourceProperties {

        /**
         * 数据库连接地址（如 {@code jdbc:mysql://127.0.0.1:3306/eduai?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true}）。
         */
        private String url;

        /**
         * 数据库用户名。
         */
        private String username;

        /**
         * 数据库密码。
         */
        private String password;

        /**
         * 数据库驱动类名（如 {@code com.mysql.cj.jdbc.Driver}）。
         */
        private String driverClassName;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        /** getDriverClassName 方法 */
        public String getDriverClassName() {
            return driverClassName;
        }

        /** setDriverClassName 方法 */
        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }
    }
}
