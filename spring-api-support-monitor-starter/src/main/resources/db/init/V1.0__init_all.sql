-- =====================================================================
-- V1.0__init_all.sql - spring-api-support-monitor-starter 全量 DDL（合并自 2 个文件）
-- 通道: INIT 同步建表；老库缺列/缺表由 information_schema + PREPARE 段幂等补齐。
-- 种子数据见 V1.0__initdata_all.sql（虚拟线程异步，不阻塞启动）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 来源: V0.0.0__h2_compat.sql
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_menu (sys_menu_id BIGINT AUTO_INCREMENT PRIMARY KEY, sys_menu_name VARCHAR(255), sys_menu_path VARCHAR(255), sys_menu_pid BIGINT, sys_menu_type INT, sys_menu_sort INT, sys_menu_component VARCHAR(512), sys_menu_icon VARCHAR(255), sys_menu_hidden INT DEFAULT 0, sys_menu_keep_alive INT DEFAULT 0, create_by INT, create_time TIMESTAMP, update_by INT, update_time TIMESTAMP);

CREATE TABLE IF NOT EXISTS sys_role_menu (sys_role_menu_id BIGINT AUTO_INCREMENT PRIMARY KEY, sys_role_id BIGINT, sys_menu_id BIGINT);

CREATE TABLE IF NOT EXISTS sys_user_log (sys_log_id BIGINT AUTO_INCREMENT PRIMARY KEY, sys_log_username VARCHAR(255), sys_log_type VARCHAR(50), sys_log_status INT, sys_log_error_msg VARCHAR(2000), sys_log_url VARCHAR(512), sys_log_call_time BIGINT, create_by INT, create_time TIMESTAMP);

CREATE TABLE IF NOT EXISTS sys_setting (sys_setting_id BIGINT AUTO_INCREMENT PRIMARY KEY, sys_setting_name VARCHAR(255), sys_setting_code VARCHAR(255), sys_setting_value VARCHAR(2000), sys_setting_group VARCHAR(255), sys_setting_value_type VARCHAR(50), sys_setting_sort INT, sys_setting_status INT DEFAULT 1, create_by INT, create_time TIMESTAMP);

CREATE TABLE IF NOT EXISTS sys_setting_group (sys_setting_group_id BIGINT AUTO_INCREMENT PRIMARY KEY, sys_setting_group_name VARCHAR(255), sys_setting_group_code VARCHAR(255), sys_setting_group_sort INT, sys_setting_group_enable INT DEFAULT 1, create_by INT, create_time TIMESTAMP);

CREATE TABLE IF NOT EXISTS sys_file_system_group (sys_file_system_group_id INT AUTO_INCREMENT PRIMARY KEY, sys_file_system_group_name VARCHAR(100), sys_file_system_group_parent_id INT DEFAULT 0, create_by INT, create_time TIMESTAMP);

CREATE TABLE IF NOT EXISTS sys_job_cluster (sys_job_cluster_id VARCHAR(64) PRIMARY KEY, sys_job_cluster_leader_node VARCHAR(255), sys_job_cluster_lease_until_ms BIGINT, sys_job_cluster_version INT);


-- ---------------------------------------------------------------------
-- 来源: V1.0__init_monitor_complete.sql
-- ---------------------------------------------------------------------
-- 监控中心完整表结构
-- 包含 ACME 证书管理、服务器/组件布局管理、Docker 软件库与软件仓库

SET NAMES utf8;

SET FOREIGN_KEY_CHECKS = 0;

-- ACME 账户表
DROP TABLE IF EXISTS `monitor_acme_account`;

CREATE TABLE `monitor_acme_account` (
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

-- ============================================================================
-- 老库动态补列段：monitor_acme_account.provider_type（并入自 V1.2__add_acme_account_provider_type.sql）
-- 新库全量 DDL 已含该列；老库缺列时此处幂等补列
-- ============================================================================
SET @acme_col_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'monitor_acme_account' AND column_name = 'acme_account_provider_type'
);

SET @acme_add_sql = IF(@acme_col_exists = 0,
    'ALTER TABLE monitor_acme_account ADD COLUMN acme_account_provider_type varchar(32) NOT NULL DEFAULT ''acme'' COMMENT ''证书服务商类型(acme=ACME协议/acme4j、cas=阿里云CAS)'' AFTER acme_account_server',
    'SELECT ''acme_account_provider_type already exists, skipped'''
);

