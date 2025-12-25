-- ==================== 同步任务相关表 DDL ====================
-- 版本: 1.0
-- 描述: 创建数据同步任务所需的表结构
-- 注意: 此文件由 plugin.monitor.sync.auto-create-table=true 配置触发执行
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 同步任务表
DROP TABLE IF EXISTS `monitor_sync_task`;
CREATE TABLE `monitor_sync_task` (
  `sync_task_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `sync_task_name` VARCHAR(200) NOT NULL COMMENT '任务名称',
  `sync_task_desc` VARCHAR(500) DEFAULT NULL COMMENT '任务描述',
  `sync_task_status` VARCHAR(20) DEFAULT 'STOPPED' COMMENT '任务状态: STOPPED/RUNNING/ERROR',
  `sync_task_enabled` TINYINT DEFAULT 1 COMMENT '是否启用: 0-否 1-是',
  `sync_task_batch_size` INT DEFAULT 1000 COMMENT '批处理大小',
  `sync_task_consume_timeout` INT DEFAULT 30000 COMMENT '消费超时时间(ms)',
  `sync_task_retry_count` INT DEFAULT 3 COMMENT '重试次数',
  `sync_task_retry_interval` INT DEFAULT 1000 COMMENT '重试间隔(ms)',
  `sync_task_sync_interval` INT DEFAULT 0 COMMENT '同步间隔(ms)，0表示实时',
  `sync_task_ack_enabled` TINYINT DEFAULT 0 COMMENT '是否启用ACK: 0-否 1-是',
  `sync_task_transaction_enabled` TINYINT DEFAULT 0 COMMENT '是否启用事务: 0-否 1-是',
  `sync_task_cron` VARCHAR(100) DEFAULT NULL COMMENT 'Cron表达式',
  `sync_task_layout` TEXT COMMENT '设计器布局信息(JSON)',
  `sync_task_last_run_time` DATETIME DEFAULT NULL COMMENT '最后运行时间',
  `sync_task_last_run_status` VARCHAR(20) DEFAULT NULL COMMENT '最后运行状态',
  `sync_task_run_count` BIGINT DEFAULT 0 COMMENT '总运行次数',
  `sync_task_success_count` BIGINT DEFAULT 0 COMMENT '成功次数',
  `sync_task_fail_count` BIGINT DEFAULT 0 COMMENT '失败次数',
  `sync_task_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `sync_task_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`sync_task_id`),
  KEY `idx_task_name` (`sync_task_name`),
  KEY `idx_task_status` (`sync_task_status`),
  KEY `idx_create_time` (`sync_task_create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步任务表';

-- 同步节点表
DROP TABLE IF EXISTS `monitor_sync_node`;
CREATE TABLE `monitor_sync_node` (
  `sync_node_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `sync_task_id` BIGINT NOT NULL COMMENT '关联任务ID',
  `sync_node_type` VARCHAR(20) NOT NULL COMMENT '节点类型: INPUT/OUTPUT/FILTER/DATA_CENTER',
  `sync_node_spi_name` VARCHAR(100) NOT NULL COMMENT 'SPI名称',
  `sync_node_name` VARCHAR(200) DEFAULT NULL COMMENT '节点显示名称',
  `sync_node_key` VARCHAR(100) NOT NULL COMMENT '节点唯一标识(前端生成)',
  `sync_node_config` TEXT COMMENT '节点配置(JSON)',
  `sync_node_position` VARCHAR(100) DEFAULT NULL COMMENT '节点位置(JSON: {x,y})',
  `sync_node_order` INT DEFAULT 0 COMMENT '节点顺序',
  `sync_node_enabled` TINYINT DEFAULT 1 COMMENT '是否启用: 0-否 1-是',
  `sync_node_desc` VARCHAR(500) DEFAULT NULL COMMENT '节点描述',
  `sync_node_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `sync_node_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`sync_node_id`),
  KEY `idx_task_id` (`sync_task_id`),
  KEY `idx_node_key` (`sync_node_key`),
  KEY `idx_node_type` (`sync_node_type`),
  CONSTRAINT `fk_node_task` FOREIGN KEY (`sync_task_id`) REFERENCES `monitor_sync_task` (`sync_task_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步节点表';

-- 同步连线表
DROP TABLE IF EXISTS `monitor_sync_connection`;
CREATE TABLE `monitor_sync_connection` (
  `sync_connection_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '连线ID',
  `sync_task_id` BIGINT NOT NULL COMMENT '关联任务ID',
  `source_node_id` BIGINT DEFAULT NULL COMMENT '源节点ID',
  `source_node_key` VARCHAR(100) NOT NULL COMMENT '源节点Key',
  `source_handle` VARCHAR(50) DEFAULT 'output' COMMENT '源节点端口',
  `target_node_id` BIGINT DEFAULT NULL COMMENT '目标节点ID',
  `target_node_key` VARCHAR(100) NOT NULL COMMENT '目标节点Key',
  `target_handle` VARCHAR(50) DEFAULT 'input' COMMENT '目标节点端口',
  `connection_type` VARCHAR(20) DEFAULT 'DATA' COMMENT '连线类型: DATA/CONTROL',
  `connection_label` VARCHAR(100) DEFAULT NULL COMMENT '连线标签',
  `sync_connection_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`sync_connection_id`),
  KEY `idx_task_id` (`sync_task_id`),
  KEY `idx_source_key` (`source_node_key`),
  KEY `idx_target_key` (`target_node_key`),
  CONSTRAINT `fk_conn_task` FOREIGN KEY (`sync_task_id`) REFERENCES `monitor_sync_task` (`sync_task_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步连线表';

-- 同步任务执行日志表
DROP TABLE IF EXISTS `monitor_sync_task_log`;
CREATE TABLE `monitor_sync_task_log` (
  `sync_log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `sync_task_id` BIGINT NOT NULL COMMENT '关联任务ID',
  `sync_log_status` VARCHAR(20) DEFAULT 'RUNNING' COMMENT '执行状态: RUNNING/SUCCESS/FAIL/TIMEOUT',
  `sync_log_trigger_type` VARCHAR(20) DEFAULT 'MANUAL' COMMENT '触发类型: MANUAL/SCHEDULE/API',
  `sync_log_read_count` BIGINT DEFAULT 0 COMMENT '读取数量',
  `sync_log_write_count` BIGINT DEFAULT 0 COMMENT '写入数量',
  `sync_log_success_count` BIGINT DEFAULT 0 COMMENT '成功数量',
  `sync_log_fail_count` BIGINT DEFAULT 0 COMMENT '失败数量',
  `sync_log_retry_count` BIGINT DEFAULT 0 COMMENT '重试数量',
  `sync_log_dead_letter_count` BIGINT DEFAULT 0 COMMENT '死信数量',
  `sync_log_filter_count` BIGINT DEFAULT 0 COMMENT '过滤数量',
  `sync_log_start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `sync_log_end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `sync_log_cost` BIGINT DEFAULT 0 COMMENT '耗时(毫秒)',
  `sync_log_avg_process_time` DOUBLE DEFAULT 0 COMMENT '平均处理时间(毫秒)',
  `sync_log_throughput` DOUBLE DEFAULT 0 COMMENT '吞吐量(条/秒)',
  `sync_log_message` TEXT COMMENT '执行消息/错误信息',
  `sync_log_stack_trace` TEXT COMMENT '详细堆栈(错误时)',
  `sync_log_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`sync_log_id`),
  KEY `idx_task_id` (`sync_task_id`),
  KEY `idx_log_status` (`sync_log_status`),
  KEY `idx_start_time` (`sync_log_start_time`),
  KEY `idx_create_time` (`sync_log_create_time`),
  CONSTRAINT `fk_log_task` FOREIGN KEY (`sync_task_id`) REFERENCES `monitor_sync_task` (`sync_task_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步任务执行日志表';

-- 同步任务实时日志详情表
DROP TABLE IF EXISTS `monitor_sync_task_log_detail`;
CREATE TABLE `monitor_sync_task_log_detail` (
  `log_detail_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '详细日志ID',
  `sync_log_id` BIGINT NOT NULL COMMENT '关联执行日志ID',
  `sync_task_id` BIGINT NOT NULL COMMENT '关联任务ID',
  `log_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '日志时间(精确到毫秒)',
  `log_level` VARCHAR(10) DEFAULT 'INFO' COMMENT '日志级别: DEBUG/INFO/WARN/ERROR',
  `log_node_key` VARCHAR(100) DEFAULT NULL COMMENT '相关节点Key',
  `log_message` TEXT COMMENT '日志消息',
  `log_data` TEXT COMMENT '日志数据(JSON)',
  PRIMARY KEY (`log_detail_id`),
  KEY `idx_sync_log_id` (`sync_log_id`),
  KEY `idx_task_id` (`sync_task_id`),
  KEY `idx_log_time` (`log_time`),
  KEY `idx_log_level` (`log_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步任务实时日志详情表';

SET FOREIGN_KEY_CHECKS = 1;
