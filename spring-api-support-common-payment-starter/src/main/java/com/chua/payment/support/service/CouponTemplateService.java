package com.chua.payment.support.service;

import com.chua.payment.support.entity.CouponTemplate;

import java.util.List;

/**
 * 优惠券模板服务
 */
public interface CouponTemplateService {

    CouponTemplate getById(Long id);

    List<CouponTemplate> listByMerchantId(Long merchantId, String status);

    CouponTemplate create(CouponTemplate template);

    CouponTemplate update(CouponTemplate template);

    boolean delete(Long id);

    /** 增加已使用数量 */
    boolean incrementUsedCount(Long id);
}
