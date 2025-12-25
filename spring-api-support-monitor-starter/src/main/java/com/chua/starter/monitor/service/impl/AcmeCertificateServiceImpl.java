package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.acme.support.AcmeCertificateResult;
import com.chua.acme.support.AcmeConnectionResult;
import com.chua.acme.support.AcmeProvider;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.oauth.CurrentUser;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import com.chua.starter.monitor.mapper.MonitorAcmeAccountMapper;
import com.chua.starter.monitor.mapper.MonitorAcmeCertificateMapper;
import com.chua.starter.monitor.mapper.MonitorAcmeOrderMapper;
import com.chua.starter.monitor.pojo.acme.AcmeApplyCertRequest;
import com.chua.starter.monitor.pojo.acme.AcmeCertificateDetailVo;
import com.chua.starter.monitor.pojo.acme.CertStats;
import com.chua.starter.monitor.properties.AcmeProperties;
import com.chua.starter.monitor.service.acme.AcmeCertificateAsyncService;
import com.chua.starter.monitor.service.acme.AcmeCertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ACME 证书服务实现
 *
 * 作者: CH
 * 创建时间: 2025-10-17
 * 版本: 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcmeCertificateServiceImpl implements AcmeCertificateService {

    private final MonitorAcmeAccountMapper accountMapper;
    private final MonitorAcmeCertificateMapper certificateMapper;
    private final MonitorAcmeOrderMapper orderMapper;
    private final AcmeProperties acmeProperties;
    private final AuthService authService;
    private final AcmeCertificateAsyncService asyncService;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 保存/更新 ACME 账户 ForCert
     *
     * @param account 账户
     * @return 结果（账户ID）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> saveAccountForCert(MonitorAcmeAccount account) {
        if (account == null || StringUtils.isNullOrEmpty(account.getAcmeAccountEmail())) {
            return ReturnResult.illegal("参数错误: acmeAccountEmail 不能为空");
        }
        if (StringUtils.isNullOrEmpty(account.getAcmeAccountServer())) {
            return ReturnResult.illegal("参数错误: acmeAccountServer 不能为空");
        }
        log.info("[ACME] 保存账户: email={}, server={}", account.getAcmeAccountEmail(), account.getAcmeAccountServer());
        if (account.getAcmeAccountId() == null) {
            accountMapper.insert(account);
        } else {
            accountMapper.updateById(account);
        }
        return ReturnResult.ok(account.getAcmeAccountId());
    }

    /**
     * 申请证书 ForCert（异步执行，立即返回）
     *
     * @param request 请求
     * @return 证书ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> applyCertificateForCert(AcmeApplyCertRequest request) {
        if (request == null || request.getAcmeAccountId() == null || StringUtils.isNullOrEmpty(request.getPrimaryDomain())) {
            return ReturnResult.illegal("参数错误: acmeAccountId/primaryDomain 不能为空");
        }
        // 查询账户信息
        MonitorAcmeAccount account = accountMapper.selectById(request.getAcmeAccountId());
        if (account == null) {
            return ReturnResult.illegal("ACME账户不存在: " + request.getAcmeAccountId());
        }

        List<String> sans = request.getSanDomains() == null ? List.of() : request.getSanDomains();
        String sanStr = String.join(",", sans.stream().filter(StringUtils::isNotEmpty).toList());
        String challenge = StringUtils.defaultString(request.getChallengeType(), "HTTP-01");

        // 新建证书记录（pending）
        MonitorAcmeCertificate cert = new MonitorAcmeCertificate();
        cert.setAcmeAccountId(request.getAcmeAccountId());
        cert.setAcmeCertPrimaryDomain(request.getPrimaryDomain());
        cert.setAcmeCertSan(sanStr);
        cert.setAcmeCertChallengeType(challenge);
        cert.setAcmeCertStatus("pending");
        certificateMapper.insert(cert);
        int certId = cert.getAcmeCertId();

        log.info("[ACME] 提交证书申请, certId={}, domain={}, san={}, challenge={}", certId, cert.getAcmeCertPrimaryDomain(), sanStr, challenge);

        // 获取当前用户ID用于消息推送
        Integer userId = null;
        try {
            CurrentUser currentUser = authService.getCurrentUser();
            if (currentUser != null && currentUser.getUserId() != null) {
                userId = Integer.parseInt(currentUser.getUserId());
            }
        } catch (Exception e) {
            log.warn("[ACME] 获取当前用户失败，消息推送可能受影响: {}", e.getMessage());
        }

        // 异步执行证书申请
        asyncService.applyCertificateAsync(certId, request, userId);

        return ReturnResult.ok(certId, "证书申请已提交，请在消息中心查看进度");
    }

    /**
     * 续签证书 ForCert
     *
     * @param certId 证书ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> renewCertificateForCert(Integer certId) {
        MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
        if (cert == null) {
            return ReturnResult.illegal("证书不存在: " + certId);
        }
        MonitorAcmeAccount account = accountMapper.selectById(cert.getAcmeAccountId());
        if (account == null) {
            return ReturnResult.illegal("ACME账户不存在: " + cert.getAcmeAccountId());
        }
        log.info("[ACME] 续签证书: id={}, domain={}", certId, cert.getAcmeCertPrimaryDomain());

        // 通过 SPI 获取 ACME 提供者
        AcmeProvider provider = ServiceProvider.of(AcmeProvider.class).getNewExtension("acme4j");
        if (provider == null) {
            return ReturnResult.illegal("ACME 服务提供者不可用");
        }

        // 连接 ACME 服务器（支持 EAB，ZeroSSL 等 CA 需要）
        AcmeConnectionResult connResult = provider.connect(
                account.getAcmeAccountServer(),
                account.getAcmeAccountEmail(),
                account.getAcmeAccountPrivateKeyPem(),
                account.getAcmeAccountEabKid(),
                account.getAcmeAccountEabHmacKey()
        );
        if (!connResult.isSuccess()) {
            provider.close();
            return ReturnResult.illegal("连接 ACME 服务器失败: " + connResult.getErrorMessage());
        }

        // 续签证书
        List<String> domains = buildDomains(cert);
        String challenge = cert.getAcmeCertChallengeType();
        try {
            AcmeCertificateResult certResult = provider.renewCertificate(domains, challenge);
            if (certResult.isSuccess()) {
                fillCertificateSuccess(cert, certResult);
                certificateMapper.updateById(cert);
                log.info("[ACME] 续签成功: id={}, domain={}", certId, cert.getAcmeCertPrimaryDomain());
                return ReturnResult.ok(true, "续签成功");
            } else {
                cert.setAcmeCertLastError(certResult.getErrorMessage());
                certificateMapper.updateById(cert);
                return ReturnResult.illegal("续签失败: " + certResult.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("[ACME] 续签失败: id={}", certId, e);
            cert.setAcmeCertLastError(shortMsg(e));
            certificateMapper.updateById(cert);
            return ReturnResult.illegal("续签失败: " + e.getMessage());
        } finally {
            provider.close();
        }
    }

    /**
     * 吊销证书 ForCert
     *
     * @param certId 证书ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> revokeCertificateForCert(Integer certId) {
        MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
        if (cert == null) {
            return ReturnResult.illegal("证书不存在: " + certId);
        }
        MonitorAcmeAccount account = accountMapper.selectById(cert.getAcmeAccountId());
        if (account == null) {
            return ReturnResult.illegal("ACME账户不存在: " + cert.getAcmeAccountId());
        }
        log.warn("[ACME] 吊销证书: id={}, domain={}", certId, cert.getAcmeCertPrimaryDomain());

        // 通过 SPI 获取 ACME 提供者
        AcmeProvider provider = ServiceProvider.of(AcmeProvider.class).getNewExtension("acme4j");
        if (provider == null) {
            return ReturnResult.illegal("ACME 服务提供者不可用");
        }

        // 连接 ACME 服务器
        AcmeConnectionResult connResult = provider.connect(
                account.getAcmeAccountServer(),
                account.getAcmeAccountEmail(),
                account.getAcmeAccountPrivateKeyPem()
        );
        if (!connResult.isSuccess()) {
            provider.close();
            return ReturnResult.illegal("连接 ACME 服务器失败: " + connResult.getErrorMessage());
        }

        try {
            boolean revoked = provider.revokeCertificate(cert.getAcmeCertChainPem());
            if (revoked) {
                cert.setAcmeCertStatus("revoked");
                certificateMapper.updateById(cert);
                log.info("[ACME] 证书吊销成功: id={}", certId);
                return ReturnResult.ok(true);
            } else {
                return ReturnResult.illegal("吊销失败");
            }
        } catch (Exception e) {
            log.error("[ACME] 吊销失败: id={}", certId, e);
            return ReturnResult.illegal("吊销失败: " + e.getMessage());
        } finally {
            provider.close();
        }
    }

    /**
     * 获取证书详情 ForCert（含账户和订单信息）
     *
     * @param certId 证书ID
     * @return 证书详情VO
     */
    @Override
    public ReturnResult<AcmeCertificateDetailVo> getCertificateDetailForCert(Integer certId) {
        MonitorAcmeCertificate cert = certificateMapper.selectById(certId);
        if (cert == null) {
            return ReturnResult.illegal("证书不存在: " + certId);
        }

        AcmeCertificateDetailVo vo = new AcmeCertificateDetailVo();
        vo.setCertificate(cert);

        // 查询关联账户
        if (cert.getAcmeAccountId() != null) {
            vo.setAccount(accountMapper.selectById(cert.getAcmeAccountId()));
        }

        // 查询关联订单
        vo.setOrders(orderMapper.selectList(new LambdaQueryWrapper<MonitorAcmeOrder>()
                .eq(MonitorAcmeOrder::getAcmeCertId, certId)
                .orderByDesc(MonitorAcmeOrder::getUpdateTime)));

        // 计算到期天数
        if (StringUtils.isNotEmpty(cert.getAcmeCertNotAfter())) {
            try {
                LocalDateTime notAfter = LocalDateTime.parse(cert.getAcmeCertNotAfter(), DATE);
                long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), notAfter);
                vo.setDaysUntilExpiry(days);
                vo.setSoonExpiring(days <= 30 && days >= 0);
            } catch (Exception e) {
                log.warn("[ACME] 解析证书到期时间失败: certId={}, notAfter={}", certId, cert.getAcmeCertNotAfter());
            }
        }

        return ReturnResult.ok(vo);
    }

    /**
     * 获取证书统计信息 ForCert
     *
     * @return 统计信息
     */
    @Override
    public ReturnResult<CertStats> getCertStatsForCert() {
        CertStats stats = new CertStats();

        // 账户总数
        stats.setAccountCount(accountMapper.selectCount(null));

        // 证书总数
        stats.setCertCount(certificateMapper.selectCount(null));

        // 按状态统计
        stats.setValidCount(certificateMapper.selectCount(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, "valid")));
        stats.setPendingCount(certificateMapper.selectCount(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .in(MonitorAcmeCertificate::getAcmeCertStatus, "pending", "validating")));
        stats.setRevokedCount(certificateMapper.selectCount(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, "revoked")));
        stats.setFailedCount(certificateMapper.selectCount(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, "failed")));

        // 统计即将到期和已过期的证书
        String now = LocalDateTime.now().format(DATE);
        String soon = LocalDateTime.now().plusDays(30).format(DATE);

        stats.setExpiringCount(certificateMapper.selectCount(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, "valid")
                .ge(MonitorAcmeCertificate::getAcmeCertNotAfter, now)
                .le(MonitorAcmeCertificate::getAcmeCertNotAfter, soon)));

        stats.setExpiredCount(certificateMapper.selectCount(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, "valid")
                .lt(MonitorAcmeCertificate::getAcmeCertNotAfter, now)));

        return ReturnResult.ok(stats);
    }

    /**
     * 检查并续签到期前 N 天的证书（调度）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndRenewSoonExpired() {
        int days = Math.max(acmeProperties.getRenewDaysBefore(), 7);
        String deadline = LocalDate.now().plusDays(days).atStartOfDay().format(DATE);
        List<MonitorAcmeCertificate> list = certificateMapper.selectList(new LambdaQueryWrapper<MonitorAcmeCertificate>()
                .eq(MonitorAcmeCertificate::getAcmeCertStatus, "valid")
                .le(MonitorAcmeCertificate::getAcmeCertNotAfter, deadline)
        );
        for (MonitorAcmeCertificate it : list) {
            try {
                log.info("[ACME] 到期前{}天自动续签: id={}, domain={}, notAfter={}", days, it.getAcmeCertId(), it.getAcmeCertPrimaryDomain(), it.getAcmeCertNotAfter());
                renewCertificateForCert(it.getAcmeCertId());
            } catch (Exception e) {
                log.error("[ACME] 自动续签失败, id={}", it.getAcmeCertId(), e);
            }
        }
    }

    // ======================== 内部工具方法 ========================

    /**
     * 构建域名列表（主域名 + SAN）
     *
     * @param cert 证书实体
     * @return 域名列表
     */
    private List<String> buildDomains(MonitorAcmeCertificate cert) {
        List<String> domains = new ArrayList<>();
        domains.add(cert.getAcmeCertPrimaryDomain());
        if (StringUtils.isNotEmpty(cert.getAcmeCertSan())) {
            for (String s : cert.getAcmeCertSan().split(",")) {
                if (StringUtils.isNotEmpty(s)) {
                    domains.add(s.trim());
                }
            }
        }
        return domains;
    }

    /**
     * 成功签发/续签后回填证书信息到实体
     *
     * @param target 目标证书实体
     * @param src    证书申请结果
     */
    private void fillCertificateSuccess(MonitorAcmeCertificate target, AcmeCertificateResult src) {
        target.setAcmeCertStatus("valid");
        if (src.getNotBefore() != null) {
            target.setAcmeCertNotBefore(src.getNotBefore().format(DATE));
        }
        if (src.getNotAfter() != null) {
            target.setAcmeCertNotAfter(src.getNotAfter().format(DATE));
            // 计划续签日期：到期前 N 天
            LocalDateTime renewAt = src.getNotAfter().minusDays(Math.max(acmeProperties.getRenewDaysBefore(), 7));
            target.setAcmeCertRenewAt(renewAt.format(DATE));
        }
        target.setAcmeCertChainPem(src.getCertificatePem());
        target.setAcmeCertKeyPem(src.getPrivateKeyPem());
        target.setAcmeCertLastError(null);
    }

    /**
     * 提取简短异常信息
     *
     * @param e 异常
     * @return 简短信息
     */
    private String shortMsg(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return "";
        }
        return msg.length() > 512 ? msg.substring(0, 512) : msg;
    }
}
