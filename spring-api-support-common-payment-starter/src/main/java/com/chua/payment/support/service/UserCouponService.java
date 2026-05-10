package com.chua.payment.support.service;

import com.chua.payment.support.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户优惠券服务
 */
public interface UserCouponService {

    UserCoupon getById(Long id);

    UserCoupon getByCode(String code);

    List<UserCoupon> listByUserId(Long merchantId, Long userId, String status);

    /** 领取优惠券 */
    UserCoupon receive(Long merchantId, Long userId, Long templateId);

    /** 冻结优惠券（下单时锁定） */
    UserCoupon freeze(String code, String orderNo);

    /** 使用优惠券（支付成功后确认使用） */
    UserCoupon use(String code);

    /** 退回优惠券（退款时退回） */
    UserCoupon returnBack(String code);

    /** 计算优惠券减免金额 */
    BigDecimal calculateDiscount(UserCoupon coupon, BigDecimal orderAmount);
}
