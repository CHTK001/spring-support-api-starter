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

DROP TABLE IF EXISTS `achievement_tier`;
CREATE TABLE `achievement_tier` (
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
    UNIQUE INDEX `uk_type_level` (`type`, `level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成就段位定义表';

DROP TABLE IF EXISTS `user_achievement`;
CREATE TABLE `user_achievement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` VARCHAR(64) NOT NULL COMMENT '分组标识',
    `max_level` INT NOT NULL DEFAULT 0 COMMENT '该组已解锁的最高段位等级',
    `unlocked_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次解锁时间',
    `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_type` (`user_id`, `type`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户成就记录表';

-- 登录成就
INSERT INTO `achievement_tier` (`type`, `level`, `name`, `badge`, `description`, `rule`, `sort`) VALUES
('login', 1, '初次登录', '🌱', '第一次登录系统', 'loginCount >= 1', 1),
('login', 2, '登录达人 Ⅰ', '🏅', '累计登录 7 天', 'loginCount >= 7', 2),
('login', 3, '登录达人 Ⅱ', '🎖️', '累计登录 30 天', 'loginCount >= 30', 3),
('login', 4, '登录达人 Ⅲ', '🏆', '累计登录 100 天', 'loginCount >= 100', 4);

-- 签到成就
INSERT INTO `achievement_tier` (`type`, `level`, `name`, `badge`, `description`, `rule`, `sort`) VALUES
('sign', 1, '签到新秀', '📌', '累计签到 3 天', 'signInCount >= 3', 1),
('sign', 2, '签到达人', '📅', '累计签到 7 天', 'signInCount >= 7', 2),
('sign', 3, '签到精英', '🔥', '累计签到 30 天', 'signInCount >= 30', 3),
('sign', 4, '签到大师', '💎', '累计签到 100 天', 'signInCount >= 100', 4);

-- 菜单成就
INSERT INTO `achievement_tier` (`type`, `level`, `name`, `badge`, `description`, `rule`, `sort`) VALUES
('menu', 1, '菜单探索者', '🔍', '累计浏览菜单 10 次', 'menuVisitedCount >= 10', 1),
('menu', 2, '菜单达人', '📋', '累计浏览菜单 50 次', 'menuVisitedCount >= 50', 2),
('menu', 3, '菜单专家', '🗂️', '累计浏览菜单 200 次', 'menuVisitedCount >= 200', 3);

-- 消息成就
INSERT INTO `achievement_tier` (`type`, `level`, `name`, `badge`, `description`, `rule`, `sort`) VALUES
('message', 1, '初识消息', '💬', '累计发送 10 条消息', 'messageSentCount >= 10', 1),
('message', 2, '消息达人', '📣', '累计发送 100 条消息', 'messageSentCount >= 100', 2),
('message', 3, '消息高手', '🎙️', '累计发送 1000 条消息', 'messageSentCount >= 1000', 3);

-- 头像框成就
INSERT INTO `achievement_tier` (`type`, `level`, `name`, `badge`, `description`, `rule`, `sort`) VALUES
('frame', 1, '头像收集者', '🖼️', '拥有 3 个头像框', 'frameOwnedCount >= 3', 1),
('frame', 2, '头像收藏家', '🎨', '拥有 10 个头像框', 'frameOwnedCount >= 10', 2),
('frame', 3, '头像大师', '👑', '拥有 30 个头像框', 'frameOwnedCount >= 30', 3);

SET FOREIGN_KEY_CHECKS = 1;
