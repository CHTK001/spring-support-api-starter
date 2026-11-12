-- =====================================================================
-- V1.2__init_all_api_common_tenant.sql - spring-api-support-common-tenant-starter 全量 DDL
-- 合并自：V1.0__init_all_api_common_tenant.sql（全量建表）
--         V1.1__init_all_api_common_tenant.sql（6 个数据库连接字段，幂等补列）
--         V1.2__init_all_api_common_tenant.sql（sys_tenant_datasource_id，幂等补列）
-- 通道: INIT 同步建表；老库缺列/缺表由 information_schema + PREPARE 段幂等补齐。
-- 种子数据见 V1.0__initdata_all.sql（虚拟线程异步，不阻塞启动）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 来源: V1.0__init_tenant_complete.sql
-- ---------------------------------------------------------------------
-- =============================================
-- 租户管理表结构（与当前实体对齐，覆盖旧 V1.0 结构）
--
-- 表清单:
--   sys_tenant              租户信息表
--   sys_service             服务产品表
--   sys_service_module      服务模块表（模块关联菜单 sys_menu_id）
--   sys_service_module_item 服务模块菜单关联表（sys_menu_id 供租户菜单推送）
--   sys_tenant_service      租户-服务订阅表
--
-- @author CH
-- @version 2.0.0
-- @since 2026-08-07
-- =============================================


