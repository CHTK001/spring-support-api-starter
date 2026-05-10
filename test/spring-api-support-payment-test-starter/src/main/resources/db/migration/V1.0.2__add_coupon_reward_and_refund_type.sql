-- ==========================================================
-- 支付系统新增表：优惠券模板、用户优惠券、充值奖励规则
-- 遵循 pay_ 前缀 + 表名.字段 命名规范
-- ==========================================================

-- 1. 优惠券模板
CREATE TABLE IF NOT EXISTS `pay_coupon_template` (
    `pay_coupon_template_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `pay_coupon_template_name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    `pay_coupon_template_type` VARCHAR(32) NOT NULL COMMENT '类型: DISCOUNT-折扣, REDUCE-满减, CASH-代金券',
    `pay_coupon_template_discount_rate` DECIMAL(5,4) DEFAULT NULL COMMENT '折扣率（折扣券用，如 0.8000 表示八折）',
    `pay_coupon_template_reduce_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '减免金额（满减券/代金券用）',
    `pay_coupon_template_condition_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '使用门槛（满多少可用，null 表示无门槛）',
    `pay_coupon_template_total_count` INT DEFAULT 0 COMMENT '发行总量（0 表示不限量）',
    `pay_coupon_template_used_count` INT DEFAULT 0 COMMENT '已使用数量',
    `pay_coupon_template_valid_days` INT DEFAULT NULL COMMENT '有效期天数（从领取日起）',
    `pay_coupon_template_status` VARCHAR(16) DEFAULT 'ENABLED' COMMENT '状态: ENABLED-启用, DISABLED-禁用',
    `pay_coupon_template_merchant_id` BIGINT NOT NULL COMMENT '商户ID',
    `pay_coupon_template_description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `pay_coupon_template_created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_coupon_template_updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_coupon_template_merchant` (`pay_coupon_template_merchant_id`),
    INDEX `idx_coupon_template_status` (`pay_coupon_template_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- 2. 用户优惠券
CREATE TABLE IF NOT EXISTS `pay_user_coupon` (
    `pay_user_coupon_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `pay_user_coupon_template_id` BIGINT NOT NULL COMMENT '模板ID',
    `pay_user_coupon_user_id` BIGINT NOT NULL COMMENT '用户ID',
    `pay_user_coupon_merchant_id` BIGINT NOT NULL COMMENT '商户ID',
    `pay_user_coupon_code` VARCHAR(64) NOT NULL COMMENT '优惠券编码（唯一）',
    `pay_user_coupon_status` VARCHAR(16) DEFAULT 'UNUSED' COMMENT '状态: UNUSED-未使用, FROZEN-冻结, USED-已使用, EXPIRED-已过期, RETURNED-已退回',
    `pay_user_coupon_discount_rate` DECIMAL(5,4) DEFAULT NULL COMMENT '折扣率（冗余自模板）',
    `pay_user_coupon_reduce_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '减免金额（冗余自模板）',
    `pay_user_coupon_condition_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '使用门槛（冗余自模板）',
    `pay_user_coupon_order_no` VARCHAR(64) DEFAULT NULL COMMENT '冻结/使用的订单号',
    `pay_user_coupon_frozen_time` DATETIME DEFAULT NULL COMMENT '冻结时间',
    `pay_user_coupon_used_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `pay_user_coupon_expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `pay_user_coupon_created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_user_coupon_updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_coupon_code` (`pay_user_coupon_code`),
    INDEX `idx_user_coupon_user` (`pay_user_coupon_user_id`),
    INDEX `idx_user_coupon_merchant` (`pay_user_coupon_merchant_id`),
    INDEX `idx_user_coupon_status` (`pay_user_coupon_status`),
    INDEX `idx_user_coupon_template` (`pay_user_coupon_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 3. 充值奖励规则
CREATE TABLE IF NOT EXISTS `pay_recharge_reward_rule` (
    `pay_recharge_reward_rule_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `pay_recharge_reward_rule_merchant_id` BIGINT NOT NULL COMMENT '商户ID',
    `pay_recharge_reward_rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `pay_recharge_reward_rule_reward_type` VARCHAR(32) NOT NULL COMMENT '奖励类型: COUPON-赠送优惠券, BALANCE-赠送余额, DISCOUNT-充值折扣',
    `pay_recharge_reward_rule_reward_value` VARCHAR(128) NOT NULL COMMENT '奖励值: 优惠券模板ID/余额比例/折扣率',
    `pay_recharge_reward_rule_min_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '最低充值金额（达到才触发奖励）',
    `pay_recharge_reward_rule_status` VARCHAR(16) DEFAULT 'ENABLED' COMMENT '状态: ENABLED-启用, DISABLED-禁用',
    `pay_recharge_reward_rule_description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `pay_recharge_reward_rule_sort_order` INT DEFAULT 0 COMMENT '排序号',
    `pay_recharge_reward_rule_created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_recharge_reward_rule_updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_reward_rule_merchant` (`pay_recharge_reward_rule_merchant_id`),
    INDEX `idx_reward_rule_status` (`pay_recharge_reward_rule_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值奖励规则表';

-- 4. 支付订单表新增字段：此项由 API 端点处理，SQL 迁移仅处理表创建
