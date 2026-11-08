-- =====================================================================
-- V1.2__init_all_api_common_tenant.sql - 租户库改为引用数据源实例
-- 通道: 幂等添加 sys_tenant_datasource_id 到 sys_tenant 表
-- 混合: MySQL 不支持 ALTER TABLE ADD COLUMN IF NOT EXISTS，改用
--       information_schema 行计数 + PREPARE/EXECUTE 段完成动态执行添加
--
-- 取值语义:
--   NULL / 0  用本租户自填的 sys_tenant_host/port/username_sql/password_sql（存量行为）
--   -1        用平台自身实例（datasource-routing-admin-url），只需填库名
--   > 0       用数据源中心台账 ms_datasource 里对应 id 的实例，只需填库名
-- 库名始终取 sys_tenant_dbname：一个实例承载多个租户，台账里的库名只是模板。
--
-- @author CH
-- @version 4.0.0
-- @since 2026-11-08
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. sys_tenant_datasource_id 引用的数据源实例（-1=平台自身实例）
-- ---------------------------------------------------------------------
SET @col_count = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_tenant' AND COLUMN_NAME = 'sys_tenant_datasource_id');
SET @add_datasource_id = CASE WHEN @col_count = 0 THEN 'ALTER TABLE `sys_tenant` ADD COLUMN `sys_tenant_datasource_id` BIGINT DEFAULT NULL COMMENT ''引用的数据源实例 id，-1=平台自身实例，NULL=用自填字段''' ELSE 'SELECT 1' END;
PREPARE stmt_datasource_id FROM @add_datasource_id;
EXECUTE stmt_datasource_id;

-- ---------------------------------------------------------------------
-- 清理资源
-- ---------------------------------------------------------------------
DEALLOCATE PREPARE stmt_datasource_id;
