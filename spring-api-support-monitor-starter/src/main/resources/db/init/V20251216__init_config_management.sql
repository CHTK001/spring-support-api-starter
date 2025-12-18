-- 配置管理表
-- Create by CH
-- @since 2024/12/16

CREATE TABLE IF NOT EXISTS `monitor_sys_gen_config` (
  `monitor_sys_gen_config_id` INT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `monitor_sys_gen_config_key` VARCHAR(255) NOT NULL COMMENT '配置键',
  `monitor_sys_gen_config_value` TEXT COMMENT '配置值',
  `monitor_sys_gen_config_description` VARCHAR(500) COMMENT '配置描述',
  `monitor_sys_gen_config_env` VARCHAR(50) COMMENT '环境 (dev/test/prod)',
  `monitor_sys_gen_config_status` TINYINT DEFAULT 1 COMMENT '配置状态 0-禁用 1-启用',
  `monitor_sys_gen_config_app` VARCHAR(100) COMMENT '所属应用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `update_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_config_id`),
  UNIQUE KEY `uk_key_env` (`monitor_sys_gen_config_key`, `monitor_sys_gen_config_env`),
  KEY `idx_env` (`monitor_sys_gen_config_env`),
  KEY `idx_app` (`monitor_sys_gen_config_app`),
  KEY `idx_status` (`monitor_sys_gen_config_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置管理表';

-- 配置下发历史表
CREATE TABLE IF NOT EXISTS `monitor_sys_gen_config_push_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `monitor_sys_gen_config_id` INT NOT NULL COMMENT '配置ID',
  `monitor_sys_gen_config_key` VARCHAR(255) NOT NULL COMMENT '配置键',
  `monitor_sys_gen_config_value` TEXT COMMENT '配置值（推送时的快照）',
  `monitor_sys_gen_server_id` INT NOT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_name` VARCHAR(100) COMMENT '服务器名称',
  `push_success` TINYINT DEFAULT 0 COMMENT '是否成功 0-失败 1-成功',
  `error_message` VARCHAR(500) COMMENT '错误信息',
  `push_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下发时间',
  `operator` VARCHAR(64) COMMENT '操作人',
  PRIMARY KEY (`id`),
  KEY `idx_config_id` (`monitor_sys_gen_config_id`),
  KEY `idx_server_id` (`monitor_sys_gen_server_id`),
  KEY `idx_push_time` (`push_time`),
  KEY `idx_push_success` (`push_success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置下发历史表';
