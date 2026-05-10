-- ==========================================================
-- 支付系统表结构（完整）
-- 包含: 全局配置、优惠券模板、用户优惠券、充值奖励规则
-- 遵循 pay_ 前缀 + 表名.字段 命名规范
-- ==========================================================

-- 1. 支付全局配置
CREATE TABLE IF NOT EXISTS `pay_global_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(64) NOT NULL DEFAULT 'DEFAULT' COMMENT '配置键',
    `notify_base_url` VARCHAR(255) DEFAULT NULL COMMENT '回调基础地址',
    `return_url` VARCHAR(255) DEFAULT NULL COMMENT '默认回跳地址',
    `callback_path_template` VARCHAR(255) DEFAULT '/{orderNo}/{merchantId}' COMMENT '回调路径模板',
    `auto_refresh_seconds` INT DEFAULT 60 COMMENT '首页自动刷新秒数',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `sample_pay_notify_url` VARCHAR(500) DEFAULT NULL COMMENT '支付回调示例 URL',
    `sample_refund_notify_url` VARCHAR(500) DEFAULT NULL COMMENT '退款回调示例 URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_global_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付全局配置表';

-- 2. 优惠券模板
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

-- 3. 用户优惠券
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

-- 4. 充值奖励规则
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
