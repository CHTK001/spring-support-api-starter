-- =====================================================
-- 增量脚本: 为用户登录日志表补齐响应消息字段
-- 兼容存量库，避免 MyBatis-Plus insert 时报 Unknown column
-- =====================================================

SET @sys_user_log_message_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user_log'
      AND COLUMN_NAME = 'sys_log_message'
);

SET @sys_user_log_message_sql := IF(
    @sys_user_log_message_exists = 0,
    'ALTER TABLE `sys_user_log` ADD COLUMN `sys_log_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT ''响应消息'' AFTER `sys_log_status`',
    'SELECT 1'
);

PREPARE stmt FROM @sys_user_log_message_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


