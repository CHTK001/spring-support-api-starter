-- =====================================================================
-- V1.0__init_all_api_common_tenant.sql - spring-api-support-common-tenant-starter 全量 DDL（合并自 1 个文件）
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

