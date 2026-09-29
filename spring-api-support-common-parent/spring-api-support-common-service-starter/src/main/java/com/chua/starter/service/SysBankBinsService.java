package com.chua.starter.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysBankBins;

import java.util.List;

/**
 * 银行卡bin
 * @since 2024/12/6
 * @author CH
 */
public interface SysBankBinsService extends IService<SysBankBins> {


    /**
     * bin
     *
     * @param bankBin bankbin
     * @return SysBankBins
     */
    ReturnResult<List<SysBankBins>> bin(String bankBin);


    /**
     * 列表
     *
     * @return List<SysBankBins>
     */
    List<SysBankBins> all();
}
