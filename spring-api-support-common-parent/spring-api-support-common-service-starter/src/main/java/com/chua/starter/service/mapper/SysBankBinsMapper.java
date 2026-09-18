package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.service.SysBankBins;
import org.apache.ibatis.annotations.Mapper;

/**
 * 银行卡BIN信息数据访问层接口
 *
 * @since 2024/12/6
 * @author CH
 */
@Mapper
public interface SysBankBinsMapper extends BaseMapper<SysBankBins> {

}