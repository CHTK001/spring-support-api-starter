-- ==========================================================
-- 补丁: 支付全局配置新增优惠券提现控制字段 + 提现审批表
-- ==========================================================

-- 1. pay_global_config 增加 coupon_deduct_withdrawable
ALTER TABLE `pay_global_config`
    ADD COLUMN `pay_global_config_coupon_deduct_withdrawable` TINYINT DEFAULT 1 COMMENT '优惠券抵扣部分是否可提现: 0-不可提现(冻结), 1-可提现';

-- 2. 钱包提现审批表
CREATE TABLE IF NOT EXISTS `pay_wallet_withdraw_approval` (
    `pay_wallet_withdraw_approval_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `pay_wallet_withdraw_approval_merchant_id` BIGINT NOT NULL COMMENT '商户ID',
    `pay_wallet_withdraw_approval_user_id` BIGINT NOT NULL COMMENT '用户ID',
    `pay_wallet_withdraw_approval_amount` DECIMAL(18,2) NOT NULL COMMENT '提现金额',
    `pay_wallet_withdraw_approval_bank_account` VARCHAR(64) NOT NULL COMMENT '银行账号',
    `pay_wallet_withdraw_approval_bank_name` VARCHAR(100) DEFAULT NULL COMMENT '银行名称',
    `pay_wallet_withdraw_approval_account_name` VARCHAR(100) DEFAULT NULL COMMENT '账户名',
    `pay_wallet_withdraw_approval_withdraw_no` VARCHAR(64) NOT NULL COMMENT '提现流水号',
    `pay_wallet_withdraw_approval_status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待审批, APPROVED-已通过, REJECTED-已拒绝',
    `pay_wallet_withdraw_approval_operator` VARCHAR(64) DEFAULT NULL COMMENT '审批操作人',
    `pay_wallet_withdraw_approval_remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `pay_wallet_withdraw_approval_approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `pay_wallet_withdraw_approval_created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_wallet_withdraw_approval_updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_approval_merchant_user` (`pay_wallet_withdraw_approval_merchant_id`, `pay_wallet_withdraw_approval_user_id`),
    INDEX `idx_approval_status` (`pay_wallet_withdraw_approval_status`),
    INDEX `idx_approval_withdraw_no` (`pay_wallet_withdraw_approval_withdraw_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包提现审批表';
