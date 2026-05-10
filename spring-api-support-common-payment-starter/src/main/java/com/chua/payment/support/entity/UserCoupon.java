package com.chua.payment.support.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券
 */
@Data
@Schema(description = "用户优惠券")
@TableName("pay_user_coupon")
public class UserCoupon {

    @TableId(value = "pay_user_coupon_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("pay_user_coupon_template_id")
    @Schema(description = "优惠券模板ID", example = "1")
    private Long templateId;

    @TableField("pay_user_coupon_user_id")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @TableField("pay_user_coupon_merchant_id")
    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    /** 优惠券编码（唯一，下单时传入） */
    @TableField("pay_user_coupon_code")
    @Schema(description = "优惠券编码", example = "CPN1700000000000")
    private String code;

    /** 状态: UNUSED-未使用, FROZEN-冻结, USED-已使用, EXPIRED-已过期, RETURNED-已退回 */
    @TableField("pay_user_coupon_status")
    @Schema(description = "状态: UNUSED-未使用, FROZEN-冻结, USED-已使用, EXPIRED-已过期, RETURNED-已退回", example = "UNUSED")
    private String status;

    /** 折扣率（冗余自模板） */
    @TableField("pay_user_coupon_discount_rate")
    @Schema(description = "折扣率（冗余自模板）", example = "0.8")
    private BigDecimal discountRate;

    /** 减免金额（冗余自模板） */
    @TableField("pay_user_coupon_reduce_amount")
    @Schema(description = "减免金额（冗余自模板）", example = "10.00")
    private BigDecimal reduceAmount;

    /** 使用门槛（冗余自模板） */
    @TableField("pay_user_coupon_condition_amount")
    @Schema(description = "使用门槛（冗余自模板）", example = "100.00")
    private BigDecimal conditionAmount;

    /** 冻结/使用的订单号 */
    @TableField("pay_user_coupon_order_no")
    @Schema(description = "冻结/使用的订单号")
    private String orderNo;

    /** 冻结时间 */
    @TableField("pay_user_coupon_frozen_time")
    @Schema(description = "冻结时间")
    private LocalDateTime frozenTime;

    /** 使用时间 */
    @TableField("pay_user_coupon_used_time")
    @Schema(description = "使用时间")
    private LocalDateTime usedTime;

    /** 过期时间 */
    @TableField("pay_user_coupon_expire_time")
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @TableField(value = "pay_user_coupon_created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "pay_user_coupon_updated_at", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
