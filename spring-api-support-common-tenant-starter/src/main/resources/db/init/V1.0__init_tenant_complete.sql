/*
 * 租户管理模块 - 数据库初始化脚本
 * 
 * 包含表:
 *   - sys_tenant: 租户信息表
 *   - sys_service: 服务表
 *   - sys_service_module: 服务模块表
 *   - sys_service_module_item: 服务模块功能项表
 *   - sys_tenant_service: 租户服务关联表
 *
 * 用于多租户SaaS架构，管理租户及其订阅的服务模块
 *
 * @author System
 * @since 2025-01-01
 * @version 1.0
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 租户信息表 (sys_tenant)
-- 用途: 存储租户基本信息，支持多租户隔离
-- =====================================================
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant` (
    `sys_tenant_id` int NOT NULL AUTO_INCREMENT COMMENT '租户ID - 主键自增',
    `sys_tenant_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租户名称 - 企业/组织名称',
    `sys_tenant_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租户编码 - 唯一标识码，用于API调用和数据隔离',
    `sys_tenant_logo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '租户Logo - Logo图片URL地址',
    `sys_tenant_description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '租户描述 - 企业/组织简介',
    `sys_tenant_contact_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人姓名 - 主要联系人',
    `sys_tenant_contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话 - 联系人手机号码',
    `sys_tenant_contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系邮箱 - 联系人电子邮件',
    `sys_tenant_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系地址 - 企业办公地址',
    `sys_tenant_domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义域名 - 租户专属访问域名',
    `sys_tenant_expire_time` datetime NULL DEFAULT NULL COMMENT '服务到期时间 - NULL表示永久有效',
    `sys_tenant_max_users` int NULL DEFAULT 100 COMMENT '最大用户数 - 该租户允许的最大用户数量限制',
    `sys_tenant_current_users` int NULL DEFAULT 0 COMMENT '当前用户数 - 该租户当前已注册的用户数量',
    `sys_tenant_storage_quota` bigint NULL DEFAULT 10737418240 COMMENT '存储配额 - 允许使用的存储空间(字节)，默认10GB',
    `sys_tenant_storage_used` bigint NULL DEFAULT 0 COMMENT '已用存储 - 当前已使用的存储空间(字节)',
    `sys_tenant_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'STANDARD' COMMENT '租户类型 - TRIAL:试用, STANDARD:标准, PROFESSIONAL:专业, ENTERPRISE:企业',
    `sys_tenant_status` int NULL DEFAULT 1 COMMENT '租户状态 - 0:禁用, 1:正常, 2:试用中, 3:已过期, 4:欠费冻结',
    `sys_tenant_data_isolation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'SCHEMA' COMMENT '数据隔离模式 - SHARED:共享表, SCHEMA:独立Schema, DATABASE:独立数据库',
    `sys_tenant_theme` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主题配置 - 租户自定义主题JSON配置',
    `sys_tenant_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '扩展配置 - 租户其他配置信息(JSON格式)',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_tenant_id`) USING BTREE,
    UNIQUE INDEX `uk_tenant_code` (`sys_tenant_code`) USING BTREE COMMENT '租户编码唯一索引',
    UNIQUE INDEX `uk_tenant_domain` (`sys_tenant_domain`) USING BTREE COMMENT '租户域名唯一索引',
    INDEX `idx_tenant_status` (`sys_tenant_status`) USING BTREE COMMENT '状态索引',
    INDEX `idx_expire_time` (`sys_tenant_expire_time`) USING BTREE COMMENT '过期时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '租户信息表 - 存储多租户SaaS系统的租户基本信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 2. 服务表 (sys_service)
-- 用途: 定义系统提供的服务产品
-- =====================================================
DROP TABLE IF EXISTS `sys_service`;
CREATE TABLE `sys_service` (
    `sys_service_id` int NOT NULL AUTO_INCREMENT COMMENT '服务ID - 主键自增',
    `sys_service_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务名称 - 服务的显示名称',
    `sys_service_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务编码 - 服务唯一标识码',
    `sys_service_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务图标 - 图标URL或图标类名',
    `sys_service_description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务描述 - 服务功能的详细说明',
    `sys_service_price_monthly` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '月度价格 - 按月订阅的价格(元)',
    `sys_service_price_yearly` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '年度价格 - 按年订阅的价格(元)',
    `sys_service_trial_days` int NULL DEFAULT 0 COMMENT '试用天数 - 免费试用期天数，0表示不提供试用',
    `sys_service_sort` int NULL DEFAULT 0 COMMENT '排序号 - 服务在列表中的显示顺序',
    `sys_service_status` int NULL DEFAULT 1 COMMENT '服务状态 - 0:下架, 1:上架, 2:即将上线',
    `sys_service_features` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '功能特性 - 服务包含的功能列表(JSON数组)',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_service_id`) USING BTREE,
    UNIQUE INDEX `uk_service_code` (`sys_service_code`) USING BTREE COMMENT '服务编码唯一索引',
    INDEX `idx_service_status` (`sys_service_status`) USING BTREE COMMENT '状态索引',
    INDEX `idx_service_sort` (`sys_service_sort`) USING BTREE COMMENT '排序索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '服务表 - 定义系统提供的可订阅服务产品' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 3. 服务模块表 (sys_service_module)
-- 用途: 定义服务下的功能模块
-- =====================================================
DROP TABLE IF EXISTS `sys_service_module`;
CREATE TABLE `sys_service_module` (
    `sys_service_module_id` int NOT NULL AUTO_INCREMENT COMMENT '模块ID - 主键自增',
    `sys_service_id` int NOT NULL COMMENT '所属服务ID - 关联sys_service表',
    `sys_module_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块名称 - 模块的显示名称',
    `sys_module_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块编码 - 模块唯一标识码',
    `sys_module_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块图标 - 图标URL或图标类名',
    `sys_module_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块描述 - 模块功能说明',
    `sys_module_parent_id` int NULL DEFAULT 0 COMMENT '父模块ID - 支持模块层级结构，0表示顶级模块',
    `sys_module_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块路径 - 前端路由路径',
    `sys_module_sort` int NULL DEFAULT 0 COMMENT '排序号 - 模块在列表中的显示顺序',
    `sys_module_status` int NULL DEFAULT 1 COMMENT '模块状态 - 0:禁用, 1:启用',
    `sys_module_is_addon` int NULL DEFAULT 0 COMMENT '是否附加模块 - 0:基础模块(服务包含), 1:附加模块(需额外购买)',
    `sys_module_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '模块价格 - 附加模块的价格(元/月)',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_service_module_id`) USING BTREE,
    UNIQUE INDEX `uk_module_code` (`sys_module_code`) USING BTREE COMMENT '模块编码唯一索引',
    INDEX `idx_service_id` (`sys_service_id`) USING BTREE COMMENT '服务ID索引',
    INDEX `idx_parent_id` (`sys_module_parent_id`) USING BTREE COMMENT '父模块索引',
    INDEX `idx_module_sort` (`sys_module_sort`) USING BTREE COMMENT '排序索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '服务模块表 - 定义服务下的功能模块' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 4. 服务模块功能项表 (sys_service_module_item)
-- 用途: 定义模块下的具体功能项/权限点
-- =====================================================
DROP TABLE IF EXISTS `sys_service_module_item`;
CREATE TABLE `sys_service_module_item` (
    `sys_module_item_id` int NOT NULL AUTO_INCREMENT COMMENT '功能项ID - 主键自增',
    `sys_service_module_id` int NOT NULL COMMENT '所属模块ID - 关联sys_service_module表',
    `sys_item_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '功能项名称 - 功能的显示名称',
    `sys_item_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '功能项编码 - 功能唯一标识码，用于权限控制',
    `sys_item_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'FUNCTION' COMMENT '功能类型 - MENU:菜单, BUTTON:按钮, API:接口, FUNCTION:功能点',
    `sys_item_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '功能描述 - 功能说明',
    `sys_item_api_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'API路径 - 关联的后端API接口路径',
    `sys_item_sort` int NULL DEFAULT 0 COMMENT '排序号 - 功能项在列表中的显示顺序',
    `sys_item_status` int NULL DEFAULT 1 COMMENT '功能状态 - 0:禁用, 1:启用',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_module_item_id`) USING BTREE,
    UNIQUE INDEX `uk_item_code` (`sys_item_code`) USING BTREE COMMENT '功能项编码唯一索引',
    INDEX `idx_module_id` (`sys_service_module_id`) USING BTREE COMMENT '模块ID索引',
    INDEX `idx_item_type` (`sys_item_type`) USING BTREE COMMENT '功能类型索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '服务模块功能项表 - 定义模块下的具体功能项和权限点' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 5. 租户服务关联表 (sys_tenant_service)
-- 用途: 记录租户订阅的服务及其状态
-- =====================================================
DROP TABLE IF EXISTS `sys_tenant_service`;
CREATE TABLE `sys_tenant_service` (
    `sys_tenant_service_id` int NOT NULL AUTO_INCREMENT COMMENT '关联ID - 主键自增',
    `sys_tenant_id` int NOT NULL COMMENT '租户ID - 关联sys_tenant表',
    `sys_service_id` int NOT NULL COMMENT '服务ID - 关联sys_service表',
    `sys_subscription_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'MONTHLY' COMMENT '订阅类型 - TRIAL:试用, MONTHLY:月付, YEARLY:年付, PERMANENT:永久',
    `sys_subscription_start` datetime NOT NULL COMMENT '订阅开始时间 - 服务生效开始日期',
    `sys_subscription_end` datetime NULL DEFAULT NULL COMMENT '订阅结束时间 - 服务到期日期，NULL表示永久',
    `sys_subscription_status` int NULL DEFAULT 1 COMMENT '订阅状态 - 0:已取消, 1:生效中, 2:试用中, 3:已过期, 4:续费中',
    `sys_auto_renew` int NULL DEFAULT 0 COMMENT '自动续费 - 0:否, 1:是',
    `sys_modules_enabled` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '启用的模块 - 已启用的模块ID列表(JSON数组)',
    `sys_modules_addon` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '附加模块 - 额外购买的附加模块ID列表(JSON数组)',
    `sys_price_paid` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '实付金额 - 本次订阅实际支付的金额(元)',
    `sys_order_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单号 - 关联的支付订单号',
    `sys_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注 - 订阅相关备注信息',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_tenant_service_id`) USING BTREE,
    UNIQUE INDEX `uk_tenant_service` (`sys_tenant_id`, `sys_service_id`) USING BTREE COMMENT '租户服务唯一索引 - 确保每个租户只有一条服务订阅记录',
    INDEX `idx_tenant_id` (`sys_tenant_id`) USING BTREE COMMENT '租户ID索引',
    INDEX `idx_service_id` (`sys_service_id`) USING BTREE COMMENT '服务ID索引',
    INDEX `idx_subscription_status` (`sys_subscription_status`) USING BTREE COMMENT '订阅状态索引',
    INDEX `idx_subscription_end` (`sys_subscription_end`) USING BTREE COMMENT '订阅结束时间索引 - 用于检查即将过期的订阅'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '租户服务关联表 - 记录租户订阅的服务及订阅状态' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 初始数据 - 默认租户(系统管理员)
-- =====================================================
INSERT INTO `sys_tenant` (`sys_tenant_id`, `sys_tenant_name`, `sys_tenant_code`, `sys_tenant_description`, `sys_tenant_type`, `sys_tenant_status`, `sys_tenant_max_users`, `create_name`, `create_by`, `create_time`, `update_time`)
VALUES (1, '系统管理平台', 'SYSTEM', '系统默认租户，用于平台管理', 'ENTERPRISE', 1, -1, '系统初始化', 0, NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;
