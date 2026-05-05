-- H2 compatibility: INIT scripts (V1.0-V1.7) fail on ON UPDATE CURRENT_TIMESTAMP
-- This script recreates missing tables with H2-compatible DDL

-- ==================== 1. merchant table ====================
CREATE TABLE IF NOT EXISTS `merchant` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `merchant_no` VARCHAR(32) NOT NULL UNIQUE,
  `merchant_name` VARCHAR(100) NOT NULL,
  `contact_name` VARCHAR(50) DEFAULT NULL,
  `contact_phone` VARCHAR(20) DEFAULT NULL,
  `contact_email` VARCHAR(100) DEFAULT NULL,
  `business_license` VARCHAR(200) DEFAULT NULL,
  `legal_person` VARCHAR(50) DEFAULT NULL,
  `default_notify_url` VARCHAR(255) DEFAULT NULL,
  `default_return_url` VARCHAR(255) DEFAULT NULL,
  `payment_wallet_enabled` TINYINT DEFAULT 0,
  `payment_auto_close_enabled` TINYINT DEFAULT 1,
  `payment_auto_close_minutes` INT DEFAULT 30,
  `payment_split_table_enabled` TINYINT DEFAULT 0,
  `payment_profit_sharing_enabled` TINYINT DEFAULT 0,
  `payment_coupon_enabled` TINYINT DEFAULT 0,
  `remark` VARCHAR(500) DEFAULT NULL,
  `status` TINYINT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_merchant_no ON merchant(merchant_no);
CREATE INDEX IF NOT EXISTS idx_merchant_status ON merchant(status);

-- ==================== 2. merchant_channel table ====================
CREATE TABLE IF NOT EXISTS `merchant_channel` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `merchant_id` BIGINT NOT NULL,
  `channel_type` VARCHAR(32) NOT NULL,
  `channel_sub_type` VARCHAR(32) NOT NULL,
  `channel_name` VARCHAR(100) NOT NULL,
  `app_id` VARCHAR(120) DEFAULT NULL,
  `merchant_no` VARCHAR(120) DEFAULT NULL,
  `api_key` TEXT DEFAULT NULL,
  `private_key` TEXT DEFAULT NULL,
  `public_key` TEXT DEFAULT NULL,
  `cert_path` VARCHAR(255) DEFAULT NULL,
  `sandbox_mode` TINYINT DEFAULT 0,
  `notify_url` VARCHAR(255) DEFAULT NULL,
  `return_url` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT DEFAULT 0,
  `ext_config` TEXT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_channel_merchant_id ON merchant_channel(merchant_id);
CREATE INDEX IF NOT EXISTS idx_channel_type ON merchant_channel(channel_type);
CREATE INDEX IF NOT EXISTS idx_channel_status ON merchant_channel(status);

-- ==================== 3. wallet_account table ====================
CREATE TABLE IF NOT EXISTS `wallet_account` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `merchant_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `available_balance` DECIMAL(18,2) NOT NULL DEFAULT 0,
  `frozen_balance` DECIMAL(18,2) NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_wallet_account_merchant ON wallet_account(merchant_id);
CREATE INDEX IF NOT EXISTS idx_wallet_account_user ON wallet_account(user_id);

-- ==================== 4. wallet_account_log table ====================
CREATE TABLE IF NOT EXISTS `wallet_account_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `merchant_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `biz_type` VARCHAR(32) NOT NULL,
  `biz_no` VARCHAR(64) NOT NULL,
  `change_type` VARCHAR(16) NOT NULL,
  `change_amount` DECIMAL(18,2) NOT NULL,
  `balance_before` DECIMAL(18,2) NOT NULL,
  `balance_after` DECIMAL(18,2) NOT NULL,
  `operator` VARCHAR(100) DEFAULT NULL,
  `remark` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==================== 5. payment_notify_error table ====================
CREATE TABLE IF NOT EXISTS `payment_notify_error` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `notify_log_id` BIGINT NOT NULL,
  `notify_type` VARCHAR(32) NOT NULL,
  `tenant_id` BIGINT DEFAULT NULL,
  `merchant_id` BIGINT DEFAULT NULL,
  `order_no` VARCHAR(64) DEFAULT NULL,
  `refund_no` VARCHAR(64) DEFAULT NULL,
  `error_type` VARCHAR(64) NOT NULL,
  `error_message` TEXT DEFAULT NULL,
  `error_stack` LONGTEXT DEFAULT NULL,
  `request_data` LONGTEXT DEFAULT NULL,
  `retry_count` INT DEFAULT 0,
  `max_retry_count` INT DEFAULT 5,
  `next_retry_time` DATETIME DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL,
  `resolved_at` DATETIME DEFAULT NULL,
  `resolved_by` VARCHAR(64) DEFAULT NULL,
  `remark` VARCHAR(512) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==================== 6. merchant_payment_config table ====================
CREATE TABLE IF NOT EXISTS `merchant_payment_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tenant_id` BIGINT DEFAULT NULL,
  `merchant_id` BIGINT NOT NULL,
  `order_reusable` TINYINT DEFAULT 1,
  `order_timeout_minutes` INT DEFAULT NULL,
  `pending_order_limit` INT DEFAULT NULL,
  `auto_cancel_timeout_order` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `create_by` VARCHAR(64) DEFAULT NULL,
  `update_by` VARCHAR(64) DEFAULT NULL,
  `create_name` VARCHAR(64) DEFAULT NULL,
  `update_name` VARCHAR(64) DEFAULT NULL
);

-- ==================== 8. payment_global_config table ====================
CREATE TABLE IF NOT EXISTS `payment_global_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `config_key` VARCHAR(32) NOT NULL,
  `payment_notify_base_url` VARCHAR(255) DEFAULT NULL,
  `payment_return_url` VARCHAR(255) DEFAULT NULL,
  `payment_callback_path_template` VARCHAR(128) DEFAULT '/{orderNo}/{merchantId}',
  `payment_auto_refresh_seconds` INT DEFAULT 60,
  `remark` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==================== 7. wallet_order table ====================
CREATE TABLE IF NOT EXISTS `wallet_order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL,
  `order_type` VARCHAR(32) NOT NULL,
  `tenant_id` BIGINT DEFAULT NULL,
  `merchant_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `related_user_id` BIGINT DEFAULT NULL,
  `amount` DECIMAL(15,2) NOT NULL,
  `status` VARCHAR(32) NOT NULL,
  `third_party_order_no` VARCHAR(128) DEFAULT NULL,
  `bank_account` VARCHAR(64) DEFAULT NULL,
  `bank_name` VARCHAR(128) DEFAULT NULL,
  `account_name` VARCHAR(64) DEFAULT NULL,
  `request_payload` TEXT DEFAULT NULL,
  `response_payload` TEXT DEFAULT NULL,
  `operator` VARCHAR(64) DEFAULT NULL,
  `remark` VARCHAR(512) DEFAULT NULL,
  `completed_at` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `create_by` VARCHAR(64) DEFAULT NULL,
  `update_by` VARCHAR(64) DEFAULT NULL,
  `create_name` VARCHAR(64) DEFAULT NULL,
  `update_name` VARCHAR(64) DEFAULT NULL
);
