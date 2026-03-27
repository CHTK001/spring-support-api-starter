SET @exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'monitor_sys_gen_server_setting'
      AND COLUMN_NAME = 'monitor_sys_gen_server_setting_proxy_instance_id'
);

SET @ddl := IF(
    @exists = 0,
    'ALTER TABLE monitor_sys_gen_server_setting ADD COLUMN monitor_sys_gen_server_setting_proxy_instance_id INT NULL DEFAULT NULL COMMENT ''代理实例ID'' AFTER monitor_sys_gen_server_setting_proxy_enabled',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
