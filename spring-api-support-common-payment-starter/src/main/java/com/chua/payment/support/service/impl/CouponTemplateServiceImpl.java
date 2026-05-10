package com.chua.payment.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.payment.support.entity.CouponTemplate;
import com.chua.payment.support.exception.PaymentException;
import com.chua.payment.support.mapper.CouponTemplateMapper;
import com.chua.payment.support.service.CouponTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 优惠券模板服务实现
 */
@Service
@RequiredArgsConstructor
public class CouponTemplateServiceImpl implements CouponTemplateService {

    private final CouponTemplateMapper couponTemplateMapper;

    @Override
    public CouponTemplate getById(Long id) {
        return couponTemplateMapper.selectById(id);
    }

    @Override
    public List<CouponTemplate> listByMerchantId(Long merchantId, String status) {
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<CouponTemplate>()
                .eq(CouponTemplate::getMerchantId, merchantId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(CouponTemplate::getStatus, status);
        }
        return couponTemplateMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponTemplate create(CouponTemplate template) {
        if (template.getUsedCount() == null) {
            template.setUsedCount(0);
        }
        if (template.getStatus() == null) {
            template.setStatus("ENABLED");
        }
        couponTemplateMapper.insert(template);
        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponTemplate update(CouponTemplate template) {
        couponTemplateMapper.updateById(template);
        return couponTemplateMapper.selectById(template.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        return couponTemplateMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementUsedCount(Long id) {
        CouponTemplate template = couponTemplateMapper.selectById(id);
        if (template == null) {
            throw new PaymentException("优惠券模板不存在");
        }
        int updated = couponTemplateMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<CouponTemplate>()
                        .eq(CouponTemplate::getId, id)
                        .set(CouponTemplate::getUsedCount,
                                template.getUsedCount() != null ? template.getUsedCount() + 1 : 1));
        return updated > 0;
    }
}
