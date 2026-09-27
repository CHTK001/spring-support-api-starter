package com.chua.starter.video.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * H2 配置库初始化。
 *
 * <p>通用 {@code db/init/*.sql} 为 MySQL 方言，在 H2 下不执行；本配置在 h2 profile 下
 * 直接执行视频配置表 DDL（{@code sys_setting}），且在 {@code ApplicationReadyEvent}
 * 触发的配置播种之前完成建表。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Configuration
@Profile("h2")
public class H2SchemaConfiguration {

    /**
     * 建表脚本路径。
     */
    private static final String SCHEMA = "db/init/video/h2/V1.0__init_video_h2.sql";

    /**
     * 注册 H2 配置库初始化器。
     *
     * @param dataSource 数据源
     * @return ApplicationRunner
     */
    @Bean
    public ApplicationRunner videoH2SchemaInitializer(DataSource dataSource) {
        return args -> {
            try {
                ResourceDatabasePopulator populator =
                        new ResourceDatabasePopulator(new ClassPathResource(SCHEMA));
                populator.setContinueOnError(true);
                populator.execute(dataSource);
                log.info("[Video] H2 配置库初始化完成: {}", SCHEMA);
            } catch (Exception e) {
                log.warn("[Video] H2 配置库初始化失败: {}", e.getMessage());
            }
        };
    }
}
