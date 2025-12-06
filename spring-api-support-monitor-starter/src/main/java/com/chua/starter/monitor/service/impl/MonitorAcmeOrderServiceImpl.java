package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import com.chua.starter.monitor.mapper.MonitorAcmeOrderMapper;
import com.chua.starter.monitor.service.MonitorAcmeOrderService;
import org.springframework.stereotype.Service;

/**
 * ACME 订单服务实现
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-06
 */
@Service
public class MonitorAcmeOrderServiceImpl extends ServiceImpl<MonitorAcmeOrderMapper, MonitorAcmeOrder>
        implements MonitorAcmeOrderService {
}
