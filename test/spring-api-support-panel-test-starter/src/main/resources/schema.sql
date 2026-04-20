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
    sys_project_icon CLOB,
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

-- 预置：用于远程项目解析的测试项目（endpoint -> sysProjectId）
INSERT INTO sys_project (
    sys_project_name,
    sys_project_code,
    sys_project_app_id,
    sys_project_app_secret,
    sys_project_status,
    sys_project_delete,
    sys_project_endpoint,
    sys_project_vender,
    sys_project_function,
    create_name,
    create_time,
    update_name,
    update_time
)
SELECT
    '测试nginx',
    'panel-test-nginx-001',
    'root',
    'Yaq.K11qiRenWen>a<6XsJlbEN',
    1,
    0,
    'ssh://8.139.4.229:22',
    'panel-test',
    '16',
    'panel-test',
    CURRENT_TIMESTAMP,
    'panel-test',
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_project
    WHERE sys_project_endpoint = 'ssh://8.139.4.229:22'
);
