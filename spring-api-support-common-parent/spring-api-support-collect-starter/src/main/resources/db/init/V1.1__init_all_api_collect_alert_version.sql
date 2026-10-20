-- =====================================================================
-- V1.1__init_all_api_collect_alert_version.sql
-- 通道: INIT 同步建表（MySQL 8 语法，H2 MODE=MySQL 回放）。
-- 为采集增强新增两张表：告警规则（失败次数/零产出/数据量骤降/节点超时/错误率）
-- 与流程版本快照（保存/回滚 DSL）。
-- 列名统一带表名前缀，公共审计列沿用 SysBase（create_*/update_*）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 采集告警规则
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `collect_alert_rule` (
    `collect_alert_rule_id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `collect_flow_id` INT NULL DEFAULT NULL COMMENT '所属流程ID，空表示全局规则',
    `collect_alert_rule_name` varchar(128) NULL DEFAULT NULL COMMENT '规则名称',
    `alert_type` varchar(32) NULL DEFAULT 'FAILURE_COUNT' COMMENT '条件类型 - FAILURE_COUNT/ZERO_OUTPUT/RECORDS_DROP/NODE_TIMEOUT/ERROR_RATE',
    `threshold` INT NULL DEFAULT 0 COMMENT '阈值，含义随 alert_type 而定',
    `alert_timing` varchar(16) NULL DEFAULT 'ALWAYS' COMMENT '告警时机 - ALWAYS/ONCE',
    `alert_enabled` INT NULL DEFAULT 1 COMMENT '是否启用 - 0:停用, 1:启用',
    `last_alert_time` datetime(6) NULL DEFAULT NULL COMMENT '最近触发时间',
    `last_alert_result` varchar(500) NULL DEFAULT NULL COMMENT '最近触发结果',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人',
    `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`collect_alert_rule_id`),
    INDEX `collect_alert_rule_idx_flow` (`collect_flow_id`),
    INDEX `collect_alert_rule_idx_enabled` (`alert_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据采集-告警规则';

-- ---------------------------------------------------------------------
-- 流程版本快照
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `collect_flow_version` (
    `collect_flow_version_id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `collect_flow_id` INT NULL DEFAULT NULL COMMENT '流程ID',
    `version_no` INT NULL DEFAULT 0 COMMENT '版本号，从1开始递增',
    `flow_dsl` longtext NULL DEFAULT NULL COMMENT '流程 DSL 快照（JSON）',
    `version_remark` varchar(255) NULL DEFAULT NULL COMMENT '版本说明',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人',
    `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`collect_flow_version_id`),
    INDEX `collect_flow_version_idx_flow_no` (`collect_flow_id`, `version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据采集-流程版本';