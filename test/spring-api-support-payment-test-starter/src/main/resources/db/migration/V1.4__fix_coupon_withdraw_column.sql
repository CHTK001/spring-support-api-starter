-- ==========================================================
-- 补丁: 支付全局配置新增优惠券提现控制字段(修复列名)
-- ==========================================================

-- 添加正确的列名(带前缀，匹配实体映射)
ALTER TABLE `pay_global_config`
    ADD COLUMN `pay_global_config_coupon_deduct_withdrawable` TINYINT DEFAULT 1 COMMENT '优惠券抵扣部分是否可提现: 0-不可提现(冻结), 1-可提现';
