-- Ensure sample_pay_notify_url and sample_refund_notify_url columns exist
-- Note: table uses pay_global_config_ prefix for all columns
-- Uses IF NOT EXISTS for idempotent execution (MySQL 8.0+)
ALTER TABLE `pay_global_config` ADD COLUMN IF NOT EXISTS `pay_global_config_sample_pay_notify_url` VARCHAR(500) DEFAULT NULL COMMENT '支付回调示例 URL';
ALTER TABLE `pay_global_config` ADD COLUMN IF NOT EXISTS `pay_global_config_sample_refund_notify_url` VARCHAR(500) DEFAULT NULL COMMENT '退款回调示例 URL';
