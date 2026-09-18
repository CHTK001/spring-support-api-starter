-- =====================================================================
-- V1.0__init_all_api_common_system.sql - spring-api-support-common-system-starter 全量 DDL（合并自 1 个文件）
-- 通道: INIT 同步建表；老库缺列/缺表由 information_schema + PREPARE 段幂等补齐。
-- 种子数据见 V1.0__initdata_all.sql（虚拟线程异步，不阻塞启动）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 来源: V1.0__init_sys_error_log.sql
-- ---------------------------------------------------------------------
/*
 * 前端异常上报日志表 - 数据库初始化脚本
 *
 * 包含表:
 *   - sys_error_log: 前端异常上报日志表, 承接 monitor worker 批量上报的运行时错误
 *
 * 使用 CREATE TABLE IF NOT EXISTS 保证脚本幂等, 已存在的库不会重复建表。
 *
 * @author CH
 * @since 2026-08-08
 */
CREATE TABLE IF NOT EXISTS `sys_error_log` (
  `sys_error_log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '异常日志ID',
  `sys_error_key` varchar(512) DEFAULT NULL COMMENT '去重指纹(message+source+行列号归一化)',
  `sys_error_source` varchar(32) DEFAULT NULL COMMENT '异常来源: js:JS运行时错误 rejection:未处理Promise拒绝 resource:资源加载失败 manual:手动捕获',
  `sys_error_message` varchar(2000) DEFAULT NULL COMMENT '异常消息',
  `sys_error_stack` text DEFAULT NULL COMMENT '异常堆栈',
  `sys_error_filename` varchar(512) DEFAULT NULL COMMENT '出错文件URL',
  `sys_error_lineno` INT DEFAULT NULL COMMENT '出错行号',
  `sys_error_colno` INT DEFAULT NULL COMMENT '出错列号',
  `sys_error_resource_url` varchar(512) DEFAULT NULL COMMENT '资源加载失败的目标URL',
  `sys_error_tag_name` varchar(32) DEFAULT NULL COMMENT '资源元素标签(如IMG/SCRIPT/LINK)',
  `sys_error_first_at` BIGINT DEFAULT NULL COMMENT '首次发生时间(毫秒时间戳)',
  `sys_error_count` INT DEFAULT 1 COMMENT '去重窗口内累计发生次数',
  `sys_error_report_time` BIGINT DEFAULT NULL COMMENT '客户端上报时间(毫秒时间戳)',
  `create_name` varchar(255) DEFAULT NULL COMMENT '创建人姓名',
  `create_by` INT DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) DEFAULT NULL COMMENT '更新人姓名',
  `update_by` INT DEFAULT NULL COMMENT '更新人',
  `sys_error_status` int NULL DEFAULT 0 COMMENT '处理状态: 0-未处理 1-已处理 2-已忽略',
  PRIMARY KEY (`sys_error_log_id`),
  KEY `sys_error_log_idx_error_log_source` (`sys_error_source`),
  KEY `sys_error_log_idx_error_log_key` (`sys_error_key`),
  KEY `sys_error_log_idx_error_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='前端异常上报日志表';


-- ---------------------------------------------------------------------
-- 来源: SysUserTripartiteBind 实体（第三方账号绑定），此前无对应建表脚本
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user_tripartite_bind` (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `sys_tenant_id` int NULL DEFAULT NULL COMMENT '租户ID',
  `login_code_type` varchar(255) NULL DEFAULT NULL COMMENT '登录类型',
  `login_username` varchar(255) NULL DEFAULT NULL COMMENT '登录账号',
  `login_uuid` varchar(255) NULL DEFAULT NULL COMMENT '三方唯一标识',
  `login_avatar` varchar(255) NULL DEFAULT NULL COMMENT '头像',
  `login_name` varchar(255) NULL DEFAULT NULL COMMENT '昵称',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户第三方绑定';
