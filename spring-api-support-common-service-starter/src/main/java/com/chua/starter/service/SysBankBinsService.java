package com.chua.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.entity.SysBankBins;

import java.util.List;

/**
 * 银行卡bin
 * @author CH
 * @since 2024/12/6
 */
public interface SysBankBinsService extends IService<SysBankBins> {


    /**
     * bin
     *
     * @param bankBin bankBin
     * @return SysBankBins
     */
    ReturnResult<List<SysBankBins>> bin(String bankBin);


    /**
     * list
     *
     * @return List<SysBankBins>
     */
    List<SysBankBins> all();
}
