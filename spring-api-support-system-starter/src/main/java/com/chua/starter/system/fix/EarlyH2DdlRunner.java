// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * H2 环境下，在 mapper bean 初始化之前，直接在所有 DataSource 上创建 AI 模块所需的最小表。
 * 不依赖外部 SQL 文件，避免语法兼容性问题。
 *
 * @author CH
 * @since 4.0.0.51
 */
@Profile("h2")
@Component
public class EarlyH2DdlRunner implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(EarlyH2DdlRunner.class);
    private final AtomicBoolean done = new AtomicBoolean(false);
    /**
     * application上下文
    */
    private ApplicationContext applicationContext;

    private static final String[] AI_TABLES = {
        "CREATE TABLE IF NOT EXISTS sys_ai_global_setting (" +
        "sys_ai_global_setting_id INT AUTO_INCREMENT PRIMARY KEY, " +
        "sys_ai_global_setting_group VARCHAR(64) DEFAULT 'default', " +
        "sys_ai_global_setting_field VARCHAR(128), " +
        "sys_ai_global_setting_value CLOB, " +
        "sys_ai_global_setting_value_type VARCHAR(32) DEFAULT 'STRING', " +
        "sys_ai_global_setting_description VARCHAR(512), " +
        "sys_ai_global_setting_sort INT DEFAULT 0, " +
        "sys_ai_global_setting_status INT DEFAULT 1, " +
        "create_name VARCHAR(255), create_by INT, " +
        "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        "update_name VARCHAR(255), update_by INT)"
        ,
        "CREATE TABLE IF NOT EXISTS sys_ai_requirement (" +
        "sys_ai_requirement_id VARCHAR(64) NOT NULL PRIMARY KEY, " +
        "sys_ai_requirement_display_no INT, " +
        "sys_ai_requirement_title VARCHAR(512), " +
        "sys_ai_requirement_content CLOB, " +
        "sys_ai_requirement_tag VARCHAR(128), " +
        "sys_ai_requirement_order_key VARCHAR(64), " +
        "sys_ai_requirement_status VARCHAR(32) DEFAULT 'pending', " +
        "sys_ai_requirement_completion_note VARCHAR(1024), " +
        "sys_ai_requirement_owner_conversation_id VARCHAR(64), " +
        "sys_ai_requirement_owner_terminal_id VARCHAR(64), " +
        "sys_ai_requirement_origin VARCHAR(32), " +
        "sys_ai_requirement_started_at BIGINT, " +
        "sys_ai_requirement_completed_at BIGINT, " +
        "sys_ai_requirement_attempt_count INT DEFAULT 0, " +
        "sys_ai_requirement_extra CLOB, " +
        "create_name VARCHAR(64), create_by INT, " +
        "create_time BIGINT, update_name VARCHAR(64), update_by INT, update_time BIGINT)"
        ,
        "CREATE TABLE IF NOT EXISTS sys_ai_conversation (" +
        "sys_ai_conversation_id VARCHAR(64) NOT NULL PRIMARY KEY, " +
        "sys_ai_conversation_bot_id VARCHAR(128), " +
        "sys_ai_conversation_title VARCHAR(255), " +
        "sys_ai_conversation_content CLOB, " +
        "sys_ai_conversation_status VARCHAR(32) DEFAULT 'active', " +
        "sys_ai_conversation_owner_id VARCHAR(64), " +
        "sys_ai_conversation_owner_name VARCHAR(128), " +
        "sys_ai_conversation_create_time BIGINT, " +
        "sys_ai_conversation_update_time BIGINT, " +
        "create_name VARCHAR(64), create_by INT, " +
        "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        "update_name VARCHAR(64), update_by INT, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        ,
        "CREATE TABLE IF NOT EXISTS sys_ai_conversation_message (" +
        "sys_ai_conversation_message_id VARCHAR(64) NOT NULL PRIMARY KEY, " +
        "sys_ai_conversation_message_conversation_id VARCHAR(64), " +
        "sys_ai_conversation_message_role VARCHAR(32), " +
        "sys_ai_conversation_message_content CLOB, " +
        "sys_ai_conversation_message_create_time BIGINT)"
    };

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (done.compareAndSet(false, true)) {
            Map<String, DataSource> dataSources = applicationContext.getBeansOfType(DataSource.class);
            log.info("[H2DDL] early DDL on {} datasource(s): {}", dataSources.size(), dataSources.keySet());
            for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
                try {
                    JdbcTemplate jdbc = new JdbcTemplate(entry.getValue());
                    for (String sql : AI_TABLES) {
                        jdbc.execute(sql);
                    }
                    log.info("[H2DDL] created AI tables on datasource: {}", entry.getKey());
                } catch (Exception e) {
                    log.warn("[H2DDL] failed on datasource {}: {}", entry.getKey(), e.getMessage());
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}