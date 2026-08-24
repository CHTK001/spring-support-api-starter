-- 监控模块核心业务表（资产、采集任务、解析规则、网关、全局配置）
-- 对应 pages/monitor 前端模块

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- 监控资产表
DROP TABLE IF EXISTS `ms_monitor`;
CREATE TABLE `ms_monitor` (
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
DROP TABLE IF EXISTS `ms_monitor_task`;
CREATE TABLE `ms_monitor_task` (
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
DROP TABLE IF EXISTS `ms_monitor_metric_config`;
CREATE TABLE `ms_monitor_metric_config` (
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
DROP TABLE IF EXISTS `ms_monitor_gateway`;
CREATE TABLE `ms_monitor_gateway` (
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
DROP TABLE IF EXISTS `ms_monitor_config`;
CREATE TABLE `ms_monitor_config` (
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