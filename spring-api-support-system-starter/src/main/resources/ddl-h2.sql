-- H2 early DDL (CREATE TABLE IF NOT EXISTS only)
CREATE TABLE sys_avatar_frame (
    sys_avatar_frame_id int NOT NULL AUTO_INCREMENT COMMENT '婵犮垼鍩栧娆撳磿濮橆剦娴栭柛婊呯槡',
    sys_avatar_frame_name varchar(100) NOT NULL COMMENT '婵犮垼鍩栧娆撳磿濮橆剦娴栭柛鈩冾殕閸婂磭绱?,
    sys_avatar_frame_type varchar(50) DEFAULT 'base' COMMENT '缂備緡鍋夐褔鎮? base-闂佺硶鏅炲▍锝夈€?birthday-闂佹眹鍨婚崰鎰?event-濠电偠灏褎鎱?,
    sys_avatar_frame_image varchar(500) DEFAULT NULL COMMENT '婵犮垼鍩栧娆撳磿濮橆剦娴栭柛鈩冾殕缁傚牓鏌ｅΔ鈧Σ鐢奓',
    sys_avatar_frame_color varchar(200) DEFAULT NULL COMMENT '濠电偞鎸婚崝鏇°亹婢舵劖鍤勯悘鐐靛亾閻濐垶鏌?css)',
    sys_avatar_frame_desc varchar(500) DEFAULT NULL COMMENT '闂佺顕х换妤呭醇?,
    sys_avatar_frame_acquire varchar(50) DEFAULT 'default' COMMENT '闂佸吋鍎抽崲鑼躲亹閸ヮ剚鐒婚柡鍌濇硶缁? default-婵帗绋掗…鍫ヮ敇?birthday-闂佹眹鍨婚崰鎰?achievement-闂佺懓鐡ㄩ崝鏇烆潩?,
    sys_avatar_frame_sort int DEFAULT 0 COMMENT '闂佸湱鍎ょ敮鎺旇姳?,
    sys_avatar_frame_status int DEFAULT 1 COMMENT '闂佺粯顭堥崺鏍焵? 0-缂備礁鍊烽懗鍫曞极?1-闂佸憡鍑归崹鎶藉极?,
    create_name varchar(255) DEFAULT NULL COMMENT '闂佸憡甯楃粙鎴犵磽閹惧顩查柛婵嗗濞硷綁鏌?,
    create_by int DEFAULT NULL COMMENT '闂佸憡甯楃粙鎴犵磽閹惧顩?,
    create_time datetime DEFAULT NULL COMMENT '闂佸憡甯楃粙鎴犵磽閹捐绫嶉柛顐ｆ礃閿?,
    update_time datetime DEFAULT NULL COMMENT '闂佸搫娲ら悺銊╁蓟婵犲洤绫嶉柛顐ｆ礃閿?,
    update_name varchar(255) DEFAULT NULL COMMENT '闂佸搫娲ら悺銊╁蓟婵犲啰顩查柛婵嗗濞硷綁鏌?,
    update_by int DEFAULT NULL COMMENT '闂佸搫娲ら悺銊╁蓟婵犲啰顩?,
    PRIMARY KEY (sys_avatar_frame_id) USING BTREE,
    KEY idx_type (sys_avatar_frame_type) USING BTREE,
    KEY idx_status (sys_avatar_frame_status) USING BTREE
) ;

