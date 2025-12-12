-- ========================================
-- 脚本执行记录表
-- 用于记录脚本执行的历史记录
-- @author CH
-- @since 2024-12-12
-- ========================================

-- 创建脚本执行记录表（如果不存在）
CREATE TABLE IF NOT EXISTS `monitor_sys_gen_script_execution` (
    `monitor_sys_gen_script_execution_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '执行记录ID',
    `monitor_sys_gen_script_id` INT NOT NULL COMMENT '脚本ID',
    `monitor_sys_gen_script_execution_parameters` TEXT COMMENT '执行参数',
    `monitor_sys_gen_script_execution_status` VARCHAR(20) DEFAULT 'RUNNING' COMMENT '执行状态: RUNNING, SUCCESS, FAILED, TIMEOUT, CANCELLED',
    `monitor_sys_gen_script_execution_start_time` DATETIME COMMENT '开始执行时间',
    `monitor_sys_gen_script_execution_end_time` DATETIME COMMENT '结束执行时间',
    `monitor_sys_gen_script_execution_duration` BIGINT COMMENT '执行耗时(毫秒)',
    `monitor_sys_gen_script_execution_exit_code` INT COMMENT '退出码',
    `monitor_sys_gen_script_execution_stdout` LONGTEXT COMMENT '标准输出',
    `monitor_sys_gen_script_execution_stderr` LONGTEXT COMMENT '错误输出',
    `monitor_sys_gen_script_execution_error_message` TEXT COMMENT '错误信息',
    `monitor_sys_gen_script_execution_process_id` BIGINT COMMENT '进程ID',
    `monitor_sys_gen_script_execution_task_id` VARCHAR(100) COMMENT '任务ID',
    `monitor_sys_gen_script_execution_server_id` INT COMMENT '执行服务器ID（单个）',
    `monitor_sys_gen_script_execution_server_ids` VARCHAR(500) COMMENT '执行服务器ID列表（逗号分隔，支持多服务器）',
    `monitor_sys_gen_script_execution_server_names` VARCHAR(1000) COMMENT '执行服务器名称列表（逗号分隔）',
    `monitor_sys_gen_script_execution_method` VARCHAR(20) DEFAULT 'SSH' COMMENT '执行方式: SSH, NODE',
    `monitor_sys_gen_script_execution_content` LONGTEXT COMMENT '脚本内容快照（执行时的脚本内容）',
    `monitor_sys_gen_script_execution_script_name` VARCHAR(200) COMMENT '脚本名称',
    `monitor_sys_gen_script_execution_script_type` VARCHAR(50) COMMENT '脚本类型',
    `monitor_sys_gen_script_execution_user` VARCHAR(100) COMMENT '执行人',
    `monitor_sys_gen_script_execution_trigger_type` VARCHAR(20) DEFAULT 'MANUAL' COMMENT '触发类型: MANUAL, SCHEDULED, API',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`monitor_sys_gen_script_execution_id`),
    INDEX `idx_script_id` (`monitor_sys_gen_script_id`),
    INDEX `idx_status` (`monitor_sys_gen_script_execution_status`),
    INDEX `idx_start_time` (`monitor_sys_gen_script_execution_start_time`),
    INDEX `idx_method` (`monitor_sys_gen_script_execution_method`),
    INDEX `idx_user` (`monitor_sys_gen_script_execution_user`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='脚本执行记录表';

-- ========================================
-- 如果表已存在，添加新字段的ALTER语句
-- ========================================

-- 添加多服务器支持字段
ALTER TABLE `monitor_sys_gen_script_execution` 
ADD COLUMN IF NOT EXISTS `monitor_sys_gen_script_execution_server_ids` VARCHAR(500) COMMENT '执行服务器ID列表（逗号分隔，支持多服务器）' AFTER `monitor_sys_gen_script_execution_server_id`;

ALTER TABLE `monitor_sys_gen_script_execution` 
ADD COLUMN IF NOT EXISTS `monitor_sys_gen_script_execution_server_names` VARCHAR(1000) COMMENT '执行服务器名称列表（逗号分隔）' AFTER `monitor_sys_gen_script_execution_server_ids`;

-- 添加执行方式字段
ALTER TABLE `monitor_sys_gen_script_execution` 
ADD COLUMN IF NOT EXISTS `monitor_sys_gen_script_execution_method` VARCHAR(20) DEFAULT 'SSH' COMMENT '执行方式: SSH, NODE' AFTER `monitor_sys_gen_script_execution_server_names`;

-- 添加脚本内容快照字段
ALTER TABLE `monitor_sys_gen_script_execution` 
ADD COLUMN IF NOT EXISTS `monitor_sys_gen_script_execution_content` LONGTEXT COMMENT '脚本内容快照（执行时的脚本内容）' AFTER `monitor_sys_gen_script_execution_method`;

-- 添加脚本名称字段
ALTER TABLE `monitor_sys_gen_script_execution` 
ADD COLUMN IF NOT EXISTS `monitor_sys_gen_script_execution_script_name` VARCHAR(200) COMMENT '脚本名称' AFTER `monitor_sys_gen_script_execution_content`;

-- 添加脚本类型字段
ALTER TABLE `monitor_sys_gen_script_execution` 
ADD COLUMN IF NOT EXISTS `monitor_sys_gen_script_execution_script_type` VARCHAR(50) COMMENT '脚本类型' AFTER `monitor_sys_gen_script_execution_script_name`;

-- 添加执行人字段
ALTER TABLE `monitor_sys_gen_script_execution` 
ADD COLUMN IF NOT EXISTS `monitor_sys_gen_script_execution_user` VARCHAR(100) COMMENT '执行人' AFTER `monitor_sys_gen_script_execution_script_type`;

-- 添加索引
CREATE INDEX IF NOT EXISTS `idx_method` ON `monitor_sys_gen_script_execution` (`monitor_sys_gen_script_execution_method`);
CREATE INDEX IF NOT EXISTS `idx_user` ON `monitor_sys_gen_script_execution` (`monitor_sys_gen_script_execution_user`);
