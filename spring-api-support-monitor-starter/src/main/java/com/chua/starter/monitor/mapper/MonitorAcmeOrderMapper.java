package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * ACME 订单 Mapper，提供 monitor_acme_order 表的基础增删改查能力。
 *
 * @since 2025-10-17
 * @author CH
 */
@Mapper
public interface MonitorAcmeOrderMapper extends BaseMapper<MonitorAcmeOrder> {
}
