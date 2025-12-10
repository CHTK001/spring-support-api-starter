package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.mapper.MonitorAcmeCertificateMapper;
import com.chua.starter.monitor.service.acme.MonitorAcmeCertificateService;
import org.springframework.stereotype.Service;

/**
 * ACME 证书服务实现
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-06
 */
@Service
public class MonitorAcmeCertificateServiceImpl extends ServiceImpl<MonitorAcmeCertificateMapper, MonitorAcmeCertificate>
        implements MonitorAcmeCertificateService {
}
