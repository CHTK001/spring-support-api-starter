-- 添加告警历史表的忽略原因字段
-- V1.3__Add_Alert_History_Ignore_Reason_Field.sql

-- 检查字段是否存在，如果不存在则添加
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'monitor_sys_gen_server_alert_history' 
     AND COLUMN_NAME = 'monitor_sys_gen_server_alert_history_ignore_reason') = 0,
    'ALTER TABLE monitor_sys_gen_server_alert_history ADD COLUMN monitor_sys_gen_server_alert_history_ignore_reason VARCHAR(500) COMMENT ''忽略原因''',
    'SELECT ''字段已存在'' AS message'
));

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
