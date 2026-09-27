-- =====================================================================
-- V1.0__init_all_api_monitor.sql - spring-api-support-monitor-starter 全量 DDL（合并自 2 个文件）
-- 通道: INIT 同步建表；老库缺列/缺表由 information_schema + PREPARE 段幂等补齐。
-- 种子数据见 V1.0__initdata_all.sql（虚拟线程异步，不阻塞启动）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 来源: V0.0.0__h2_compat.sql
-- ---------------------------------------------------------------------
-- ---------------------------------------------------------------------
-- 来源: V1.0__init_monitor_complete.sql
-- ---------------------------------------------------------------------
-- 监控中心完整表结构
-- 包含 ACME 证书管理、服务器/组件布局管理、Docker 软件库与软件仓库

SET NAMES utf8;

SET FOREIGN_KEY_CHECKS = 0;

-- ACME 账户表

CREATE TABLE IF NOT EXISTS `monitor_acme_account` (
  `acme_account_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `acme_account_email` varchar(255) NULL DEFAULT NULL COMMENT '账号邮箱',
  `acme_account_server` varchar(255) NULL DEFAULT NULL COMMENT 'ACME 服务端点',
  `acme_account_provider_type` varchar(32) NOT NULL DEFAULT 'acme' COMMENT '证书服务商类型(acme=ACME协议/acme4j、cas=阿里云CAS)（并入自 V1.2__add_acme_account_provider_type.sql）',
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

CREATE TABLE IF NOT EXISTS `monitor_acme_certificate` (
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
  KEY `monitor_acme_certificate_idx_account_id` (`acme_account_id`),
  KEY `monitor_acme_certificate_idx_domain` (`acme_cert_primary_domain`),
  KEY `monitor_acme_certificate_idx_status` (`acme_cert_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACME 证书表';

-- ACME 订单表

CREATE TABLE IF NOT EXISTS `monitor_acme_order` (
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
  KEY `monitor_acme_order_idx_account_id` (`acme_account_id`),
  KEY `monitor_acme_order_idx_cert_id` (`acme_cert_id`),
  KEY `monitor_acme_order_idx_status` (`acme_order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACME 订单表';

-- 服务器布局表

CREATE TABLE IF NOT EXISTS `monitor_sys_gen_server_layout` (
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
  KEY `monitor_sys_gen_server_layout_idx_server_id` (`monitor_sys_gen_server_id`),
  KEY `monitor_sys_gen_server_layout_idx_type` (`monitor_sys_gen_server_layout_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器布局表';

-- 服务器组件布局表

CREATE TABLE IF NOT EXISTS `monitor_sys_gen_server_component_layout` (
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
  KEY `monitor_sys_gen_server_component_layout_idx_server_id` (`monitor_sys_gen_server_id`),
  KEY `monitor_sys_gen_server_component_layout_idx_component_id` (`monitor_sys_gen_server_component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器组件布局表';

-- 被监控服务器主机表

CREATE TABLE IF NOT EXISTS `monitor_server` (
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
  KEY `monitor_server_idx_name` (`server_name`),
  KEY `monitor_server_idx_ip` (`server_ip`),
  KEY `monitor_server_idx_group` (`server_group`),
  KEY `monitor_server_idx_status` (`server_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='被监控服务器主机表';

-- ============================================================================
-- monitor_acme_account.provider_type（并入自 V1.2__add_acme_account_provider_type.sql）
-- 该列已内联进上方建表本体，不再保留老库补列 ALTER：MySQL 8 不支持
-- ADD COLUMN IF NOT EXISTS（仅 MariaDB 支持），无条件 ALTER 重复执行会报 1060。
-- ============================================================================
-- 操作日志表（并入自 V1.3__add_operation_log.sql）
CREATE TABLE IF NOT EXISTS ms_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    operator VARCHAR(64) NOT NULL COMMENT '操作用户',
    operation VARCHAR(32) NOT NULL COMMENT '操作类型',
    target_module VARCHAR(32) NOT NULL COMMENT '目标模块',
    target_id VARCHAR(64) COMMENT '目标对象ID',
    description TEXT COMMENT '操作描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_operator (operator),
    INDEX idx_target (target_module, target_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =====================================================================
-- 监控模块核心业务表（并入自 V1.1__init_ms_monitor.sql）
-- 资产、采集任务、解析规则、网关、全局配置；对应 pages/monitor 前端模块
-- =====================================================================

-- 监控资产表
-- 采集任务表
-- 指标解析规则表
-- 监控网关表
-- 全局配置表
SET FOREIGN_KEY_CHECKS = 1;

