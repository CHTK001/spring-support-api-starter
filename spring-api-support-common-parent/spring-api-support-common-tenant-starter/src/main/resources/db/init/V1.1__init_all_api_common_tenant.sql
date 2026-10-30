-- =====================================================================
-- V1.1__init_all_api_common_tenant.sql - 租户专属数据库连接信息列
-- 通道: 幂等添加 6 个数据库连接字段到 sys_tenant 表
-- 混合: MySQL 不支持 ALTER TABLE ADD COLUMN IF NOT EXISTS，改用
--       information_schema 行计数 + PREPARE/EXECUTE 段完成动态执行添加
-- 字段: sys_tenant_host / sys_tenant_port / sys_tenant_username_sql /
--       sys_tenant_password_sql / sys_tenant_dbname / sys_tenant_ip
--
-- @author CH
-- @version 4.0.0
-- @since 2026-10-30
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. sys_tenant_host 主库地址（租户专属数据库实例地址）
-- ---------------------------------------------------------------------
SET @col_count = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_tenant' AND COLUMN_NAME = 'sys_tenant_host');
SET @add_host = CASE WHEN @col_count = 0 THEN 'ALTER TABLE `sys_tenant` ADD COLUMN `sys_tenant_host` VARCHAR(255) DEFAULT NULL COMMENT ''主库地址（租户专属数据库实例地址）''' ELSE 'SELECT 1' END;
PREPARE stmt_host FROM @add_host;
EXECUTE stmt_host;

-- ---------------------------------------------------------------------
-- 2. sys_tenant_port 主库端口（租户专属数据库实例端口）
-- ---------------------------------------------------------------------
SET @col_count = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_tenant' AND COLUMN_NAME = 'sys_tenant_port');
SET @add_port = CASE WHEN @col_count = 0 THEN 'ALTER TABLE `sys_tenant` ADD COLUMN `sys_tenant_port` INT DEFAULT NULL COMMENT ''主库端口（租户专属数据库实例端口）''' ELSE 'SELECT 1' END;
PREPARE stmt_port FROM @add_port;
EXECUTE stmt_port;

-- ---------------------------------------------------------------------
-- 3. sys_tenant_username_sql 主库账号（租户专属数据库登录账号）
-- ---------------------------------------------------------------------
SET @col_count = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_tenant' AND COLUMN_NAME = 'sys_tenant_username_sql');
SET @add_username = CASE WHEN @col_count = 0 THEN 'ALTER TABLE `sys_tenant` ADD COLUMN `sys_tenant_username_sql` VARCHAR(255) DEFAULT NULL COMMENT ''主库账号（租户专属数据库登录账号）''' ELSE 'SELECT 1' END;
PREPARE stmt_username FROM @add_username;
EXECUTE stmt_username;

-- ---------------------------------------------------------------------
-- 4. sys_tenant_password_sql 主库口令（租户专属数据库登录口令，密文落库）
-- ---------------------------------------------------------------------
SET @col_count = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_tenant' AND COLUMN_NAME = 'sys_tenant_password_sql');
SET @add_password = CASE WHEN @col_count = 0 THEN 'ALTER TABLE `sys_tenant` ADD COLUMN `sys_tenant_password_sql` VARCHAR(255) DEFAULT NULL COMMENT ''主库口令（租户专属数据库登录口令，密文落库）''' ELSE 'SELECT 1' END;
PREPARE stmt_password FROM @add_password;
EXECUTE stmt_password;

-- ---------------------------------------------------------------------
-- 5. sys_tenant_dbname 数据库名（租户专属数据库名）
-- ---------------------------------------------------------------------
SET @col_count = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_tenant' AND COLUMN_NAME = 'sys_tenant_dbname');
SET @add_dbname = CASE WHEN @col_count = 0 THEN 'ALTER TABLE `sys_tenant` ADD COLUMN `sys_tenant_dbname` VARCHAR(255) DEFAULT NULL COMMENT ''数据库名（租户专属数据库名）''' ELSE 'SELECT 1' END;
PREPARE stmt_dbname FROM @add_dbname;
EXECUTE stmt_dbname;

-- ---------------------------------------------------------------------
-- 6. sys_tenant_ip 主库IP（租户专属数据库实例 IP）
-- ---------------------------------------------------------------------
SET @col_count = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_tenant' AND COLUMN_NAME = 'sys_tenant_ip');
SET @add_ip = CASE WHEN @col_count = 0 THEN 'ALTER TABLE `sys_tenant` ADD COLUMN `sys_tenant_ip` VARCHAR(255) DEFAULT NULL COMMENT ''主库IP（租户专属数据库实例 IP）''' ELSE 'SELECT 1' END;
PREPARE stmt_ip FROM @add_ip;
EXECUTE stmt_ip;

-- ---------------------------------------------------------------------
-- 清理资源
-- ---------------------------------------------------------------------
DEALLOCATE PREPARE stmt_host;
DEALLOCATE PREPARE stmt_port;
DEALLOCATE PREPARE stmt_username;
DEALLOCATE PREPARE stmt_password;
DEALLOCATE PREPARE stmt_dbname;
DEALLOCATE PREPARE stmt_ip;
