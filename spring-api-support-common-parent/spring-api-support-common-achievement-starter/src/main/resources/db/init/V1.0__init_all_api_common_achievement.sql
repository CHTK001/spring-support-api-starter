-- =====================================================================
-- V1.0__init_all_api_common_achievement.sql - spring-api-support-common-achievement-starter 全量 DDL（合并自 1 个文件）
-- 通道: INIT 同步建表；老库缺列/缺表由 information_schema + PREPARE 段幂等补齐。
-- 种子数据见 V1.0__initdata_all.sql（虚拟线程异步，不阻塞启动）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 来源: V1.0__init_achievement.sql
-- ---------------------------------------------------------------------
/*
 * 成就系统 - 数据库初始化脚本
 *
 * 包含表:
 *   - achievement_tier: 成就段位定义表
 *   - user_achievement: 用户成就记录表
 *
 * <p>规则表达式格式：使用 utils-support-common-starter 的 CircuitBreaker 表达式语法，
 * 支持 > >= < <= == != AND OR NOT LIKE IN BETWEEN 等运算符。</p>
 *
 * <p>示例：loginCount >= 7 表示登录次数大于等于 7 次。</p>
 *
 * @author CH
 * @since 2026-08-07
 */

SET NAMES utf8;

SET FOREIGN_KEY_CHECKS = 0;


CREATE TABLE IF NOT EXISTS `achievement_tier` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` VARCHAR(64) NOT NULL COMMENT '分组标识',
    `level` INT NOT NULL COMMENT '段位等级',
    `name` VARCHAR(128) NOT NULL COMMENT '成就名称',
    `badge` VARCHAR(256) NULL DEFAULT NULL COMMENT '徽章图片 URL 或 emoji',
    `description` VARCHAR(512) NULL DEFAULT NULL COMMENT '成就描述',
    `rule` VARCHAR(512) NOT NULL COMMENT 'CircuitBreaker 表达式规则',
    `sort` INT NULL DEFAULT 0 COMMENT '同组内排序权重',
    `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `achievement_tier_uk_type_level` (`type`, `level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成就段位定义表';


CREATE TABLE IF NOT EXISTS `user_achievement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` VARCHAR(64) NOT NULL COMMENT '分组标识',
    `max_level` INT NOT NULL DEFAULT 0 COMMENT '该组已解锁的最高段位等级',
    `unlocked_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次解锁时间',
    `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `user_achievement_uk_user_type` (`user_id`, `type`),
    INDEX `user_achievement_idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户成就记录表';

-- 成就段位种子数据已拆分至 V1.0__initdata_achievement.sql（异步执行，不阻塞启动）

SET FOREIGN_KEY_CHECKS = 1;

