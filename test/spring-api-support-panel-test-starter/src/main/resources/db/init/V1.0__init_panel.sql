-- 运维项目管理表
CREATE TABLE IF NOT EXISTS sys_devops_project (
    project_id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_name          VARCHAR(100)  NOT NULL                COMMENT '项目名称',
    project_description   TEXT          NULL                    COMMENT '项目描述',
    project_icon          VARCHAR(255)  NULL                    COMMENT '项目图标URL',
    server_os             VARCHAR(10)   NULL DEFAULT 'unknown'  COMMENT '服务器OS类型',
    sort_order            INT           NOT NULL DEFAULT 0      COMMENT '排序',
    status                TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：1启用 0禁用',
    server_type           VARCHAR(10)   NULL                    COMMENT '连接类型：SSH/WINRM/LOCAL',
    server_host           VARCHAR(512)  NULL                    COMMENT '主机地址',
    server_port           INT           NULL                    COMMENT '端口号',
    server_username       VARCHAR(512)  NULL                    COMMENT '用户名',
    server_password       VARCHAR(1024) NULL                    COMMENT '密码',
    server_private_key    TEXT          NULL                    COMMENT 'SSH私钥',
    server_connect_timeout INT          NOT NULL DEFAULT 30     COMMENT '连接超时（秒）',
    server_enabled        TINYINT       NOT NULL DEFAULT 0      COMMENT '服务器信息是否激活',
    server_activated_at   DATETIME      NULL                    COMMENT '最近激活时间',
    log_path              VARCHAR(512)  NULL                    COMMENT '日志路径',
    config_path           VARCHAR(512)  NULL                    COMMENT '配置文件路径',
    lib_path              VARCHAR(512)  NULL                    COMMENT '依赖目录路径',
    root_path             VARCHAR(512)  NULL                    COMMENT '项目根目录',
    start_script          TEXT          NULL                    COMMENT '启动脚本',
    stop_script           TEXT          NULL                    COMMENT '停止脚本',
    restart_script        TEXT          NULL                    COMMENT '重启脚本',
    create_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by             VARCHAR(64)   NULL,
    update_by             VARCHAR(64)   NULL
);

-- 服务器主机表（避免测试后端在新库初始化时因 server_host 缺失而启动失败）
CREATE TABLE IF NOT EXISTS server_host (
    server_id             INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    server_name           VARCHAR(255)  NULL,
    server_code           VARCHAR(255)  NULL,
    server_type           VARCHAR(64)   NULL,
    server_os_type        VARCHAR(64)   NULL,
    server_architecture   VARCHAR(64)   NULL,
    server_host           VARCHAR(255)  NULL,
    server_public_ip      VARCHAR(255)  NULL,
    server_port           INT           NULL,
    server_username       VARCHAR(255)  NULL,
    server_password       VARCHAR(2048) NULL,
    server_private_key    LONGTEXT      NULL,
    server_base_directory VARCHAR(1024) NULL,
    server_tags           VARCHAR(1024) NULL,
    server_enabled        TINYINT(1)    DEFAULT 1,
    server_description    VARCHAR(1024) NULL,
    server_metadata_json  LONGTEXT      NULL,
    create_name           VARCHAR(255)  NULL,
    create_by             INT           NULL,
    create_time           DATETIME      NULL,
    update_time           DATETIME      NULL,
    update_name           VARCHAR(255)  NULL,
    update_by             INT           NULL
);

-- 系统配置分组表（用于系统配置初始化）
CREATE TABLE IF NOT EXISTS sys_setting_group (
    sys_setting_group_id INT AUTO_INCREMENT PRIMARY KEY,
    sys_setting_group_name VARCHAR(255),
    sys_setting_group_code VARCHAR(255),
    sys_setting_group_icon VARCHAR(255),
    sys_setting_group_enable BOOLEAN,
    sys_setting_group_remark VARCHAR(1000),
    sys_setting_group_sort INT,
    sys_setting_group_use_project_interface BOOLEAN,
    create_name VARCHAR(255),
    create_by INT,
    create_time TIMESTAMP,
    update_name VARCHAR(255),
    update_by INT,
    update_time TIMESTAMP
);

-- 系统配置表（用于系统配置初始化）
CREATE TABLE IF NOT EXISTS sys_setting (
    sys_setting_id INT AUTO_INCREMENT PRIMARY KEY,
    sys_setting_name VARCHAR(255),
    sys_setting_value VARCHAR(255),
    sys_setting_config VARCHAR(255),
    sys_setting_remark VARCHAR(255),
    sys_setting_sort INT,
    sys_setting_version INT,
    sys_setting_status INT,
    sys_setting_admin INT,
    sys_setting_value_type VARCHAR(255),
    sys_setting_group VARCHAR(255),
    sys_setting_collect INT,
    sys_setting_in_system INT,
    sys_setting_app_inner INT,
    create_name VARCHAR(255),
    create_by INT,
    create_time TIMESTAMP,
    update_name VARCHAR(255),
    update_by INT,
    update_time TIMESTAMP
);

