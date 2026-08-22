-- 监控中心完整表结构
-- 包含 ACME 证书管理、服务器/组件布局管理、Docker 软件库与软件仓库

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ACME 账户表
DROP TABLE IF EXISTS `monitor_acme_account`;
CREATE TABLE `monitor_acme_account` (
  `acme_account_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `acme_account_email` varchar(255) NULL DEFAULT NULL COMMENT '账号邮箱',
  `acme_account_server` varchar(255) NULL DEFAULT NULL COMMENT 'ACME 服务端点',
  `acme_account_status` varchar(255) NULL DEFAULT NULL COMMENT '账户状态',
  `acme_account_private_key_pem` text NULL DEFAULT NULL COMMENT '账户私钥（PEM）',
  `acme_account_eab_kid` varchar(255) NULL DEFAULT NULL COMMENT 'EAB Key ID (ZeroSSL 等需要)',
  `acme_account_eab_hmac_key` varchar(512) NULL DEFAULT NULL COMMENT 'EAB HMAC Key (Base64 编码)',
  `acme_account_remark` varchar(255) NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`acme_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACME 账户表';

-- ACME 证书表
DROP TABLE IF EXISTS `monitor_acme_certificate`;
CREATE TABLE `monitor_acme_certificate` (
  `acme_cert_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `acme_account_id` int NULL DEFAULT NULL COMMENT '账户ID',
  `acme_cert_primary_domain` varchar(255) NULL DEFAULT NULL COMMENT '主域名',
  `acme_cert_san` varchar(255) NULL DEFAULT NULL COMMENT '备用域名(SAN)，逗号分隔',
  `acme_cert_challenge_type` varchar(255) NULL DEFAULT NULL COMMENT '挑战类型（HTTP-01/DNS-01）',
  `acme_cert_status` varchar(255) NULL DEFAULT NULL COMMENT '证书状态',
  `acme_cert_not_before` varchar(255) NULL DEFAULT NULL COMMENT '有效期起',
  `acme_cert_not_after` varchar(255) NULL DEFAULT NULL COMMENT '有效期止',
  `acme_cert_renew_at` varchar(255) NULL DEFAULT NULL COMMENT '计划续签日期',
  `acme_cert_chain_pem` text NULL COMMENT '证书PEM（含链）',
  `acme_cert_key_pem` text NULL COMMENT '私钥PEM',
  `acme_cert_last_error` varchar(255) NULL DEFAULT NULL COMMENT '最近错误',
  `acme_cert_deploy_mode` varchar(20) NULL DEFAULT 'manual' COMMENT '部署模式：local(本机自动)/manual(手动)',
  `acme_cert_webroot_path` varchar(255) NULL DEFAULT NULL COMMENT '本机模式webroot路径',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`acme_cert_id`),
  KEY `idx_account_id` (`acme_account_id`),
  KEY `idx_domain` (`acme_cert_primary_domain`),
  KEY `idx_status` (`acme_cert_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACME 证书表';

-- ACME 订单表
DROP TABLE IF EXISTS `monitor_acme_order`;
CREATE TABLE `monitor_acme_order` (
  `acme_order_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `acme_account_id` int NULL DEFAULT NULL COMMENT '账户ID',
  `acme_cert_id` int NULL DEFAULT NULL COMMENT '证书ID',
  `acme_order_url` varchar(255) NULL DEFAULT NULL COMMENT '订单URL',
  `acme_authz_urls` text NULL COMMENT '授权URL列表(JSON)',
  `acme_order_status` varchar(255) NULL DEFAULT NULL COMMENT '状态',
  `acme_certificate_url` varchar(255) NULL DEFAULT NULL COMMENT '最终证书URL',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`acme_order_id`),
  KEY `idx_account_id` (`acme_account_id`),
  KEY `idx_cert_id` (`acme_cert_id`),
  KEY `idx_status` (`acme_order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACME 订单表';

-- 服务器布局表
DROP TABLE IF EXISTS `monitor_sys_gen_server_layout`;
CREATE TABLE `monitor_sys_gen_server_layout` (
  `monitor_sys_gen_server_layout_id` int NOT NULL AUTO_INCREMENT COMMENT '布局ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_layout_name` varchar(255) NULL DEFAULT NULL COMMENT '布局名称',
  `monitor_sys_gen_server_layout_config` text NULL DEFAULT NULL COMMENT '布局配置 JSON',
  `monitor_sys_gen_server_layout_type` varchar(32) NULL DEFAULT NULL COMMENT '布局类型（default/generated/copied/custom）',
  `monitor_sys_gen_server_layout_default` int NULL DEFAULT 0 COMMENT '是否默认布局（0/1）',
  `monitor_sys_gen_server_layout_status` int NULL DEFAULT 1 COMMENT '布局状态（0禁用/1启用）',
  `monitor_sys_gen_server_layout_description` varchar(512) NULL DEFAULT NULL COMMENT '布局描述',
  `monitor_sys_gen_server_layout_version` int NULL DEFAULT 1 COMMENT '布局版本号',
  `monitor_sys_gen_server_layout_sort` int NULL DEFAULT 0 COMMENT '布局排序',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_layout_id`),
  KEY `idx_server_id` (`monitor_sys_gen_server_id`),
  KEY `idx_type` (`monitor_sys_gen_server_layout_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器布局表';

-- 服务器组件布局表
DROP TABLE IF EXISTS `monitor_sys_gen_server_component_layout`;
CREATE TABLE `monitor_sys_gen_server_component_layout` (
  `monitor_sys_gen_server_component_layout_id` int NOT NULL AUTO_INCREMENT COMMENT '组件布局ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_component_id` int NULL DEFAULT NULL COMMENT '关联组件ID',
  `monitor_sys_gen_server_component_layout_x` int NULL DEFAULT 0 COMMENT 'X 坐标',
  `monitor_sys_gen_server_component_layout_y` int NULL DEFAULT 0 COMMENT 'Y 坐标',
  `monitor_sys_gen_server_component_layout_w` int NULL DEFAULT 0 COMMENT '宽度',
  `monitor_sys_gen_server_component_layout_h` int NULL DEFAULT 0 COMMENT '高度',
  `monitor_sys_gen_server_component_layout_z_index` int NULL DEFAULT 0 COMMENT '层级',
  `monitor_sys_gen_server_component_layout_movable` int NULL DEFAULT 1 COMMENT '是否可移动（0/1）',
  `monitor_sys_gen_server_component_layout_resizable` int NULL DEFAULT 1 COMMENT '是否可调整大小（0/1）',
  `monitor_sys_gen_server_component_layout_status` int NULL DEFAULT 1 COMMENT '布局状态（0禁用/1启用）',
  `monitor_sys_gen_server_component_layout_sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `monitor_sys_gen_server_component_layout_chart_config` text NULL DEFAULT NULL COMMENT '组件图表配置 JSON',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_component_layout_id`),
  KEY `idx_server_id` (`monitor_sys_gen_server_id`),
  KEY `idx_component_id` (`monitor_sys_gen_server_component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器组件布局表';

-- 被监控服务器主机表
DROP TABLE IF EXISTS `monitor_server`;
CREATE TABLE `monitor_server` (
  `server_id` int NOT NULL AUTO_INCREMENT COMMENT '服务器ID',
  `server_name` varchar(255) NULL DEFAULT NULL COMMENT '服务器名称',
  `server_ip` varchar(64) NULL DEFAULT NULL COMMENT '服务器IP',
  `server_host` varchar(255) NULL DEFAULT NULL COMMENT '主机名',
  `server_port` int NULL DEFAULT NULL COMMENT '端口',
  `server_os` varchar(64) NULL DEFAULT NULL COMMENT '操作系统',
  `server_group` varchar(128) NULL DEFAULT NULL COMMENT '分组',
  `server_account` varchar(128) NULL DEFAULT NULL COMMENT '登录账号',
  `server_status` int NULL DEFAULT 1 COMMENT '状态（0禁用/1启用）',
  `server_remark` varchar(1024) NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`server_id`),
  KEY `idx_name` (`server_name`),
  KEY `idx_ip` (`server_ip`),
  KEY `idx_group` (`server_group`),
  KEY `idx_status` (`server_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='被监控服务器主机表';

SET FOREIGN_KEY_CHECKS = 1;
