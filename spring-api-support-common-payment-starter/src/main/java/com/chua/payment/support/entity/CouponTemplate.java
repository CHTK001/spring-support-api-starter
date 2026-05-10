package com.chua.payment.support.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板
 */
@Data
@Schema(description = "优惠券模板")
@TableName("pay_coupon_template")
public class CouponTemplate {

    @TableId(value = "pay_coupon_template_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "优惠券名称", example = "新人满减券")
    private String name;

    @Schema(description = "类型: DISCOUNT-折扣, REDUCE-满减, CASH-代金券", example = "REDUCE")
    private String type;

    @Schema(description = "折扣率（折扣券用，如 0.8 表示八折）", example = "0.8")
    private BigDecimal discountRate;

    @Schema(description = "减免金额（满减券/代金券用）", example = "10.00")
    private BigDecimal reduceAmount;

    @Schema(description = "使用门槛（满多少可用，null 表示无门槛）", example = "100.00")
    private BigDecimal conditionAmount;

    @Schema(description = "发行总量（0 表示不限量）", example = "1000")
    private Integer totalCount;

    @Schema(description = "已使用数量")
    private Integer usedCount;

    @Schema(description = "有效期天数（从领取日起）", example = "30")
    private Integer validDays;

    @Schema(description = "状态: ENABLED-启用, DISABLED-禁用", example = "ENABLED")
    private String status;

    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