CREATE TABLE IF NOT EXISTS `sys_tenant` (
    `sys_tenant_id` varchar(64) NOT NULL COMMENT '租户ID - 雪花ID即租户唯一编码',
    `sys_tenant_name` varchar(255) NULL DEFAULT NULL COMMENT '租户名称',
    `sys_tenant_username` varchar(255) NULL DEFAULT NULL COMMENT '租户登录账号',
    `sys_tenant_password` varchar(255) NULL DEFAULT NULL COMMENT '租户密码',
    `sys_tenant_sign` varchar(255) NULL DEFAULT NULL COMMENT '签名',
    `sys_tenant_corporation` varchar(255) NULL DEFAULT NULL COMMENT '公司名称',
    `sys_tenant_delete` int NULL DEFAULT 0 COMMENT '删除标记 - 0:未删除, 1:已删除',
    `sys_tenant_status` int NULL DEFAULT 0 COMMENT '状态 - 0:正常, 1:禁用',
    `sys_tenant_contact` varchar(255) NULL DEFAULT NULL COMMENT '联系人',
    `sys_tenant_phone` varchar(50) NULL DEFAULT NULL COMMENT '联系电话',
    `sys_tenant_address` varchar(500) NULL DEFAULT NULL COMMENT '联系地址',
    `sys_tenant_email` varchar(255) NULL DEFAULT NULL COMMENT '联系邮箱',
    `sys_tenant_code` varchar(100) NULL DEFAULT NULL COMMENT '租户编码(兼容)',
    `sys_tenant_gid` varchar(64) NULL DEFAULT NULL COMMENT '全局ID',
    `sys_tenant_home_url` varchar(500) NULL DEFAULT NULL COMMENT '租户首页地址',
    `sys_tenant_remark` varchar(1000) NULL DEFAULT NULL COMMENT '备注',
    `sys_tenant_host` varchar(255) NULL DEFAULT NULL COMMENT '主库地址（租户专属数据库实例地址）',
    `sys_tenant_port` int NULL DEFAULT NULL COMMENT '主库端口（租户专属数据库实例端口）',
    `sys_tenant_username_sql` varchar(255) NULL DEFAULT NULL COMMENT '主库账号（租户专属数据库登录账号）',
    `sys_tenant_password_sql` varchar(255) NULL DEFAULT NULL COMMENT '主库口令（租户专属数据库登录口令，密文落库）',
    `sys_tenant_dbname` varchar(255) NULL DEFAULT NULL COMMENT '数据库名（租户专属数据库名）',
    `sys_tenant_ip` varchar(255) NULL DEFAULT NULL COMMENT '主库IP（租户专属数据库实例 IP）',
    `create_name` varchar(255) NULL DEFAULT NULL,
    `create_by` int NULL DEFAULT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    `update_name` varchar(255) NULL DEFAULT NULL,
    `update_by` int NULL DEFAULT NULL,
    `update_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`sys_tenant_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ;


CREATE TABLE IF NOT EXISTS `sys_service` (
    `sys_service_id` int NOT NULL AUTO_INCREMENT,
    `sys_service_name` varchar(255) NULL DEFAULT NULL COMMENT '服务名称',
    `sys_service_code` varchar(100) NULL DEFAULT NULL COMMENT '服务编码',
    `sys_service_desc` varchar(500) NULL DEFAULT NULL COMMENT '服务描述',
    `sys_service_sort` int NULL DEFAULT 0 COMMENT '排序',
    `sys_service_delete` int NULL DEFAULT 0 COMMENT '删除标记 - 0:未删除, 1:已删除',
    `sys_service_status` int NULL DEFAULT 0 COMMENT '状态 - 0:正常, 1:禁用',
    `create_name` varchar(255) NULL DEFAULT NULL,
    `create_by` int NULL DEFAULT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    `update_name` varchar(255) NULL DEFAULT NULL,
    `update_by` int NULL DEFAULT NULL,
    `update_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`sys_service_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ;


CREATE TABLE IF NOT EXISTS `sys_service_module` (
    `sys_service_module_id` int NOT NULL AUTO_INCREMENT,
    `sys_service_module_name` varchar(255) NULL DEFAULT NULL COMMENT '模块名称',
    `sys_service_module_code` varchar(100) NULL DEFAULT NULL COMMENT '模块编码',
    `sys_service_module_desc` varchar(500) NULL DEFAULT NULL COMMENT '模块描述',
    `sys_service_module_sort` int NULL DEFAULT 0 COMMENT '排序',
    `sys_service_module_delete` int NULL DEFAULT 0 COMMENT '删除标记 - 0:未删除, 1:已删除',
    `sys_service_module_status` int NULL DEFAULT 0 COMMENT '状态 - 0:正常, 1:禁用',
    `sys_service_module_type` varchar(50) NULL DEFAULT 'SERVICE' COMMENT '类型 - API/SERVICE/FUNCTION',
    `sys_menu_id` int NULL DEFAULT NULL COMMENT '关联菜单ID - 功能模块(FUNCTION)映射到平台菜单',
    `create_name` varchar(255) NULL DEFAULT NULL,
    `create_by` int NULL DEFAULT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    `update_name` varchar(255) NULL DEFAULT NULL,
    `update_by` int NULL DEFAULT NULL,
    `update_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`sys_service_module_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ;


CREATE TABLE IF NOT EXISTS `sys_service_module_item` (
    `sys_service_module_item_id` int NOT NULL AUTO_INCREMENT,
    `sys_service_id` int NOT NULL COMMENT '服务ID',
    `sys_service_module_id` int NOT NULL COMMENT '模块ID',
    `sys_menu_id` int NULL DEFAULT NULL COMMENT '菜单ID - 关联sys_menu表，租户菜单推送依据',
    `create_name` varchar(255) NULL DEFAULT NULL,
    `create_by` int NULL DEFAULT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    `update_name` varchar(255) NULL DEFAULT NULL,
    `update_by` int NULL DEFAULT NULL,
    `update_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`sys_service_module_item_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ;


CREATE TABLE IF NOT EXISTS `sys_tenant_service` (
    `sys_tenant_service_id` int NOT NULL AUTO_INCREMENT,
    `sys_tenant_id` varchar(64) NOT NULL COMMENT '租户ID',
    `sys_service_id` int NOT NULL COMMENT '服务ID',
    `sys_tenant_service_valid_time` date NULL DEFAULT NULL COMMENT '服务到期时间 - NULL表示永久有效',
    `create_name` varchar(255) NULL DEFAULT NULL,
    `create_by` int NULL DEFAULT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    `update_name` varchar(255) NULL DEFAULT NULL,
    `update_by` int NULL DEFAULT NULL,
    `update_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`sys_tenant_service_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ;

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
