package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * ACME 订单 Mapper
 *
 * 作者: CH
 * 创建时间: 2025-10-17
 * 版本: 1.0.0
 */
@Mapper
public interface MonitorAcmeOrderMapper extends BaseMapper<MonitorAcmeOrder> {
}
