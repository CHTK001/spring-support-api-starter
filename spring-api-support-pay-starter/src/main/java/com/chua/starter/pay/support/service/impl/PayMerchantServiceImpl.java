package com.chua.starter.pay.support.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.ApiCacheKey;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.pay.support.dto.PayMerchantStatisticDTO;
import com.chua.starter.pay.support.entity.PayMerchant;
import com.chua.starter.pay.support.mapper.PayMerchantMapper;
import com.chua.starter.pay.support.service.PayMerchantService;
import com.chua.starter.service.configuration.CustomTenantedKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

/**
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayMerchantServiceImpl extends ServiceImpl<PayMerchantMapper, PayMerchant> implements PayMerchantService {

    final CustomTenantedKeyGenerator customTenantedKeyGenerator;
    private final TransactionTemplate transactionTemplate;

    @Override
    @ApiCacheKey("'sys:pay:merchant' + #merchantCode")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator", unless = "#result == null")
    public ReturnResult<PayMerchant> getOneByCode(String merchantCode) {
        PayMerchant payMerchant = baseMapper.selectOne(Wrappers.<PayMerchant>lambdaQuery().eq(PayMerchant::getPayMerchantCode, merchantCode));
        return null == payMerchant ? ReturnResult.illegal() : ReturnResult.ok(payMerchant);
    }


    @Override
    public PayMerchant savePayMerchant(PayMerchant payMerchant) {
        payMerchant.setPayMerchantDelete(0);
        payMerchant.setPayMerchantStatus(1);
        baseMapper.insert(payMerchant);
        return payMerchant;
    }

    @Override
    public ReturnResult<Boolean> updatePayMerchant(PayMerchant payMerchant) {
        PayMerchant payMerchant1 = baseMapper.selectById(payMerchant.getPayMerchantId());
        payMerchant.setPayMerchantCode(null);
        int updateById = baseMapper.updateById(payMerchant);
        clearCache(payMerchant1);
        return ReturnResult.of(updateById > 0);
    }

    @Override
    public ReturnResult<Boolean> deletePayMerchant(Integer payMerchantId) {
        PayMerchant payMerchant1 = baseMapper.selectById(payMerchantId);
        int updateById = baseMapper.deleteById(payMerchantId);
        clearCache(payMerchant1);
        return ReturnResult.of(updateById > 0);

    }

    @Override
    public IPage<PayMerchant> pageForMerchant(Query<PayMerchant> query, PayMerchant payMerchant) {
        return baseMapper.selectPage(query.createPage(), query.mpjLambda()
                .likeRight(StringUtils.isNotBlank(payMerchant.getPayMerchantName()), PayMerchant::getPayMerchantName, payMerchant.getPayMerchantName())
                .likeRight(StringUtils.isNotBlank(payMerchant.getPayMerchantCode()), PayMerchant::getPayMerchantCode, payMerchant.getPayMerchantCode())
        );
    }

    @Override
    public PayMerchantStatisticDTO statMerchantInfo() {
        return baseMapper.statMerchantInfo();
    }

    /**
     * 清除缓存
     *
     * @param payMerchant payMerchant
     */
    private void clearCache(PayMerchant payMerchant) {
        customTenantedKeyGenerator.evictWithPrefix(REDIS_CACHE_ALWAYS, REDIS_CACHE_ALWAYS, "sys:pay:merchant", payMerchant.getPayMerchantCode());
    }
}
