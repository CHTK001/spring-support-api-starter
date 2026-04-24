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
