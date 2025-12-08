-- 预览扩展名配置表
-- 用于配置 ViewServletFilter 的扩展名白名单/黑名单
-- @author CH
-- @since 2024/12/08

CREATE TABLE IF NOT EXISTS `system_server_setting_preview_extension` (
    `system_server_setting_preview_extension_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `preview_extension_server_id` INT(11) NOT NULL COMMENT '所属服务器ID',
    `preview_extension_disabled` VARCHAR(2000) DEFAULT NULL COMMENT '禁用预览的扩展名列表（逗号分隔）',
    `preview_extension_allowed` VARCHAR(2000) DEFAULT NULL COMMENT '允许预览的扩展名列表（逗号分隔）',
    `preview_extension_whitelist_mode` TINYINT(1) DEFAULT 0 COMMENT '是否启用白名单模式（0=黑名单，1=白名单）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`system_server_setting_preview_extension_id`),
    UNIQUE KEY `uk_server_id` (`preview_extension_server_id`),
    KEY `idx_server_id` (`preview_extension_server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预览扩展名配置表';

-- 文件分享表
-- 用于存储分享链接和闪图信息
CREATE TABLE IF NOT EXISTS `monitor_sys_file_share` (
    `share_id` VARCHAR(64) NOT NULL COMMENT '分享ID（UUID）',
    `server_id` BIGINT NOT NULL COMMENT '服务器ID',
    `bucket` VARCHAR(255) DEFAULT NULL COMMENT '存储桶',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名',
    `share_type` VARCHAR(20) DEFAULT 'SHARE' COMMENT '分享类型：SHARE-普通分享，FLASH-闪图',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间，NULL表示永不过期',
    `view_count` INT DEFAULT 0 COMMENT '查看次数',
    `max_view_count` INT DEFAULT -1 COMMENT '最大查看次数，-1表示无限制，1表示闪图',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否已删除：0-否，1-是',
    PRIMARY KEY (`share_id`),
    INDEX `idx_server_id` (`server_id`),
    INDEX `idx_expire_time` (`expire_time`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件分享表';


-- 服务发现配置表添加 Monitor 和 Hazelcast 模式相关字段
-- @author CH
-- @since 2024/12/08

-- 添加 Monitor 模式相关字段
ALTER TABLE system_server_setting_service_discovery
    ADD COLUMN IF NOT EXISTS service_discovery_monitor_host VARCHAR(255) DEFAULT NULL COMMENT 'Monitor服务地址，用于连接SyncServer';

ALTER TABLE system_server_setting_service_discovery
    ADD COLUMN IF NOT EXISTS service_discovery_monitor_port INT DEFAULT NULL COMMENT 'Monitor服务端口，用于连接SyncServer';

-- 添加 Hazelcast 模式相关字段
ALTER TABLE system_server_setting_service_discovery
    ADD COLUMN IF NOT EXISTS service_discovery_hazelcast_cluster_name VARCHAR(200) DEFAULT NULL COMMENT 'Hazelcast集群名称';

ALTER TABLE system_server_setting_service_discovery
    ADD COLUMN IF NOT EXISTS service_discovery_hazelcast_members VARCHAR(1000) DEFAULT NULL COMMENT 'Hazelcast成员地址，多个用逗号分隔';

ALTER TABLE system_server_setting_service_discovery
    ADD COLUMN IF NOT EXISTS service_discovery_hazelcast_port INT DEFAULT NULL COMMENT 'Hazelcast端口';

ALTER TABLE system_server_setting_service_discovery
    ADD COLUMN IF NOT EXISTS service_discovery_hazelcast_timeout INT DEFAULT NULL COMMENT 'Hazelcast连接超时，单位秒';

-- 更新 mode 字段的注释，增加 MONITOR 选项
ALTER TABLE system_server_setting_service_discovery
    MODIFY COLUMN service_discovery_mode VARCHAR(50) DEFAULT NULL COMMENT '服务发现模式: MONITOR/SPRING/TABLE/HAZELCAST';

