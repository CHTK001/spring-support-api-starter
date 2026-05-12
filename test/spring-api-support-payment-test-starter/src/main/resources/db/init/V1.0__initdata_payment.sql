-- ==========================================================
-- 支付系统初始化数据：示例商户 + 钱包
-- 使用 INSERT IGNORE 保证幂等，已存在则跳过
-- ==========================================================

-- 1. 商户
INSERT IGNORE INTO `pay_merchant` (
    `pay_merchant_id`, `pay_merchant_no`, `pay_merchant_name`,
    `pay_merchant_contact_name`, `pay_merchant_contact_phone`,
    `pay_merchant_payment_wallet_enabled`,
    `pay_merchant_payment_auto_close_enabled`, `pay_merchant_payment_auto_close_minutes`,
    `pay_merchant_payment_split_table_enabled`, `pay_merchant_payment_profit_sharing_enabled`,
    `pay_merchant_payment_coupon_enabled`, `pay_merchant_status`
) VALUES (
    1, 'M202605090000000001', '示例商户',
    '张三', '13800138001',
    1, 0, 30, 0, 0, 0, 1
);

-- 2. 支付渠道：钱包
INSERT IGNORE INTO `pay_merchant_channel` (
    `pay_merchant_channel_id`, `pay_merchant_id`,
    `pay_merchant_channel_channel_type`, `pay_merchant_channel_channel_sub_type`,
    `pay_merchant_channel_channel_name`,
    `pay_merchant_channel_sandbox_mode`, `pay_merchant_channel_status`
) VALUES (
    6, 1,
    'WALLET', 'BALANCE',
    '站内钱包',
    0, 1
);

-- 3. 钱包账户（用户ID=1）
INSERT IGNORE INTO `pay_wallet_account` (
    `pay_wallet_account_id`, `pay_wallet_account_merchant_id`,
    `pay_wallet_account_user_id`, `pay_wallet_account_available_balance`,
    `pay_wallet_account_frozen_balance`, `pay_wallet_account_status`
) VALUES (
    4, 1,
    1, 1000.00,
    0.00, 1
);

-- 4. 全局配置：回调基础地址
INSERT IGNORE INTO `pay_global_config` (
    `pay_global_config_id`, `pay_global_config_config_key`,
    `pay_global_config_notify_base_url`, `pay_global_config_return_url`,
    `pay_global_config_callback_path_template`, `pay_global_config_auto_refresh_seconds`
) VALUES (
    1, 'DEFAULT',
    'http://127.0.0.1:8085/payment/api', 'http://127.0.0.1:8085',
    '/{orderNo}/{merchantId}', 30
);
