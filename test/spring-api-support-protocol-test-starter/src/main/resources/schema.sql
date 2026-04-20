CREATE TABLE IF NOT EXISTS proxy_server (
  proxy_server_id INT AUTO_INCREMENT PRIMARY KEY,
  proxy_server_name VARCHAR(255),
  proxy_server_host VARCHAR(255),
  proxy_server_port INT,
  proxy_server_type VARCHAR(100),
  proxy_server_context_path VARCHAR(255),
  proxy_server_status VARCHAR(32),
  proxy_server_description VARCHAR(500),
  proxy_server_config CLOB,
  proxy_server_auto_start BOOLEAN,
  proxy_server_max_connections INT,
  proxy_server_timeout INT,
  create_name VARCHAR(255),
  create_by INT,
  create_time TIMESTAMP,
  update_name VARCHAR(255),
  update_by INT,
  update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS proxy_server_setting (
  proxy_server_setting_id INT AUTO_INCREMENT PRIMARY KEY,
  proxy_server_setting_name VARCHAR(255),
  proxy_server_setting_type VARCHAR(100),
  proxy_server_setting_server_id INT,
  proxy_server_setting_order INT,
  proxy_server_setting_description VARCHAR(500),
  proxy_server_setting_enabled BOOLEAN,
  proxy_server_setting_class_name VARCHAR(500),
  proxy_server_setting_version VARCHAR(50),
  proxy_server_setting_config CLOB,
  proxy_server_setting_filter_id VARCHAR(255),
  proxy_server_setting_https_enabled BOOLEAN,
  proxy_server_setting_https_cert_type VARCHAR(20),
  proxy_server_setting_https_pem_cert BLOB,
  proxy_server_setting_https_pem_key BLOB,
  proxy_server_setting_https_pem_key_password VARCHAR(255),
  proxy_server_setting_https_keystore BLOB,
  proxy_server_setting_https_keystore_password VARCHAR(255),
  create_name VARCHAR(255),
  create_by INT,
  create_time TIMESTAMP,
  update_name VARCHAR(255),
  update_by INT,
  update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS proxy_server_setting_item (
  proxy_server_setting_item_id INT AUTO_INCREMENT PRIMARY KEY,
  proxy_server_setting_item_setting_id INT,
  proxy_server_setting_item_name VARCHAR(255),
  proxy_server_setting_item_value CLOB,
  proxy_server_setting_item_description VARCHAR(500),
  proxy_server_setting_item_type VARCHAR(50),
  proxy_server_setting_item_required BOOLEAN,
  proxy_server_setting_item_default_value CLOB,
  proxy_server_setting_item_validation_rule VARCHAR(500),
  proxy_server_setting_item_order INT,
  create_name VARCHAR(255),
  create_by INT,
  create_time TIMESTAMP,
  update_name VARCHAR(255),
  update_by INT,
  update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS proxy_server_setting_file_storage (
  proxy_server_setting_file_storage_id INT AUTO_INCREMENT PRIMARY KEY,
  file_storage_server_id INT,
  file_storage_type VARCHAR(50),
  file_storage_base_path VARCHAR(500),
  file_storage_endpoint VARCHAR(255),
  file_storage_bucket VARCHAR(128),
  file_storage_access_key VARCHAR(200),
  file_storage_secret_key VARCHAR(200),
  file_storage_region VARCHAR(100),
  file_storage_enabled BOOLEAN,
  file_storage_connection_timeout BIGINT,
  create_name VARCHAR(255),
  create_by INT,
  create_time TIMESTAMP,
  update_name VARCHAR(255),
  update_by INT,
  update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS proxy_server_setting_service_discovery (
  proxy_server_setting_service_discovery_id INT AUTO_INCREMENT PRIMARY KEY,
  service_discovery_server_id INT,
  service_discovery_mode VARCHAR(50),
  service_discovery_balance VARCHAR(50),
  service_discovery_bean_name VARCHAR(200),
  service_discovery_enabled BOOLEAN,
  create_name VARCHAR(255),
  create_by INT,
  create_time TIMESTAMP,
  update_name VARCHAR(255),
  update_by INT,
  update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS proxy_server_setting_service_discovery_mapping (
  proxy_server_setting_service_discovery_mapping_id INT AUTO_INCREMENT PRIMARY KEY,
  service_discovery_server_id INT,
  service_discovery_name VARCHAR(100),
  service_discovery_address VARCHAR(200),
  service_discovery_weight INT,
  service_discovery_enabled BOOLEAN,
  create_name VARCHAR(255),
  create_by INT,
  create_time TIMESTAMP,
  update_name VARCHAR(255),
  update_by INT,
  update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS proxy_server_setting_ip_rate_limit (
  proxy_server_setting_ip_rate_limit_id INT AUTO_INCREMENT PRIMARY KEY,
  ip_rate_limit_server_id INT,
  ip_rate_limit_ip VARCHAR(100),
  ip_rate_limit_qps INT,
  ip_rate_limit_type VARCHAR(32),
  ip_rate_limit_enabled BOOLEAN,
  ip_rate_limit_setting_id INT,
  create_name VARCHAR(255),
  create_by INT,
  create_time TIMESTAMP,
  update_name VARCHAR(255),
  update_by INT,
  update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS proxy_server_setting_address_rate_limit (
  proxy_server_setting_address_rate_limit_id INT AUTO_INCREMENT PRIMARY KEY,
  address_rate_limit_server_id INT,
  address_rate_limit_address VARCHAR(200),
  address_rate_limit_qps INT,
  address_rate_limit_type VARCHAR(32),
  address_rate_limit_enabled BOOLEAN,
  address_rate_limit_setting_id INT,
  create_name VARCHAR(255),
  create_by INT,
  create_time TIMESTAMP,
  update_name VARCHAR(255),
  update_by INT,
  update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS proxy_server_setting_preview_extension (
  proxy_server_setting_preview_extension_id INT AUTO_INCREMENT PRIMARY KEY,
  preview_extension_server_id INT,
  preview_extension_disabled VARCHAR(2000),
  preview_extension_allowed VARCHAR(2000),
  preview_extension_whitelist_mode BOOLEAN,
  create_name VARCHAR(255),
  create_by INT,
  create_time TIMESTAMP,
  update_name VARCHAR(255),
  update_by INT,
  update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS proxy_server_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  server_id INT,
  filter_type VARCHAR(100),
  process_status VARCHAR(100),
  client_ip VARCHAR(64),
  client_geo VARCHAR(255),
  access_time TIMESTAMP,
  duration_ms BIGINT,
  store_time TIMESTAMP,
  create_name VARCHAR(255),
  create_by INT,
  create_time TIMESTAMP,
  update_name VARCHAR(255),
  update_by INT,
  update_time TIMESTAMP
);

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
