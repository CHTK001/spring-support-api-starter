SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sign_in_record`;
CREATE TABLE `sign_in_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `sign_date` DATE NOT NULL COMMENT '签到日期',
    `points` INT NOT NULL DEFAULT 0 COMMENT '本次签到获得积分',
    `streak` INT NOT NULL DEFAULT 1 COMMENT '连续签到天数',
    `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_sign_date` (`user_id`, `sign_date`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_sign_date` (`sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到记录表';

DROP TABLE IF EXISTS `sign_in_points_rule`;
CREATE TABLE `sign_in_points_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` VARCHAR(64) NOT NULL COMMENT '规则类型：DAILY_SIGN / STREAK_BONUS_3 / STREAK_BONUS_7 / STREAK_BONUS_30',
    `points` INT NOT NULL COMMENT '奖励积分',
    `description` VARCHAR(256) NULL DEFAULT NULL COMMENT '规则描述',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到积分规则表';

DROP TABLE IF EXISTS `sign_in_points_record`;
CREATE TABLE `sign_in_points_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `sign_in_record_id` BIGINT NOT NULL COMMENT '关联签到记录ID',
    `rule_id` BIGINT NOT NULL COMMENT '关联积分规则ID',
    `points` INT NOT NULL COMMENT '本次积分',
    `create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_sign_in_record_id` (`sign_in_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到积分流水表';

-- 默认积分规则
INSERT INTO `sign_in_points_rule` (`type`, `points`, `description`, `status`) VALUES
('DAILY_SIGN', 10, '每日签到基础积分', 1),
('STREAK_BONUS_3', 5, '连续签到3天额外奖励', 1),
('STREAK_BONUS_7', 10, '连续签到7天额外奖励', 1),
('STREAK_BONUS_30', 50, '连续签到30天额外奖励', 1);

SET FOREIGN_KEY_CHECKS = 1;