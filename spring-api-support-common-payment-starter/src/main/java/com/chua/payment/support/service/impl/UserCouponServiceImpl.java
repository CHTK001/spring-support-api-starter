package com.chua.payment.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chua.payment.support.entity.CouponTemplate;
import com.chua.payment.support.entity.UserCoupon;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.mapper.UserCouponMapper;
import com.chua.payment.support.service.CouponTemplateService;
import com.chua.payment.support.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 用户优惠券服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl implements UserCouponService {

    private final UserCouponMapper userCouponMapper;
    private final CouponTemplateService couponTemplateService;

    @Override
    public UserCoupon getById(Long id) {
        return userCouponMapper.selectById(id);
    }

    @Override
    public UserCoupon getByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getCode, code)
                .last("limit 1"));
    }

    @Override
    public List<UserCoupon> listByUserId(Long merchantId, Long userId, String status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getMerchantId, merchantId)
                .eq(UserCoupon::getUserId, userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(UserCoupon::getStatus, status);
        }
        return userCouponMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCoupon receive(Long merchantId, Long userId, Long templateId) {
        CouponTemplate template = couponTemplateService.getById(templateId);
        if (template == null) {
            throw new PaymentException("优惠券模板不存在");
        }
        if (!"ENABLED".equals(template.getStatus())) {
            throw new PaymentException("优惠券已停用");
        }
        if (template.getTotalCount() != null && template.getUsedCount() != null
                && template.getUsedCount() >= template.getTotalCount()) {
            throw new PaymentException("优惠券已领完");
        }

        String code = "CPN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        UserCoupon coupon = new UserCoupon();
        coupon.setTemplateId(templateId);
        coupon.setUserId(userId);
        coupon.setMerchantId(merchantId);
        coupon.setCode(code);
        coupon.setStatus("UNUSED");
        coupon.setDiscountRate(template.getDiscountRate());
        coupon.setReduceAmount(template.getReduceAmount());
        coupon.setConditionAmount(template.getConditionAmount());

        if (template.getValidDays() != null && template.getValidDays() > 0) {
            coupon.setExpireTime(LocalDateTime.now().plusDays(template.getValidDays()));
        }

        userCouponMapper.insert(coupon);
        return coupon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCoupon freeze(String code, String orderNo) {
        UserCoupon coupon = getByCode(code);
        if (coupon == null) {
            throw new PaymentException("优惠券不存在");
        }
        if (!"UNUSED".equals(coupon.getStatus())) {
            throw new PaymentException("优惠券状态不正确");
        }
        if (coupon.getExpireTime() != null && coupon.getExpireTime().isBefore(LocalDateTime.now())) {
            expireCoupon(coupon);
            throw new PaymentException("优惠券已过期");
        }

        coupon.setStatus("FROZEN");
        coupon.setOrderNo(orderNo);
        coupon.setFrozenTime(LocalDateTime.now());
        userCouponMapper.updateById(coupon);
        return coupon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCoupon use(String code) {
        UserCoupon coupon = getByCode(code);
        if (coupon == null) {
            throw new PaymentException("优惠券不存在");
        }
        if (!"FROZEN".equals(coupon.getStatus())) {
            throw new PaymentException("优惠券状态不正确，当前状态: " + coupon.getStatus());
        }

        coupon.setStatus("USED");
        coupon.setUsedTime(LocalDateTime.now());
        userCouponMapper.updateById(coupon);

        if (coupon.getTemplateId() != null) {
            couponTemplateService.incrementUsedCount(coupon.getTemplateId());
        }
        return coupon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCoupon returnBack(String code) {
        UserCoupon coupon = getByCode(code);
        if (coupon == null) {
            return null;
        }
        if (!"FROZEN".equals(coupon.getStatus()) && !"USED".equals(coupon.getStatus())) {
            log.warn("优惠券状态不可退回: code={}, status={}", code, coupon.getStatus());
            return coupon;
        }

        coupon.setStatus("UNUSED");
        coupon.setOrderNo(null);
        coupon.setFrozenTime(null);
        coupon.setUsedTime(null);
        userCouponMapper.updateById(coupon);
        return coupon;
    }

    @Override
    public BigDecimal calculateDiscount(UserCoupon coupon, BigDecimal orderAmount) {
        if (coupon == null || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (coupon.getConditionAmount() != null && orderAmount.compareTo(coupon.getConditionAmount()) < 0) {
            return BigDecimal.ZERO;
        }
        String type = resolveCouponType(coupon);
        return switch (type) {
            case "DISCOUNT" -> {
                BigDecimal rate = coupon.getDiscountRate() != null ? coupon.getDiscountRate() : BigDecimal.ONE;
                if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) > 0) {
                    rate = BigDecimal.ONE;
                }
                yield orderAmount.multiply(BigDecimal.ONE.subtract(rate));
            }
            case "REDUCE", "CASH" ->
                    coupon.getReduceAmount() != null ? coupon.getReduceAmount().min(orderAmount) : BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    private String resolveCouponType(UserCoupon coupon) {
        if (coupon.getDiscountRate() != null && coupon.getDiscountRate().compareTo(BigDecimal.ZERO) > 0
                && coupon.getDiscountRate().compareTo(BigDecimal.ONE) < 0) {
            return "DISCOUNT";
        }
        if (coupon.getReduceAmount() != null && coupon.getReduceAmount().compareTo(BigDecimal.ZERO) > 0) {
            return "REDUCE";
        }
        return "CASH";
    }

    private void expireCoupon(UserCoupon coupon) {
        userCouponMapper.update(null, new LambdaUpdateWrapper<UserCoupon>()
                .eq(UserCoupon::getId, coupon.getId())
                .eq(UserCoupon::getStatus, "UNUSED")
                .set(UserCoupon::getStatus, "EXPIRED"));
    }
}