-- 项目管理基础表（用于项目管理页面远程项目解析）
CREATE TABLE IF NOT EXISTS sys_project (
    sys_project_id INT AUTO_INCREMENT PRIMARY KEY,
    sys_project_name VARCHAR(255),
    sys_project_icon TEXT,
    sys_project_group VARCHAR(255),
    sys_project_code VARCHAR(255),
    sys_project_app_id VARCHAR(255),
    sys_project_app_secret VARCHAR(255),
    sys_project_sign VARCHAR(255),
    sys_project_app_key VARCHAR(255),
    sys_project_sign_code VARCHAR(255),
    sys_project_delete INT DEFAULT 0,
    sys_project_status INT DEFAULT 1,
    sys_project_sort INT DEFAULT 0,
    sys_project_cdn VARCHAR(255),
    sys_project_endpoint VARCHAR(255),
    sys_project_local_path VARCHAR(255),
    sys_project_api_address VARCHAR(255),
    sys_project_location_temp_path VARCHAR(255),
    sys_project_location_oss_address VARCHAR(255),
    sys_project_vender VARCHAR(64),
    sys_project_remark VARCHAR(255),
    sys_project_function VARCHAR(255),
    sys_project_smtp_host VARCHAR(255),
    sys_project_smtp_port INT,
    sys_project_smtp_password VARCHAR(255),
    sys_project_smtp_from VARCHAR(255),
    create_name VARCHAR(255),
    create_by INT,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    update_name VARCHAR(255),
    update_by INT
);

-- 项目默认配置表（兼容 SysProjectMapper 子查询）
CREATE TABLE IF NOT EXISTS sys_project_default (
    sys_project_default_id INT AUTO_INCREMENT PRIMARY KEY,
    sys_project_default_type VARCHAR(255),
    sys_project_id INT
);

-- 项目容器软件预设表
CREATE TABLE IF NOT EXISTS sys_project_container_software_preset (
    sys_project_container_software_preset_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    software_code VARCHAR(128) NULL,
    service_name VARCHAR(255) NULL COMMENT '服务名称(安装后的系统服务名)',
    software_name VARCHAR(255) NULL,
    software_category VARCHAR(64) NULL,
    preset_type VARCHAR(32) NULL COMMENT '预设类型：ENVIRONMENT-环境(仅安装/卸载/状态) SOFTWARE-软件(完整生命周期) DOCKERFILE-Dockerfile构建(仅安装)',
    software_version VARCHAR(64) NULL,
    software_icon VARCHAR(255) NULL,
    software_vendor VARCHAR(128) NULL,
    os_type VARCHAR(32) NULL,
    software_arch VARCHAR(32) NULL,
    install_path VARCHAR(255) NULL,
    expose_ports VARCHAR(255) NULL,
    has_port_mapping TINYINT(1) DEFAULT 0 COMMENT '是否需要端口映射',
    install_script LONGTEXT NULL,
    start_script LONGTEXT NULL,
    stop_script LONGTEXT NULL,
    restart_script LONGTEXT NULL,
    status_script LONGTEXT NULL,
    uninstall_script LONGTEXT NULL,
    preset_builtin INT DEFAULT 1,
    preset_delete INT DEFAULT 0,
    preset_status INT DEFAULT 1,
    preset_sort INT DEFAULT 0,
    preset_remark LONGTEXT NULL,
    system_env_vars LONGTEXT NULL COMMENT 'System 模式环境变量 JSON',
    docker_env_vars LONGTEXT NULL COMMENT 'Docker 模式环境变量 JSON',
    create_name VARCHAR(255) NULL,
    create_by INT NULL,
    create_time TIMESTAMP NULL,
    update_name VARCHAR(255) NULL,
    update_by INT NULL,
    update_time TIMESTAMP NULL
);

