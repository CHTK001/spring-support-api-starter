CREATE TABLE IF NOT EXISTS sys_project (
    sys_project_id INT PRIMARY KEY,
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
    create_time TIMESTAMP NULL,
    update_time TIMESTAMP NULL,
    update_name VARCHAR(255),
    update_by INT
);

CREATE TABLE IF NOT EXISTS sys_ai_module (
    sys_ai_module_id INT PRIMARY KEY AUTO_INCREMENT,
    sys_ai_module_name VARCHAR(255),
    sys_ai_module_role_setting INT DEFAULT 0,
    sys_ai_module_code VARCHAR(255),
    sys_ai_module_type VARCHAR(64),
    sys_ai_module_url VARCHAR(255),
    sys_project_id INT,
    sys_ai_module_vlm INT DEFAULT 0,
    sys_ai_module_location TINYINT DEFAULT 0,
    sys_ai_module_manufacturers VARCHAR(64),
    sys_ai_module_status INT DEFAULT 1,
    sys_ai_module_sort INT DEFAULT 0,
    sys_ai_module_version VARCHAR(255),
    sys_ai_module_remark VARCHAR(255),
    create_name VARCHAR(255),
    create_by INT,
    create_time DATETIME NULL,
    update_time DATETIME NULL,
    update_name VARCHAR(255),
    update_by INT
);

CREATE TABLE IF NOT EXISTS sys_ai_vincent_setting (
    sys_ai_vincent_id INT PRIMARY KEY AUTO_INCREMENT,
    sys_ai_module_id INT,
    sys_ai_vincent_supported_size VARCHAR(255),
    sys_ai_vincent_supported_max_size INT,
    sys_ai_vincent_supported_style VARCHAR(255),
    sys_ai_vincent_supported_number INT,
    sys_ai_vincent_support_async INT DEFAULT 0,
    sys_ai_vincent_support_audio INT DEFAULT 0,
    sys_ai_vincent_supported_quality VARCHAR(255),
    sys_ai_vincent_supported_fps VARCHAR(255),
    sys_ai_vincent_support_ref_image INT DEFAULT 0,
    create_name VARCHAR(255),
    create_by INT,
    create_time DATETIME NULL,
    update_time DATETIME NULL,
    update_name VARCHAR(255),
    update_by INT
);

CREATE TABLE IF NOT EXISTS sys_ai_group (
    sys_ai_group_id INT PRIMARY KEY AUTO_INCREMENT,
    sys_ai_group_name VARCHAR(255),
    sys_project_id INT,
    sys_user_id VARCHAR(255),
    sys_ai_group_color VARCHAR(64),
    sys_ai_group_config TEXT,
    sys_ai_group_default INT DEFAULT 0,
    sys_ai_group_sort INT DEFAULT 0,
    create_name VARCHAR(255),
    create_by INT,
    create_time DATETIME NULL,
    update_time DATETIME NULL,
    update_name VARCHAR(255),
    update_by INT
);

CREATE TABLE IF NOT EXISTS sys_ai_group_message (
    sys_ai_group_message_id INT PRIMARY KEY AUTO_INCREMENT,
    sys_ai_group_id INT,
    sys_ai_group_message_role VARCHAR(64),
    sys_ai_group_message_content LONGTEXT,
    sys_ai_group_message_type VARCHAR(64),
    create_name VARCHAR(255),
    create_by INT,
    create_time DATETIME NULL,
    update_time DATETIME NULL,
    update_name VARCHAR(255),
    update_by INT
);

INSERT INTO sys_project (
    sys_project_id,
    sys_project_name,
    sys_project_code,
    sys_project_status,
    sys_project_delete,
    sys_project_endpoint,
    sys_project_vender,
    create_name,
    create_time,
    update_name,
    update_time
)
SELECT
    6,
    'AI测试项目',
    'ai-test-project',
    1,
    0,
    'https://api.openai.com/v1/chat/completions',
    'openai',
    'ai-test',
    CURRENT_TIMESTAMP,
    'ai-test',
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_project
    WHERE sys_project_id = 6
);

INSERT INTO sys_ai_module (
    sys_ai_module_id,
    sys_ai_module_name,
    sys_ai_module_role_setting,
    sys_ai_module_code,
    sys_ai_module_type,
    sys_ai_module_url,
    sys_project_id,
    sys_ai_module_vlm,
    sys_ai_module_location,
    sys_ai_module_manufacturers,
    sys_ai_module_status,
    sys_ai_module_sort,
    sys_ai_module_version,
    sys_ai_module_remark,
    create_name,
    create_by,
    create_time,
    update_time,
    update_name,
    update_by
)
SELECT
    1,
    'gpt-4o-mini',
    0,
    'gpt-4o-mini',
    'LLM',
    'https://api.openai.com/v1/chat/completions',
    6,
    0,
    0,
    'openai',
    1,
    0,
    'test',
    'AI测试模型',
    'ai-test',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'ai-test',
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_ai_module
    WHERE sys_ai_module_id = 1
);
