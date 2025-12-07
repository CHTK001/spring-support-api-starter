package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.service.SysBankBinsService;
import com.chua.starter.entity.service.SysBankBins;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 组织机构接口
 *
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "银行卡bin接口")
@RestController
@RequestMapping("/v2/bank")
@RequiredArgsConstructor
public class SysBankBinsController {

    private final SysBankBinsService sysBankBinsService;

    /**
     * 获取银行卡bin信息
     *
     * @param bankBin 银行卡bin
     * @return {@link ReturnResult}<{@link Boolean}>
     */
    @Operation(summary = "获取银行卡bin信息")
    @GetMapping("/bin")
    public ReturnResult<List<SysBankBins>> get(String bankBin) {
        return sysBankBinsService.bin(bankBin);
    }


    /**
     * 所有银行卡bin
     *
     * @return {@link ReturnResult}<{@link List}<{@link SysBankBins}>}
     */
    @Operation(summary = "所有银行卡bin")
    @GetMapping("/all")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:bank:bin:all'", keyGenerator = "customTenantedKeyGenerator")
    public ReturnResult<List<SysBankBins>> all() {
        return ReturnResult.ok(sysBankBinsService.all());
    }
}