-- 项目容器软件关联表
CREATE TABLE IF NOT EXISTS sys_project_container_software_binding (
    sys_project_container_software_binding_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sys_project_id INT NULL,
    sys_project_container_software_preset_id INT NULL,
    software_code VARCHAR(128) NULL,
    software_name VARCHAR(255) NULL,
    service_name VARCHAR(255) NULL COMMENT '服务名称(安装后的系统服务名)',
    software_category VARCHAR(64) NULL,
    software_version VARCHAR(64) NULL,
    software_icon VARCHAR(255) NULL,
    software_vendor VARCHAR(128) NULL,
    os_type VARCHAR(32) NULL,
    software_arch VARCHAR(32) NULL,
    install_path VARCHAR(255) NULL,
    expose_ports VARCHAR(255) NULL,
    has_port_mapping TINYINT(1) DEFAULT 0 COMMENT '是否需要端口映射',
    install_script LONGTEXT NULL,
    start_script LONGTEXT NULL,
    stop_script LONGTEXT NULL,
    restart_script LONGTEXT NULL,
    status_script LONGTEXT NULL,
    uninstall_script LONGTEXT NULL,
    install_status VARCHAR(32) NULL,
    runtime_status VARCHAR(32) NULL,
    binding_custom INT DEFAULT 0,
    binding_delete INT DEFAULT 0,
    binding_status INT DEFAULT 1,
    binding_sort INT DEFAULT 0,
    binding_remark LONGTEXT NULL,
    system_env_vars LONGTEXT NULL COMMENT 'System 模式环境变量 JSON',
    docker_env_vars LONGTEXT NULL COMMENT 'Docker 模式环境变量 JSON',
    create_name VARCHAR(255) NULL,
    create_by INT NULL,
    create_time TIMESTAMP NULL,
    update_name VARCHAR(255) NULL,
    update_by INT NULL,
    update_time TIMESTAMP NULL
);

-- 项目容器服务编排表
CREATE TABLE IF NOT EXISTS sys_project_container_service_topology (
    sys_project_container_service_topology_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sys_project_id INT NULL,
    topology_name VARCHAR(255) NULL,
    editor_nodes_json LONGTEXT NULL,
    editor_edges_json LONGTEXT NULL,
    imported_projects_json LONGTEXT NULL,
    node_config_json LONGTEXT NULL,
    topology_delete INT DEFAULT 0,
    topology_status INT DEFAULT 1,
    topology_remark VARCHAR(500) NULL,
    create_name VARCHAR(255) NULL,
    create_by INT NULL,
    create_time TIMESTAMP NULL,
    update_name VARCHAR(255) NULL,
    update_by INT NULL,
    update_time TIMESTAMP NULL
);

-- ============================================================
-- 增量迁移：为可能已存在的表补充新字段（先于数据插入）
-- 使用 information_schema 兼容 MySQL 5.7+（避免 ADD COLUMN IF NOT EXISTS 语法差异）
-- ============================================================

-- sys_project_container_software_preset 表
SET @p = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_project_container_software_preset' AND COLUMN_NAME = 'service_name');
SET @s = IF(@p = 0, 'ALTER TABLE sys_project_container_software_preset ADD COLUMN service_name VARCHAR(255) NULL AFTER software_code', 'SELECT 1');
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @p = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_project_container_software_preset' AND COLUMN_NAME = 'has_port_mapping');
SET @s = IF(@p = 0, 'ALTER TABLE sys_project_container_software_preset ADD COLUMN has_port_mapping TINYINT(1) DEFAULT 0 AFTER expose_ports', 'SELECT 1');
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @p = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_project_container_software_preset' AND COLUMN_NAME = 'system_env_vars');
SET @s = IF(@p = 0, 'ALTER TABLE sys_project_container_software_preset ADD COLUMN system_env_vars LONGTEXT NULL AFTER preset_remark', 'SELECT 1');
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @p = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_project_container_software_preset' AND COLUMN_NAME = 'docker_env_vars');
SET @s = IF(@p = 0, 'ALTER TABLE sys_project_container_software_preset ADD COLUMN docker_env_vars LONGTEXT NULL AFTER system_env_vars', 'SELECT 1');
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- sys_project_container_software_binding 表
SET @p = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_project_container_software_binding' AND COLUMN_NAME = 'service_name');
SET @s = IF(@p = 0, 'ALTER TABLE sys_project_container_software_binding ADD COLUMN service_name VARCHAR(255) NULL AFTER software_name', 'SELECT 1');
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @p = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_project_container_software_binding' AND COLUMN_NAME = 'has_port_mapping');
SET @s = IF(@p = 0, 'ALTER TABLE sys_project_container_software_binding ADD COLUMN has_port_mapping TINYINT(1) DEFAULT 0 AFTER expose_ports', 'SELECT 1');
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @p = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_project_container_software_binding' AND COLUMN_NAME = 'system_env_vars');
SET @s = IF(@p = 0, 'ALTER TABLE sys_project_container_software_binding ADD COLUMN system_env_vars LONGTEXT NULL AFTER binding_remark', 'SELECT 1');
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @p = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_project_container_software_binding' AND COLUMN_NAME = 'docker_env_vars');
SET @s = IF(@p = 0, 'ALTER TABLE sys_project_container_software_binding ADD COLUMN docker_env_vars LONGTEXT NULL AFTER system_env_vars', 'SELECT 1');
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
