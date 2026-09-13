package com.chua.starter.system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * H2 数据库初始化器
 */
@Slf4j
@Component
public class H2DataInitializer implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 检查是否已经初始化过
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'SYS_USER'",
                Integer.class
            );
            if (count != null && count > 0) {
                log.info("[H2初始化] 数据库已存在表，跳过初始化");
                return;
            }

            log.info("[H2初始化] 开始创建表结构...");

            // 先删除旧表，确保结构最新
            String[] dropTables = {"sys_user", "sys_role", "sys_user_role", "sys_dept", "sys_menu",
                "sys_role_menu", "sys_dict", "sys_dict_item", "sys_setting", "sys_log", "sys_user_log",
                "sys_post", "sys_user_post", "sys_announcement", "sys_feedback", "sys_error_log",
                "sys_message", "sys_message_template", "sys_tenant", "sys_online", "sys_dept_principal_rel",
                "sys_setting_history", "sys_ip_blackwhite", "sys_user_lock"};
            for (String t : dropTables) {
                try { jdbcTemplate.execute("DROP TABLE IF EXISTS " + t); } catch (Exception e) { /* ignore */ }
            }

            // 用户表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_user (" +
                "sys_user_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_user_username VARCHAR(100) NOT NULL, " +
                "sys_user_password VARCHAR(200) NOT NULL, " +
                "sys_user_nickname VARCHAR(100), " +
                "sys_user_email VARCHAR(100), " +
                "sys_user_phone VARCHAR(20), " +
                "sys_user_sex INT DEFAULT 0, " +
                "sys_user_avatar VARCHAR(500), " +
                "sys_user_avatar_frame VARCHAR(500), " +
                "sys_user_avatar_frame_show INT DEFAULT 0, " +
                "sys_dept_id BIGINT, " +
                "sys_user_status INT DEFAULT 1, " +
                "sys_user_delete INT DEFAULT 0, " +
                "sys_user_in_system INT DEFAULT 0, " +
                "sys_user_lock INT DEFAULT 0, " +
                "sys_user_lock_time TIMESTAMP, " +
                "sys_user_login_count INT DEFAULT 0, " +
                "sys_user_last_login_time TIMESTAMP, " +
                "sys_user_last_ip VARCHAR(50), " +
                "sys_user_last_address VARCHAR(200), " +
                "sys_user_full_address VARCHAR(500), " +
                "sys_user_register_ip VARCHAR(50), " +
                "sys_user_register_address VARCHAR(200), " +
                "sys_user_password_update_time TIMESTAMP, " +
                "sys_user_must_change_password INT DEFAULT 0, " +
                "sys_user_totp_code VARCHAR(200), " +
                "sys_user_salt VARCHAR(100), " +
                "sys_user_card VARCHAR(50), " +
                "sys_user_remark VARCHAR(500), " +
                "sys_user_birthday TIMESTAMP, " +
                "sys_client_id VARCHAR(100), " +
                "sys_tenant_id BIGINT, " +
                "create_name VARCHAR(100), create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_name VARCHAR(100), update_by INT, update_time TIMESTAMP)");

            // 角色表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_role (" +
                "sys_role_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_role_name VARCHAR(100) NOT NULL, " +
                "sys_role_code VARCHAR(100) NOT NULL, " +
                "sys_role_status INT DEFAULT 1, " +
                "sys_role_sort INT DEFAULT 0, " +
                "sys_role_remark VARCHAR(500), " +
                "sys_role_in_system INT DEFAULT 0, " +
                "sys_role_data_scope INT DEFAULT 1, " +
                "sys_role_data_scope_dept_ids VARCHAR(500), " +
                "sys_role_readable INT DEFAULT 1, " +
                "sys_role_writeable INT DEFAULT 1, " +
                "sys_role_executable INT DEFAULT 1, " +
                "sys_role_board_card VARCHAR(500), " +
                "sys_role_readonly INT DEFAULT 0, " +
                "create_name VARCHAR(100), create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_name VARCHAR(100), update_by INT, update_time TIMESTAMP)");

            // 用户角色关联表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_user_role (" +
                "sys_user_role_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_user_id BIGINT, sys_role_id BIGINT)");

            // 部门表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_dept (" +
                "sys_dept_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_dept_name VARCHAR(100), sys_dept_pid BIGINT, " +
                "sys_dept_sort INT DEFAULT 0, sys_dept_status INT DEFAULT 1, " +
                "sys_dept_leader VARCHAR(50), sys_dept_phone VARCHAR(20), " +
                "sys_dept_email VARCHAR(100), sys_dept_delete INT DEFAULT 0, " +
                "sys_dept_principal VARCHAR(100), sys_dept_contact VARCHAR(200), " +
                "sys_dept_code VARCHAR(100), sys_dept_tree_id VARCHAR(255), " +
                "sys_dept_remark VARCHAR(500), " +
                "sys_dept_data_permission INT DEFAULT 1, " +
                "sys_dept_data_permission_dept_id BIGINT, " +
                "sys_dept_data_permission_dept_ids VARCHAR(500), " +
                "create_name VARCHAR(100), create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_name VARCHAR(100), update_by INT, update_time TIMESTAMP)");

            // 菜单表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_menu (" +
                "sys_menu_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_menu_name VARCHAR(255), sys_menu_path VARCHAR(255), " +
                "sys_menu_pid BIGINT, sys_menu_type INT, sys_menu_sort INT, " +
                "sys_menu_component VARCHAR(512), sys_menu_icon VARCHAR(255), " +
                "sys_menu_hidden INT DEFAULT 0, sys_menu_keep_alive INT DEFAULT 0, " +
                "sys_menu_perm VARCHAR(255), sys_menu_role VARCHAR(500), " +
                "sys_menu_home VARCHAR(255), sys_menu_engine VARCHAR(50), " +
                "sys_menu_title VARCHAR(255), sys_menu_active_path VARCHAR(255), " +
                "sys_menu_frame_src VARCHAR(500), sys_menu_i18n VARCHAR(255), " +
                "sys_menu_tree_id VARCHAR(255), sys_menu_extra_icon VARCHAR(255), " +
                "sys_menu_redirect VARCHAR(255), " +
                "sys_menu_enter_transition VARCHAR(100), sys_menu_leave_transition VARCHAR(100), " +
                "sys_menu_hidden_tag INT DEFAULT 0, sys_menu_fixed_tag INT DEFAULT 0, " +
                "sys_menu_is_tag INT DEFAULT 0, sys_menu_tag_type VARCHAR(50), " +
                "sys_menu_tag_name VARCHAR(100), sys_menu_show_parent INT DEFAULT 0, " +
                "create_name VARCHAR(100), create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_name VARCHAR(100), update_by INT, update_time TIMESTAMP)");

            // 角色菜单关联表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_role_menu (" +
                "sys_role_menu_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_role_id BIGINT, sys_menu_id BIGINT)");

            // 字典表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_dict (" +
                "sys_dict_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_dict_name VARCHAR(100), sys_dict_code VARCHAR(100), " +
                "sys_dict_status INT DEFAULT 1, sys_dict_remark VARCHAR(500), " +
                "sys_dict_in_system INT DEFAULT 0, sys_dict_pid BIGINT DEFAULT 0, " +
                "sys_dict_sort INT DEFAULT 0, sys_dict_i18n VARCHAR(255), " +
                "create_name VARCHAR(100), create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_name VARCHAR(100), update_by INT, update_time TIMESTAMP)");

            // 字典项表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_dict_item (" +
                "sys_dict_item_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_dict_id BIGINT, sys_dict_item_label VARCHAR(100), " +
                "sys_dict_item_value VARCHAR(200), sys_dict_item_sort INT DEFAULT 0, " +
                "sys_dict_item_status INT DEFAULT 1, " +
                "create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_by INT, update_time TIMESTAMP)");

            // 系统设置表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_setting (" +
                "sys_setting_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_setting_name VARCHAR(255), sys_setting_code VARCHAR(255), " +
                "sys_setting_value CLOB, sys_setting_group VARCHAR(255), " +
                "sys_setting_value_type VARCHAR(50), sys_setting_sort INT, " +
                "sys_setting_status INT DEFAULT 1, sys_setting_admin INT DEFAULT 0, " +
                "sys_setting_is_sensitive INT DEFAULT 0, " +
                "sys_setting_config CLOB, sys_setting_remark VARCHAR(500), " +
                "sys_setting_version INT DEFAULT 1, sys_setting_collect INT DEFAULT 0, " +
                "sys_setting_in_system INT DEFAULT 0, sys_setting_app_inner INT DEFAULT 0, " +
                "sys_setting_validate VARCHAR(500), " +
                "create_name VARCHAR(100), create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_name VARCHAR(100), update_by INT, update_time TIMESTAMP)");

            // 操作日志表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_log (" +
                "sys_log_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_log_username VARCHAR(255), sys_log_module VARCHAR(100), " +
                "sys_log_action VARCHAR(100), sys_log_method VARCHAR(500), " +
                "sys_log_url VARCHAR(500), sys_log_param CLOB, " +
                "sys_log_status INT DEFAULT 1, sys_log_error_msg CLOB, " +
                "sys_log_cost BIGINT, sys_log_ip VARCHAR(128), " +
                "sys_log_address VARCHAR(255), sys_log_browser VARCHAR(128), " +
                "sys_log_from VARCHAR(50), " +
                "create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 登录日志表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_user_log (" +
                "sys_log_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_log_username VARCHAR(255), sys_log_login_type VARCHAR(50), " +
                "sys_log_status INT, sys_log_message VARCHAR(2000), " +
                "sys_log_url VARCHAR(512), sys_log_cost BIGINT, " +
                "sys_log_ip VARCHAR(128), sys_log_address VARCHAR(255), " +
                "sys_log_browser VARCHAR(128), sys_log_system VARCHAR(128), " +
                "sys_log_from VARCHAR(50), sys_dept_id BIGINT, " +
                "create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 岗位表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_post (" +
                "sys_post_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_post_name VARCHAR(100), sys_post_code VARCHAR(100), " +
                "sys_post_status INT DEFAULT 1, sys_post_sort INT DEFAULT 0, " +
                "sys_post_remark VARCHAR(500), " +
                "create_name VARCHAR(100), create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_name VARCHAR(100), update_by INT, update_time TIMESTAMP)");

            // 用户岗位关联表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_user_post (" +
                "sys_user_post_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_user_id BIGINT, sys_post_id BIGINT)");

            // 消息表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_message (" +
                "sys_message_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_message_title VARCHAR(255), sys_message_content CLOB, " +
                "sys_message_type VARCHAR(50), sys_message_sender_id BIGINT, " +
                "sys_message_receiver_id BIGINT, sys_message_status INT DEFAULT 0, " +
                "sys_message_read INT DEFAULT 0, " +
                "create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_by INT, update_time TIMESTAMP)");

            // 异常日志表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_error_log (" +
                "sys_error_log_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_error_log_title VARCHAR(500), sys_error_log_content CLOB, " +
                "sys_error_log_stack CLOB, sys_error_log_source VARCHAR(100), " +
                "sys_error_log_status INT DEFAULT 0, " +
                "create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_by INT, update_time TIMESTAMP)");

            // 公告表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_announcement (" +
                "sys_announcement_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_announcement_title VARCHAR(255), sys_announcement_content CLOB, " +
                "sys_announcement_type VARCHAR(50), sys_announcement_status INT DEFAULT 0, " +
                "sys_announcement_level INT DEFAULT 0, sys_announcement_position VARCHAR(50), " +
                "sys_announcement_target_roles VARCHAR(500), sys_announcement_sort INT DEFAULT 0, " +
                "sys_announcement_publish_time TIMESTAMP, " +
                "sys_announcement_expire_time TIMESTAMP, " +
                "sys_announcement_target_url VARCHAR(500), " +
                "sys_announcement_publisher_name VARCHAR(100), " +
                "create_name VARCHAR(100), create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_name VARCHAR(100), update_by INT, update_time TIMESTAMP)");

            // 反馈表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_feedback (" +
                "sys_feedback_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_feedback_content CLOB, sys_feedback_type VARCHAR(50), " +
                "sys_feedback_status INT DEFAULT 0, sys_feedback_priority INT DEFAULT 1, " +
                "sys_feedback_user_id BIGINT, " +
                "create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_by INT, update_time TIMESTAMP)");

            // 租户表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_tenant (" +
                "sys_tenant_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_tenant_name VARCHAR(255), sys_tenant_code VARCHAR(100), " +
                "sys_tenant_status INT DEFAULT 1, sys_tenant_expire_time TIMESTAMP, " +
                "sys_tenant_delete INT DEFAULT 0, sys_tenant_contact VARCHAR(100), " +
                "sys_tenant_phone VARCHAR(20), sys_tenant_email VARCHAR(100), " +
                "sys_tenant_address VARCHAR(500), sys_tenant_remark VARCHAR(500), " +
                "create_name VARCHAR(100), create_by INT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_name VARCHAR(100), update_by INT, update_time TIMESTAMP)");

            // 部门负责人关联表
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_dept_principal_rel (" +
                "sys_dept_principal_rel_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "sys_dept_id BIGINT, sys_user_id BIGINT)");

            log.info("[H2初始化] 表结构创建完成");

            // 插入初始数据
            jdbcTemplate.execute("INSERT INTO sys_role (sys_role_name, sys_role_code, sys_role_status, sys_role_in_system, sys_role_data_scope) " +
                "VALUES ('Super Admin', 'SUPER_ADMIN', 1, 1, 1)");
            jdbcTemplate.execute("INSERT INTO sys_user (sys_user_username, sys_user_password, sys_user_nickname, sys_user_status, sys_user_in_system) " +
                "VALUES ('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'Administrator', 1, 1)");
            jdbcTemplate.execute("INSERT INTO sys_user_role (sys_user_id, sys_role_id) VALUES (1, 1)");
            jdbcTemplate.execute("INSERT INTO sys_dept (sys_dept_name, sys_dept_pid, sys_dept_sort, sys_dept_status) VALUES ('Root Dept', 0, 0, 1)");
            jdbcTemplate.execute("INSERT INTO sys_setting (sys_setting_name, sys_setting_code, sys_setting_value, sys_setting_group, sys_setting_value_type, sys_setting_sort, sys_setting_status) " +
                "VALUES ('System Name', 'system.name', 'System Platform', 'basic', 'string', 1, 1)");

            log.info("[H2初始化] 初始数据插入完成");
        } catch (Exception e) {
            log.error("[H2初始化] 初始化失败: {}", e.getMessage(), e);
        }
    }
}
