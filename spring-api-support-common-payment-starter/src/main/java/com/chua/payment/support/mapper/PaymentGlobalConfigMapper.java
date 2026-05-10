package com.chua.payment.support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.payment.support.entity.PaymentGlobalConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付全局配置 Mapper
 */
@Mapper
public interface PaymentGlobalConfigMapper extends BaseMapper<PaymentGlobalConfig> {
}
