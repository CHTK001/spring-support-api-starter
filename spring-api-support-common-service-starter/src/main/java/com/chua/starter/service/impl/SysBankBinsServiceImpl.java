package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.ApiCacheKey;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.service.SysBankBinsService;
import com.chua.starter.service.mapper.SysBankBinsMapper;
import com.chua.starter.system.entity.SysBankBins;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 *
 * @since 2024/12/6
 * @author CH    
 */
@Service
@RequiredArgsConstructor
public class SysBankBinsServiceImpl extends ServiceImpl<SysBankBinsMapper, SysBankBins> implements SysBankBinsService {

    @Lazy
    @Autowired
    private SysBankBinsService sysBankBinsService;

    @Override
    public ReturnResult<List<SysBankBins>> bin(String bankBin) {
        if (ReturnResult.isSuccess(bankBin)) {
            return ReturnResult.error("银行卡号不能为空");
        }
        return ReturnResult.ok(sysBankBinsService.all().stream()
                .filter(item -> item.getSysBlankBinBin().startsWith(bankBin)).toList());
    }

    @Override
    @ApiCacheKey("'sys:bank:bin:all'")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public List<SysBankBins> all() {
        return baseMapper.selectList(Wrappers.lambdaQuery());
    }

}
