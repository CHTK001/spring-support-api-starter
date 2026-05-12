package com.chua.payment.support.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板视图对象
 *
 * @author CH
 * @since 2026-05-11
 */
@Data
public class CouponTemplateVO implements Serializable {

    private Long id;

    /** 优惠券编码 */
    private String couponCode;

    /** 优惠券名称 */
    private String couponName;

    /** 优惠券类型: CASH-现金抵扣 */
    private String couponType;

    /** 折扣类型: FIXED-固定金额, PERCENT-折扣比例 */
    private String discountType;

    /** 优惠值（固定金额时为金额，折扣比例时为百分比数值） */
    private BigDecimal discountValue;

    /** 使用门槛（0 表示无门槛） */
    private BigDecimal minAmount;

    /** 发行总量 */
    private Integer totalCount;

    /** 已使用数量 */
    private Integer usedCount;

    /** 有效期开始时间 */
    private LocalDateTime startTime;

    /** 有效期结束时间 */
    private LocalDateTime endTime;

    /** 状态: 1-启用, 0-停用 */
    private Integer status;

    /** 商户ID */
    private Long merchantId;

    /** 商户名称 */
    private String merchantName;

    /** 描述 */
    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
