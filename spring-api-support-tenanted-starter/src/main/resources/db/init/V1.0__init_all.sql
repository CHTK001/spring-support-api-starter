-- =====================================================================
-- V1.0__init_all.sql - spring-api-support-tenanted-starter 全量 DDL（合并自 1 个文件）
-- 通道: INIT 同步建表；老库缺列/缺表由 information_schema + PREPARE 段幂等补齐。
-- 种子数据见 V1.0__initdata_all.sql（虚拟线程异步，不阻塞启动）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 来源: V0.0.0__h2_compat.sql
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_menu (sys_menu_id BIGINT AUTO_INCREMENT PRIMARY KEY, sys_menu_name VARCHAR(255), sys_menu_path VARCHAR(255), sys_menu_pid BIGINT, sys_menu_type INT, sys_menu_sort INT, sys_menu_component VARCHAR(512), sys_menu_icon VARCHAR(255), sys_menu_hidden INT DEFAULT 0, sys_menu_keep_alive INT DEFAULT 0, create_by INT, create_time TIMESTAMP, update_by INT, update_time TIMESTAMP);

CREATE TABLE IF NOT EXISTS sys_role_menu (sys_role_menu_id BIGINT AUTO_INCREMENT PRIMARY KEY, sys_role_id BIGINT, sys_menu_id BIGINT);

CREATE TABLE IF NOT EXISTS sys_user_log (sys_log_id BIGINT AUTO_INCREMENT PRIMARY KEY, sys_log_username VARCHAR(255), sys_log_type VARCHAR(50), sys_log_status INT, sys_log_error_msg VARCHAR(2000), sys_log_url VARCHAR(512), sys_log_call_time BIGINT, create_by INT, create_time TIMESTAMP);

CREATE TABLE IF NOT EXISTS sys_setting (sys_setting_id BIGINT AUTO_INCREMENT PRIMARY KEY, sys_setting_name VARCHAR(255), sys_setting_code VARCHAR(255), sys_setting_value VARCHAR(2000), sys_setting_group VARCHAR(255), sys_setting_value_type VARCHAR(50), sys_setting_sort INT, sys_setting_status INT DEFAULT 1, create_by INT, create_time TIMESTAMP);

CREATE TABLE IF NOT EXISTS sys_setting_group (sys_setting_group_id BIGINT AUTO_INCREMENT PRIMARY KEY, sys_setting_group_name VARCHAR(255), sys_setting_group_code VARCHAR(255), sys_setting_group_sort INT, sys_setting_group_enable INT DEFAULT 1, create_by INT, create_time TIMESTAMP);

CREATE TABLE IF NOT EXISTS sys_file_system_group (sys_file_system_group_id INT AUTO_INCREMENT PRIMARY KEY, sys_file_system_group_name VARCHAR(100), sys_file_system_group_parent_id INT DEFAULT 0, create_by INT, create_time TIMESTAMP);

CREATE TABLE IF NOT EXISTS sys_job_cluster (sys_job_cluster_id VARCHAR(64) PRIMARY KEY, sys_job_cluster_leader_node VARCHAR(255), sys_job_cluster_lease_until_ms BIGINT, sys_job_cluster_version INT);

