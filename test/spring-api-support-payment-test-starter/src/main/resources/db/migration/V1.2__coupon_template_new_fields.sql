-- ==========================================================
-- 补丁: 优惠券模板新增字段
-- 为 pay_coupon_template 补充 couponCode、startTime、endTime
-- ==========================================================

-- 依赖 continue-on-error: true，如果列已存在则忽略重复错误
ALTER TABLE `pay_coupon_template`
    ADD COLUMN `pay_coupon_template_code` VARCHAR(64) DEFAULT NULL COMMENT '优惠券编码' AFTER `pay_coupon_template_name`;
ALTER TABLE `pay_coupon_template`
    ADD COLUMN `pay_coupon_template_start_time` DATETIME DEFAULT NULL COMMENT '有效期开始时间' AFTER `pay_coupon_template_valid_days`;
ALTER TABLE `pay_coupon_template`
    ADD COLUMN `pay_coupon_template_end_time` DATETIME DEFAULT NULL COMMENT '有效期结束时间' AFTER `pay_coupon_template_start_time`;