PREPARE acme_stmt FROM @acme_add_sql;

EXECUTE acme_stmt;

DEALLOCATE PREPARE acme_stmt;

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
CREATE TABLE IF NOT EXISTS `ms_monitor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `monitor_name` varchar(128) NOT NULL COMMENT '资产名称',
  `monitor_host` varchar(256) NOT NULL COMMENT '主机地址',
  `monitor_port` int NOT NULL DEFAULT 22 COMMENT '端口',
  `service_type` varchar(32) NOT NULL DEFAULT 'SERVER' COMMENT '服务类型(SERVER/REDIS/MYSQL/PG/ORACLE/MSSQL)',
  `server_type` varchar(32) NULL DEFAULT NULL COMMENT '系统类型(LINUX/WINDOWS/MACOS)',
  `monitor_group` varchar(64) NULL DEFAULT NULL COMMENT '分组',
  `monitor_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态(1启用/0禁用)',
  `monitor_description` varchar(512) NULL DEFAULT NULL COMMENT '备注说明',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_service_type` (`service_type`),
  KEY `idx_monitor_group` (`monitor_group`),
  KEY `idx_monitor_enabled` (`monitor_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监控资产表';

-- 采集任务表
CREATE TABLE IF NOT EXISTS `ms_monitor_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_name` varchar(128) NOT NULL COMMENT '任务名称',
  `monitor_id` bigint NOT NULL COMMENT '关联监控资产ID',
  `task_type` varchar(32) NOT NULL DEFAULT 'HEALTH' COMMENT '任务类型(HEALTH/COLLECT/ALERT/CUSTOM)',
  `task_cron` varchar(64) NULL DEFAULT NULL COMMENT 'Cron 表达式',
  `task_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态(1启用/0禁用)',
  `task_config` text NULL COMMENT '任务配置(JSON)',
  `task_status` varchar(32) NOT NULL DEFAULT 'STOPPED' COMMENT '运行状态(RUNNING/STOPPED/ERROR)',
  `task_description` varchar(512) NULL DEFAULT NULL COMMENT '备注说明',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_monitor_id` (`monitor_id`),
  KEY `idx_task_type` (`task_type`),
  KEY `idx_task_status` (`task_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采集任务表';

-- 指标解析规则表
CREATE TABLE IF NOT EXISTS `ms_monitor_metric_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `metric_name` varchar(128) NOT NULL COMMENT '指标名称',
  `task_id` bigint NOT NULL COMMENT '关联采集任务ID',
  `metric_type` varchar(32) NOT NULL COMMENT '指标类型(CPU/MEMORY/DISK/NETWORK/JVM/REDIS)',
  `match_pattern` varchar(512) NULL DEFAULT NULL COMMENT '匹配模式(正则)',
  `extract_rule` varchar(512) NULL DEFAULT NULL COMMENT '提取规则(JSON)',
  `unit` varchar(32) NULL DEFAULT NULL COMMENT '单位(%)',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_metric_type` (`metric_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标解析规则表';

-- 监控网关表
CREATE TABLE IF NOT EXISTS `ms_monitor_gateway` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `gateway_name` varchar(128) NOT NULL COMMENT '网关名称',
  `monitor_id` bigint NOT NULL COMMENT '关联监控资产ID',
  `gateway_host` varchar(256) NOT NULL COMMENT '网关主机',
  `gateway_port` int NOT NULL COMMENT '网关端口',
  `protocol` varchar(32) NOT NULL DEFAULT 'TCP' COMMENT '协议(TCP/UDP/HTTP)',
  `auth_enabled` tinyint NOT NULL DEFAULT 0 COMMENT '认证开关(1启用/0禁用)',
  `gateway_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '网关状态(ACTIVE/STANDBY/ERROR)',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_monitor_id` (`monitor_id`),
  KEY `idx_gateway_status` (`gateway_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监控网关表';

-- 全局配置表
CREATE TABLE IF NOT EXISTS `ms_monitor_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(128) NOT NULL COMMENT '配置键',
  `config_value` text NULL COMMENT '配置值',
  `config_type` varchar(32) NOT NULL DEFAULT 'STRING' COMMENT '值类型(STRING/NUMBER/BOOLEAN/JSON)',
  `config_group` varchar(64) NULL DEFAULT NULL COMMENT '配置分组',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局配置表';

SET FOREIGN_KEY_CHECKS = 1;