CREATE TABLE sys_user_avatar_frame (
    sys_user_avatar_frame_id int NOT NULL AUTO_INCREMENT COMMENT '闂佹椿娼块崝宥夊春濞戞瑥绶為柡澶嬪灥閸撳吋淇婂Δ鈧幑鍑?,
    sys_user_id int NOT NULL COMMENT '闂佹椿娼块崝宥夊春濮圭拏',
    sys_avatar_frame_id int NOT NULL COMMENT '婵犮垼鍩栧娆撳磿濮橆剦娴栭柛婊呯槡',
    acquire_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '闂佸吋鍎抽崲鑼躲亹閸ヮ剙绫嶉柛顐ｆ礃閿?,
    expire_time datetime DEFAULT NULL COMMENT '闁哄鏅涘ú锕€锕㈤敓鐘茬睄闁割偅娲橀敍?NULL=濠殿喗锕㈤弲鑼不?',
    create_name varchar(255) DEFAULT NULL COMMENT '闂佸憡甯楃粙鎴犵磽閹惧顩查柛婵嗗濞硷綁鏌?,
    create_by int DEFAULT NULL COMMENT '闂佸憡甯楃粙鎴犵磽閹惧顩?,
    create_time datetime DEFAULT NULL COMMENT '闂佸憡甯楃粙鎴犵磽閹捐绫嶉柛顐ｆ礃閿?,
    update_time datetime DEFAULT NULL COMMENT '闂佸搫娲ら悺銊╁蓟婵犲洤绫嶉柛顐ｆ礃閿?,
    update_name varchar(255) DEFAULT NULL COMMENT '闂佸搫娲ら悺銊╁蓟婵犲啰顩查柛婵嗗濞硷綁鏌?,
    update_by int DEFAULT NULL COMMENT '闂佸搫娲ら悺銊╁蓟婵犲啰顩?,
    PRIMARY KEY (sys_user_avatar_frame_id) USING BTREE,
    UNIQUE KEY uk_user_frame (sys_user_id, sys_avatar_frame_id) USING BTREE,
    KEY idx_user_id (sys_user_id) USING BTREE,
    KEY idx_frame_id (sys_avatar_frame_id) USING BTREE
) ;

