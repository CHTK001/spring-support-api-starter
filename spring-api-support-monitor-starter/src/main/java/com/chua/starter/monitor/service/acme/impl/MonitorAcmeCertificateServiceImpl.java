package com.chua.starter.monitor.service.acme;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.mapper.MonitorAcmeCertificateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * ACME 证书服务实现
 *
 * @author CH
 * @since 4.0.0.42
 */
@Service
public class MonitorAcmeCertificateServiceImpl
        extends ServiceImpl<MonitorAcmeCertificateMapper, MonitorAcmeCertificate>
        implements MonitorAcmeCertificateService {

    @Override
    public List<MonitorAcmeCertificate> listByAccountId(Integer accountId) {
        if (accountId == null) {
            return Collections.emptyList();
        }
        return list(Wrappers.<MonitorAcmeCertificate>lambdaQuery()
                .eq(MonitorAcmeCertificate::getAcmeAccountId, accountId)
                .orderByDesc(MonitorAcmeCertificate::getCreateTime));
    }

    @Override
    public List<MonitorAcmeCertificate> listByDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return Collections.emptyList();
        }
        return list(Wrappers.<MonitorAcmeCertificate>lambdaQuery()
                .eq(MonitorAcmeCertificate::getAcmeCertPrimaryDomain, domain)
                .or().like(MonitorAcmeCertificate::getAcmeCertSan, domain));
    }

    @Override
    public List<MonitorAcmeCertificate> listExpiring(long now, long expireWindowMs) {
        long cutoff = now + expireWindowMs;
        return list(Wrappers.<MonitorAcmeCertificate>lambdaQuery()
                .isNotNull(MonitorAcmeCertificate::getAcmeCertNotAfter)
                .apply("UNIX_TIMESTAMP(STR_TO_DATE(acme_cert_not_after, '%Y-%m-%dT%H:%i:%sZ')) * 1000 < {0}", cutoff)
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, "issued"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonitorAcmeCertificate saveOrUpdateCert(MonitorAcmeCertificate cert) {
        if (cert == null) {
            return null;
        }
        if (cert.getAcmeCertId() == null) {
            cert.setCreateTime(LocalDateTime.now());
            cert.setUpdateTime(LocalDateTime.now());
            save(cert);
            return cert;
        }
        cert.setUpdateTime(LocalDateTime.now());
        boolean ok = updateById(cert);
        return ok ? cert : null;
    }

    @Override
    public boolean updateStatus(Integer certId, String newStatus) {
        if (certId == null || newStatus == null) {
            return false;
        }
        MonitorAcmeCertificate update = new MonitorAcmeCertificate();
        update.setAcmeCertId(certId);
        update.setAcmeCertStatus(newStatus);
        update.setUpdateTime(LocalDateTime.now());
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeCert(Integer certId) {
        if (certId == null) {
            return false;
        }
        return removeById(certId);
    }

    @Override
    public long countCerts() {
        return count();
    }
}
