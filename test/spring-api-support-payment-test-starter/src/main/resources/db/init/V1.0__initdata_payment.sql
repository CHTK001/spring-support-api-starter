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

-- 5. 支付渠道：微信真实网关 — NATIVE（扫码支付，无需 openId）
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
    8, 1,
    'WECHAT', 'NATIVE',
    '微信扫码（真实网关）',
    0, 1,
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
    '/api/payment/notify/wechat/8',
    '{"merchantSerialNumber":"32E23D6076C66410662CEC0C928A8B59E5F89272"}'
);

-- 6. 支付渠道：支付宝沙箱 — 电脑网站支付
-- 私钥已用 AES-128-ECB 加密存储，公钥需从沙箱控制台获取
-- 使用前请登录 https://open.alipay.com/develop/sandbox/app
-- 找到 appId=9021000162612793 → "支付宝公钥" → 更新下方 public_key
INSERT IGNORE INTO `pay_merchant_channel` (
    `pay_merchant_channel_id`, `pay_merchant_id`,
    `pay_merchant_channel_channel_type`, `pay_merchant_channel_channel_sub_type`,
    `pay_merchant_channel_channel_name`,
    `pay_merchant_channel_sandbox_mode`, `pay_merchant_channel_status`,
    `pay_merchant_channel_app_id`,
    `pay_merchant_channel_private_key`,
    `pay_merchant_channel_public_key`,
    `pay_merchant_channel_notify_url`,
    `pay_merchant_channel_ext_config`
) VALUES (
    7, 1,
    'ALIPAY', 'PAY_WEB',
    '支付宝沙箱（电脑网站）',
    1, 1,
    '9021000162612793',
    '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',
    '-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhmbQM7UL/cJ1tC5rkot4
Yrg2+s76hvPWfm618vHsrc9LI1atlh8iFet0k5f9fuxnfGNNrbeo/ji5vCxuBhVD
xhUHW2olBbXZvhkuYO4jLBFCVpsIxRLi/qj7fxP8tOfFRXYzZreE7l+hsYmlsxB8
HvMji6qfw3isDBs6RTUr8VerXdGWA3uEQ6PEEpa4OaO6RNNbLDTYj1IO5yMqd2j6
Q2s+ScNln0Fc+DvM68BM2DMCZvMl2DV/ZiJMLOqA5k7FORSkDwJZkX9HvGpzc2x0
74Nb5KWozdYFHd9ZEweaIwQnX72S/uydPX8TGv6ZEn16qpChhJi4r+0tOrjXFEiQ
OQIDAQAB
-----END PUBLIC KEY-----',
    '/api/notify/alipay/pay/7',
    '{"serverUrl":"https://openapi-sandbox.dl.alipaydev.com/gateway.do","charset":"UTF-8","format":"json","signType":"RSA2"}'
);