CREATE TABLE IF NOT EXISTS sys_ai_bot_binding (
  sys_ai_bot_binding_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_bot_binding_user_id BIGINT,
  sys_ai_bot_binding_owner_key VARCHAR(128),
  sys_ai_bot_binding_bot_id VARCHAR(128),
  sys_ai_bot_binding_bot_token VARCHAR(512),
  sys_ai_bot_binding_platform VARCHAR(32),
  sys_ai_bot_binding_nickname VARCHAR(128),
  sys_ai_bot_binding_avatar_url VARCHAR(512),
  sys_ai_bot_binding_gateway_port INT,
  sys_ai_bot_binding_status VARCHAR(32) DEFAULT 'offline',
  sys_ai_bot_binding_bind_source VARCHAR(32),
  sys_ai_bot_binding_model VARCHAR(128),
  sys_ai_bot_binding_thinking TINYINT DEFAULT 0,
  sys_ai_bot_binding_thinking_effort VARCHAR(16),
  sys_ai_bot_binding_system_prompt CLOB,
  sys_ai_bot_binding_record_usage TINYINT DEFAULT 1,
  sys_ai_bot_binding_context_thinking TINYINT,
  sys_ai_bot_binding_context_size INT,
  sys_ai_bot_binding_history_size INT,
  sys_ai_bot_binding_command_enabled TINYINT,
  sys_ai_bot_binding_last_active_time TIMESTAMP,
  create_name VARCHAR(64),
  create_by INT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_name VARCHAR(64),
  update_by INT,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_global_setting (
  sys_ai_global_setting_id INT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_global_setting_group VARCHAR(64) DEFAULT 'default',
  sys_ai_global_setting_field VARCHAR(128),
  sys_ai_global_setting_value CLOB,
  sys_ai_global_setting_value_type VARCHAR(32) DEFAULT 'STRING',
  sys_ai_global_setting_description VARCHAR(512),
  sys_ai_global_setting_sort INT DEFAULT 0,
  sys_ai_global_setting_status INT DEFAULT 1,
  create_name VARCHAR(255),
  create_by INT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_name VARCHAR(255),
  update_by INT
);

CREATE TABLE IF NOT EXISTS sys_ai_usage (
  sys_ai_usage_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_usage_group_name VARCHAR(32),
  sys_ai_usage_provider VARCHAR(64),
  sys_ai_usage_model VARCHAR(128),
  sys_ai_usage_call_type VARCHAR(16),
  sys_ai_usage_prompt_tokens INT,
  sys_ai_usage_completion_tokens INT,
  sys_ai_usage_total_tokens INT,
  sys_ai_usage_input_cost DOUBLE,
  sys_ai_usage_output_cost DOUBLE,
  sys_ai_usage_total_cost DOUBLE,
  sys_ai_usage_request_id VARCHAR(128),
  sys_ai_usage_user_id VARCHAR(64),
  sys_ai_usage_call_time TIMESTAMP,
  sys_ai_usage_inference_strength VARCHAR(32),
  sys_ai_usage_endpoint VARCHAR(128),
  sys_ai_usage_ip VARCHAR(64),
  sys_ai_usage_type VARCHAR(16),
  sys_ai_usage_billing_mode VARCHAR(16),
  sys_ai_usage_cached_tokens INT,
  sys_ai_usage_cached_cost DOUBLE,
  sys_ai_usage_input_price_per_1m DOUBLE,
  sys_ai_usage_output_price_per_1m DOUBLE,
  sys_ai_usage_cache_read_price_per_1m DOUBLE,
  sys_ai_usage_service_tier VARCHAR(32),
  sys_ai_usage_ratio DOUBLE,
  sys_ai_usage_original_cost DOUBLE,
  sys_ai_usage_first_token_latency BIGINT,
  sys_ai_usage_total_latency BIGINT,
  create_name VARCHAR(64),
  create_by INT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_name VARCHAR(64),
  update_by INT
);

CREATE TABLE IF NOT EXISTS sys_ai_usage_daily (
  sys_ai_usage_daily_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_usage_daily_stat_date DATE,
  sys_ai_usage_daily_group_name VARCHAR(64),
  sys_ai_usage_daily_provider VARCHAR(64),
  sys_ai_usage_daily_model VARCHAR(128),
  sys_ai_usage_daily_total_calls BIGINT DEFAULT 0,
  sys_ai_usage_daily_total_prompt_tokens BIGINT DEFAULT 0,
  sys_ai_usage_daily_total_completion_tokens BIGINT DEFAULT 0,
  sys_ai_usage_daily_total_tokens BIGINT DEFAULT 0,
  sys_ai_usage_daily_total_cost DOUBLE DEFAULT 0,
  sys_ai_usage_daily_total_cached_tokens BIGINT DEFAULT 0,
  sys_ai_usage_daily_total_latency BIGINT DEFAULT 0,
  sys_ai_usage_daily_total_first_token_latency BIGINT DEFAULT 0,
  sys_ai_usage_daily_standard_cost DOUBLE DEFAULT 0,
  create_name VARCHAR(64),
  create_by VARCHAR(64),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_name VARCHAR(64),
  update_by VARCHAR(64),
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_pricing (
  sys_ai_pricing_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_pricing_provider VARCHAR(64),
  sys_ai_pricing_model VARCHAR(128),
  sys_ai_pricing_input_price_per_1k DOUBLE,
  sys_ai_pricing_output_price_per_1k DOUBLE,
  sys_ai_pricing_currency VARCHAR(8),
  sys_ai_pricing_effective_date TIMESTAMP,
  sys_ai_pricing_source VARCHAR(32),
  create_name VARCHAR(64),
  create_by INT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_name VARCHAR(64),
  update_by INT,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_manufacturer (
  sys_ai_manufacturer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_manufacturer_name VARCHAR(64),
  sys_ai_manufacturer_code VARCHAR(64),
  sys_ai_manufacturer_status INT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_manufacturer_key (
  sys_ai_manufacturer_key_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_manufacturer_key_manufacturer_id BIGINT,
  sys_ai_manufacturer_key_key VARCHAR(256),
  sys_ai_manufacturer_key_status INT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_model_setting (
  sys_ai_model_setting_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_model_setting_bot_id VARCHAR(128),
  sys_ai_model_setting_field VARCHAR(128),
  sys_ai_model_setting_value CLOB,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_app_key (
  sys_ai_app_key_id INT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_app_key_app_id VARCHAR(128),
  sys_ai_app_key_app_key VARCHAR(128),
  sys_ai_app_key_secret VARCHAR(256),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_ssh_host (
  sys_ai_ssh_host_id             VARCHAR(36) NOT NULL PRIMARY KEY,
  sys_ai_ssh_host_name           VARCHAR(255),
  sys_ai_ssh_host_host           VARCHAR(255),
  sys_ai_ssh_host_port           INT DEFAULT 22,
  sys_ai_ssh_host_username       VARCHAR(128),
  sys_ai_ssh_host_auth_type      VARCHAR(32) DEFAULT 'password',
  sys_ai_ssh_host_password       VARCHAR(1024),
  sys_ai_ssh_host_private_key    CLOB,
  sys_ai_ssh_host_passphrase     VARCHAR(1024),
  sys_ai_ssh_host_certificate    CLOB,
  sys_ai_ssh_host_sudo_password  VARCHAR(1024),
  sys_ai_ssh_host_host_fingerprint VARCHAR(512),
  sys_ai_ssh_host_status         VARCHAR(32) DEFAULT 'unknown',
  sys_ai_ssh_host_last_connected_at BIGINT,
  sys_ai_ssh_host_created_at     BIGINT NOT NULL,
  sys_ai_ssh_host_updated_at     BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_ai_agent (
  sys_ai_agent_id              VARCHAR(64) NOT NULL PRIMARY KEY,
  sys_ai_agent_parent_id       VARCHAR(64),
  sys_ai_agent_name            VARCHAR(128) NOT NULL,
  sys_ai_agent_description     VARCHAR(512),
  sys_ai_agent_role            VARCHAR(255),
  sys_ai_agent_instruction     CLOB,
  sys_ai_agent_system_prompt   CLOB,
  sys_ai_agent_provider_id     VARCHAR(64),
  sys_ai_agent_model           VARCHAR(255),
  sys_ai_agent_mcp_ids         CLOB,
  sys_ai_agent_skill_ids       CLOB,
  sys_ai_agent_strategy        CLOB,
  sys_ai_agent_enabled         TINYINT DEFAULT 1,
  sys_ai_agent_sort_order      INT DEFAULT 0,
  sys_ai_agent_create_time     BIGINT NOT NULL,
  sys_ai_agent_update_time     BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_ai_mcp_server (
  sys_ai_mcp_server_id          VARCHAR(36) NOT NULL PRIMARY KEY,
  sys_ai_mcp_server_name        VARCHAR(255) NOT NULL,
  sys_ai_mcp_server_description VARCHAR(512),
  sys_ai_mcp_server_transport   CLOB NOT NULL,
  sys_ai_mcp_server_original_json CLOB,
  sys_ai_mcp_server_builtin    TINYINT DEFAULT 0,
  sys_ai_mcp_server_enabled    TINYINT DEFAULT 1,
  sys_ai_mcp_server_last_test_status VARCHAR(32),
  sys_ai_mcp_server_last_connected_at BIGINT,
  sys_ai_mcp_server_created_at BIGINT,
  sys_ai_mcp_server_updated_at  BIGINT
);

CREATE TABLE IF NOT EXISTS sys_ai_bot_message (
  sys_ai_bot_message_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_bot_message_bot_id   VARCHAR(128),
  sys_ai_bot_message_direction VARCHAR(16),
  sys_ai_bot_message_from_user VARCHAR(128),
  sys_ai_bot_message_content  CLOB,
  sys_ai_bot_message_create_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_model (
  sys_ai_model_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_model_name            VARCHAR(255),
  sys_ai_model_code            VARCHAR(128),
  sys_ai_model_provider_id     BIGINT,
  sys_ai_model_capabilities    CLOB,
  sys_ai_model_description     VARCHAR(512),
  sys_ai_model_enabled         TINYINT DEFAULT 1,
  sys_ai_model_sort            INT DEFAULT 0,
  sys_ai_model_create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  sys_ai_model_update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  sys_ai_model_deleted         INT DEFAULT 0,
  sys_ai_model_required_pipelines CLOB,
  sys_ai_model_optional_pipelines CLOB,
  sys_ai_model_input_price     DECIMAL(18,8),
  sys_ai_model_output_price    DECIMAL(18,8),
  sys_ai_model_cache_price      DECIMAL(18,8),
  sys_ai_model_aliases          CLOB,
  sys_ai_model_last_latency     BIGINT,
  sys_ai_model_last_test_time  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_role (
  sys_ai_role_id            INT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_role_name          VARCHAR(255),
  sys_ai_role_code          VARCHAR(128),
  sys_ai_role_description   VARCHAR(512),
  sys_ai_role_system_prompt CLOB,
  sys_ai_role_model         VARCHAR(128),
  sys_ai_role_temperature   DOUBLE,
  sys_ai_role_sort          INT DEFAULT 0,
  sys_ai_role_status        INT DEFAULT 1,
  sys_ai_role_delete        INT DEFAULT 0,
  create_name               VARCHAR(255),
  create_time               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time               TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_role_group (
  sys_ai_role_group_id      INT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_role_group_name    VARCHAR(255),
  sys_ai_role_group_sort    INT DEFAULT 0,
  sys_ai_role_group_status  INT DEFAULT 1,
  sys_ai_role_group_delete  INT DEFAULT 0,
  create_name               VARCHAR(255),
  create_time               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time               TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_provider (
  sys_ai_provider_id        VARCHAR(64) PRIMARY KEY,
  sys_ai_provider_name      VARCHAR(255),
  sys_ai_provider_type      VARCHAR(64),
  sys_ai_provider_base_url  VARCHAR(512),
  sys_ai_provider_api_key   VARCHAR(1024),
  sys_ai_provider_logo      VARCHAR(512),
  sys_ai_provider_models    CLOB,
  sys_ai_provider_enabled   TINYINT DEFAULT 1,
  sys_ai_provider_deleted   INT DEFAULT 0,
  sys_ai_provider_platform  VARCHAR(64),
  sys_ai_provider_protocol  VARCHAR(32),
  sys_ai_provider_sort_order INT DEFAULT 0,
  sys_ai_provider_attributes CLOB,
  sys_ai_provider_extra     CLOB,
  sys_ai_provider_proxy     VARCHAR(255),
  sys_ai_provider_create_time BIGINT,
  sys_ai_provider_update_time BIGINT
);

CREATE TABLE IF NOT EXISTS sys_ai_provider_model (
  sys_ai_provider_model_provider_id    VARCHAR(64)  NOT NULL,
  sys_ai_provider_model_model          VARCHAR(255) NOT NULL,
  sys_ai_provider_model_enabled        TINYINT      DEFAULT 1,
  sys_ai_provider_model_sort_order     INT          DEFAULT 0,
  sys_ai_provider_model_description    CLOB,
  sys_ai_provider_model_capabilities   CLOB,
  sys_ai_provider_model_metrics        CLOB,
  sys_ai_provider_model_create_time    BIGINT,
  sys_ai_provider_model_update_time    BIGINT,
  PRIMARY KEY (sys_ai_provider_model_provider_id, sys_ai_provider_model_model)
);

CREATE TABLE IF NOT EXISTS sys_ai_provider_connection (
  sys_ai_provider_connection_id         VARCHAR(64)  NOT NULL PRIMARY KEY,
  sys_ai_provider_connection_provider_id VARCHAR(64) NOT NULL,
  sys_ai_provider_connection_role       VARCHAR(64)  NOT NULL,
  sys_ai_provider_connection_label      VARCHAR(255),
  sys_ai_provider_connection_base_url   VARCHAR(512),
  sys_ai_provider_connection_auth_scheme VARCHAR(32),
  sys_ai_provider_connection_credentials CLOB,
  sys_ai_provider_connection_extra      CLOB,
  sys_ai_provider_connection_create_time BIGINT,
  sys_ai_provider_connection_update_time BIGINT
);

CREATE TABLE IF NOT EXISTS sys_ai_service_status (
  sys_ai_service_status_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_service_status_service_name VARCHAR(255),
  sys_ai_service_status_running TINYINT DEFAULT 0,
  sys_ai_service_status_pid     BIGINT,
  sys_ai_service_status_start_time TIMESTAMP,
  sys_ai_service_status_uptime_sec BIGINT,
  sys_ai_service_status_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_manufacturer (
  sys_ai_manufacturer_id            INT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_manufacturer_code          VARCHAR(128),
  sys_ai_manufacturer_name          VARCHAR(255),
  sys_ai_manufacturer_factory       VARCHAR(255),
  sys_ai_manufacturer_base_url      VARCHAR(512),
  sys_ai_manufacturer_app_key       VARCHAR(255),
  sys_ai_manufacturer_api_secret    VARCHAR(512),
  sys_ai_manufacturer_openai_compatible TINYINT DEFAULT 0,
  sys_ai_manufacturer_logo          VARCHAR(512),
  sys_ai_manufacturer_remark        VARCHAR(512),
  sys_ai_manufacturer_status        INT DEFAULT 1,
  sys_ai_manufacturer_sort          INT DEFAULT 0,
  create_time                       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time                       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_connector (
  sys_ai_connector_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_connector_key VARCHAR(128),
  sys_ai_connector_name VARCHAR(128),
  sys_ai_connector_description VARCHAR(512),
  sys_ai_connector_type VARCHAR(64),
  sys_ai_connector_category VARCHAR(64),
  sys_ai_connector_icon VARCHAR(256),
  sys_ai_connector_endpoint CLOB,
  sys_ai_connector_params CLOB,
  sys_ai_connector_builtin TINYINT DEFAULT 0,
  sys_ai_connector_enabled TINYINT DEFAULT 1,
  sys_ai_connector_last_test_status VARCHAR(32),
  sys_ai_connector_last_tested_at TIMESTAMP,
  sys_ai_connector_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  sys_ai_connector_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_requirement (
  sys_ai_requirement_id VARCHAR(36) NOT NULL,
  sys_ai_requirement_display_no INT,
  sys_ai_requirement_title VARCHAR(512),
  sys_ai_requirement_content CLOB,
  sys_ai_requirement_tag VARCHAR(128),
  sys_ai_requirement_order_key VARCHAR(64),
  sys_ai_requirement_status VARCHAR(32) DEFAULT 'pending',
  sys_ai_requirement_completion_note VARCHAR(1024),
  sys_ai_requirement_owner_conversation_id VARCHAR(64),
  sys_ai_requirement_owner_terminal_id VARCHAR(64),
  sys_ai_requirement_origin VARCHAR(32),
  sys_ai_requirement_started_at BIGINT,
  sys_ai_requirement_completed_at BIGINT,
  sys_ai_requirement_attempt_count INT DEFAULT 0,
  sys_ai_requirement_extra CLOB,
  create_name VARCHAR(64),
  create_by INT,
  create_time BIGINT,
  update_name VARCHAR(64),
  update_by INT,
  update_time BIGINT,
  PRIMARY KEY (sys_ai_requirement_id)
);

CREATE TABLE IF NOT EXISTS sys_ai_requirement_check (
  sys_ai_requirement_check_id VARCHAR(36) NOT NULL,
  sys_ai_requirement_check_requirement_id VARCHAR(36),
  sys_ai_requirement_check_status VARCHAR(32) DEFAULT 'pending',
  create_time BIGINT,
  update_time BIGINT,
  PRIMARY KEY (sys_ai_requirement_check_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_global_setting_field ON sys_ai_global_setting(sys_ai_global_setting_group, sys_ai_global_setting_field);
CREATE INDEX IF NOT EXISTS idx_ai_agent_parent ON sys_ai_agent(sys_ai_agent_parent_id);
CREATE INDEX IF NOT EXISTS idx_ai_provider_conn_pid ON sys_ai_provider_connection(sys_ai_provider_connection_provider_id);

-- ===== sys_ai_conversation (对话表，SysAiConversationStore 依赖) =====
CREATE TABLE IF NOT EXISTS sys_ai_conversation (
  sys_ai_conversation_id VARCHAR(64) NOT NULL,
  sys_ai_conversation_bot_id VARCHAR(128),
  sys_ai_conversation_title VARCHAR(255),
  sys_ai_conversation_content CLOB,
  sys_ai_conversation_status VARCHAR(32) DEFAULT 'active',
  sys_ai_conversation_owner_id VARCHAR(64),
  sys_ai_conversation_owner_name VARCHAR(128),
  sys_ai_conversation_create_time BIGINT,
  sys_ai_conversation_update_time BIGINT,
  create_name VARCHAR(64),
  create_by INT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_name VARCHAR(64),
  update_by INT,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (sys_ai_conversation_id)
);

-- ===== sys_ai_conversation_message (对话消息表) =====
CREATE TABLE IF NOT EXISTS sys_ai_conversation_message (
  sys_ai_conversation_message_id VARCHAR(64) NOT NULL,
  sys_ai_conversation_message_conversation_id VARCHAR(64),
  sys_ai_conversation_message_role VARCHAR(32),
  sys_ai_conversation_message_content CLOB,
  sys_ai_conversation_message_create_time BIGINT,
  PRIMARY KEY (sys_ai_conversation_message_id)
);