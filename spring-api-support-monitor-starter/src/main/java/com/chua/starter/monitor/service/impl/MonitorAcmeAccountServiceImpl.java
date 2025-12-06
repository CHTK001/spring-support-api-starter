package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.mapper.MonitorAcmeAccountMapper;
import com.chua.starter.monitor.service.MonitorAcmeAccountService;
import org.springframework.stereotype.Service;

/**
 * ACME 账户服务实现
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-06
 */
@Service
public class MonitorAcmeAccountServiceImpl extends ServiceImpl<MonitorAcmeAccountMapper, MonitorAcmeAccount>
        implements MonitorAcmeAccountService {
}
