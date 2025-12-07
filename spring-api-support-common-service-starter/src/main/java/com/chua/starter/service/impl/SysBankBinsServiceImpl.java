package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.service.SysBankBinsService;
import com.chua.starter.service.mapper.SysBankBinsMapper;
import com.chua.starter.entity.service.SysBankBins;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 银行卡BIN服务实现类
 *
 * @author CH
 * @since 2024/12/6
 */
@Service
@RequiredArgsConstructor
public class SysBankBinsServiceImpl extends ServiceImpl<SysBankBinsMapper, SysBankBins> implements SysBankBinsService {

    @Lazy
    @Autowired
    private SysBankBinsService sysBankBinsService;

    /**
     * 根据银行卡号前缀查询对应的银行BIN信息
     *
     * @param bankBin 银行卡号前缀，例如："622202"
     * @return 匹配的银行BIN列表结果
     */
    @Override
    public ReturnResult<List<SysBankBins>> bin(String bankBin) {
        if (!ReturnResult.isSuccess(bankBin)) {
            return ReturnResult.error("银行卡号不能为空");
        }
        return ReturnResult.ok(sysBankBinsService.all().stream()
                .filter(item -> item.getSysBlankBinBin().startsWith(bankBin)).toList());
    }

    /**
     * 获取所有银行BIN信息，并进行缓存
     *
     * @return 所有银行BIN信息列表
     */
    @Override
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:bank:bin:all'", keyGenerator = "customTenantedKeyGenerator")
    public List<SysBankBins> all() {
        return baseMapper.selectList(Wrappers.lambdaQuery());
    }

}
