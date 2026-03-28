SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `monitor_spider_task` (
  `spider_task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `spider_task_name` varchar(200) NOT NULL COMMENT '任务名称',
  `spider_task_desc` varchar(500) DEFAULT NULL COMMENT '任务描述',
  `spider_task_url` varchar(1000) NOT NULL COMMENT '目标URL',
  `spider_task_url_pattern` varchar(1000) DEFAULT NULL COMMENT 'URL模式',
  `spider_task_extract_rules` longtext COMMENT '提取规则JSON',
  `spider_task_selector_type` varchar(20) DEFAULT 'XPATH' COMMENT '选择器类型',
  `spider_task_status` tinyint DEFAULT '1' COMMENT '任务状态: 0-停用, 1-启用',
  `spider_task_schedule_type` varchar(20) DEFAULT 'NONE' COMMENT '调度类型: NONE-一次性, CRON-定时',
  `spider_task_cron` varchar(100) DEFAULT NULL COMMENT 'CRON表达式',
  `spider_task_thread_num` int DEFAULT '1' COMMENT '线程数',
  `spider_task_sleep_time` int DEFAULT '1000' COMMENT '爬取间隔(毫秒)',
  `spider_task_retry_times` int DEFAULT '3' COMMENT '重试次数',
  `spider_task_timeout` int DEFAULT '30' COMMENT '超时时间(秒)',
  `spider_task_headers` longtext COMMENT '请求头配置(JSON)',
  `spider_task_cookies` longtext COMMENT 'Cookie配置',
  `spider_task_user_agent` varchar(500) DEFAULT NULL COMMENT 'User-Agent配置',
  `spider_task_proxy` varchar(500) DEFAULT NULL COMMENT '代理配置',
  `spider_task_max_depth` int DEFAULT '0' COMMENT '最大爬取深度',
  `spider_task_enable_url_discover` tinyint(1) DEFAULT '0' COMMENT '是否启用URL自动发现',
  `spider_task_page_pattern` varchar(1000) DEFAULT NULL COMMENT '分页URL模式',
  `spider_task_max_pages` int DEFAULT '0' COMMENT '最大分页数',
  `spider_task_incremental_mode` varchar(20) DEFAULT 'NONE' COMMENT '增量爬取模式',
  `spider_task_rate_limit` int DEFAULT '0' COMMENT '速率限制',
  `spider_task_pipeline_type` varchar(20) DEFAULT 'JSON' COMMENT '存储类型: JSON/DATABASE/FILE',
  `spider_task_pipeline_config` longtext COMMENT '存储配置(JSON)',
  `spider_task_last_run_time` datetime DEFAULT NULL COMMENT '最后运行时间',
  `spider_task_next_run_time` datetime DEFAULT NULL COMMENT '下次运行时间',
  `spider_task_run_status` varchar(20) DEFAULT 'IDLE' COMMENT '运行状态: IDLE/RUNNING/STOPPED/ERROR',
  `spider_task_total_success` bigint DEFAULT '0' COMMENT '总成功数',
  `spider_task_total_fail` bigint DEFAULT '0' COMMENT '总失败数',
  `spider_task_job_id` int DEFAULT NULL COMMENT '关联的Job ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`spider_task_id`),
  KEY `idx_spider_task_status` (`spider_task_status`),
  KEY `idx_spider_task_run_status` (`spider_task_run_status`),
  KEY `idx_spider_task_name` (`spider_task_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫任务表';

CREATE TABLE IF NOT EXISTS `monitor_spider_data` (
  `spider_data_id` bigint NOT NULL AUTO_INCREMENT COMMENT '数据ID',
  `spider_task_id` bigint NOT NULL COMMENT '任务ID',
  `spider_data_content` longtext COMMENT '爬取的数据内容',
  `spider_data_url` varchar(1000) DEFAULT NULL COMMENT '数据来源URL',
  `spider_data_hash` varchar(64) DEFAULT NULL COMMENT '数据哈希(用于去重)',
  `crawl_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '爬取时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`spider_data_id`),
  KEY `idx_spider_data_task_id` (`spider_task_id`),
  UNIQUE KEY `uk_spider_data_hash` (`spider_data_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫数据表';

CREATE TABLE IF NOT EXISTS `monitor_spider_log` (
  `spider_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `spider_task_id` bigint NOT NULL COMMENT '任务ID',
  `spider_log_level` varchar(20) DEFAULT 'INFO' COMMENT '日志级别',
  `spider_log_message` longtext COMMENT '日志内容',
  `spider_log_url` varchar(1000) DEFAULT NULL COMMENT '相关URL',
  `spider_log_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '日志时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`spider_log_id`),
  KEY `idx_spider_log_task_id` (`spider_task_id`),
  KEY `idx_spider_log_time` (`spider_log_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫日志表';

CREATE TABLE IF NOT EXISTS `monitor_spider_node` (
  `node_id` bigint NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `spider_task_id` bigint NOT NULL COMMENT '所属任务ID',
  `node_key` varchar(64) DEFAULT NULL COMMENT '节点唯一标识',
  `node_name` varchar(128) NOT NULL COMMENT '节点名称',
  `node_type` varchar(32) NOT NULL COMMENT '节点类型',
  `spi_name` varchar(64) NOT NULL COMMENT 'SPI组件名称',
  `node_config` longtext COMMENT '节点配置JSON',
  `node_description` varchar(512) DEFAULT NULL COMMENT '节点描述',
  `position_x` int DEFAULT '100' COMMENT 'X坐标',
  `position_y` int DEFAULT '100' COMMENT 'Y坐标',
  `node_order` int DEFAULT '0' COMMENT '节点排序',
  `node_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `requires_config` tinyint(1) DEFAULT '0' COMMENT '是否需要配置',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`node_id`),
  KEY `idx_spider_node_task_id` (`spider_task_id`),
  KEY `idx_spider_node_type` (`node_type`),
  KEY `idx_spider_node_key` (`spider_task_id`,`node_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫节点表';

CREATE TABLE IF NOT EXISTS `monitor_spider_connection` (
  `connection_id` bigint NOT NULL AUTO_INCREMENT COMMENT '连接ID',
  `spider_task_id` bigint NOT NULL COMMENT '所属任务ID',
  `source_node_id` bigint NOT NULL COMMENT '源节点ID',
  `target_node_id` bigint NOT NULL COMMENT '目标节点ID',
  `source_port` varchar(32) DEFAULT 'output' COMMENT '源端口',
  `target_port` varchar(32) DEFAULT 'input' COMMENT '目标端口',
  `connection_type` varchar(32) DEFAULT 'DATA' COMMENT '连接类型',
  `connection_label` varchar(128) DEFAULT NULL COMMENT '连接标签',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`connection_id`),
  KEY `idx_spider_connection_task_id` (`spider_task_id`),
  KEY `idx_spider_connection_source_node_id` (`source_node_id`),
  KEY `idx_spider_connection_target_node_id` (`target_node_id`),
  UNIQUE KEY `uk_spider_connection` (`spider_task_id`,`source_node_id`,`target_node_id`,`source_port`,`target_port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫连线表';

ALTER TABLE `monitor_spider_task`
  ADD COLUMN IF NOT EXISTS `spider_task_max_depth` int DEFAULT '0' COMMENT '最大爬取深度',
  ADD COLUMN IF NOT EXISTS `spider_task_enable_url_discover` tinyint(1) DEFAULT '0' COMMENT '是否启用URL自动发现',
  ADD COLUMN IF NOT EXISTS `spider_task_page_pattern` varchar(1000) DEFAULT NULL COMMENT '分页URL模式',
  ADD COLUMN IF NOT EXISTS `spider_task_max_pages` int DEFAULT '0' COMMENT '最大分页数',
  ADD COLUMN IF NOT EXISTS `spider_task_incremental_mode` varchar(20) DEFAULT 'NONE' COMMENT '增量爬取模式',
  ADD COLUMN IF NOT EXISTS `spider_task_rate_limit` int DEFAULT '0' COMMENT '速率限制',
  ADD COLUMN IF NOT EXISTS `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  ADD COLUMN IF NOT EXISTS `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE `monitor_spider_data`
  ADD COLUMN IF NOT EXISTS `crawl_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '爬取时间',
  ADD COLUMN IF NOT EXISTS `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

ALTER TABLE `monitor_spider_log`
  ADD COLUMN IF NOT EXISTS `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

SET FOREIGN_KEY_CHECKS = 1;
