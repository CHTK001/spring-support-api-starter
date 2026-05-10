package com.chua.payment.support.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值奖励规则
 */
@Data
@Schema(description = "充值奖励规则")
@TableName("pay_recharge_reward_rule")
public class RechargeRewardRule {

    @TableId(value = "pay_recharge_reward_rule_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("pay_recharge_reward_rule_merchant_id")
    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    /** 规则名称 */
    @TableField("pay_recharge_reward_rule_name")
    @Schema(description = "规则名称", example = "首充送10元券")
    private String name;

    /** 奖励类型: COUPON-赠送优惠券, BALANCE-赠送余额, DISCOUNT-充值折扣 */
    @TableField("pay_recharge_reward_rule_reward_type")
    @Schema(description = "奖励类型: COUPON-赠送优惠券, BALANCE-赠送余额, DISCOUNT-充值折扣", example = "COUPON")
    private String rewardType;

    /** 奖励值: 优惠券模板ID/余额比例/折扣率 */
    @TableField("pay_recharge_reward_rule_reward_value")
    @Schema(description = "奖励值: 优惠券模板ID/余额比例/折扣率", example = "1")
    private String rewardValue;

    /** 最低充值金额（达到才触发奖励） */
    @TableField("pay_recharge_reward_rule_min_amount")
    @Schema(description = "最低充值金额（达到才触发奖励）", example = "100.00")
    private BigDecimal minAmount;

    /** 状态: ENABLED-启用, DISABLED-禁用 */
    @TableField("pay_recharge_reward_rule_status")
    @Schema(description = "状态: ENABLED-启用, DISABLED-禁用", example = "ENABLED")
    private String status;

    @TableField("pay_recharge_reward_rule_description")
    @Schema(description = "描述")
    private String description;

    /** 排序号 */
    @TableField("pay_recharge_reward_rule_sort_order")
    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @TableField(value = "pay_recharge_reward_rule_created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "pay_recharge_reward_rule_updated_at", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
