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
    'http://127.0.0.1:8090/api', 'http://127.0.0.1:8855',
    '/{orderNo}/{merchantId}', 30
);

-- 5. 支付渠道：微信沙箱 — NATIVE（扫码支付，无需 openId）
INSERT IGNORE INTO `pay_merchant_channel` (
    `pay_merchant_channel_id`, `pay_merchant_id`,
    `pay_merchant_channel_channel_type`, `pay_merchant_channel_channel_sub_type`,
    `pay_merchant_channel_channel_name`,
    `pay_merchant_channel_sandbox_mode`, `pay_merchant_channel_status`,
    `pay_merchant_channel_app_id`, `pay_merchant_channel_merchant_no`,
    `pay_merchant_channel_api_key`, `pay_merchant_channel_private_key`,
    `pay_merchant_channel_notify_url`,
    `pay_merchant_channel_ext_config`
) VALUES (
    7, 1,
    'WECHAT', 'NATIVE',
    '微信沙箱（扫码）',
    1, 1,
    'wx631110cae53f7736', '1725633524',
    '24c09f7647dcafbfe4b21d8e6ca124b9',
    '-----BEGIN PRIVATE KEY-----
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC+Wvnx2wRP6X2a
BgkQ97P+h+Tlos4nELCA+RNU2noVUGmUYy7XXAOTiQze1QzDVXJWqvzdT25r6I1V
Ple+6VRWbhVlTXHo0ElA0R5lv5JaxySkR+Lb4meqwCZfrXTyI1abB8kHzjSmMrJj
wpl+lq9atNDj/hVBZfoar0LvQldU6LaZcpgzdk1feaR0xTx8NW4GqxxMBBwXAtQw
t2fIQddX75vi40Lj3CXejye87ABlKR+gQ83FMXpXcGtrtkgXZoveGHZkU7KGo9Ug
WR94w/3ZvYvbJ59Jt+Zr61m5sgSGHIHz4K8L82FVczdEJO0ZZ3l/e5L1fusYa6QM
yPYG8o3BAgMBAAECggEAYcRgM3QF1vVZaU5IQcq3S7eIPNlNgIr13IgRAbfklCXk
1C3M1ReRTqlJTvmlwH2WaCUSEqjFUe7zCCEo+AjnJC0OGCWap7SyqvO85Q1roew+
SZISVlUGYZCnJOT195dm1+E7wyKJsALB1LMSGw5RqgjMcQ0C5b/2oNX7Tt+H8zdf
+ADAUyZKJCCqj05XtjhS3Hv3QQSRjm/0Vm/zJQUVH8xJWQIXvKC9h/ccVuOTJNk1
lBDByhgW+Tmk+RbplkEBoQbsf8ux/KEK6WwwDRg5hjlEllGEBKIBcQwbaa2gFmKS
eLOq4XgvsWQ/TcDT/9XGK2k04/d+Rpu0Z5ioz2OrQQKBgQDmkjKuRPdL9eOhfrg+
D6QeW+WJLKLmdK7CF3nJG8vKKaPMNlpgx5pUJKQiisH4jPibOD6AUs5FW+1cNuk9
RkhBTOOBYyC2BSiMLmaaVhtxf798zU7JzgBzlye8SM2eqFyxfzP0+6zw/07AU8G6
nNOQ5aABcnPEuQHMUEsFBfdTbQKBgQDTWVpRRoRhYAMdRrkBrMm0gmkve8W0KovI
TrCSb8xXjjxot24TbxebkeiNxmFSd1LYVJ/NE0ZEhJ5cjFsfV77rbC3jN7azEJ+0
EnmkjSnKdFEHPSMTb68/0iLU+kzDDF2i63SxYntsw9/jcEeuuXgG5GVgjd7uC7z+
QXU1DqMbJQKBgDLVjsjvOZqMtsgXOSWtqyBqYFvy9GNc3F68cERPlYmXzp4qaZim
yTa98VYQQ5257uHB7725A/M0xksrbBbvVOTYv9v57+9RidQGMubaDyjh4mTwmutR
OLZDYCZZ4sOVGQKlKd5cUo+7qjArBy5OfzKV/cXz5uQ7adMS7MdJvnKtAoGBAMbK
lbw60stwbp175wpfRr3lqCBEkDodhqsa8NRcGd0E6WcpmXzoElF+1Tl9Lg9OMvtN
5Z0UExThlL7COfdqJibz8tl0jwEwkrbiZc5UBMnrzEc9xx9B/pnOAntHDyqrF+vl
AjbSxfsQ5ibcdmAvDWmWLJC8AMLxAYw5b5A/O0WNAoGBAJeMbJS5R9kar3Ndy/eX
SMx34jMjJyjZl+8pe/wl93WTXWLvGuy5ok1Ig6NIUdFex/Zn7PB/yxTLqY8h1TSL
5oOVQD7l7jv3hrYQniF4B0X7FGUcQ2aXaMQy2l8Fs9PUtAbU0DdKE8l2XshgAWg8
QjvGV+sU34zYiBIyNJEZ6mib
-----END PRIVATE KEY-----',
    '/api/payment/notify/wechat/7',
    '{"merchantSerialNumber":"32E23D6076C66410662CEC0C928A8B59E5F89272"}'
);
