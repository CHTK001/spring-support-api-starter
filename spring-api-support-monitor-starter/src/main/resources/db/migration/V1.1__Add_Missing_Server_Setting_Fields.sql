-- 添加缺失的服务器配置字段
-- 作者: CH
-- 日期: 2024/12/27

-- 添加Docker监控启用字段
ALTER TABLE monitor_sys_gen_server_setting
    ADD COLUMN monitor_sys_gen_server_setting_docker_monitor_enabled INT DEFAULT 0 COMMENT '是否启用Docker监控 0:否 1:是';

-- 添加Docker API版本字段
ALTER TABLE monitor_sys_gen_server_setting
    ADD COLUMN monitor_sys_gen_server_setting_docker_api_version VARCHAR(20) DEFAULT '1.40' COMMENT 'Docker API版本';

-- 更新连接超时默认值为0
UPDATE monitor_sys_gen_server_setting
SET monitor_sys_gen_server_setting_connection_timeout = 0
WHERE monitor_sys_gen_server_setting_connection_timeout = 30;

-- 添加索引以提高查询性能
CREATE INDEX idx_server_setting_server_id ON monitor_sys_gen_server_setting (monitor_sys_gen_server_id);
CREATE INDEX idx_server_setting_monitor_enabled ON monitor_sys_gen_server_setting (monitor_sys_gen_server_setting_monitor_enabled);
CREATE INDEX idx_server_setting_alert_enabled ON monitor_sys_gen_server_setting (monitor_sys_gen_server_setting_alert_enabled);
