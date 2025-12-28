-- Spider可视化设计相关表
-- 创建时间: 2024/12/26

-- Spider节点表
CREATE TABLE IF NOT EXISTS `monitor_spider_node` (
    `node_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '节点ID',
    `spider_task_id` BIGINT NOT NULL COMMENT '所属任务ID',
    `node_key` VARCHAR(64) DEFAULT NULL COMMENT '节点唯一标识',
    `node_name` VARCHAR(128) NOT NULL COMMENT '节点名称',
    `node_type` VARCHAR(32) NOT NULL COMMENT '节点类型: URL_SOURCE/DOWNLOADER/PROCESSOR/MIDDLEWARE/DATA_FILTER/PIPELINE/SCHEDULER/SITE_CONFIG/PROXY_POOL/UA_POOL',
    `spi_name` VARCHAR(64) NOT NULL COMMENT 'SPI组件名称',
    `node_config` TEXT COMMENT '节点配置JSON',
    `node_description` VARCHAR(512) DEFAULT NULL COMMENT '节点描述',
    `position_x` INT DEFAULT 100 COMMENT 'X坐标',
    `position_y` INT DEFAULT 100 COMMENT 'Y坐标',
    `node_order` INT DEFAULT 0 COMMENT '节点排序',
    `node_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
    `requires_config` TINYINT(1) DEFAULT 0 COMMENT '是否需要配置: 0-不需要, 1-需要',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`node_id`),
    INDEX `idx_spider_task_id` (`spider_task_id`),
    INDEX `idx_node_type` (`node_type`),
    INDEX `idx_node_key` (`spider_task_id`, `node_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spider任务节点表';

-- Spider连接表
CREATE TABLE IF NOT EXISTS `monitor_spider_connection` (
    `connection_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '连接ID',
    `spider_task_id` BIGINT NOT NULL COMMENT '所属任务ID',
    `source_node_id` BIGINT NOT NULL COMMENT '源节点ID',
    `target_node_id` BIGINT NOT NULL COMMENT '目标节点ID',
    `source_port` VARCHAR(32) DEFAULT 'output' COMMENT '源端口',
    `target_port` VARCHAR(32) DEFAULT 'input' COMMENT '目标端口',
    `connection_type` VARCHAR(32) DEFAULT 'DATA' COMMENT '连接类型: DATA-数据流, CONTROL-控制流',
    `connection_label` VARCHAR(128) DEFAULT NULL COMMENT '连接标签',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`connection_id`),
    INDEX `idx_spider_task_id` (`spider_task_id`),
    INDEX `idx_source_node_id` (`source_node_id`),
    INDEX `idx_target_node_id` (`target_node_id`),
    UNIQUE INDEX `uk_connection` (`spider_task_id`, `source_node_id`, `target_node_id`, `source_port`, `target_port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spider任务连接表';

-- 添加外键约束（可选，根据实际需求启用）
-- ALTER TABLE `monitor_spider_node` ADD CONSTRAINT `fk_node_task` FOREIGN KEY (`spider_task_id`) REFERENCES `monitor_spider_task`(`spider_task_id`) ON DELETE CASCADE;
-- ALTER TABLE `monitor_spider_connection` ADD CONSTRAINT `fk_conn_task` FOREIGN KEY (`spider_task_id`) REFERENCES `monitor_spider_task`(`spider_task_id`) ON DELETE CASCADE;
-- ALTER TABLE `monitor_spider_connection` ADD CONSTRAINT `fk_conn_source` FOREIGN KEY (`source_node_id`) REFERENCES `monitor_spider_node`(`node_id`) ON DELETE CASCADE;
-- ALTER TABLE `monitor_spider_connection` ADD CONSTRAINT `fk_conn_target` FOREIGN KEY (`target_node_id`) REFERENCES `monitor_spider_node`(`node_id`) ON DELETE CASCADE;
